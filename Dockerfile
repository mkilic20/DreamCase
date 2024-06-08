FROM maven:3.8.8-eclipse-temurin-17 AS Builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=Builder /app/target/*.jar /app/application.jar
COPY wait-for-it.sh .
ENTRYPOINT ["./wait-for-it.sh", "case-mysqldb:3306", "--", "java", "-jar", "application.jar"]
