# 建構階段
FROM node:20-alpine AS build

WORKDIR /app

# 接收建置參數
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# 運行階段
FROM nginx:alpine

# 將 build 出來的靜態檔案複製到 nginx 預設目錄
COPY --from=build /app/build /usr/share/nginx/html

# 可選：自訂 nginx 設定
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
