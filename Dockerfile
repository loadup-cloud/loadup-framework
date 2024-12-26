FROM maven:3.9.8-eclipse-temurin-17
WORKDIR /app
COPY . .
COPY settings.xml /root/.m2/settings.xml

WORKDIR /app/loadup-dependencies
RUN mvn clean install -DskipTests

WORKDIR /app
RUN mvn clean install -DskipTests


CMD ["echo", "Use 'docker run' to build and deploy Maven project"]
