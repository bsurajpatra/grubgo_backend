# Use a stable Maven with OpenJDK 11 for building
FROM maven:3.8.6-openjdk-11 AS builder

# Set JAVA_HOME and ensure Maven uses JDK 11
ENV JAVA_HOME=/usr/local/openjdk-11
RUN echo $JAVA_HOME

COPY . .
RUN mvn clean package -DskipTests

# Use a slim OpenJDK 11 image for running the application
FROM openjdk:11-jdk-slim
COPY --from=builder /target/AaharExpress_B-0.0.1-SNAPSHOT.jar AaharExpress_B.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "AaharExpress_B.jar"]
