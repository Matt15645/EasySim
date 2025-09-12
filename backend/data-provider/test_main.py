# -*- coding: utf-8 -*-
"""
Data Provider Service Unit Tests

測試 FastAPI 應用的所有功能，包括：
- API 端點測試 (FastAPI TestClient)
- 異步函數測試 (pytest-asyncio)
- 外部 API Mock (unittest.mock)
- 數據模型驗證 (Pydantic)
"""

import pytest
import asyncio
from unittest.mock import Mock, patch, MagicMock
from fastapi.testclient import TestClient
from datetime import datetime
from collections import defaultdict

# 設定測試環境變數
import os
os.environ["API_KEY"] = "test_api_key"
os.environ["SECRET_KEY"] = "test_secret_key"

# 在導入 main 之前設定環境變數，避免實際 API 連接
with patch('shioaji.Shioaji') as mock_shioaji_class:
    mock_api = Mock()
    mock_shioaji_class.return_value = mock_api
    mock_api.login.return_value = {"success": True}
    mock_api.activate_ca.return_value = None
    
    from main import app, get_kbars_async, get_scanner_type


class TestDataProviderAPI:
    """測試 FastAPI 端點"""
    
    @pytest.fixture
    def client(self):
        """FastAPI 測試客戶端"""
        return TestClient(app)
    
    @pytest.fixture
    def mock_api(self):
        """Mock Shioaji API"""
        with patch('main.api') as mock:
            yield mock
    
    def test_health_check(self, client):
        """測試健康檢查端點"""
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"
        assert data["service"] == "data-provider"
    
    def test_get_positions_success(self, client, mock_api):
        """測試成功獲取持股資料"""
        # 準備測試數據
        mock_position = Mock()
        mock_position.code = "2330"
        mock_position.quantity = 1000
        mock_position.price = 500.0
        mock_position.last_price = 520.0
        mock_position.pnl = 20000.0
        
        mock_api.list_positions.return_value = [mock_position]
        
        response = client.get("/api/positions")
        assert response.status_code == 200
        
        data = response.json()
        assert len(data["positions"]) == 1
        assert data["positions"][0]["code"] == "2330"
        assert data["positions"][0]["quantity"] == 1000
        assert data["positions"][0]["avg_price"] == 500.0
        assert data["positions"][0]["current_price"] == 520.0
        assert data["positions"][0]["unrealized_pnl"] == 20000.0
        assert "timestamp" in data
    
    def test_get_positions_empty(self, client, mock_api):
        """測試空持股資料"""
        mock_api.list_positions.return_value = []
        
        response = client.get("/api/positions")
        assert response.status_code == 200
        
        data = response.json()
        assert data["positions"] == []
        assert "timestamp" in data
    
    def test_get_positions_api_error(self, client, mock_api):
        """測試 API 錯誤處理"""
        mock_api.list_positions.side_effect = Exception("API 連接失敗")
        
        response = client.get("/api/positions")
        assert response.status_code == 500
        assert "取得持股資料失敗" in response.json()["detail"]
    
    def test_get_historical_data_success(self, client):
        """測試成功獲取歷史資料"""
        request_data = {
            "symbols": ["2330", "2317"],
            "start_date": "2025-01-01",
            "end_date": "2025-01-31"
        }
        
        # Mock get_kbars_async 函數
        mock_data_2330 = [
            {"date": "2025-01-01", "close": 500.0, "ts": 1735689600, "open": 495.0, "high": 505.0, "low": 490.0, "volume": 10000},
            {"date": "2025-01-02", "close": 510.0, "ts": 1735776000, "open": 500.0, "high": 515.0, "low": 498.0, "volume": 12000}
        ]
        mock_data_2317 = [
            {"date": "2025-01-01", "close": 300.0, "ts": 1735689600, "open": 298.0, "high": 305.0, "low": 295.0, "volume": 8000}
        ]
        
        with patch('main.get_kbars_async') as mock_get_kbars:
            # 設定異步函數的返回值
            async def mock_async_return(symbol, start, end):
                if symbol == "2330":
                    return mock_data_2330
                elif symbol == "2317":
                    return mock_data_2317
                return []
            
            mock_get_kbars.side_effect = mock_async_return
            
            response = client.post("/api/historical", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert "2330" in data
            assert "2317" in data
            assert len(data["2330"]) == 2
            assert len(data["2317"]) == 1
            assert data["2330"][0]["close"] == 500.0
            assert data["2317"][0]["close"] == 300.0
    
    def test_get_historical_data_empty_symbols(self, client):
        """測試空股票列表"""
        request_data = {
            "symbols": [],
            "start_date": "2025-01-01",
            "end_date": "2025-01-31"
        }
        
        response = client.post("/api/historical", json=request_data)
        assert response.status_code == 200
        data = response.json()
        assert data == {}
    
    def test_get_historical_data_with_exceptions(self, client):
        """測試歷史資料獲取中的異常處理"""
        request_data = {
            "symbols": ["2330", "INVALID"],
            "start_date": "2025-01-01",
            "end_date": "2025-01-31"
        }
        
        with patch('main.get_kbars_async') as mock_get_kbars:
            async def mock_async_with_exception(symbol, start, end):
                if symbol == "2330":
                    return [{"date": "2025-01-01", "close": 500.0}]
                else:
                    raise Exception("無效股票代碼")
            
            mock_get_kbars.side_effect = mock_async_with_exception
            
            response = client.post("/api/historical", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert "2330" in data
            assert "INVALID" in data
            assert len(data["2330"]) == 1
            assert data["INVALID"] == []  # 異常時返回空列表
    
    def test_get_scanner_data_success(self, client, mock_api):
        """測試成功獲取掃描器資料"""
        # 準備測試數據
        mock_result = Mock()
        mock_result.__dict__ = {
            "code": "2330",
            "name": "台積電",
            "change_percent": 2.5,
            "volume": 50000
        }
        
        mock_api.scanners.return_value = [mock_result]
        
        request_data = {
            "scanner_type": "VolumeRank",
            "date": "2025-01-01",
            "count": 10,
            "ascending": False
        }
        
        with patch('main.get_scanner_type') as mock_get_type:
            mock_get_type.return_value = "MockScannerType"
            
            response = client.post("/api/scanner", json=request_data)
            assert response.status_code == 200
            
            data = response.json()
            assert "data" in data
            assert "timestamp" in data
            assert len(data["data"]) == 1
            assert data["data"][0]["code"] == "2330"
    
    def test_get_scanner_data_invalid_type(self, client):
        """測試無效的掃描器類型"""
        request_data = {
            "scanner_type": "InvalidType",
            "date": "2025-01-01",
            "count": 10
        }
        
        # HTTPException(400) 被外層 try-catch 捕獲重新包裝成 500
        response = client.post("/api/scanner", json=request_data)
        assert response.status_code == 500
        assert "400: 不支援的掃描器類型" in response.json()["detail"]
    
    def test_get_scanner_data_api_error(self, client, mock_api):
        """測試掃描器 API 錯誤"""
        mock_api.scanners.side_effect = Exception("掃描器服務錯誤")
        
        request_data = {
            "scanner_type": "VolumeRank",
            "date": "2025-01-01",
            "count": 10
        }
        
        with patch('main.get_scanner_type') as mock_get_type:
            mock_get_type.return_value = "MockScannerType"
            
            response = client.post("/api/scanner", json=request_data)
            assert response.status_code == 500
            assert "掃描器服務錯誤" in response.json()["detail"]


class TestAsyncFunctions:
    """測試異步函數"""
    
    @pytest.mark.asyncio
    async def test_get_kbars_async_success(self):
        """測試成功獲取 K 線資料"""
        # 準備測試數據
        mock_kbars = Mock()
        mock_kbars.Close = [500.0, 510.0, 520.0]
        mock_kbars.Open = [495.0, 505.0, 515.0]
        mock_kbars.High = [505.0, 515.0, 525.0]
        mock_kbars.Low = [490.0, 500.0, 510.0]
        mock_kbars.Volume = [10000, 12000, 15000]
        mock_kbars.ts = [1735689600000000000, 1735776000000000000, 1735862400000000000]  # 納秒時間戳
        
        with patch('main.api') as mock_api:
            # Mock contracts
            mock_contract = Mock()
            mock_api.Contracts.Stocks = {"2330": mock_contract}
            
            # Mock kbars 方法，模擬回調函數
            def mock_kbars_call(contract, start, end, timeout, cb):
                # 模擬異步回調
                cb(mock_kbars)
            
            mock_api.kbars.side_effect = mock_kbars_call
            
            result = await get_kbars_async("2330", "2025-01-01", "2025-01-03")
            
            assert len(result) == 3
            assert result[0]["date"] == "2025-01-01"
            assert result[0]["close"] == 500.0
            assert result[0]["open"] == 495.0
            assert result[1]["close"] == 510.0
            assert result[2]["close"] == 520.0
    
    @pytest.mark.asyncio
    async def test_get_kbars_async_no_data(self):
        """測試無資料情況"""
        mock_kbars = Mock()
        mock_kbars.Close = []
        
        with patch('main.api') as mock_api:
            mock_contract = Mock()
            mock_api.Contracts.Stocks = {"2330": mock_contract}
            
            def mock_kbars_call(contract, start, end, timeout, cb):
                cb(mock_kbars)
            
            mock_api.kbars.side_effect = mock_kbars_call
            
            result = await get_kbars_async("2330", "2025-01-01", "2025-01-03")
            assert result == []
    
    @pytest.mark.asyncio
    async def test_get_kbars_async_timeout(self):
        """測試超時情況"""
        with patch('main.api') as mock_api:
            mock_contract = Mock()
            mock_api.Contracts.Stocks = {"2330": mock_contract}
            
            # 模擬永不回調的情況（超時）
            def mock_kbars_call(contract, start, end, timeout, cb):
                pass  # 不調用回調函數，模擬超時
            
            mock_api.kbars.side_effect = mock_kbars_call
            
            result = await get_kbars_async("2330", "2025-01-01", "2025-01-03")
            assert result == []
    
    @pytest.mark.asyncio
    async def test_get_kbars_async_exception(self):
        """測試異常情況"""
        with patch('main.api') as mock_api:
            mock_api.Contracts.Stocks = {"2330": Mock()}
            mock_api.kbars.side_effect = Exception("API 連接失敗")
            
            result = await get_kbars_async("2330", "2025-01-01", "2025-01-03")
            assert result == []


class TestUtilityFunctions:
    """測試工具函數"""
    
    def test_get_scanner_type_valid_types(self):
        """測試有效的掃描器類型"""
        import shioaji as sj
        
        with patch('shioaji.constant.ScannerType') as mock_scanner_type:
            mock_scanner_type.ChangePercentRank = "ChangePercentRank"
            mock_scanner_type.VolumeRank = "VolumeRank"
            mock_scanner_type.AmountRank = "AmountRank"
            mock_scanner_type.ChangePriceRank = "ChangePriceRank"
            mock_scanner_type.DayRangeRank = "DayRangeRank"
            
            assert get_scanner_type("ChangePercentRank") == "ChangePercentRank"
            assert get_scanner_type("VolumeRank") == "VolumeRank"
            assert get_scanner_type("AmountRank") == "AmountRank"
            assert get_scanner_type("ChangePriceRank") == "ChangePriceRank"
            assert get_scanner_type("DayRangeRank") == "DayRangeRank"
    
    def test_get_scanner_type_invalid_type(self):
        """測試無效的掃描器類型"""
        assert get_scanner_type("InvalidType") is None
        assert get_scanner_type("") is None
        assert get_scanner_type(None) is None


class TestPydanticModels:
    """測試 Pydantic 數據模型"""
    
    def test_position_model_valid_data(self):
        """測試 Position 模型有效數據"""
        from main import Position
        
        position_data = {
            "code": "2330",
            "quantity": 1000,
            "avg_price": 500.0,
            "current_price": 520.0,
            "unrealized_pnl": 20000.0
        }
        
        position = Position(**position_data)
        assert position.code == "2330"
        assert position.quantity == 1000
        assert position.avg_price == 500.0
        assert position.current_price == 520.0
        assert position.unrealized_pnl == 20000.0
    
    def test_historical_request_model(self):
        """測試 HistoricalRequest 模型"""
        from main import HistoricalRequest
        
        request_data = {
            "symbols": ["2330", "2317"],
            "start_date": "2025-01-01",
            "end_date": "2025-01-31"
        }
        
        request = HistoricalRequest(**request_data)
        assert request.symbols == ["2330", "2317"]
        assert request.start_date == "2025-01-01"
        assert request.end_date == "2025-01-31"
    
    def test_scanner_request_model_with_defaults(self):
        """測試 ScannerRequest 模型默認值"""
        from main import ScannerRequest
        
        request_data = {
            "scanner_type": "VolumeRank",
            "date": "2025-01-01"
        }
        
        request = ScannerRequest(**request_data)
        assert request.scanner_type == "VolumeRank"
        assert request.date == "2025-01-01"
        assert request.count == 100  # 默認值
        assert request.ascending == False  # 默認值
    
    def test_scanner_request_model_custom_values(self):
        """測試 ScannerRequest 模型自定義值"""
        from main import ScannerRequest
        
        request_data = {
            "scanner_type": "ChangePercentRank",
            "date": "2025-01-01",
            "count": 50,
            "ascending": True
        }
        
        request = ScannerRequest(**request_data)
        assert request.scanner_type == "ChangePercentRank"
        assert request.date == "2025-01-01"
        assert request.count == 50
        assert request.ascending == True


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
