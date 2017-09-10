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
    
    public CenterScapeAPIClient(Vertx vertx) {
        // Create the HTTP client and configure the host and post.
        // TODO: Use config file instead.

        credentials = String.format("%s:%s", "admin", "qdsHA2018~19");
//        credentials = String.format("%s:%s", "admin", "Password1234");

        client = vertx.createHttpClient(new HttpClientOptions()
                .setSsl(true)
                .setTrustAll(true)
                .setDefaultHost("ha.qds.hk")
                .setDefaultPort(443)
                .setVerifyHost(false)
        );
    }

    public void close() {
        // Don't forget to close the client when you are done.
        client.close();
    }

    public void getIdentifierMap (Handler<AsyncResult<JsonArray>> handler) {
        // Emit a HTTP GET
        // TODO: Use config file.
        client.get("/api/entity?filter=true&type=$tAsset&attribute=$aLocation&operator=eq&value=All",
                response ->
                        // Handler called when the response is received
                        // We register a second handler to retrieve the body
                        response.bodyHandler(body -> {
                            logger.debug(body.toJsonArray().encodePrettily());
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

//    public void getNames(Handler<AsyncResult<JsonArray>> handler) {
//        // Emit a HTTP GET
//        // TODO: Use config file.
//        client.get("/api/entity?filter=true&type=$tAsset&attribute=$aLocation&operator=eq&value=All",
//                response ->
//                        // Handler called when the response is received
//                        // We register a second handler to retrieve the body
//                        response.bodyHandler(body -> {
//                            // When the body is read, invoke the result handler
//                            handler.handle(Future.succeededFuture(body.toJsonArray()));
//                        }))
//                .exceptionHandler(t -> {
//                    // If something bad happen, report the failure to the passed handler
//                    handler.handle(Future.failedFuture(t));
//                })
//                // Put authentication header
//                .putHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()))
//                // Call end to send the request
//                .end();
//    }
//
//    public void addName(String name, Handler<AsyncResult<Void>> handler) {
//        // Emit a HTTP POST
//        client.post("/names",
//                response -> {
//                    // Check the status code and act accordingly
//                    if (response.statusCode() == 200) {
//                        handler.handle(Future.succeededFuture());
//                    } else {
//                        handler.handle(Future.failedFuture(response.statusMessage()));
//                    }
//                })
//                .exceptionHandler(t -> handler.handle(Future.failedFuture(t)))
//                // Pass the name we want to add
//                .end(name);
//    }
}

