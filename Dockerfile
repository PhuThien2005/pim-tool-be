# Stage 1: Build mã nguồn bằng Maven
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests && cp target/*.[jw]ar /app/app.jar

# Stage 2: Runtime với JRE siêu nhẹ
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar

# Khống chế RAM tối đa 350MB để không bị Render kill container (gói Free có 512MB RAM)
ENV JAVA_TOOL_OPTIONS="-Xmx350m -Xms256m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
