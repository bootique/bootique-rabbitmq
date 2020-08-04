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

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.bootique.rabbitmq.client.ChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A map of named RabbitMQ connections. Configured via YAML and accessed via {@link ChannelFactory#getConnectionManager()}.
 * Though you'd rarely need to access the connection directly. It is too low-level and RabbitMQ access in Bootique is
 * done via {@link ChannelFactory}.
 *
 * @since 2.0
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private Map<String, ConnectionFactory> factories;
    private ConcurrentMap<String, AtomicReference<Connection>> connections;
    private volatile boolean shutdown;

    public ConnectionManager(Map<String, ConnectionFactory> factories) {
        this.factories = Objects.requireNonNull(factories);

        // RabbitMQ connections are thread-safe and we can reuse them for parallel calls
        // https://www.rabbitmq.com/api-guide.html
        // so just create a fixed-size self-inflating cache of connections by name
        this.connections = new ConcurrentHashMap<>();
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
            throw new IllegalStateException("No configuration present for Connection named '" + connectionName + "'");
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

    public void shutdown() {

        LOGGER.info("Shutting down RabbitMQ connections...");

        // this will prevent new connections creation, and we'll drain the ones already created...
        this.shutdown = true;

        connections.values()
                .stream()
                .map(AtomicReference::get)
                .filter(r -> r != null && r.isOpen())
                .forEach(c -> {
                    try {
                        c.close();
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

        LOGGER.info("Creating named RabbitMQ connection '{}'", connectionName);
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(String.format("Can't create connection \"%s\".", connectionName), e);
        }
    }
}
