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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A channel manager that pools a certain number of open Channels, allowing their reuse. Channels are pooled separately
 * per connection. This manager provides weak guarantees on the max number of open channels. Pool capacity parameter
 * passed in the constructor  controls how many channels can exist in the cache, but the channel manager is allowed to
 * open more channels if requested. It just won't cache any number over the capacity.
 *
 * @since 3.0.M1
 */
public class PoolingChannelManager implements RmqChannelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolingChannelManager.class);

    private final RmqChannelManager delegate;
    private final ConcurrentMap<String, BlockingQueue<Channel>> channelPools;
    private final int perConnectionCapacity;

    public PoolingChannelManager(RmqChannelManager delegate, int perConnectionCapacity) {
        this.delegate = delegate;
        this.channelPools = new ConcurrentHashMap<>();
        this.perConnectionCapacity = perConnectionCapacity;
    }

    @Override
    public Channel createChannel(String connectionName) {

        BlockingQueue<Channel> pool = channelPools.computeIfAbsent(
                connectionName,
                n -> new LinkedBlockingQueue<>(perConnectionCapacity));

        Channel channel = pool.poll();

        // weak guarantees... not checking now many total channels are currently in use
        if (channel == null) {
            return createPoolableChannel(pool, connectionName);
        }

        channel.clearConfirmListeners();
        channel.clearReturnListeners();
        return channel;
    }

    public void close() {
        channelPools.values().forEach(this::closePool);
    }

    protected void closePool(BlockingQueue<Channel> pool) {
        List<Channel> localChannels = new ArrayList<>();
        pool.drainTo(localChannels);
        pool.forEach(this::closeChannel);
    }

    protected Channel createPoolableChannel(BlockingQueue<Channel> pool, String connectionName) {
        Channel rawChannel = delegate.createChannel(connectionName);
        return new PoolAwareChannel(rawChannel, pool);
    }

    protected void closeChannel(Channel channel) {
        if (channel.isOpen() && channel.getConnection().isOpen()) {

            LOGGER.debug("Closing channel {}", channel.getChannelNumber());
            try {
                channel.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing channel {}: {}", channel.getChannelNumber(), e.getMessage());
            }
        }
    }
}
