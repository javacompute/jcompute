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

/**
 * Spherical polar coordinates.
 */
public record Polar3(
        /**
         * radial distance: r ≥ 0
         */
        double r,
        /**
         * 0° ≤ θ ≤ 180°, or 0 rad ≤ θ ≤ π rad
         */
        double polarAngleRad,
        /**
         * -180° ≤ φ < 180°, or -π rad ≤ φ < π rad
         */
        double azimuthRad) {

    public final static Polar3 ORIGIN = new Polar3(0, 0, 0);

    public Polar3 {
        if(r<0) throw new IllegalArgumentException("requires r non negative");
        if(polarAngleRad<0) throw new IllegalArgumentException("requires polarAngle non negative");
        if(polarAngleRad>Math.PI) throw new IllegalArgumentException("requires polarAngle less than or equal π");

        if(azimuthRad < -Math.PI) {
            long n = Math.round(-0.5*azimuthRad/Math.PI);
            azimuthRad+= 2*n * Math.PI ;
        } else if(azimuthRad > Math.PI) {
            long n = Math.round(0.5*azimuthRad/Math.PI);
            azimuthRad-= 2*n * Math.PI ;
        }
    }

    public boolean isOrigin() {
        return r==0.;
    }

    /**
     * 0° ≤ θ ≤ 180°, or 0 rad ≤ θ ≤ π rad,
     */
    public double polarAngleDeg() {
        return Math.toDegrees(polarAngleRad);
    }

    /**
     * -180° ≤ φ < 180°, or -π rad ≤ φ < π rad
     */
    public double azimuthDeg() {
        return Math.toDegrees(azimuthRad);
    }

    public Cartesian3 toCartesian() {
        if(isOrigin()) return Cartesian3.ORIGIN;
        double r_sinTheta = r * Math.sin(polarAngleRad);
        double r_cosTheta = r * Math.cos(polarAngleRad);

        return new Cartesian3(
                r_sinTheta * Math.cos(azimuthRad),
                r_sinTheta * Math.sin(azimuthRad),
                r_cosTheta);
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof Polar3 other
            ? (this.isOrigin()
                    && other.isOrigin())
              || (this.r == other.r
                && this.polarAngleRad == other.polarAngleRad
                && this.azimuthRad == other.azimuthRad)
            : false;
    }

    @Override
    public final int hashCode() {
        return this.isOrigin()
            ? 0
            : 31 * (31 * Double.hashCode(r) + Double.hashCode(polarAngleRad)) + Double.hashCode(azimuthRad);
    }

}
