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
import io.bootique.rabbitmq.client.publisher.RmqPublisher;
import io.bootique.rabbitmq.client.publisher.RmqPublishers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class PublisherIT extends RabbitMQBaseTest {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app("-c", "classpath:publisher.yml")
            .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testPublishers() throws IOException, TimeoutException {

        ChannelFactory cf = app.getInstance(ChannelFactory.class);
        Map<Integer, String> received = new ConcurrentHashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        try (Channel consumeChannel = cf.openChannel("c1", "e1", "consumer1", "*.X")) {

            consumeChannel.basicConsume("consumer1", new DefaultConsumer(consumeChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    received.put(counter.incrementAndGet(),
                            new String(body) + "," + properties.getMessageId() + "," + envelope.getRoutingKey());
                    consumeChannel.basicAck(envelope.getDeliveryTag(), false);
                }
            });

            RmqPublisher p1 = app.getInstance(RmqPublishers.class).publisher("p1");
            p1.newMessage()
                    .properties(MessageProperties.TEXT_PLAIN.builder().messageId("2345").build())
                    .publish("M1".getBytes());

            RmqPublisher p2 = app.getInstance(RmqPublishers.class).publisher("p2");
            p2.newMessage()
                    .properties(MessageProperties.TEXT_PLAIN.builder().messageId("6789").build())
                    .publish("M2".getBytes());

            assertTimeout(Duration.ofSeconds(1), () -> {
                while (received.size() < 2) {
                    Thread.sleep(100);
                }
            });

            assertTrue(received.containsValue("M1,2345,p1.X"));
            assertTrue(received.containsValue("M2,6789,p2.X"));
        }
    }

}
