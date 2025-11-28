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

import com.rabbitmq.client.BlockedCallback;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.UnblockedCallback;

import java.net.InetAddress;
import java.util.Map;

class TestConnection implements Connection {

    boolean open = true;

    @Override
    public void abort() {
    }

    @Override
    public InetAddress getAddress() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public int getChannelMax() {
        return 0;
    }

    @Override
    public int getFrameMax() {
        return 0;
    }

    @Override
    public int getHeartbeat() {
        return 0;
    }

    @Override
    public Map<String, Object> getClientProperties() {
        return Map.of();
    }

    @Override
    public String getClientProvidedName() {
        return "";
    }

    @Override
    public Map<String, Object> getServerProperties() {
        return Map.of();
    }

    @Override
    public Channel createChannel() {
        return null;
    }

    @Override
    public Channel createChannel(int i) {
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
    public void close(int i) {
    }

    @Override
    public void close(int i, String s, int i1) {
    }

    @Override
    public void abort(int i, String s) {

    }

    @Override
    public void abort(int i) {

    }

    @Override
    public void abort(int i, String s, int i1) {

    }

    @Override
    public void addBlockedListener(BlockedListener blockedListener) {

    }

    @Override
    public BlockedListener addBlockedListener(BlockedCallback blockedCallback, UnblockedCallback unblockedCallback) {
        return null;
    }

    @Override
    public boolean removeBlockedListener(BlockedListener blockedListener) {
        return false;
    }

    @Override
    public void clearBlockedListeners() {

    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String s) {
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
