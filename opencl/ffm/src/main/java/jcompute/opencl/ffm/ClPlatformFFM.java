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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jcompute.core.internal.DeferredLazyConstant;
import jcompute.opencl.ClDevice;
import jcompute.opencl.ClPlatform;
import jcompute.opencl.ffm._CL.AddressConstant;
import jcompute.opencl.ffm.gen.opencl_h;
import jcompute.opencl.ffm.gen.opencl_h_1;

record ClPlatformFFM(
		int index,
		AddressConstant address,
		String platformName,
		String platformVendor,
		String platformVersion,
		String platformExtensions,
		LazyConstant<List<ClDevice>> devicesLazy) implements ClPlatform {

	ClPlatformFFM {
		platformName = platformName!=null
				? platformName
				: getString(address, opencl_h_1.CL_PLATFORM_NAME());
		platformVendor = platformName!=null
				? platformVendor
				: getString(address, opencl_h_1.CL_PLATFORM_VENDOR());
		platformVersion = platformVersion!=null
				? platformVersion
				: getString(address, opencl_h_1.CL_PLATFORM_VERSION());
		platformExtensions = platformExtensions!=null
				? platformExtensions
				: getString(address, opencl_h_1.CL_PLATFORM_EXTENSIONS());
		devicesLazy = Objects.requireNonNull(devicesLazy);
	}

    ClPlatformFFM(final int index, final AddressConstant platformId) {
        var listDevices = new DeferredLazyConstant<>(ClPlatformFFM::listDevices);
    	this(index, platformId, null, null, null, null, listDevices.constant());
    	listDevices.bind(this);
    }

    MemorySegment id() { return address.memorySegment(); }

    @Override public String getPlatformName() { return platformName; }
    @Override public String getPlatformVendor() { return platformVendor; }
    @Override public String getPlatformVersion() { return platformVersion; }
    @Override public String getPlatformExtensions() { return platformExtensions; }

    @Override
    public List<ClDevice> getDevices() {
        return devicesLazy.get();
    }

    // -- HELPER

    private static String getString(final AddressConstant address, final int paramName) {
    	try(var ffm = new FFMHelper()) {
    		return ffm.readString((a, b, c)->opencl_h_1.clGetPlatformInfo(address.memorySegment(), paramName, a, b, c));
    	}
    }

    private static List<ClDevice> listDevices(final ClPlatformFFM platform) {
        Objects.requireNonNull(platform);
    	try(var ffm = new FFMHelper()) {

    		int maxDevices = 32;
    		final var devicesArray = ffm.addressArray(maxDevices);
    		final var numDevices = ffm.uint();

    		int err = opencl_h_1.clGetDeviceIDs(
    				platform.id(),
    				opencl_h.CL_DEVICE_TYPE_ALL(),
    				maxDevices,
    				devicesArray.memorySegment(),
    				numDevices.memorySegment());

    		if(err!=opencl_h_1.CL_SUCCESS())
	        	return List.of();

    		var devices = new ArrayList<ClDevice>(numDevices.intValue());

    		for (int i = 0; i < numDevices.intValue(); i++) {
    			devices.add(new ClDeviceFFM(devicesArray.pointerAt(i), platform, i));
    		}

    		return Collections.unmodifiableList(devices);
    	}
    }

}
