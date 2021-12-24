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
package io.bootique.rabbitmq.client.channel;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * An RMQ Channel that returns itself to the pool upon closing.
 *
 * @since 3.0.M1
 */
public class PoolAwareChannel implements Channel {

    final Channel delegate;
    private final BlockingQueue<Channel> pool;
    private boolean open;

    public PoolAwareChannel(Channel delegate, BlockingQueue<Channel> pool) {
        this.delegate = delegate;
        this.pool = pool;
        this.open = delegate.isOpen();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException, TimeoutException {
        // return open to the pool if we can, otherwise close
        boolean accepted = pool.offer(delegate);
        if (!accepted) {
            delegate.close();
        }

        this.open = false;
    }

    @Override
    public void close(int closeCode, String closeMessage) throws IOException, TimeoutException {
        // if we are closing with code and message, do not return to the pool, instead do the real close
        this.delegate.close(closeCode, closeMessage);
        this.open = false;
    }

    @Override
    public int getChannelNumber() {
        return delegate.getChannelNumber();
    }

    @Override
    public Connection getConnection() {
        return delegate.getConnection();
    }

    @Override
    public void abort() throws IOException {
        delegate.abort();
    }

    @Override
    public void abort(int closeCode, String closeMessage) throws IOException {
        delegate.abort(closeCode, closeMessage);
    }

    @Override
    public void addReturnListener(ReturnListener listener) {
        delegate.addReturnListener(listener);
    }

    @Override
    public ReturnListener addReturnListener(ReturnCallback returnCallback) {
        return delegate.addReturnListener(returnCallback);
    }

    @Override
    public boolean removeReturnListener(ReturnListener listener) {
        return delegate.removeReturnListener(listener);
    }

    @Override
    public void clearReturnListeners() {
        delegate.clearReturnListeners();
    }

    @Override
    public void addConfirmListener(ConfirmListener listener) {
        delegate.addConfirmListener(listener);
    }

    @Override
    public ConfirmListener addConfirmListener(ConfirmCallback ackCallback, ConfirmCallback nackCallback) {
        return delegate.addConfirmListener(ackCallback, nackCallback);
    }

    @Override
    public boolean removeConfirmListener(ConfirmListener listener) {
        return delegate.removeConfirmListener(listener);
    }

    @Override
    public void clearConfirmListeners() {
        delegate.clearConfirmListeners();
    }

    @Override
    public Consumer getDefaultConsumer() {
        return delegate.getDefaultConsumer();
    }

    @Override
    public void setDefaultConsumer(Consumer consumer) {
        delegate.setDefaultConsumer(consumer);
    }

    @Override
    public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {
        delegate.basicQos(prefetchSize, prefetchCount, global);
    }

    @Override
    public void basicQos(int prefetchCount, boolean global) throws IOException {
        delegate.basicQos(prefetchCount, global);
    }

    @Override
    public void basicQos(int prefetchCount) throws IOException {
        delegate.basicQos(prefetchCount);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, AMQP.BasicProperties props, byte[] body) throws IOException {
        delegate.basicPublish(exchange, routingKey, props, body);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException {
        delegate.basicPublish(exchange, routingKey, mandatory, props, body);
    }

    @Override
    public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, AMQP.BasicProperties props, byte[] body) throws IOException {
        delegate.basicPublish(exchange, routingKey, mandatory, immediate, props, body);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
        return delegate.exchangeDeclare(exchange, type);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type) throws IOException {
        return delegate.exchangeDeclare(exchange, type);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeDeclare(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public void exchangeDeclareNoWait(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        delegate.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public void exchangeDeclareNoWait(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
        delegate.exchangeDeclareNoWait(exchange, type, durable, autoDelete, internal, arguments);
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclarePassive(String name) throws IOException {
        return delegate.exchangeDeclarePassive(name);
    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
        return delegate.exchangeDelete(exchange, ifUnused);
    }

    @Override
    public void exchangeDeleteNoWait(String exchange, boolean ifUnused) throws IOException {
        delegate.exchangeDeleteNoWait(exchange, ifUnused);
    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String exchange) throws IOException {
        return delegate.exchangeDelete(exchange);
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String destination, String source, String routingKey) throws IOException {
        return delegate.exchangeBind(destination, source, routingKey);
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeBind(destination, source, routingKey, arguments);
    }

    @Override
    public void exchangeBindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        delegate.exchangeBindNoWait(destination, source, routingKey, arguments);
    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException {
        return delegate.exchangeUnbind(destination, source, routingKey);
    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        return delegate.exchangeUnbind(destination, source, routingKey, arguments);
    }

    @Override
    public void exchangeUnbindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
        delegate.exchangeUnbindNoWait(destination, source, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare() throws IOException {
        return delegate.queueDeclare();
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        return delegate.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
    }

    @Override
    public void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        delegate.queueDeclareNoWait(queue, durable, exclusive, autoDelete, arguments);
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
        return delegate.queueDeclarePassive(queue);
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String queue) throws IOException {
        return delegate.queueDelete(queue);
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
        return delegate.queueDelete(queue, ifUnused, ifEmpty);
    }

    @Override
    public void queueDeleteNoWait(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
        delegate.queueDeleteNoWait(queue, ifUnused, ifEmpty);
    }

    @Override
    public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey) throws IOException {
        return delegate.queueBind(queue, exchange, routingKey);
    }

    @Override
    public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        return delegate.queueBind(queue, exchange, routingKey, arguments);
    }

    @Override
    public void queueBindNoWait(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        delegate.queueBindNoWait(queue, exchange, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey) throws IOException {
        return delegate.queueUnbind(queue, exchange, routingKey);
    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
        return delegate.queueUnbind(queue, exchange, routingKey, arguments);
    }

    @Override
    public AMQP.Queue.PurgeOk queuePurge(String queue) throws IOException {
        return delegate.queuePurge(queue);
    }

    @Override
    public GetResponse basicGet(String queue, boolean autoAck) throws IOException {
        return delegate.basicGet(queue, autoAck);
    }

    @Override
    public void basicAck(long deliveryTag, boolean multiple) throws IOException {
        delegate.basicAck(deliveryTag, multiple);
    }

    @Override
    public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {
        delegate.basicNack(deliveryTag, multiple, requeue);
    }

    @Override
    public void basicReject(long deliveryTag, boolean requeue) throws IOException {
        delegate.basicReject(deliveryTag, requeue);
    }

    @Override
    public String basicConsume(String queue, Consumer callback) throws IOException {
        return delegate.basicConsume(queue, callback);
    }

    @Override
    public String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
        return delegate.basicConsume(queue, deliverCallback, cancelCallback);
    }

    @Override
    public String basicConsume(String queue, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, deliverCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, deliverCallback, cancelCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
        return delegate.basicConsume(queue, autoAck, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, deliverCallback, cancelCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, deliverCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, deliverCallback, cancelCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback) throws IOException {
        return delegate.basicConsume(queue, autoAck, arguments, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, arguments, deliverCallback, cancelCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, arguments, deliverCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, arguments, deliverCallback, cancelCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, deliverCallback, cancelCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, deliverCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, deliverCallback, cancelCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, Consumer callback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, callback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback, cancelCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback, shutdownSignalCallback);
    }

    @Override
    public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
        return delegate.basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, deliverCallback, cancelCallback, shutdownSignalCallback);
    }

    @Override
    public void basicCancel(String consumerTag) throws IOException {
        delegate.basicCancel(consumerTag);
    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover() throws IOException {
        return delegate.basicRecover();
    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover(boolean requeue) throws IOException {
        return delegate.basicRecover(requeue);
    }

    @Override
    public AMQP.Tx.SelectOk txSelect() throws IOException {
        return delegate.txSelect();
    }

    @Override
    public AMQP.Tx.CommitOk txCommit() throws IOException {
        return delegate.txCommit();
    }

    @Override
    public AMQP.Tx.RollbackOk txRollback() throws IOException {
        return delegate.txRollback();
    }

    @Override
    public AMQP.Confirm.SelectOk confirmSelect() throws IOException {
        return delegate.confirmSelect();
    }

    @Override
    public long getNextPublishSeqNo() {
        return delegate.getNextPublishSeqNo();
    }

    @Override
    public boolean waitForConfirms() throws InterruptedException {
        return delegate.waitForConfirms();
    }

    @Override
    public boolean waitForConfirms(long timeout) throws InterruptedException, TimeoutException {
        return delegate.waitForConfirms(timeout);
    }

    @Override
    public void waitForConfirmsOrDie() throws IOException, InterruptedException {
        delegate.waitForConfirmsOrDie();
    }

    @Override
    public void waitForConfirmsOrDie(long timeout) throws IOException, InterruptedException, TimeoutException {
        delegate.waitForConfirmsOrDie(timeout);
    }

    @Override
    public void asyncRpc(Method method) throws IOException {
        delegate.asyncRpc(method);
    }

    @Override
    public Command rpc(Method method) throws IOException {
        return delegate.rpc(method);
    }

    @Override
    public long messageCount(String queue) throws IOException {
        return delegate.messageCount(queue);
    }

    @Override
    public long consumerCount(String queue) throws IOException {
        return delegate.consumerCount(queue);
    }

    @Override
    public CompletableFuture<Command> asyncCompletableRpc(Method method) throws IOException {
        return delegate.asyncCompletableRpc(method);
    }

    @Override
    public void addShutdownListener(ShutdownListener listener) {
        delegate.addShutdownListener(listener);
    }

    @Override
    public void removeShutdownListener(ShutdownListener listener) {
        delegate.removeShutdownListener(listener);
    }

    @Override
    public ShutdownSignalException getCloseReason() {
        return delegate.getCloseReason();
    }

    @Override
    public void notifyListeners() {
        delegate.notifyListeners();
    }
}
