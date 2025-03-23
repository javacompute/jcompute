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
public class ShortUtils {

    public void toBytes(final short v, final byte[] bytes) {
        bytes[0] = (byte)(v >> 8);
        bytes[1] = (byte)(v);
    }

    public short fromBytes(final byte[] bytes) {
        return pack(bytes[0], bytes[1]);
    }

    public short pack(final byte mostSignificant, final byte leastSignificant) {
        return (short)(((mostSignificant & 0xff)<<8)
                | (leastSignificant & 0xff));
    }

    // JUnit
    short[] samples() {
        return new short[] {
                Short.MIN_VALUE,
                -16234,
                -1,
                0,
                1,
                16234,
                Short.MAX_VALUE,
        };
    }

}
