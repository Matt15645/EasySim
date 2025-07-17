# stock-management system/data-provider/main.py
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel
import shioaji as sj
import os
from dotenv import load_dotenv

# 在應用程式啟動時載入 .env 檔案中的環境變數
load_dotenv()

API_KEY = os.getenv("API_KEY")
SECRET_KEY = os.getenv("SECRET_KEY")
CA_CERT_PATH=os.getenv("CA_CERT_PATH"),
CA_PASSWORD=os.getenv("CA_PASSWORD"),
if not API_KEY or not SECRET_KEY:
    print("❌ 錯誤: 未找到 API Key 或 Secret Key，請確認已正確設定環境變數或 .env 檔案。")
    exit(1)
    
app = FastAPI(
    title="Data Provider Service",
    description="提供金融數據。",
    version="0.1.0"
)

api = sj.Shioaji(simulation = True)   # Chose Mode

accounts = api.login(
    api_key = API_KEY,         
    secret_key = SECRET_KEY   
)

api.activate_ca(
        ca_path = CA_CERT_PATH[0],
        ca_passwd = CA_PASSWORD[0],
)

