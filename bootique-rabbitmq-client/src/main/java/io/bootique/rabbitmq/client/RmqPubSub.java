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

import io.bootique.rabbitmq.client.pubsub.RmqPubEndpoint;
import io.bootique.rabbitmq.client.pubsub.RmqSubEndpoint;

import java.util.Map;

/**
 * An injectable singleton that is an entry point to preconfigured named publishing and subscription RMQ endpoints.
 * An "endpoint" is a Bootique abstraction intended to simplify configuration and working with RabbitMQ topologies -
 * connections, channels, exchanges, queues and routing keys.
 *
 * @since 2.0.B1
 */
public class RmqPubSub {

    private final Map<String, RmqPubEndpoint> pubEndpoints;
    private final Map<String, RmqSubEndpoint> subEndpoints;

    public RmqPubSub(Map<String, RmqPubEndpoint> pubEndpoints, Map<String, RmqSubEndpoint> subEndpoints) {
        this.pubEndpoints = pubEndpoints;
        this.subEndpoints = subEndpoints;
    }

    public RmqPubEndpoint pubEndpoint(String name) {
        RmqPubEndpoint endpoint = pubEndpoints.get(name);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unmapped RMQ publish endpoint: " + name);
        }

        return endpoint;
    }

    public RmqSubEndpoint subEndpoint(String name) {
        RmqSubEndpoint endpoint = subEndpoints.get(name);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unmapped RMQ subscription endpoint: " + name);
        }

        return endpoint;
    }
}
