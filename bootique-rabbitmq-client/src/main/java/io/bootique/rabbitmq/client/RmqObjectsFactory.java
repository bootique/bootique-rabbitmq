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
import io.bootique.di.Injector;
import io.bootique.log.BootLogger;
import io.bootique.rabbitmq.client.channel.RmqChannelFactory;
import io.bootique.rabbitmq.client.connection.ConnectionFactoryFactory;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.topology.RmqExchange;
import io.bootique.rabbitmq.client.pubsub.RmqPubEndpoint;
import io.bootique.rabbitmq.client.pubsub.RmqPubEndpointFactory;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpoint;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpointFactory;
import io.bootique.rabbitmq.client.topology.RmqQueue;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;
import io.bootique.shutdown.ShutdownManager;

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

    private Map<String, ConnectionFactoryFactory> connections;
    private Map<String, RmqExchange> exchanges;
    private Map<String, RmqQueue> queues;
    private Map<String, RmqPubEndpointFactory> pub;
    private Map<String, RmqSubEndpointFactory> sub;

    /**
     * @since 3.0.M1
     */
    public RmqTopologyManager createTopologyManager() {
        return new RmqTopologyManager(
                exchanges != null ? exchanges : Collections.emptyMap(),
                queues != null ? queues : Collections.emptyMap());
    }

    public RmqEndpoints createEndpoints(RmqChannelFactory channelFactory, ShutdownManager shutdownManager) {
        return new RmqEndpoints(
                createPubEndpoints(channelFactory),
                createSubEndpoints(channelFactory, shutdownManager));
    }

    public RmqConnectionManager createConnectionManager(
            Injector injector,
            BootLogger bootLogger,
            ShutdownManager shutdownManager) {

        Map<String, ConnectionFactory> factories = createConnectionFactories(injector);

        RmqConnectionManager manager = new RmqConnectionManager(factories);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down RabbitMQ ConnectionManager...");
            manager.shutdown();
        });

        return manager;
    }

    protected Map<String, ConnectionFactory> createConnectionFactories(Injector injector) {
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ConnectionFactory> map = new HashMap<>();
        connections.forEach((k, v) -> map.put(k, v.createConnectionFactory(k, injector)));
        return map;
    }

    protected Map<String, RmqPubEndpoint> createPubEndpoints(RmqChannelFactory channelFactory) {
        if (pub == null || pub.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqPubEndpoint> map = new HashMap<>();
        pub.forEach((k, v) -> map.put(k, v.create(channelFactory)));
        return map;
    }

    protected Map<String, RmqSubEndpoint> createSubEndpoints(RmqChannelFactory channelFactory, ShutdownManager shutdownManager) {
        if (sub == null || sub.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RmqSubEndpoint> map = new HashMap<>();
        sub.forEach((k, v) -> map.put(k, v.create(channelFactory, shutdownManager)));
        return map;
    }

    @BQConfigProperty
    public void setConnections(Map<String, ConnectionFactoryFactory> connections) {
        this.connections = connections;
    }

    @BQConfigProperty("Configuration for RMQ exchanges. Exchanges are created lazily only when a channel is open that requires it")
    public void setExchanges(Map<String, RmqExchange> exchanges) {
        this.exchanges = exchanges;
    }

    @BQConfigProperty("Configuration for RMQ queues. Queues are created lazily only when a channel is open that requires it")
    public void setQueues(Map<String, RmqQueue> queues) {
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
}
