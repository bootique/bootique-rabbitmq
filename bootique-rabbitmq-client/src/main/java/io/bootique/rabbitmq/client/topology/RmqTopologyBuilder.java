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
package io.bootique.rabbitmq.client.topology;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @since 2.0
 */
public class RmqTopologyBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqTopologyBuilder.class);

    private final Map<String, Consumer<Channel>> topologyActions;

    public RmqTopologyBuilder() {
        this.topologyActions = new LinkedHashMap<>();
    }

    /**
     * @deprecated since 3.0.M1 in favor of {@link #build(Channel)}
     */
    @Deprecated
    public Channel buildTopology(Channel channel) {
        build(channel);
        return channel;
    }

    /**
     * @since 3.0
     */
    public void build(Channel channel) {
        topologyActions.values().forEach(ta -> ta.accept(channel));
    }

    // TODO: deprecate?
    public RmqTopologyBuilder ensureExchange(String exchangeName) {
        return ensureExchange(exchangeName, new RmqExchangeConfigFactory().createConfig());
    }

    /**
     * @since 3.0
     */
    public RmqTopologyBuilder ensureExchange(String exchangeName, RmqExchangeConfig exchangeConfig) {
        RmqTopology.required(exchangeName, "Undefined exchange name");
        topologyActions.computeIfAbsent("e:" + exchangeName, k -> c -> exchangeConfig.exchangeDeclare(c, exchangeName));
        return this;
    }

    /**
     * Adds a topology action to establish a named queue with default settings (durable, non-exclusive, non-auto-delete).
     */
    // TODO: deprecate?
    public RmqTopologyBuilder ensureQueue(String queueName) {
        return ensureQueue(queueName, new RmqQueueConfigFactory().createConfig());
    }

    /**
     * Adds a topology action to create a named queue. Queue configuration is taken from "queueConfig"
     * parameter. If the config is null, an implicit default configuration is used. If the template name doesn't
     * correspond to an existing configuration, an exception is thrown.
     *
     * @since 3.0
     */
    public RmqTopologyBuilder ensureQueue(String queueName, RmqQueueConfig queueConfig) {
        RmqTopology.required(queueName, "Undefined queue name");
        Objects.requireNonNull(queueConfig, "'queueConfig' is null");
        topologyActions.computeIfAbsent("q:" + queueName, k -> c -> queueConfig.queueDeclare(c, queueName));
        return this;
    }

    public RmqTopologyBuilder ensureQueueBoundToExchange(String queueName, String exchangeName, String routingKey) {

        // TODO: should we remove the next two lines, as they may result in unexpected exchage/queue configurations?
        //   Instead we'd require an explicit call to 'ensureQueue(q, c)' and 'ensureExchange(e, c)'
        ensureQueue(queueName);
        ensureExchange(exchangeName);

        topologyActions.computeIfAbsent("eq:" + exchangeName + ":" + queueName + ":" + routingKey,
                k -> c -> queueBind(c, queueName, exchangeName, routingKey));

        return this;
    }

    protected void queueBind(Channel channel, String queueName, String exchangeName, String routingKey) {

        LOGGER.debug("binding queue '{}' to exchange '{}' with key '{}'", queueName, exchangeName, routingKey);

        try {
            channel.queueBind(queueName, exchangeName, routingKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}