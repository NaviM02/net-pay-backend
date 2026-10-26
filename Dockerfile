# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

# Copiamos primero el wrapper y el pom para aprovechar la cache de dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -DskipTests

# Copiamos el codigo y empaquetamos el jar
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S app && adduser -S app -G app
RUN apk add --no-cache curl
USER app

COPY --from=build --chown=app:app /workspace/target/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/health > /dev/null || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
