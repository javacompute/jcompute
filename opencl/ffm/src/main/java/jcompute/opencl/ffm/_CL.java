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
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import org.jspecify.annotations.Nullable;

import lombok.experimental.UtilityClass;

import jcompute.core.mem.ByteArray;
import jcompute.opencl.ffm.gen.opencl_h_1;

@UtilityClass
class _CL {

    // -- ERROR HANDLING

	void assertSuccess(final int ret, final Supplier<String> message) {
        if(ret!=opencl_h_1.CL_SUCCESS()) {
        	var msg = message.get();
            System.err.printf("%s%n", msg);
            throw new IllegalStateException(msg);
        }
    }

    <T> T checkedApply(final Function<UInt, T> function, final Supplier<String> message) {
        try (var arena = Arena.ofConfined()){
        	var uint = new UInt(arena);
            final T t = function.apply(uint);
            final int ret = uint.intValue();
            assertSuccess(ret, message);
            return t;
        }
    }

    <T> T checkedApply2(final BiFunction<Arena, UInt, T> function, final Supplier<String> message) {
        try (var arena = Arena.ofConfined()){
        	var uint = new UInt(arena);
            final T t = function.apply(arena, uint);
            final int ret = uint.intValue();
            assertSuccess(ret, message);
            return t;
        }
    }

    // -- ARG UTILS

    public MemorySegment stringOutArg(final Arena arena, final String value) {
    	final byte[] str = Arrays.copyOf(value.getBytes(), value.length()-1);
		return ByteArray.wrap(arena, str).memorySegment();
    }

    // -- ARG WRAPPERS

	public record UInt(MemorySegment memorySegment) {
		public UInt set(final long value) {
			memorySegment.set(JAVA_INT, 0L, (int) (value & 0xFFFF_FFFFL));
			return this;
		}
		public int intValue() {
			return memorySegment.get(JAVA_INT, 0L);
		}
		public long uintValue() {
			return Integer.toUnsignedLong(intValue());
		}
		public UInt(final Arena arena) {
			this(arena.allocate(JAVA_INT));
		}
	}

	public record ULong(MemorySegment memorySegment) {
		public ULong set(final long value) {
			memorySegment.set(JAVA_LONG, 0L, value);
			return this;
		}
		public long longValue() {
			return memorySegment.get(JAVA_LONG, 0L);
		}
		public ULong(final Arena arena) {
			this(arena.allocate(JAVA_LONG));
		}
	}

	public record StringConstant(MemorySegment memorySegment) {
		public StringConstant(final Arena arena, final String value) {
			final byte[] str = value.getBytes();
			this(arena.allocate(JAVA_BYTE, str.length + 1));
			memorySegment.copyFrom(MemorySegment.ofArray(str));
		}
		public static String readCString(final MemorySegment seg) {
			long len = 0;
			while (len < seg.byteSize() && seg.get(ValueLayout.JAVA_BYTE, len) != 0) {
				len++;
			}
			byte[] bytes = new byte[(int) len];
			MemorySegment.ofArray(bytes).copyFrom(seg);
			return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
		}
	}

	public record AddressConstant(MemorySegment memorySegment) {
	}

	public record AddressArray(MemorySegment memorySegment, long length) {
		public static AddressArray wrap(final Arena arena, final AddressConstant... array) {
			return new AddressArray(arena, array.length)
				.copyFrom(array);
		}
		public static AddressArray wrap(final Arena arena, final MemorySegment... array) {
			return new AddressArray(arena, array.length)
				.copyFrom(array);
		}
		public AddressArray(final Arena arena, final long length) {
			this(arena.allocate(MemoryLayout.sequenceLayout(length, ADDRESS)), length);
		}
		public AddressArray copyFrom(final @Nullable AddressConstant[] values) {
	    	if(values==null)
	    		return this;
	    	final int size = (int)Math.min(length, values.length);
	    	for (int i = 0; i < size; i++) {
	    		memorySegment.set(ADDRESS, i, values[i].memorySegment());
			}
	    	return this;
	    }
		public AddressArray copyFrom(final @Nullable MemorySegment[] values) {
	    	if(values==null)
	    		return this;
	    	final int size = (int)Math.min(length, values.length);
	    	for (int i = 0; i < size; i++) {
	    		memorySegment.set(ADDRESS, i, values[i]);
			}
	    	return this;
	    }
		public AddressConstant pointerAt(final long index) {
			return new AddressConstant(memorySegment.get(ADDRESS, index * ADDRESS.byteSize()));
		}
	}

}
