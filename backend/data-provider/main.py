# stock-management system/data-provider/main.py
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel
import shioaji as sj
import os
from dotenv import load_dotenv
from typing import List, Optional
from datetime import datetime

# 在應用程式啟動時載入 .env 檔案中的環境變數
load_dotenv()

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

accounts = api.login(
    api_key = API_KEY,         
    secret_key = SECRET_KEY   
)

if CA_CERT_PATH and CA_PASSWORD:
    api.activate_ca(ca_path=CA_CERT_PATH, ca_passwd=CA_PASSWORD)
else:
    print("⚠️ CA 憑證路徑或密碼未設定，略過憑證啟用。")

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

class TickRequest(BaseModel):
    symbols: List[str]
    dates: List[str]  # ["2025-07-20", "2025-07-21", ...]
    
# 只回傳原始資料，不做任何計算
@app.get("/api/positions", response_model=PositionResponse)
async def get_positions():
    try:
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

@app.post("/api/ticks")
def get_ticks(req: TickRequest):
    result = {}
    for symbol in req.symbols:
        result[symbol] = []
        for date in req.dates:
            try:
                ticks = api.ticks(
                    contract=api.Contracts.Stocks[symbol],
                    date=date,
                    query_type=sj.constant.TicksQueryType.LastCount,
                    last_cnt=1,
                )
                if ticks and hasattr(ticks, 'close') and ticks.close:
                    result[symbol].append({
                        "date": date,
                        "close": ticks.close[0] if isinstance(ticks.close, list) else ticks.close,
                        "ts": ticks.ts[0] if isinstance(ticks.ts, list) else ticks.ts
                    })
            except Exception as e:
                print(f"取得 {symbol} 在 {date} 的資料時發生錯誤: {e}")
                # 繼續處理下一個日期，不中斷整個流程
                continue
    return result

# 新增健康檢查端點
@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "data-provider"}