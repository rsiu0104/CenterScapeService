package hk.com.quantum.beijing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CenterScapeService extends AbstractVerticle {

    //Logging
    private static final Logger logger = LoggerFactory.getLogger(CenterScapeService.class.getName());

    private JsonArray IdentifierMap = new JsonArray();

    @Override
    public void start() {
        logger.info("CenterScape Service is running...");

        CenterScapeAPIClient client = new CenterScapeAPIClient(vertx, config());

        // Scheduler that runs periodically to get CS GUID-EPC Map.
        // TODO: use config
        vertx.setPeriodic(config().getInteger("cs.pollingPeriod", 10000), id ->{
            client.getIdentifierMap((ar -> {
                if (ar.succeeded()) {
                    logger.info("succeed");
                    IdentifierMap = ar.result();
                    logger.info("IdentifierMap: Received " + IdentifierMap.size() + " record(s).");
                } else {
                    logger.info("error?");
                    logger.error("Unable to retrieve the IdentifierMap: "
                            + ar.cause().getMessage());
                }
            }));

            logger.info("Here!");
        });

        // Consume eb messages.
        vertx.eventBus().consumer("eb", message -> {
            HashMap<String, JsonObject> HashUpdates = new HashMap<String, JsonObject>();

            // Corner case at initialization when IdentiMap is still empty.
            if(IdentifierMap.size() > 0) {
                JsonObject update = new JsonObject(message.body().toString());

                // Assign Message to JsonArray of Updates
                JsonArray UpdateArray = update.getJsonArray("updates");
                logger.info("Eventbus: Received " + UpdateArray.size() + " record(s)");

                // Iterate through the updates
                for (int i = 0; i < UpdateArray.size(); i++) {

                    String Epc = UpdateArray.getJsonObject(i).getString("epc");
                    String TimeStampStr = UpdateArray.getJsonObject(i).getString("first_seen_timestamp");
                    Long TimeStampUSec = Long.parseLong(TimeStampStr);


                    // Find the GUID of the updates by EPC.
                    String guid = getString("EPC", Epc, "guid");
                    logger.info("Rec " + i + ": " +
                            "EPC: " + Epc + " " +
                            "GUID: " + guid + " " +
                            "TS: " + formatUSec(TimeStampUSec));
                    //                        "GUID of " + UpdateArray.getJsonObject(i).getString("epc") + " is " + guid);

                    // Algo to handle updates of the same EPC/GUID.
                    // Simple solution is to hash it.
                    // Should we record the first timestammp or the last?
                    JsonObject EntityJson = new JsonObject();
                    String EntityType = getString("guid", guid, "type");
                    EntityJson.put("type", EntityType);
                    EntityJson.put("$aDetectedLocation", "$tUnknownLocation");
                    HashUpdates.put(guid, EntityJson);
                }

                logger.info("HashUpdates: " + HashUpdates.toString());

                // Update CS API foreach HashMap
                for (Map.Entry<String, JsonObject> entry : HashUpdates.entrySet()) {
                    client.putUpdates(entry.getKey(), entry.getValue(), (ar -> {
                        if (ar.succeeded()) {
                            logger.info("Put to CS API is successful!");
                        } else {
                            logger.error("Unable to put to CS API: " + ar.cause().getMessage());
                        }
                    }));
                }
            }
        });


    }
    
    // Search for the JsonObject with EPC = 2106000000111 and return the value of GUID
    // getString("EPC", "210600000000111", "GUID") returns 
    private String getString (String SearchKey, String SearchValue, String ReturnKey) {

        boolean Found = false;
        int i = 0;
        String ReturnValue = null;

        //Iterate through the JSonArray
        while (!Found && i<IdentifierMap.size()) {

            // Make sure this JsonObject has the SearchKey AND ReturnKey
            if (IdentifierMap.getJsonObject(i).containsKey(SearchKey) && IdentifierMap.getJsonObject(i).containsKey(ReturnKey)) {
                if (Objects.equals(IdentifierMap.getJsonObject(i).getString(SearchKey), SearchValue)) {
                    ReturnValue = IdentifierMap.getJsonObject(i).getString(ReturnKey);
                    Found = true;
                }
            }
            logger.debug("i: " + i + "Found is " + Boolean.toString(Found) + ", " +
                    "Search: " +IdentifierMap.getJsonObject(i).getString(SearchKey) + " = " + SearchValue + ", " +
                    "Value of the ReturnKey " + ReturnValue
            );
            i++;
        }
        return ReturnValue;
    }

    private static String formatUSec (long Microseconds) {
        String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (Microseconds/1000));
        return date;
    }

    private void convertMessage(JsonObject InventoryUpdates) {

        try {
            // Logic to update assets via CenterScape API.

            // 1. Create HTTP client.
            // 2. Prepare HTTP Post request.




//            switch (operation) {
//                case SAY_HELLO_WORLD:
//                    message.reply("HELLO WORLD");
//                    break;
//                default:
//                    logger.error("Unable to handle operation {}", operation);
//                    message.reply("Unsupported operation");
//            }
        } catch (final Exception ex) {
            logger.error("Unable to handle operation due to exception", ex);
        }
    }

    public void stop(){
        vertx.close();
    }
}
