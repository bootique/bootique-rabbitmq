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
package io.bootique.rabbitmq.client.consumer;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import io.bootique.rabbitmq.client.ChannelBuilder;
import io.bootique.rabbitmq.client.ChannelFactory;
import io.bootique.rabbitmq.client.topology.RmqTopology;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @since 2.0.B1
 */
public class RmqConsumerBuilder {

    private final ChannelFactory channelFactory;
    private final String connectionName;

    private String exchange;
    private String queue;
    private String routingKey;
    private boolean autoAck;

    public RmqConsumerBuilder(ChannelFactory channelFactory, String connectionName) {
        this.channelFactory = channelFactory;
        this.connectionName = connectionName;
    }

    public RmqConsumerBuilder exchange(String exchange) {
        this.exchange = RmqTopology.normalizeName(exchange);
        return this;
    }

    public RmqConsumerBuilder queue(String queue) {
        this.queue = RmqTopology.normalizeName(queue);
        return this;
    }

    public RmqConsumerBuilder routingKey(String routingKey) {
        this.routingKey = RmqTopology.normalizeName(routingKey);
        return this;
    }

    public RmqConsumerBuilder autoAck(boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }

    /**
     * Registers delivery and cancelation consumers to listen to messages on the queue configured by this builder.
     */
    public void consume(DeliverCallback onDeliver, CancelCallback onCancel) {

        // TODO: creating and closing a new channel after subscribing a single consumer is fairly inefficient,
        //  though we can transparently address it in the ChannelFactory with channel pooling

        try (Channel channel = createChannelWithTopology()) {
            channel.basicConsume(queue, autoAck, onDeliver, onCancel);
        } catch (IOException e) {
            throw new RuntimeException("Error publishing RMQ message for connection: " + connectionName, e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout publishing RMQ message for connection: " + connectionName, e);
        }
    }

    /**
     * Registers a consumer to listen to messages on the queue configured by this builder.
     */
    public void consume(Consumer consumer) {

        // TODO: creating and closing a new channel after subscribing a single consumer is fairly inefficient,
        //  though we can transparently address it in the ChannelFactory with channel pooling

        try (Channel channel = createChannelWithTopology()) {
            channel.basicConsume(queue, autoAck, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Error publishing RMQ message for connection: " + connectionName, e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout publishing RMQ message for connection: " + connectionName, e);
        }
    }

    protected Channel createChannelWithTopology() {

        RmqTopology.required(queue, "Consumer queue is not defined");

        ChannelBuilder builder = channelFactory.newChannel(connectionName);
        if (RmqTopology.isDefined(exchange)) {
            builder.ensureQueueBoundToExchange(queue, exchange, routingKey);
        } else {
            builder.ensureQueue(queue);
        }

        return builder.open();
    }

}
