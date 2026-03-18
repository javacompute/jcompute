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
package jcompute.core.mem;

import lombok.RequiredArgsConstructor;

import jcompute.core.shape.Shape;
import jcompute.core.util.function.LongToStringFunction;

@RequiredArgsConstructor
final class ToStringHelper {

    private final Shape shape;
    private final int maxElementsToPrint = 1000;

    public String toString(final LongToStringFunction gidToStringFunction) {
        final StringBuilder sb = new StringBuilder();
        int remainigElementsToPrint = maxElementsToPrint;
        sb.append("JComputeArray[").append(shape).append(", ");

        switch (shape.dimensionCount()) {
            case 1 -> {
                sb.append("{");
                for (long i = 0L; i < shape.sizeX() && --remainigElementsToPrint >= 0; ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(gidToStringFunction.applyAsString(i));
                    if (remainigElementsToPrint == -1) {
                        sb.append(", truncated...");
                    }
                }
                return sb.append("}]").toString();
            }
            case 2 -> {
                sb.append("{");
                for (long i = 0L; i < shape.sizeX(); ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append("{");
                    for (long j = 0L; j < shape.sizeY() && --remainigElementsToPrint >= 0; ++j) {
                        if (j > 0) {
                            sb.append(", ");
                        }
                        sb.append(gidToStringFunction.applyAsString(shape.gid2d(i, j)));
                    }
                    if (remainigElementsToPrint == -1) {
                        sb.append(", truncated...");
                    }
                    sb.append("}");
                }
                return sb.append("}]").toString();
            }
            case 3 -> {
                sb.append("{");
                for (long i = 0L; i < shape.sizeX(); ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append("{");
                    for (long j = 0L; j < shape.sizeY(); ++j) {
                        if (j > 0) {
                            sb.append(", ");
                        }
                        sb.append("{");
                        for (long k = 0L; k < shape.sizeZ() && --remainigElementsToPrint >= 0; ++k) {
                            if (k > 0) {
                                sb.append(", ");
                            }
                            sb.append(gidToStringFunction.applyAsString(shape.gid3d(i, j, k)));
                        }
                        if (remainigElementsToPrint == -1) {
                            sb.append(", truncated...");
                        }
                        sb.append("}");
                    }
                }
                return sb.append("}]").toString();
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + shape.dimensionCount());
        }
    }

}
