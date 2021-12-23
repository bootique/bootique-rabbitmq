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

import java.util.Map;
import java.util.Objects;

/**
 * Allows creating RabbitMQ queue/exchange topologies with bindings between them. Exchange and queue properties are
 * taken from configuration.
 *
 * @since 3.0.M1
 */
public class RmqTopologyManager {

    private final Map<String, RmqExchange> exchanges;
    private final Map<String, RmqQueue> queues;
    private final TopologyCache topologyCache;

    public RmqTopologyManager(Map<String, RmqExchange> exchanges, Map<String, RmqQueue> queues) {
        this.exchanges = Objects.requireNonNull(exchanges);
        this.queues = Objects.requireNonNull(queues);

        // TODO: make this configurable?
        this.topologyCache = new TopologyCache(100);
    }

    public RmqTopologyBuilder newTopology() {
        return new RmqTopologyBuilder(exchanges, queues, topologyCache);
    }

    public void clearCache() {
        topologyCache.clear();
    }
}
