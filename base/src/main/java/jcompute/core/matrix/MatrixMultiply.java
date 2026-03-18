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
package jcompute.core.matrix;

import java.lang.foreign.Arena;

import lombok.experimental.UtilityClass;

import jcompute.core.mem.FloatArray;
import jcompute.core.shape.Shape;

@UtilityClass
public class MatrixMultiply {

    public FloatArray multiplyTransposed(final Arena arena, final FloatArray a, final FloatArray b) {
        if(!a.shape().equals(b.shape()))
            throw new IllegalArgumentException("Shape mismatch: %s <-> %s"
                    .formatted(a.shape(), b.shape()));
        if(a.shape().dimensionCount()!=2)
            throw new IllegalArgumentException("Invalid Shape dimension count: %d"
                    .formatted(a.shape().dimensionCount()));

        final var inShape = a.shape();
        final long n = inShape.sizeX();
        final var outShape = Shape.of(n, n);
        final var r = FloatArray.of(arena, outShape);

        for (long i = 0L; i < n; ++i) {
            for (long j = 0L; j < n; ++j) {
                float element = a.dotProduct(inShape.gid2d(i, 0), b, inShape.gid2d(j, 0), inShape.sizeY());
                r.put(outShape.gid2d(i, j), element);
            }
        }

        return r;
    }

}
