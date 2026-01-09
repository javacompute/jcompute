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
package jcompute.core.util.primitive;

import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(IntUtils.class)
class IntUtilsTest {

    @Test
    void packing() {
        var byteArray = new byte[Integer.BYTES];
        for(int v : IntUtils.samples()) {
            IntUtils.toBytes(v, byteArray);

            assertEquals(v, IntUtils.fromBytes(byteArray));
            assertEquals(v, IntUtils.pack(
                    ShortUtils.pack(byteArray[0], byteArray[1]),
                    ShortUtils.pack(byteArray[2], byteArray[3])));
        }
    }

    /// instead of consuming the entire stream, should exit once allEqual holds no longer true after 11 numbers generated
    @Test
    void earlyExit() {

        final class LimitedIntSupplier implements IntSupplier {
            final int limit = 10;
            int acc = 0;
            int callCount = 0;
            @Override public int getAsInt() {
                this.callCount++;
                if(acc==limit) return 0;
                return acc++;
            }
        }

        var limitedIntSupplier = new LimitedIntSupplier();
        IntStream.generate(limitedIntSupplier)
            .limit(100)
            .mapAdjacent((a, b)->b-a)
            .allEqual();

        assertEquals(11, limitedIntSupplier.callCount);
    }

    /// https://www.youtube.com/watch?v=UjEyLb_A3SY
    record IntegerProgression(int[] numbers) {
        public boolean test() {
            return IntStream.of(numbers.nullToEmpty())
                    .sorted()
                    .mapAdjacent((a, b) -> a-b)
                    .allEqual();
        }
    }

    @Test
    void integerProgression() {
        assertTrue(new IntegerProgression(null).test());
        assertTrue(new IntegerProgression(new int[] {}).test());
        assertTrue(new IntegerProgression(new int[] {1}).test());
        assertTrue(new IntegerProgression(new int[] {1, 2}).test());
        assertTrue(new IntegerProgression(new int[] {1, 3, 2}).test());
        assertTrue(new IntegerProgression(new int[] {3, 1, 7, 9, 5}).test());
        assertFalse(new IntegerProgression(new int[] {1, 4, 2}).test());
        assertFalse(new IntegerProgression(new int[] {3, 1, 7, 9, 6}).test());
    }

}
