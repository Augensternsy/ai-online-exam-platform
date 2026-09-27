# 前端 Dockerfile - Vue.js + Nginx 静态资源托管
# 多阶段构建

# 第一阶段：构建 Vue 项目
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 第二阶段：Nginx 静态资源托管
FROM nginx:alpine

# 复制构建产物到 Nginx 目录
COPY --from=builder /build/student /usr/share/nginx/html/student

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
