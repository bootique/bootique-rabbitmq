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

@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = AMQPConnectionFactoryFactory.class)
public abstract class ConnectionFactoryFactory implements PolymorphicConfiguration {

    private int requestedChannelMax;
    private int requestedFrameMax;
    private int requestedHeartbeat;
    private int connectionTimeout;
    private int handshakeTimeout;
    private int shutdownTimeout;

    private boolean automaticRecoveryEnabled;
    private boolean topologyRecovery;

    private long networkRecoveryInterval;


    public ConnectionFactory createConnectionFactory() {
        return configureFactory(new ConnectionFactory());
    }

    protected ConnectionFactory configureFactory(ConnectionFactory factory) {
        factory.setRequestedChannelMax(requestedChannelMax);
        factory.setRequestedFrameMax(requestedFrameMax);
        factory.setRequestedHeartbeat(requestedHeartbeat);
        factory.setConnectionTimeout(connectionTimeout);
        factory.setHandshakeTimeout(handshakeTimeout);
        factory.setShutdownTimeout(shutdownTimeout);
        factory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);
        factory.setTopologyRecoveryEnabled(topologyRecovery);
        factory.setNetworkRecoveryInterval(networkRecoveryInterval);
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
