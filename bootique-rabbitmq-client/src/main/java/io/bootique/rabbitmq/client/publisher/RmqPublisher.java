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

import io.bootique.rabbitmq.client.ChannelFactory;
import io.bootique.rabbitmq.client.RmqTopology;

import java.util.Objects;

/**
 * @since 2.0.B1
 */
public class RmqPublisher {

    private final ChannelFactory channelFactory;
    private final String connectionName;
    private final String defaultExchange;
    private final String defaultRoutingKey;

    public RmqPublisher(ChannelFactory channelFactory, String connectionName, String defaultExchange, String defaultRoutingKey) {
        this.channelFactory = Objects.requireNonNull(channelFactory);
        this.connectionName = Objects.requireNonNull(connectionName);
        this.defaultExchange = RmqTopology.normalizeName(defaultExchange);
        this.defaultRoutingKey = RmqTopology.normalizeName(defaultRoutingKey);
    }

    /**
     * Creates a message builder initialized with this publisher settings. Callers may customize routing and send
     * parameters before publishing a message.
     */
    public RmqMessageBuilder newMessage() {
        return new RmqMessageBuilder(channelFactory, connectionName)
                .exchange(defaultExchange)
                .routingKey(defaultRoutingKey);
    }

    /**
     * Publishes a message to RabbitMQ using the default publisher settings.
     */
    public void publish(byte[] message) {
        newMessage().publish(message);
    }

}
