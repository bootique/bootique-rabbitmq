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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class EndpointsIT extends RabbitMQBaseTest {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app("-c", "classpath:endpoints.yml")
            .module(b -> BQCoreModule.extend(b)
                    // emulating suggested best practices - 1 connection for pub, 1 - for sub
                    .setProperty("bq.rabbitmq.connections.pubConnection.uri", rmq.getAmqpUrl())
                    .setProperty("bq.rabbitmq.connections.subConnection.uri", rmq.getAmqpUrl()))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void p1Route() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);
        Sub s1 = new Sub();
        Sub s2 = new Sub();

        endpoints.sub("s1").subscribe(s1);
        endpoints.sub("s2").subscribe(s2);

        endpoints.pub("p1")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("10").build())
                .publish("M1".getBytes());

        endpoints.pub("p1")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("20").build())
                .publish("M2".getBytes());

        s1.waitUntilDelivered(2);
        s1.assertReceived("M1,10,p1.X", "First message not received");
        s1.assertReceived("M2,20,p1.X", "Second message not received");

        s2.assertDeliveryCount(0);
    }

    @Test
    public void p2Route() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);
        Sub s3 = new Sub();
        Sub s4 = new Sub();

        endpoints.sub("s3").newSubscription().queueName("s3-queue").subscribe(s3);
        endpoints.sub("s4").newSubscription().queueName("s4-queue").subscribe(s4);

        endpoints.pub("p2")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("30").build())
                .publish("M3".getBytes());

        endpoints.pub("p2")
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
    @Disabled("This works on OSX, but fails on CI/CD - the second delivery still goes through. Unclear why")
    public void testConsumerExceptions() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);

        ThrowingSub s3 = new ThrowingSub();
        endpoints.sub("s3")
                .newSubscription()
                .queueName("s3-exception-queue")
                .subscribe(s3);

        endpoints.pub("p1").publish("M1".getBytes());
        s3.waitUntilDelivered(1);
        s3.assertReceived("M1,p1.X", "First message not received");

        // by default RabbitMQ closes the Channel on consumer exceptions
        endpoints.pub("p1").publish("M2".getBytes());
        s3.ensureNotDelivered(1);
    }

    @Test
    public void cancelSubscription() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);

        RmqSubEndpoint s1Endpoint = endpoints.sub("s1");

        Sub s1 = new Sub();
        String consumerTag = s1Endpoint.subscribe(s1);
        assertEquals(1, s1Endpoint.getSubscriptionsCount());

        endpoints.pub("p1").publish("M1".getBytes());
        s1.waitUntilDelivered(1);
        s1.assertReceived("M1,p1.X", "First message not received");

        s1Endpoint.cancelSubscription(consumerTag);
        assertEquals(0, s1Endpoint.getSubscriptionsCount());

        endpoints.pub("p1").publish("M2".getBytes());
        s1.ensureNotDelivered(1);
    }

    @Test
    public void subWithAck() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);
        Sub s3 = new Sub();

        endpoints.sub("s3").newSubscription()
                .queueName("s3-ack-queue")
                .autoAck(false)
                .subscribe(c -> new AckConsumer(c, s3));

        endpoints.pub("p2")
                .newMessage()
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("50").build())
                .publish("M3".getBytes());

        s3.waitUntilDelivered(1);
        s3.assertReceived("M3,50,p2.X", "Message not received");
    }

    @Test
    public void p1QueueWithTTL() {
        RmqEndpoints endpoints = app.getInstance(RmqEndpoints.class);
        Sub s5 = new Sub();

        endpoints.sub("s5").subscribe(s5);

        endpoints.pub("p1")
                .newMessage()
                .routingKey("p1.ttl")
                .properties(MessageProperties.TEXT_PLAIN.builder().messageId("60").build())
                .publish("M5".getBytes());

        s5.waitUntilDelivered(1);
        s5.assertReceived("M5,60,p1.ttl", "First message not received");

        // TODO: actually verify that TTL results in message expiration
        //   for now only verifying that the TTL argument didn't cause RMQ exceptions
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

    static class ThrowingSub extends Sub {

        @Override
        public void handle(String consumerTag, Delivery message) {
            super.handle(consumerTag, message);
            throw new RuntimeException("Emulating consumer exception for " + consumerTag);
        }
    }
}
