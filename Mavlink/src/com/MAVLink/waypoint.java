package com.MAVLink;

import java.util.ArrayList;

import com.MAVLink.Messages.enums.MAV_CMD;
import com.google.android.gms.maps.model.LatLng;

public class waypoint {
	public LatLng coord;
	public Double Height;
	public int cmd;

	public waypoint(LatLng c, Double h, int cmd) {
		coord = c;
		Height = h;
		this.cmd = cmd;
	}

	public waypoint(Double Lat, Double Lng, Double h, int cmd) {
		coord = new LatLng(Lat, Lng);
		Height = h;
		this.cmd = cmd;
	}
	
	
	private static ArrayList<String> commandList = null;
	public static ArrayList<String> getCommandList() {
		if (commandList == null)
		{
			commandList = new ArrayList<String>();
			commandList.add("WAYPOINT");
			commandList.add("LOITER_TURNS");
			commandList.add("LOITER_TIME");
			commandList.add("LOITER_UNLIM");
			commandList.add("RETURN_TO_LAUNCH");
			commandList.add("LAND");
			commandList.add("TAKEOFF");
			commandList.add("ROI");
			commandList.add("DO_SET_ROI");
			commandList.add("CONDITION_DELAY");
			commandList.add("CONDITION_CHANGE_ALT");
			commandList.add("CONDITION_DISTANCE");
			commandList.add("CONDITION_YAW");
			commandList.add("DO_JUMP");
			commandList.add("DO_CHANGE_SPEED");
			commandList.add("DO_SET_HOME");
			commandList.add("DO_SET_RELAY");
			commandList.add("DO_REPEAT_RELAY");
			commandList.add("DO_SET_SERVO");
			commandList.add("DO_REPEAT_SERVO");
			commandList.add("DO_DIGICAM_CONTROL");
			commandList.add("DO_MOUNT_CONTROL");
		}
		return commandList;
	}
	
	public static int parseString(String str) {
		if (str.equalsIgnoreCase("WAYPOINT"))
			return MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		else if (str.equalsIgnoreCase("LOITER_TURNS"))
			return MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
		else if (str.equalsIgnoreCase("LOITER_TIME"))
			return MAV_CMD.MAV_CMD_NAV_LOITER_TIME;
		else if (str.equalsIgnoreCase("LOITER_UNLIM"))
			return MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM;		
		else if (str.equalsIgnoreCase("RETURN_TO_LAUNCH"))
			return MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;		
		else if (str.equalsIgnoreCase("LAND"))
			return MAV_CMD.MAV_CMD_NAV_LAND;
		else if (str.equalsIgnoreCase("TAKEOFF"))
			return MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		else if (str.equalsIgnoreCase("ROI"))
			return MAV_CMD.MAV_CMD_NAV_ROI;		
		else if (str.equalsIgnoreCase("DO_SET_ROI"))
			return MAV_CMD.MAV_CMD_DO_SET_ROI;		
		else if (str.equalsIgnoreCase("CONDITION_DELAY"))
			return MAV_CMD.MAV_CMD_CONDITION_DELAY;
		else if (str.equalsIgnoreCase("CONDITION_CHANGE_ALT"))
			return MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT;
		else if (str.equalsIgnoreCase("CONDITION_DISTANCE"))
			return MAV_CMD.MAV_CMD_CONDITION_DISTANCE;
		else if (str.equalsIgnoreCase("CONDITION_YAW"))
			return MAV_CMD.MAV_CMD_CONDITION_YAW;		
		else if (str.equalsIgnoreCase("DO_JUMP"))
			return MAV_CMD.MAV_CMD_DO_JUMP;
		else if (str.equalsIgnoreCase("DO_CHANGE_SPEED"))
			return MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;		
		else if (str.equalsIgnoreCase("DO_SET_HOME"))
			return MAV_CMD.MAV_CMD_DO_SET_HOME;
		else if (str.equalsIgnoreCase("DO_SET_RELAY"))
			return MAV_CMD.MAV_CMD_DO_SET_RELAY;
		else if (str.equalsIgnoreCase("DO_REPEAT_RELAY"))
			return MAV_CMD.MAV_CMD_DO_REPEAT_RELAY;
		else if (str.equalsIgnoreCase("DO_SET_SERVO"))
			return MAV_CMD.MAV_CMD_DO_SET_SERVO;
		else if (str.equalsIgnoreCase("DO_REPEAT_SERVO"))
			return MAV_CMD.MAV_CMD_DO_REPEAT_SERVO;		
		else if (str.equalsIgnoreCase("DO_DIGICAM_CONTROL"))
			return MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL;
		else if (str.equalsIgnoreCase("DO_MOUNT_CONTROL"))
			return MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL;		
		return -1;
	}

    // Mancano:
    // SPLINE_WAYPOINT
    // DO_SET_CAM_TRIGG_DIST
	public static String parseCommand(int cmd) {
		switch (cmd) {
		case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
			return "WAYPOINT";
		case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
			return "LOITER_TURNS";
		case MAV_CMD.MAV_CMD_NAV_LOITER_TIME:
			return "LOITER_TIME";
		case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM:
			return "LOITER_UNLIM";
		case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
			return "RETURN_TO_LAUNCH";
		case MAV_CMD.MAV_CMD_NAV_LAND:
			return "LAND";
		case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
			return "TAKEOFF";
		case MAV_CMD.MAV_CMD_NAV_ROI:
			return "ROI";			
		case MAV_CMD.MAV_CMD_DO_SET_ROI:
			return "DO_SET_ROI";
		case MAV_CMD.MAV_CMD_CONDITION_DELAY:
			return "CONDITION_DELAY";
		case MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT:
			return "CONDITION_CHANGE_ALT";
		case MAV_CMD.MAV_CMD_CONDITION_DISTANCE:
			return "CONDITION_DISTANCE";
		case MAV_CMD.MAV_CMD_CONDITION_YAW:
			return "CONDITION_YAW";
		case MAV_CMD.MAV_CMD_DO_JUMP:
			return "DO_JUMP";
		case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED:
			return "DO_CHANGE_SPEED";
		case MAV_CMD.MAV_CMD_DO_SET_HOME:
			return "DO_SET_HOME";
		case MAV_CMD.MAV_CMD_DO_SET_RELAY:
			return "DO_SET_RELAY";
		case MAV_CMD.MAV_CMD_DO_REPEAT_RELAY:
			return "DO_REPEAT_RELAY";
		case MAV_CMD.MAV_CMD_DO_SET_SERVO:
			return "DO_SET_SERVO";
		case MAV_CMD.MAV_CMD_DO_REPEAT_SERVO:
			return "DO_REPEAT_SERVO";
		case MAV_CMD.MAV_CMD_DO_DIGICAM_CONTROL:
			return "DO_DIGICAM_CONTROL";
		case MAV_CMD.MAV_CMD_DO_MOUNT_CONTROL:
			return "DO_MOUNT_CONTROL";
		default:
			return "";
		}
	}
}