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

import lombok.Getter;
import lombok.experimental.Accessors;

import jcompute.opencl.ClContext;
import jcompute.opencl.ClKernel;
import jcompute.opencl.ClProgram;
import jcompute.opencl.ffm._CL.AddressArray;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm._CL.StringConstant;
import jcompute.opencl.ffm.gen.opencl_h;

final class ClProgramFFM extends ClProgram {

    @Getter @Accessors(fluent = true) private final AddressConstant address;

    ClProgramFFM(
            final AddressConstant address,
            final ClContext context) {
        super(context);
        this.address = address;
    }

    MemorySegment id() { return address.memorySegment(); }

    /**
     * @implNote yet only supports contexts bound to only a single device
     */
    @Override
    public ClProgram build()  {

    	try(var arena = Arena.ofConfined()) {
    		var deviceIds = getContext().devices().stream()
    	    		.map(ClDeviceFFM.class::cast)
    	    		.map(ClDeviceFFM::id)
    	    		.toArray(MemorySegment[]::new);

    		var devices = AddressArray.wrap(arena, deviceIds);

            // https://www.intel.com/content/www/us/en/docs/opencl-sdk/developer-reference-processor-graphics/2015-1/optimization-options.html
            MemorySegment options = MemorySegment.NULL; // all enabled by default
                    //"-cl-opt-disable";
                    //"-cl-mad-enable";

            /* Build Kernel Program */
            _CL.assertSuccess(
            		opencl_h.clBuildProgram(id(), 1, devices.memorySegment(), options, MemorySegment.NULL, MemorySegment.NULL),
                    ()->"failed to build program %s%nbuild-log: %s".formatted(
                        this,
                        getBuildProgramInfo(opencl_h.CL_PROGRAM_BUILD_LOG())
                        ));
    	}
        return this;
    }

    @Override
    protected int releaseProgramInternal() {
        return opencl_h.clReleaseProgram(id());
    }

    @Override
    protected ClKernel createKernelInternal(final String kernelName) {
        var kernelId = _CL.checkedApply2((arena, ret_pointer)->{
        		var kernelNameArg =  new StringConstant(arena, kernelName);
        		return new AddressConstant(
        				opencl_h.clCreateKernel(this.id(), kernelNameArg.memorySegment(), ret_pointer.memorySegment()));
        	},
            ()->"failed to create kernel '%s' for program %s".formatted(kernelName, this));
        return new ClKernelFFM(kernelId, this, kernelName);
    }

    // -- HELPER

    private String getBuildProgramInfo(final int paramName) {
        var deviceId = ((ClDeviceFFM)getContext().getSingleDeviceElseFail()).id();
        try(var ffm = new FFMHelper()) {
    		return ffm.readString((a, b, c)->opencl_h.clGetProgramBuildInfo(id(), deviceId, paramName, a, b, c));
    	}
    }

}
