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
import io.bootique.rabbitmq.client.topology.RmqExchangeConfig;
import io.bootique.rabbitmq.client.topology.RmqExchangeConfigFactory;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;

import java.util.Objects;

/**
 * @since 2.0
 */
@BQConfig
public class RmqPubEndpointFactory {

    private String connection;
    private String exchangeConfig;
    private String exchangeName;
    private String routingKey;

    public RmqPubEndpoint create(RmqChannelManager channelManager, RmqTopologyManager topologyManager) {

        Objects.requireNonNull(connection, "Publisher 'connection' is undefined");
        Objects.requireNonNull(exchangeName, "Publisher 'exchangeName' is undefined");

        return new RmqPubEndpoint(
                new RmqEndpointDriver(channelManager, connection),
                createExchangeConfig(topologyManager),
                exchangeName,
                routingKey);
    }

    protected RmqExchangeConfig createExchangeConfig(RmqTopologyManager topologyManager) {
        return this.exchangeConfig != null
                ? topologyManager.getExchangeConfig(exchangeConfig)
                : new RmqExchangeConfigFactory().createConfig();
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
    @BQConfigProperty("Default exchange name to be used for message dispatch")
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    @BQConfigProperty("Default routing key to be used for message dispatch")
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}

