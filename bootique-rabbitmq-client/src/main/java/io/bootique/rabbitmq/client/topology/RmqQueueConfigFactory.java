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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.0.M1
 */
@BQConfig
public class RmqQueueConfigFactory {

    private static final String X_MESSAGE_TTL = "x-message-ttl";

    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
    private Map<String, Object> arguments;

    public RmqQueueConfigFactory() {
        this.durable = true;
    }

    @BQConfigProperty("Sets queue durability. The default is 'true'")
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @BQConfigProperty("Sets queue exclusivity. The default is 'false'")
    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    @BQConfigProperty("Sets queue auto-delete behavior. The default is 'false'")
    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    @BQConfigProperty("Sets additional arguments passed to the queue declaration. E.g. 'x-message-ttl', etc.")
    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public RmqQueueConfig createConfig() {
        return new RmqQueueConfig(durable, exclusive, autoDelete, cleanArguments());
    }

    // certain arguments can be bound as Strings in the map, whereas RMQ expects numbers for them.
    // Perform conversion for the known args
    protected Map<String, Object> cleanArguments() {

        if (arguments == null || arguments.isEmpty()) {
            return Collections.emptyMap();
        }

        Object xMessageTTL = arguments.get(X_MESSAGE_TTL);
        if (xMessageTTL == null || xMessageTTL instanceof Integer) {
            return arguments;
        }

        Map<String, Object> clean = new HashMap<>(arguments);

        try {
            clean.put(X_MESSAGE_TTL, Integer.valueOf(xMessageTTL.toString()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error converting '" + X_MESSAGE_TTL + "' queue argument to an Integer: " + xMessageTTL, e);
        }

        return clean;
    }
}
