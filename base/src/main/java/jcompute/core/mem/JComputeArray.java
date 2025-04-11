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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

import jcompute.core.shape.Shape;
import jdk.incubator.vector.VectorShape;

public interface JComputeArray {

    final static int VECTOR_BIT_SIZE = VectorShape.preferredShape().vectorBitSize();

    Shape shape();
    ValueLayout valueLayout();
    MemorySegment memorySegment();
    int bytesPerElement();

    // -- EQUALITY

    default boolean isEqualTo(final JComputeArray other) {
        return Objects.equals(this.valueLayout(), other.valueLayout())
                && Objects.equals(this.shape(), other.shape())
                && equals(this.memorySegment(), other.memorySegment());
    }

    public static boolean equals(final JComputeArray array, final Object obj) {
        return (obj instanceof JComputeArray other)
                ? array.isEqualTo(other)
                : false;
    }

    public static boolean equals(final MemorySegment a, final MemorySegment b) {
        return a.mismatch(b) == -1;
    }

}
