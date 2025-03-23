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
package jcompute.combinatorics.setcover;

import java.lang.foreign.Arena;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.SneakyThrows;

import jcompute.core.io.Compressor;
import jcompute.core.mem.ByteArray;
import jcompute.core.mem.LongArray;
import jcompute.core.timing.Timing;
import jcompute.opencl.ClDevice;

class SetCoverTest {

    private ByteArray outputMem;
    private SetCoverParams setCoverParams;

    @Test @Disabled("takes ~410s")
    void cpu() {

        try(Arena arena = Arena.ofShared()) {

            setup(arena);

            var setCover = new SetCoverKernels.Java64Bit(setCoverParams, outputMem);
            setCover.run();

            validate();
        }
    }

    /**
     * <pre>
     * AMD Radeon RX 6950XT: 2800ms per iteration at 225W, 2540MHz (80 CUs)
     *   - 630Ws per iteration
     * AMD Radeon RX 7600: 5800ms per iteration at 144W, 2950MHz (32 CUs)
     *   - 835Ws per iteration
     * </pre>
     */
    @Test
    void gpu() {

        try(Arena arena = Arena.ofConfined()) {

            setup(arena);

            ClDevice.getDefault().ifPresent(clDevice->{

                var setCover = new SetCoverKernels.OpenCL64Bit(clDevice, setCoverParams, outputMem);

                //for (int i = 0; i < 100; i++) {
                    Timing.run("gpu", ()->{
                        setCover.run();
                    });
                //}

                validate();

            });

        }

    }

    // -- HELPER

    @SneakyThrows
    private void setup(final Arena arena) {
        var stopWatch = Timing.now();

        try(var fis = this.getClass().getResourceAsStream("wheel-35-7-6.lzma")) {
            var kSets = LongArray.read(arena, Compressor.lzma().in(fis));
            assertNotNull(kSets);
            this.setCoverParams = new SetCoverParams(arena, 35, 7, 6, kSets);
        }

        this.outputMem = ByteArray.of(arena, setCoverParams.shape());
        stopWatch.stop();

        System.out.printf("setup %s%n", setCoverParams);
        System.out.printf("setup took %s%n", stopWatch);
    }

    private void validate() {
        //assert all ones
        setCoverParams.shape().forEach(gid->{
            assertEquals((byte)1, outputMem.get(gid), ()->"at gid="+gid);
        });
    }

}
