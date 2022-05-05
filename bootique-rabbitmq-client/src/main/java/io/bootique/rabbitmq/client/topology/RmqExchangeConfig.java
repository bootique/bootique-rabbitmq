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

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class RmqExchangeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqExchangeConfig.class);

    private final BuiltinExchangeType type;
    private final boolean durable;
    private final boolean autoDelete;
    private final boolean internal;
    private final Map<String, Object> arguments;

    public RmqExchangeConfig(
            BuiltinExchangeType type,
            boolean durable,
            boolean autoDelete,
            boolean internal,
            Map<String, Object> arguments) {

        this.type = Objects.requireNonNull(type);
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.internal = internal;
        this.arguments = arguments;
    }

    public void exchangeDeclare(Channel channel, String exchangeName) {
        RmqTopology.required(exchangeName, "Undefined exchange name");

        LOGGER.debug("declaring exchange '{}'", exchangeName);

        try {
            channel.exchangeDeclare(exchangeName, type, durable, autoDelete, internal, arguments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
