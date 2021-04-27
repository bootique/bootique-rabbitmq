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
package io.bootique.rabbitmq.client.publisher;

import java.util.Map;

/**
 * Injectable service that provides access to preconfigured named publishers.
 *
 * @since 2.0.M1
 */
public class RmqPublisherManager {

    private final Map<String, RmqPublisher> publishers;

    public RmqPublisherManager(Map<String, RmqPublisher> publishers) {
        this.publishers = publishers;
    }

    public RmqPublisher publisher(String name) {

        RmqPublisher publisher = publishers.get(name);
        if (publisher == null) {
            throw new IllegalArgumentException("Unmapped publisher: " + name);
        }

        return publisher;
    }
}
