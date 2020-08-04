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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.log.BootLogger;
import io.bootique.rabbitmq.client.config.ExchangeConfig;
import io.bootique.rabbitmq.client.config.QueueConfig;
import io.bootique.rabbitmq.client.config.ConnectionConfig;
import io.bootique.shutdown.ShutdownManager;

import java.util.Map;

/**
 * @since 2.0
 */
@BQConfig
public class ChannelFactoryFactory {

    private Map<String, ConnectionConfig> connections;
    private Map<String, ExchangeConfig> exchanges;
    private Map<String, QueueConfig> queues;

    public ChannelFactory createChannelFactory(BootLogger bootLogger, ShutdownManager shutdownManager) {
        ConnectionFactory connectionFactory = createConnectionFactory(bootLogger, shutdownManager);
        return new ChannelFactory(connectionFactory, exchanges, queues);
    }

    protected ConnectionFactory createConnectionFactory(BootLogger bootLogger, ShutdownManager shutdownManager) {
        ConnectionFactory factory = new ConnectionFactory(connections);
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "shutting down RabbitMQ ConnectionFactory...");
            factory.shutdown();
        });

        return factory;
    }


    @BQConfigProperty
    public void setConnections(Map<String, ConnectionConfig> connections) {
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