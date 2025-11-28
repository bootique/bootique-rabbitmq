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
package io.bootique.rabbitmq.client.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PoolingChannelManagerTest {

    @Test
    public void createChannel() throws IOException, TimeoutException {

        TestChannel c1 = new TestChannel();
        TestChannel c2 = new TestChannel();
        TestChannel c3 = new TestChannel();

        RmqChannelManager delegate = new TestChannelManager(c1, c2, c3);

        PoolingChannelManager m = new PoolingChannelManager(delegate, 2);
        Channel pc1 = m.createChannel("c");
        assertSame(c1, ((PoolAwareChannel) pc1).delegate);

        Channel pc2 = m.createChannel("c");
        assertSame(c2, ((PoolAwareChannel) pc2).delegate);

        pc1.close();
        pc2.close();

        Channel pc3 = m.createChannel("c");
        assertSame(c1, ((PoolAwareChannel) pc3).delegate);

        Channel pc4 = m.createChannel("c");
        assertSame(c2, ((PoolAwareChannel) pc4).delegate);

        Channel pc5 = m.createChannel("c");
        assertSame(c3, ((PoolAwareChannel) pc5).delegate);
    }

    @Test
    public void close() throws IOException, TimeoutException {

        Connection connection = new TestConnection();
        TestChannel c1 = new TestChannel() {
            @Override
            public Connection getConnection() {
                return connection;
            }
        };


        TestChannel c2 = new TestChannel() {
            @Override
            public Connection getConnection() {
                return connection;
            }
        };

        TestChannel c3 = new TestChannel() {
            @Override
            public Connection getConnection() {
                return connection;
            }
        };

        RmqChannelManager delegate = new TestChannelManager(c1, c2, c3);
        PoolingChannelManager m = new PoolingChannelManager(delegate, 2);
        Channel pc1 = m.createChannel("c1");
        Channel pc2 = m.createChannel("c1");
        Channel pc3 = m.createChannel("c2");
        pc1.close();
        pc2.close();
        pc3.close();

        assertTrue(c1.open);
        assertTrue(c2.open);
        assertTrue(c3.open);

        m.close();

        assertFalse(c1.open);
        assertFalse(c2.open);
        assertFalse(c3.open);
    }

    static class TestChannelManager implements RmqChannelManager {
        final AtomicInteger counter;
        final TestChannel[] channels;

        public TestChannelManager(TestChannel... channels) {
            this.counter = new AtomicInteger();
            this.channels = channels;
        }

        @Override
        public Channel createChannel(String connectionName) {
            return channels[counter.getAndIncrement()];
        }
    }
}
