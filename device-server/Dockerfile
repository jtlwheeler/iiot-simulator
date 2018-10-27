FROM openjdk:8-jre-alpine

ENV JAR device-server-1.0-SNAPSHOT-all.jar

# Set the location of the verticles
ENV HOME_DIR /usr/app

EXPOSE 12686

# Copy your fat jar to the container
COPY build/libs/$JAR $HOME_DIR/

WORKDIR $HOME_DIR
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $JAR"]