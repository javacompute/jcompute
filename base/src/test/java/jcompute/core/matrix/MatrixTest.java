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
import java.util.Random;
import java.util.function.DoubleBinaryOperator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jcompute.core.mem.FloatArray;
import jcompute.core.shape.Shape;
import jcompute.core.util.function.FloatUnaryOperator;
import jcompute.core.util.timing.Timing;

// and others, rename
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
    void toArray2D() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap(arena, 1, 2, 3, 4, 5, 6)
                    .reshape(Shape.of(2, 3));
            var b = FloatArray.wrap(arena, 7, 8, 9, 10, 11, 12)
                    .reshape(Shape.of(3, 2));
            assertArrayEquals(new float[][] {{1, 2, 3}, {4, 5, 6}}, a.toArray2D());
            assertArrayEquals(new float[][] {{7, 8}, {9, 10}, {11, 12}}, b.toArray2D());
        }
    }

    @Test
    void multiplyClassic() {
        float[][] a = {{1, 2, 3}, {4, 5, 6}};
        float[][] b = {{7, 8}, {9, 10}, {11, 12}};
        float[][] expected = {{58, 64}, {139, 154}};
        assertArrayEquals(expected, MatrixMultiply.multiplyFloat(a, b));
    }

    @Test
    void multiply() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap2D(arena, new float[][]{{1, 2, 3}, {4, 5, 6}});
            var b = FloatArray.wrap(arena, 7, 8, 9, 10, 11, 12)
                    .reshape(Shape.of(3, 2));
            var ab = MatrixMultiply.multiply(arena, a, b);
            assertEquals(FloatArray.wrap(arena, 58, 64, 139, 154)
                    .reshape(Shape.of(2, 2)),
                    ab);
        }
    }

    @Test
    void multiplyTransposed() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap(arena, 1, 2, 3, 4, 5, 6)
                    .reshape(Shape.of(2, 3));
            var aInvA = MatrixMultiply.multiplyTransposed(arena, a, a);
            assertEquals(FloatArray.wrap(arena, 14, 32, 32, 77)
                    .reshape(Shape.of(2, 2)),
                    aInvA);
        }
    }

    // MatrixTest 1024x1024 took 102 ms
    @Test @Disabled
    void bench() {
        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.of(arena, Shape.of(1024, 1024));
            var b = FloatArray.of(arena, Shape.of(1024, 1024));

            var rand = new Random(19);
            a.mapInPlace(0, a.shape().totalSize(), _->rand.nextFloat());
            b.mapInPlace(0, b.shape().totalSize(), _->rand.nextFloat());

            // classic
            float[][] matA = a.toArray2D();
            float[][] matB = b.transpose(arena).toArray2D();
            float[][] matC = MatrixMultiply.multiplyFloat(matA, matB);
            float[][] matC2 = MatrixMultiply.multiplyFloat2(matA, matB); // higher precision

            var t = Timing.now();
            var c = MatrixMultiply.multiplyTransposed(arena, a, b);
            t.stop();
            System.out.println("MatrixTest 1024x1024 took " + t);
            assertEquals(Shape.of(1024, 1024), c.shape());

            var result = c.toArray2D();
            for(int i = 0; i < Math.toIntExact(c.shape().sizeX()); ++i) {
                assertArrayEquals(matC2[i], matC[i], 1E-3f);
                assertArrayEquals(matC2[i], result[i], 1E-3f);
            }
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
