FROM eclipse-temurin:21-jdk

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src

EXPOSE 8080

CMD ["sh", "./mvnw", "spring-boot:run"]
