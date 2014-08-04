package com.laser.ui.dialogs;

import com.laser.MAVLink.Drone;
import com.laser.waypoints.MissionReader;


public abstract class OpenMissionDialog extends OpenFileDialog {
	
	public abstract void waypointFileLoaded();
	
	private Drone drone;
	
	public OpenMissionDialog(Drone drone) {
		super();				
		this.drone = drone;
	}

	@Override
	protected FileReader createReader() {
		return new MissionReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) 
	{
		drone.home = ((MissionReader)reader).getHome();
		drone.waypoints = ((MissionReader)reader).getWaypoints();
		waypointFileLoaded();				
	}
}
