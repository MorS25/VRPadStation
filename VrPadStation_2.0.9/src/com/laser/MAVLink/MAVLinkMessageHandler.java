package com.laser.MAVLink;


import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_attitude;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_nav_controller_output;
import com.MAVLink.Messages.ardupilotmega.msg_radio;
import com.MAVLink.Messages.ardupilotmega.msg_raw_imu;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.MAVLink.Messages.ardupilotmega.msg_sys_status;
import com.MAVLink.Messages.ardupilotmega.msg_vfr_hud;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;
import com.MAVLink.Messages.enums.MAV_STATE;
import com.google.android.gms.maps.model.LatLng;


public class MAVLinkMessageHandler {

	private Drone drone; 

	public MAVLinkMessageHandler(Drone drone) 
	{
		this.drone = drone;
	}
	
	public void receiveData(MAVLinkMessage msg) 
	{
		if (msg_radio.MAVLINK_MSG_ID_RADIO != msg.msgid) {
			if (msg.sysid != drone.getsysId() || msg.compid != drone.getcompId()) {
//				Log.d("SYS1", msg.sysid + " " + drone.getsysId() + " "  + msg.compid + " " + msg.msgid);
//				if (msg.sysid != 1)
//					Log.d("SYS2", msg.sysid + " " + drone.getsysId() + " " + msg.compid + " " + msg.msgid);
				return;
			}
		}
			
		switch (msg.msgid) 
		{
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;				
			drone.setRollPitchYaw(	m_att.roll * 180.0 / Math.PI, 
									m_att.pitch* 180.0 / Math.PI, 
									m_att.yaw * 180.0 / Math.PI);
			break;	
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			drone.setAltitudeGroundAndAirSpeeds(m_hud.alt,m_hud.groundspeed,m_hud.airspeed);	
			drone.setClimbRate(m_hud.climb);
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			drone.setWpno(((msg_mission_current) msg).seq);
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
//			drone.setId(msg.sysid, msg.compid);
			drone.setType(msg_heart.type);
			drone.setArmedAndFailsafe(
					(msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED,
					msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL);
            ApmModes newMode;
            newMode = ApmModes.getMode(msg_heart.custom_mode,drone.getType());
            drone.setMode(newMode);
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			drone.setPosition(new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7));
			drone.setGpsAltitude(((msg_global_position_int)msg).alt/1E3);
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			msg_sys_status m_sys = (msg_sys_status) msg;
			drone.setBatteryState(m_sys.voltage_battery/1000.0,m_sys.battery_remaining,m_sys.current_battery/100.0);
			drone.setSensorsHealth(m_sys.onboard_control_sensors_present, m_sys.onboard_control_sensors_enabled, m_sys.onboard_control_sensors_health);
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			// TODO implement link quality
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			drone.setGpsState(((msg_gps_raw_int) msg).fix_type,((msg_gps_raw_int) msg).satellites_visible, ((msg_gps_raw_int)msg).eph);
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			drone.setDisttowpAndSpeedAltErrors(m_nav.wp_dist,m_nav.alt_error,m_nav.aspd_error);
			break;
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			msg_rc_channels_raw m_rc = (msg_rc_channels_raw) msg;
			drone.setRcChannelsRaw(m_rc);
			break;
		case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
			msg_servo_output_raw m_servo = (msg_servo_output_raw) msg;
			drone.setRcOutputRaw(m_servo);
			break;			
		case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
			msg_raw_imu m_imu = (msg_raw_imu) msg;
			drone.m_last_imu_raw = m_imu;
			break;
		}
	}
	
}
