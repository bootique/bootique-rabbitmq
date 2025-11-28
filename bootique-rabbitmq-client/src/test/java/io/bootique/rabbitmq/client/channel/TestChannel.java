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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Command;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnCallback;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

class TestChannel implements Channel {

    boolean open = true;

    @Override
    public void abort() {
    }

    @Override
    public int getChannelNumber() {
        return 0;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public void close(int i, String s) {
        open = false;
    }

    @Override
    public void abort(int i, String s) {
    }

    @Override
    public void addReturnListener(ReturnListener returnListener) {
    }

    @Override
    public ReturnListener addReturnListener(ReturnCallback returnCallback) {
        return null;
    }

    @Override
    public boolean removeReturnListener(ReturnListener returnListener) {
        return false;
    }

    @Override
    public void clearReturnListeners() {

    }

    @Override
    public void addConfirmListener(ConfirmListener confirmListener) {

    }

    @Override
    public ConfirmListener addConfirmListener(ConfirmCallback confirmCallback, ConfirmCallback confirmCallback1) {
        return null;
    }

    @Override
    public boolean removeConfirmListener(ConfirmListener confirmListener) {
        return false;
    }

    @Override
    public void clearConfirmListeners() {

    }

    @Override
    public Consumer getDefaultConsumer() {
        return null;
    }

    @Override
    public void setDefaultConsumer(Consumer consumer) {

    }

    @Override
    public void basicQos(int i, int i1, boolean b) {

    }

    @Override
    public void basicQos(int i, boolean b) {

    }

    @Override
    public void basicQos(int i) {

    }

    @Override
    public void basicPublish(String s, String s1, AMQP.BasicProperties basicProperties, byte[] bytes) {

    }

    @Override
    public void basicPublish(String s, String s1, boolean b, AMQP.BasicProperties basicProperties, byte[] bytes) {

    }

    @Override
    public void basicPublish(String s, String s1, boolean b, boolean b1, AMQP.BasicProperties basicProperties, byte[] bytes) {

    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, String s1) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, BuiltinExchangeType builtinExchangeType) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, String s1, boolean b) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, BuiltinExchangeType builtinExchangeType, boolean b) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, String s1, boolean b, boolean b1, Map<String, Object> map) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, BuiltinExchangeType builtinExchangeType, boolean b, boolean b1, Map<String, Object> map) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, String s1, boolean b, boolean b1, boolean b2, Map<String, Object> map) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclare(String s, BuiltinExchangeType builtinExchangeType, boolean b, boolean b1, boolean b2, Map<String, Object> map) {
        return null;
    }

    @Override
    public void exchangeDeclareNoWait(String s, String s1, boolean b, boolean b1, boolean b2, Map<String, Object> map) {

    }

    @Override
    public void exchangeDeclareNoWait(String s, BuiltinExchangeType builtinExchangeType, boolean b, boolean b1, boolean b2, Map<String, Object> map) {

    }

    @Override
    public AMQP.Exchange.DeclareOk exchangeDeclarePassive(String s) {
        return null;
    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String s, boolean b) {
        return null;
    }

    @Override
    public void exchangeDeleteNoWait(String s, boolean b) {

    }

    @Override
    public AMQP.Exchange.DeleteOk exchangeDelete(String s) {
        return null;
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String s, String s1, String s2) {
        return null;
    }

    @Override
    public AMQP.Exchange.BindOk exchangeBind(String s, String s1, String s2, Map<String, Object> map) {
        return null;
    }

    @Override
    public void exchangeBindNoWait(String s, String s1, String s2, Map<String, Object> map) {

    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String s, String s1, String s2) {
        return null;
    }

    @Override
    public AMQP.Exchange.UnbindOk exchangeUnbind(String s, String s1, String s2, Map<String, Object> map) {
        return null;
    }

    @Override
    public void exchangeUnbindNoWait(String s, String s1, String s2, Map<String, Object> map) {

    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare() {
        return null;
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare(String s, boolean b, boolean b1, boolean b2, Map<String, Object> map) {
        return null;
    }

    @Override
    public void queueDeclareNoWait(String s, boolean b, boolean b1, boolean b2, Map<String, Object> map) {

    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclarePassive(String s) {
        return null;
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String s) {
        return null;
    }

    @Override
    public AMQP.Queue.DeleteOk queueDelete(String s, boolean b, boolean b1) {
        return null;
    }

    @Override
    public void queueDeleteNoWait(String s, boolean b, boolean b1) {

    }

    @Override
    public AMQP.Queue.BindOk queueBind(String s, String s1, String s2) {
        return null;
    }

    @Override
    public AMQP.Queue.BindOk queueBind(String s, String s1, String s2, Map<String, Object> map) {
        return null;
    }

    @Override
    public void queueBindNoWait(String s, String s1, String s2, Map<String, Object> map) {

    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String s, String s1, String s2) {
        return null;
    }

    @Override
    public AMQP.Queue.UnbindOk queueUnbind(String s, String s1, String s2, Map<String, Object> map) {
        return null;
    }

    @Override
    public AMQP.Queue.PurgeOk queuePurge(String s) {
        return null;
    }

    @Override
    public GetResponse basicGet(String s, boolean b) {
        return null;
    }

    @Override
    public void basicAck(long l, boolean b) {

    }

    @Override
    public void basicNack(long l, boolean b, boolean b1) {

    }

    @Override
    public void basicReject(long l, boolean b) {

    }

    @Override
    public String basicConsume(String s, Consumer consumer) {
        return "";
    }

    @Override
    public String basicConsume(String s, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, Consumer consumer) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, Map<String, Object> map, Consumer consumer) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, Map<String, Object> map, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, Map<String, Object> map, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, Map<String, Object> map, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, Consumer consumer) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, boolean b1, boolean b2, Map<String, Object> map, Consumer consumer) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, boolean b1, boolean b2, Map<String, Object> map, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, boolean b1, boolean b2, Map<String, Object> map, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public String basicConsume(String s, boolean b, String s1, boolean b1, boolean b2, Map<String, Object> map, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback consumerShutdownSignalCallback) {
        return "";
    }

    @Override
    public void basicCancel(String s) {

    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover() {
        return null;
    }

    @Override
    public AMQP.Basic.RecoverOk basicRecover(boolean b) {
        return null;
    }

    @Override
    public AMQP.Tx.SelectOk txSelect() {
        return null;
    }

    @Override
    public AMQP.Tx.CommitOk txCommit() {
        return null;
    }

    @Override
    public AMQP.Tx.RollbackOk txRollback() {
        return null;
    }

    @Override
    public AMQP.Confirm.SelectOk confirmSelect() {
        return null;
    }

    @Override
    public long getNextPublishSeqNo() {
        return 0;
    }

    @Override
    public boolean waitForConfirms() {
        return false;
    }

    @Override
    public boolean waitForConfirms(long l) {
        return false;
    }

    @Override
    public void waitForConfirmsOrDie() {

    }

    @Override
    public void waitForConfirmsOrDie(long l) {

    }

    @Override
    public void asyncRpc(Method method) {

    }

    @Override
    public Command rpc(Method method) {
        return null;
    }

    @Override
    public long messageCount(String s) {
        return 0;
    }

    @Override
    public long consumerCount(String s) {
        return 0;
    }

    @Override
    public CompletableFuture<Command> asyncCompletableRpc(Method method) {
        return null;
    }

    @Override
    public void addShutdownListener(ShutdownListener shutdownListener) {

    }

    @Override
    public void removeShutdownListener(ShutdownListener shutdownListener) {

    }

    @Override
    public ShutdownSignalException getCloseReason() {
        return null;
    }

    @Override
    public void notifyListeners() {

    }

    @Override
    public boolean isOpen() {
        return open;
    }
}
