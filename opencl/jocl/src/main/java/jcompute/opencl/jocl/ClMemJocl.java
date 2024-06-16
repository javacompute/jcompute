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
package jcompute.opencl.jocl;

import org.jocl.CL;
import org.jocl.cl_mem;

import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

import jcompute.core.mem.JComputeArray;
import jcompute.opencl.ClContext;
import jcompute.opencl.ClMem;

public final class ClMemJocl extends ClMem {

    @Getter @Accessors(fluent = true) private final cl_mem id;

    protected ClMemJocl(
            final cl_mem id,
            final ClContext context,
            final JComputeArray computeArray) {
        super(context, computeArray);
        this.id = id;
    }

    // -- HELPER

    /**
     * Returns a new memory object for given context.
     */
    static ClMem createMemory(final ClContextJocl context, final JComputeArray computeArray, final long options) {
        long size = computeArray.shape().totalSize();
        int sizeOf = computeArray.bytesPerElement();

        val ret_pointer = new int[1];
        val memId = CL.clCreateBuffer(context.id(), options,
                size * sizeOf, null, ret_pointer);
        val ret = ret_pointer[0];
        _Util.assertSuccess(ret, ()->
                String.format("failed to create memory object (size=%d*%d) for context %s", sizeOf, size, context));

        return new ClMemJocl(memId, context, computeArray);
    }

    @Override
    protected int releaseMemObject() {
        return CL.clReleaseMemObject(id());
    }

}

