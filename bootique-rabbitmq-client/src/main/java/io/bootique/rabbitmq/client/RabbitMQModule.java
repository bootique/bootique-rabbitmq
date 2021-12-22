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

import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Injector;
import io.bootique.di.Provides;
import io.bootique.log.BootLogger;
import io.bootique.rabbitmq.client.channel.RmqChannelFactory;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

public class RabbitMQModule extends ConfigModule {

    @Singleton
    @Provides
    RmqObjectsFactory provideRmqObjectsFactory(ConfigurationFactory configFactory) {
        return config(RmqObjectsFactory.class, configFactory);
    }

    @Singleton
    @Provides
    RmqConnectionManager provideConnectionManager(
            RmqObjectsFactory factory,
            BootLogger bootLogger,
            ShutdownManager shutdownManager,
            Injector injector) {
        return factory.createConnectionManager(injector, bootLogger, shutdownManager);
    }

    @Singleton
    @Provides
    RmqTopologyManager provideTopologyManager(RmqObjectsFactory factory) {
        return factory.createTopologyManager();
    }

    @Deprecated
    @Singleton
    @Provides
    RmqChannelFactory provideChannelFactory(RmqConnectionManager connectionManager, RmqTopologyManager topologyManager) {
        return new RmqChannelFactory(connectionManager, topologyManager);
    }

    @Singleton
    @Provides
    RmqEndpoints provideEndpoints(
            RmqObjectsFactory factory,
            RmqConnectionManager connectionManager,
            RmqTopologyManager topologyManager,
            ShutdownManager shutdownManager) {
        return factory.createEndpoints(connectionManager, topologyManager, shutdownManager);
    }
}
