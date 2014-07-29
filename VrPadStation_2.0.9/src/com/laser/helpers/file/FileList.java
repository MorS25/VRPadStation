package com.laser.helpers.file;

import java.io.File;
import java.io.FilenameFilter;

public class FileList {

	public static String[] getWaypointFileList() 
	{
		FilenameFilter filenameFilter = new FilenameFilter() 
		{
			public boolean accept(File dir, String filename) {
				return filename.contains(".txt");
			}
		};
		return getFileList(DirectoryPath.getWaypointsPath(), filenameFilter);
	}

	public static String[] getParametersFileList() 
	{
		FilenameFilter filenameFilter = new FilenameFilter() 
		{
			public boolean accept(File dir, String filename) {
				return filename.contains(".param");
			}
		};
		return getFileList(DirectoryPath.getParametersPath(), filenameFilter);
	}

	public static String[] getKMZFileList() 
	{
		FilenameFilter filenameFilter = new FilenameFilter() 
		{
			public boolean accept(File dir, String filename) {
				return filename.contains(".kml") || filename.contains(".kmz");
			}
		};
		return getFileList(DirectoryPath.getGCPPath(), filenameFilter);
	}

	public static String[] getFileList(String path, FilenameFilter filenameFilter) 
	{
		File mPath = new File(path);
		try {
			mPath.mkdirs();
			if (mPath.exists()) {
				return mPath.list(filenameFilter);
			}
		} catch (SecurityException ex) {}
		return new String[0];
	}

}
