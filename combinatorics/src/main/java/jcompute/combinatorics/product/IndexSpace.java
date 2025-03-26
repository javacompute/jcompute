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
package jcompute.combinatorics.product;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jcompute.core.util.function.MultiIntConsumer;
import jcompute.core.util.function.MultiIntPredicate;
import jcompute.core.util.function.PrefixedMultiIntConsumer;

///Given multiple indices, an index space is spanned with index ranges `n0` x `n1` x ...
///
///For indices `i` ranging from 0 to `(n0-1)`, `j` from 0 to `(n1-1)`, and so on,
///the Cartesian product forms all possible ordered tuples `(i, j, k, ...)`.
///
///Example:
/// ```
///  // print all 3 x 2 (index) tuples
///  var indexSpace = IndexSpace.create(3, 2);
///  indexSpace.forEachSequential(tuple->System.out.println("tuple: %d, %d".formatted(tuple[0], tuple[1])));
/// ```
///Caveats / Future Work:
///- no branch filtering
public interface IndexSpace {

    enum IndexOrder {
        /// Unconstraint, meaning all tuples are considered.
        ANY,
        /// Only tuples are considered, where index<sub>0</sub> < index<sub>1</sub> < ...
        ASCENDING;
    }

    enum Concurrency {
        SEQUENTIAL{
            @Override
            IntStream range(final int upperExclusive) {
                return IntStream.range(0, upperExclusive);
            }
        },
        PARALLEL{
            @Override
            IntStream range(final int upperExclusive) {
                return IntStream.range(0, upperExclusive).parallel();
            }
        };
        abstract IntStream range(int upperExclusive);
    }

    public record Visiting(IndexOrder indexOrder, Concurrency concurrency) {
        public static Visiting sequential() {
            return new Visiting(IndexOrder.ANY, Concurrency.SEQUENTIAL);
        }
        public static Visiting parallel() {
            return new Visiting(IndexOrder.ANY, Concurrency.PARALLEL);
        }
        public IntStream range(final int upperExclusive) {
            return concurrency.range(upperExclusive);
        }
    }

    /**
     * Number of distinct tuples of this space.
     */
    BigInteger cardinality();
    /**
     * Number of indices (dimensions) of this space.
     */
    int indexCount();
    /**
     * Streams the index ranges, that is the size of all the dimensions of this space.
     */
    IntStream streamIndexRanges();

    /**
     * Visits all distinct tuples honoring given {@link IndexOrder}.
     */
    void forEach(Visiting visiting, MultiIntConsumer intConsumer);
    /**
     * Visits all distinct tuples, that pass given branchFilter also honoring given {@link IndexOrder}.
     */
    void forEach(Visiting visiting, MultiIntPredicate branchFilter, MultiIntConsumer intConsumer);

    /**
     * Streams all distinct tuples, honoring given {@link IndexOrder}.
     */
    Stream<int[]> stream(Visiting visiting);

    /**
     * Creates a collector for each possible integer of the first dimension, then streams them after they passed given prefixedIntConsumer.
     */
    <T> Stream<T> streamCollectors(final IntFunction<T> collectorFactory, final PrefixedMultiIntConsumer<T> prefixedIntConsumer);

    /**
     * Visits up to all distinct tuples, optionally returning any that matches given predicate.
     */
    Optional<int[]> findAny(MultiIntPredicate intPredicate);

    // -- SHORTCUTS

    default void forEachSequential(final MultiIntConsumer intConsumer) {
        forEach(Visiting.sequential(), intConsumer);
    }
    default void forEachParallel(final MultiIntConsumer intConsumer) {
        forEach(Visiting.parallel(), intConsumer);
    }
    default void forEachSequential(final MultiIntPredicate branchFilter, final MultiIntConsumer intConsumer) {
        forEach(Visiting.sequential(), branchFilter, intConsumer);
    }
    default void forEachParallel(final MultiIntPredicate branchFilter, final MultiIntConsumer intConsumer) {
        forEach(Visiting.parallel(), branchFilter, intConsumer);
    }
    default Stream<int[]> streamSequential() {
        return stream(Visiting.sequential());
    }
    default Stream<int[]> streamParallel() {
        return stream(Visiting.parallel());
    }

    // -- FACTORY

    public static IndexSpace create(final int... dim) {
        if(dim==null
            || dim.length==0
            || IntStream.of(dim).anyMatch(size->size<=0)) return new IndexSpace0();

        return switch (dim.length) {
            case 1 -> new IndexSpace1(dim[0]);
            case 2 -> new IndexSpace2(dim[0], dim[1]);
            case 3 -> new IndexSpace3(dim[0], dim[1], dim[2]);
            case 4 -> new IndexSpace4(dim[0], dim[1], dim[2], dim[3]);
            case 5 -> new IndexSpace5(dim[0], dim[1], dim[2], dim[3], dim[4]);
            case 6 -> new IndexSpace6(dim[0], dim[1], dim[2], dim[3], dim[4],
                dim[5]);
            case 7 -> new IndexSpace7(dim[0], dim[1], dim[2], dim[3], dim[4],
                dim[5], dim[6]);
            case 8 -> new IndexSpace8(dim[0], dim[1], dim[2], dim[3], dim[4],
                dim[5], dim[6], dim[7]);
//            case 8 -> new IndexSpaceComposite(
//                new IndexSpace3(dim[0], dim[1], dim[2]),
//                new IndexSpace5(dim[3], dim[4], dim[5], dim[6], dim[7]));
            default -> new IndexSpaceN(dim);
        };
    }



}
