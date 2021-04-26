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

package io.bootique.rabbitmq.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.junit5.BQTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@BQTest
public class TopicExchangeIT extends RabbitMQBaseTest {

    @Test
    public void testAmqpConfig() throws IOException, TimeoutException {
        BQRuntime runtime = testFactory
                .app("-c", "classpath:exchange-topic.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.rabbitmq.connections.c1.uri", rmq.getAmqpUrl()))
                .autoLoadModules()
                .createRuntime();

        assertCanSendAndReceive(runtime.getInstance(ChannelFactory.class));
    }

    private void assertCanSendAndReceive(ChannelFactory channelFactory) throws IOException, TimeoutException {
        try (Channel channel = channelFactory.openChannel("c1", "topicExchange")) {

            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, "topicExchange", "a.*");

            String message = "Hello World!";
            channel.basicPublish("topicExchange", "a.b", null, message.getBytes("UTF-8"));

            GetResponse response = channel.basicGet(queueName, false);
            assertNotNull(response);
            assertEquals(message, new String(response.getBody()));
        }
    }
}
