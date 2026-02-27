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
package jcompute.fx.cam;

import java.util.function.Consumer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Translate;
import jcompute.core.util.coors.Cartesian3;
import jcompute.core.util.coors.Polar3;
import jcompute.fx.mouse.DragAndScrollHandler;

public record DefaultCameraControl(
        ControlableCamera controlableCamera,
        /**
         * center of orbiting rotations
         */
        Translate worldPivot,
        /**
         * camera position
         */
        Translate worldPosition,
        /**
         * camera look-at polar angle in degrees
         */
        DoubleProperty thetaDegree,
        /**
         * camera look-at azimuth angle in degrees
         */
        DoubleProperty phiDegree)
implements DragAndScrollHandler<jcompute.fx.cam.DefaultCameraControl.Snapshot> {

    public DefaultCameraControl(
            final ControlableCamera controlableCamera) {
        this(controlableCamera,
                new Translate(), new Translate(),
                new SimpleDoubleProperty(0.), new SimpleDoubleProperty(0.));
    }

    public DefaultCameraControl(
            final ControlableCamera controlableCamera,
            final Translate worldPivot,
            final Translate worldPosition,
            final DoubleProperty thetaDegree,
            final DoubleProperty phiDegree) {
        this.controlableCamera = controlableCamera;
        this.worldPivot = worldPivot;
        this.worldPosition = worldPosition;
        this.thetaDegree = thetaDegree;
        this.phiDegree = phiDegree;
        bind(controlableCamera.scene());
        controlableCamera.bindPositionTo(worldPosition);
        controlableCamera.bindRotations(thetaDegree, phiDegree);
    }

    /**
     * The camera is positioned at 'from' and oriented such that it looks at 'to'.
     *
     * <p> 'to' is also set as the pivot point for orbital camera rotation.
     */
    public DefaultCameraControl pointAt(final Cartesian3 from, final Cartesian3 to) {
        position(from);
        pointAt(to);
        pivot(to);
        return this;
    }

    /**
     * Sets a new world pivot point for orbital camera rotation.
     */
    public void pivot(final Cartesian3 worldPivot) {
        copy(worldPivot, this.worldPivot);
    }

    /**
     * Orients the camera's local rotations such that it points to the lookAt world coordinates.
     */
    public void pointAt(final Cartesian3 worldLookAt) {
        var lookAtVector = worldLookAt
                .minus(worldPositionCartesian());

        final double phi = Math.toDegrees(Math.atan2(lookAtVector.y(), lookAtVector.x())) - 90;
        final double theta = -Math.toDegrees(Math.atan2(Math.hypot(lookAtVector.x(), lookAtVector.y()), lookAtVector.z()));
        pointAtDirection(theta, phi);
    }

    public void pointAtDirection(final double thetaDegree, final double phiDegree) {
        this.thetaDegree.setValue(thetaDegree);
        this.phiDegree.setValue(phiDegree);
    }

    public void pointAtDirection(final Polar3 dirVec) {
        pointAtDirection(dirVec.polarAngleDeg(), dirVec.azimuthDeg());
    }

    public void pointAtPivot() {
        pointAt(worldPivotCartesian());
    }

    /**
     * Sets a new world position.
     */
    public void position(final Cartesian3 worldPosition) {
        copy(worldPosition, this.worldPosition);
    }

    // -- DRAG-AND-SCROLL-HANDLER

    @Override
    public Snapshot onStartDragging(final MouseEvent e, final Consumer<Cursor> cursorCallback) {
        switch (e.getButton()) {
            case PRIMARY -> {
                cursorCallback.accept(Cursor.HAND);
            }
            case SECONDARY -> {
                cursorCallback.accept(Cursor.CROSSHAIR);
            }
            case MIDDLE -> {
            }
            default -> {}
        }
        return new Snapshot(this);
    }

    @Override
    public void onDragging(final Snapshot snapshot, final MouseEvent e, final Point2D offset) {
        switch (e.getButton()) {
            case PRIMARY -> {
                // Orbits around the worldPivot keeping distance and camera angles (in the world) fixed.
                var oldOrbit = snapshot.orbitSpherical();
                var newOrbit = oldOrbit.orbit(
                        oldOrbit.polarAngleRad() - 0.01 * offset.getY(),
                        oldOrbit.azimuthRad() - 0.01 * offset.getX());
                var newPos = newOrbit.toCartesian()
                        .plus(toCartesian(worldPivot));
                position(newPos);
                pointAtPivot();
            }
            case SECONDARY -> {
                pointAtDirection(
                        snapshot.thetaDegree - 0.1 * offset.getY(),
                        snapshot.phiDegree - 0.1 * offset.getX());
            }
            case MIDDLE -> {
            }
            default -> {}
        }
    }

    @Override
    public void onDragged(final Snapshot snapshot, final MouseEvent e, final Point2D offset) {
    }

    @Override
    public void onScrolled(final int direction, final Point2D pivotPoint) {
        position(orbitVector()
                .times(1. + direction * 0.15)
                .plus(worldPivotCartesian()));
    }

    // -- HELPER

    record Snapshot(
            Polar3 orbitSpherical,
            double thetaDegree,
            double phiDegree) {
        Snapshot(final DefaultCameraControl control){
            this(control.orbitVector().toSpherical(),
                    control.thetaDegree.doubleValue(), control.phiDegree.doubleValue());
        }
    }

    private Cartesian3 worldPositionCartesian() {
        return controlableCamera.worldPosition();
    }

    private Cartesian3 worldPivotCartesian() {
        return toCartesian(worldPivot);
    }

    private Cartesian3 orbitVector() {
        return worldPositionCartesian()
                .minus(worldPivotCartesian());
    }

    private static Cartesian3 toCartesian(final Translate t) {
        return new Cartesian3(t.getX(), t.getY(), t.getZ());
    }

    private static void copy(final Cartesian3 source, final Translate dest) {
        dest.setX(source.x());
        dest.setY(source.y());
        dest.setZ(source.z());
    }

}
