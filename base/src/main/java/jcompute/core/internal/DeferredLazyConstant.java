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
package jcompute.core.internal;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/// As of JEP 401, {@code this} cannot escape a record constructor context
/// unless the record's components are fully initialized.
///
/// This utility helps to create a LazyConstant that binds an argument for the computing function later (deferred).
///
/// Be aware, the {@link LazyConstant} instance as returned from {@link DeferredLazyConstant#constant()}
/// will throw a {@link NullPointerException} when its {@code get()} member is called,
/// if there was no prior binding via {@link DeferredLazyConstant#bind(Object)}.
///
/// In other words: <em>don't forget to bind!</em>
///
/// Example Usage (inside a record constructor):
///  ```java
///   var deferredConst = new DeferredLazyConstant<>(SampleRecord::listChildren);
///   this(deferredConst.constant());
///   deferredConst.bind(this);
/// ```
@SuppressWarnings("preview")
public final class DeferredLazyConstant<T, R> {

    private final Function<T, R> computingFunction;
    private final AtomicReference<T> instanceRef;

    public DeferredLazyConstant(final Function<T, R> computingFunction) {
        this.instanceRef = new AtomicReference<>();
        this.computingFunction = Objects.requireNonNull(computingFunction);
    }

    /**
     * Each invocation returns a new {@link LazyConstant} instance, that will compute its constant value based on
     * this instance's {@code computingFunction} and an argument that must be bound exactly once.
     */
    public LazyConstant<R> constant() {
        return LazyConstant.of(()->computingFunction.apply(
                Objects.requireNonNull(instanceRef.get(), ()->
                        "LazyConstant computed before the argument of the computingFunction was bound. "
                        + "Perhaps you forgot to call DeferredLazyConstant#bind(T)")));
    }

    /**
     * Binds the argument. Must be called exactly once, before {@code get()} on any constant from {@link #constant()}.
     * @throws IllegalStateException if already bound
     * @throws NullPointerException if instance is {@code null}
     */
    public void bind(final T instance) {
        if (!instanceRef.compareAndSet(null, Objects.requireNonNull(instance)))
            throw new IllegalStateException("already bound");
    }
}

