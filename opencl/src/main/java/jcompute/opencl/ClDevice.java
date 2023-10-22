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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.SizeTPointer;
import org.bytedeco.opencl._cl_device_id;
import org.bytedeco.opencl.global.OpenCL;

import static org.bytedeco.opencl.global.OpenCL.CL_DEVICE_TYPE_ALL;
import static org.bytedeco.opencl.global.OpenCL.clGetDeviceIDs;
import static org.bytedeco.opencl.global.OpenCL.clGetDeviceInfo;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

public class ClDevice {

    public enum DeviceType {
        CPU,
        GPU,
        ACCELERATOR,
        OTHER;

        public boolean isCPU() { return this == DeviceType.CPU; }
        public boolean isGPU() { return this == DeviceType.GPU; }
        public boolean isAccelerator() { return this == DeviceType.ACCELERATOR; }

        static DeviceType fromClDeviceType(final int cl_device_type) {
            switch (cl_device_type) {
            case OpenCL.CL_DEVICE_TYPE_CPU: return CPU;
            case OpenCL.CL_DEVICE_TYPE_GPU: return GPU;
            case OpenCL.CL_DEVICE_TYPE_ACCELERATOR: return ACCELERATOR;
            default:
                return OTHER;
            }
        }
    }

    public static Stream<ClDevice> streamAll() {
        return ClPlatform.listPlatforms().stream()
                .flatMap(platform->platform.getDevices().stream());
    }

    public static ClDevice getDefault() {
        return getBest(_Util.getDefaultClPreferredDeviceComparator());
    }

    public static ClDevice getBest(final Comparator<ClDevice> deviceComparator) {
        val best = new ClDevice[] {null};
        return streamAll()
                .reduce(best[0], (a, b)->deviceComparator.compare(a, b)>=0 ? a : b);
    }

    @Getter private final ClPlatform platform;
    @Getter private final int index;
    @Getter @Accessors(fluent = true) private final _cl_device_id id;

    private ClDevice(
            final ClPlatform platform,
            final int index,
            final _cl_device_id deviceHandle) {
        this.platform = platform;
        this.index = index;
        this.id = deviceHandle;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getType().name());
    }

    public DeviceType getType() {
        return DeviceType.fromClDeviceType(getInt(id, OpenCL.CL_DEVICE_TYPE));
    }

    public String getName() {
        return getString(id, OpenCL.CL_DEVICE_NAME);
    }

    public int getMaxComputeUnits() {
        return getInt(id, OpenCL.CL_DEVICE_MAX_COMPUTE_UNITS);
    }

    public long[] getMaxWorkItemSizes() {
        return getLongs(id, OpenCL.CL_DEVICE_MAX_WORK_ITEM_SIZES, 3);
    }

    public long getMaxWorkGroupSize() {
        return getLong(id, OpenCL.CL_DEVICE_MAX_WORK_GROUP_SIZE);
    }

    public long getMaxClockFrequency() {
        return getInt(id, OpenCL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
    }

    public ClContext createContext() {
        return ClContext.createContext(this);
    }

    // -- HELPER

    static List<ClDevice> listDevices(final ClPlatform platform) {
        val platformId = platform.id();
        // Obtain the number of devices for the platform
        final int[] numDevicesRef = new int[1];
        clGetDeviceIDs(platformId, CL_DEVICE_TYPE_ALL, 0, (PointerPointer<?>)null, numDevicesRef);
        final int deviceCount = numDevicesRef[0];

        val deviceBuffer = new PointerPointer<_cl_device_id>(deviceCount);
        clGetDeviceIDs(platformId, CL_DEVICE_TYPE_ALL, deviceCount, deviceBuffer, (int[])null);

        val devices = new ArrayList<ClDevice>(deviceCount);
        for (int i = 0; i < deviceCount; i++) {
            devices.add(
                    new ClDevice(null, i, new _cl_device_id(deviceBuffer.get(i))));
        }

        Pointer.free(deviceBuffer);

        return Collections.unmodifiableList(devices);
    }

    private static String getString(final _cl_device_id deviceId, final int paramName) {
        val sizePointer = new SizeTPointer(1);
        clGetDeviceInfo(deviceId, paramName, 0, null, sizePointer);
        final int size = (int)sizePointer.get();
        val buffer = new BytePointer(size);
        clGetDeviceInfo(deviceId, paramName, size, buffer, null);
        val result = new byte[size];
        buffer.get(result);
        return new String(result).trim();
    }

    private static long getLong(final _cl_device_id deviceId, final int paramName) {
        return getLongs(deviceId, paramName, 1)[0];
    }

    private static long[] getLongs(final _cl_device_id deviceId, final int paramName, final int numValues) {
        val sizePointer = new SizeTPointer(1);
        clGetDeviceInfo(deviceId, paramName, 0, null, sizePointer);
        final int size = (int)sizePointer.get();
        val buffer = new LongPointer(size);
        clGetDeviceInfo(deviceId, paramName, size, buffer, null);
        val result = new long[numValues];
        buffer.get(result, 0, numValues);
        return result;
    }

    private static int getInt(final _cl_device_id deviceId, final int paramName) {
        return getInts(deviceId, paramName, 1)[0];
    }

    private static int[] getInts(final _cl_device_id deviceId, final int paramName, final int numValues) {
        val sizePointer = new SizeTPointer(1);
        clGetDeviceInfo(deviceId, paramName, 0, null, sizePointer);
        final int size = (int)sizePointer.get();
        val buffer = new IntPointer(size);
        clGetDeviceInfo(deviceId, paramName, size, buffer, null);
        val result = new int[numValues];
        buffer.get(result, 0, numValues);
        return result;
    }

}
