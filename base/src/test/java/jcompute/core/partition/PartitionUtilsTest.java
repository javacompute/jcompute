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
package jcompute.core.partition;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.RequiredArgsConstructor;

class PartitionUtilsTest {

    @RequiredArgsConstructor
    enum Scenario {
        ONE(1, 1, "0,1 "),
        _16x4(16, 4, "0,4 4,4 8,4 12,4 "),
        _17x4(17, 4, "0,5 5,4 9,4 13,4 "),
        _18x4(18, 4, "0,5 5,5 10,4 14,4 "),
        _19x4(19, 4, "0,5 5,5 10,5 15,4 "),
        ;
        final long length;
        final int n;
        final String expected;
        void verify(final StringBuilder actual) {
            assertEquals(expected, actual.toString());
        }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void visitChunks(final Scenario scenario) {
        var sb = new StringBuilder();
        PartitionUtils.visitChunks(scenario.length, scenario.n, (start, len)->
            sb
                .append(start).append(",")
                .append(len).append(" "));
        scenario.verify(sb);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void streamChunks(final Scenario scenario) {
        final var sb = new StringBuilder();
        PartitionUtils.streamChunks(scenario.length, scenario.n)
            .forEach(it->{
                sb
                    .append(it.start()).append(",")
                    .append(it.length()).append(" ");
            });
        scenario.verify(sb);
    }

}
