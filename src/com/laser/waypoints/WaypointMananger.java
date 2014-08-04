package com.laser.waypoints;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item_reached;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_mission_set_current;
import com.laser.MAVLink.Drone;
import com.laser.service.MAVLinkClient;


public class WaypointMananger {	

	public interface OnWaypointManagerListener
	{
		public abstract void onWaypointsReceived(List<waypoint> waypoints);
		public abstract void onWriteWaypoints(msg_mission_ack msg);
	}
	
	private MAVLinkClient MAV;
	private OnWaypointManagerListener listener;
	private short waypointsCount;
	private List<waypoint> waypointsList;	
	private int writeIndex;	// waypoint witch is currently being written

	enum waypointStates {IDLE, READ_REQUEST, READING_WP, WRITING_WP, WAITING_WRITE_ACK}
	private waypointStates state = waypointStates.IDLE;
	private Drone drone;
	
	public WaypointMananger(MAVLinkClient MAV, OnWaypointManagerListener listener, Drone drone) {
		this.MAV = MAV;
		this.listener = listener;
		this.drone = drone;
		waypointsList = new ArrayList<waypoint>();
	}

	public void getWaypoints() 
	{
		state = waypointStates.READ_REQUEST;
		requestWaypointsList();
	}

	/** Write waypoints to the MAV */
	public void writeWaypoints(List<waypoint> data) 
	{
		if ((waypointsList != null))
		{
			waypointsList.clear();
			waypointsList.addAll(data);
			writeIndex = 0;
			state = waypointStates.WRITING_WP;
			sendWaypointCount();
		}
	}

	/** Sets the current waypoint in the MAV */
	public void setCurrentWaypoint(int i) 
	{
		if ((waypointsList != null)) 
			sendSetCurrentWaypoint((short )i);
	}

	/* CALLBACKS */
	public void onWaypointReached(int wpNumber) {}
	private void onCurrentWaypointUpdate(short seq) {}


	public boolean processMessage(MAVLinkMessage msg) 
	{
		switch (state) 
		{
		default:
		case IDLE:
			break;
		case READ_REQUEST:
			if (msg.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT) 
			{
				requestFirstWaypoint(msg);
				state = waypointStates.READING_WP;
				return true;
			}
			break;
		case READING_WP:
			if (msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) 
			{
				processReceivedWaypoint((msg_mission_item) msg);
				if (waypointsList.size() < waypointsCount) {
					requestWaypoint();
				} else {
					state = waypointStates.IDLE;
					sendAck();
					listener.onWaypointsReceived(waypointsList);
				}
				return true;
			}
			break;
		case WRITING_WP:
			if (msg.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST) 
			{
				sendWaypoint(writeIndex++);
				if (writeIndex >= waypointsList.size()) {
					state = waypointStates.WAITING_WRITE_ACK;
				}
				return true;
			}
			break;
		case WAITING_WRITE_ACK:
			if (msg.msgid == msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK)
			{
				listener.onWriteWaypoints((msg_mission_ack) msg);
				state = waypointStates.IDLE;
				return true;
			}
			break;
		}

		if (msg.msgid == msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED) {
			onWaypointReached(((msg_mission_item_reached) msg).seq);
			return true;
		}
		if (msg.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT) {
			onCurrentWaypointUpdate(((msg_mission_current) msg).seq);
			return true;
		}
		
		return false;
	}

	private void requestFirstWaypoint(MAVLinkMessage msg) {
		waypointsCount = ((msg_mission_count) msg).count;
		waypointsList.clear();
		requestWaypoint();
	}

	private void processReceivedWaypoint(msg_mission_item msg) {
		Double Lat = (double) msg.x;
		Double Lng = (double) msg.y;
		Double h = (double) msg.z;
		int cmd = msg.command;
		waypointsList.add(new waypoint(Lat, Lng, h, cmd));
	}

	private void sendAck() {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.type = 0; // TODO use MAV_MISSION_RESULT constant
		MAV.sendMavPacket(msg.pack());
	}

	private void requestWaypointsList() {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAV.sendMavPacket(msg.pack());
	}

	private void requestWaypoint() {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.seq = (short) waypointsList.size();
		MAV.sendMavPacket(msg.pack());
	}

	private void sendWaypointCount() {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.count = (short) waypointsList.size();
		MAV.sendMavPacket(msg.pack());
	}

	private void sendWaypoint(int index) 
	{
		msg_mission_item msg = new msg_mission_item();
		msg.seq = (short) index;
		msg.current = (byte) ((index == 0) ? 1 : 0); // TODO use correct parameter for HOME
		msg.frame = 0; 
		msg.command = (short) waypointsList.get(index).cmd;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.x = (float) waypointsList.get(index).coord.latitude;
		msg.y = (float) waypointsList.get(index).coord.longitude;
		msg.z = waypointsList.get(index).Height.floatValue();
		msg.autocontinue = 1; 
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAV.sendMavPacket(msg.pack());
	}

	private void sendSetCurrentWaypoint(short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.seq = i;
		MAV.sendMavPacket(msg.pack());
	}
}
