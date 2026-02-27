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
package jcompute.fx.mouse;

import java.util.concurrent.atomic.AtomicReference;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

class MouseHandler {

    private MouseEvent drag0;

    public <T> void bind(final EventTarget node, final DragAndScrollHandler<T> handler) {

        setCursor(node, Cursor.DEFAULT);

        final var stateRef = new AtomicReference<T>();

        addEventHandler(node, MouseEvent.MOUSE_DRAGGED, e -> {
            if (drag0 == null) {
                this.drag0 = e;
                stateRef.set(handler.onStartDragging(e, cursor->setCursor(node, cursor)));
            }
            var delta = delta(e, drag0);
            if (isZero(delta, 1e-3))
                return;
            handler.onDragging(stateRef.get(), e, delta);
        });

        addEventHandler(node, MouseEvent.MOUSE_RELEASED, e -> {
            if (drag0 == null)
                return;
            setCursor(e.getSource(), Cursor.DEFAULT);

            var delta = delta(e, drag0);
            if (!isZero(delta, 1e-3)) {
                handler.onDragged(stateRef.get(), e, delta);
            }
            drag0 = null;
            stateRef.set(null);
        });

        addEventHandler(node, ScrollEvent.SCROLL, e -> {
            int direction = Double.compare(0, e.getDeltaY());
            if (direction == 0)
                return;
            handler.onScrolled(direction, new Point2D(e.getSceneX(), e.getSceneY()));
        });

    }

    // -- HELPER

    private <T extends Event> void addEventHandler(
            final EventTarget target,
            final EventType<T> eventType,
            final EventHandler<T> eventHandler) {
        if (target instanceof Node node) {
            node.addEventHandler(eventType, eventHandler);
        } else if (target instanceof Scene scene) {
            scene.addEventHandler(eventType, eventHandler);
        } else
            throw new RuntimeException("Could not add EventHandler to target: " + target);
    }

    private void setCursor(final Object target, final Cursor cursor) {
        if (target instanceof Node node) {
            node.setCursor(cursor);
        } else if (target instanceof Scene scene) {
            scene.setCursor(cursor);
        } else
            throw new RuntimeException("Could not set Cursor for target: " + target);
    }

    private static Point2D delta(final MouseEvent a, final MouseEvent b) {
        return new Point2D(a.getScreenX() - b.getScreenX(), a.getScreenY() - b.getScreenY());
    }

    private static boolean isZero(final Point2D p, final double eps) {
        return Math.abs(p.getX())<eps && Math.abs(p.getY())<eps;
    }

}
