# stock-management/data-provider/Dockerfile
FROM python:3.10-slim-bullseye

# 設定容器內的工作目錄
WORKDIR /app

# 防止 pip 緩存，減少最終映像大小
ENV PIP_NO_CACHE_DIR=off

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 將 requirements.txt 複製到容器中，並優先安裝依賴
# 這樣做可以利用 Docker 層緩存，如果 requirements.txt 沒有改變，就不會重新安裝依賴
COPY requirements.txt .
RUN pip install -r requirements.txt

# 將應用程式碼複製到容器中
# .dockerignore 檔案會排除不需要複製的內容（例如 venv/）
COPY . .

# 定義容器監聽的端口
EXPOSE 8000

# 定義容器啟動時執行的命令
# 使用 gunicorn (搭配 uvicorn.workers.UvicornWorker) 是生產環境運行 ASGI 應用程式的常見方式
# --bind 0.0.0.0:8000 綁定所有網路介面
# --workers 4  (設定 worker 數量，通常是 2 * CPU 核心數 + 1，或根據負載調整)
# main:app 表示運行 main.py 檔案中的 app 實例
CMD ["gunicorn", "main:app", "--workers", "4", "--bind", "0.0.0.0:8000", "-k", "uvicorn.workers.UvicornWorker"]