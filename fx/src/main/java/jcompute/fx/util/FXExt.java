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

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;

import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

    public Box addBoxNonCentered(final Group group, final double sx, final double sy, final double sz) {
        var box = new Box(sx, sy, sz);
        var innerGroup = addNode(group, new Group(box));
        innerGroup.translateXProperty().bind(box.widthProperty().multiply(0.5));
        innerGroup.translateYProperty().bind(box.heightProperty().multiply(0.5));
        innerGroup.translateZProperty().bind(box.depthProperty().multiply(0.5));
        innerGroup.setUserData("non-centered");
        return box;
    }

    public Circle addCircle(final Group group, final Point2D placement, final double radius) {
        return addNode(group, new Circle(placement.getX(), placement.getY(), radius));
    }

    public Cylinder addCylinder(final Group group, final double radius, final double height) {
        return addNode(group, new Cylinder(radius, height));
    }

    public Line addLine(final Group group, final Point2D lineStart, final Point2D lineEnd) {
        return addNode(group, new Line(lineStart.getX(), lineStart.getY(), lineEnd.getX(), lineEnd.getY()));
    }

    public Sphere addSphere(final Group group, final double radius) {
        return addNode(group, new Sphere(radius));
    }

    public PointLight addPointLight(final Group group) {
        return addNode(group, new PointLight());
    }

    public Polygon addPolygon(final Group group, final Point2D ...p) {
        var coors = Stream.of(p)
            .flatMapToDouble(it->DoubleStream.of(it.getX(), it.getY()))
            .toArray();
        return addNode(group, new Polygon(coors));
    }

    public MeshView addMeshView(final Group group, final Mesh mesh) {
        return addNode(group, new MeshView(mesh));
    }

    public Text addText(final Group group, final Font font, final Point2D placement, final String text) {
        var node = new Text(placement.getX(), placement.getY(), text);
        node.setFont(font);
        node.setStrokeWidth(0.5);
        // Calculate center position
        var bounds = node.getLayoutBounds();
        node.setTranslateX(bounds.getWidth() * -.5);
        node.setTextOrigin(VPos.CENTER);
        return addNode(group, node);
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

    public Point3D asPoint3D(final Point2D point, final double z) {
        return new Point3D(point.getX(), point.getY(), z);
    }

    public Point2D asPoint2D(final Point3D point) {
        return new Point2D(point.getX(), point.getY());
    }

    public Translate asTranslate(final Point3D point) {
        return new Translate(point.getX(), point.getY(), point.getZ());
    }

    public Point2D asRotated90DegreeCounterClockwise(final Point2D p) {
        return new Point2D(-p.getY(), p.getX());
    }

    public Point2D asRotated90DegreeClockwise(final Point2D p) {
        return new Point2D(p.getY(), -p.getX());
    }

}
