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
package jcompute.opencl.ffm;

import java.io.File;
import java.util.Optional;

record LibraryLocator() {

	public Optional<File> locateOpenCL() {
	    return lookup("/opt/rocm/lib/opencl/libamdocl64.so")
	            .or(()->lookup("/usr/lib/x86_64-linux-gnu/libOpenCL.so.1"));
	}

	private Optional<File> lookup(final String fileName) {
        var file = new File(fileName);
        return file.exists()
            ? Optional.of(file)
            : Optional.empty();
    }

}
