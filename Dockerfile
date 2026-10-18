# Usa o Java 17 (se estiver usando Java 21, mude para eclipse-temurin:21-jdk-alpine)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copia o arquivo .jar compilado para dentro do contêiner
COPY target/*.jar app.jar

# Expõe a porta 8080
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]