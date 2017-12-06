package io.bootique.rabbitmq.client.channel;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Map;

public class ChannelFactory {
    private Map<String, ExchangeConfig> exchanges;
    private Map<String, QueueConfig> queues;

    public ChannelFactory(Map<String, ExchangeConfig> exchanges, Map<String, QueueConfig> queues) {
        this.exchanges = exchanges;
        this.queues = queues;
    }

    /**
     * TODO: Comment what this method should do (and actually do)
     */
    public Channel openChannel(Connection connection, String exchangeName, String routingKey) {
        return openChannel(connection, exchangeName, null, routingKey);
    }

    /**
     * TODO: Comment what this method should do (and actually do)
     * Create channel and bind queue to exchange.
     */
    public Channel openChannel(Connection connection, String exchangeName, String queueName, String routingKey) {
        try {
            Channel channel = connection.createChannel();
            exchangeDeclare(channel, exchangeName);

            if (queueName == null) {
                queueName = channel.queueDeclare().getQueue();
            } else {
                queueDeclare(channel, queueName);
            }

            channel.queueBind(queueName, exchangeName, routingKey);
            return  channel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void queueDeclare(Channel channel, String queueName) throws IOException {
        QueueConfig queueConfig = queues.computeIfAbsent(queueName, name -> {
            throw new IllegalStateException("No configuration present for Queue named '" + name + "'");
        });

        queueConfig.queueDeclare(channel, queueName);
    }

    private void exchangeDeclare(Channel channel, String exchangeName) throws IOException {
        ExchangeConfig exchangeConfig = exchanges.computeIfAbsent(exchangeName, name -> {
            throw new IllegalStateException("No configuration present for Exchange named '" + name + "'");
        });

        exchangeConfig.exchangeDeclare(channel, exchangeName);
    }
}
