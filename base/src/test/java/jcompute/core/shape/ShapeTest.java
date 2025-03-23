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
package jcompute.core.shape;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShapeTest {

    Shape shape2;

    static Stream<Arguments> args() {
        return Stream.of(
                Arguments.of(Named.of("Shape.empty()", Shape.empty()), 0L),
                Arguments.of(Named.of("Shape.of(7)", Shape.of(7)), 7L),
                Arguments.of(Named.of("Shape.of(3,5)", Shape.of(3,5)), 15L),
                Arguments.of(Named.of("Shape.of(3,5,7)", Shape.of(3,5,7)), 105L)
        );
    }

    @ParameterizedTest
    @MethodSource("args")
    void roundtripOnExternalization(final Shape shape, final long expectedTotalSize) throws IOException {

        assertEquals(expectedTotalSize, shape.totalSize());

        byte[] serializedBytes;

        try(var bos = new ByteArrayOutputStream()) {
            shape.write(bos);
            bos.flush();
            serializedBytes = bos.toByteArray();
        }
        try(var bis = new ByteArrayInputStream(serializedBytes)) {
            shape2 = Shape.read(bis);
        }

        assertEquals(shape, shape2);
    }

}
