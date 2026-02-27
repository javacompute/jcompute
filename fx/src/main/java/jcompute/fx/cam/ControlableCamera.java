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

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import jcompute.core.util.coors.Cartesian3;

public record ControlableCamera(
        Scene scene,
        PerspectiveCamera camera) {

    /**
     * Uses fixedEyeAtCameraZero for intuitive transformations.
     */
    public ControlableCamera(final Scene scene) {
        this(scene, new PerspectiveCamera(true));
    }

    public ControlableCamera {
        scene.setCamera(camera);
//      new Group(camera);
//              // compute camera translation so that projected origin lands at (w/2,h/2).
//              .tx(scene.widthProperty().multiply(-0.5))
//              .ty(scene.heightProperty().multiply(0.5));
    }

    /**
     * Move camera in world coordinates.
     */
    public ControlableCamera bindPositionTo(final Translate worldPosition) {
        camera.translateXProperty().bind(worldPosition.xProperty());
        camera.translateYProperty().bind(worldPosition.yProperty());
        camera.translateZProperty().bind(worldPosition.zProperty());
        return this;
    }

//    /**
//     * Move camera in world coordinates.
//     */
//    public void position(final Cartesian3 pos) {
//        camera.setTranslateX(pos.x());
//        camera.setTranslateX(pos.y());
//        camera.setTranslateX(pos.z());
//    }

    /**
     * Rotate camera fixed in world coordinates.
     */
    public ControlableCamera bindRotations(
            final ObservableDoubleValue thetaDegree,
            final ObservableDoubleValue phiDegree) {
        var rotX = new Rotate(0., Rotate.X_AXIS);
        var rotZ = new Rotate(0., Rotate.Z_AXIS);
        rotX.angleProperty().bind(thetaDegree);
        rotZ.angleProperty().bind(phiDegree);
        camera.getTransforms().clear();
        camera.getTransforms().addAll(rotZ, rotX);
        return this;
    }

    /**
     * Rotate camera fixed in world coordinates.
     */
    public ControlableCamera bindRotations(
            final ObservableDoubleValue thetaDegree,
            final ObservableDoubleValue phiDegree,
            final ObservableDoubleValue rollDegree) {
        var rotX = new Rotate(0., Rotate.X_AXIS);
        //var rotY = new Rotate(0., Rotate.Y_AXIS);
        var rotZ = new Rotate(0., Rotate.Z_AXIS);
        rotX.angleProperty().bind(thetaDegree);
        //rotY.angleProperty().bind(yawDegree);
        rotZ.angleProperty().bind(phiDegree);
        camera.getTransforms().clear();
        camera.getTransforms().addAll(rotZ, rotX);
        return this;
    }

    public Cartesian3 worldPosition() {
        return new Cartesian3(
            camera().getTranslateX(),
            camera().getTranslateY(),
            camera().getTranslateZ());
    }

}
