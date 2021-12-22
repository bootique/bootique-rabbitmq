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
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;

import java.util.Objects;

/**
 * An injectable singleton that is the main access point to the RabbitMQ client.
 *
 * @deprecated since 3.0.M1, as channels (without topology) are created via {@link RmqChannelManager}, and topologies
 * are created via {@link RmqTopologyManager}.
 */
@Deprecated
public class RmqChannelFactory {

    private final RmqChannelManager channelManager;
    private final RmqTopologyManager topologyManager;

    public RmqChannelFactory(
            RmqChannelManager channelManager,
            RmqTopologyManager topologyManager) {

        this.channelManager = Objects.requireNonNull(channelManager);
        this.topologyManager = Objects.requireNonNull(topologyManager);
    }

    /**
     * Opens a new RabbitMQ channel. Connection name must be referenced in configuration.
     *
     * @since 2.0
     */
    public Channel openChannel(String connectionName) {
        return newChannel(connectionName).open();
    }

    /**
     * Returns a channel builder object that allows to open a channel and configure a desired RMQ topology
     * (exchanges, queues).
     *
     * @since 2.0
     */
    public RmqChannelBuilder newChannel(String connectionName) {
        return new RmqChannelBuilder(channelManager, topologyManager.newTopology()).connectionName(connectionName);
    }
}
