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

import java.nio.channels.FileChannel;
import java.nio.file.Path;

import lombok.SneakyThrows;

import jcompute.core.io.FileChannelReader;

public record SafeTensorsReader(Path modelPath) {

    @SneakyThrows
    public void dumpInfo() {
        try (var fileChannel = FileChannel.open(modelPath)) {
            var reader = new FileChannelReader(fileChannel);
            var header = SafeTensorsFormatHeader.parse(reader.readString());
            System.out.println("%s".formatted(header));

//            long startPos = fileChannel.position(); // header byte size
//
//            for (var tensorDescriptor : header.tensorById().values()) {
//                final int totalBytes = tensorDescriptor.totalBytes();
//                final byte[] rawParams = reader.readBytes(totalBytes);
//                //TODO each tensor could be mapped to memory segment
//            }

        }

    }

}
