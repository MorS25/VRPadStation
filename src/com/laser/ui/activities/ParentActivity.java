package com.laser.ui.activities;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_sys_status;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.app.VrPadStationApp.ConnectionStateListener;
import com.laser.utils.LaserConstants;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ParentActivity extends Activity implements OnClickListener, 
														ConnectionStateListener {
	
	private VrPadStationApp app;
	private ActionBar bar;
	
	private ImageButton btnConnect;
	private ImageButton btnGcs;
	private ImageButton btnRadio;
	private ImageButton btnGimbal;
	private ImageButton btnSettings;
	private TextView textViewVoltage;
	private TextView textViewDrone;
	private TextView textViewGpsStatus;
	
	public ParentActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		app = (VrPadStationApp) getApplication();
		app.setConnectionStateListener(this);
		
		bar = getActionBar();
		bar.setTitle("");
		bar.setLogo(getResources().getDrawable(R.drawable.logo_laser_bianco_256));
		
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		bar.setCustomView(R.layout.layout_action_bar);
		
        textViewVoltage = (TextView) bar.getCustomView().findViewById(R.id.textViewVoltage);
        textViewVoltage.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
        
        textViewGpsStatus = (TextView) bar.getCustomView().findViewById(R.id.textViewGpsStatus);
        textViewGpsStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
        
        textViewDrone = (TextView) bar.getCustomView().findViewById(R.id.textViewDrone);
        textViewDrone.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		
		btnConnect = (ImageButton) bar.getCustomView().findViewById(R.id.btnConnect);
	    btnConnect.setOnClickListener(this);
	    
	    btnGcs = (ImageButton) bar.getCustomView().findViewById(R.id.btnGcs);
	    btnGcs.setOnClickListener(this);
	    
	    btnRadio = (ImageButton) bar.getCustomView().findViewById(R.id.btnRadio);
	    btnRadio.setOnClickListener(this);
	    
	    btnGimbal = (ImageButton) bar.getCustomView().findViewById(R.id.btnGimbal);
	    btnGimbal.setOnClickListener(this);
	    
	    btnSettings = (ImageButton) bar.getCustomView().findViewById(R.id.btnSettings);
	    btnSettings.setOnClickListener(this);
		
	    
		if (app.isMavlinkConnected())
		{
			btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.disconnect_small));
//			enableButtons(true);
		}
		else
		{
			btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.connect_small));
//			enableButtons(false);
		}
		enableButtons(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		app.removeConnectionStateListener(this);
		super.onDestroy();
	}
    
	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.btnConnect:
			app.toggleConnectionState();
			break;
		case R.id.btnGcs:
			Intent iGcs = new Intent(this, GcsActivity.class);
			try {
				startActivity(iGcs);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnRadio:
			Intent iRadio = new Intent(this, RadioActivity.class);
			try {
				startActivity(iRadio);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btnGimbal:
			showToast("Not yet implemented!");
			break;
		case R.id.btnSettings:
			Intent iSettings = new Intent(this, SettingsActivity.class);
			try
			{
				startActivityForResult(iSettings, LaserConstants.SETTINGS_CODE);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	public void showToast(String text)
	{
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void enableButtons(boolean enabled)
	{
		btnGcs.setEnabled(enabled);
		btnRadio.setEnabled(enabled);
		btnGimbal.setEnabled(enabled);
	}

	@Override
	public void mavlinkConnected() {
		btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.disconnect_small));
		enableButtons(true);
	}

	@Override
	public void mavlinkDisconnected() {
		btnConnect.setImageDrawable(getResources().getDrawable(R.drawable.connect_small));
//		enableButtons(false);
	}

	@Override
	public void mavlinkReceivedData(MAVLinkMessage msg) {
		switch (msg.msgid) 
		{
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			updateDrone();
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			updateDroneBattery();			
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			updateDroneGpsStatus();
			break;
		}
	}

	@Override
	public void serialReceivedData(byte[] readData) {}
	
	private void updateDroneGpsStatus()
	{
		if (textViewGpsStatus != null)
		{
			String gpsFix = "";
			if (app.drone.getSatCount() >= 0) {
				switch (app.drone.getFixType()) {
				case 1:
					gpsFix = ("No Fix (" + app.drone.getSatCount() + ")\nHDOP: " + app.drone.getHDOP());
					textViewGpsStatus.setTextColor(Color.RED);
					break;
				case 2:
					gpsFix = ("2D Fix(" + app.drone.getSatCount() + ")\nHDOP: " + app.drone.getHDOP());
					textViewGpsStatus.setTextColor(Color.YELLOW);
					break;
				case 3:
					gpsFix = ("3D Fix (" + app.drone.getSatCount() + ")\nHDOP: " + app.drone.getHDOP());
					textViewGpsStatus.setTextColor(Color.GREEN);
					break;
				default:
					gpsFix = ("No GPS (" + app.drone.getSatCount() + ")\nHDOP: " + app.drone.getHDOP());
					textViewGpsStatus.setTextColor(Color.RED);
					break;
				}
			}
			textViewGpsStatus.setText(" " + gpsFix + " ");
		}
	}
	
	private void updateDrone()
	{
		if (textViewDrone != null)
		{			
			textViewDrone.setText(app.drone.getMode().getName());
			if (app.drone.isArmed())
				textViewDrone.append("\nARMED");
			else
				textViewDrone.append("\nDISARMED");
		}
	}
	
	private void updateDroneBattery() 
	{
		if (textViewVoltage != null)
			textViewVoltage.setText(app.drone.getBattVolt() + " V\n" + app.drone.getBattRemain() + "%");
	}
	
}
