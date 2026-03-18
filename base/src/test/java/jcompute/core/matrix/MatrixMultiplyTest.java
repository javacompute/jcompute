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

import jcompute.core.mem.FloatArray;
import jcompute.core.shape.Shape;

class MatrixMultiplyTest {

    @Test
    void test() {

        try(var arena = Arena.ofConfined()) {
            var a = FloatArray.wrap(arena, 1f, 2f, 3f, 4f)
                    .reshape(Shape.of(2, 2));
            var b = a.transpose(arena);

            System.err.println("%s".formatted(a));
            System.err.println("%s".formatted(b));

            MatrixMultiply.multiply(a, b);

        }
    }

}
