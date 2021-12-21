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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RmqTopologyTest {

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
    public void testEquals() {

        Map<String, RmqExchange> exchanges = new HashMap<>();
        exchanges.put("e1", e1);
        exchanges.put("e2", e2);

        Map<String, RmqQueue> queues = new HashMap<>();
        queues.put("q1", q1);
        queues.put("q2", q2);

        RmqTopology t1 = new RmqTopologyBuilder(exchanges, queues).build();
        RmqTopology t2 = new RmqTopologyBuilder(exchanges, queues).build();
        RmqTopology t3 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").build();
        RmqTopology t4 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").build();
        RmqTopology t5 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1").build();
        RmqTopology t6 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1").build();
        RmqTopology t7 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k1").build();
        RmqTopology t8 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k1").build();
        RmqTopology t9 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k2").build();

        assertEquals(t1, t2);
        assertEquals(t3, t4);
        assertEquals(t5, t6);
        assertEquals(t7, t8);
        assertNotEquals(t1, t3);
        assertNotEquals(t3, t5);
        assertNotEquals(t5, t7);
        assertNotEquals(t7, t9);
    }

    @Test
    public void testHashCode() {

        Map<String, RmqExchange> exchanges = new HashMap<>();
        exchanges.put("e1", e1);
        exchanges.put("e2", e2);

        Map<String, RmqQueue> queues = new HashMap<>();
        queues.put("q1", q1);
        queues.put("q2", q2);

        RmqTopology t1 = new RmqTopologyBuilder(exchanges, queues).build();
        RmqTopology t2 = new RmqTopologyBuilder(exchanges, queues).build();
        RmqTopology t3 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").build();
        RmqTopology t4 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").build();
        RmqTopology t5 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1").build();
        RmqTopology t6 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1").build();
        RmqTopology t7 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k1").build();
        RmqTopology t8 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k1").build();
        RmqTopology t9 = new RmqTopologyBuilder(exchanges, queues).ensureExchange("e2").ensureQueue("q1")
                .ensureQueueBoundToExchange("q1", "e1", "k2").build();

        assertEquals(t1.hashCode(), t2.hashCode());
        assertEquals(t3.hashCode(), t4.hashCode());
        assertEquals(t5.hashCode(), t6.hashCode());
        assertEquals(t7.hashCode(), t8.hashCode());
        assertNotEquals(t1.hashCode(), t3.hashCode());
        assertNotEquals(t3.hashCode(), t5.hashCode());
        assertNotEquals(t5.hashCode(), t7.hashCode());
        assertNotEquals(t7.hashCode(), t9.hashCode());
    }
}
