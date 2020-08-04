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
package io.bootique.rabbitmq.junit5.tester;

import io.bootique.BQCoreModule;
import io.bootique.BQCoreModuleExtender;
import io.bootique.di.Binder;

/**
 * @since 2.0
 */
public class ConnectionPropertyBuilder {

    private String propertyPrefix;
    private BQCoreModuleExtender extender;

    protected ConnectionPropertyBuilder(String propertyPrefix, BQCoreModuleExtender extender) {
        this.propertyPrefix = propertyPrefix;
        this.extender = extender;
    }

    public static ConnectionPropertyBuilder create(Binder binder, String connectionName) {
        String propertyPrefix = String.format("bq.rabbitmq.connections.%s.", connectionName);
        return new ConnectionPropertyBuilder(propertyPrefix, BQCoreModule.extend(binder));
    }

    public ConnectionPropertyBuilder property(String suffix, String value) {
        extender.setProperty(propertyPrefix + suffix, value);
        return this;
    }
}
