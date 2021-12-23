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
package io.bootique.rabbitmq.client.topology;

import com.rabbitmq.client.BuiltinExchangeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TopologyCacheTest {

    static RmqExchange e1;
    static RmqExchange e2;
    static RmqQueue q1;
    static RmqQueue q2;

    @BeforeAll
    static void createConfigs() {
        e1 = new RmqExchange();
        e1.setAutoDelete(true);
        e1.setType(BuiltinExchangeType.FANOUT);
        e1.setDurable(false);

        e2 = new RmqExchange();
        e2.setAutoDelete(false);
        e1.setType(BuiltinExchangeType.DIRECT);
        e1.setDurable(true);

        q1 = new RmqQueue();
        q1.setDurable(false);
        q1.setExclusive(false);
        q1.setAutoDelete(false);

        q2 = new RmqQueue();
        q2.setDurable(true);
        q2.setExclusive(true);
        q2.setAutoDelete(true);
    }

    @Test
    public void testSave() {

        Map<String, RmqExchange> exchanges = new HashMap<>();
        exchanges.put("e1", e1);
        exchanges.put("e2", e2);

        Map<String, RmqQueue> queues = new HashMap<>();
        queues.put("q1", q1);
        queues.put("q2", q2);

        TopologyCache cache = new TopologyCache(2);

        Supplier<RmqTopologyActions> a1 = () ->
                new RmqTopologyBuilder(exchanges, queues, mock(TopologyCache.class))
                        .ensureExchange("x")
                        .buildActions();

        Supplier<RmqTopologyActions> a2 = () ->
                new RmqTopologyBuilder(exchanges, queues, mock(TopologyCache.class))
                        .ensureExchange("y")
                        .buildActions();

        Supplier<RmqTopologyActions> a3 = () ->
                new RmqTopologyBuilder(exchanges, queues, mock(TopologyCache.class))
                        .ensureExchange("z")
                        .buildActions();

        assertTrue(cache.save(a1.get()));
        assertEquals(1, cache.size());

        assertTrue(cache.save(a2.get()));
        assertEquals(2, cache.size());

        assertTrue(cache.save(a3.get()));
        assertEquals(2, cache.size());

        assertFalse(cache.save(a2.get()), "Wrongfully evicted");
        assertFalse(cache.save(a3.get()), "Wrongfully evicted");
        assertTrue(cache.save(a1.get()));
    }

}
