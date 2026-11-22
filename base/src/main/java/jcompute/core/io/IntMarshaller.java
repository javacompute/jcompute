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
import java.nio.IntBuffer;

import jcompute.core.exception.JComputeException;
import jcompute.core.util.primitive.IntUtils;

public record IntMarshaller(
        /// size of the int array used to buffer transfers
        int bufferSize,
        int[] intArray, byte[] byteArray) {

    @FunctionalInterface
    public static interface IntBulkConsumer {
        void accept(int[] values, int offset, int length) throws Throwable;
    }

    public IntMarshaller() {
        this(64);
    }

    public IntMarshaller(final int bufferSize) {
        IntUtils.assertGreaterThanZero(bufferSize, ()->"bufferSize must be greater than zero");
        this(bufferSize, new int[bufferSize], new byte[Integer.BYTES * bufferSize]);
    }

    public void write(final int value, final OutputStream out) {
        toBytes(value, byteArray);
        try {
            out.write(byteArray, 0, Integer.BYTES);
        } catch (IOException e) {
            throw JComputeException.wrap(e);
        }
    }

    public int read(final InputStream in) {
        try {
            in.read(byteArray, 0, Integer.BYTES);
            return fromBytes(byteArray);
        } catch (IOException e) {
            throw JComputeException.wrap(e);
        }
    }

    public void transfer(final long size, final IntBulkConsumer in, final IntBulkConsumer out) {
        long remainingLongs = size;
        try {
            while(remainingLongs>0) {
                int blockSize = (int)Math.min(remainingLongs, bufferSize);
                in.accept(intArray, 0, blockSize);
                out.accept(intArray, 0, blockSize);
                remainingLongs-=blockSize;
            }
        } catch (Throwable e) {
            throw JComputeException.wrap(e);
        }
    }

    @SuppressWarnings("unused")
    public void transfer(final long size, final InputStream in, final IntBulkConsumer out) {
        transfer(size, (final int[] values, final int offset, final int length)->{
            int bytesToRead = length * Integer.BYTES;
            int bytesRead = in.read(byteArray, 0, bytesToRead);
            if(bytesRead<bytesToRead)
                throw new IOException("could not read all bytes of a single block");
            fromBytes(length, byteArray, intArray);
        }, out);
    }

    @SuppressWarnings("unused")
    public void transfer(final long size, final IntBulkConsumer in, final OutputStream out) {
        transfer(size, in, (final int[] values, final int offset, final int length)->{
            toBytes(length, values, byteArray);
            out.write(byteArray, 0, length * Integer.BYTES);
        });
    }

    public void writeBuffer(final IntBuffer buffer, final OutputStream out) {
        buffer.rewind();
        transfer(buffer.capacity(), buffer::get, out);
        try {
            out.close();
        } catch (IOException e) {
            throw JComputeException.wrap(e);
        }
    }

    public IntBuffer readBuffer(final int size, final InputStream in) {
        var buffer = IntBuffer.allocate(size);
        transfer(size, in, buffer::put);
        return buffer;
    }

    public void writeSegment(final MemorySegment in, final OutputStream out) {
        writeBuffer(in.asByteBuffer().asIntBuffer(), out);
    }

    public void readSegment(final long size, final InputStream in, final MemorySegment out) {
        transfer(size, in, out.asByteBuffer().asIntBuffer()::put);
    }

    // -- LOW LEVEL BYTE CONVERSION

    static void toBytes(final int v, final byte[] bytes) {
        bytes[0] = (byte)(v >> 24);
        bytes[1] = (byte)(v >> 16);
        bytes[2] = (byte)(v >> 8);
        bytes[3] = (byte)(v);
    }

    static void toBytes(final int limit, final int[] data, final byte[] bytes) {
        int j = 0;
        for (int i = 0; i < limit; i++) {
            final int v = data[i];
            bytes[j++] = (byte)(v >> 24);
            bytes[j++] = (byte)(v >> 16);
            bytes[j++] = (byte)(v >> 8);
            bytes[j++] = (byte)(v);
        }
    }

    static int fromBytes(final byte[] bytes) {
        int v = 0;
        v|= (bytes[0] & 0xff) << 24;
        v|= (bytes[1] & 0xff) << 16;
        v|= (bytes[2] & 0xff) << 8;
        v|= (bytes[3] & 0xff) << 0;
        return v;
    }

    static void fromBytes(final int limit, final byte[] bytes, final int[] data) {
        int j = 0;
        for (int i = 0; i < limit; i++) {
            int v = 0;
            v|= (bytes[j++] & 0xffL) << 24;
            v|= (bytes[j++] & 0xffL) << 16;
            v|= (bytes[j++] & 0xffL) << 8;
            v|= (bytes[j++] & 0xffL) << 0;
            data[i] = v;
        }
    }

}