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

package io.bootique.rabbitmq.client.connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A map of named RabbitMQ connections. Connections are created on demand and cached for further reuse. Configured via
 * YAML and injectable via DI. You'd rarely need to access connections directly, so the most useful method here is
 * {@link #createChannel(String)}.
 *
 * @since 2.0
 */
public class RmqConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmqConnectionManager.class);

    private final Map<String, ConnectionFactory> factories;
    private final Map<String, AtomicReference<Connection>> connections;
    private volatile boolean shutdown;

    public RmqConnectionManager(Map<String, ConnectionFactory> factories) {
        this.factories = Objects.requireNonNull(factories);

        // RabbitMQ connections are thread-safe and we can reuse them for parallel calls
        // https://www.rabbitmq.com/api-guide.html
        // so just create a fixed-size self-inflating cache of connections by name
        this.connections = new HashMap<>();
        factories.keySet().forEach(name -> connections.put(name, new AtomicReference<>()));
    }

    public Collection allNames() {
        return factories.keySet();
    }

    public Connection forName(String connectionName) {

        // states:
        // 1. no connection
        // 2. closed connection
        // 3. open connection

        AtomicReference<Connection> ref = connections.get(connectionName);
        if (ref == null) {
            throw new IllegalStateException("No configuration present for RabbitMQ connection named '" + connectionName + "'");
        }

        Connection c = ref.get();

        if (c == null || !c.isOpen()) {

            Connection newConnection = createConnection(connectionName);
            if (ref.compareAndSet(c, newConnection)) {
                c = newConnection;
            }
            // another thread just opened a connection.. assuming it is valid in the pool
            else {
                try {
                    newConnection.close();
                } catch (IOException e) {
                    // ignore...
                }

                c = Objects.requireNonNull(ref.get());
            }
        }

        return c;
    }

    /**
     * Creates and returns a new channel for a named connection.
     *
     * @since 3.0.M1
     */
    public Channel createChannel(String connectionName) {
        Objects.requireNonNull(connectionName, "'connectionName' is null");

        try {
            return forName(connectionName).createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {

        LOGGER.debug("Shutting down RabbitMQ connections...");

        // this will prevent new connections creation, and we'll drain the ones already created...
        this.shutdown = true;

        connections.values()
                .stream()
                .map(AtomicReference::get)
                .filter(r -> r != null && r.isOpen())
                .forEach(c -> {

                    try {

                        // checking for open state, as RMQ connections are verbose about multiple close attempts
                        if (c.isOpen()) {
                            c.close();
                        }

                    } catch (IOException e) {
                        // ignore...
                    }
                });
    }


    private Connection createConnection(String connectionName) {

        if (shutdown) {
            throw new IllegalStateException("ConnectionFactoryManaged was shutdown");
        }

        ConnectionFactory factory = factories.get(connectionName);
        if (factory == null) {
            throw new IllegalStateException("No factory present for Connection named '" + connectionName + "'");
        }

        LOGGER.debug("Creating named RabbitMQ connection '{}'", connectionName);
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(String.format("Can't create connection \"%s\".", connectionName), e);
        }
    }
}
