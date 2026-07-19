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

import jcompute.opencl.ClContext;
import jcompute.opencl.ClDevice;
import jcompute.opencl.ClPlatform;
import jcompute.opencl.ffm._CL.AddressArray;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm.gen.opencl_h;
import jcompute.opencl.ffm.gen.opencl_h_1;

public final class ClDeviceFFM extends ClDevice {

    @Getter @Accessors(fluent = true) private final AddressConstant address;

    ClDeviceFFM(
            final AddressConstant address,
            final ClPlatform platform,
            final int index) {
        super(platform, index);
        this.address = address;
    }

    MemorySegment id() { return address.memorySegment(); }

    /**
     * Returns a context bound to a single device.
     * <p>
     * @apiNote OpenCL supports binding to multiple devices as well
     */
    @Override
    public ClContext createContext() {
		var contextId = _CL.checkedApply2((arena, errcode_ret)-> {
				final int num_devices = 1;
				final AddressArray devices = AddressArray.wrap(arena, address());
	    		return new AddressConstant(
	    				opencl_h.clCreateContext(MemorySegment.NULL, num_devices, devices.memorySegment(), MemorySegment.NULL, MemorySegment.NULL, errcode_ret.memorySegment()));
    		},
            ()->"failed to create context for device %s".formatted(this.getName()));
        return new ClContextFFM(contextId, List.of(this));
    }

    @Override
    public DeviceType getType() {
        return fromClDeviceType(getInt(id(), opencl_h.CL_DEVICE_TYPE()));
    }

    @Override
    public String getName() {
        return getString(id(), opencl_h.CL_DEVICE_NAME());
    }

    @Override
    public int getMaxComputeUnits() {
        return getInt(id(), opencl_h.CL_DEVICE_MAX_COMPUTE_UNITS());
    }

    @Override
    public long[] getMaxWorkItemSizes() {
        return getLongs(id(), opencl_h.CL_DEVICE_MAX_WORK_ITEM_SIZES(), 3);
    }

    @Override
    public long getMaxWorkGroupSize() {
        return getLong(id(), opencl_h.CL_DEVICE_MAX_WORK_GROUP_SIZE());
    }

    @Override
    public long getMaxClockFrequency() {
        return getInt(id(), opencl_h.CL_DEVICE_MAX_CLOCK_FREQUENCY());
    }

    // -- HELPER

    private static String getString(final MemorySegment deviceId, final int paramName) {
    	try(var ffm = new FFMHelper()) {
    		return ffm.readString((a, b, c)->opencl_h_1.clGetDeviceInfo(deviceId, paramName, a, b, c)).trim();
    	}
    }

    private static long getLong(final MemorySegment deviceId, final int paramName) {
        return getLongs(deviceId, paramName, 1)[0];
    }

    private static long[] getLongs(final MemorySegment deviceId, final int paramName, final int numValues) {
    	try(var ffm = new FFMHelper()) {
    		return ffm.readLongs((a, b, c)->opencl_h_1.clGetDeviceInfo(deviceId, paramName, a, b, c));
    	}
    }

    private static int getInt(final MemorySegment deviceId, final int paramName) {
        return getInts(deviceId, paramName, 1)[0];
    }

    private static int[] getInts(final MemorySegment deviceId, final int paramName, final int numValues) {
        try(var ffm = new FFMHelper()) {
    		return ffm.readInts((a, b, c)->opencl_h_1.clGetDeviceInfo(deviceId, paramName, a, b, c));
    	}
    }

    private static DeviceType fromClDeviceType(final int cl_device_type) {
        return switch (cl_device_type) {
			case opencl_h.CL_DEVICE_TYPE_CPU -> DeviceType.CPU;
			case opencl_h.CL_DEVICE_TYPE_GPU -> DeviceType.GPU;
			case opencl_h.CL_DEVICE_TYPE_ACCELERATOR -> DeviceType.ACCELERATOR;
			default -> DeviceType.OTHER;
		};
    }

}
