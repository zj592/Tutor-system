# syntax=docker/dockerfile:1

# ---------- 阶段一：编译打包 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 使用国内镜像源，避免容器内拉依赖超时
COPY maven-settings.xml /root/.m2/settings.xml

# 先只拷 pom 再拷源码：只改 Java 代码时这一层缓存可以命中，不必重下依赖
COPY pom.xml .
RUN mvn -B -s /root/.m2/settings.xml dependency:go-offline -DskipTests || true

COPY src ./src
RUN mvn -B -s /root/.m2/settings.xml clean package -DskipTests

# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# 只把编好的 jar 从上一阶段搬过来，最终镜像不含源码与 Maven
COPY --from=builder /build/target/tutor-system-1.0.0.jar /app/app.jar

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Shanghai"

EXPOSE 8080

# JRE 镜像里没有 curl/wget，用 bash 自带的 /dev/tcp 探活
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD bash -c '</dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
