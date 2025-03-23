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

import lombok.experimental.UtilityClass;

@UtilityClass
public class IntUtils {

    public void toBytes(final int v, final byte[] bytes) {
        bytes[0] = (byte)(v >> 24);
        bytes[1] = (byte)(v >> 16);
        bytes[2] = (byte)(v >> 8);
        bytes[3] = (byte)(v);
    }

    public int fromBytes(final byte[] bytes) {
        int v = 0;
        v|= (bytes[0] & 0xff) << 24;
        v|= (bytes[1] & 0xff) << 16;
        v|= (bytes[2] & 0xff) << 8;
        v|= (bytes[3] & 0xff) << 0;
        return v;
    }

    public int pack(final short mostSignificant, final short leastSignificant) {
        return ((mostSignificant & 0xffff)<<16)
                | (leastSignificant & 0xffff);
    }

    // JUnit
    int[] samples() {
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
