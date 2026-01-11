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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.SneakyThrows;

import jcompute.core.util.primitive.IntUtils;

class IntMarshallerTest {

    @Test
    void single() {
        var byteArray = new byte[Integer.BYTES];
        for(int v : IntUtils.samples()) {
            IntMarshaller.toBytes(v, byteArray);
            assertEquals(v, IntMarshaller.fromBytes(byteArray));
        }
    }

    @SneakyThrows
    @Test
    void bulk() {
        var samples = IntUtils.samples();
        var marshaller = new IntMarshaller();

        try(var bos = new ByteArrayOutputStream(samples.length * Integer.BYTES)) {

            marshaller.writeBuffer(IntBuffer.wrap(samples), bos);

            bos.flush();
            var marshalledBytes = bos.toByteArray();

            try(var bis = new ByteArrayInputStream(marshalledBytes)) {
                var recoveredAray = marshaller.readBuffer(samples.length, bis)
                        .array();

                Assertions.assertArrayEquals(samples, recoveredAray);

            }
        }
    }

}
