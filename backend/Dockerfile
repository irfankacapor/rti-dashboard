FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN ./mvnw -B package -DskipTests

EXPOSE 8443

CMD ["sh", "-c", "java -jar target/*.jar"] 