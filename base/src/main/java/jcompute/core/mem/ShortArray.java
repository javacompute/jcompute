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

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfShort;
import java.nio.ShortBuffer;

import jcompute.core.shape.Shape;

public record ShortArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    final static OfShort VALUE_LAYOUT = ValueLayout.JAVA_SHORT;

    public static ShortArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), VALUE_LAYOUT);
        var memorySegment = arena.allocate(layout);
        return new ShortArray(shape, memorySegment);
    }

    public static ShortArray wrap(final Arena arena, final short[] values) {
        var array = ShortArray.of(arena, Shape.of(values.length));
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
     * Returns the {@code short} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public short get(final long gid) {
        return memorySegment.getAtIndex(VALUE_LAYOUT, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code short} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code short} value to copy
     * @return this
     */
    public ShortArray put(final long gid, final short value) {
        memorySegment.setAtIndex(VALUE_LAYOUT, gid, value);
        return this;
    }

//    public ShortArray fill(final ShortUnaryOperator filler) {
//        shape().forEach(gid->put(gid, filler.applyAsShort(gid)));
//        return this;
//    }

    // -- IO

//    public static ShortArray read(final Arena arena, final InputStream in) {
//        var shape = Shape.read(in);
//        var array = of(arena, shape);
//        new LongExternalizer().readSegment(shape.totalSize(), in, array.memorySegment());
//        return array;
//    }
//
//    public ShortArray write(final OutputStream out) {
//        shape.write(out);
//        new LongExternalizer().writeSegment(memorySegment, out);
//        return this;
//    }

    @Override
    public int bytesPerElement() {
        return 2;
    }

    public ShortBuffer toBuffer() {
        return memorySegment.asByteBuffer().asShortBuffer();
    }

//    public short[] toArray() {
//        return memorySegment().toArray(ValueLayout.JAVA_SHORT);
//    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
