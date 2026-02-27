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
package jcompute.fx.util;

import lombok.experimental.UtilityClass;

import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import jcompute.fx.cam.ControlableCamera;
import jcompute.fx.cam.DefaultCameraControl;

@UtilityClass
public class FXExt {

    // -- NODE EXT

    public <T extends Node> T clearTransforms(final T node) {
        node.getTransforms().clear();
        return node;
    }
    public <T extends Node> T addTransforms(final T node, final Transform ...elements) {
        node.getTransforms().addAll(elements);
        return node;
    }

    public <T extends Node> T translate(final T node, final double tx, final double ty, final double tz) {
        node.setTranslateX(tx);
        node.setTranslateY(ty);
        node.setTranslateZ(tz);
        return node;
    }
    public <T extends Node> T translate(final T node, final Point3D direction) {
        return translate(node, direction.getX(), direction.getY(), direction.getZ());
    }
    public <T extends Node> T tx(final T node, final double tx) {
        node.setTranslateX(tx);
        return node;
    }
    public <T extends Node> T ty(final T node, final double ty) {
        node.setTranslateY(ty);
        return node;
    }
    public <T extends Node> T tz(final T node, final double tz) {
        node.setTranslateZ(tz);
        return node;
    }
    public <T extends Node> T tx(final T node, final ObservableDoubleValue tx) {
        node.translateXProperty().bind(tx);
        return node;
    }
    public <T extends Node> T ty(final T node, final ObservableDoubleValue ty) {
        node.translateYProperty().bind(ty);
        return node;
    }
    public <T extends Node> T tz(final T node, final ObservableDoubleValue tz) {
        node.translateZProperty().bind(tz);
        return node;
    }
    public <T extends Node> T rotx(final T node, final double degrees) {
        node.setRotationAxis(Rotate.X_AXIS);
        node.setRotate(degrees);
        return node;
    }
    public <T extends Node> T roty(final T node, final double degrees) {
        node.setRotationAxis(Rotate.Y_AXIS);
        node.setRotate(degrees);
        return node;
    }
    public <T extends Node> T rotz(final T node, final double degrees) {
        node.setRotationAxis(Rotate.Z_AXIS);
        node.setRotate(degrees);
        return node;
    }

    // -- SHAPE EXT

    public <T extends Shape3D> T material(final T node, final Material material) {
        node.setMaterial(material);
        return node;
    }

    public <T extends Shape3D> T phong(final T node, final Color diffuseColor) {
        return material(node, new PhongMaterial(diffuseColor));
    }

    public <T extends Shape3D> T phong(final T node, final Color diffuseColor, final Color specularColor) {
        var material = new PhongMaterial(diffuseColor);
        material.setSpecularColor(specularColor);
        return material(node, material);
    }

    // -- GROUP EXT

    public <T extends Node> T addNode(final Group group, final T node) {
        group.getChildren().add(node);
        return node;
    }

    public Box addBox(final Group group, final double sx, final double sy, final double sz) {
        return addNode(group, new Box(sx, sy, sz));
    }

    public Cylinder addCylinder(final Group group, final double radius, final double height) {
        return addNode(group, new Cylinder(radius, height));
    }

    public Sphere addSphere(final Group group, final double radius) {
        return addNode(group, new Sphere(radius));
    }

    public PointLight addPointLight(final Group group) {
        return addNode(group, new PointLight());
    }

    // -- SCENE EXT

    public ControlableCamera addCamera(final Scene scene, final double farClip) {
        var cam = new ControlableCamera(scene);
        cam.camera().setFarClip(farClip);
        return cam;
    }

    public DefaultCameraControl addDefaultControl(final ControlableCamera camera) {
        return new DefaultCameraControl(camera);
    }

    // -- POINT EXT

    public Translate asTranslate(final Point3D point) {
        return new Translate(point.getX(), point.getY(), point.getZ());
    }

}
