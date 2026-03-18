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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jcompute.core.mem.FloatArray;
import jcompute.core.shape.Shape;

class MatrixTest {

    @Test
    void transpose() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap(arena, 1, 2, 3, 4, 5, 6)
                    .reshape(Shape.of(2, 3));
            var b = a.transpose(arena);
            assertEquals(FloatArray.wrap(arena, 1, 4, 2, 5, 3, 6)
                    .reshape(Shape.of(3, 2)),
                b);
        }
    }

    @Test
    void multiplyTransposed() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap(arena, 1, 2, 3, 4, 5, 6)
                    .reshape(Shape.of(2, 3));
            var c = MatrixMultiply.multiplyTransposed(arena, a, a);
            assertEquals(FloatArray.wrap(arena, 14, 32, 32, 77)
                    .reshape(Shape.of(2, 2)),
                c);
        }
    }

}
