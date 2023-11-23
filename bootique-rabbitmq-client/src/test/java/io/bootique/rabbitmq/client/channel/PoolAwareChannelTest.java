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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PoolAwareChannelTest {

    @Test
    public void close() throws IOException, TimeoutException {
        Channel delegate = mock(Channel.class);
        when(delegate.isOpen()).thenReturn(true);

        BlockingQueue<Channel> cache = new LinkedBlockingQueue<>(5);
        PoolAwareChannel channel  = new PoolAwareChannel(delegate, cache);


        assertTrue(channel.isOpen());
        channel.close();
        assertFalse(channel.isOpen());

        // underlying channel should stay open
        verify(delegate, times(0)).close();
        assertSame(delegate, cache.poll());
    }

    @Test
    public void close_CacheIsFull() throws IOException, TimeoutException {
        Channel delegate = mock(Channel.class);
        when(delegate.isOpen()).thenReturn(true);

        BlockingQueue<Channel> filledCache = new LinkedBlockingQueue<>(1);
        filledCache.offer(mock((Channel.class)));

        PoolAwareChannel channel  = new PoolAwareChannel(delegate, filledCache);

        assertTrue(channel.isOpen());
        channel.close();
        assertFalse(channel.isOpen());

        // underlying channel could not be cached and should have been closed
        verify(delegate, times(1)).close();
    }
}
