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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import jcompute.core.exception.JComputeException;

public interface Compressor {

    OutputStream out(OutputStream os);
    InputStream in(InputStream is);

    // -- FACTORIES

    static Compressor passThrough() {
        return PASS_THROUGH;
    }

    static final Compressor PASS_THROUGH = new Compressor() {
        @Override
        public OutputStream out(final OutputStream os) {
            return os;
        }
        @Override
        public InputStream in(final InputStream is) {
            return is;
        }
    };

    static Compressor gzip() {
        return forName(CompressorStreamFactory.GZIP);
    }

    static Compressor bzip2() {
        return forName(CompressorStreamFactory.BZIP2);
    }

    static Compressor lzma() {
        return forName(CompressorStreamFactory.LZMA);
    }

    static Compressor forName(final String compressorName) {
        return new Compressor() {
            @Override
            public OutputStream out(final OutputStream os) {
                try {
                    return new CompressorStreamFactory()
                            .createCompressorOutputStream(compressorName, os);
                } catch (CompressorException e) {
                    throw JComputeException.wrap(e);
                }
            }
            @Override
            public InputStream in(final InputStream in) {
                try {
                    return new CompressorStreamFactory()
                            .createCompressorInputStream(compressorName, in);
                } catch (CompressorException e) {
                    throw JComputeException.wrap(e);
                }
            }
        };
    }

}
