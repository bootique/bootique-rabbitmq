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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 3.0.M1
 */
public class TopologyCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyCache.class);

    // since there's no standard java collection that is both an LRU and concurrent, let's do our own primitive
    // LRU tracking using access timestamp to decide who to evict. Usually an app only has a limited number of
    // topologies, so any inefficiencies in this algorithm should not be terribly impactful

    private final ConcurrentMap<Set<String>, Long> seen;
    private final int capacity;

    public TopologyCache(int capacity) {

        if (capacity < 1) {
            throw new IllegalArgumentException("Cache size should be 1 or higher. Was " + capacity);
        }

        this.seen = new ConcurrentHashMap<>();
        this.capacity = capacity;
    }

    public int size() {
        return seen.size();
    }

    public int capacity() {
        return capacity;
    }

    public void clear() {
        seen.clear();
    }

    public void evict(RmqTopologyActions topology) {
        seen.remove(topology.getKey());
    }

    public boolean save(RmqTopologyActions topology) {

        // Override the existing topology key with the fresh timestamp to track the LRU entries.
        // Keep only the key in the cache to reduce memory. The cache is for tracking seen keys, not for
        // rerunning the topologies
        Long lastTs = seen.put(topology.getKey(), System.currentTimeMillis());

        if (lastTs == null) {
            resizeCache();
            return true;
        }

        return false;
    }

    private void resizeCache() {

        // using the loop just in case, generally cache size would only be exceeded by 1, except in high concurrency
        // situations

        while (seen.size() > capacity) {

            //  Usually an app has a limited number of topologies, and we run resize only when there's no cache hit.
            //  So the O(N) inefficiency of the resize algorithm should not be terribly impactful, but still
            //  complain here...

            LOGGER.warn(
                    "The number of cached topologies exceeded the size of the cache ({}). " +
                            "Will evict least recently used topologies",
                    capacity);

            long minTs = Long.MAX_VALUE;
            Set<String> toEvict = null;

            for (Map.Entry<Set<String>, Long> e : seen.entrySet()) {
                long ts = e.getValue();
                if (ts < minTs) {
                    minTs = ts;
                    toEvict = e.getKey();
                }
            }

            if (toEvict != null) {
                seen.remove(toEvict);
            }
        }
    }
}
