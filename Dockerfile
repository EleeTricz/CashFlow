# Usa imagem leve com Java 17
FROM eclipse-temurin:17-jdk-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado para dentro do container
COPY target/cashflow-0.0.1-SNAPSHOT.jar app.jar

# Exponha a porta que a aplicação usa
EXPOSE 8080

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]
