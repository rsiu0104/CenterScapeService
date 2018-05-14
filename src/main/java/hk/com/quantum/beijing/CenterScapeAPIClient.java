package hk.com.quantum.beijing;

import io.netty.handler.codec.http.HttpHeaders;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Base64;

public class CenterScapeAPIClient {

    //Logging
    private static final Logger logger = LoggerFactory.getLogger(CenterScapeAPIClient.class.getName());

    private HttpClient client;
    private String credentials;
    
    public CenterScapeAPIClient(Vertx vertx, JsonObject config) {

        // Create the HTTP client and configure the host and post.
        credentials = String.format("%s:%s", config.getString("cs.user"), config.getString("cs.password"));

        try {
            client = vertx.createHttpClient(new HttpClientOptions()
                    .setSsl(config.getBoolean("cs.setSsl", false))
                    .setTrustAll(config.getBoolean("cs.setSsl", false))
                    .setDefaultHost(config.getString("cs.host"))
                    .setDefaultPort(config.getInteger("cs.port", 8083))
                    .setVerifyHost(false)
            );
        } catch (Exception e) {
            logger.error("Error creating CenterScapeAPIClient, please config file. Exception : " + e);
        }
    }

    public void close() {
        // Don't forget to close the client when you are done.
        client.close();
    }

    public void getIdentifierMap (String url, Handler<AsyncResult<JsonArray>> handler) {
        // Emit a HTTP GET
        // TODO: Use config file.
        client.get(url,
                response ->
                        // Handler called when the response is received
                        // We register a second handler to retrieve the body
                        response.bodyHandler(body -> {
                            logger.trace(body.toJsonArray().encodePrettily());
                            // When the map is populated, invoke the result handler
                            handler.handle(Future.succeededFuture(body.toJsonArray()));
                        }))
        .exceptionHandler(t -> {
            // If something bad happen, report the failure to the passed handler
            handler.handle(Future.failedFuture(t));
        })
        // Put authentication header
        .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()))
        // Call end to send the request
        .end();
    }

    public void putUpdates (String guid, JsonObject EntityJson, Handler<AsyncResult<Void>> handler) {
        if(guid != null && !guid.isEmpty()) {
            // Emit a HTTP PUT
            client.put("/api/entity/" + guid,
                    response -> {
                        // Check the status code and act accordingly
                        if (response.statusCode() == 200) {
                            handler.handle(Future.succeededFuture());
                        } else {
                            handler.handle(Future.failedFuture(response.statusMessage()));
                        }
                    })
                    .exceptionHandler(t -> handler.handle(Future.failedFuture(t)))
                    .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()))
                    // Pass the name we want to add
                    .end(EntityJson.toString());
        }
    }
}

