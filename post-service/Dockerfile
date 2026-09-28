# syntax=docker/dockerfile:1.7
# Build multi-stage. Contexto de build: raíz del repositorio (ver docker-compose.yml).

# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY shared-kernel shared-kernel
COPY platform-core platform-core
COPY platform-mvc-commons platform-mvc-commons
COPY auth-service auth-service
COPY post-service post-service
COPY feed-service feed-service
COPY api-gateway api-gateway
# La caché de ~/.m2 acelera builds sucesivos (BuildKit)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl post-service -am package -DskipTests \
 && java -Djarmode=tools -jar post-service/target/post-service.jar extract --layers --launcher --destination /workspace/extracted \
 && mkdir -p /workspace/extracted/dependencies /workspace/extracted/spring-boot-loader \
            /workspace/extracted/snapshot-dependencies /workspace/extracted/application

# ---------- Etapa 2: runtime mínimo, usuario no root ----------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
# Capas ordenadas de menos a más cambiantes para aprovechar la caché de Docker
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8082
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8082/actuator/health/liveness || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
