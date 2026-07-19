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

import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

import jcompute.core.mem.ByteArray;
import jcompute.core.mem.DoubleArray;
import jcompute.core.mem.JComputeArray;
import jcompute.core.mem.LongArray;
import jcompute.core.mem.ShortArray;
import jcompute.core.shape.Shape;
import jcompute.opencl.ClCommandQueue;
import jcompute.opencl.ClContext;
import jcompute.opencl.ClKernel;
import jcompute.opencl.ClMem;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm.gen.opencl_h;

final class ClCommandQueueFFM extends ClCommandQueue {

    @Getter @Accessors(fluent = true) private final AddressConstant address;

    ClCommandQueueFFM(final AddressConstant address, final ClContext context) {
        super(context);
        this.address = address;
    }

    MemorySegment id() { return address.memorySegment(); }

    @Override
    protected int flushQueue() {
        return opencl_h.clFlush(address.memorySegment());
    }
    @Override
    protected int finishQueue() {
        return opencl_h.clFinish(address.memorySegment());
    }
    @Override
    protected int releaseQueue() {
        return opencl_h.clReleaseCommandQueue(address.memorySegment());
    }
    @Override
    protected int enqueueWriteBuffer(final ClMem memObj, final boolean blocking) {
        return opencl_h.clEnqueueWriteBuffer(address.memorySegment(), ((ClMemFFM)memObj).id(),
                blocking ? opencl_h.CL_TRUE() : opencl_h.CL_FALSE(),
                0,
                memObj.size() * memObj.sizeOf(),
                pointerOf(memObj.computeArray()),
                0, MemorySegment.NULL, MemorySegment.NULL);
    }
    @Override
    protected int enqueueReadBuffer(final ClMem memObj, final boolean blocking) {
        return opencl_h.clEnqueueReadBuffer(address.memorySegment(), ((ClMemFFM)memObj).id(),
                blocking ? opencl_h.CL_TRUE() : opencl_h.CL_FALSE(),
                0,
                memObj.size() * memObj.sizeOf(),
                pointerOf(memObj.computeArray()),
                0, MemorySegment.NULL, MemorySegment.NULL);
    }

    @Override
    public String toString() {
        return "addr: " + id().address();
    }

    @Override
    protected int enqueueNDRangeKernel(
            final ClKernel kernel, final int work_dim,
            final Shape globalSize, final @Nullable Shape localSize) {

    	try(var arena = Arena.ofConfined()) {
	        final MemorySegment global_work_offset = MemorySegment.NULL;
	        final MemorySegment global_work_size = LongArray.wrap(arena, globalSize.sizeX(), globalSize.sizeY(), globalSize.sizeZ())
	        		.memorySegment();
	        final MemorySegment local_work_size = localSize!=null
	                ? LongArray.wrap(arena, localSize.sizeX(), localSize.sizeY(), localSize.sizeZ())
	    	        		.memorySegment()
	                : MemorySegment.NULL;
	        return opencl_h.clEnqueueNDRangeKernel(address.memorySegment(), ((ClKernelFFM)kernel).id(), work_dim,
	        		global_work_offset, global_work_size, local_work_size,
	                0, MemorySegment.NULL, MemorySegment.NULL);
    	}
    }

    // -- HELPER

    private static MemorySegment pointerOf(final JComputeArray jcomputeArray) {
        final MemorySegment pointer = switch (jcomputeArray) {
            case ByteArray array -> array.memorySegment();
            case ShortArray array -> array.memorySegment();
            case LongArray array -> array.memorySegment();
            case DoubleArray array -> array.memorySegment();
            default -> throw new IllegalArgumentException("Unexpected value: " + jcomputeArray.getClass());
        };
        return pointer;
    }

}
