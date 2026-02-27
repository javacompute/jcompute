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

import java.util.function.Consumer;

import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

public interface DragAndScrollHandler<T> {

    T onStartDragging(MouseEvent e, Consumer<Cursor> cursorCallback);
    void onDragging(T state, MouseEvent e, Point2D offset);
    void onDragged(T state, MouseEvent e, Point2D offset);
    void onScrolled(int direction, Point2D pivotPoint);

    default void bind(final EventTarget node) {
        new MouseHandler().bind(node, this);
    }

}
