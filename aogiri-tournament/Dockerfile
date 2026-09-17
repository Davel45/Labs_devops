# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy root pom and all module poms to allow Maven multi-module resolution
COPY pom.xml .
COPY aogiri-user/pom.xml aogiri-user/
COPY aogiri-tournament/pom.xml aogiri-tournament/
COPY aogiri-match/pom.xml aogiri-match/
COPY aogiri-gateway/pom.xml aogiri-gateway/
COPY aogiri-app/pom.xml aogiri-app/
COPY aogiri-config-server/pom.xml aogiri-config-server/

# Copy service source code
COPY aogiri-tournament/src aogiri-tournament/src

# Build and package the service
RUN mvn clean package -pl aogiri-tournament -am -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/aogiri-tournament/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
