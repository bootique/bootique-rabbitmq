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
import java.util.function.Consumer;

/**
 * @since 2.0.B1
 */
public class RmqTopologyBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqTopologyBuilder.class);

    private final Map<String, RmqExchange> exchangeConfigs;
    private final Map<String, RmqQueue> queueConfigs;
    private final TopologyCache topologyCache;

    private final Map<String, Consumer<Channel>> topologyActions;

    public RmqTopologyBuilder(
            Map<String, RmqExchange> exchangeConfigs,
            Map<String, RmqQueue> queueConfigs,
            TopologyCache topologyCache) {
        this.exchangeConfigs = exchangeConfigs;
        this.queueConfigs = queueConfigs;
        this.topologyActions = new LinkedHashMap<>();
        this.topologyCache = topologyCache;
    }

    protected RmqTopologyActions buildActions() {
        return new RmqTopologyActions(topologyActions.keySet(), topologyActions.values());
    }

    /**
     * @deprecated since 3.0.M1 in favor of {@link #create(Channel, boolean)}
     */
    @Deprecated
    public Channel buildTopology(Channel channel) {
        create(channel, true);
        return channel;
    }

    /**
     * Creates the topology defined in the builder on the broker. Will do nothing if "force" is "false", and the
     * topology was previously created in the app.
     *
     * @since 3.0.M1
     */
    public boolean create(Channel channel, boolean force) {
        RmqTopologyActions actions = buildActions();

        if (topologyCache.save(actions) || force) {

            LOGGER.debug("Creating or updating an RMQ topology {}", actions.getKey());

            try {
                actions.apply(channel);
            } catch (Exception e) {
                topologyCache.evict(actions);
                LOGGER.warn("error creating topology", e);
                throw new RuntimeException(e);
            }
            return true;

        } else {
            LOGGER.debug("Reusing an existing RMQ topology {}", actions.getKey());
            return false;
        }
    }

    public RmqTopologyBuilder ensureExchange(String exchangeName) {
        RmqTopology.required(exchangeName, "Undefined exchange name");
        topologyActions.computeIfAbsent("e:" + exchangeName, k -> c -> exchangeDeclare(c, exchangeName));
        return this;
    }

    public RmqTopologyBuilder ensureQueue(String queueName) {
        RmqTopology.required(queueName, "Undefined queue name");
        topologyActions.computeIfAbsent("q:" + queueName, k -> c -> queueDeclare(c, queueName));
        return this;
    }

    public RmqTopologyBuilder ensureQueueBoundToExchange(String queueName, String exchangeName, String routingKey) {

        ensureExchange(exchangeName);
        ensureQueue(queueName);

        topologyActions.computeIfAbsent("eq:" + exchangeName + ":" + queueName + ":" + routingKey,
                k -> c -> queueBind(c, queueName, exchangeName, routingKey));

        return this;
    }

    protected void exchangeDeclare(Channel channel, String exchangeName) {

        RmqExchange exchange = exchangeConfigs.get(exchangeName);
        if (exchange == null) {
            // Have to throw, as unfortunately we can't create an exchange with default parameters.
            // We need to know its type at the minimum
            throw new IllegalStateException("No configuration present for exchange named '" + exchangeName + "'");
        }

        LOGGER.debug("declaring exchange '{}'", exchangeName);

        try {
            exchange.exchangeDeclare(channel, exchangeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void queueDeclare(Channel channel, String queueName) {
        RmqQueue queue = queueConfigs.containsKey(queueName)
                ? queueConfigs.get(queueName)
                // create a queue on the fly with default settings.
                // TODO: print a warning?
                : new RmqQueue();

        LOGGER.debug("declaring queue '{}'", queueName);

        try {
            queue.queueDeclare(channel, queueName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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