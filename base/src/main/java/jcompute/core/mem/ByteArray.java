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
import java.nio.ByteBuffer;

import jcompute.core.shape.Shape;
import jcompute.core.util.function.LongToByteFunction;
import jcompute.core.util.primitive.ByteUtils;

public record ByteArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    public static ByteArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), ValueLayout.JAVA_BYTE);
        var memorySegment = arena.allocate(layout);
        return new ByteArray(shape, memorySegment);
    }

    public static ByteArray wrap(final Arena arena, final byte[] values) {
        var array = ByteArray.of(arena, Shape.of(values.length));
        for (int i = 0; i < values.length; i++) {
            array.memorySegment.setAtIndex(ValueLayout.JAVA_BYTE, i, values[i]);
        }
        return array;
    }

    @Override
    public ValueLayout valueLayout() {
        return ValueLayout.JAVA_BYTE;
    }

    /**
     * Returns the {@code byte} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public byte get(final long gid) {
        return memorySegment.getAtIndex(ValueLayout.JAVA_BYTE, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code byte} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code byte} value to copy
     */
    public ByteArray put(final long gid, final byte value) {
        memorySegment.setAtIndex(ValueLayout.JAVA_BYTE, gid, value);
        return this;
    }

    public ByteArray fill(final LongToByteFunction filler) {
        shape().forEach(gid->put(gid, filler.applyAsByte(gid)));
        return this;
    }

    // -- IO

    public static ByteArray read(final Arena arena, final InputStream in) {
        var shape = Shape.read(in);
        var array = of(arena, shape);
        ByteUtils.readIntoBuffer(shape.totalSize(), in, array.memorySegment().asByteBuffer());
        return array;
    }

    public ByteArray write(final OutputStream out) {
        shape.write(out);
        ByteUtils.writeToBuffer(memorySegment.asByteBuffer(), out);
        return this;
    }

    @Override
    public int bytesPerElement() {
        return 1;
    }

    public ByteBuffer toBuffer() {
        return memorySegment.asByteBuffer();
    }

    public byte[] toArray() {
        return toBuffer().array();
    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
