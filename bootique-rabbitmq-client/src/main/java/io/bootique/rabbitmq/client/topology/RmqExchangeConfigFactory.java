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

package io.bootique.rabbitmq.client.topology;

import com.rabbitmq.client.BuiltinExchangeType;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import java.util.Collections;
import java.util.Map;

@BQConfig
public class RmqExchangeConfigFactory {

    private BuiltinExchangeType type;
    private boolean durable;
    private boolean autoDelete;
    private boolean internal;
    private Map<String, Object> arguments;

    public RmqExchangeConfig createConfig() {

        return new RmqExchangeConfig(
                type != null ? type : BuiltinExchangeType.TOPIC,
                durable,
                autoDelete,
                internal,
                this.arguments != null ? this.arguments : Collections.emptyMap());
    }

    @BQConfigProperty
    public void setType(BuiltinExchangeType type) {
        this.type = type;
    }

    @BQConfigProperty
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @BQConfigProperty
    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    @BQConfigProperty
    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @BQConfigProperty
    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}
