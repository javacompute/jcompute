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
package jcompute.opencl.spi;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import jcompute.opencl.ClBinding;

@UtilityClass
public class OpenCLBindings {

    @Getter(lazy = true)
    private final List<ClBinding> bindings = bindings();

    @Getter(lazy = true)
    private final ClBinding defaultBinding = defaultBinding();

    // -- HELPER

    private static List<ClBinding> bindings() {
        return allProviders()
                .map(OpenCLBindingProvider::getBinding)
                .toList();
    }

    private static Stream<OpenCLBindingProvider> allProviders() {
        final ServiceLoader<OpenCLBindingProvider> loader =
                ServiceLoader.load(OpenCLBindingProvider.class);
        return loader.stream()
                .map(Provider::get);
    }

    private static ClBinding defaultBinding() {
        var availableBindings = getBindings();
        if(availableBindings.isEmpty())
			throw new IllegalStateException("Service Loader failed to find a OpenCLBindingProvider.");
        if(availableBindings.size()>1) {
            System.out.println("jcompute.opencl.spi.OpenCLBindings: Mulitple OpenCL bindings found, using first in list %s.".formatted(availableBindings));
        } else {
        	System.out.println("jcompute.opencl.spi.OpenCLBindings: using OpenCL binding %s.".formatted(availableBindings.get(0)));
        }
        return availableBindings.getFirst();
    }

}
