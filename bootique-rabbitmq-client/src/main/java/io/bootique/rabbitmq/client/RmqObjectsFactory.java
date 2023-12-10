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

import com.rabbitmq.client.ConnectionFactory;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.rabbitmq.client.channel.PoolingChannelManager;
import io.bootique.rabbitmq.client.channel.RmqChannelManager;
import io.bootique.rabbitmq.client.channel.SimpleChannelManager;
import io.bootique.rabbitmq.client.connection.ConnectionFactoryFactory;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.pubsub.RmqPubEndpoint;
import io.bootique.rabbitmq.client.pubsub.RmqPubEndpointFactory;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpoint;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpointFactory;
import io.bootique.rabbitmq.client.topology.*;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Bootique factory for RMQ connection, topology, publisher and consumer objects.
 *
 * @since 2.0
 */
@BQConfig
public class RmqObjectsFactory {

    private final ShutdownManager shutdownManager;

    private Map<String, ConnectionFactoryFactory> connections;
    private Map<String, RmqExchangeConfigFactory> exchanges;
    private Map<String, RmqQueueConfigFactory> queues;
    private Map<String, RmqPubEndpointFactory> pub;
    private Map<String, RmqSubEndpointFactory> sub;
    private int channelPoolCapacity;

    @Inject
    public RmqObjectsFactory(ShutdownManager shutdownManager) {
        this.shutdownManager = shutdownManager;
    }

    public RmqConnectionManager createConnectionManager() {
        Map<String, ConnectionFactory> factories = createConnectionFactories();
        return shutdownManager.onShutdown(
                new RmqConnectionManager(factories),
                RmqConnectionManager::shutdown);
    }

    /**
     * @since 3.0
     */
    public RmqChannelManager createChannelManager(RmqConnectionManager connectionManager) {
        SimpleChannelManager nonPoolingManager = new SimpleChannelManager(connectionManager);
        return channelPoolCapacity > 0
                ? new PoolingChannelManager(nonPoolingManager, channelPoolCapacity)
                : nonPoolingManager;
    }

    /**
     * @since 3.0
     */
    public RmqTopologyManager createTopologyManager() {
        return new RmqTopologyManager(
                createExchangeConfigs(),
                createQueueConfigs());
    }

    public RmqEndpoints createEndpoints(RmqChannelManager channelManager, RmqTopologyManager topologyManager) {
        return new RmqEndpoints(
                createPubEndpoints(channelManager, topologyManager),
                createSubEndpoints(channelManager, topologyManager));
    }

    protected Map<String, RmqQueueConfig> createQueueConfigs() {
        if (queues == null || queues.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqQueueConfig> map = new HashMap<>();
        queues.forEach((k, v) -> map.put(k, v.createConfig()));
        return map;
    }

    protected Map<String, RmqExchangeConfig> createExchangeConfigs() {
        if (exchanges == null || exchanges.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqExchangeConfig> map = new HashMap<>();
        exchanges.forEach((k, v) -> map.put(k, v.createConfig()));
        return map;
    }

    protected Map<String, ConnectionFactory> createConnectionFactories() {
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ConnectionFactory> map = new HashMap<>();
        connections.forEach((k, v) -> map.put(k, v.createConnectionFactory(k)));
        return map;
    }

    protected Map<String, RmqPubEndpoint> createPubEndpoints(
            RmqChannelManager channelManager,
            RmqTopologyManager topologyManager) {

        if (pub == null || pub.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqPubEndpoint> map = new HashMap<>();
        pub.forEach((k, v) -> map.put(k, v.create(channelManager, topologyManager)));
        return map;
    }

    protected Map<String, RmqSubEndpoint> createSubEndpoints(RmqChannelManager channelManager, RmqTopologyManager topologyManager) {

        if (sub == null || sub.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqSubEndpoint> map = new HashMap<>();
        sub.forEach((k, v) -> map.put(k, v.create(channelManager, topologyManager)));
        return map;
    }

    @BQConfigProperty
    public void setConnections(Map<String, ConnectionFactoryFactory> connections) {
        this.connections = connections;
    }

    @BQConfigProperty("Configuration for RMQ exchanges. Exchanges are created lazily only when a channel is open that requires it")
    public void setExchanges(Map<String, RmqExchangeConfigFactory> exchanges) {
        this.exchanges = exchanges;
    }

    @BQConfigProperty("Configuration for RMQ queues. Queues are created lazily only when a channel is open that requires it")
    public void setQueues(Map<String, RmqQueueConfigFactory> queues) {
        this.queues = queues;
    }

    @BQConfigProperty
    public void setPub(Map<String, RmqPubEndpointFactory> pub) {
        this.pub = pub;
    }

    @BQConfigProperty
    public void setSub(Map<String, RmqSubEndpointFactory> sub) {
        this.sub = sub;
    }

    /**
     * @since 3.0
     */
    @BQConfigProperty("Per-Connection Channel pool capacity. If set to a value greater than zero, Bootique would pool " +
            "and reuse Channels. Default is zero, i.e. no pooling")
    public void setChannelPoolCapacity(int channelPoolCapacity) {
        this.channelPoolCapacity = channelPoolCapacity;
    }
}
