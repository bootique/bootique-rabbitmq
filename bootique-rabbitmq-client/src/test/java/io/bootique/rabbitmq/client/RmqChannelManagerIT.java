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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.junit5.BQTest;
import io.bootique.rabbitmq.client.channel.RmqChannelManager;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;
import io.bootique.rabbitmq.client.unit.RabbitMQBaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class RmqChannelManagerIT extends RabbitMQBaseTest {

    @Test
    public void testSingleRoutingKey() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:exchange-topic.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        try (Channel channel = runtime.getInstance(RmqChannelManager.class).createChannel("c1")) {

            runtime.getInstance(RmqTopologyManager.class).newTopology()
                    .ensureQueueBoundToExchange("new_queue", "topicExchange", "a.*")
                    .create(channel, true);

            String message = "Hello World!";
            channel.basicPublish("topicExchange", "a.b", null, message.getBytes(StandardCharsets.UTF_8));

            GetResponse response = channel.basicGet("new_queue", false);
            assertNotNull(response);
            assertEquals(message, new String(response.getBody()));
        }
    }

    @Test
    public void testMultiRoutingKeys() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:exchange-topic.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        try (Channel channel = runtime.getInstance(RmqChannelManager.class).createChannel("c1")) {

            runtime.getInstance(RmqTopologyManager.class).newTopology()
                    .ensureQueueBoundToExchange("rq_queue", "topicExchange", "a.*")
                    .ensureQueueBoundToExchange("rq_queue", "topicExchange", "b.*")
                    .create(channel, true);

            String messageA = "For A";
            channel.basicPublish("topicExchange", "a.x", null, messageA.getBytes(StandardCharsets.UTF_8));
            GetResponse rA = channel.basicGet("rq_queue", false);
            assertNotNull(rA);
            assertEquals(messageA, new String(rA.getBody()));

            String messageB = "For B";
            channel.basicPublish("topicExchange", "b.x", null, messageB.getBytes(StandardCharsets.UTF_8));
            GetResponse rB = channel.basicGet("rq_queue", false);
            assertNotNull(rB);
            assertEquals(messageB, new String(rB.getBody()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ex_durable", "ex_non_durable"})
    public void testChannelCache(String exchange) throws IOException, TimeoutException, InterruptedException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:channel-cache.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        RmqChannelManager channelManager = runtime.getInstance(RmqChannelManager.class);

        // preallocate read channels
        Channel cr1 = channelManager.createChannel("c");
        Channel cr2 = channelManager.createChannel("c");
        assertNotEquals(cr1.getChannelNumber(), cr2.getChannelNumber(), "Channel is still open and should not be reused");

        Channel cw1 = channelManager.createChannel("c");
        assertNotEquals(cr2.getChannelNumber(), cw1.getChannelNumber(), "Channel is still open and should not be reused");

        String queue = exchange + "_q";
        runtime.getInstance(RmqTopologyManager.class).newTopology()
                .ensureQueueBoundToExchange(queue, exchange, "a.*")
                .create(cw1, true);

        cw1.basicPublish(exchange, "a.x", null, "M1".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300L);
        assertResponse("M1", cr1.basicGet(queue, true));

        // close c2 and see if it gets reused
        cw1.close();
        Channel throwaway = channelManager.createChannel("c");
        assertEquals(cw1.getChannelNumber(), throwaway.getChannelNumber(), "Channel must be reused after close");

        // open a new (non-reused) channel and see if it can work with the existing topology
        Channel cw2 = channelManager.createChannel("c");
        cw2.basicPublish(exchange, "a.x", null, "M2".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300L);
        assertResponse("M2", cr2.basicGet(queue, true));
    }

    @Test
    public void testTopologyCache() throws IOException, InterruptedException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:topology-cache.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        RmqChannelManager channelManager = runtime.getInstance(RmqChannelManager.class);
        RmqTopologyManager topologyManager = runtime.getInstance(RmqTopologyManager.class);

        Channel cr = channelManager.createChannel("c");
        Channel cw = channelManager.createChannel("c");

        assertTrue(topologyManager.newTopology()
                .ensureQueueBoundToExchange("ex_q", "ex", "a.*")
                .create(cr, false));

        cw.basicPublish("ex", "a.x", null, "M1".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300L);
        assertResponse("M1", cr.basicGet("ex_q", true));

        assertFalse(topologyManager.newTopology()
                .ensureQueueBoundToExchange("ex_q", "ex", "a.*")
                .create(cr, false));

        cw.basicPublish("ex", "a.x", null, "M2".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300L);
        assertResponse("M2", cr.basicGet("ex_q", true));

        topologyManager.clearCache();
        assertTrue(topologyManager.newTopology()
                .ensureQueueBoundToExchange("ex_q", "ex", "a.*")
                .create(cr, false));

        cw.basicPublish("ex", "a.x", null, "M3".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300L);
        assertResponse("M3", cr.basicGet("ex_q", true));
    }

    private void assertResponse(String expected, GetResponse response) {
        assertNotNull(response);
        assertEquals(expected, new String(response.getBody()));
    }
}
