# CenterScape Service

CenterScape is an Data Center Management software. It has an HTTP REST API.

This service is design to
(1) periodically get all entity from CS and
(2) update entity's detected location.

## Customization at CenterScape
1. Created "EPC" asset attribute with Record Value Changes = true, Values Are Unique = true
1. Created "INVENTORY_TAKE_ID" asset attribute with Record Value Changes = true, Values Are Unique = true.
    * This is an unique ID of the inventory take.
    * The ID is make up of READER_NAME + USER + TIMESTAMP
1. Created "LAST_INVENTORY_TAKE_TIME" asset attribute  with Record Value Changes = true.
1. Add "EPC" asset attribute to "Equipment" asset type and "Summary - Location" asset type


## Build

```
mvn clean package
```

## Build and Debug (with config)

```
mvn compile vertx:debug -Dvertx.runArgs="-cluster -Djava.net.preferIPv4Stack=true -conf src\conf\config.json"
```

## Run

```
java -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true -conf src\conf\config.json
```
## Run with Logging

```
java -Djava.util.logging.config.file=src\conf\logging.properties -jar target\CenterScapeService-1.0-SNAPSHOT.original.jar --cluster -Djava.net.preferIPv4Stack=true -conf src\conf\config.json
```
