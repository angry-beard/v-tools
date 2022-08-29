package com.vertx.amqp.client;

import com.vertx.utils.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;

public class Receiver extends AbstractVerticle {

    private final String address = "examples";

    public static void main(String[] args) {
        Runner.runExample(Receiver.class);
    }

    @Override
    public void start() throws Exception {
        ProtonClient client = ProtonClient.create(vertx);

        client.connect("localhost", 8099, res -> {
            if (!res.succeeded()) {
                System.out.println("Connect failed: " + res.cause());
                return;
            }

            ProtonConnection connection = res.result();
            connection.open();

            connection.createReceiver(address).handler((delivery, msg) -> {
                String content = (String) ((AmqpValue) msg.getBody()).getValue();
                System.out.println("Received message with content: " + content);
            }).open();
        });
    }
}
