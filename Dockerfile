# Etapa 1: Construcción
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (se cachea si no cambia el pom.xml)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instalar procps para pgrep (necesario para healthcheck)
RUN apk add --no-cache procps && \
    rm -rf /var/cache/apk/*

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Health check simple (verifica que el proceso Java esté corriendo)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD pgrep -f "java.*app.jar" || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

