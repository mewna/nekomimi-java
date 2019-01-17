FROM maven:3.5.3-jdk-8

COPY . /app
WORKDIR /app
RUN mvn -B -q clean package

FROM openjdk:8-jre-slim
COPY --from=0 /app/target/nekomimi*.jar /app/nekomimi.jar

ENTRYPOINT ["/usr/bin/java", "-Xms128M", "-Xmx2048M", "-jar", "/app/nekomimi.jar"]