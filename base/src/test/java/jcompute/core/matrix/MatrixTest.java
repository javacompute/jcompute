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
import java.util.function.DoubleBinaryOperator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jcompute.core.mem.FloatArray;
import jcompute.core.shape.Shape;
import jcompute.core.util.function.FloatUnaryOperator;

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

    @Test
    void sigmoid() {
        // torch.special.expit(input, *, out=None) → Tensor
        // Computes the expit (also known as the logistic sigmoid function) of the elements of input.
        FloatUnaryOperator sigmoid = in->(float)(1./(1. + Math.exp(-in)));
        assertEquals(0.7153f, sigmoid.applyAsFloat(0.9213f), 1E-4);
        assertEquals(0.7481f, sigmoid.applyAsFloat(1.0887f), 1E-4);
        assertEquals(0.2920f, sigmoid.applyAsFloat(-0.8858f), 1E-4);
        assertEquals(0.1458f, sigmoid.applyAsFloat(-1.7683f), 1E-4);
    }

    @Test
    void bceloss() {
        DoubleBinaryOperator bceLoss = (x, y)->-(y*Math.log(x) + (1-y) * Math.log(1 - x));
        assertEquals(0.0852, bceLoss.applyAsDouble(0.9183, 1), 1E-4);

    }

}
