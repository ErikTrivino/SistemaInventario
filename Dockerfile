# Etapa 1: Construcción
FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Construimos el JAR saltando los tests para agilizar la imagen
RUN gradle bootJar -x test --no-daemon

# Etapa 2: Ejecución
FROM openjdk:21-jdk-slim
WORKDIR /app
EXPOSE 8080

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /home/gradle/src/build/libs/SistemadeInventario-0.0.1-SNAPSHOT.jar app.jar

# Configuración de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
