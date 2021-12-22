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

import com.rabbitmq.client.Channel;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.topology.RmqTopologyBuilder;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;

/**
 * @since 3.0.M1
 */
public class RmqEndpointDriver {

    private final RmqConnectionManager connectionManager;
    private final RmqTopologyManager topologyManager;
    private final String connectionName;

    public RmqEndpointDriver(
            RmqConnectionManager connectionManager,
            RmqTopologyManager topologyManager,
            String connectionName) {
        this.connectionManager = connectionManager;
        this.topologyManager = topologyManager;
        this.connectionName = connectionName;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public Channel createChannel() {
        return connectionManager.createChannel(connectionName);
    }

    public RmqTopologyBuilder newTopology() {
        return topologyManager.newTopology();
    }
}