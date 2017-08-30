package hk.com.quantum.beijing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.HashMap;

public class CenterScapeService extends AbstractVerticle {

    private WebClient client;

    //Logging
    private static final Logger logger = LoggerFactory.getLogger(CenterScapeService.class.getName());

    @Override
    public void start() {
        logger.info("CenterScape Service is running...");

        // Create a web client.
        client = WebClient.create(vertx);

        // Scheduler to pull in
        // -------------------------------------------
        // | CS             Mobile App      Function |
        // | GUID           epc
        // |
        // -------------------------------------------

        // Scheduler that runs every 10s.
        // TODO: use config
        vertx.setPeriodic(10000, id ->{
             HashMap<String, String> epc2GUIDMap = new HashMap(getGUID());
        });

        // Consume eb messages.
        vertx.eventBus().consumer("eb", message -> {
            JsonObject update = new JsonObject(message.body().toString());
            logger.debug("Received: " + update.encodePrettily());
            convertMessage(update);
        });
    }

    private HashMap<String, String> getGUID () {
        // TODO: use config
        HttpRequest<JsonObject> request = client
            .get(80, "cs2.qds.hk", "/api/entity")
            .as(BodyCodec.jsonObject());
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

}
