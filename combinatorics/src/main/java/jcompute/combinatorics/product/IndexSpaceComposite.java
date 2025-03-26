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

//experimental
record IndexSpaceComposite(IndexSpace a, IndexSpace b) implements IndexSpace {

    @Override public int indexCount() { return a.indexCount() + b.indexCount(); }
    @Override public BigInteger cardinality() {
        return a.cardinality()
            .multiply(b.cardinality());
    }

    @Override
    public IntStream streamIndexRanges() {
        return IntStream.concat(a.streamIndexRanges(), b.streamIndexRanges());
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntConsumer intConsumer) {
        a.forEach(visiting, va->{
            var v = new int[indexCount()];
            System.arraycopy(va, 0, v, 0, va.length);
            b.forEach(Visiting.sequential(), vb->{
                System.arraycopy(vb, 0, v, va.length, vb.length); // this hurts performance
                intConsumer.accept(v);
            });
        });
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntPredicate branchFilter, final MultiIntConsumer intConsumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Stream<T> streamCollectors(final IntFunction<T> collectorFactory,
        final PrefixedMultiIntConsumer<T> prefixedIntConsumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<int[]> findAny(final MultiIntPredicate intPredicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<int[]> stream(final Visiting visiting) {
        throw new UnsupportedOperationException();
    }

}
