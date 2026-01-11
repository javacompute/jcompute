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
import java.nio.LongBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.SneakyThrows;

import jcompute.core.util.primitive.LongUtils;

class LongMarshallerTest {

    @Test
    void single() {
        var byteArray = new byte[Long.BYTES];
        for(long v : LongUtils.samples()) {
            LongMarshaller.toBytes(v, byteArray);
            assertEquals(v, LongMarshaller.fromBytes(byteArray));
        }
    }

    @SneakyThrows
    @Test
    void bulk() {
        var samples = LongUtils.samples();
        var marshaller = new LongMarshaller();

        try(var bos = new ByteArrayOutputStream(samples.length * Long.BYTES)) {

            marshaller.writeBuffer(LongBuffer.wrap(samples), bos);
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
