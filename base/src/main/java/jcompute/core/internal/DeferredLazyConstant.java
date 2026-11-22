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

/**
 * As of JEP 401, {@code this} cannot escape a record constructor context unless the record is fully initialized.
 *
 * <p>This utility helps to create a LazyConstant that binds an argument for the computing function later (deferred).
 */
public final class DeferredLazyConstant<T, R> {

    private final Function<T, R> computingFunction;
    private final AtomicReference<T> intanceRef;

    public DeferredLazyConstant(final Function<T, R> computingFunction) {
        this.intanceRef = new AtomicReference<>();
        this.computingFunction = Objects.requireNonNull(computingFunction);
    }

    @SuppressWarnings("preview")
    public LazyConstant<R> constant() {
        return LazyConstant.of(()->computingFunction.apply(Objects.requireNonNull(intanceRef.get())));
    }

    public void bind(final T instance) {
        intanceRef.set(instance);
    }
}

