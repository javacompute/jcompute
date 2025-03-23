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
package jcompute.opencl.bytedeco;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jcompute.opencl.ClDevice;
import jcompute.opencl.ClDevice.DeviceType;
import jcompute.opencl.ClPlatform;

class ClPlatformTest {

    @Test
    void platform() {
        var clPlatforms = ClPlatform.listPlatforms();
        assertTrue(clPlatforms.size()>0);
        System.err.printf("NumberOfPlatforms: %d%n", clPlatforms.size());
    }

    @Test
    void device() {
        var clPlatforms = ClPlatform.listPlatforms();
        clPlatforms.forEach(platform->{
            System.err.printf("Platform: %s%n", platform.getPlatformVersion());

            assertTrue(platform.getDevices().size()>0);

            platform.getDevices().forEach(device->{
                System.err.printf("\t Device[%d]: %s%n", device.getIndex(), device.getName());
                System.err.printf("\t\t Type: %s%n", device.getType().name());
                System.err.printf("\t\t CUs: %d%n", device.getMaxComputeUnits());
                System.err.printf("\t\t Max Clock: %d%n", device.getMaxClockFrequency());
                System.err.printf("\t\t Max Work Group Size: %d%n", device.getMaxWorkGroupSize());
                System.err.printf("\t\t Max Work Item Sizes: %s%n", LongStream.of(device.getMaxWorkItemSizes())
                        .mapToObj(x->""+x).collect(Collectors.joining(", ")));
            });
        });
    }

    @Test
    void defaultDevice() {
        ClDevice.getDefault().ifPresent(device->{
            assertEquals(DeviceType.GPU, device.getType());
        });
    }

}
