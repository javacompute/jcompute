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
package jcompute.ai.io.safetens;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jcompute.core.util.primitive.IntUtils;
import jcompute.core.util.primitive.LongUtils;
import tools.jackson.databind.ObjectMapper;

public record SafeTensorsFormatHeader(
        Map<String, TensorDescriptor> tensorById) {

    /// "pattern": "([UIF])(8|16|32|64|128|256)",
    /// Type of the array. U - unsigned int, I - signed int, F - IEEE 754 floating-point. Number is the count of bits.
    public enum DataType {
        U8, U16, U32, U64, U128, U256,
        I8, I16, I32, I64, I128, I256,
        F8, F16, F32, F64, F128, F256,
        BF16;
    }

    public record TensorDescriptor(
            String name,
            DataType dtype,
            ///Size of each dimension.
            int[] shape,
            ///maximum: 281474976710655
            ///A natural integer no more than 48 bits (current CPU limitation, not all 64 bits are used)
            long[] data_offsets) {

        public TensorDescriptor {
            Objects.requireNonNull(dtype);
            Objects.requireNonNull(shape);
            Objects.requireNonNull(data_offsets);
            if(data_offsets.length!=2) throw new IllegalArgumentException("invlaid data_offsets");
            Math.toIntExact(data_offsets[1] - data_offsets[0]); // fail fast if overflows
        }

        public final int totalBytes() {
            return Math.toIntExact(data_offsets[1] - data_offsets[0]);
        }

        @Override
        public final String toString() {
            return "%s shape[%s] data_offsets[%s]".formatted(dtype.name(),
                    IntUtils.toString(shape),
                    LongUtils.toString(data_offsets));
        }
    }

    public static SafeTensorsFormatHeader parse(final String headerJson) {
        var mapper = new ObjectMapper();
        var collectionType = mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, TensorDescriptor.class);
        @SuppressWarnings("unchecked")
        var map = (LinkedHashMap<String, TensorDescriptor>)mapper.readValue(headerJson, collectionType);
        return new SafeTensorsFormatHeader(map);
    }

    @Override
    public final String toString() {
        return tensorById.entrySet().stream()
            .map(e->"%s: %s".formatted(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));
    }

}
