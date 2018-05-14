# CenterScape Service

CenterScape is an Data Center Management software. It has an HTTP REST API.

This service is designed to
1. Periodically get all entities from CS (getIdentityMap)
1. Update entity's detected location from fixed and mobile readers (putUpdates)

## Customization at CenterScape

### Asset Type/Attributes
1. Created "EPC" asset attribute with Record Value Changes = true, Values Are Unique = true
1. Created "INVENTORY_TAKE_ID" asset attribute with Record Value Changes = true.
    * This is an unique ID of the inventory take.
    * The ID is make up of READER_NAME + USER + TIMESTAMP
1. Created "LAST_INVENTORY_TAKE_TIME" asset attribute  with Record Value Changes = true.
1. Created "FIXED_READER_ID" asset attribute with Record Value Changes = true.
1. Created "INVENTORY_DETECTED_LOCATION" asset attribute with type "Custom Type Reference" and Custom Attribute Type "Location" and Record Value Changes = true.
1. Add "EPC", "INVENTORY_TAKE_ID", "LAST_INVENTORY_TAKE_TIME", "INVENTORY_DETECTED_LOCATION", "FIXED_READER_ID" asset attributes to "Equipment" asset type.
1. Add "EPC" asset attribute to "Summary - Location" asset type.
1. Create "Reader" asset type to "Equipment\Peripheral" asset type.
1. Create "READER_NAME" asset attribute with Record Value Changes = true.
1. Create "LAST_HEART_BEAT" asset attribute with Record Value Changes = true.
1. Create "ALARM_IP" asset attribute with Record Value Changes = true.
1. Add "LAST_HEART_BEAT", "ALARM_IP", "READER asset attribute to "Reader" asset type.

### Alert Management
1. Create "Alert Fixed Reader" Alert Action.
    * Add Name, Assigned Location, Description, Fixed Reader ID and EPC in Additional Attributes.
2. Create "Non-schedule Move-out" Threshold.
    * Add


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
