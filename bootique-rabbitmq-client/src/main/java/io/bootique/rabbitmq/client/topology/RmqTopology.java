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

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A set of actions to generate or restore a certains RabbitMQ exchanges/queues topology. Also contains static helper
 * methods to validate and normalize topology object names (exchanges, queue, routing keys). For RabbitMQ both empty
 * and null names are "undefined". This class helps to deal with this logic in the Bootique code.
 *
 * @since 2.0.B1
 */
public class RmqTopology {

    /**
     * Throws if the "topologyObjectName" is undefined. Used to validated required topology parameters.
     */
    public static String required(String topologyObjectName, String message) {
        if (!isDefined(topologyObjectName)) {
            throw new IllegalArgumentException(message);
        }

        return topologyObjectName;
    }

    public static String normalizeName(String topologyObjectName) {
        return topologyObjectName == null ? "" : topologyObjectName;
    }

    public static boolean isDefined(String topologyObjectName) {
        return topologyObjectName != null && topologyObjectName.length() > 0;
    }

    private final Map<String, Consumer<Channel>> actionsByTopologyObject;

    /**
     * @since 3.0.M1
     */
    // keeping constructor non-public, as the keys in the provided map have special meaning defined in RmqTopologyBuilder
    protected RmqTopology(Map<String, Consumer<Channel>> actionsByTopologyObject) {
        this.actionsByTopologyObject = actionsByTopologyObject;
    }

    /**
     * @since 3.0.M1
     */
    public void ensureTopology(Channel channel) {
        actionsByTopologyObject.values().forEach(ta -> ta.accept(channel));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RmqTopology that = (RmqTopology) o;

        // comparing topologies by the key set. Unique topology action names  are defined by the RmqTopologyBuilder
        return actionsByTopologyObject.keySet().equals(that.actionsByTopologyObject.keySet());
    }

    @Override
    public int hashCode() {
        // comparing topologies by the key set. Unique topology action names  are defined by the RmqTopologyBuilder
        return Objects.hash(actionsByTopologyObject.keySet());
    }

    @Override
    public String toString() {
        return String.valueOf(actionsByTopologyObject.keySet());
    }
}
