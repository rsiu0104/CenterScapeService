package hk.com.quantum.beijing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;

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

        // Initialization
//        vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
//                new DropwizardMetricsOptions()
//                        .setEnabled(true)
//                        .setJmxEnabled(true)
//        ));
        MetricsService service = MetricsService.create(vertx);
        EventBus eb = vertx.eventBus();
        MessageConsumer<JsonObject> consumer = eb.consumer("eb");

        // Once API Client is instantiated, let's get the first IdentifierMap instead of waiting for polling period to elapse.
        CenterScapeAPIClient client = new CenterScapeAPIClient(vertx, config());
        client.getIdentifierMap((ar -> {
            if (ar.succeeded()) {
                IdentifierMap = ar.result();
                logger.info("IdentifierMap: Received " + IdentifierMap.size() + " record(s).");
            } else {
                logger.error("Unable to retrieve the IdentifierMap: "
                        + ar.cause().getMessage());
            }
        }));

        // Scheduler that runs periodically to get CS GUID-EPC Map.
        vertx.setPeriodic(config().getInteger("cs.pollingPeriod", 10000), id ->{
            client.getIdentifierMap((ar -> {
                if (ar.succeeded()) {
                    IdentifierMap = ar.result();
                    logger.info("IdentifierMap: Received " + IdentifierMap.size() + " record(s).");
                } else {
                    logger.error("Unable to retrieve the IdentifierMap: "
                            + ar.cause().getMessage());
                }
            }));
        });

        // send metrics message to the event bus
        vertx.setPeriodic(config().getInteger("metric.pollingperiod", 60000), t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            eb.publish("microservice.monitor.metrics", metrics);
        });

        // Consume eb messages.
        consumer.handler(message -> {
            HashMap<String, JsonObject> HashUpdates = new HashMap<String, JsonObject>();
            JsonObject update = new JsonObject(message.body().toString());

            // Assign Message to JsonArray of Updates
            JsonArray UpdateArray = update.getJsonArray("updates");
            String reader = update.getString("reader_name");

            logger.info("Eventbus: Received " + UpdateArray.size() + " record(s)");
            message.reply(UpdateArray.size());

            // Corner case at initialization when IdentiMap is still empty. Don't do anything yet.
            if(IdentifierMap.size() > 0) {

                // Iterate through the updates
                for (int i = 0; i < UpdateArray.size(); i++) {
                    String Epc = UpdateArray.getJsonObject(i).getString("epc");
                    String TimeStampStr = UpdateArray.getJsonObject(i).getString("first_seen_timestamp");
                    Long TimeStampUSec = Long.parseLong(TimeStampStr);
                    boolean isHeartBeat = Objects.equals(Epc, "********");
                    String guid = null;

                    // Find the GUID of the updates by EPC.
                    if (isHeartBeat) {
                        guid = getString("$aName", reader, "guid");
                    } else {
                        guid = getString("EPC", Epc, "guid");
                    }

                    logger.info("Rec " + i + ": " +
                            "EPC: " + Epc + " " +
                            "GUID: " + guid + " " +
                            "TS: " + formatUSec(TimeStampUSec));

                    // Algo to handle updates of the same EPC/GUID.
                    // Simple solution is to hash it and show the latest update. Right now the hashing is done in the SWC Service side.
                    // TODO need to put in logic to handle heartbeat here.
                    if (guid != null && !guid.isEmpty()){
                        JsonObject EntityJson = new JsonObject();
                        String EntityType = getString("guid", guid, "type");
                        EntityJson.put("type", EntityType);
                        EntityJson.put("$aDetectedLocation", "$tUnknownLocation");
                        if (isHeartBeat) EntityJson.put("LAST_HEART_BEAT", TimeStampUSec);
                        HashUpdates.put(guid, EntityJson);
                    }
                }

                logger.info("HashUpdates (" + HashUpdates.size() + ") : " + HashUpdates.toString());

                // Update CS API foreach HashMap
                for (Map.Entry<String, JsonObject> entry : HashUpdates.entrySet()) {
                    client.putUpdates(entry.getKey(), entry.getValue(), (ar -> {
                        if (ar.succeeded()) {
                            logger.debug("Put to CS API is successful!");
                        } else {
                            logger.error("Unable to put to CS API: " + ar.cause().getMessage());
                        }
                    }));
                }
            }
        });

        consumer.completionHandler(res -> {
            if (res.succeeded()) {
                logger.info("The handler registration has reached all nodes");
            } else {
                logger.error("Registration failed!");
            }
        });
    }
    
    // Search for the JsonObject with EPC = 2106000000111 and return the value of GUID
    // getString("EPC", "210600000000111", "GUID") returns the GUID of 210600000000111
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
            logger.trace("i: " + i + "Found is " + Boolean.toString(Found) + ", " +
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

    public void stop(){
        vertx.close();
    }
}
