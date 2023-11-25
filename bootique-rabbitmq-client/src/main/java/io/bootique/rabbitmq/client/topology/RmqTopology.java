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

/**
 * Helps the rest of the code to validate and normalize RabbitMQ topology object names (exchanges, queue, routing
 * keys). For RabbitMQ both empty and null names are "undefined". This class helps to deal with this logic in the
 * Bootique code.
 *
 * @since 2.0
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
}
