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

import io.bootique.rabbitmq.client.topology.RmqTopology;

import java.util.Objects;

/**
 * A stateless RabbitMQ message publisher with preconfigured target exchange and routing key.
 *
 * @since 2.0.B1
 */
public class RmqPubEndpoint {

    private final RmqEndpointDriver driver;
    private final String defaultExchange;
    private final String defaultRoutingKey;

    public RmqPubEndpoint(
            RmqEndpointDriver driver,
            String defaultExchange,
            String defaultRoutingKey) {

        this.driver = Objects.requireNonNull(driver);
        this.defaultExchange = RmqTopology.normalizeName(defaultExchange);
        this.defaultRoutingKey = RmqTopology.normalizeName(defaultRoutingKey);
    }

    /**
     * Creates a message builder initialized with this endpoint settings. Callers may customize routing and send
     * parameters before publishing a message.
     */
    public RmqMessageBuilder newMessage() {
        return new RmqMessageBuilder(driver).exchange(defaultExchange).routingKey(defaultRoutingKey);
    }

    /**
     * Publishes a message to RabbitMQ using the default endpoint settings.
     */
    public void publish(byte[] message) {
        newMessage().publish(message);
    }
}
