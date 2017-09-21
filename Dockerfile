FROM openjdk:8-jre-alpine

EXPOSE 8083

# Copy your fat jar to the container
COPY target/*SNAPSHOT.jar /app.jar
COPY src/conf/logging.properties /logging.properties
COPY src/conf/config.json /config.json

#ENTRYPOINT ["sh", "-c"]
CMD ["/usr/bin/java", "-Djava.util.logging.config.file=/logging.properties", "-jar", "/app.jar", "--cluster", "-Djava.net.preferIPv4Stack=true", "-Djgroups.tcp.address=NON_LOOPBACK", "-conf", "/config.json"]
#CMD ["exec java -Djava.util.logging.config.file=src\conf\logging.properties -jar $VERTICLE_FILE"]