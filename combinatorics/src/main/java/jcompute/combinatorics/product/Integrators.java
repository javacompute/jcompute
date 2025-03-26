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

import java.util.stream.Gatherer.Downstream;
import java.util.stream.Gatherer.Integrator;

import jcompute.core.util.function.MultiIntPredicate;

final class Integrators {

    record Integrator1()
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            return mip.test(i);
        }
    }

    record Integrator2(int n1)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                if(!mip.test(i, j)) return false;
            }
            return true;
        }
    }

    record Integrator3(int n1, int n2)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    if(!mip.test(i, j, k)) return false;
                }
            }
            return true;
        }
    }

    record Integrator4(int n1, int n2, int n3)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    for(int l=0; l<n3; ++l){
                        if(!mip.test(i, j, k, l)) return false;
                    }
                }
            }
            return true;
        }
    }

    record Integrator5(int n1, int n2, int n3, int n4)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    for(int l=0; l<n3; ++l){
                        for(int m=0; m<n4; ++m){
                            if(!mip.test(i, j, k, l, m)) return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    record Integrator6(int n1, int n2, int n3, int n4, int n5)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    for(int l=0; l<n3; ++l){
                        for(int m=0; m<n4; ++m){
                            for(int n=0; n<n5; ++n){
                                if(!mip.test(i, j, k, l, m, n)) return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    record Integrator7(int n1, int n2, int n3, int n4, int n5, int n6)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    for(int l=0; l<n3; ++l){
                        for(int m=0; m<n4; ++m){
                            for(int n=0; n<n5; ++n){
                                for(int o=0; o<n6; ++o){
                                    if(!mip.test(i, j, k, l, m, n, o)) return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    record Integrator8(int n1, int n2, int n3, int n4, int n5, int n6, int n7)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=0; j<n1; ++j){
                for(int k=0; k<n2; ++k){
                    for(int l=0; l<n3; ++l){
                        for(int m=0; m<n4; ++m){
                            for(int n=0; n<n5; ++n){
                                for(int o=0; o<n6; ++o){
                                    for(int p=0; p<n7; ++p){
                                        if(!mip.test(i, j, k, l, m, n, o, p)) return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    record IntegratorAsc2(int n1)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                if(!mip.test(i, j)) return false;
            }
            return true;
        }
    }

    record IntegratorAsc3(int n1, int n2)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    if(!mip.test(i, j, k)) return false;
                }
            }
            return true;
        }
    }

    record IntegratorAsc4(int n1, int n2, int n3)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    for(int l=k+1; l<n3; ++l){
                        if(!mip.test(i, j, k, l)) return false;
                    }
                }
            }
            return true;
        }
    }

    record IntegratorAsc5(int n1, int n2, int n3, int n4)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    for(int l=k+1; l<n3; ++l){
                        for(int m=l+1; m<n4; ++m){
                            if(!mip.test(i, j, k, l, m)) return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    record IntegratorAsc6(int n1, int n2, int n3, int n4, int n5)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    for(int l=k+1; l<n3; ++l){
                        for(int m=l+1; m<n4; ++m){
                            for(int n=m+1; n<n5; ++n){
                                if(!mip.test(i, j, k, l, m, n)) return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    record IntegratorAsc7(int n1, int n2, int n3, int n4, int n5, int n6)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    for(int l=k+1; l<n3; ++l){
                        for(int m=l+1; m<n4; ++m){
                            for(int n=m+1; n<n5; ++n){
                                for(int o=n+1; o<n6; ++o){
                                    if(!mip.test(i, j, k, l, m, n, o)) return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    record IntegratorAsc8(int n1, int n2, int n3, int n4, int n5, int n6, int n7)
    implements Integrator<Void, Integer, int[]> {
        @Override
        public boolean integrate(final Void state, final Integer i, final Downstream<? super int[]> downstream) {
            final MultiIntPredicate mip = downstream::push;
            for(int j=i+1; j<n1; ++j){
                for(int k=j+1; k<n2; ++k){
                    for(int l=k+1; l<n3; ++l){
                        for(int m=l+1; m<n4; ++m){
                            for(int n=m+1; n<n5; ++n){
                                for(int o=n+1; o<n6; ++o){
                                    for(int p=o+1; p<n7; ++p){
                                        if(!mip.test(i, j, k, l, m, n, o, p)) return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
}
