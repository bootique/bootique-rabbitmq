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

package io.bootique.rabbitmq.client.integration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.rabbitmq.client.ChannelFactory;
import io.bootique.test.junit.BQTestFactory;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Runs RabbitMQ and tests against real RMQ instance.
 *
 * @author Ibragimov Ruslan
 */
public class RabbitMQModuleIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQModuleIT.class);

    @ClassRule
    public static final RabbitMQContainer rmq = new RabbitMQContainer("rabbitmq:3.8-alpine");

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAmqpConfig() throws IOException, TimeoutException {
        LOGGER.info("Rabbit url: {}", rmq.getAmqpUrl());

        BQRuntime runtime = testFactory
                .app("--config=classpath:amqp.yml")
                .module(b ->
                        BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.bqConnection.port", String.valueOf(rmq.getAmqpPort())))
                .module(b -> b.bind(RabbitMqTester.class))
                .autoLoadModules()
                .createRuntime();

        runtime.getInstance(RabbitMqTester.class).assertCanSendAndReceive();
    }

    @Test
    public void testUriConfig() throws IOException, TimeoutException {
        LOGGER.info("Rabbit url: {}", rmq.getAmqpUrl());

        BQRuntime runtime = testFactory
                .app("--config=classpath:uri.yml")
                .module(b ->
                        BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.bqConnection.uri", rmq.getAmqpUrl()))
                .module(b -> b.bind(RabbitMqTester.class))
                .autoLoadModules()
                .createRuntime();

        runtime.getInstance(RabbitMqTester.class).assertCanSendAndReceive();
    }

    static class RabbitMqTester {

        private static final String CONNECTION_NAME = "bqConnection";
        private static final String EXCHANGE_NAME = "bqExchange";
        private static final String QUEUE_NAME = "bqQueue";

        private ChannelFactory channelFactory;

        @Inject
        public RabbitMqTester(ChannelFactory channelFactory) {
            this.channelFactory = channelFactory;
        }

        //
        //    Queue and Exchange names must be the same or an IOException is thrown:
        //            -- channel error; protocol method: #method<channel.close>
        //            (reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
        //
        public void assertCanSendAndReceive() throws IOException, TimeoutException {
            try (Channel channel = channelFactory.openChannel(CONNECTION_NAME, EXCHANGE_NAME, QUEUE_NAME, "")) {
                // RabbitMQ Exchange with "bqQueue" must exist or IOException is thrown
                byte[] message = "Hello World!".getBytes("UTF-8");
                channel.basicPublish("", QUEUE_NAME, null, message);
                GetResponse getResponse = channel.basicGet(QUEUE_NAME, false);

                assertEquals(new String(message), new String(getResponse.getBody()));
            }
        }
    }
}
