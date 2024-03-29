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
import io.bootique.rabbitmq.client.topology.RmqExchangeConfig;
import io.bootique.rabbitmq.client.topology.RmqQueueConfig;
import io.bootique.rabbitmq.client.topology.RmqTopology;
import io.bootique.rabbitmq.client.topology.RmqTopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builds a single RMQ subscription for a queue. Allows to configure RMQ topology for that queue. Internally manages
 * opening one Channel per subscription. The open Channel is registered with the parent endpoint and can be closed if
 * the subscription is canceled.
 *
 * @since 2.0
 */
public class RmqSubBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqSubBuilder.class);

    private final RmqEndpointDriver driver;
    private final Map<String, Channel> consumerChannels;
    private final RmqExchangeConfig exchangeConfig;
    private final RmqQueueConfig queueConfig;

    private String exchangeName;
    private String queueName;
    private String routingKey;
    private boolean autoAck;

    protected RmqSubBuilder(
            RmqEndpointDriver driver,
            Map<String, Channel> consumerChannels,
            RmqExchangeConfig exchangeConfig,
            RmqQueueConfig queueConfig) {

        this.driver = Objects.requireNonNull(driver);
        this.consumerChannels = Objects.requireNonNull(consumerChannels);
        this.exchangeConfig = Objects.requireNonNull(exchangeConfig);
        this.queueConfig = Objects.requireNonNull(queueConfig);
    }

    /**
     * Redefines the exchange name for the subscription. Despite renaming, Exchange properties are still taken from the
     * original exchange config associated with the endpoint.
     *
     * @since 3.0
     */
    public RmqSubBuilder exchangeName(String exchangeName) {
        this.exchangeName = RmqTopology.normalizeName(exchangeName);
        return this;
    }

    /**
     * Redefines the queue name for the subscription. Despite renaming, Queue properties are still taken from the
     * original queue config associated with the endpoint.
     *
     * @since 3.0
     */
    public RmqSubBuilder queueName(String queueName) {
        this.queueName = RmqTopology.normalizeName(queueName);
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
        return subscribe(c -> consumer);
    }

    /**
     * Registers a consumer to listen to messages on the queue configured by this builder. A consumer is created using
     * the provided lambda, and can reference the subscription Channel (e.g. for explicit delivery "ack").
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(Function<Channel, Consumer> consumerFactory) {
        Channel channel = createChannelWithTopology();
        Consumer consumer = consumerFactory.apply(channel);
        String consumerTag;

        try {
            consumerTag = channel.basicConsume(queueName, autoAck, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Error publishing RMQ message for connection: " + driver.getConnectionName(), e);
        }

        // track consumer channel to be able to stop consumers and close channels
        consumerChannels.put(consumerTag, channel);

        return consumerTag;
    }

    protected Channel createChannelWithTopology() {

        // TODO: empty queues names are quite valid in RMQ. The broker would assign the name in this case
        //   https://www.rabbitmq.com/queues.html#server-named-queues
        //   Though for server-named queues to work, we'd need to capture the generated queue name
        //   to bind it to the exchange

        RmqTopology.required(queueName, "Consumer queue is not defined");

        RmqTopologyBuilder topologyBuilder = new RmqTopologyBuilder()
                .ensureQueue(queueName, queueConfig);

        if (RmqTopology.isDefined(exchangeName)) {
            topologyBuilder.ensureExchange(exchangeName, exchangeConfig);
            topologyBuilder.ensureQueueBoundToExchange(queueName, exchangeName, routingKey);
        }

        Channel channel = driver.createChannel();
        topologyBuilder.build(channel);
        return channel;
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
