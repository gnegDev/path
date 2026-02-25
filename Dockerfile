# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache Maven dependencies in a separate layer.
# Re-runs only when pom.xml changes.
COPY pom.xml ./
RUN mvn dependency:go-offline -B -q

# Compile and package (tests are skipped; run them in CI separately).
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/path-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
