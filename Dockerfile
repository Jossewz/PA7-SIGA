FROM eclipse-temurin:21-jdk

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sh ./mvnw -q -DskipTests dependency:go-offline

COPY src src

EXPOSE 8080

CMD ["sh", "./mvnw", "spring-boot:run"]
