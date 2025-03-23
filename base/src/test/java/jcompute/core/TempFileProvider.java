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
package jcompute.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.SneakyThrows;

import jcompute.core.util.function.throwing.ThrowingConsumer;
import jcompute.core.util.function.throwing.ThrowingFunction;

public class TempFileProvider implements AutoCloseable, Supplier<File> {

    private File tempFile;

    @SneakyThrows
    public TempFileProvider(final Class<?> caller) {
        this.tempFile = File.createTempFile(caller.getName(), UUID.randomUUID().toString());
    }

    @Override
    public File get() {
        return tempFile;
    }

    @SneakyThrows
    public void write(final ThrowingConsumer<OutputStream> outputStreamConsumer) {
        try(final OutputStream fos = new FileOutputStream(tempFile)) {
            outputStreamConsumer.accept(fos);
        }
    }

    @SneakyThrows
    public <R> R read(final ThrowingFunction<InputStream, R> inputStreamReader) {
        try(final InputStream fis = new FileInputStream(tempFile)) {
            return inputStreamReader.apply(fis);
        }
    }

    @Override
    public void close() {
        tempFile.delete();
    }

}
