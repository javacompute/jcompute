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
package jcompute.core.shape;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import lombok.SneakyThrows;

import jcompute.core.io.LongMarshaller;
import jcompute.core.util.function.BiLongConsumer;
import jcompute.core.util.function.TriLongConsumer;

/**
 * Tuple of long, where the elements give the lengths of the corresponding array dimensions.
 */
public record Shape(int dimensionCount, long totalSize, long sizeX, long sizeY, long sizeZ)
implements Serializable {

    public static Shape empty() {
        return new Shape(0, 0, 0, 0, 0);
    }

    public static Shape of(final long size) {
        return new Shape(1, size, size, 0, 0);
    }

    public static Shape of(final long sizeX, final long sizeY) {
        return new Shape(2, Math.multiplyExact(sizeX, sizeY), sizeX, sizeY, 0);
    }

    public static Shape of(final long sizeX, final long sizeY, final long sizeZ) {
        return new Shape(3,
                Math.multiplyExact(Math.multiplyExact(sizeX, sizeY), sizeZ), sizeX, sizeY, sizeZ);
    }

    /**
     * Returns the global indices as {@link LongStream} ranging from zero up to ({@link #totalSize} - 1).
     * @return 0..(totalSize-1)
     */
    public LongStream stream() {
        return LongStream.range(0, totalSize());
    }

    /**
     * Visits the global indices from zero up to ({@link #totalSize} - 1).
     * @return this
     */
    public Shape forEach(final LongConsumer onIndex) {
        final long end = totalSize();
        for(long i = 0L; i<end; ++i) {
            onIndex.accept(i);
        }
        return this;
    }

    /**
     * Visits the global indices from offset up to (offset + size - 1).
     * @return this
     */
    public Shape forEach(final long offset, final long size, final LongConsumer onIndex) {
        long end = offset + size;
        for(long i = offset; i<end; ++i) {
            onIndex.accept(i);
        }
        return this;
    }

    public void forEach(final BiLongConsumer onIndex) {
        switch (dimensionCount) {
        case 1: {
            for(long i = 0L; i<sizeX; ++i) {
                onIndex.accept(i, 0L);
            }
            return;
        }
        case 2: {
            for(long i = 0L; i<sizeX; ++i) {
                for(long j = 0L; j<sizeY; ++j) {
                    onIndex.accept(i, j);
                }
            }
            return;
        }
        default:
            throw new IllegalArgumentException("Unexpected value: " + dimensionCount);
        }
    }

    /**
     * Return the global index for given 2d index (i,j).
     */
    public long gid2d(final long i, final long j) {
        return i * sizeY() + j;
    }

    public void forEach(final TriLongConsumer onIndex) {
        switch (dimensionCount) {
        case 1: {
            for(long i = 0L; i<sizeX; ++i) {
                onIndex.accept(i, 0L, 0L);
            }
            return;
        }
        case 2: {
            for(long i = 0L; i<sizeX; ++i) {
                for(long j = 0L; j<sizeY; ++j) {
                    onIndex.accept(i, j, 0L);
                }
            }
            return;
        }
        case 3: {
            for(long i = 0L; i<sizeX; ++i) {
                for(long j = 0L; j<sizeY; ++j) {
                    for(long k = 0L; k<sizeZ; ++k) {
                        onIndex.accept(i, j, k);
                    }
                }
            }
            return;
        }
        default:
            throw new IllegalArgumentException("Unexpected value: " + dimensionCount);
        }
    }

    /**
     * Return the global index for given 3d index (i,j,k).
     */
    public long gid3d(final long i, final long j, final long k) {
        return ( i * sizeY() + j ) * sizeZ() + k;
    }

    // -- IO

    @SneakyThrows
    public void write(final OutputStream out) {
        out.write(dimensionCount);
        if(dimensionCount==0) return;
        var marshaller = new LongMarshaller(1);
        marshaller.write(sizeX, out);
        if(dimensionCount>1) {
            marshaller.write(sizeY, out);
        }
        if(dimensionCount>2) {
            marshaller.write(sizeZ, out);
        }
    }

    @SneakyThrows
    public static Shape read(final InputStream in) {
        final int dimensionCount = in.read();
        if(dimensionCount==0) return empty();
        var marshaller = new LongMarshaller(1);
        if(dimensionCount==1) {
            return Shape.of(
                    marshaller.read(in));
        }
        if(dimensionCount==2) {
            return Shape.of(
                    marshaller.read(in),
                    marshaller.read(in));
        }
        if(dimensionCount==3) {
            return Shape.of(
                    marshaller.read(in),
                    marshaller.read(in),
                    marshaller.read(in));
        }
        throw new IllegalArgumentException("Unexpected value: " + dimensionCount);
    }

}

