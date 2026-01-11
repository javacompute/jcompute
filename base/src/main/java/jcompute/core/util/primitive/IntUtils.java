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

import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;
import java.util.stream.IntStream;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IntUtils {

    // -- ARITHEMTIC

    /// non-negative remainder of x divided by modulus
    public int euclidianMod(final int x, final int modulus) {
        assertGreaterThanZero(modulus, ()->"modulus must be greater than zero");
        int result = x % modulus;
        if (result < 0) {
            result += modulus;
        }
        return result;
    }

    // -- PACKING

    public int pack(final short mostSignificant, final short leastSignificant) {
        return ((mostSignificant & 0xffff)<<16)
                | (leastSignificant & 0xffff);
    }

    // -- ARRAY

    public boolean isEmpty(final int[] array) {
        return array==null
                || array.length == 0;
    }

    public int size(final int[] array) {
        return array==null
                ? 0
                : array.length;
    }

    public int[] nullToEmpty(final int[] array) {
        return array!=null
                ? array
                : new int[0];
    }

    public boolean allEqual(final int[] array) {
        if(size(array)<2) return true;
        int firstElement = array[0];
        for (int number : array) {
            if (number != firstElement)
                return false; // found a different number
        }
        return true; // all numbers are the same
    }

    // -- STREAM

    public boolean allEqual(final IntStream stream) {
        return stream
                .allMatch(new AllEqualIntPredicate());
    }

    public IntStream mapAdjacent(final IntStream stream, final IntBinaryOperator mapper) {
        if(stream.isParallel())
            throw new IllegalArgumentException("adjacency is only well defined for a sequential stream");

        return stream
                .boxed()
                .gather(mapAdjacent(mapper))
                .mapToInt(Integer::intValue);
    }

    // -- ASSERT


    public int assertNonNegative(final int value, final Supplier<String> msgSupplier) {
        if(value<0)
            throw new IllegalArgumentException(msgSupplier.get());
        return value;
    }

    public int assertGreaterThanZero(final int value, final Supplier<String> msgSupplier) {
        if(value<1)
            throw new IllegalArgumentException(msgSupplier.get());
        return value;
    }

    // -- HELPER

    private static Gatherer<Integer, ?, Integer> mapAdjacent(final IntBinaryOperator mapper) {
        class SlidingWindow {
            int latest;
            boolean first = true;

            boolean integrate(final Integer element, final Downstream<? super Integer> downstream) {
                if(first) {
                    this.latest = element;
                    this.first = false;
                    return true;
                }
                var res = downstream.push(mapper.applyAsInt(latest, element));
                this.latest = element;
                return res;
            }
        }
        return Gatherer.<Integer, SlidingWindow, Integer>ofSequential(
                SlidingWindow::new, // Initializer
                Integrator.<SlidingWindow, Integer, Integer>ofGreedy(SlidingWindow::integrate));
    }

    private static final class AllEqualIntPredicate
    implements IntPredicate {
        boolean initialized = false;
        int first = 0;
        @Override
        public boolean test(final int value) {
            if(!initialized) {
                this.first = value;
                this.initialized = true;
                return true;
            }
            return value == first;
        }
    }

    // JUnit
    public int[] samples() {
        return new int[] {
                Integer.MIN_VALUE,
                -1_623_456_789,
                -1,
                0,
                1,
                1_623_456_789,
                Integer.MAX_VALUE,
        };
    }

}
