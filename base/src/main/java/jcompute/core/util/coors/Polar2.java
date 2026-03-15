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

import jcompute.core.util.primitive.FloatUtils.FloatRange;

/**
 * Spherical polar coordinates.
 */
public record Polar2(
        /**
         * radial distance: r ≥ 0
         */
        double r,
        /**
         * -180° ≤ φ < 180°, or -π rad ≤ φ < π rad
         */
        double azimuthRad) {

    public final static Polar2 ORIGIN = new Polar2(0, 0);

    public Polar2 {
        if(r<0) throw new IllegalArgumentException("requires r non negative");

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
    
    public boolean isNorth(float toleranceDegree) {
    	var range = new FloatRange(90f-toleranceDegree, 90f+toleranceDegree);
    	return range.contains((float)azimuthDeg());
    }
    public boolean isSouth(float toleranceDegree) {
    	var range = new FloatRange(-90f-toleranceDegree, -90f+toleranceDegree);
    	return range.contains((float)azimuthDeg());
    }
    public boolean isWest(float toleranceDegree) {
    	var az = (float)azimuthDeg();
    	return new FloatRange(180f-toleranceDegree, 180f+toleranceDegree).contains(az)
    			|| new FloatRange(-180f-toleranceDegree, -180f+toleranceDegree).contains(az);
    }
    public boolean isEast(float toleranceDegree) {
    	var range = new FloatRange(-toleranceDegree, toleranceDegree);
    	return range.contains((float)azimuthDeg());
    }

    /**
     * -180° ≤ φ < 180°, or -π rad ≤ φ < π rad
     */
    public double azimuthDeg() {
        return Math.toDegrees(azimuthRad);
    }

    public Polar2 orbit(final double azimuthRad) {
        return new Polar2(r, azimuthRad);
    }

    public Cartesian2 toCartesian() {
        if(isOrigin()) return Cartesian2.ORIGIN;
        return new Cartesian2(
            r * Math.cos(azimuthRad),
            r * Math.sin(azimuthRad));
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof Polar2 other
            ? (this.isOrigin()
                    && other.isOrigin())
              || (this.r == other.r
                && this.azimuthRad == other.azimuthRad)
            : false;
    }

    @Override
    public final int hashCode() {
        return this.isOrigin()
            ? 0
            : 31 * Double.hashCode(r) + Double.hashCode(azimuthRad);
    }

}
