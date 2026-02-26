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
package jcompute.core.util.coors;

import java.util.stream.DoubleStream;

/**
 * Cartesian 3 dimensional coordinates.
 */
public record Cartesian3(double x, double y, double z) {

    public final static Cartesian3 ORIGIN = new Cartesian3(0, 0, 0);

    public double norm() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public DoubleStream stream() {
        return DoubleStream.of(x, y, z);
    }

    public Cartesian3 times(final double factor) {
        return new Cartesian3(this.x * factor, this.y * factor, this.z * factor);
    }

    public Cartesian3 plus(final Cartesian3 operand) {
        return new Cartesian3(this.x + operand.x, this.y + operand.y, this.z + operand.z);
    }

    public Cartesian3 minus(final Cartesian3 operand) {
        return new Cartesian3(this.x - operand.x, this.y - operand.y, this.z - operand.z);
    }

    public Polar3 toSpherical() {
        if(this.equals(ORIGIN)) return Polar3.ORIGIN;

        double r = norm();
        double cosTheta = z / r;
        double theta = Math.acos(cosTheta); // in [0, pi]
        // atan2 returns value in (-pi, pi]
        double phi = Math.atan2(y, x);
        return new Polar3(r, theta, phi);
    }

}
