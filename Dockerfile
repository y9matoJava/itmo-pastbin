# Этап 1: Сборка
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Копируем maven wrapper и pom.xml отдельно для кэширования зависимостей
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Копируем исходники и собираем JAR (без тестов)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Этап 2: Запуск (минимальный образ)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаём непривилегированного пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
