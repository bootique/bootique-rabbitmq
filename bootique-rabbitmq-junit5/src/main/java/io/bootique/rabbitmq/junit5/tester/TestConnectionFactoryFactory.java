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
package io.bootique.rabbitmq.junit5.tester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.rabbitmq.client.ConnectionFactory;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.rabbitmq.client.connection.ConnectionFactoryFactory;
import io.bootique.rabbitmq.junit5.RmqTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

@JsonTypeName("bqrmqtest")
// must be able to deserialize over the existing configs, so instruct Jackson to be lenient
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestConnectionFactoryFactory extends ConnectionFactoryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionFactoryFactory.class);

    private final Injector injector;

    @Inject
    public TestConnectionFactoryFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    protected ConnectionFactory configureFactory(ConnectionFactory factory, String connectionName) {

        RmqTester tester = injector.getInstance(Key.get(RmqTester.class, connectionName));
        String url = tester.getAmqpUrl();

        LOGGER.info("Connecting to RabbitMQ at {}", url);

        try {
            factory.setUri(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RabbitMQ URI connection factory", e);
        }

        return super.configureFactory(factory, connectionName);
    }
}
