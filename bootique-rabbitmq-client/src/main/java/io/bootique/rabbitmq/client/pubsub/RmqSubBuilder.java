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

import com.rabbitmq.client.*;
import io.bootique.rabbitmq.client.channel.RmqChannelBuilder;
import io.bootique.rabbitmq.client.channel.RmqChannelFactory;
import io.bootique.rabbitmq.client.topology.RmqTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Builds a single RMQ subscription for a queue. Allows to configure RMQ topology for that queue. Internally manages
 * opening one Channel per subscription. The open Channel is registered with the parent endpoint and can be closed if
 * the subscription is canceled.
 *
 * @since 2.0.B1
 */
public class RmqSubBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqSubBuilder.class);

    private final RmqChannelFactory channelFactory;
    private final String connectionName;
    private final Map<String, Channel> consumerChannels;

    private String exchange;
    private String queue;
    private String routingKey;
    private boolean autoAck;

    protected RmqSubBuilder(RmqChannelFactory channelFactory, String connectionName, Map<String, Channel> consumerChannels) {
        this.channelFactory = channelFactory;
        this.connectionName = connectionName;
        this.consumerChannels = consumerChannels;
    }

    public RmqSubBuilder exchange(String exchange) {
        this.exchange = RmqTopology.normalizeName(exchange);
        return this;
    }

    public RmqSubBuilder queue(String queue) {
        this.queue = RmqTopology.normalizeName(queue);
        return this;
    }

    public RmqSubBuilder routingKey(String routingKey) {
        this.routingKey = RmqTopology.normalizeName(routingKey);
        return this;
    }

    public RmqSubBuilder autoAck(boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }

    /**
     * Registers delivery and cancellation consumers to listen to messages on the queue configured by this builder.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(DeliverCallback onDeliver) {
        return subscribe(onDeliver, t -> LOGGER.debug("Subscription was canceled for '{}'", t));
    }

    /**
     * Registers delivery and cancellation consumers to listen to messages on the queue configured by this builder.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(DeliverCallback onDeliver, CancelCallback onCancel) {
        return subscribe(new DeliverOrCancelConsumer(onDeliver, onCancel));
    }

    /**
     * Registers a consumer to listen to messages on the queue configured by this builder.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(Consumer consumer) {

        Channel channel = createChannelWithTopology();
        String consumerTag;

        try {
            consumerTag = channel.basicConsume(queue, autoAck, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Error publishing RMQ message for connection: " + connectionName, e);
        }

        // track consumer channel to be able to stop consumers and close channels
        consumerChannels.put(consumerTag, channel);

        return consumerTag;
    }

    protected Channel createChannelWithTopology() {

        RmqTopology.required(queue, "Consumer queue is not defined");

        RmqChannelBuilder builder = channelFactory.newChannel(connectionName);
        if (RmqTopology.isDefined(exchange)) {
            builder.ensureQueueBoundToExchange(queue, exchange, routingKey);
        } else {
            builder.ensureQueue(queue);
        }

        return builder.open();
    }

    private static class DeliverOrCancelConsumer implements Consumer {
        private final CancelCallback onCancel;
        private final DeliverCallback onDeliver;

        public DeliverOrCancelConsumer(DeliverCallback onDeliver, CancelCallback onCancel) {
            this.onCancel = onCancel;
            this.onDeliver = onDeliver;
        }

        @Override
        public void handleConsumeOk(String consumerTag) {

        }

        @Override
        public void handleCancelOk(String consumerTag) {

        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            onCancel.handle(consumerTag);
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

        }

        @Override
        public void handleRecoverOk(String consumerTag) {

        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            onDeliver.handle(consumerTag, new Delivery(envelope, properties, body));
        }
    }
}
