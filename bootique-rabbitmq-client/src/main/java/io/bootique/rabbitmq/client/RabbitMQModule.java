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

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.rabbitmq.client.channel.RmqChannelManager;
import io.bootique.rabbitmq.client.connection.RmqConnectionManager;
import io.bootique.rabbitmq.client.topology.RmqTopologyManager;

import javax.inject.Singleton;

public class RabbitMQModule implements BQModule {

    private static final String CONFIG_PREFIX = "rabbitmq";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates RabbitMQ client library")
                .config(CONFIG_PREFIX, RmqObjectsFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Singleton
    @Provides
    RmqObjectsFactory provideRmqObjectsFactory(ConfigurationFactory configFactory) {
        return configFactory.config(RmqObjectsFactory.class, CONFIG_PREFIX);
    }

    @Singleton
    @Provides
    RmqConnectionManager provideConnectionManager(RmqObjectsFactory factory) {
        return factory.createConnectionManager();
    }

    @Singleton
    @Provides
    RmqChannelManager provideChannelManager(RmqObjectsFactory factory, RmqConnectionManager connectionManager) {
        return factory.createChannelManager(connectionManager);
    }

    @Singleton
    @Provides
    RmqTopologyManager provideTopologyManager(RmqObjectsFactory factory) {
        return factory.createTopologyManager();
    }

    @Singleton
    @Provides
    RmqEndpoints provideEndpoints(
            RmqObjectsFactory factory,
            RmqChannelManager channelManager,
            RmqTopologyManager topologyManager) {
        return factory.createEndpoints(channelManager, topologyManager);
    }
}
