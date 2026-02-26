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
package jcompute.core.util.coors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoorsTest {

    @Test
    void origin() {
        Assertions.assertEquals(Cartesian3.ORIGIN, new Cartesian3(0, 0, 0));
        Assertions.assertEquals(Polar3.ORIGIN, new Polar3(0, 0, 0));
        Assertions.assertEquals(Polar3.ORIGIN, new Polar3(0, 1, 1));
    }

    @Test
    void convert() {
        assertRoundtrip(new Cartesian3(0, 0, 0), 1E-9);
        assertRoundtrip(new Cartesian3(1, 1, 1), 1E-9);
        assertRoundtrip(new Cartesian3(-1, 1, 1), 1E-9);
        assertRoundtrip(new Cartesian3(1, -1, 1), 1E-9);
        assertRoundtrip(new Cartesian3(1, 1, -1), 1E-9);
        assertRoundtrip(new Cartesian3(-1, -1, 1), 1E-9);
        assertRoundtrip(new Cartesian3(-1, 1, -1), 1E-9);
        assertRoundtrip(new Cartesian3(1, -1, -1), 1E-9);
        assertRoundtrip(new Cartesian3(-1, -1, -1), 1E-9);
    }

    void assertRoundtrip(final Cartesian3 a, final double eps) {
        assertEquals(a, a.toSpherical().toCartesian(), eps);
    }

    void assertEquals(final Cartesian3 a, final Cartesian3 b, final double eps) {
        Assertions.assertEquals(a.x(), b.x(), eps);
    }

}
