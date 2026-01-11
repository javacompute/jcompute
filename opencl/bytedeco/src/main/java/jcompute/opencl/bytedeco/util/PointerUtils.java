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
package jcompute.opencl.bytedeco.util;

import java.lang.foreign.MemorySegment;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.ShortPointer;

import lombok.experimental.UtilityClass;

import jcompute.core.io.LongMarshaller;
import jcompute.core.mem.ByteArray;
import jcompute.core.mem.DoubleArray;
import jcompute.core.mem.LongArray;
import jcompute.core.mem.ShortArray;

@UtilityClass
public class PointerUtils {

    // -- BYTEDECO

    public BytePointer pointer(final ByteArray array) {
        return new BytePointer(array.memorySegment().asByteBuffer());
    }

    public ShortPointer pointer(final ShortArray array) {
        return new ShortPointer(array.memorySegment().asByteBuffer().asShortBuffer());
    }

    public LongPointer pointer(final LongArray array) {
        return new LongPointer(array.memorySegment().asByteBuffer().asLongBuffer());
    }

    public DoublePointer pointer(final DoubleArray array) {
        return new DoublePointer(array.memorySegment().asByteBuffer().asDoubleBuffer());
    }

    // -- IN

    public BytePointer copy(final ByteArray array, final BytePointer pointer) {
        pointer.position(0);
        for (long gid = 0; gid < array.shape().totalSize(); gid++) {
            pointer.put(gid, array.get(gid));
        }
        return pointer;
    }

    public LongPointer copy(final LongArray array, final LongPointer pointer) {
        pointer.position(0);
        for (long gid = 0; gid < array.shape().totalSize(); gid++) {
            pointer.put(gid, array.get(gid));
        }
        return pointer;
    }

    public DoublePointer copy(final DoubleArray array, final DoublePointer pointer) {
        pointer.position(0);
        for (long gid = 0; gid < array.shape().totalSize(); gid++) {
            pointer.put(gid, array.get(gid));
        }
        return pointer;
    }

    // -- OUT

    public ByteArray copy(final BytePointer pointer, final ByteArray array) {
        pointer.position(0);
        for (long gid = 0; gid < array.shape().totalSize(); gid++) {
            array.put(gid, pointer.get(gid));
        }
        return array;
    }

    @SuppressWarnings("unused")
    public LongArray copy(final LongPointer pointer, final LongArray array) {
        var size = array.shape().totalSize();
        var to = array.memorySegment();

        var gid = new long[] {0L};
        var marshaller = new LongMarshaller((int)Math.min(1024<<2, size));
        var block = MemorySegment.ofArray(marshaller.longArray());
        marshaller.transfer(size,
                (values, offset, length)->{
                    pointer.position(gid[0]);
                    pointer.get(values, offset, length);
                },
                (values, offset, length)->{
                    MemorySegment.copy(block, 0, to, gid[0]<<3, length<<3);
                    gid[0]+=length;
                });
        pointer.position(0);
        return array;
    }

    public DoubleArray copy(final DoublePointer pointer, final DoubleArray array) {
        pointer.position(0);
        for (long gid = 0; gid < array.shape().totalSize(); gid++) {
            array.put(gid, pointer.get(gid));
        }
        return array;
    }

}
