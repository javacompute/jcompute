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
public record Cartesian2(double x, double y) {

    public final static Cartesian2 ORIGIN = new Cartesian2(0, 0);

    public double norm() {
        return Math.sqrt(x*x + y*y);
    }

    public DoubleStream stream() {
        return DoubleStream.of(x, y);
    }

    public Cartesian2 times(final double factor) {
        return new Cartesian2(this.x * factor, this.y * factor);
    }

    public Cartesian2 plus(final Cartesian2 operand) {
        return new Cartesian2(this.x + operand.x, this.y + operand.y);
    }

    public Cartesian2 minus(final Cartesian2 operand) {
        return new Cartesian2(this.x - operand.x, this.y - operand.y);
    }

    public Polar2 toPolar() {
        if(this.equals(ORIGIN)) 
        	return Polar2.ORIGIN;
        double r = norm();
        // atan2 returns value in (-pi, pi]
        double phi = Math.atan2(y, x);
        return new Polar2(r, phi);
    }

}
