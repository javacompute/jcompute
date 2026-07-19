/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package jcompute.opencl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import jcompute.core.mem.JComputeArray;
import jcompute.opencl.ClMem.MemMode;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ClContext implements AutoCloseable {

    //public abstract cl_context id();
    @Getter @Accessors(fluent = true) private final List<ClDevice> devices;
    private final Stack<ClResource> childResources = new Stack<ClResource>();

    protected abstract ClCommandQueue createQueueInternal();
    protected abstract ClProgram createProgramInternal(String programSource);
    protected abstract ClMem createMemoryInternal(JComputeArray array, MemMode memMode);
    protected abstract int releaseContextIntenral();

    public final ClDevice getSingleDeviceElseFail() {
        if(devices().size()!=1)
			throw new IllegalArgumentException(String.format(
                    "context %s is required to be bound to exaclty one device", this));
        return devices().get(0);
    }

    /**
     * Returns a new command queue for given context.
     * @implNote yet only supports contexts bound to only a single device
     */
    public final ClCommandQueue createQueue() {
        return add(createQueueInternal());
    }

    public final ClProgram createProgram(final String programSource) {
        return add(createProgramInternal(programSource));
    }

    public final ClMem createMemoryReadWrite(final JComputeArray array) {
        return add(createMemoryInternal(array, MemMode.MEM_READ_WRITE));
    }
    public final ClMem createMemoryReadOnly(final JComputeArray array) {
        return add(createMemoryInternal(array, MemMode.MEM_READ_ONLY));
    }
    public final ClMem createMemoryWriteOnly(final JComputeArray array) {
        return add(createMemoryInternal(array, MemMode.MEM_WRITE_ONLY));
    }

    @Override
    public final void close() {
        while(!childResources.isEmpty()) {
            childResources.pop().free();
        }
        _Util.assertSuccess(releaseContextIntenral(), ()->
            String.format("failed to release context for devices %s", devices()));
    }

    @Override
    public String toString() {
        return String.format("ClContext[%s]", devices());
    }

    // -- UTILITY

    @SneakyThrows
    public final ClProgram createProgram(final Class<?> cls, final String resourceName) {
        try(var is = cls.getResourceAsStream(resourceName)){
            var source = _Util.read(is, StandardCharsets.UTF_8);
            return createProgram(source);
        }
    }

    // -- HELPER

    private final <T extends ClResource> T add(final T resource) {
        childResources.push(resource);
        return resource;
    }

}
