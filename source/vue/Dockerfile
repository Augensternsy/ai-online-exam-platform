# ===================================================================
# 前端镜像：管理端 + 学生端双端构建，Nginx 统一托管并反向代理后端
# 构建上下文：source/vue
# ===================================================================

FROM node:18-alpine AS builder
WORKDIR /build

# webpack4 在 OpenSSL 3 下需启用 legacy provider（npm 脚本中的 set 为 Windows 语法，由 ENV 提供）
ENV NODE_OPTIONS=--openssl-legacy-provider

# ---------- 管理端 ----------
COPY xzs-admin/package*.json ./xzs-admin/
RUN cd xzs-admin && npm install --registry=https://registry.npmmirror.com
COPY xzs-admin ./xzs-admin
RUN cd xzs-admin && npm run build

# ---------- 学生端 ----------
COPY xzs-student/package*.json ./xzs-student/
RUN cd xzs-student && npm install --registry=https://registry.npmmirror.com
COPY xzs-student ./xzs-student
RUN cd xzs-student && npm run build

# ---------- Nginx 运行时 ----------
FROM nginx:alpine

# 管理端产物（outputDir: admin）
COPY --from=builder /build/xzs-admin/admin /usr/share/nginx/html/admin
# 学生端产物（outputDir: student）
COPY --from=builder /build/xzs-student/student /usr/share/nginx/html/student

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
