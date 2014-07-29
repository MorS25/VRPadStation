package com.laser.waypoints;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.waypoint;
import com.laser.ui.dialogs.OpenFileDialog.FileReader;
import com.laser.helpers.file.DirectoryPath;
import com.laser.helpers.file.FileList;
import com.laser.helpers.file.FileManager;

public class MissionReader implements FileReader {
	
	private waypoint homeWaypoint;
	private List<waypoint> waypointsList;

	public MissionReader() {
		this.waypointsList = new ArrayList<waypoint>();
	}

	public waypoint getHome() {
		return homeWaypoint;
	}

	public List<waypoint> getWaypoints() {
		return waypointsList;
	}

	public boolean openMission(String file) 
	{
		if (!FileManager.isExternalStorageAvaliable())
			return false;
		
		try {
			FileInputStream in = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			if (!isWaypointFile(reader)) 
			{
				in.close();
				return false;
			}
			parseHomeWaypointLine(reader);
			parseWaypointLines(reader);

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void parseWaypointLines(BufferedReader reader) throws IOException 
	{
		String line;
		waypointsList.clear();
		while ((line = reader.readLine()) != null) 
		{
			String[] RowData = line.split("\t");
			waypointsList.add(new waypoint(Double.valueOf(RowData[8]), 
										   Double.valueOf(RowData[9]), 
										   Double.valueOf(RowData[10]),
										   Integer.valueOf(RowData[3])));
		}

	}

	private void parseHomeWaypointLine(BufferedReader reader) throws IOException 
	{
		String[] RowData1 = reader.readLine().split("\t");
		homeWaypoint = new waypoint(Double.valueOf(RowData1[8]),
									Double.valueOf(RowData1[9]), 
									Double.valueOf(RowData1[10]), 
									Integer.valueOf(RowData1[3]));
	}

	private static boolean isWaypointFile(BufferedReader reader) throws IOException 
	{
		return reader.readLine().contains("QGC WPL 110");
	}

	@Override
	public String getPath() 
	{
		return DirectoryPath.getWaypointsPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getWaypointFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openMission(file);
	}
}
