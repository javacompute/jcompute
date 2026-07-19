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

import java.lang.foreign.MemorySegment;
import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;

import jcompute.core.mem.JComputeArray;
import jcompute.core.mem.LongArray;
import jcompute.opencl.ClCommandQueue;
import jcompute.opencl.ClContext;
import jcompute.opencl.ClDevice;
import jcompute.opencl.ClMem;
import jcompute.opencl.ClMem.MemMode;
import jcompute.opencl.ClProgram;
import jcompute.opencl.ffm._CL.AddressArray;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm._CL.StringConstant;
import jcompute.opencl.ffm.gen.opencl_h;

public final class ClContextFFM extends ClContext {

    @Getter @Accessors(fluent = true) private final AddressConstant address;

    ClContextFFM(final AddressConstant address, final List<ClDevice> devices) {
        super(devices);
        this.address = address;
    }

    MemorySegment id() { return address.memorySegment(); }

    @Override
    protected ClCommandQueue createQueueInternal() {
        final ClContextFFM context = this;
        var deviceId = ((ClDeviceFFM)context.getSingleDeviceElseFail()).id();
        var queueId = _CL.checkedApply2((arena, ret_pointer)->{

				// OpenCL.CL_QUEUE_PROPERTIES,
				// | OpenCL.CL_QUEUE_ON_DEVICE
				// | OpenCL.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE,
				// zero terminated list of queue creation properties
				// https://registry.khronos.org/OpenCL/sdk/3.0/docs/man/html/clCreateCommandQueueWithProperties.html
		        //var properties = LongArray.wrap(arena, 0L);
	        	var properties = LongArray.wrap(arena, 0L);
	        	return new AddressConstant(
	    			opencl_h.clCreateCommandQueueWithProperties(
		                context.id(),
		                deviceId,
		                properties.memorySegment(),
		                ret_pointer.memorySegment()));
	    	},
            ()->"failed to create command-queue for context %s".formatted(context));

        return new ClCommandQueueFFM(queueId, context);
    }

    @Override
    protected ClProgram createProgramInternal(final String programSource) {
    	var programId = _CL.checkedApply2((arena, ret_pointer)->{
    			var src = new StringConstant(arena, programSource);
    			final AddressArray sources = AddressArray.wrap(arena, src.memorySegment());
    			var lengths = LongArray.wrap(arena, (long)programSource.length());
    			return new AddressConstant(
    					opencl_h.clCreateProgramWithSource(this.id(), 1, sources.memorySegment(), lengths.memorySegment(), ret_pointer.memorySegment()));
    		},
			()-> "failed to create program for context %s".formatted(this));
    	return new ClProgramFFM(programId, this).build();
    }

    @Override
    protected ClMem createMemoryInternal(final JComputeArray computeArray, final MemMode memMode) {
        var clMemMode = switch (memMode) {
            case MEM_READ_WRITE -> opencl_h.CL_MEM_READ_WRITE();
            case MEM_READ_ONLY -> opencl_h.CL_MEM_READ_ONLY();
            case MEM_WRITE_ONLY -> opencl_h.CL_MEM_WRITE_ONLY();
        };
        return createMemoryInternal(computeArray, clMemMode);
    }

    @Override
    protected int releaseContextIntenral() {
        return opencl_h.clReleaseContext(id());
    }

    // -- HELPER

    /**
     * Returns a new memory object for given context.
     */
    private ClMemFFM createMemoryInternal(final JComputeArray computeArray, final long clMemMode) {
        long size = computeArray.shape().totalSize();
        int sizeOf = computeArray.bytesPerElement();

        var memId = _CL.checkedApply(ret_pointer->
        	new AddressConstant(opencl_h.clCreateBuffer(this.id(), clMemMode, size * sizeOf, MemorySegment.NULL, ret_pointer.memorySegment())),
            ()->"failed to create memory object (size=%d*%d) for context %s".formatted(sizeOf, size, this));
        return new ClMemFFM(memId, this, computeArray);
    }

}
