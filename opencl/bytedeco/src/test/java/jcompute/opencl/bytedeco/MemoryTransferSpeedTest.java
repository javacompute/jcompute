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

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.util.stream.IntStream;

import org.bytedeco.javacpp.LongPointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jcompute.core.mem.LongArray;
import jcompute.core.shape.Shape;
import jcompute.core.util.timing.Timing;
import jcompute.opencl.bytedeco.util.PointerUtils;

class MemoryTransferSpeedTest {

    final static int N = 6724520;

    @Test
    void randomAccess() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            var mem = LongArray.of(arena, Shape.of(N));
            Timing.run("randomAccess", ()->{
                mem.fill(gid->gid);
                mem.shape().forEach(gid->{
                    assertEquals(gid, mem.get(gid));
                });
            });
        }
    }

    @Test
    void javaArray() throws IOException {
        var mem = new long[N];
        Timing.run("array", ()->{
            IntStream.range(0, N)
                .forEach(gid->mem[gid]=gid);
            IntStream.range(0, N)
                .forEach(gid->assertEquals(gid, mem[gid]));
        });
    }

    @Test
    void pointer() throws IOException {
        try(var mem = new LongPointer(N)) {
            Timing.run("pointer", ()->{
                IntStream.range(0, N)
                    .forEach(gid->mem.put(gid, gid));
                IntStream.range(0, N)
                    .forEach(gid->assertEquals(gid, mem.get(gid)));
            });
        }
    }

    LongPointer pointer;

    @Test
    void pointer2() throws IOException {

        try (Arena arena = Arena.ofConfined()) {
            var in = LongArray.of(arena, Shape.of(N));
            var out = LongArray.of(arena, Shape.of(N));
            in.fill(gid->gid);

            //try(var cl = ClDevice.getDefault().createContext()) {

                Timing.run("tx-in", ()->{
                    pointer = PointerUtils.pointer(in);
                });
                Timing.run("tx-out", ()->{
                    PointerUtils.copy(pointer, out);
                });

            //}

            in.shape().forEach(gid->{
                assertEquals(gid, out.get(gid), ()->"at gid: " + gid);
            });
        }
    }

    @Test
    void pointer3() throws IOException {

        try (Arena arena = Arena.ofConfined()) {
            var mem = LongArray.of(arena, Shape.of(N));
            mem.fill(gid->0);

            //try(var cl = ClDevice.getDefault().createContext()) {

                pointer = PointerUtils.pointer(mem);
                Timing.run("fill", ()->{
                    mem.shape().forEach(gid->pointer.put(gid, gid));
                });

                mem.shape().forEach(gid->{
                    assertEquals(gid, mem.get(gid), ()->"at gid: " + gid);
                });
            //}

        }
    }

    @Test
    void arena() throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            var layout = MemoryLayout.sequenceLayout(N, ValueLayout.JAVA_LONG);
            var mem = arena.allocate(layout);
            Timing.run("arena", ()->{
                IntStream.range(0, N)
                    .forEach(gid->mem.setAtIndex(ValueLayout.JAVA_LONG, gid, gid));
                IntStream.range(0, N)
                    .forEach(gid->assertEquals(gid, mem.getAtIndex(ValueLayout.JAVA_LONG, gid)));
            });
        }
    }

}
