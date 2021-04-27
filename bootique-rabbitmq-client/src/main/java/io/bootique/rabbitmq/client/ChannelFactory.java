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

package io.bootique.rabbitmq.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.bootique.rabbitmq.client.connection.ConnectionManager;
import io.bootique.rabbitmq.client.exchange.ExchangeConfig;
import io.bootique.rabbitmq.client.queue.QueueConfig;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * An injectable singleton that is the main access point to the RabbitMQ client.
 */
public class ChannelFactory {

    private ConnectionManager connectionManager;
    private Map<String, ExchangeConfig> exchanges;
    private Map<String, QueueConfig> queues;

    public ChannelFactory(
            ConnectionManager connectionManager,
            Map<String, ExchangeConfig> exchanges,
            Map<String, QueueConfig> queues) {

        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.exchanges = Objects.requireNonNull(exchanges);
        this.queues = Objects.requireNonNull(queues);
    }

    /**
     * @since 2.0
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Opens a new RabbitMQ channel. Connection name must be referenced in configuration.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName) {

        Objects.requireNonNull(connectionName, "Null connection name");

        try {
            return connectionManager.forName(connectionName).createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a new RabbitMQ channel and declares an exchange. Connection and exchange names must be referenced in
     * configuration.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName) {

        RmqTopology.required(exchangeName, "No exchange name");
        ExchangeConfig exchangeConfig = exchanges.get(exchangeName);
        if (exchangeConfig == null) {
            // Have to throw, as unfortunately we can't create an exchange with default parameters.
            // We need to know its type at the minimum
            throw new IllegalStateException("No configuration present for exchange named '" + exchangeName + "'");
        }

        Channel channel = openChannel(connectionName);

        // TODO: track previously open exchanges to skip potential repeat declarations that slow us down

        try {
            exchangeConfig.exchangeDeclare(channel, exchangeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    /**
     * Opens a new RabbitMQ channel, declares an exchange and binds a named queue to the exchange. Connection, exchange
     * and queue names must be referenced in configuration.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName, String queueName) {
        return openChannel(connectionName, exchangeName, queueName, "");
    }

    /**
     * Opens a new RabbitMQ channel, declares an exchange and binds a named queue to the exchange. Connection, exchange
     * and queue names must be referenced in configuration.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName, String queueName, String routingKey) {

        RmqTopology.required(queueName, "No queue name");
        QueueConfig queueConfig = queues.containsKey(queueName)
                ? queues.get(queueName)
                // create a queue on the fly with default settings. TODO: print a warning?
                : new QueueConfig();

        Channel channel = openChannel(connectionName, exchangeName);
        try {
            queueConfig.queueDeclare(channel, queueName);
            channel.queueBind(queueName, exchangeName, routingKey);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    /**
     * @deprecated since 2.0 in favor of {@link #openChannel(String, String, String)}
     */
    @Deprecated
    public Channel openChannel(Connection connection, String exchangeName, String routingKey) {
        return doOpenChannel(connection, exchangeName, null, routingKey);
    }

    /**
     * @deprecated since 2.0 in favor of {@link #openChannel(String, String, String, String)}
     */
    @Deprecated
    public Channel openChannel(Connection connection, String exchangeName, String queueName, String routingKey) {
        return doOpenChannel(connection, exchangeName, queueName, routingKey);
    }

    @Deprecated
    private Channel doOpenChannel(Connection connection, String exchangeName, String queueName, String routingKey) {
        try {
            Channel channel = connection.createChannel();

            // This code assumes an exchange is present. While probably rare, there are cases when producer and consumer
            //  communicate directly over a queue, ignoring exchanges... This is why the method is deprecated
            exchangeDeclare(channel, exchangeName);

            if (queueName == null) {
                // This code is suspect. There are two distinct cases for when "queueName" is null:
                //  1. I am a producer and sending to an exchange. I don't need a queue
                //  2. I am a consumer for an exchange, and I need a fresh dynamically-named queue bound to an exchange
                //  Here we are addressing case #2, and creating unneeded queue for #1
                // This is why the method is deprecated
                queueName = channel.queueDeclare().getQueue();
            } else {
                queueDeclare(channel, queueName);
            }

            channel.queueBind(queueName, exchangeName, routingKey);
            return channel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    private void queueDeclare(Channel channel, String queueName) throws IOException {
        QueueConfig queueConfig = queues.computeIfAbsent(queueName, name -> {
            throw new IllegalStateException("No configuration present for Queue named '" + name + "'");
        });

        queueConfig.queueDeclare(channel, queueName);
    }

    @Deprecated
    private void exchangeDeclare(Channel channel, String exchangeName) throws IOException {
        ExchangeConfig exchangeConfig = exchanges.computeIfAbsent(exchangeName, name -> {
            throw new IllegalStateException("No configuration present for Exchange named '" + name + "'");
        });

        exchangeConfig.exchangeDeclare(channel, exchangeName);
    }

}
