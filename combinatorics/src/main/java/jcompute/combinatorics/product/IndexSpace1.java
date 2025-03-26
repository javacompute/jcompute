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
import java.util.stream.Gatherer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jcompute.core.util.function.MultiIntConsumer;
import jcompute.core.util.function.MultiIntPredicate;
import jcompute.core.util.function.PrefixedMultiIntConsumer;

record IndexSpace1(int n0) implements IndexSpace {

    @Override public int indexCount() { return 1; }
    @Override public BigInteger cardinality() { return BigInteger.valueOf(n0); }

    @Override
    public IntStream streamIndexRanges() {
        return IntStream.of(n0);
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntConsumer intConsumer) {
        visiting.range(n0).forEach(i->{
            intConsumer.accept(i);
        });
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntPredicate branchFilter, final MultiIntConsumer intConsumer) {
        visiting.range(n0).forEach(i->{
            if(branchFilter.test(i)) intConsumer.accept(i);
        });
    }

    @Override
    public Stream<int[]> stream(final Visiting visiting) {
        return visiting.range(n0)
            .mapToObj(Integer::valueOf)
            .gather(Gatherer.of(new Integrators.Integrator1()));
    }

    @Override
    public <T> Stream<T> streamCollectors(final IntFunction<T> collectorFactory, final PrefixedMultiIntConsumer<T> prefixedIntConsumer) {
        return Concurrency.PARALLEL.range(n0).mapToObj(i->{
            T t = collectorFactory.apply(i);
            prefixedIntConsumer.accept(t, i);
            return t;
        });
    }

    @Override
    public Optional<int[]> findAny(final MultiIntPredicate intPredicate) {
        return Concurrency.PARALLEL.range(n0)
            .filter(intPredicate::test)
            .mapToObj(i->new int[] {i})
            .findAny();
    }

}
