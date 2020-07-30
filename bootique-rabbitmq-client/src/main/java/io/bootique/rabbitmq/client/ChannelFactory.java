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
import io.bootique.rabbitmq.client.config.ExchangeConfig;
import io.bootique.rabbitmq.client.config.QueueConfig;

import java.io.IOException;
import java.util.Map;

/**
 * An injectable singleton that is the main access point to the RabbitMQ client.
 */
public class ChannelFactory {

    private ConnectionFactory connectionFactory;
    private Map<String, ExchangeConfig> exchanges;
    private Map<String, QueueConfig> queues;

    public ChannelFactory(
            ConnectionFactory connectionFactory,
            Map<String, ExchangeConfig> exchanges,
            Map<String, QueueConfig> queues) {

        this.connectionFactory = connectionFactory;
        this.exchanges = exchanges;
        this.queues = queues;
    }

    /**
     * @since 2.0
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Provides a channel for communication with RabbitMQ.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName) {
        return doOpenChannel(connectionFactory.forName(connectionName), exchangeName, null, "");
    }

    /**
     * Provides a channel for communication with RabbitMQ.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName, String routingKey) {
        return doOpenChannel(connectionFactory.forName(connectionName), exchangeName, null, routingKey);
    }

    /**
     * Provides a channel for communication with RabbitMQ.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName, String exchangeName, String queueName, String routingKey) {
        return doOpenChannel(connectionFactory.forName(connectionName), exchangeName, queueName, routingKey);
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

    protected Channel doOpenChannel(Connection connection, String exchangeName, String queueName, String routingKey) {
        try {
            Channel channel = connection.createChannel();
            exchangeDeclare(channel, exchangeName);

            if (queueName == null) {
                // TODO: this code is suspect. There are two distinct cases for when "queueName" is null:
                //  1. I am a producer and sending to an exchange. I don't need a queue
                //  2. I am a consumer for an exchange, and I need a fresh dynamically-named queue bound to an exchange
                //  Here we are addressing case #2, and creating unneeded queue for #1
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

    private void queueDeclare(Channel channel, String queueName) throws IOException {
        QueueConfig queueConfig = queues.computeIfAbsent(queueName, name -> {
            throw new IllegalStateException("No configuration present for Queue named '" + name + "'");
        });

        queueConfig.queueDeclare(channel, queueName);
    }

    private void exchangeDeclare(Channel channel, String exchangeName) throws IOException {
        ExchangeConfig exchangeConfig = exchanges.computeIfAbsent(exchangeName, name -> {
            throw new IllegalStateException("No configuration present for Exchange named '" + name + "'");
        });

        exchangeConfig.exchangeDeclare(channel, exchangeName);
    }
}
