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

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ClDevice {

    public enum DeviceType {
        CPU,
        GPU,
        ACCELERATOR,
        OTHER;
        public boolean isCPU() { return this == DeviceType.CPU; }
        public boolean isGPU() { return this == DeviceType.GPU; }
        public boolean isAccelerator() { return this == DeviceType.ACCELERATOR; }
    }

    public static Stream<ClDevice> streamAll() {
        return ClPlatform.listPlatforms().stream()
                .flatMap(platform->platform.getDevices().stream());
    }

    public static Optional<ClDevice> getDefault() {
        return getBest(_Util.getDefaultClPreferredDeviceComparator());
    }

    public static ClDevice getDefaultOrElseFail() {
        return getDefault().orElseThrow(()->new NoSuchElementException("could not find an ClDevice"));
    }

    public static Optional<ClDevice> getBest(final Comparator<ClDevice> deviceComparator) {
        return streamAll()
                .reduce((a, b)->deviceComparator.compare(a, b)>=0 ? a : b);
    }

    @Getter private final ClPlatform platform;
    @Getter private final int index;

    public abstract DeviceType getType();
    public abstract String getName();
    public abstract int getMaxComputeUnits();
    public abstract long[] getMaxWorkItemSizes();
    public abstract long getMaxWorkGroupSize();
    public abstract long getMaxClockFrequency();
    public abstract ClContext createContext();

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getType().name());
    }

}
