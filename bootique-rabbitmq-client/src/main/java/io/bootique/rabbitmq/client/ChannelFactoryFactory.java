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
import io.bootique.rabbitmq.client.connection.ConnectionManager;
import io.bootique.rabbitmq.client.exchange.ExchangeConfig;
import io.bootique.rabbitmq.client.queue.QueueConfig;
import io.bootique.rabbitmq.client.connection.ConnectionFactoryFactory;
import io.bootique.shutdown.ShutdownManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
@BQConfig
public class ChannelFactoryFactory {

    private Map<String, ConnectionFactoryFactory> connections;
    private Map<String, ExchangeConfig> exchanges;
    private Map<String, QueueConfig> queues;

    public ChannelFactory createChannelFactory(BootLogger bootLogger, ShutdownManager shutdownManager, Injector injector) {
        Map<String, ConnectionFactory> factories = createConnectionFactories(injector);

        return new ChannelFactory(
                createConnectionManager(factories, bootLogger, shutdownManager),
                exchanges != null ? exchanges : Collections.emptyMap(),
                queues != null ? queues : Collections.emptyMap());
    }

    protected Map<String, ConnectionFactory> createConnectionFactories(Injector injector) {
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ConnectionFactory> map = new HashMap<>();
        connections.forEach((k, v) -> map.put(k, v.createConnectionFactory(k, injector)));
        return map;
    }

    protected ConnectionManager createConnectionManager(
            Map<String, ConnectionFactory> connectionFactories,
            BootLogger bootLogger,
            ShutdownManager shutdownManager) {

        ConnectionManager manager = new ConnectionManager(connectionFactories);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down RabbitMQ ConnectionManager...");
            manager.shutdown();
        });

        return manager;
    }

    @BQConfigProperty
    public void setConnections(Map<String, ConnectionFactoryFactory> connections) {
        this.connections = connections;
    }

    @BQConfigProperty
    public void setExchanges(Map<String, ExchangeConfig> exchanges) {
        this.exchanges = exchanges;
    }

    @BQConfigProperty
    public void setQueues(Map<String, QueueConfig> queues) {
        this.queues = queues;
    }
}
