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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.opencl._cl_platform_id;
import org.bytedeco.opencl.global.OpenCL;

import static org.bytedeco.opencl.global.OpenCL.clGetPlatformIDs;

import lombok.Getter;

import jcompute.opencl.ClBinding;
import jcompute.opencl.ClPlatform;
import jcompute.opencl.spi.OpenCLBindingProvider;

public final class BindingProviderBd implements OpenCLBindingProvider {

    @Override
    public ClBinding getBinding() {
        return BindingHolder.getBinding();
    }

    private final static class BindingHolder {
        @Getter(lazy = true)
        private final static ClBinding binding = createBinding();
    }

    // -- PLATFORMS

    private static ClBinding createBinding() {
        final var platforms = createPlatforms();
        return () -> platforms;
    }

    /**
     * Lists all available OpenCL implementations.
     */
    private static List<ClPlatform> createPlatforms() {

        var outPlatformCount = new int[1];

        // count available OpenCL platforms
        var ret = clGetPlatformIDs(0, (PointerPointer<?>)null, outPlatformCount);
        if(ret!=OpenCL.CL_SUCCESS) return List.of();

        final int platformCount = outPlatformCount[0];

        try(var pointer = new PointerPointer<_cl_platform_id>(platformCount)) {

            // fetch available OpenCL platforms
            _Util.assertSuccess(
                    clGetPlatformIDs(platformCount, pointer, (int[])null),
                    ()->"failed to call clGetPlatformIDs");

            var platforms = new ArrayList<ClPlatform>(platformCount);
            for (int i = 0; i < platformCount; i++) {
                platforms.add(
                        new ClPlatformBd(i, new _cl_platform_id(pointer.get(i))));
            }

            return Collections.unmodifiableList(platforms);
        }
    }

}
