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
import java.nio.DoubleBuffer;

import jcompute.core.shape.Shape;

public record DoubleArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    public static DoubleArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), ValueLayout.JAVA_DOUBLE);
        var memorySegment = arena.allocate(layout);
        return new DoubleArray(shape, memorySegment);
    }

    public static DoubleArray wrap(final Arena arena, final double[] values) {
        var array = DoubleArray.of(arena, Shape.of(values.length));
        for (int i = 0; i < values.length; i++) {
            array.memorySegment.setAtIndex(ValueLayout.JAVA_DOUBLE, i, values[i]);
        }
        return array;
    }

    @Override
    public ValueLayout valueLayout() {
        return ValueLayout.JAVA_DOUBLE;
    }

    /**
     * Returns the {@code double} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public double get(final long gid) {
        return memorySegment.getAtIndex(ValueLayout.JAVA_DOUBLE, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code double} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code double} value to copy
     * @return this
     */
    public DoubleArray put(final long gid, final double value) {
        memorySegment.setAtIndex(ValueLayout.JAVA_DOUBLE, gid, value);
        return this;
    }

    @Override
    public int bytesPerElement() {
        return 8;
    }

    public DoubleBuffer toBuffer() {
        return memorySegment.asByteBuffer().asDoubleBuffer();
    }

    public double[] toArray() {
        return toBuffer().array();
    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
