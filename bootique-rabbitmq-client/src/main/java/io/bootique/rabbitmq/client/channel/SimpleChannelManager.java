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
package io.bootique.rabbitmq.client.channel;

import com.rabbitmq.client.Channel;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;

import java.io.IOException;
import java.util.Objects;

/**
 * @since 3.0.M1
 */
public class SimpleChannelManager implements RmqChannelManager {

    private final RmqConnectionManager connectionManager;

    public SimpleChannelManager(RmqConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Channel createChannel(String connectionName) {
        Objects.requireNonNull(connectionName, "'connectionName' is null");

        try {
            return connectionManager.forName(connectionName).createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
