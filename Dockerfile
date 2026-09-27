# ===================================================================
# 后端镜像：Spring Boot + Java 8，Maven 多阶段构建
# 构建上下文：项目根目录（docker compose 默认配置）
# ===================================================================

# ---------- 阶段一：Maven 构建 ----------
FROM maven:3.8-openjdk-8 AS builder
WORKDIR /build

# 先复制 pom，利用 Docker 缓存预下载依赖
COPY source/xzs/pom.xml ./
RUN mvn -B dependency:go-offline

# 复制源码并打包（跳过测试，测试用例需要外部数据库环境）
COPY source/xzs/src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ---------- 阶段二：运行时 ----------
FROM eclipse-temurin:8-jre
WORKDIR /app

COPY --from=builder /build/target/xzs-3.9.0.jar app.jar
RUN mkdir -p /app/log

EXPOSE 8000

ENV JAVA_OPTS="-XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# 容器内探活：仅检测 8000 端口可连接（镜像不安装 curl）
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
  CMD bash -c 'echo > /dev/tcp/localhost/8000' || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
