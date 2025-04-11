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
import java.nio.LongBuffer;
import java.util.function.LongUnaryOperator;

import jcompute.core.shape.Shape;
import jcompute.core.util.primitive.LongUtils.LongExternalizer;

public record LongArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    public static LongArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), ValueLayout.JAVA_LONG);
        var memorySegment = arena.allocate(layout);
        return new LongArray(shape, memorySegment);
    }

    public static LongArray wrap(final Arena arena, final long[] values) {
        var array = LongArray.of(arena, Shape.of(values.length));
        for (int i = 0; i < values.length; i++) {
            array.memorySegment.setAtIndex(ValueLayout.JAVA_LONG, i, values[i]);
        }
        return array;
    }

    @Override
    public ValueLayout valueLayout() {
        return ValueLayout.JAVA_LONG;
    }

    /**
     * Returns the {@code long} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public long get(final long gid) {
        return memorySegment.getAtIndex(ValueLayout.JAVA_LONG, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code long} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code long} value to copy
     * @return this
     */
    public LongArray put(final long gid, final long value) {
        memorySegment.setAtIndex(ValueLayout.JAVA_LONG, gid, value);
        return this;
    }

    public LongArray fill(final LongUnaryOperator filler) {
        shape().forEach(gid->put(gid, filler.applyAsLong(gid)));
        return this;
    }

    // -- IO

    public static LongArray read(final Arena arena, final InputStream in) {
        var shape = Shape.read(in);
        var array = of(arena, shape);
        new LongExternalizer().readSegment(shape.totalSize(), in, array.memorySegment());
        return array;
    }

    public LongArray write(final OutputStream out) {
        shape.write(out);
        new LongExternalizer().writeSegment(memorySegment, out);
        return this;
    }

    @Override
    public int bytesPerElement() {
        return 8;
    }

    public LongBuffer toBuffer() {
        return memorySegment.asByteBuffer().asLongBuffer();
    }

    public long[] toArray() {
        return toBuffer().array();
    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
