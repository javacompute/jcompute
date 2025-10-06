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
package jcompute.core.partition;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

import jcompute.core.util.function.BiLongConsumer;

@UtilityClass
public class PartitionUtils {

    /**
     * Splits given length into n fairly sized chunks,
     * then visits their start/length values.
     */
    public void visitChunks(final long length, final int n, final BiLongConsumer chunkConsumer) {
        checkArguments(length, n);
        if(length==0) return;
        if(length==1 || n==1) {
            chunkConsumer.accept(0L, length);
            return;
        }
        final long baseSize = length / n; // Base size of each chunk
        final long remainder = length % n; // Remaining length to distribute

        long len = 0L;
        long start = 0L;
        for (long i = 0; i < n; i++, start+=len) {
            len = baseSize + (i < remainder ? 1 : 0);
            chunkConsumer.accept(start, len);
        }
    }

    public record IndexPartition(long start, long length) {
    }

    /**
     * Splits given length into n fairly sized chunks,
     * then streams their start/length values.
     */
    public Stream<IndexPartition> streamChunks(final long length, final int n) {
        checkArguments(length, n);
        if(length==0) return Stream.empty();
        if(length==1 || n==1) {
            return Stream.of(new IndexPartition(0l, length));
        }
        final long baseSize = length / n; // Base size of each chunk
        final long remainder = length % n; // Remaining length to distribute

        final var start = new long[] {0L};

        return LongStream.range(0L, n)
            .mapToObj(i->{
                long len = baseSize + (i < remainder ? 1 : 0);
                long s = start[0];
                start[0]+=len;
                return new IndexPartition(s, len);
            });
    }

    // -- HELPER


    /// asserts n<=size
    private void checkArguments(final long length, final int n) {
        if(length<0) throw new IllegalArgumentException("length (%d) must be non-negative".formatted(length));
        if(n<1) throw new IllegalArgumentException("chunk count n (%d) must be greater than 0".formatted(n));
        if(n>length) throw new IllegalArgumentException("cannot produce %d chunks from length %d".formatted(n, length));
    }

}
