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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import io.bootique.rabbitmq.client.topology.RmqTopology;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @since 2.0.B1
 */
public class RmqMessageBuilder {

    private final RmqEndpointDriver driver;

    private String exchange;
    private String routingKey;
    private boolean mandatory;
    private boolean immediate;
    private AMQP.BasicProperties properties;

    protected RmqMessageBuilder(RmqEndpointDriver driver) {
        this.driver = Objects.requireNonNull(driver);
        this.exchange = "";
        this.routingKey = "";
    }

    public RmqMessageBuilder exchange(String exchange) {
        this.exchange = RmqTopology.normalizeName(exchange);
        return this;
    }

    /**
     * Sets routing key that should be used for message dispatching.
     */
    public RmqMessageBuilder routingKey(String routingKey) {
        this.routingKey = RmqTopology.normalizeName(routingKey);
        return this;
    }

    public RmqMessageBuilder mandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public RmqMessageBuilder immediate(boolean immediate) {
        this.immediate = immediate;
        return this;
    }

    public RmqMessageBuilder properties(AMQP.BasicProperties properties) {
        this.properties = Objects.requireNonNull(properties);
        return this;
    }

    public void publish(byte[] message) {

        Objects.requireNonNull(message);

        AMQP.BasicProperties properties = this.properties != null
                ? this.properties
                : MessageProperties.MINIMAL_BASIC;
        
        try (Channel channel = createChannelWithTopology()) {
            channel.basicPublish(exchange, routingKey, mandatory, immediate, properties, message);
        } catch (IOException e) {
            throw new RuntimeException("Error publishing RMQ message for connection: " + driver.getConnectionName(), e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout opening channel or publishing RMQ message for connection: " + driver.getConnectionName(), e);
        }
    }

    protected Channel createChannelWithTopology() {
        Channel channel = driver.createChannel();

        if (RmqTopology.isDefined(exchange)) {
            driver.newTopology().ensureExchange(exchange).build(channel);
        }

        return channel;
    }
}
