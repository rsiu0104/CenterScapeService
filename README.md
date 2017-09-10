# CenterScape Service

CenterScape is an Data Center Management software. It has an HTTP REST API.

This service is design to consume the HTTP Post and put the event on the Event Bus.


## Build

```
mvn clean package
```

## Build and Debug

```
mvn compile vertx:debug -Dvertx.runArgs="-cluster -Djava.net.preferIPv4Stack=true -Djava.util.logging.config.file=src\conf\logging.properties"
```

## Run

```
java -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true
```
## Run with Logging

```
java -Djava.util.logging.config.file=src\conf\logging.properties -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true
```
