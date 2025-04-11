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

//TODO experimental, assumes SPECIES.vectorBitSize()==512
//TODO not fast enough to be usable yet
public record Q6KBlockReader(MemorySegment memorySegment) {
    public final static int FLOATS_PER_BLOCK = 256;
    public final static int BYTES_PER_BLOCK = 210;

    //debug    final static VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

    public float dot(
            final int blockOffset, final int blockCount,
            final FloatArray floatArray, final int floatArrayOffset) {
        var acc = FloatVector.zero(SPECIES);
        int boff = blockOffset;
        int k = floatArrayOffset;

        for(int bi=0; bi<blockCount; ++bi, boff+= BYTES_PER_BLOCK) {
            var partialSum = FloatVector.zero(SPECIES);
            var scale16 = subScaleBytes(boff);
            int scaleLaneOffset = 0;
            for (int i = 0; i < 2; i++) {
                var lsfBitChunks = lsfBitChunks(boff, i);
                var msfBitChunks = msfBitChunks(boff, i);
                ByteVector q;

                q = q0(msfBitChunks, lsfBitChunks);
                for(int j = 0; j<2; ++j) {
                    partialSum = floatArray.floatVector(SPECIES, k).mul(scale16.lane(scaleLaneOffset++)).fma(q.castShape(SPECIES, j), partialSum); k+=SPECIES.length();
                }

                q = q1(msfBitChunks, lsfBitChunks);
                for(int j = 0; j<2; ++j) {
                    partialSum = floatArray.floatVector(SPECIES, k).mul(scale16.lane(scaleLaneOffset++)).fma(q.castShape(SPECIES, j), partialSum); k+=SPECIES.length();
                }

                q = q2(msfBitChunks, lsfBitChunks);
                for(int j = 0; j<2; ++j) {
                    partialSum = floatArray.floatVector(SPECIES, k).mul(scale16.lane(scaleLaneOffset++)).fma(q.castShape(SPECIES, j), partialSum); k+=SPECIES.length();
                }

                q = q3(msfBitChunks, lsfBitChunks);
                for(int j = 0; j<2; ++j) {
                    partialSum = floatArray.floatVector(SPECIES, k).mul(scale16.lane(scaleLaneOffset++)).fma(q.castShape(SPECIES, j), partialSum); k+=SPECIES.length();
                }
            }
            acc = acc.add(partialSum.mul(mainScale(boff)));
        }
        return acc.reduceLanes(VectorOperators.ADD);
     }

    // -- HELPER

    /// 'part' half of 256 x 4 bit chunks (512 bit = 64 byte)
    private ByteVector lsfBitChunks(final long blockOffset, final int part) {
        return ByteVector.fromMemorySegment(
            ByteVector.SPECIES_512, memorySegment, blockOffset + part * 64, ByteOrder.LITTLE_ENDIAN);
    }
    /// 'part' half of 256 x 2 bit chunks (256 bit = 32 byte)
    private ByteVector msfBitChunks(final long blockOffset, final int part) {
        return ByteVector.fromMemorySegment(
            ByteVector.SPECIES_256, memorySegment, blockOffset + 128 + part * 32, ByteOrder.LITTLE_ENDIAN);
    }
    /// 16 signed scales, quantized with 8 bits
    private ByteVector subScaleBytes(final long blockOffset) {
        return ByteVector.fromMemorySegment(
            ByteVector.SPECIES_128, memorySegment, blockOffset + 128 + 64, ByteOrder.LITTLE_ENDIAN);
    }
    private float mainScale(final long blockOffset) {
        return Float.float16ToFloat(memorySegment.get(ValueLayout.JAVA_SHORT, blockOffset + 128 + 64 + 16));
    }

    /**
     * @param msf ByteVector.SPECIES_256
     * @param lsf ByteVector.SPECIES_512
     */
    private ByteVector q0(final ByteVector msf, final ByteVector lsf) {
        return lsf
            .castShape(ByteVector.SPECIES_256, 0)
            .reinterpretAsBytes()
            .and((byte) 0x0f)
            .or(msf
                .lanewise(VectorOperators.LSHL, 4)
                .and((byte) 0b110000))
            .sub((byte) 32);
    }
    /**
     * @param msf ByteVector.SPECIES_256
     * @param lsf ByteVector.SPECIES_512
     */
    private ByteVector q1(final ByteVector msf, final ByteVector lsf) {
        return lsf
            .castShape(ByteVector.SPECIES_256, 1)
            .reinterpretAsBytes()
            .and((byte) 0x0f)
            .or(msf
                .lanewise(VectorOperators.LSHL, 2)
                .and((byte) 0b110000))
            .sub((byte) 32);
    }
    /**
     * @param msf ByteVector.SPECIES_256
     * @param lsf ByteVector.SPECIES_512
     */
    private ByteVector q2(final ByteVector msf, final ByteVector lsf) {
        return lsf
            .castShape(ByteVector.SPECIES_256, 0)
            .reinterpretAsBytes()
            .lanewise(VectorOperators.LSHR, 4)
            .and((byte) 0x0f)
            .or(msf
                .and((byte) 0b110000))
            .sub((byte) 32);
    }
    /**
     * @param msf ByteVector.SPECIES_256
     * @param lsf ByteVector.SPECIES_512
     */
    private ByteVector q3(final ByteVector msf, final ByteVector lsf) {
        return lsf
            .castShape(ByteVector.SPECIES_256, 1)
            .reinterpretAsBytes()
            .lanewise(VectorOperators.LSHR, 4)
            .and((byte) 0x0f)
            .or(msf
                .lanewise(VectorOperators.LSHR, 2)
                .and((byte) 0b110000))
            .sub((byte) 32);
    }

}