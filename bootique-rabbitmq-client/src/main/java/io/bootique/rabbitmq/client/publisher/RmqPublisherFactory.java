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
package io.bootique.rabbitmq.client.publisher;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.rabbitmq.client.ChannelFactory;

import java.util.Objects;

/**
 * @since 2.0.B1
 */
@BQConfig
public class RmqPublisherFactory {

    private String connection;
    private String exchange;
    private String routingKey;

    public RmqPublisher create(ChannelFactory channelFactory) {
        Objects.requireNonNull(connection, "Publisher connection name is undefined");
        return new RmqPublisher(channelFactory, connection, exchange, routingKey);
    }

    @BQConfigProperty
    public void setConnection(String connection) {
        this.connection = connection;
    }

    @BQConfigProperty
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @BQConfigProperty
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}

