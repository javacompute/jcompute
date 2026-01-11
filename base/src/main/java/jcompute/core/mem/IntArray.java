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
package jcompute.core.mem;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.IntBuffer;
import java.util.function.LongToIntFunction;

import jcompute.core.io.IntMarshaller;
import jcompute.core.shape.Shape;

public record IntArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    private final static ValueLayout.OfInt VALUE_LAYOUT = ValueLayout.JAVA_INT;

    public static IntArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), VALUE_LAYOUT);
        var memorySegment = arena.allocate(layout);
        return new IntArray(shape, memorySegment);
    }

    public static IntArray wrap(final Arena arena, final int[] values) {
        var array = IntArray.of(arena, Shape.of(values.length));
        for (int i = 0; i < values.length; i++) {
            array.memorySegment.setAtIndex(VALUE_LAYOUT, i, values[i]);
        }
        return array;
    }

    @Override
    public ValueLayout valueLayout() {
        return VALUE_LAYOUT;
    }

    /**
     * Returns the {@code int} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public long get(final long gid) {
        return memorySegment.getAtIndex(VALUE_LAYOUT, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code int} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code long} value to copy
     * @return this
     */
    public IntArray put(final long gid, final int value) {
        memorySegment.setAtIndex(VALUE_LAYOUT, gid, value);
        return this;
    }

    public IntArray fill(final LongToIntFunction filler) {
        shape().forEach(gid->put(gid, filler.applyAsInt(gid)));
        return this;
    }

    // -- IO

    public static IntArray read(final Arena arena, final InputStream in) {
        var shape = Shape.read(in);
        var array = of(arena, shape);
        new IntMarshaller().readSegment(shape.totalSize(), in, array.memorySegment());
        return array;
    }

    public IntArray write(final OutputStream out) {
        shape.write(out);
        new IntMarshaller((int)Math.min(64L, shape.totalSize())).writeSegment(memorySegment, out);
        return this;
    }

    @Override
    public int bytesPerElement() {
        return 4;
    }

    public IntBuffer toBuffer() {
        return memorySegment.asByteBuffer().asIntBuffer();
    }

    public int[] toArray() {
        return toBuffer().array();
    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
