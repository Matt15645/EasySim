# stock-management/data-provider/Dockerfile
FROM python:3.10-slim-bullseye

# 設定容器內的工作目錄
WORKDIR /app

# 防止 pip 緩存，減少最終映像大小
ENV PIP_NO_CACHE_DIR=off

# 設置shioaji日誌檔案路徑到臨時目錄
ENV SJ_LOG_PATH=/tmp/shioaji.log

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 將 requirements.txt 複製到容器中，並優先安裝依賴
# 這樣做可以利用 Docker 層緩存，如果 requirements.txt 沒有改變，就不會重新安裝依賴
COPY requirements.txt .
RUN pip install -r requirements.txt

# 將應用程式碼複製到容器中
# .dockerignore 檔案會排除不需要複製的內容（例如 venv/）
COPY . .
# COPY Sinopac.pfx /app/Sinopac.pfx

# 創建一個非root用戶並設置正確的權限
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
RUN chmod -R 755 /app

# 確保日誌檔案可以被寫入
RUN touch /app/shioaji.log && chown appuser:appuser /app/shioaji.log

# 切換到非root用戶
USER appuser

# 定義容器監聽的端口
EXPOSE 8000

# 定義容器啟動時執行的命令
# 使用 gunicorn (搭配 uvicorn.workers.UvicornWorker) 是生產環境運行 ASGI 應用程式的常見方式
# --bind 0.0.0.0:8000 綁定所有網路介面
# --workers 1  (對於有狀態的 API 連線，使用單一 worker)
# main:app 表示運行 main.py 檔案中的 app 實例
CMD ["gunicorn", "main:app", "--workers", "1", "--bind", "0.0.0.0:8000", "-k", "uvicorn.workers.UvicornWorker"]