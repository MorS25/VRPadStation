package com.laser.ui.fragments;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.laser.MAVLink.Drone;
import com.google.android.gms.maps.model.LatLng;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.helpers.SolarRadiationAsyncTask;
import com.laser.ui.layers.MapLayer;
import com.laser.ui.widgets.AutoResizeTextView;
import com.laser.utils.GeoUtils;
import com.laser.utils.LaserConstants;

public class OverviewFragment extends Fragment implements OnClickListener {
	
	
	 private ImageButton btnZoom;
	 private Button btnToggleArm;
	 private ImageButton btnClearTrack;
	 private ImageButton btnAltDown;
	 private ImageButton btnAltUp;
	 private AutoResizeTextView tvPadBattery;
	 private AutoResizeTextView tvSolarRadiation;
//	 private AutoResizeTextView tvHomeDistance;
//	 private AutoResizeTextView tvAltitude;
//	 private AutoResizeTextView tvGroundSpeed;
//	 private AutoResizeTextView tvAirSpeed;
//	 private AutoResizeTextView tvClimbRate;
	 private AutoResizeTextView tvSetAltitude;
	 private TextView textViewDrone;
	 
	 private Drone drone;	 	 
	 private int batteryLevel = 0;
	 private boolean updateFields = false;
	 
	 private double altitude = 0;
	 
	private OverviewListener listener;
	public interface OverviewListener {
		void zoom();
		void toggleArmed();
		void clearTrack();
		void setAltitude(double d);
	}
	public void setListeners(MapLayer listener)
	{
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.overview_fragment, container, false);
		btnZoom = (ImageButton) view.findViewById(R.id.btnZoom);
		btnToggleArm = (Button) view.findViewById(R.id.btnToggleArm);
		btnClearTrack = (ImageButton) view.findViewById(R.id.btnClearTrack);
		btnAltUp = (ImageButton) view.findViewById(R.id.btnAltUp);
		btnAltDown = (ImageButton) view.findViewById(R.id.btnAltDown);
		tvPadBattery = (AutoResizeTextView) view.findViewById(R.id.tvPadBattery);
		tvSolarRadiation = (AutoResizeTextView) view.findViewById(R.id.tvSolarRadiation);
//		tvHomeDistance = (AutoResizeTextView) view.findViewById(R.id.tvHomeDistance);
//		tvAltitude = (AutoResizeTextView) view.findViewById(R.id.tvAltitude);
//		tvGroundSpeed = (AutoResizeTextView) view.findViewById(R.id.tvGroundSpeed);
//		tvAirSpeed = (AutoResizeTextView) view.findViewById(R.id.tvAirSpeed);
//		tvClimbRate = (AutoResizeTextView) view.findViewById(R.id.tvClimbRate);
		tvSetAltitude = (AutoResizeTextView) view.findViewById(R.id.tvSetAltitude);
		textViewDrone = (TextView) view.findViewById(R.id.textViewDrone);
		
		tvPadBattery.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		tvSolarRadiation.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
//		tvHomeDistance.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
//		tvAltitude.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
//		tvGroundSpeed.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
//		tvAirSpeed.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
//		tvClimbRate.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		tvSetAltitude.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		textViewDrone.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		btnToggleArm.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);

		btnZoom.setOnClickListener(this);
		btnToggleArm.setOnClickListener(this);
		btnClearTrack.setOnClickListener(this);
		btnAltUp.setOnClickListener(this);
		btnAltDown.setOnClickListener(this);		

		if (drone != null)
			updateArmButton(drone.isArmed());
		else
			updateArmButton(false);
		
		return view;
	}
		
	private boolean viewCreated = false;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewCreated = true;
		
		updateAltitude(altitude);
		
		this.drone = ((VrPadStationApp)getActivity().getApplication()).drone;
		updateBattery(batteryLevel, false);
		
		new UpdateSolarRadiation().execute();
		
		updateTimer.postDelayed(runnableUpdateFields, 2000);
	}

	Handler updateTimer = new Handler();
	private Runnable runnableUpdateFields = new Runnable() {		
		@Override
		public void run() {
			updateFields = true;
			updateTimer.postDelayed(runnableUpdateFields, 1000);
		}
	};
	
	private class UpdateSolarRadiation extends SolarRadiationAsyncTask {
		
		@Override
		protected void onPostExecute(Integer result) 
		{			
			if (result == null || result < 0) {
				tvSolarRadiation.setTextColor(Color.WHITE);
				tvSolarRadiation.setText("Error reading solar radiation level");
			} else if (result < 4) {
				tvSolarRadiation.setTextColor(Color.GREEN);
				tvSolarRadiation.setText("Solar radiation level: GOOD");
			} else if (result == 4) {
				tvSolarRadiation.setTextColor(Color.YELLOW);
				tvSolarRadiation.setText("Solar radiation level: WARNING");
			} else {	// result > 4
				tvSolarRadiation.setTextColor(Color.RED);
				tvSolarRadiation.setText("Solar radiation level: CRITICAL");
			}				
			super.onPostExecute(result);
		}
	}
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewCreated = false;
	}
	
	@Override
	public void onClick(View v) {
		if (listener != null)
		{
			if (v == btnZoom)
				listener.zoom();
			else if (v == btnToggleArm)
	    		listener.toggleArmed();
			else if (v == btnClearTrack)
	    		listener.clearTrack();
			else if (v == btnAltUp)
				listener.setAltitude(0.5);
			else if (v == btnAltDown)
				listener.setAltitude(-0.5);
		}
	}

	public void updateArmButton(boolean armed) 
	{
		if (btnToggleArm != null)
		{
			if (armed)
			{
				//btnToggleArm.setImageDrawable(getResources().getDrawable(R.drawable.disarm));
				btnToggleArm.setTextColor(Color.RED);
				btnToggleArm.setText("DISARM");
			}
			else
			{
				//btnToggleArm.setImageDrawable(getResources().getDrawable(R.drawable.arm));
				btnToggleArm.setTextColor(Color.GREEN);
				btnToggleArm.setText("ARM");
			}
		}			
	}

	public void updateBattery(int level, boolean isAc) 
	{
		batteryLevel = level;
		if (viewCreated)
		{	
			tvPadBattery.setTextColor(Color.WHITE);
			tvPadBattery.setText("Pad battery: ");
			
			if (batteryLevel >= 80)
				tvPadBattery.setTextColor(Color.GREEN);
			else if (batteryLevel < 80 && batteryLevel >= 40)
				tvPadBattery.setTextColor(Color.YELLOW);
			else if (batteryLevel < 40)
				tvPadBattery.setTextColor(Color.RED);
			tvPadBattery.append(String.valueOf(batteryLevel));
			// non posso colorare in modo diverso il testo di una TextView
			// tvPadBattery.setTextColor(Color.WHITE);
			if (isAc)
				tvPadBattery.append("% AC");
			else
				tvPadBattery.append("% ");
		}
	}

	public void receivedData() {
		if (viewCreated && updateFields)
		{	
			updateFields = false;
			float altitude = (float) drone.getAltitude();
			float groundSpeed = (float) drone.getGroundSpeed();
			float airSpeed = (float) drone.getAirSpeed();
			float climbRate = drone.getClimbRate();
			LatLng homePos = drone.getHome().coord;
			LatLng currPos = drone.getPosition();
			msg_rc_channels_raw rc = drone.getRcChannelsRaw();
			if (rc == null)
				rc = new msg_rc_channels_raw();
			msg_servo_output_raw servo = drone.getRcOutputRaw();
			if (servo == null)
				servo = new msg_servo_output_raw();
			int distance = -1;
			if (homePos != null && currPos != null)
			{
				distance = (int) GeoUtils.calculateDistance(currPos, homePos);
//				tvHomeDistance.setText("Home distance: " + distance + " m");
			}	
//			tvAltitude.setText("Altitude: " + altitude + " m");
//			tvGroundSpeed.setText("Ground speed: " + groundSpeed + " m/s");
//			tvAirSpeed.setText("Air speed: " + airSpeed + " m/s");
//			tvClimbRate.setText("Climb rate: " + climbRate + " m/s");
			
			String textDrone = "Home distance: " + distance + " m\n" +
							   "Altitude: " + altitude + " m\n" +
							   "Ground speed: " + groundSpeed + " m/s\n" +
							   "Air speed: " + airSpeed + " m/s\n" +
							   "Climb rate: " + climbRate + " m/s" + 
							   "\n\nRC Channel 1: " + rc.chan1_raw +
							   "\nRC Channel 2: " + rc.chan2_raw +
							   "\nRC Channel 3: " + rc.chan3_raw +
							   "\nRC Channel 4: " + rc.chan4_raw +
							   "\nRC Channel 5: " + rc.chan5_raw +
							   "\nRC Channel 6: " + rc.chan6_raw +
							   "\nRC Channel 7: " + rc.chan7_raw +
							   "\nRC Channel 8: " + rc.chan8_raw +
							   "\n\nServo 1: " + servo.servo1_raw +
							   "\nServo 2: " + servo.servo2_raw +
							   "\nServo 3: " + servo.servo3_raw +
							   "\nServo 4: " + servo.servo4_raw +
							   "\nServo 5: " + servo.servo5_raw +
							   "\nServo 6: " + servo.servo6_raw +
							   "\nServo 7: " + servo.servo7_raw +
							   "\nServo 8: " + servo.servo8_raw;
			textViewDrone.setText(textDrone);
		}
	}

	public void updateAltitude(double targetAltitude) {
		if (tvSetAltitude != null)
			tvSetAltitude.setText((float)targetAltitude + " m");
	
		altitude = targetAltitude;
	}
	
	
}
