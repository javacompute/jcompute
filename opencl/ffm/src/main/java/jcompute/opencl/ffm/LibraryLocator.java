package jcompute.opencl.ffm;

import java.io.File;
import java.util.Optional;

public record LibraryLocator() {

	public Optional<File> locateOpenCL() {
		var file = new File("/usr/lib/x86_64-linux-gnu/libOpenCL.so.1");
		return file.exists()
			? Optional.of(file)
			: Optional.empty();
	}

}
