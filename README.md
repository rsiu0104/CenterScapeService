# CenterScape Service

CenterScape is an Data Center Management software. It has an HTTP REST API.

This service is design to consume the HTTP Post and put the event on the Event Bus.


## Build

```
mvn clean package
```

## Run

```
java -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true
```
## Run with Debug

```
java -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true
```
