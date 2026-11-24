FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY auth-service/pom.xml auth-service/pom.xml
COPY community-service/pom.xml community-service/pom.xml
COPY borrowcircle-service/pom.xml borrowcircle-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY payments-service/pom.xml payments-service/pom.xml
COPY chat-service/pom.xml chat-service/pom.xml
COPY notifications-service/pom.xml notifications-service/pom.xml
COPY reviews-service/pom.xml reviews-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -B -q -N dependency:go-offline
COPY . .
ARG MODULE
RUN mvn -B -pl ${MODULE} -am package -DskipTests
RUN cp ${MODULE}/target/${MODULE}-0.1.0.jar /workspace/service.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/service.jar /app/service.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/service.jar"]
