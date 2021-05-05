/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.rabbitmq.client;

import com.rabbitmq.client.*;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpoint;
import io.bootique.rabbitmq.client.unit.RabbitMQBaseTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class PubSubIT extends RabbitMQBaseTest {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app("-c", "classpath:pubsub.yml")
            .module(b -> BQCoreModule.extend(b)
                    // emulating suggested best practices - 1 connection for pub, 1 - for sub
                    .setProperty("bq.rabbitmq.connections.pubConnection.uri", rmq.getAmqpUrl())
                    .setProperty("bq.rabbitmq.connections.subConnection.uri", rmq.getAmqpUrl()))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testP1Route() {
        RmqPubSub pubSub = app.getInstance(RmqPubSub.class);
        Sub s1 = new Sub();
        Sub s2 = new Sub();

        pubSub.subEndpoint("s1").subscribe(s1);
        pubSub.subEndpoint("s2").subscribe(s2);

        pubSub.pubEndpoint("p1")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("10").build())
                .publish("M1".getBytes());

        pubSub.pubEndpoint("p1")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("20").build())
                .publish("M2".getBytes());

        s1.waitUntilDelivered(2);
        s1.assertReceived("M1,10,p1.X", "First message not received");
        s1.assertReceived("M2,20,p1.X", "Second message not received");

        s2.assertDeliveryCount(0);
    }

    @Test
    public void testP2Route() {
        RmqPubSub pubSub = app.getInstance(RmqPubSub.class);
        Sub s3 = new Sub();
        Sub s4 = new Sub();

        pubSub.subEndpoint("s3").newSubscription().queue("s3-queue").subscribe(s3);
        pubSub.subEndpoint("s4").newSubscription().queue("s4-queue").subscribe(s4);

        pubSub.pubEndpoint("p2")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("30").build())
                .publish("M3".getBytes());

        pubSub.pubEndpoint("p2")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("40").build())
                .publish("M4".getBytes());

        s3.waitUntilDelivered(2);
        s3.assertReceived("M3,30,p2.X", "First message not received");
        s3.assertReceived("M4,40,p2.X", "Second message not received");

        s4.waitUntilDelivered(2);
        s4.assertReceived("M3,30,p2.X", "First message not received");
        s4.assertReceived("M4,40,p2.X", "Second message not received");
    }

    @Test
    public void testCancelSubscription() {
        RmqPubSub pubSub = app.getInstance(RmqPubSub.class);

        RmqSubEndpoint s1Endpoint = pubSub.subEndpoint("s1");

        Sub s1 = new Sub();
        String consumerTag = s1Endpoint.subscribe(s1);
        assertEquals(1, s1Endpoint.getSubscriptionsCount());

        pubSub.pubEndpoint("p1").publish("M1".getBytes());
        s1.waitUntilDelivered(1);
        s1.assertReceived("M1,p1.X", "First message not received");

        s1Endpoint.cancelSubscription(consumerTag);
        assertEquals(0, s1Endpoint.getSubscriptionsCount());

        pubSub.pubEndpoint("p1").publish("M2".getBytes());
        s1.ensureNotDelivered(1);
    }

    @Test
    public void testSubWithAck() {
        RmqPubSub pubSub = app.getInstance(RmqPubSub.class);
        Sub s3 = new Sub();

        pubSub.subEndpoint("s3").newSubscription()
                .queue("s3-ack-queue")
                .autoAck(false)
                .subscribe(c -> new AckConsumer(c, s3));

        pubSub.pubEndpoint("p2")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("50").build())
                .publish("M3".getBytes());

        s3.waitUntilDelivered(1);
        s3.assertReceived("M3,50,p2.X", "Message not received");
    }

    static class Sub implements DeliverCallback {

        Map<Integer, String> received = new ConcurrentHashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void handle(String consumerTag, Delivery message) {
            String decoded = new String(message.getBody());

            String id = message.getProperties().getMessageId() != null
                    ? message.getProperties().getMessageId() + ","
                    : "";

            String withMeta = decoded + "," + id + message.getEnvelope().getRoutingKey();
            received.put(counter.incrementAndGet(), withMeta);
        }

        void waitUntilDelivered(int expectedMessageCount) {
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                while (received.size() < expectedMessageCount) {
                    Thread.sleep(100);
                }
            }, "Expected " + expectedMessageCount + ", delivered: " + received.size());
        }

        void ensureNotDelivered(int expectedMessageCount) {
            assertDeliveryCount(expectedMessageCount);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                fail(e);
            }
            assertDeliveryCount(expectedMessageCount);
        }

        void assertReceived(String expected, String message) {
            assertTrue(received.containsValue(expected), message);
        }

        void assertDeliveryCount(int expected) {
            assertEquals(expected, received.size());
        }
    }

    static class AckConsumer extends DefaultConsumer {

        private final Sub delegate;

        public AckConsumer(Channel channel, Sub delegate) {
            super(channel);
            this.delegate = delegate;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            delegate.handle(consumerTag, new Delivery(envelope, properties, body));
            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }
    }
}
