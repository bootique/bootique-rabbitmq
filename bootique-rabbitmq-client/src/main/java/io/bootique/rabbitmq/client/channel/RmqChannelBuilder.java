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
package io.bootique.rabbitmq.client.channel;

import com.rabbitmq.client.Channel;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.topology.RmqTopologyBuilder;

import java.util.Objects;

/**
 * Builder of a Channel and the underlying exchange and queue topology.
 *
 * @since 2.0.B1
 * @deprecated since 3.0.M1, as channels (without topology) are created via {@link RmqConnectionManager}, and topology
 * is created via {@link io.bootique.rabbitmq.client.topology.RmqTopologyManager}.
 */
@Deprecated
public class RmqChannelBuilder {

    private final RmqChannelManager channelManager;
    private final RmqTopologyBuilder topologyBuilder;

    private String connectionName;

    public RmqChannelBuilder(
            RmqChannelManager channelManager,
            RmqTopologyBuilder topologyBuilder) {

        this.channelManager = channelManager;
        this.topologyBuilder = topologyBuilder;
    }

    public RmqChannelBuilder connectionName(String connectionName) {
        this.connectionName = connectionName;
        return this;
    }

    public RmqChannelBuilder ensureExchange(String exchangeName) {
        topologyBuilder.ensureExchange(exchangeName);
        return this;
    }

    public RmqChannelBuilder ensureQueue(String queueName) {
        topologyBuilder.ensureQueue(queueName);
        return this;
    }

    public RmqChannelBuilder ensureQueueBoundToExchange(String queueName, String exchangeName, String routingKey) {
        topologyBuilder.ensureQueueBoundToExchange(queueName, exchangeName, routingKey);
        return this;
    }

    public Channel open() {
        return topologyBuilder.buildTopology(createChannel());
    }

    protected Channel createChannel() {
        Objects.requireNonNull(connectionName, "'connectionName' is not set");
        return channelManager.createChannel(connectionName);
    }
}
