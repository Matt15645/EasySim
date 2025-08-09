# 使用 OpenJDK 17 作為基礎映像
FROM openjdk:17-jdk-slim

# 安裝 curl 用於健康檢查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 設定工作目錄
WORKDIR /app

# 定義 JAR 文件的路徑
ARG JAR_FILE=target/account_service-0.0.1-SNAPSHOT.jar

# 複製 JAR 文件到容器中
COPY ${JAR_FILE} app.jar

# 暴露端口
EXPOSE 8082

# 設定環境變數
ENV SPRING_PROFILES_ACTIVE=docker

# 定義容器啟動時的執行命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]