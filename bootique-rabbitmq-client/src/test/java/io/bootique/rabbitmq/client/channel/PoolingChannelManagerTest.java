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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PoolingChannelManagerTest {

    @Test
    public void testCreateChannel() throws IOException, TimeoutException {

        Channel c1 = mock(Channel.class);
        Channel c2 = mock(Channel.class);
        Channel c3 = mock(Channel.class);

        RmqChannelManager delegate = mock(RmqChannelManager.class);
        when(delegate.createChannel(anyString())).thenReturn(c1, c2, c3);

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
    public void testClose() throws IOException, TimeoutException {

        Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);

        Channel c1 = mock(Channel.class);
        when(c1.isOpen()).thenReturn(true);
        when(c1.getConnection()).thenReturn(connection);

        Channel c2 = mock(Channel.class);
        when(c2.isOpen()).thenReturn(true);
        when(c2.getConnection()).thenReturn(connection);

        Channel c3 = mock(Channel.class);
        when(c3.isOpen()).thenReturn(true);
        when(c3.getConnection()).thenReturn(connection);

        RmqChannelManager delegate = mock(RmqChannelManager.class);
        when(delegate.createChannel(anyString())).thenReturn(c1, c2, c3);

        PoolingChannelManager m = new PoolingChannelManager(delegate, 2);
        Channel pc1 = m.createChannel("c1");
        Channel pc2 = m.createChannel("c1");
        Channel pc3 = m.createChannel("c2");
        pc1.close();
        pc2.close();
        pc3.close();

        verifyNoInteractions(c1);
        verifyNoInteractions(c2);
        verifyNoInteractions(c3);

        m.close();

        verify(c1).close();
        verify(c2).close();
        verify(c3).close();
    }
}
