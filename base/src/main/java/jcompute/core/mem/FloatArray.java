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
import java.nio.FloatBuffer;

import jcompute.core.shape.Shape;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;

public record FloatArray(
        Shape shape,
        MemorySegment memorySegment) implements JComputeArray {

    public final static VectorSpecies<Float> SPECIES = VectorShape.forBitSize(VECTOR_BIT_SIZE)
        .withLanes(float.class); //  e.g. FloatVector.SPECIES_256;

    public static FloatArray of(final Arena arena, final Shape shape) {
        var layout = MemoryLayout.sequenceLayout(shape.totalSize(), ValueLayout.JAVA_FLOAT);
        var memorySegment = arena.allocate(layout);
        return new FloatArray(shape, memorySegment);
    }

    public static FloatArray wrap(final Arena arena, final float[] values) {
        var array = FloatArray.of(arena, Shape.of(values.length));
        for (int i = 0; i < values.length; i++) {
            array.memorySegment.setAtIndex(ValueLayout.JAVA_FLOAT, i, values[i]);
        }
        return array;
    }

    @Override
    public ValueLayout valueLayout() {
        return ValueLayout.JAVA_FLOAT;
    }

    /**
     * Returns the {@code double} value from the underlying buffer at global index {@code gid}.
     * @param gid the global index into the underlying buffer
     */
    public float get(final long gid) {
        return memorySegment.getAtIndex(ValueLayout.JAVA_FLOAT, gid);
    }

    /**
     * Sets the i-th element of the underlying buffer to given {@code double} value.
     * @param gid the global index into the underlying buffer
     * @param value the {@code double} value to copy
     * @return this
     */
    public FloatArray put(final long gid, final float value) {
        memorySegment.setAtIndex(ValueLayout.JAVA_FLOAT, gid, value);
        return this;
    }

    @Override
    public int bytesPerElement() {
        return 4;
    }

    public FloatBuffer toBuffer() {
        return memorySegment.asByteBuffer().asFloatBuffer();
    }

    public float[] toArray() {
        return toBuffer().array();
    }

    // -- VECTOR API

    public FloatVector floatVector(final VectorSpecies<Float> species, final int offset) {
        return FloatVector.fromMemorySegment(species, memorySegment, offset * Float.BYTES, ValueLayout.JAVA_FLOAT.order());
    }

    // -- DOT PRODUCT

    public float dotProductNonSimd(
            final int thisOffset,
            final FloatArray other, final int otherOffset,
            final int size) {
        float result = 0f;
        for (int j = 0; j < size; j++) {
            result += this.get(thisOffset + j) * other.get(otherOffset + j);
        }
        return result;
    }

    public float dotProduct(
            final int thisOffset,
            final FloatArray other, final int otherOffset,
            final int size) {

        if(size < SPECIES.length()) return dotProductNonSimd(0, other, 0, size);

        // perform dot product using Vector API
        var sum = FloatVector.zero(SPECIES);
        int i = 0;
        for (; i < size - SPECIES.length(); i += SPECIES.length()) {
            var a = this.floatVector(SPECIES, thisOffset + i);
            var b = other.floatVector(SPECIES, otherOffset + i);
            sum = sum.add(a.mul(b));
        }
        float dotProduct = sum.reduceLanes(VectorOperators.ADD);
        // remaining if any
        for (; i < size; i++) {
            dotProduct += this.get(thisOffset + i) * other.get(otherOffset + i);
        }
        return dotProduct;
    }

    public float dotProduct(final FloatArray other) {
        final int thisSize = Math.toIntExact(shape().totalSize());
        final int otherSize = Math.toIntExact(shape().totalSize());
        if(thisSize!=otherSize) throw new IllegalArgumentException(
            "this shape (%d) and other shape (%d) must be of equal totalSize"
            .formatted(thisSize, otherSize));
        return dotProduct(0, other, 0, thisSize);
    }

    // -- CONTRACT

    @Override
    public boolean equals(final Object obj) {
        return JComputeArray.equals(this, obj);
    }

}
