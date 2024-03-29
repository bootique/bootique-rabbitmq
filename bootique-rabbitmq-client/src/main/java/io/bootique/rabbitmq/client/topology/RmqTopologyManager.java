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
 * @since 3.0
 */
public class RmqTopologyManager {

    private final Map<String, RmqExchangeConfig> exchangeConfigs;
    private final Map<String, RmqQueueConfig> queueConfigs;

    public RmqTopologyManager(Map<String, RmqExchangeConfig> exchangeConfigs, Map<String, RmqQueueConfig> queueConfigs) {
        this.exchangeConfigs = Objects.requireNonNull(exchangeConfigs);
        this.queueConfigs = Objects.requireNonNull(queueConfigs);
    }

    public RmqQueueConfig getQueueConfig(String name) {
        RmqQueueConfig config = queueConfigs.get(name);
        if (config == null) {
            throw new IllegalArgumentException("Unknown queue config name: " + name);
        }

        return config;
    }

    public RmqExchangeConfig getExchangeConfig(String name) {
        RmqExchangeConfig config = exchangeConfigs.get(name);
        if (config == null) {
            throw new IllegalArgumentException("Unknown exchange config name: " + name);
        }

        return config;
    }
}
