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
package jcompute.opencl.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import jcompute.core.mem.ByteArray;
import jcompute.core.mem.IntArray;
import jcompute.core.mem.LongArray;
import jcompute.core.shape.Shape;
import jcompute.opencl.ffm._CL.AddressArray;
import jcompute.opencl.ffm._CL.UInt;
import jcompute.opencl.ffm._CL.ULong;

record FFMHelper(Arena arena) implements AutoCloseable {

	public FFMHelper() {
		this(Arena.ofConfined());
	}

	@Override
	public void close() {
		arena.close();
	}

	// -- UINT

	public UInt uint() {
		return new UInt(arena);
	}
	public UInt uint(final long value) {
		return uint().set(value);
	}

	// -- ULONG

	public ULong ulong() {
		return new ULong(arena);
	}
	public ULong ulong(final long value) {
		return ulong().set(value);
	}

	// -- ADDRESS

	public AddressArray addressArray(final long length) {
		return new AddressArray(arena, length);
	}

	// -- BYTE ARRAY

	public ByteArray byteArray(final long length) {
		return ByteArray.of(arena, Shape.of(length));
	}

	// -- INT ARRAY

	public IntArray intArray(final long length) {
		return IntArray.of(arena, Shape.of(length));
	}

	// -- LONG ARRAY

	public LongArray longArray(final long length) {
		return LongArray.of(arena, Shape.of(length));
	}

    // -- DYNAMIC READ

	public byte[] readBytes(final DynamicRead function) {
        final var sizePointer = ulong();
		function.accept(0, MemorySegment.NULL, sizePointer.memorySegment());
		long size = sizePointer.longValue();
		final var data = byteArray(size);
		function.accept(size, data.memorySegment(), MemorySegment.NULL);
		return data.toArray();
	}
    public String readString(final DynamicRead function) {
        var bytes = readBytes(function);
        return new String(bytes, 0, bytes.length - 1);
    }
    public int[] readInts(final DynamicRead function) {
    	final var sizePointer = ulong();
		function.accept(0, MemorySegment.NULL, sizePointer.memorySegment());
		long size = sizePointer.longValue();
		final var data = intArray(size);
		function.accept(size, data.memorySegment(), MemorySegment.NULL);
		return data.toArray();
    }
    public long[] readLongs(final DynamicRead function) {
    	final var sizePointer = ulong();
		function.accept(0, MemorySegment.NULL, sizePointer.memorySegment());
		long size = sizePointer.longValue();
		final var data = longArray(size);
		function.accept(size, data.memorySegment(), MemorySegment.NULL);
		return data.toArray();
    }

    static interface DynamicRead {
        void accept(long param_value_size, MemorySegment param_value, MemorySegment param_value_size_ret);
    }

}
