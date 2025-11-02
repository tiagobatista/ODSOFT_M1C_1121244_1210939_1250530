# Multi-stage build para otimizar a imagem

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom.xml e fazer download das dependências (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar aplicação (skip tests para build mais rápido)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Criar usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Criar diretório para uploads
RUN mkdir -p /app/uploads-psoft-g1 && chown -R spring:spring /app

# Copiar o JAR da stage de build
COPY --from=build /app/target/*.jar app.jar

# Mudar para usuário não-root
USER spring:spring

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Executar aplicação com profiles ativos
ENTRYPOINT ["java", "-jar", "app.jar"]