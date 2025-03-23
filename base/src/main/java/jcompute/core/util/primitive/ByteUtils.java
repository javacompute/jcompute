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
package jcompute.core.util.primitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ByteUtils {

    @SneakyThrows
    public void writeToBuffer(final ByteBuffer buffer, final OutputStream out) {
        final int size = buffer.capacity();
        try(WritableByteChannel channel = Channels.newChannel(out)){
            buffer.rewind();
            int count = channel.write(buffer);
            if(count!=size) {
                throw new IOException(String.format("Could only write %d of %d bytes.", count, size));
            }
        }
    }

    @SneakyThrows
    public void readIntoBuffer(final long size, final InputStream in, final ByteBuffer byteBuffer) {
        try(ReadableByteChannel channel = Channels.newChannel(in)) {
            long count = 0;
            while(count<size) {
                int read = channel.read(byteBuffer);
                if(read==-1) break;
                count+= read;
            }
            if(count!=size) {
                throw new IOException(String.format("Could only read %d of %d bytes.", count, size));
            }
        }
    }

}
