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
package jcompute.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.MemorySegment;
import java.nio.LongBuffer;

import lombok.SneakyThrows;

import jcompute.core.util.primitive.IntUtils;

public record LongMarshaller(
        /// size of the long array used to buffer transfers
        int bufferSize,
        long[] longArray, byte[] byteArray) {

    @FunctionalInterface
    public static interface LongBulkConsumer {
        void accept(long[] values, int offset, int length) throws Throwable;
    }

    public LongMarshaller() {
        this(64);
    }

    public LongMarshaller(final int bufferSize) {
        IntUtils.assertGreaterThanZero(bufferSize, ()->"bufferSize must be greater than zero");
        this(bufferSize, new long[bufferSize], new byte[Long.BYTES * bufferSize]);
    }

    @SneakyThrows
    public void write(final long value, final OutputStream out) {
        toBytes(value, byteArray);
        out.write(byteArray, 0, Long.BYTES);
    }

    @SneakyThrows
    public long read(final InputStream in) {
        in.read(byteArray, 0, Long.BYTES);
        return fromBytes(byteArray);
    }

    @SneakyThrows
    public void transfer(final long size, final LongBulkConsumer in, final LongBulkConsumer out) {
        long remainingLongs = size;
        while(remainingLongs>0) {
            int blockSize = (int)Math.min(remainingLongs, bufferSize);
            in.accept(longArray, 0, blockSize);
            out.accept(longArray, 0, blockSize);
            remainingLongs-=blockSize;
        }
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public void transfer(final long size, final InputStream in, final LongBulkConsumer out) {
        transfer(size, (long[] values, int offset, int length)->{
            int bytesToRead = length * Long.BYTES;
            int bytesRead = in.read(byteArray, 0, bytesToRead);
            if(bytesRead<bytesToRead) {
                throw new IOException("could not read all bytes of a single block");
            }
            fromBytes(length, byteArray, longArray);
        }, out);
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public void transfer(final long size, final LongBulkConsumer in, final OutputStream out) {
        transfer(size, in, (long[] values, int offset, int length)->{
            toBytes(length, values, byteArray);
            out.write(byteArray, 0, length * Long.BYTES);
        });
    }

    @SneakyThrows
    public void writeBuffer(final LongBuffer buffer, final OutputStream out) {
        buffer.rewind();
        transfer(buffer.capacity(), buffer::get, out);
        out.close();
    }

    @SneakyThrows
    public LongBuffer readBuffer(final int size, final InputStream in) {
        var buffer = LongBuffer.allocate(size);
        transfer(size, in, buffer::put);
        return buffer;
    }

    public void writeSegment(final MemorySegment in, final OutputStream out) {
        writeBuffer(in.asByteBuffer().asLongBuffer(), out);
    }

    public void readSegment(final long size, final InputStream in, final MemorySegment out) {
        transfer(size, in, out.asByteBuffer().asLongBuffer()::put);
    }

    // -- LOW LEVEL BYTE CONVERSION

    static void toBytes(final long v, final byte[] bytes) {
        bytes[0] = (byte)(v >> 56);
        bytes[1] = (byte)(v >> 48);
        bytes[2] = (byte)(v >> 40);
        bytes[3] = (byte)(v >> 32);
        bytes[4] = (byte)(v >> 24);
        bytes[5] = (byte)(v >> 16);
        bytes[6] = (byte)(v >> 8);
        bytes[7] = (byte)(v);
    }

    static void toBytes(final int limit, final long[] data, final byte[] bytes) {
        int j = 0;
        for (int i = 0; i < limit; i++) {
            final long v = data[i];
            bytes[j++] = (byte)(v >> 56);
            bytes[j++] = (byte)(v >> 48);
            bytes[j++] = (byte)(v >> 40);
            bytes[j++] = (byte)(v >> 32);
            bytes[j++] = (byte)(v >> 24);
            bytes[j++] = (byte)(v >> 16);
            bytes[j++] = (byte)(v >> 8);
            bytes[j++] = (byte)(v);
        }
    }

    static long fromBytes(final byte[] bytes) {
        long v = 0L;
        v|= (bytes[0] & 0xffL) << 56;
        v|= (bytes[1] & 0xffL) << 48;
        v|= (bytes[2] & 0xffL) << 40;
        v|= (bytes[3] & 0xffL) << 32;
        v|= (bytes[4] & 0xffL) << 24;
        v|= (bytes[5] & 0xffL) << 16;
        v|= (bytes[6] & 0xffL) << 8;
        v|= (bytes[7] & 0xffL) << 0;
        return v;
    }

    static void fromBytes(final int limit, final byte[] bytes, final long[] data) {
        int j = 0;
        for (int i = 0; i < limit; i++) {
            long v = 0L;
            v|= (bytes[j++] & 0xffL) << 56;
            v|= (bytes[j++] & 0xffL) << 48;
            v|= (bytes[j++] & 0xffL) << 40;
            v|= (bytes[j++] & 0xffL) << 32;
            v|= (bytes[j++] & 0xffL) << 24;
            v|= (bytes[j++] & 0xffL) << 16;
            v|= (bytes[j++] & 0xffL) << 8;
            v|= (bytes[j++] & 0xffL) << 0;
            data[i] = v;
        }
    }

}