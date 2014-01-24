package com.buzzinate.dm.fs;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

/****************************************************************
 * Implement the FileSystem API for the raw local filesystem.
 *
 *****************************************************************/
public class CLocalFileSystem extends LocalFileSystem {

	@Override
	public boolean mkdirs(Path f, FsPermission permission) throws IOException {
		return mkdirs0(f);
	}

	@Override
	public void setPermission(Path p, FsPermission permission)
			throws IOException {
		// NO OP
	}
	
	@Override
	public Path getHomeDirectory() {
	    return new Path("/tmp/lezhi/");
	}

	private boolean mkdirs0(Path f) {
		Path parent = f.getParent();
	    File p2f = pathToFile(f);
	    return (parent == null || mkdirs0(parent)) &&
	      (p2f.mkdir() || p2f.isDirectory());
	}
}