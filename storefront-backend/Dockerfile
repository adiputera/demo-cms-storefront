FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy parent pom and shared module
COPY pom.xml .
COPY shared-entities ./shared-entities
COPY storefront-backend ./storefront-backend

# Build the application
RUN mvn clean package -pl storefront-backend -am -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/storefront-backend/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
