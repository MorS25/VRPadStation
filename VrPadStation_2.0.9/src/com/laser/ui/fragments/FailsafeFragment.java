package com.laser.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.laser.parameters.Parameter;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.widgets.RangeProgressBar;
import com.laser.ui.widgets.SpinnerAuto;
import com.laser.ui.widgets.SpinnerAuto.OnSpinnerItemSelectedListener;
import com.laser.utils.LaserConstants;

public class FailsafeFragment extends Fragment implements OnSpinnerItemSelectedListener {
	

	private SpinnerAuto spinnerOptions;
	private EditText editTextFspwm;
	private EditText editTextLowBattery;
	private EditText editTextReservedMah;
	
	private RangeProgressBar progressIn1;
	private RangeProgressBar progressIn2;
	private RangeProgressBar progressIn3;
	private RangeProgressBar progressIn4;
	private RangeProgressBar progressIn5;
	private RangeProgressBar progressIn6;
	private RangeProgressBar progressIn7;
	private RangeProgressBar progressIn8;
	private RangeProgressBar progressOut1;
	private RangeProgressBar progressOut2;
	private RangeProgressBar progressOut3;
	private RangeProgressBar progressOut4;
	private RangeProgressBar progressOut5;
	private RangeProgressBar progressOut6;
	private RangeProgressBar progressOut7;
	private RangeProgressBar progressOut8;	
	private TextView textViewIn1;
	private TextView textViewIn2;
	private TextView textViewIn3;
	private TextView textViewIn4;
	private TextView textViewIn5;
	private TextView textViewIn6;
	private TextView textViewIn7;
	private TextView textViewIn8;
	private TextView textViewOut1;
	private TextView textViewOut2;
	private TextView textViewOut3;
	private TextView textViewOut4;
	private TextView textViewOut5;
	private TextView textViewOut6;
	private TextView textViewOut7;
	private TextView textViewOut8;
	private TextView textViewMode;
	private TextView textViewArm;
	private TextView textViewGps;
	private VrPadStationApp app;
	private final int def_rc_min = 1000;
	private final int def_rc_max = 2000;
	private CheckBox checkBoxGcsFailsafe;
	private CheckBox checkBoxBatteryFailsafe;	
	
	private Parameter pThrEnable,
					  pBattEnable, 
					  pThrValue,
					  pBattMah, 
					  pGcsEnable,
					  pLowVolt,
					  pBattVoltage;
	
	private FailsafeListener listener;
	private int selectedFailsafe = 0;
	public interface FailsafeListener {
		void onRefreshFailsafeParams();
		void onSendFailsafeParam(Parameter parameter);		
	}
	public void setListeners(FailsafeListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		app = (VrPadStationApp) getActivity().getApplication();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.failsafe_settings_fragment, container, false);		
		
		progressIn1 = (RangeProgressBar) view.findViewById(R.id.progressIn1);
		progressIn2 = (RangeProgressBar) view.findViewById(R.id.progressIn2);
		progressIn3 = (RangeProgressBar) view.findViewById(R.id.progressIn3);
		progressIn4 = (RangeProgressBar) view.findViewById(R.id.progressIn4);
		progressIn5 = (RangeProgressBar) view.findViewById(R.id.progressIn5);
		progressIn6 = (RangeProgressBar) view.findViewById(R.id.progressIn6);
		progressIn7 = (RangeProgressBar) view.findViewById(R.id.progressIn7);
		progressIn8 = (RangeProgressBar) view.findViewById(R.id.progressIn8);
		progressOut1 = (RangeProgressBar) view.findViewById(R.id.progressOut1);
		progressOut2 = (RangeProgressBar) view.findViewById(R.id.progressOut2);
		progressOut3 = (RangeProgressBar) view.findViewById(R.id.progressOut3);
		progressOut4 = (RangeProgressBar) view.findViewById(R.id.progressOut4);
		progressOut5 = (RangeProgressBar) view.findViewById(R.id.progressOut5);
		progressOut6 = (RangeProgressBar) view.findViewById(R.id.progressOut6);
		progressOut7 = (RangeProgressBar) view.findViewById(R.id.progressOut7);
		progressOut8 = (RangeProgressBar) view.findViewById(R.id.progressOut8);
		textViewIn1 = (TextView) view.findViewById(R.id.textViewIn1);
		textViewIn2 = (TextView) view.findViewById(R.id.textViewIn2);
		textViewIn3 = (TextView) view.findViewById(R.id.textViewIn3);
		textViewIn4 = (TextView) view.findViewById(R.id.textViewIn4);
		textViewIn5 = (TextView) view.findViewById(R.id.textViewIn5);
		textViewIn6 = (TextView) view.findViewById(R.id.textViewIn6);
		textViewIn7 = (TextView) view.findViewById(R.id.textViewIn7);
		textViewIn8 = (TextView) view.findViewById(R.id.textViewIn8);
		textViewOut1 = (TextView) view.findViewById(R.id.textViewOut1);
		textViewOut2 = (TextView) view.findViewById(R.id.textViewOut2);
		textViewOut3 = (TextView) view.findViewById(R.id.textViewOut3);
		textViewOut4 = (TextView) view.findViewById(R.id.textViewOut4);
		textViewOut5 = (TextView) view.findViewById(R.id.textViewOut5);
		textViewOut6 = (TextView) view.findViewById(R.id.textViewOut6);
		textViewOut7 = (TextView) view.findViewById(R.id.textViewOut7);
		textViewOut8 = (TextView) view.findViewById(R.id.textViewOut8);
		
		textViewIn1.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn2.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn3.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn4.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn5.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn6.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn7.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewIn8.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut1.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut2.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut3.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut4.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut5.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut6.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut7.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		textViewOut8.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		
		textViewIn1.setText("Radio1: " + 0);
		textViewIn2.setText("Radio2: " + 0);
		textViewIn3.setText("Radio3: " + 0);
		textViewIn4.setText("Radio4: " + 0);
		textViewIn5.setText("Radio5: " + 0);
		textViewIn6.setText("Radio6: " + 0);
		textViewIn7.setText("Radio7: " + 0);
		textViewIn8.setText("Radio8: " + 0);
		textViewOut1.setText("Servo1: " + 0);
		textViewOut2.setText("Servo2: " + 0);
		textViewOut3.setText("Servo3: " + 0);
		textViewOut4.setText("Servo4: " + 0);
		textViewOut5.setText("Servo5: " + 0);
		textViewOut6.setText("Servo6: " + 0);
		textViewOut7.setText("Servo7: " + 0);
		textViewOut8.setText("Servo8: " + 0);

		progressIn1.setBounds(def_rc_max, def_rc_min);
		progressIn2.setBounds(def_rc_max, def_rc_min);
		progressIn3.setBounds(def_rc_max, def_rc_min);
		progressIn4.setBounds(def_rc_max, def_rc_min);
		progressIn5.setBounds(def_rc_max, def_rc_min);
		progressIn6.setBounds(def_rc_max, def_rc_min);
		progressIn7.setBounds(def_rc_max, def_rc_min);
		progressIn8.setBounds(def_rc_max, def_rc_min);
		progressOut1.setBounds(def_rc_max, def_rc_min);
		progressOut2.setBounds(def_rc_max, def_rc_min);
		progressOut3.setBounds(def_rc_max, def_rc_min);
		progressOut4.setBounds(def_rc_max, def_rc_min);
		progressOut5.setBounds(def_rc_max, def_rc_min);
		progressOut6.setBounds(def_rc_max, def_rc_min);
		progressOut7.setBounds(def_rc_max, def_rc_min);
		progressOut8.setBounds(def_rc_max, def_rc_min);
		setRangeBarShowRange(false);
		
		textViewMode = (TextView) view.findViewById(R.id.textViewMode);
		textViewArm = (TextView) view.findViewById(R.id.textViewArm);
		textViewGps = (TextView) view.findViewById(R.id.textViewGps);
		textViewMode.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		textViewArm.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		textViewGps.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		
		spinnerOptions = (SpinnerAuto) view.findViewById(R.id.spinnerOptions);
		editTextFspwm = (EditText) view.findViewById(R.id.editTextFspwm);
		checkBoxBatteryFailsafe = (CheckBox) view.findViewById(R.id.checkBoxBatteryFailsafe);
		editTextLowBattery = (EditText) view.findViewById(R.id.editTextLowBattery);
		editTextReservedMah = (EditText) view.findViewById(R.id.editTextReservedMah);
		checkBoxGcsFailsafe = (CheckBox) view.findViewById(R.id.checkBoxGcsFailsafe);
		final Button btnSave = (Button) view.findViewById(R.id.btnSave);
		final Button btnRefresh = (Button) view.findViewById(R.id.btnRefresh);
		final TextView textViewOptions = (TextView) view.findViewById(R.id.textViewOptions);
		final TextView textViewFspwm = (TextView) view.findViewById(R.id.textViewFspwm);
		final TextView textViewLowBattery = (TextView) view.findViewById(R.id.textViewLowBattery);
		final TextView textViewReservedMah = (TextView) view.findViewById(R.id.textViewReservedMah);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this.getActivity(),
																	        R.array.failsafe_items,
																	        android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinnerOptions.setAdapter(adapter);
		spinnerOptions.setOnSpinnerItemSelectedListener(this);
		
		btnSave.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendParams();			
			}
		});	
		btnRefresh.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				listener.onRefreshFailsafeParams();
			}
		});	
		
		
		textViewOptions.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		textViewFspwm.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		textViewLowBattery.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		textViewReservedMah.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		editTextFspwm.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		checkBoxBatteryFailsafe.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		editTextLowBattery.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		editTextReservedMah.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		checkBoxGcsFailsafe.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnSave.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnRefresh.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		
		return view;
	}
	
	
	private void setRangeBarShowRange(boolean showRange) {
		progressIn1.showRange(showRange);
		progressIn2.showRange(showRange);
		progressIn3.showRange(showRange);
		progressIn4.showRange(showRange);
		progressIn5.showRange(showRange);
		progressIn6.showRange(showRange);
		progressIn7.showRange(showRange);
		progressIn8.showRange(showRange);
		progressOut1.showRange(showRange);
		progressOut2.showRange(showRange);
		progressOut3.showRange(showRange);
		progressOut4.showRange(showRange);
		progressOut5.showRange(showRange);
		progressOut6.showRange(showRange);
		progressOut7.showRange(showRange);
		progressOut8.showRange(showRange);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		selectedFailsafe  = position;
	}
	
	private boolean viewCreated = false;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewCreated = true;
		listener.onRefreshFailsafeParams();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewCreated = false;
	}
		
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}	
	
	public void processMessage(MAVLinkMessage msg) {		
		if (bDisableRefresh)
			return;
		
		if (viewCreated && msg.msgid ==  msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW) {
			msg_rc_channels_raw m_rc = (msg_rc_channels_raw) msg;
			
			textViewIn1.setText("Radio1: " + m_rc.chan1_raw);
			textViewIn2.setText("Radio2: " + m_rc.chan2_raw);
			textViewIn3.setText("Radio3: " + m_rc.chan3_raw);
			textViewIn4.setText("Radio4: " + m_rc.chan4_raw);
			textViewIn5.setText("Radio5: " + m_rc.chan5_raw);
			textViewIn6.setText("Radio6: " + m_rc.chan6_raw);
			textViewIn7.setText("Radio7: " + m_rc.chan7_raw);
			textViewIn8.setText("Radio8: " + m_rc.chan8_raw);
			
			progressIn1.setProgress(m_rc.chan1_raw);
			progressIn2.setProgress(m_rc.chan2_raw);
			progressIn3.setProgress(m_rc.chan3_raw);
			progressIn4.setProgress(m_rc.chan4_raw);
			progressIn5.setProgress(m_rc.chan5_raw);
			progressIn6.setProgress(m_rc.chan6_raw);
			progressIn7.setProgress(m_rc.chan7_raw);
			progressIn8.setProgress(m_rc.chan8_raw);			
		} else if (viewCreated && msg.msgid ==  msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW) {
			msg_servo_output_raw m_servo = (msg_servo_output_raw) msg;

			textViewOut1.setText("Servo1: " + m_servo.servo1_raw);
			textViewOut2.setText("Servo2: " + m_servo.servo2_raw);
			textViewOut3.setText("Servo3: " + m_servo.servo3_raw);
			textViewOut4.setText("Servo4: " + m_servo.servo4_raw);
			textViewOut5.setText("Servo5: " + m_servo.servo5_raw);
			textViewOut6.setText("Servo6: " + m_servo.servo6_raw);
			textViewOut7.setText("Servo7: " + m_servo.servo7_raw);
			textViewOut8.setText("Servo8: " + m_servo.servo8_raw);

			progressOut1.setProgress(m_servo.servo1_raw);
			progressOut2.setProgress(m_servo.servo2_raw);
			progressOut3.setProgress(m_servo.servo3_raw);
			progressOut4.setProgress(m_servo.servo4_raw);
			progressOut5.setProgress(m_servo.servo5_raw);
			progressOut6.setProgress(m_servo.servo6_raw);
			progressOut7.setProgress(m_servo.servo7_raw);
		} else if (viewCreated && msg.msgid ==  msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT) {
			if (textViewArm != null && app.drone.isArmed())
				textViewArm.setText("ARMED");
			else if (textViewArm != null && !app.drone.isArmed())
				textViewArm.setText("DISARMED");
			if (textViewMode != null)
				textViewMode.setText(app.drone.getMode().getName());
		} else if (viewCreated && msg.msgid ==  msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT) {
			if (textViewGps != null) {
				String gpsFix = "";
				if (app.drone.getSatCount() >= 0) {
					switch (app.drone.getFixType()) {
					case 1:
						gpsFix = ("No Fix");
						break;
					case 2:
						gpsFix = ("2D Fix");
						break;
					case 3:
						gpsFix = ("3D Fix");
						break;
					default:
						gpsFix = ("No GPS");
						break;
					}
				}
				textViewGps.setText(gpsFix);
			}
		} else if (viewCreated && msg.msgid ==  msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			msg_param_value vv = (msg_param_value) msg;
			Parameter param = new Parameter(vv);
			
			if (param.name.equalsIgnoreCase("FS_THR_ENABLE")) {
				pThrEnable = param;
				spinnerOptions.setSelection((int)pThrEnable.value);
				selectedFailsafe = (int) pThrEnable.value;
			}
			
			if (param.name.equalsIgnoreCase("FS_BATT_ENABLE")) {
				pBattEnable = param;
				checkBoxBatteryFailsafe.setChecked(pBattEnable.value != 0);
			}
			
			if (param.name.equalsIgnoreCase("FS_THR_VALUE")) {
				pThrValue = param;
				editTextFspwm.setText(String.valueOf(pThrValue.value));
			}
				
			if (param.name.equalsIgnoreCase("FS_BATT_MAH")) {
				pBattMah = param;
				editTextReservedMah.setText(String.valueOf(pBattMah.value));
			}
				
			if (param.name.equalsIgnoreCase("FS_GCS_ENABLE")) {
				pGcsEnable = param;
				checkBoxGcsFailsafe.setChecked(pGcsEnable.value != 0);
			}
			
			if (param.name.equalsIgnoreCase("LOW_VOLT")) {
				pLowVolt = param;
				editTextLowBattery.setText(String.valueOf(pLowVolt.value));
			}
			else if (param.name.equalsIgnoreCase("FS_BATT_VOLTAGE")) {
				pBattVoltage = param;
				editTextLowBattery.setText(String.valueOf(pBattVoltage.value));
			}
		}
	}
	
		
	boolean bDisableRefresh = false;
	private void sendParams() {
		bDisableRefresh = true;
		
		if (pThrEnable != null)
			pThrEnable.value = selectedFailsafe;
		if (pBattEnable != null)
			pBattEnable.value = (checkBoxBatteryFailsafe.isChecked() ? 1  : 0);
		if (pThrValue != null) {
			try {
				pThrValue.value = Double.parseDouble(editTextFspwm.getText().toString());
			} catch (Exception ex) {}
		}
		if (pBattMah != null) {
			try {
				pBattMah.value = Double.parseDouble(editTextReservedMah.getText().toString());
			} catch (Exception ex) {}
		}
		if (pGcsEnable != null)
			pGcsEnable.value = (checkBoxGcsFailsafe.isChecked() ? 1  : 0);

		if (pLowVolt != null) {
			try {
				pLowVolt.value = Double.parseDouble(editTextLowBattery.getText().toString());
			} catch (Exception ex) {}
		} else if (pBattVoltage != null) {
			try {
				pBattVoltage.value = Double.parseDouble(editTextLowBattery.getText().toString());
			} catch (Exception ex) {}
		}
				
		//invio
		listener.onSendFailsafeParam(pThrEnable);
		listener.onSendFailsafeParam(pBattEnable);
		listener.onSendFailsafeParam(pThrValue);
		listener.onSendFailsafeParam(pBattMah);
		listener.onSendFailsafeParam(pGcsEnable);
		listener.onSendFailsafeParam(pLowVolt);
		listener.onSendFailsafeParam(pBattVoltage);
		
		bDisableRefresh = false;
		listener.onRefreshFailsafeParams();		
	}


}
