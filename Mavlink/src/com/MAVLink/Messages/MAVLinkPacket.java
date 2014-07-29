package com.MAVLink.Messages;

import android.util.Log;

import java.io.Serializable;

import com.MAVLink.Messages.ardupilotmega.*;

/**
 * Common interface for all MAVLink Messages
 * Packet Anatomy
 * This is the anatomy of one packet. It is inspired by the CAN and SAE AS-4 standards. 

 * Byte Index  Content              Value       Explanation  
 * 0            Packet start sign  v1.0: 0xFE   Indicates the start of a new packet.  (v0.9: 0x55) 
 * 1            Payload length      0 - 255     Indicates length of the following payload.  
 * 2            Packet sequence     0 - 255     Each component counts up his send sequence. Allows to detect packet loss  
 * 3            System ID           1 - 255     ID of the SENDING system. Allows to differentiate different MAVs on the same network.  
 * 4            Component ID        0 - 255     ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.  
 * 5            Message ID          0 - 255     ID of the message - the id defines what the payload means and how it should be correctly decoded.  
 * 6 to (n+6)   Payload             0 - 255     Data of the message, depends on the message id.  
 * (n+7)to(n+8) Checksum (low byte, high byte)  ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).  

 * The checksum is the same as used in ITU X.25 and SAE AS-4 standards (CRC-16-CCITT), documented in SAE AS5669A. Please see the MAVLink source code for a documented C-implementation of it. LINK TO CHECKSUM
 * The minimum packet length is 8 bytes for acknowledgement packets without payload
 * The maximum packet length is 263 bytes for full payload
 * 
 * @author ghelle
 *
 */
public class MAVLinkPacket implements Serializable {
	private static final long serialVersionUID = 2095947771227815314L;
	
	public static final int MAVLINK_STX = 254;
	
	/**
	 * Message length. NOT counting STX, LENGTH, SEQ, SYSID, COMPID, MSGID, CRC1 and CRC2
	 */
	public int len;
	/**
	 * Message sequence
	 */
	public int seq;
	/**
	 * ID of the SENDING system. Allows to differentiate different MAVs on the
	 * same network.
	 */
	public int sysid;
	/**
	 * ID of the SENDING component. Allows to differentiate different components
	 * of the same system, e.g. the IMU and the autopilot.
	 */
	public int compid;
	/**
	 * ID of the message - the id defines what the payload means and how it
	 * should be correctly decoded.
	 */
	public int msgid;
	/**
	 * Data of the message, depends on the message id.
	 */
	public MAVLinkPayload payload;
	/**
	 * ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6)
	 * Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from
	 * message fields. Protects the packet from decoding a different version of
	 * the same packet but with different variables).
	 */
	public CRC crc;	
	
	public MAVLinkPacket(){
		payload = new MAVLinkPayload();
	}
	
	/**
	 * Check if the size of the Payload is equal to the "len" byte
	 */
	public boolean payloadIsFilled() {
		if (payload.size() >= MAVLinkPayload.MAX_PAYLOAD_SIZE-1) {
			Log.d("MAV","Buffer overflow");
			return true;
		}
		return (payload.size() == len);
	}
	
	/**
	 * Update CRC for this packet.
	 */
	public void generateCRC(){
		crc = new CRC();
		crc.update_checksum(len);
		crc.update_checksum(seq);
		crc.update_checksum(sysid);
		crc.update_checksum(compid);
		crc.update_checksum(msgid);
		payload.resetIndex();
		for (int i = 0; i < payload.size(); i++) {
			crc.update_checksum(payload.getByte());
		}
		crc.finish_checksum(msgid);
    }

	/**
	 * Encode this packet for transmission. 
	 * 
	 * @return Array with bytes to be transmitted
	 */
	public byte[] encodePacket() {
		byte[] buffer = new byte[6 + len + 2];
		int i = 0;
		buffer[i++] = (byte) MAVLINK_STX;
		buffer[i++] = (byte) len;
		buffer[i++] = (byte) seq;
		buffer[i++] = (byte) sysid;
		buffer[i++] = (byte) compid;
		buffer[i++] = (byte) msgid;
		for (int j = 0; j < payload.size(); j++) {
			buffer[i++] = payload.payload.get(j);
		}
		generateCRC();
		buffer[i++] = (byte) (crc.getLSB());
		buffer[i++] = (byte) (crc.getMSB());
		return buffer;
	}
	
	/**
	 * Unpack the data in this packet and return a MAVLink message
	 * 
	 * @return MAVLink message decoded from this packet
	 */
	public MAVLinkMessage unpack() {
		MAVLinkMessage m = null;
		switch (msgid) {
		case msg_sensor_offsets.MAVLINK_MSG_ID_SENSOR_OFFSETS:
			m = new msg_sensor_offsets(payload);
			break;
		case msg_set_mag_offsets.MAVLINK_MSG_ID_SET_MAG_OFFSETS:
			m =  new msg_set_mag_offsets(payload);
			break;
		case msg_meminfo.MAVLINK_MSG_ID_MEMINFO:
			m =  new msg_meminfo(payload);
			break;
		case msg_ap_adc.MAVLINK_MSG_ID_AP_ADC:
			m =  new msg_ap_adc(payload);
			break;
		case msg_digicam_configure.MAVLINK_MSG_ID_DIGICAM_CONFIGURE:
			m =  new msg_digicam_configure(payload);
			break;
		case msg_digicam_control.MAVLINK_MSG_ID_DIGICAM_CONTROL:
			m =  new msg_digicam_control(payload);
			break;
		case msg_mount_configure.MAVLINK_MSG_ID_MOUNT_CONFIGURE:
			m =  new msg_mount_configure(payload);
			break;
		case msg_mount_control.MAVLINK_MSG_ID_MOUNT_CONTROL:
			m =  new msg_mount_control(payload);
			break;
		case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
			m =  new msg_mount_status(payload);
			break;
		case msg_fence_point.MAVLINK_MSG_ID_FENCE_POINT:
			m =  new msg_fence_point(payload);
			break;
		case msg_fence_fetch_point.MAVLINK_MSG_ID_FENCE_FETCH_POINT:
			m =  new msg_fence_fetch_point(payload);
			break;
		case msg_fence_status.MAVLINK_MSG_ID_FENCE_STATUS:
			m =  new msg_fence_status(payload);
			break;
		case msg_ahrs.MAVLINK_MSG_ID_AHRS:
			m =  new msg_ahrs(payload);
			break;
		case msg_simstate.MAVLINK_MSG_ID_SIMSTATE:
			m =  new msg_simstate(payload);
			break;
		case msg_hwstatus.MAVLINK_MSG_ID_HWSTATUS:
			m =  new msg_hwstatus(payload);
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			m =  new msg_radio(payload);
			break;
		case msg_limits_status.MAVLINK_MSG_ID_LIMITS_STATUS:
			m =  new msg_limits_status(payload);
			break;
		case msg_wind.MAVLINK_MSG_ID_WIND:
			m =  new msg_wind(payload);
			break;
		case msg_data16.MAVLINK_MSG_ID_DATA16:
			m =  new msg_data16(payload);
			break;
		case msg_data32.MAVLINK_MSG_ID_DATA32:
			m =  new msg_data32(payload);
			break;
		case msg_data64.MAVLINK_MSG_ID_DATA64:
			m =  new msg_data64(payload);
			break;
		case msg_data96.MAVLINK_MSG_ID_DATA96:
			m =  new msg_data96(payload);
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			m = new msg_heartbeat(payload);
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			m =  new msg_sys_status(payload);
			break;
		case msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME:
			m =  new msg_system_time(payload);
			break;
		case msg_ping.MAVLINK_MSG_ID_PING:
			m =  new msg_ping(payload);
			break;
		case msg_change_operator_control.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL:
			m =  new msg_change_operator_control(payload);
			break;
		case msg_change_operator_control_ack.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK:
			m =  new msg_change_operator_control_ack(payload);
			break;
		case msg_auth_key.MAVLINK_MSG_ID_AUTH_KEY:
			m =  new msg_auth_key(payload);
			break;
		case msg_set_mode.MAVLINK_MSG_ID_SET_MODE:
			m =  new msg_set_mode(payload);
			break;
		case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ:
			m =  new msg_param_request_read(payload);
			break;
		case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST:
			m =  new msg_param_request_list(payload);
			break;
		case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
			m =  new msg_param_value(payload);
			break;
		case msg_param_set.MAVLINK_MSG_ID_PARAM_SET:
			m =  new msg_param_set(payload);
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			m =  new msg_gps_raw_int(payload);
			break;
		case msg_gps_status.MAVLINK_MSG_ID_GPS_STATUS:
			m =  new msg_gps_status(payload);
			break;
		case msg_scaled_imu.MAVLINK_MSG_ID_SCALED_IMU:
			m =  new msg_scaled_imu(payload);
			break;
		case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
			m =  new msg_raw_imu(payload);
			break;
		case msg_raw_pressure.MAVLINK_MSG_ID_RAW_PRESSURE:
			m =  new msg_raw_pressure(payload);
			break;
		case msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE:
			m =  new msg_scaled_pressure(payload);
			break;
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			m =  new msg_attitude(payload);
			break;
		case msg_attitude_quaternion.MAVLINK_MSG_ID_ATTITUDE_QUATERNION:
			m =  new msg_attitude_quaternion(payload);
			break;
		case msg_local_position_ned.MAVLINK_MSG_ID_LOCAL_POSITION_NED:
			m =  new msg_local_position_ned(payload);
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			m =  new msg_global_position_int(payload);
			break;
		case msg_rc_channels_scaled.MAVLINK_MSG_ID_RC_CHANNELS_SCALED:
			m =  new msg_rc_channels_scaled(payload);
			break;
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			m =  new msg_rc_channels_raw(payload);
			break;
		case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
			m =  new msg_servo_output_raw(payload);
			break;
		case msg_mission_request_partial_list.MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST:
			m =  new msg_mission_request_partial_list(payload);
			break;
		case msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST:
			m =  new msg_mission_write_partial_list(payload);
			break;
		case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
			m =  new msg_mission_item(payload);
			break;
		case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST:
			m =  new msg_mission_request(payload);
			break;
		case msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT:
			m =  new msg_mission_set_current(payload);
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			m =  new msg_mission_current(payload);
			break;
		case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST:
			m =  new msg_mission_request_list(payload);
			break;
		case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT:
			m =  new msg_mission_count(payload);
			break;
		case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL:
			m =  new msg_mission_clear_all(payload);
			break;
		case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
			m =  new msg_mission_item_reached(payload);
			break;
		case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
			m =  new msg_mission_ack(payload);
			break;
		case msg_set_gps_global_origin.MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN:
			m =  new msg_set_gps_global_origin(payload);
			break;
		case msg_gps_global_origin.MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN:
			m =  new msg_gps_global_origin(payload);
			break;
		case msg_set_local_position_setpoint.MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT:
			m =  new msg_set_local_position_setpoint(payload);
			break;
		case msg_local_position_setpoint.MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT:
			m =  new msg_local_position_setpoint(payload);
			break;
		case msg_global_position_setpoint_int.MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT:
			m =  new msg_global_position_setpoint_int(payload);
			break;
		case msg_set_global_position_setpoint_int.MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT:
			m =  new msg_set_global_position_setpoint_int(payload);
			break;
		case msg_safety_set_allowed_area.MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA:
			m =  new msg_safety_set_allowed_area(payload);
			break;
		case msg_safety_allowed_area.MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA:
			m =  new msg_safety_allowed_area(payload);
			break;
		case msg_set_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST:
			m =  new msg_set_roll_pitch_yaw_thrust(payload);
			break;
		case msg_set_roll_pitch_yaw_speed_thrust.MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST:
			m =  new msg_set_roll_pitch_yaw_speed_thrust(payload);
			break;
		case msg_roll_pitch_yaw_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT:
			m =  new msg_roll_pitch_yaw_thrust_setpoint(payload);
			break;
		case msg_roll_pitch_yaw_speed_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_SPEED_THRUST_SETPOINT:
			m =  new msg_roll_pitch_yaw_speed_thrust_setpoint(payload);
			break;
		case msg_set_quad_motors_setpoint.MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT:
			m =  new msg_set_quad_motors_setpoint(payload);
			break;
		case msg_set_quad_swarm_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST:
			m =  new msg_set_quad_swarm_roll_pitch_yaw_thrust(payload);
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			m =  new msg_nav_controller_output(payload);
			break;
		case msg_set_quad_swarm_led_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST:
			m =  new msg_set_quad_swarm_led_roll_pitch_yaw_thrust(payload);
			break;
		case msg_state_correction.MAVLINK_MSG_ID_STATE_CORRECTION:
			m =  new msg_state_correction(payload);
			break;
		case msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM:
			m =  new msg_request_data_stream(payload);
			break;
		case msg_data_stream.MAVLINK_MSG_ID_DATA_STREAM:
			m =  new msg_data_stream(payload);
			break;
		case msg_manual_control.MAVLINK_MSG_ID_MANUAL_CONTROL:
			m =  new msg_manual_control(payload);
			break;
		case msg_rc_channels_override.MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE:
			m =  new msg_rc_channels_override(payload);
			break;
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			m =  new msg_vfr_hud(payload);
			break;
		case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG:
			m =  new msg_command_long(payload);
			break;
		case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
			m =  new msg_command_ack(payload);
			break;
		case msg_roll_pitch_yaw_rates_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_RATES_THRUST_SETPOINT:
			m =  new msg_roll_pitch_yaw_rates_thrust_setpoint(payload);
			break;
		case msg_manual_setpoint.MAVLINK_MSG_ID_MANUAL_SETPOINT:
			m =  new msg_manual_setpoint(payload);
			break;
		case msg_local_position_ned_system_global_offset.MAVLINK_MSG_ID_LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET:
			m =  new msg_local_position_ned_system_global_offset(payload);
			break;
		case msg_hil_state.MAVLINK_MSG_ID_HIL_STATE:
			m =  new msg_hil_state(payload);
			break;
		case msg_hil_controls.MAVLINK_MSG_ID_HIL_CONTROLS:
			m =  new msg_hil_controls(payload);
			break;
		case msg_hil_rc_inputs_raw.MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW:
			m =  new msg_hil_rc_inputs_raw(payload);
			break;
		case msg_optical_flow.MAVLINK_MSG_ID_OPTICAL_FLOW:
			m =  new msg_optical_flow(payload);
			break;
		case msg_global_vision_position_estimate.MAVLINK_MSG_ID_GLOBAL_VISION_POSITION_ESTIMATE:
			m =  new msg_global_vision_position_estimate(payload);
			break;
		case msg_vision_position_estimate.MAVLINK_MSG_ID_VISION_POSITION_ESTIMATE:
			m =  new msg_vision_position_estimate(payload);
			break;
		case msg_vision_speed_estimate.MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE:
			m =  new msg_vision_speed_estimate(payload);
			break;
		case msg_vicon_position_estimate.MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE:
			m =  new msg_vicon_position_estimate(payload);
			break;
		case msg_highres_imu.MAVLINK_MSG_ID_HIGHRES_IMU:
			m =  new msg_highres_imu(payload);
			break;
		case msg_file_transfer_start.MAVLINK_MSG_ID_FILE_TRANSFER_START:
			m =  new msg_file_transfer_start(payload);
			break;
		case msg_file_transfer_dir_list.MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST:
			m =  new msg_file_transfer_dir_list(payload);
			break;
		case msg_file_transfer_res.MAVLINK_MSG_ID_FILE_TRANSFER_RES:
			m =  new msg_file_transfer_res(payload);
			break;
		case msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS:
			m =  new msg_battery_status(payload);
			break;
		case msg_setpoint_8dof.MAVLINK_MSG_ID_SETPOINT_8DOF:
			m =  new msg_setpoint_8dof(payload);
			break;
		case msg_setpoint_6dof.MAVLINK_MSG_ID_SETPOINT_6DOF:
			m =  new msg_setpoint_6dof(payload);
			break;
		case msg_memory_vect.MAVLINK_MSG_ID_MEMORY_VECT:
			m =  new msg_memory_vect(payload);
			break;
		case msg_debug_vect.MAVLINK_MSG_ID_DEBUG_VECT:
			m =  new msg_debug_vect(payload);
			break;
		case msg_named_value_float.MAVLINK_MSG_ID_NAMED_VALUE_FLOAT:
			m =  new msg_named_value_float(payload);
			break;
		case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
			m =  new msg_named_value_int(payload);
			break;
		case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
			m =  new msg_statustext(payload);
			break;
		case msg_debug.MAVLINK_MSG_ID_DEBUG:
			m =  new msg_debug(payload);
			break;
		default:
			Log.d("MAVLink", "UNKNOW MESSAGE - " + msgid);
			return null;
		}
		if (m != null) {
			m.sysid = sysid;
			m.compid = compid;
		}
		return m;
	}

}
	
