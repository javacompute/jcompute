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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jcompute.core.util.function.MultiIntConsumer;
import jcompute.core.util.function.MultiIntPredicate;
import jcompute.core.util.function.PrefixedMultiIntConsumer;

public record IndexSpaceN(int ...dim) implements IndexSpace {

    @Override public int indexCount() { return dim.length; }
    @Override public BigInteger cardinality() {
        return IntStream.of(dim)
            .mapToObj(BigInteger::valueOf)
            .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    @Override
    public IntStream streamIndexRanges() {
        return IntStream.of(dim);
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntConsumer intConsumer) {
        visiting.range(dim[0]).forEach(i->{
            var v = new int[dim.length];
            v[0] = i;
            switch (visiting.indexOrder()) {
                case ANY ->
                    new RecursiveVisitor(dim, v, intConsumer).recur(1);
                case ASCENDING ->
                    new RecursiveVisitorAsc(dim, v, intConsumer).recur(1);
            }
        });
    }

    @Override
    public void forEach(final Visiting visiting, final MultiIntPredicate branchFilter, final MultiIntConsumer intConsumer) {
        visiting.range(dim[0]).forEach(i->{
            var v = new int[dim.length];
            v[0] = i;
            switch (visiting.indexOrder()) {
                case ANY ->
                    new RecursiveVisitorWithBranchFilter(dim, v, branchFilter, intConsumer).recur(1);
                case ASCENDING ->
                    new RecursiveVisitorWithBranchFilterAsc(dim, v, branchFilter, intConsumer).recur(1);
            }
        });
    }

    @Override
    public Stream<int[]> stream(final Visiting visiting) {
        return visiting.range(dim[0])
            .mapToObj(Integer::valueOf)
            .gather(Gatherer.of(new IntegratorN(visiting.indexOrder(), dim)));
    }

    @Override
    public <T> Stream<T> streamCollectors(final IntFunction<T> collectorFactory, final PrefixedMultiIntConsumer<T> prefixedIntConsumer) {
        return Concurrency.PARALLEL.range(dim[0]).mapToObj(i->{
            var t = collectorFactory.apply(i);
            var v = new int[dim.length];
            v[0] = i;
            new RecursiveCollector<>(dim, v, t, prefixedIntConsumer).recur(1);
            return t;
        });
    }

    @Override
    public Optional<int[]> findAny(final MultiIntPredicate intPredicate) {
        final AtomicReference<int[]> result = new AtomicReference<>();
        return Concurrency.PARALLEL.range(dim[0])
            .mapToObj(i->{
                if(result.get()!=null) return null;
                var v = new int[dim.length];
                v[0] = i;
                new RecursiveFinder(dim, v, intPredicate, result).recur(1);
                return result.get();
            })
            .filter(Objects::nonNull)
            .findAny();
    }

    // -- HELPER

    private record IntegratorN(IndexOrder indexOrder, int ...dim)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            final AtomicBoolean stop = new AtomicBoolean();
            var v = new int[dim.length];
            v[0] = i;
            switch (indexOrder) {
                case ANY ->
                    new RecursiveWhile(dim, v, mip::test, stop).recur(1);
                case ASCENDING ->
                    new RecursiveWhileAsc(dim, v, mip::test, stop).recur(1);
            }
            return !stop.get();
        }
    }

    private record RecursiveVisitor(int[] dim, int[] v, MultiIntConsumer intConsumer) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=0; l<lRange; ++l){
                    v[dimIndex] = l;
                    final int kRange = dim[dimIndex+1];
                    for(int k=0; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        final int jRange = dim[dimIndex+2];
                        for(int j=0; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            final int iRange = dim[dimIndex+3];
                            for(int i=0; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                intConsumer.accept(v);
                            }
                        }
                    }
                }
                return;
            }
            for(int i=0; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
            }
        }
    }

    private record RecursiveVisitorAsc(int[] dim, int[] v, MultiIntConsumer intConsumer) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=dim[dimIndex-1]+1; l<lRange; ++l){
                    v[dimIndex] = l;
                    final int kRange = dim[dimIndex+1];
                    for(int k=l+1; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        final int jRange = dim[dimIndex+2];
                        for(int j=k+1; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            final int iRange = dim[dimIndex+3];
                            for(int i=j+1; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                intConsumer.accept(v);
                            }
                        }
                    }
                }
                return;
            }
            for(int i=dim[dimIndex-1]+1; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
            }
        }
    }

    private record RecursiveVisitorWithBranchFilter(
            int[] dim,
            int[] v,
            MultiIntPredicate branchFilter,
            MultiIntConsumer intConsumer) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=0; l<lRange; ++l){
                    v[dimIndex] = l;
                    if(!testBranch(v, dimIndex + 1)) continue;
                    final int kRange = dim[dimIndex+1];
                    for(int k=0; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        if(!testBranch(v, dimIndex + 2)) continue;
                        final int jRange = dim[dimIndex+2];
                        for(int j=0; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            if(!testBranch(v, dimIndex + 3)) continue;
                            final int iRange = dim[dimIndex+3];
                            for(int i=0; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                if(!testBranch(v, dimIndex + 4)) continue;
                                intConsumer.accept(v);
                            }
                        }
                    }
                }
                return;
            }
            for(int i=0; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                if(testBranch(v, dimIndex + 1)) {
                    recur(dimIndex + 1);
                }
            }
        }
        private boolean testBranch(final int[] v, final int len) {
            return branchFilter.test(Arrays.copyOf(v, len));
        }
    }

    private record RecursiveVisitorWithBranchFilterAsc(
            int[] dim,
            int[] v,
            MultiIntPredicate branchFilter,
            MultiIntConsumer intConsumer) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=dim[dimIndex-1]+1; l<lRange; ++l){
                    v[dimIndex] = l;
                    if(!testBranch(v, dimIndex + 1)) continue;
                    final int kRange = dim[dimIndex+1];
                    for(int k=l+1; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        if(!testBranch(v, dimIndex + 2)) continue;
                        final int jRange = dim[dimIndex+2];
                        for(int j=k+1; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            if(!testBranch(v, dimIndex + 3)) continue;
                            final int iRange = dim[dimIndex+3];
                            for(int i=j+1; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                if(!testBranch(v, dimIndex + 4)) continue;
                                intConsumer.accept(v);
                            }
                        }
                    }
                }
                return;
            }
            for(int i=dim[dimIndex-1]+1; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                if(testBranch(v, dimIndex + 1)) {
                    recur(dimIndex + 1);
                }
            }
        }
        private boolean testBranch(final int[] v, final int len) {
            return branchFilter.test(Arrays.copyOf(v, len));
        }
    }

    private record RecursiveCollector<T>(int[] dim, int[] v, T t, PrefixedMultiIntConsumer<T> prefixedIntConsumer) {
        void recur(final int dimIndex){
            if(dimIndex == v.length) {
                prefixedIntConsumer.accept(t, v);
                return;
            }
            for(int i=0; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
            }
        }
    }

    // perhaps use stream().filter(...).findAny() instead - needs benchmarks
    private record RecursiveFinder(int[] dim, int[] v, MultiIntPredicate intPredicate, AtomicReference<int[]> result) {
        void recur(final int dimIndex){
            if(dimIndex == v.length) {
                if(intPredicate.test(v)) {
                    result.set(v.clone());
                }
                return;
            }
            for(int i=0; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
            }
        }
    }

    private record RecursiveWhile(int[] dim, int[] v, MultiIntPredicate condition, AtomicBoolean stop) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=0; l<lRange; ++l){
                    v[dimIndex] = l;
                    final int kRange = dim[dimIndex+1];
                    for(int k=0; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        final int jRange = dim[dimIndex+2];
                        for(int j=0; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            final int iRange = dim[dimIndex+3];
                            for(int i=0; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                if(!condition.test(v)) {
                                    stop.set(true);
                                    return;
                                }
                            }
                        }
                    }
                }
                return;
            }
            for(int i=0; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
                if(stop.get()) return;
            }
        }
    }

    private record RecursiveWhileAsc(int[] dim, int[] v, MultiIntPredicate condition, AtomicBoolean stop) {
        void recur(final int dimIndex){
            if(dimIndex == v.length-4) {
                final int lRange = dim[dimIndex];
                for(int l=v[dimIndex-1]+1; l<lRange; ++l){
                    v[dimIndex] = l;
                    final int kRange = dim[dimIndex+1];
                    for(int k=l+1; k<kRange; ++k){
                        v[dimIndex+1] = k;
                        final int jRange = dim[dimIndex+2];
                        for(int j=k+1; j<jRange; ++j){
                            v[dimIndex+2] = j;
                            final int iRange = dim[dimIndex+3];
                            for(int i=j+1; i<iRange; ++i){
                                v[dimIndex+3] = i;
                                if(!condition.test(v)) {
                                    stop.set(true);
                                    return;
                                }
                            }
                        }
                    }
                }
                return;
            }
            for(int i=v[dimIndex-1]+1; i<dim[dimIndex]; ++i){
                v[dimIndex] = i;
                recur(dimIndex + 1);
                if(stop.get()) return;
            }
        }
    }

}
