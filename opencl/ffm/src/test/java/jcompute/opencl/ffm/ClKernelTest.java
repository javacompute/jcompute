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
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jcompute.core.mem.DoubleArray;
import jcompute.core.shape.Shape;
import jcompute.opencl.ClDevice;

class ClKernelTest {

    static final int MEM_SIZE = 128;
    final String VEC_ADD_SRC = """
            __kernel void vecAdd(__global double* a) {
                int gid = get_global_id(0);
                a[gid] += a[gid];
            }
            """;

    @Test
    void vecAdd() {

        var errors = new ArrayList<Throwable>();

        System.err.println("--- vecAdd ---");
        try {
            ClDevice.streamAll()
                .filter(dev->dev.getType().isGPU())
                .forEach(this::vecAdd);
        } catch (Throwable e) {
            System.err.println(" - ERR");
            errors.add(e);
            e.printStackTrace();
        } finally {
            System.err.println("------");
        }

        assertTrue(errors.isEmpty());
    }

    void vecAdd(final ClDevice device) {

        System.err.printf("vecAdd(%s)", device);

        try(var arena = Arena.ofConfined()) {

            /* Initialize Data */
            var mem = DoubleArray.of(arena, Shape.of(MEM_SIZE));
            mem.shape().forEach(gid->mem.put(gid, gid));

            /* Create OpenCL Context */
            try(var context = device.createContext()) {

                /* Create Command Queue */
                var queue = context.createQueue();

                /* Create Kernel program from the read in source */
                var program = context.createProgram(VEC_ADD_SRC);

                /* Create OpenCL Kernel */
                var kernel = program.createKernel("vecAdd");

                /* Create memory buffer*/
                var memObj = context.createMemoryReadWrite(mem);

                /* Set OpenCL kernel argument */
                kernel.setArgs(memObj);

                queue.enqueueWriteBuffer(memObj);

                // run kernel 2 times
                for (int j = 0; j < 2; j++) {
                    queue.enqueueNDRangeKernel(kernel, mem.shape());
                }

                queue.enqueueReadBuffer(memObj);
            }

            validate(mem);

            System.err.println(" - OK");
        }

    }

    static void validate(final DoubleArray mem) {
        mem.shape().forEach(gid->
            assertEquals(4.*gid, mem.get(gid), 1E-6));
    }

}
