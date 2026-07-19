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
package jcompute.opencl.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

import lombok.Getter;
import lombok.experimental.Accessors;

import jcompute.opencl.ClKernel;
import jcompute.opencl.ClMem;
import jcompute.opencl.ClProgram;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm.gen.opencl_h;

final class ClKernelFFM extends ClKernel {

    @Getter @Accessors(fluent = true) private final AddressConstant address;

    ClKernelFFM(final AddressConstant address, final ClProgram program, final String name) {
        super(program, name);
        this.address = address;
    }

    MemorySegment id() { return address.memorySegment(); }

    @Override
    protected int releaseKernel() {
        return opencl_h.clReleaseKernel(id());
    }

    /**
     * Add OpenCL kernel argument.
     */
    @Override
    public ClKernel setArg(final int argIndex, final ClMem memObj) {
        try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(ADDRESS);
        	ptr.set(ADDRESS, 0, ((ClMemFFM)memObj).id());
            return setArg(this, argIndex, ADDRESS.byteSize(), ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final byte value) {
        try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_BYTE);
        	ptr.set(JAVA_BYTE, 0, value);
            return setArg(this, argIndex, 1L, ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final short value) {
        try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_SHORT);
        	ptr.set(JAVA_SHORT, 0, value);
            return setArg(this, argIndex, 2L, ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final int value) {
    	try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_INT);
        	ptr.set(JAVA_INT, 0, value);
            return setArg(this, argIndex, 4L, ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final long value) {
    	try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_LONG);
        	ptr.set(JAVA_LONG, 0, value);
            return setArg(this, argIndex, 8L, ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final float value) {
    	try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_FLOAT);
        	ptr.set(JAVA_FLOAT, 0, value);
            return setArg(this, argIndex, 4L, ptr);
        }
    }

    @Override
    public ClKernel setArg(final int argIndex, final double value) {
    	try(var arena = Arena.ofConfined()) {
        	var ptr = arena.allocate(JAVA_DOUBLE);
        	ptr.set(JAVA_DOUBLE, 0, value);
            return setArg(this, argIndex, 8L, ptr);
        }
    }

    /* Set OpenCL kernel argument */
    private ClKernel setArg(final ClKernelFFM kernel, final int argIndex, final long sizeOf, final MemorySegment arg_value) {
        int ret = opencl_h.clSetKernelArg(kernel.id(), argIndex, sizeOf, arg_value);
        _CL.assertSuccess(ret, ()->
            String.format("failed to set kernel argument for kernel %s", kernel.name()));
        return kernel;
    }

}
