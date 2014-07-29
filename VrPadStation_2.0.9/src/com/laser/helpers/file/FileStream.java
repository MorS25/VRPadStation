package com.laser.helpers.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileStream {
	
	public static FileOutputStream getParameterFileStream() throws FileNotFoundException 
	{
		File myDir = new File(DirectoryPath.getParametersPath());
		myDir.mkdirs();
		File file = new File(myDir, "Parameters-" + FileManager.getTimeStamp() + ".param");
		if (file.exists())
			file.delete();
		FileOutputStream outputStream = new FileOutputStream(file);
		return outputStream;
	}
	
	public static FileOutputStream getWaypointFileStream() throws FileNotFoundException 
	{
		File myDir = new File(DirectoryPath.getWaypointsPath());
		myDir.mkdirs();
		File file = new File(myDir, "waypoints-" + FileManager.getTimeStamp() + ".txt");
		if (file.exists())
			file.delete();
		FileOutputStream outputStream = new FileOutputStream(file);
		return outputStream;
	}

	public static BufferedOutputStream getTLogFileStream() throws FileNotFoundException 
	{
		File myDir = new File(DirectoryPath.getTLogPath());
		myDir.mkdirs();
		File file = new File(myDir, FileManager.getTimeStamp() + ".tlog");
		if (file.exists())
			file.delete();
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
		return outputStream;
	}


}
