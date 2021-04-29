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

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import io.bootique.rabbitmq.client.ChannelFactory;
import io.bootique.rabbitmq.client.topology.RmqTopology;

import java.util.Objects;

/**
 * @since 2.0.B1
 */
public class RmqSubEndpoint {

    private final ChannelFactory channelFactory;
    private final String connectionName;
    private final String defaultExchange;
    private final String defaultQueue;
    private final String defaultRoutingKey;
    private final boolean defaultAutoAck;

    public RmqSubEndpoint(
            ChannelFactory channelFactory,
            String connectionName,
            String defaultExchange,
            String defaultQueue,
            String defaultRoutingKey,
            boolean defaultAutoAck) {

        this.channelFactory = Objects.requireNonNull(channelFactory);
        this.connectionName = Objects.requireNonNull(connectionName);
        this.defaultQueue = RmqTopology.normalizeName(defaultQueue);
        this.defaultExchange = RmqTopology.normalizeName(defaultExchange);
        this.defaultRoutingKey = RmqTopology.normalizeName(defaultRoutingKey);
        this.defaultAutoAck = defaultAutoAck;
    }

    /**
     * Creates a subscription builder initialized with this endpoint settings. Callers may customize RMQ topology
     * before subscribing a consumer.
     */
    public RmqSubBuilder newSubscription() {
        return new RmqSubBuilder(channelFactory, connectionName)
                .exchange(defaultExchange)
                .queue(defaultQueue)
                .routingKey(defaultRoutingKey)
                .autoAck(defaultAutoAck);
    }

    /**
     * Registers a delivery consumer to listen to messages on the queue described by this endpoint.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String consume(DeliverCallback onDeliver) {
        return newSubscription().consume(onDeliver);
    }

    /**
     * Registers delivery and cancellation consumers to listen to messages on the queue described by this endpoint.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String consume(DeliverCallback onDeliver, CancelCallback onCancel) {
        return newSubscription().consume(onDeliver, onCancel);
    }

    /**
     * Registers a consumer to listen to messages on the queue described by this endpoint.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String consume(Consumer consumer) {
        return newSubscription().consume(consumer);
    }
}
