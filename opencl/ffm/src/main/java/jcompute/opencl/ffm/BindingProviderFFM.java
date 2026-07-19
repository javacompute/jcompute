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

import java.io.File;
import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jcompute.opencl.ClBinding;
import jcompute.opencl.ClPlatform;
import jcompute.opencl.ffm.gen.opencl_h_1;
import jcompute.opencl.spi.OpenCLBindingProvider;

public class BindingProviderFFM implements OpenCLBindingProvider {

	final LazyConstant<ClBinding> clBindingLazy =
			LazyConstant.of(() -> (BindingProviderFFM::createPlatforms));

    @Override
    public ClBinding getBinding() {
        return clBindingLazy.get();
    }

    // -- PLATFORMS

    /**
     * Lists all available OpenCL implementations.
     */
    private static List<ClPlatform> createPlatforms() {

    	new LibraryLocator().locateOpenCL()
			.map(File::getAbsolutePath)
			.ifPresent(System::load);

    	try(var ffm = new FFMHelper(Arena.ofConfined())) {
	        final int maxPlatforms = 16;

	        final var numPlatforms = ffm.uint();
	        final var platformsArray = ffm.addressArray(maxPlatforms);

	        // count available OpenCL platforms
	        int err = opencl_h_1.clGetPlatformIDs(
	                maxPlatforms,
	                platformsArray.memorySegment(),
	                numPlatforms.memorySegment());

	        if(err!=opencl_h_1.CL_SUCCESS())
	        	return List.of();

	        final int platformCount = numPlatforms.intValue();
	        if(platformCount<1)
	        	return List.of();

	        var platforms = new ArrayList<ClPlatform>(platformCount);

	        for (int i = 0; i < platformCount; i++) {
	            platforms.add(
                    new ClPlatformFFM(i, platformsArray.pointerAt(i)));
	        }

	        return Collections.unmodifiableList(platforms);
    	}

    }


}
