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
package io.bootique.rabbitmq.client.pubsub;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.rabbitmq.client.channel.RmqChannelManager;
import io.bootique.rabbitmq.client.topology.*;
import io.bootique.shutdown.ShutdownManager;

import java.util.Objects;

/**
 * @since 2.0
 */
@BQConfig
public class RmqSubEndpointFactory {

    private String connection;
    private String exchangeConfig;
    private String exchangeName;
    private String queueConfig;
    private String queueName;
    private String routingKey;
    private boolean autoAck = true;

    public RmqSubEndpoint create(
            RmqChannelManager channelManager,
            RmqTopologyManager topologyManager,
            ShutdownManager shutdownManager) {

        Objects.requireNonNull(connection, "Subscriber connection name is undefined");
        RmqEndpointDriver driver = new RmqEndpointDriver(channelManager, connection);

        RmqSubEndpoint endpoint = new RmqSubEndpoint(
                driver,
                createExchangeConfig(topologyManager),
                createQueueConfig(topologyManager),
                queueName,
                exchangeName,
                routingKey,
                autoAck);

        return shutdownManager.onShutdown(endpoint, RmqSubEndpoint::close);
    }

    protected RmqExchangeConfig createExchangeConfig(RmqTopologyManager topologyManager) {
        return this.exchangeConfig != null
                ? topologyManager.getExchangeConfig(exchangeConfig)
                : new RmqExchangeConfigFactory().createConfig();
    }

    protected RmqQueueConfig createQueueConfig(RmqTopologyManager topologyManager) {
        return this.queueConfig != null
                ? topologyManager.getQueueConfig(queueConfig)
                : new RmqQueueConfigFactory().createConfig();
    }

    @BQConfigProperty
    public void setConnection(String connection) {
        this.connection = connection;
    }

    /**
     * @since 3.0
     */
    @BQConfigProperty("An optional reference to an exchange config declared in 'rabbitmq.exchanges'. By default a 'topic' exchange is assumed")
    public void setExchangeConfig(String exchangeConfig) {
        this.exchangeConfig = exchangeConfig;
    }

    /**
     * @since 3.0
     */
    @BQConfigProperty
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    /**
     * @since 3.0
     */
    @BQConfigProperty("An optional reference to a queue config declared in 'rabbitmq.queues'")
    public void setQueueConfig(String queueConfig) {
        this.queueConfig = queueConfig;
    }

    /**
     * @since 3.0
     */
    @BQConfigProperty
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @BQConfigProperty
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @BQConfigProperty("Whether to auto-acknowledge message delivery. The default is 'true'")
    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }
}

