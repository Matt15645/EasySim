# 使用 OpenJDK 17 作為基礎映像
FROM openjdk:17-jdk-slim

# 定義 JAR 文件的路徑
ARG JAR_FILE=target/auth_service-0.0.1-SNAPSHOT.jar

# 複製 JAR 文件到容器中
COPY ${JAR_FILE} app.jar

# 定義容器啟動時的執行命令
ENTRYPOINT ["java", "-jar", "/app.jar"]