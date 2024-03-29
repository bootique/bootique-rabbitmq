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
import com.rabbitmq.client.GetResponse;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.rabbitmq.client.unit.RabbitMQBaseTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RmqConnectionManagerIT extends RabbitMQBaseTest {

    @Test
    public void uri_Defaults() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app()
                .module(b -> BQCoreModule.extend(b)
                        // WHEN no explicit connections parameter are set beyond the URI
                        .setProperty("bq.rabbitmq.connections.c1.type", "uri")
                        .setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        assertCanSendAndReceive(runtime.getInstance(RmqConnectionManager.class));
    }

    @Test
    public void amqpConfig() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:connection-amqp.yml")
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.rabbitmq.connections.c1.port", String.valueOf(rmq.getAmqpPort())))
                .autoLoadModules()
                .createRuntime();

        assertCanSendAndReceive(runtime.getInstance(RmqConnectionManager.class));
    }

    @Test
    public void uriConfig() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:connection-uri.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        assertCanSendAndReceive(runtime.getInstance(RmqConnectionManager.class));
    }

    private void assertCanSendAndReceive(RmqConnectionManager connectionManager) throws IOException, TimeoutException {
        try (Connection connection = connectionManager.forName("c1")) {
            try (Channel channel = connection.openChannel().get()) {

                String queue = channel.queueDeclare().getQueue();
                String message = "Hello World!";
                channel.basicPublish("", queue, null, message.getBytes("UTF-8"));
                GetResponse getResponse = channel.basicGet(queue, false);
                assertEquals(message, new String(getResponse.getBody()));
            }
        }
    }
}
