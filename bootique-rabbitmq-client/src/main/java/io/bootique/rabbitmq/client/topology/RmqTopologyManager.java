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
 * Allows creating RabbitMQ queue/exchange topologies with bindings between them. Exhange and queue properties are
 * taken from configuration.
 *
 * @since 3.0.M1
 */
public class RmqTopologyManager {

    private final Map<String, RmqExchange> exchanges;
    private final Map<String, RmqQueueTemplate> queueTemplates;

    public RmqTopologyManager(Map<String, RmqExchange> exchanges, Map<String, RmqQueueTemplate> queueTemplates) {
        this.exchanges = Objects.requireNonNull(exchanges);
        this.queueTemplates = Objects.requireNonNull(queueTemplates);
    }

    public RmqTopologyBuilder newTopology() {
        return new RmqTopologyBuilder(exchanges, queueTemplates);
    }

    public RmqQueueTemplate getQueueTemplate(String name) {
        RmqQueueTemplate template = queueTemplates.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Unknown queue template name: " + name);
        }

        return template;
    }
}
