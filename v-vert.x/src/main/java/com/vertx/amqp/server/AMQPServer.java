package com.vertx.amqp.server;

import com.vertx.utils.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;
import io.vertx.proton.ProtonServer;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.AmqpError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.proton.ProtonHelper.message;

/**
 * @author angry_beard
 * @date 2022/8/26 14:07
 */
public class AMQPServer extends AbstractVerticle {

    private static final int PORT = 8099;

    public static void main(String[] args) {
        Runner.runExample(AMQPServer.class);
    }

    @Override
    public void start() throws Exception {
        ProtonServer server = ProtonServer.create(vertx);

        server.connectHandler((connection) -> {
            initConnection(vertx, connection);
        });

        server.listen(PORT, (res) -> {
            if (res.succeeded()) {
                System.out.println("Listening on port " + res.result().actualPort());
            } else {
                System.out.println("Failed to start listening on port " + PORT + ":");
                res.cause().printStackTrace();
            }
        });
    }

    private void initConnection(Vertx vertx, ProtonConnection connection) {
        connection.openHandler(res -> {
            System.out.println("Client connection opened, container-id: " + connection.getRemoteContainer());
            connection.open();
        });
        connection.closeHandler(c -> {
            System.out.println("Client closing connection, container-id: " + connection.getRemoteContainer());
            connection.close();
            connection.disconnect();
        });
        connection.disconnectHandler(c -> {
            System.out.println("Client socket disconnected, container-id: " + connection.getRemoteContainer());
            connection.disconnect();
        });
        connection.sessionOpenHandler(session -> {
            session.closeHandler(x -> {
                session.close();
                session.free();
            });
            session.open();
        });
        connection.senderOpenHandler(sender -> {
            initSender(vertx, connection, sender);
        });
        connection.receiverOpenHandler(AMQPServer::initReceiver);
    }

    private static void initReceiver(ProtonReceiver receiver) {
        Target remoteTarget = (org.apache.qpid.proton.amqp.messaging.Target) receiver.getRemoteTarget();
        if (remoteTarget == null) {
            // Example doesn't support 'looking up' existing links, so we will just close with an error.
            receiver.setTarget(null);
            receiver.setCondition(new ErrorCondition(AmqpError.INVALID_FIELD, "No target terminus specified"));
            receiver.open();
            receiver.close();
            return;
        }

        if (remoteTarget.getDynamic()) {
            String dynamicAddress = UUID.randomUUID().toString();
            remoteTarget.setAddress(dynamicAddress);
        }

        receiver.setTarget(remoteTarget);
        receiver.setSource(receiver.getRemoteSource());

        receiver.handler((delivery, msg) -> {
            String address = remoteTarget.getAddress();
            if (address == null) {
                address = msg.getAddress();
            }

            Section body = msg.getBody();
            if (body instanceof AmqpValue) {
                String content = (String) ((AmqpValue) body).getValue();
                System.out.println("Received message for address: " + address + ", body: " + content);
            }
        });

        receiver.detachHandler(x -> {
            receiver.detach();
            receiver.free();
        });

        receiver.closeHandler(x -> {
            receiver.close();
            receiver.free();
        });

        receiver.open();
    }

    private void initSender(Vertx vertx, ProtonConnection connection, ProtonSender sender) {
        Source remoteSource = (Source) sender.getRemoteSource();
        if (remoteSource == null) {
            sender.setTarget(null);
            sender.setCondition(new ErrorCondition(AmqpError.INVALID_FIELD, "No source terminus specified"));
            sender.open();
            sender.close();
            return;
        }

        if (remoteSource.getDynamic()) {
            String dynamicAddress = UUID.randomUUID().toString();
            remoteSource.setAddress(dynamicAddress);
        }

        sender.setSource(remoteSource);
        sender.setTarget(sender.getRemoteTarget());

        AtomicInteger sent = new AtomicInteger();
        final long timer = vertx.setPeriodic(1000, t -> {
            if (connection.isDisconnected()) {
                vertx.cancelTimer(t);
            } else {
                if (!sender.sendQueueFull()) {
                    int msgNum = sent.incrementAndGet();
                    System.out.println("Sending message " + msgNum + " to client, for address: " + remoteSource.getAddress());
                    Message m = message("Hello " + msgNum + " from Server!");
                    sender.send(m, delivery -> {
                        System.out.println("Message " + msgNum + " was received by the client.");
                    });
                }
            }
        });

        sender.detachHandler(x -> {
            vertx.cancelTimer(timer);
            sender.detach();
            sender.free();
        });

        sender.closeHandler(x -> {
            vertx.cancelTimer(timer);
            sender.close();
            sender.free();
        });
        sender.open();
    }
}
