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
package jcompute.combinatorics.setcover;

import java.lang.foreign.Arena;

import jcompute.combinatorics.base.Combinations;
import jcompute.core.mem.LongArray;
import jcompute.core.shape.Shape;

public record SetCoverParams(
        int v,
        int m,
        int t,
        LongArray kSets,
        LongArray pSets) {

    public SetCoverParams(
            final Arena arena,
            final int v,
            final int m,
            final int t,
            final LongArray kSets) {
        this(v, m, t, kSets, pSets(arena, v, m));
    }

    /**
     * Problem size.
     */
    public Shape shape() {
        return pSets.shape();
    }

    // -- HELPER

    private static LongArray pSets(final Arena arena, final int v, final int m) {
        final long size = Combinations.binomialAsLongValueExact(v, m);
        var shape = Shape.of(size);
        var pSets = LongArray.of(arena, shape);
        long colex = (1L << m) - 1;
        for (long gid = 0; gid < size; gid++) {
            pSets.put(gid, colex);
            colex = Combinations.next_colex(colex);
        }
        return pSets;
    }

}
