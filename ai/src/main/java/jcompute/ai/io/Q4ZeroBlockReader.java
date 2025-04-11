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
package jcompute.ai.io;

import static jcompute.core.mem.FloatArray.SPECIES;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

import jcompute.core.mem.FloatArray;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;

public record Q4ZeroBlockReader(MemorySegment memorySegment) {
    public final static int FLOATS_PER_BLOCK = 32;
    public final static int BYTES_PER_BLOCK = 18;

    //debug    final static VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

    public float dot(
            final int blockOffset, final int blockCount,
            final FloatArray floatArray, final int floatArrayOffset) {
        var acc = FloatVector.zero(SPECIES);
        int boff = blockOffset;
        int k = floatArrayOffset;
        for(int bi=0; bi<blockCount; ++bi, boff+= BYTES_PER_BLOCK) {
            var wScale = FloatVector.broadcast(SPECIES, scale(boff));
            var wBytes = blockBytes(boff);
            var loBytes = lowNibblesSigned(wBytes);
            var hiBytes = highNibblesSigned(wBytes);

            switch (SPECIES.vectorBitSize()) {
                case 512 -> {
                    // we have 32 floats per block, hence we can fill up 2 vectors of dim=16 from a single block
                    var sum0 = floatArray.floatVector(SPECIES, k).mul(loBytes.castShape(SPECIES, 0)); k+=SPECIES.length();
                    var sum1 = floatArray.floatVector(SPECIES, k).mul(hiBytes.castShape(SPECIES, 0)); k+=SPECIES.length();
                    acc = sum0.add(sum1).fma(wScale, acc);
                }
                case 256 -> {
                    // we have 32 floats per block, hence we can fill up 4 vectors of dim=8 from a single block
                    var sum0 = floatArray.floatVector(SPECIES, k).mul(loBytes.castShape(SPECIES, 0)); k+=SPECIES.length();
                    var sum1 = floatArray.floatVector(SPECIES, k).mul(loBytes.castShape(SPECIES, 1)); k+=SPECIES.length();
                    var sum2 = floatArray.floatVector(SPECIES, k).mul(hiBytes.castShape(SPECIES, 0)); k+=SPECIES.length();
                    var sum3 = floatArray.floatVector(SPECIES, k).mul(hiBytes.castShape(SPECIES, 1)); k+=SPECIES.length();
                    acc = sum0.add(sum1).add(sum2).add(sum3).fma(wScale, acc);
                }
                case 128 -> {
                    // we have 32 floats per block, hence we can fill up 8 vectors of dim=4 from a single block
                    for (int i = 0; i < 2; ++i) {
                        var nibbles = i == 0 ? loBytes : hiBytes;
                        var sum0 = floatArray.floatVector(SPECIES, k).mul(nibbles.castShape(SPECIES, 0)); k+=SPECIES.length();
                        var sum1 = floatArray.floatVector(SPECIES, k).mul(nibbles.castShape(SPECIES, 1)); k+=SPECIES.length();
                        var sum2 = floatArray.floatVector(SPECIES, k).mul(nibbles.castShape(SPECIES, 2)); k+=SPECIES.length();
                        var sum3 = floatArray.floatVector(SPECIES, k).mul(nibbles.castShape(SPECIES, 3)); k+=SPECIES.length();
                        acc = sum0.add(sum1).add(sum2).add(sum3).fma(wScale, acc);
                    }
                }
                default -> throw new UnsupportedOperationException(SPECIES.toString());
            }
        }
        return acc.reduceLanes(VectorOperators.ADD);
    }

    // -- HELPER

    private float scale(final long blockOffset) {
        return Float.float16ToFloat(memorySegment.get(ValueLayout.JAVA_SHORT, blockOffset));
    }
    private ByteVector blockBytes(final long blockOffset) {
        //32 x 4bit values = 128bit
        return ByteVector.fromMemorySegment(
            ByteVector.SPECIES_128, memorySegment, blockOffset + 2, ByteOrder.LITTLE_ENDIAN);
    }
    private ByteVector lowNibblesSigned(final ByteVector blockBytes) {
        return blockBytes.and((byte) 0xF).sub((byte) 8);
    }
    private ByteVector highNibblesSigned(final ByteVector blockBytes) {
        return blockBytes.lanewise(VectorOperators.LSHR, 4).sub((byte) 8);
    }

}