package io.bootique.rabbitmq.client.connection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rabbitmq.client.Connection;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class ConnectionConfig implements PolymorphicConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionConfig.class);

    private int requestedChannelMax;
    private int requestedFrameMax;
    private int requestedHeartbeat;
    private int connectionTimeout;
    private int handshakeTimeout;
    private int shutdownTimeout;

    private boolean automaticRecoveryEnabled;
    private boolean topologyRecovery;

    private long networkRecoveryInterval;

    protected abstract com.rabbitmq.client.ConnectionFactory createConnectionFactory();

    public Connection createConnection(String connectionName) {
        com.rabbitmq.client.ConnectionFactory factory = createConnectionFactory();

        factory.setRequestedChannelMax(requestedChannelMax);
        factory.setRequestedFrameMax(requestedFrameMax);
        factory.setRequestedHeartbeat(requestedHeartbeat);
        factory.setConnectionTimeout(connectionTimeout);
        factory.setHandshakeTimeout(handshakeTimeout);
        factory.setShutdownTimeout(shutdownTimeout);
        factory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);
        factory.setTopologyRecoveryEnabled(topologyRecovery);
        factory.setNetworkRecoveryInterval(networkRecoveryInterval);

        LOGGER.info("Creating RabbitMQ connection.");
        try {
            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(String.format("Can't create connection \"%s\".", connectionName) , e);
        }
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
