package com.laser.waypoints;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.MAVLink.waypoint;
import com.laser.helpers.file.FileManager;
import com.laser.helpers.file.FileStream;

public class MissionWriter {
	
	private waypoint homeWaypoint;
	private List<waypoint> waypointsList;

	public MissionWriter(waypoint home, List<waypoint> waypoints) 
	{
		this.homeWaypoint = home;
		this.waypointsList = waypoints;
	}

	public boolean saveWaypoints()
	{
		try {
			if (!FileManager.isExternalStorageAvaliable()) 
				return false;
				
			FileOutputStream out = FileStream.getWaypointFileStream();
			writeHomeLine(out);
			writeWaypointsLines(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeHomeLine(FileOutputStream out) throws IOException 
	{
		out.write(String.format(Locale.ENGLISH,
								"QGC WPL 110\n0\t1\t0\t16\t0\t0\t0\t0\t%f\t%f\t%f\t1\n",
								homeWaypoint.coord.latitude, homeWaypoint.coord.longitude, homeWaypoint.Height)
								.getBytes());
	}

	private void writeWaypointsLines(FileOutputStream out) throws IOException 
	{
		for (int i = 0; i < waypointsList.size(); i++) 
		{
			out.write(String.format(Locale.ENGLISH,
									"%d\t0\t%d\t%d\t0.000000\t0.000000\t0.000000\t0.000000\t%f\t%f\t%f\t1\n",
									i + 1,
									0, // TODO Implement Relative Altitude							
									waypointsList.get(i).cmd,
									waypointsList.get(i).coord.latitude,
									waypointsList.get(i).coord.longitude,
									waypointsList.get(i).Height).getBytes());
		}
	}
	
	
}
