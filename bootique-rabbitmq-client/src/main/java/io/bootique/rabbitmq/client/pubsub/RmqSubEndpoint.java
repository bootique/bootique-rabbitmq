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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * @since 2.0.B1
 */
public class RmqSubEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqSubEndpoint.class);

    private final RmqEndpointDriver driver;
    private final RmqExchangeConfig exchangeConfig;
    private final RmqQueueConfig queueConfig;
    private final String defaultQueue;
    private final String defaultExchange;
    private final String defaultRoutingKey;
    private final boolean defaultAutoAck;

    private final Map<String, Channel> consumerChannels;

    public RmqSubEndpoint(
            RmqEndpointDriver driver,
            RmqExchangeConfig exchangeConfig,
            RmqQueueConfig queueConfig,
            String defaultQueue,
            String defaultExchange,
            String defaultRoutingKey,
            boolean defaultAutoAck) {

        this.driver = Objects.requireNonNull(driver);
        this.exchangeConfig = Objects.requireNonNull(exchangeConfig);
        this.queueConfig = Objects.requireNonNull(queueConfig);

        this.defaultQueue = RmqTopology.normalizeName(defaultQueue);
        this.defaultExchange = RmqTopology.normalizeName(defaultExchange);
        this.defaultRoutingKey = RmqTopology.normalizeName(defaultRoutingKey);
        this.defaultAutoAck = defaultAutoAck;

        // this stores channels that were assigned to consumers (one per consumer),
        // so that we can control closing them..
        this.consumerChannels = new ConcurrentHashMap<>();
    }

    public int getSubscriptionsCount() {
        return consumerChannels.size();
    }

    /**
     * Closes all endpoint consumer channels
     */
    public void close() {

        if (consumerChannels.size() > 0) {
            LOGGER.debug("Closing {} subscriber channels", consumerChannels.size());

            // presumably it is ok to close channels without canceling subscriptions
            consumerChannels.values().forEach(c -> {
                try {
                    c.close();
                } catch (AlreadyClosedException e) {
                    // This will likely happen every time, as the underlying RmqConnectionManager is
                    //  shutdown before the endpoints are.
                } catch (IOException e) {
                    LOGGER.warn("Error closing a Channel", e);
                } catch (TimeoutException e) {
                    LOGGER.warn("Timeout closing a Channel", e);
                }
            });

            // TODO: no mechanism to prevent new subs during shutdown
            consumerChannels.clear();
        }
    }

    /**
     * Cancels previously created subscription identified by "consumerTag" returned from one of the "consume" methods.
     */
    public void cancelSubscription(String consumerTag) {

        // must close the channel after cancel, as endpoints are based on "one channel per subscription" model
        try (Channel channel = consumerChannels.remove(consumerTag)) {
            if (channel != null) {
                channel.basicCancel(consumerTag);
            }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a subscription builder initialized with this endpoint settings. Callers may customize RMQ topology
     * before subscribing a consumer.
     */
    public RmqSubBuilder newSubscription() {
        return new RmqSubBuilder(driver, consumerChannels, exchangeConfig, queueConfig)
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
    public String subscribe(DeliverCallback onDeliver) {
        return newSubscription().subscribe(onDeliver);
    }

    /**
     * Registers delivery and cancellation consumers to listen to messages on the queue described by this endpoint.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(DeliverCallback onDeliver, CancelCallback onCancel) {
        return newSubscription().subscribe(onDeliver, onCancel);
    }

    /**
     * Registers a consumer to listen to messages on the queue described by this endpoint.
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(Consumer consumer) {
        return newSubscription().subscribe(consumer);
    }

    /**
     * Registers a consumer to listen to messages on the queue described by this endpoint. A consumer is created using
     * the provided lambda, and can reference the subscription Channel (e.g. for explicit delivery "ack").
     *
     * @return consumer tag returned by the server that can be used to cancel a consumer.
     */
    public String subscribe(Function<Channel, Consumer> consumerFactory) {
        return newSubscription().subscribe(consumerFactory);
    }
}
