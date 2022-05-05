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

import java.util.Objects;

/**
 * @since 2.0.B1
 */
@BQConfig
public class RmqPubEndpointFactory {

    private String connection;
    private String exchange;
    private String routingKey;

    public RmqPubEndpoint create(RmqChannelManager channelManager) {
        Objects.requireNonNull(connection, "Publisher connection name is undefined");

        RmqEndpointDriver driver = new RmqEndpointDriver(channelManager, connection);
        return new RmqPubEndpoint(driver, exchange, routingKey);
    }

    @BQConfigProperty
    public void setConnection(String connection) {
        this.connection = connection;
    }

    @BQConfigProperty("Default exchange name to be used for message dispatch")
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @BQConfigProperty("Default routing key to be used for message dispatch")
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}

