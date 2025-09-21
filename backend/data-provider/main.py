# stock-management system/data-provider/main.py
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel
import shioaji as sj
import os
from dotenv import load_dotenv
from typing import List, Optional, Dict
import asyncio
from datetime import datetime, timedelta
from collections import defaultdict
import logging

# 在應用程式啟動時載入 .env 檔案中的環境變數
load_dotenv()

# 設定日誌
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

API_KEY = os.getenv("API_KEY")
SECRET_KEY = os.getenv("SECRET_KEY")
CA_CERT_PATH=os.getenv("CA_CERT_PATH")
CA_PASSWORD=os.getenv("CA_PASSWORD")

if not API_KEY or not SECRET_KEY:
    print("❌ 錯誤: 未找到 API Key 或 Secret Key，請確認已正確設定環境變數或 .env 檔案。")
    exit(1)
    
app = FastAPI(
    title="Data Provider Service",
    description="提供金融數據。",
    version="0.1.0"
)

api = sj.Shioaji(simulation = False)   # Chose Mode
last_login_time = None

def ensure_api_connection():
    """確保 API 連線有效，如果無效則重新登入"""
    global last_login_time
    
    try:
        # 簡單測試連線是否有效
        api.list_positions(unit=sj.constant.Unit.Share)
        return True
    except:
        # 連線無效，嘗試重新登入
        logger.info("API 連線已失效，嘗試重新登入...")
        try:
            accounts = api.login(
                api_key = API_KEY,         
                secret_key = SECRET_KEY   
            )
            
            if CA_CERT_PATH and CA_PASSWORD:
                api.activate_ca(ca_path=CA_CERT_PATH, ca_passwd=CA_PASSWORD)
            
            last_login_time = datetime.now()
            logger.info(f"✅ 重新登入成功，時間: {last_login_time}")
            return True
            
        except Exception as e:
            logger.error(f"❌ 重新登入失敗: {str(e)}")
            return False

# 初始登入
try:
    accounts = api.login(
        api_key = API_KEY,         
        secret_key = SECRET_KEY   
    )
    
    if CA_CERT_PATH and CA_PASSWORD:
        api.activate_ca(ca_path=CA_CERT_PATH, ca_passwd=CA_PASSWORD)
        logger.info("已啟用 CA 憑證")
    else:
        logger.warning("⚠️ CA 憑證路徑或密碼未設定，略過憑證啟用。")
    
    last_login_time = datetime.now()
    logger.info(f"✅ 初始登入成功，時間: {last_login_time}")
    
except Exception as e:
    logger.error(f"❌ 初始登入失敗: {str(e)}")
    exit(1)

# 新增 Pydantic 模型
class Position(BaseModel):
    # id: int
    code: str  # 股票代碼
    # direction: Action  # 買賣方向
    quantity: int  # 持股數量
    avg_price: float  # 成交價
    current_price: float  # 最新成交價
    unrealized_pnl: float  # 損益
    # yd_quantity: int  # 昨日餘額
    # cond: StockOrderCond
    # margin_purchase_amount: int  # 融資金額
    # collateral: int
    # short_sale_margin: int
    # interest: int

class PositionResponse(BaseModel):
    positions: List[Position]  # 原始持股資料列表
    timestamp: datetime  # 資料取得時間

# 新增 HistoricalRequest 模型
class HistoricalRequest(BaseModel):
    symbols: List[str]
    start_date: str  # "2025-01-01"
    end_date: str    # "2025-07-31"

class ScannerRequest(BaseModel):
    scanner_type: str           
    date: str                   
    count: int = 100           
    ascending: bool = False  

# 只回傳原始資料，不做任何計算
@app.get("/api/positions", response_model=PositionResponse)
async def get_positions():
    try:
        # 確保 API 連線有效
        if not ensure_api_connection():
            raise HTTPException(status_code=503, detail="API 連線失敗")
            
        # 取得所有帳戶的持股資訊
        positions_data = api.list_positions(unit=sj.constant.Unit.Share)
        
        # 直接轉換為 DTO，不做任何計算
        positions = []
        for position in positions_data:
            pos = Position(
                code=position.code,
                quantity=position.quantity,
                avg_price=position.price,
                current_price=position.last_price,
                unrealized_pnl=position.pnl
            )
            positions.append(pos)
        
        return PositionResponse(
            positions=positions,
            timestamp=datetime.now()
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"取得持股資料失敗: {str(e)}")

async def get_kbars_async(symbol: str, start: str, end: str) -> Dict:
    loop = asyncio.get_event_loop()
    future = loop.create_future()

    def callback(kbars):
        if not future.done():
            try:
                if kbars and hasattr(kbars, 'Close') and len(kbars.Close) > 0:
                    # 按日期分組
                    daily_data = defaultdict(list)
                    
                    for i in range(len(kbars.Close)):
                        if hasattr(kbars, 'ts') and len(kbars.ts) > i:
                            timestamp_seconds = kbars.ts[i] / 1_000_000_000
                            dt = datetime.fromtimestamp(timestamp_seconds)
                            date_str = dt.strftime("%Y-%m-%d")
                            
                            daily_data[date_str].append({
                                "timestamp": timestamp_seconds,
                                "close": float(kbars.Close[i]),
                                "open": float(kbars.Open[i]) if hasattr(kbars, 'Open') else None,
                                "high": float(kbars.High[i]) if hasattr(kbars, 'High') else None,
                                "low": float(kbars.Low[i]) if hasattr(kbars, 'Low') else None,
                                "volume": int(kbars.Volume[i]) if hasattr(kbars, 'Volume') else None
                            })
                    
                    # 取每日最後一筆資料作為收盤價
                    data = []
                    for date_str in sorted(daily_data.keys()):
                        day_records = daily_data[date_str]
                        # 按時間戳排序，取最後一筆
                        day_records.sort(key=lambda x: x["timestamp"])
                        last_record = day_records[-1]
                        
                        # 計算當日的開高低收
                        day_open = day_records[0]["open"]
                        day_high = max(record["high"] for record in day_records if record["high"] is not None)
                        day_low = min(record["low"] for record in day_records if record["low"] is not None)
                        day_close = last_record["close"]  # 當日收盤價
                        day_volume = sum(record["volume"] for record in day_records if record["volume"] is not None)
                        
                        data.append({
                            "date": date_str,
                            "close": day_close,
                            "ts": int(last_record["timestamp"]),
                            "open": day_open,
                            "high": day_high,
                            "low": day_low,
                            "volume": day_volume
                        })
                    
                    print(f"{symbol} 成功取得 {len(data)} 日的收盤資料")
                    future.set_result(data)
                else:
                    print(f"{symbol} 沒有資料")
                    future.set_result([])
                    
            except Exception as e:
                print(f"{symbol} callback 錯誤：{e}")
                future.set_result([])

    try:
        contract = api.Contracts.Stocks[symbol]
        api.kbars(
            contract=contract, 
            start=start, 
            end=end,
            timeout=0,
            cb=callback
        )
        
        result = await asyncio.wait_for(future, timeout=30.0)
        return result
        
    except asyncio.TimeoutError:
        print(f"{symbol} 請求超時")
        return []
    except Exception as e:
        print(f"{symbol} 發生錯誤：{e}")
        return []

@app.post("/api/historical")
async def get_historical_data(req: HistoricalRequest):
    try:
        # 確保 API 連線有效
        if not ensure_api_connection():
            raise HTTPException(status_code=503, detail="API 連線失敗")
            
        # 並行處理所有股票
        tasks = [get_kbars_async(symbol, req.start_date, req.end_date) for symbol in req.symbols]
        results = await asyncio.gather(*tasks, return_exceptions=True)

        # 建立回應格式
        response = {}
        for symbol, result in zip(req.symbols, results):
            if isinstance(result, Exception):
                print(f"{symbol} 處理失敗：{result}")
                response[symbol] = []
            else:
                response[symbol] = result or []

        return response
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"取得歷史資料失敗: {str(e)}")

def get_scanner_type(type_str: str):
    types = {
        "ChangePercentRank": sj.constant.ScannerType.ChangePercentRank,
        "VolumeRank": sj.constant.ScannerType.VolumeRank,
        "AmountRank": sj.constant.ScannerType.AmountRank,
        "ChangePriceRank": sj.constant.ScannerType.ChangePriceRank,
        "DayRangeRank": sj.constant.ScannerType.DayRangeRank,
    }
    return types.get(type_str)

@app.post("/api/scanner")  
def get_scanner_data(request: ScannerRequest):  
    try:
        # 確保 API 連線有效
        if not ensure_api_connection():
            raise HTTPException(status_code=503, detail="API 連線失敗")
            
        scanner_type = get_scanner_type(request.scanner_type)
        if not scanner_type:
            raise HTTPException(status_code=400, detail="不支援的掃描器類型")
        
        results = api.scanners(  
            scanner_type=scanner_type,
            date=request.date,
            count=request.count,
            ascending=request.ascending
        )
        
        return {"data": [item.__dict__ for item in results], "timestamp": datetime.now()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 新增健康檢查端點
@app.get("/health")
async def health_check():
    return {
        "status": "healthy", 
        "service": "data-provider",
        "last_login_time": last_login_time.isoformat() if last_login_time else None
    }