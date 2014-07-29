package com.laser.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_command_ack;
import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_CMD_ACK;
import com.laser.MAVLink.Drone;
import com.laser.service.MAVLinkClient;

public class AccelerometerCalibration implements OnClickListener {
	
	private MAVLinkClient MAV;
	private Context context;
	private int counter;
	private Drone drone;

	public AccelerometerCalibration(MAVLinkClient MAVClient, Drone drone) {
		this.MAV = MAVClient;
		this.drone = drone;
	}

	public void startCalibration(Context context) {
		this.context = context;
		sendStartCalibrationMessage();
		counter = 0;
	}	

	private void createCalibrationDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", this);
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

	public void processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) 
		{
			msg_statustext statusMsg = (msg_statustext) msg;
			String msgText = statusMsg.getText();
			if (msgText.contains("Place APM") || msgText.contains("Place vehicle"))
				createCalibrationDialog(statusMsg.getText());
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int id) {
		counter++;
		sendCalibrationAckMessage(counter);
		if (counter >= 6) 
		{
			createCalibrationDialog("Calibration Completed!");
			counter = 0;
		}
	}

	private void sendStartCalibrationMessage() {
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 1;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		MAV.sendMavPacket(msg.pack());
	}

	private void sendCalibrationAckMessage(int count) {
		msg_command_ack msg = new msg_command_ack();
		msg.command = (short) count;
		msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK;
		MAV.sendMavPacket(msg.pack());
	}

}
