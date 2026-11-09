# Etapa 1: build com Maven
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Baixa as dependências primeiro, em uma camada separada, para aproveitar o cache do Docker
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q clean package -DskipTests

# Etapa 2: imagem final só com JRE, rodando com usuário sem privilégios
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
