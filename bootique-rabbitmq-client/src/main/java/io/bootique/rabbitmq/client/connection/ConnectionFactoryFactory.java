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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rabbitmq.client.ConnectionFactory;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.di.Injector;

@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = AMQPConnectionFactoryFactory.class)
public abstract class ConnectionFactoryFactory implements PolymorphicConfiguration {

    private Integer requestedChannelMax;
    private Integer requestedFrameMax;
    private Integer requestedHeartbeat;
    private Integer connectionTimeout;
    private Integer handshakeTimeout;
    private Integer shutdownTimeout;
    private Boolean automaticRecoveryEnabled;
    private Boolean topologyRecovery;
    private Long networkRecoveryInterval;

    public ConnectionFactory createConnectionFactory(String connectionName, Injector injector) {
        return configureFactory(new ConnectionFactory(), connectionName, injector);
    }

    protected ConnectionFactory configureFactory(ConnectionFactory factory, String connectionName, Injector injector) {

        // let's preserve RMQ defaults if parameters are not setup explicitly
        if (requestedChannelMax != null) {
            factory.setRequestedChannelMax(requestedChannelMax);
        }

        if (requestedFrameMax != null) {
            factory.setRequestedFrameMax(requestedFrameMax);
        }

        if (requestedHeartbeat != null) {
            factory.setRequestedHeartbeat(requestedHeartbeat);
        }

        if (connectionTimeout != null) {
            factory.setConnectionTimeout(connectionTimeout);
        }

        if (handshakeTimeout != null) {
            factory.setHandshakeTimeout(handshakeTimeout);
        }

        if (shutdownTimeout != null) {
            factory.setShutdownTimeout(shutdownTimeout);
        }

        if(automaticRecoveryEnabled != null) {
            factory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);
        }

        if(topologyRecovery != null) {
            factory.setTopologyRecoveryEnabled(topologyRecovery);
        }

        if(networkRecoveryInterval != null) {
            factory.setNetworkRecoveryInterval(networkRecoveryInterval);
        }

        return factory;
    }

    @BQConfigProperty
    public void setRequestedChannelMax(int requestedChannelMax) {
        this.requestedChannelMax = requestedChannelMax;
    }

    @BQConfigProperty
    public void setRequestedFrameMax(int requestedFrameMax) {
        this.requestedFrameMax = requestedFrameMax;
    }

    @BQConfigProperty
    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    @BQConfigProperty("Connection timeout in milliseconds.")
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @BQConfigProperty("Handshake timeout in milliseconds.")
    public void setHandshakeTimeout(int handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
    }

    @BQConfigProperty("Shutdown timeout in milliseconds.")
    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    @BQConfigProperty
    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    @BQConfigProperty
    public void setTopologyRecovery(boolean topologyRecovery) {
        this.topologyRecovery = topologyRecovery;
    }

    @BQConfigProperty
    public void setNetworkRecoveryInterval(long networkRecoveryInterval) {
        this.networkRecoveryInterval = networkRecoveryInterval;
    }
}
