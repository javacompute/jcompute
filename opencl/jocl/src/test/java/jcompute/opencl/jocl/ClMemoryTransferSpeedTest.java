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

import java.lang.foreign.Arena;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jcompute.core.mem.LongArray;
import jcompute.core.shape.Shape;
import jcompute.core.timing.Timing;
import jcompute.core.timing.Timing.StopWatch;
import jcompute.opencl.ClDevice;

class ClMemoryTransferSpeedTest {

    private int n;

    final String INC_SRC = """
            __kernel void inc(__global long* a) {
                int gid = get_global_id(0);
                a[gid] += 1;
            }
            """;

    @ParameterizedTest
    @ValueSource(ints = {
            500_000,
            8_000_000,
            16_000_000 // 128 MB of memory (in ~25ms => ~5GB/s) one way; PCIe 4.0 has a rated one-way bandwidth of 2 GB/s per lane
    })
    void memTransfer(final int n) {

        this.n = n;

        System.out.printf("--- TEST n=%.1fM%n", 0.000001*n);

        try (Arena arena = Arena.ofConfined()) {
            var mem = LongArray.of(arena, Shape.of(n));
            /* Initialize Data */
            measure("fill-data-on-host", ()->{
                mem.fill(gid->gid);
            });

            ClDevice.getDefault().ifPresent(device->{
                /* Create OpenCL Context */
                try(var cl = device.createContext()) {

                    System.err.printf("running inc on %s ...%n", cl);

                    // Create Command Queue
                    var queue = cl.createQueue();
                    System.err.printf("  + queue created%n");

                    // Create Kernel program from the read in source
                    var program = cl.createProgram(INC_SRC);
                    System.err.printf("  + program created%n");

                    // Create OpenCL Kernel
                    var kernel = program.createKernel("inc");
                    System.err.printf("  + kernel created%n");

                    // Create memory buffer
                    var memObj = cl.createMemoryReadWrite(mem);
                    System.err.printf("  + mem buf created%n");

                    // Set OpenCL kernel argument
                    kernel.setArgs(memObj);
                    System.err.printf("  + kernel args set%n");

                    measure("  enqueue-write", ()->{
                        queue.enqueueWriteBuffer(memObj);
                    });

                    measure("  enqueue-run-kernel", ()->{
                        queue.enqueueNDRangeKernel(kernel, mem.shape());
                    });

                    measure("  enqueue-read", ()->{
                        queue.enqueueReadBuffer(memObj);
                    });
                }

                measure("validate-data-on-host", ()->{
                    validate(mem);
                });

            });

        }
    }

    static void validate(final LongArray mem) {
        mem.shape().forEach(gid->
            assertEquals(gid, mem.get(gid)-1, 1E-6));
    }

    void measure(final String named, final Runnable runnable) {
        final StopWatch watch = Timing.now();
        runnable.run();
        watch.stop();

        double bandwidth = 0.000008*n/watch.getMillis(); // [GB/s]

        if(Double.isInfinite(bandwidth)) {
            System.out.println(String.format("%s took %s", named, watch));
        } else {
            System.out.println(String.format("%s took %s (%.2fGB/s)", named, watch, bandwidth));
        }

    }

}
