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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @since 3.0.M1
 */
public class RmqQueueConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqQueueConfig.class);

    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDelete;
    private final Map<String, Object> arguments;

    public RmqQueueConfig(
            boolean durable,
            boolean exclusive,
            boolean autoDelete,
            Map<String, Object> arguments) {

        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;
        this.arguments = Objects.requireNonNull(arguments);
    }

    public void queueDeclare(Channel channel, String queueName) {

        // TODO: empty queues names are quite valid in RMQ. The broker would assign the name in this case
        //   https://www.rabbitmq.com/queues.html#server-named-queues
        //   Also see a note in RmqSubBuilder for why we can't (yet) have server-named queues
        RmqTopology.required(queueName, "Undefined queue name");

        LOGGER.debug("declaring queue '{}'", queueName);

        try {
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
