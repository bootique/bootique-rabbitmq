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
package io.bootique.rabbitmq.junit5;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import io.bootique.rabbitmq.junit5.tester.ConnectionPropertyBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.RabbitMQContainer;

/**
 * @since 2.0
 */
public class RmqTester implements BQBeforeScopeCallback, BQAfterScopeCallback {

    // unlike JDBC ContainerTcDbTester, where the container is passed from the environment, here the container
    // is created and fully managed internally by RmqTester
    private RabbitMQContainer container;

    public static RmqTester create() {
        return new RmqTester(new RabbitMQContainer());
    }

    public static RmqTester create(String imageName) {
        return new RmqTester(new RabbitMQContainer(imageName));
    }

    protected RmqTester(RabbitMQContainer container) {
        this.container = container;
    }

    public String getAmqpUrl() {
        return container.getAmqpUrl();
    }

    public String getAmqpsUrl() {
        return container.getAmqpsUrl();
    }

    public int getAmqpPort() {
        return container.getAmqpPort();
    }

    public int getAmqpsPort() {
        return container.getAmqpsPort();
    }

    /**
     * Returns a Bootique module that can be used to configure a test RMQ connection in test {@link io.bootique.BQRuntime}.
     * This method can be used to initialize one or more BQRuntimes in a test class, so that they can share the RabbitMQ
     * broker managed by this tester.
     *
     * @param connectionName the name of the RMQ connection to create or replace in the target runtime
     * @return a new Bootique module with test RMQ connection configuration.
     */
    public BQModule moduleWithTestRmqClient(String connectionName) {
        return b -> configure(b, connectionName);
    }

    protected void configure(Binder binder, String connectionName) {
        bindSelf(binder, connectionName);
        configureConnection(binder, connectionName);
    }

    protected void bindSelf(Binder binder, String connectionName) {
        binder.bind(Key.get(RmqTester.class, connectionName)).toInstance(this);
    }

    protected void configureConnection(Binder binder, String connectionName) {
        ConnectionPropertyBuilder.create(binder, connectionName).property("type", "bqrmqtest");
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        container.stop();
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        container.start();
    }
}
