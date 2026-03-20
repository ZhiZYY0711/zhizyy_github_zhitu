# 多阶段构建
FROM node:20-alpine AS builder

WORKDIR /app

# 安装pnpm
RUN npm install -g pnpm

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .

# 构建时注入网关地址（容器内用服务名）
ARG VITE_API_BASE_URL=http://gateway:8888
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN pnpm run build

# 生产阶段：nginx 反向代理
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
