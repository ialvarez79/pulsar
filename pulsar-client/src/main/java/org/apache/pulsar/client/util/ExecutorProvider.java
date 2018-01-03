/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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
package org.apache.pulsar.client.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

public class ExecutorProvider {
    private final int numThreads;
    private final List<ExecutorService> executors;
    private final AtomicInteger currentThread = new AtomicInteger(0);
    private final EventLoopGroup eventLoopGroup;

    public ExecutorProvider(EventLoopGroup eventLoopGroup, int numThreads, String threadNamePrefix) {
        checkArgument(numThreads >= 0);
        this.eventLoopGroup = eventLoopGroup;
        this.numThreads = numThreads;
        this.executors = Lists.newArrayListWithCapacity(numThreads);
        if (numThreads == 0) {
            checkNotNull(eventLoopGroup);
        } else {
            checkNotNull(threadNamePrefix);
            for (int i = 0; i < numThreads; i++) {
                executors.add(Executors.newSingleThreadExecutor(new DefaultThreadFactory(threadNamePrefix)));
            }
        }
    }

    public ExecutorService getExecutor() {
        if (numThreads == 0) {
            return eventLoopGroup.next();
        }
        return executors.get((currentThread.getAndIncrement() & Integer.MAX_VALUE) % numThreads);
    }

    public void shutdownNow() {
        executors.forEach(ExecutorService::shutdownNow);
    }
}
