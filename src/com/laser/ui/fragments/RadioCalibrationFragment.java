package com.laser.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.laser.parameters.Parameter;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.widgets.RangeProgressBar;
import com.laser.utils.LaserConstants;

public class RadioCalibrationFragment extends Fragment {

	private VrPadStationApp app;
	
	private RangeProgressBar progressRoll;
	private RangeProgressBar progressPitch;
	private RangeProgressBar progressThrottle;
	private RangeProgressBar progressYaw;
	private RangeProgressBar progressRadio5;
	private RangeProgressBar progressRadio6;
	private RangeProgressBar progressRadio7;
	private RangeProgressBar progressRadio8;
	private TextView txtProgressRoll;
	private TextView txtProgressPitch;
	private TextView txtProgressThrottle;
	private TextView txtProgressYaw;
	private TextView txtProgressRadio5;
	private TextView txtProgressRadio6;
	private TextView txtProgressRadio7;
	private TextView txtProgressRadio8;
	private Button btnStartCalibration;
	
	private final int def_rc_min = 800;
	private final int def_rc_max = 2200;	
	
	private boolean run = false;
	private boolean saveTrim = false;
//	private Handler handlerCalibration = new Handler();
//	private Runnable runnableCalibration = new Runnable() {		
//		@Override
//		public void run() {
//			if (run) {
//				// check for non 0 values
////                if (MainV2.comPort.MAV.cs.ch1in > 800 && MainV2.comPort.MAV.cs.ch1in < 2200)
////                {
////                    rcmin[0] = Math.Min(rcmin[0], MainV2.comPort.MAV.cs.ch1in);
////                    rcmax[0] = Math.Max(rcmax[0], MainV2.comPort.MAV.cs.ch1in);
////
////                    rcmin[1] = Math.Min(rcmin[1], MainV2.comPort.MAV.cs.ch2in);
////                    rcmax[1] = Math.Max(rcmax[1], MainV2.comPort.MAV.cs.ch2in);
////
////                    rcmin[2] = Math.Min(rcmin[2], MainV2.comPort.MAV.cs.ch3in);
////                    rcmax[2] = Math.Max(rcmax[2], MainV2.comPort.MAV.cs.ch3in);
////
////                    rcmin[3] = Math.Min(rcmin[3], MainV2.comPort.MAV.cs.ch4in);
////                    rcmax[3] = Math.Max(rcmax[3], MainV2.comPort.MAV.cs.ch4in);
////
////                    rcmin[4] = Math.Min(rcmin[4], MainV2.comPort.MAV.cs.ch5in);
////                    rcmax[4] = Math.Max(rcmax[4], MainV2.comPort.MAV.cs.ch5in);
////
////                    rcmin[5] = Math.Min(rcmin[5], MainV2.comPort.MAV.cs.ch6in);
////                    rcmax[5] = Math.Max(rcmax[5], MainV2.comPort.MAV.cs.ch6in);
////
////                    rcmin[6] = Math.Min(rcmin[6], MainV2.comPort.MAV.cs.ch7in);
////                    rcmax[6] = Math.Max(rcmax[6], MainV2.comPort.MAV.cs.ch7in);
////
////                    rcmin[7] = Math.Min(rcmin[7], MainV2.comPort.MAV.cs.ch8in);
////                    rcmax[7] = Math.Max(rcmax[7], MainV2.comPort.MAV.cs.ch8in);
////
////                    BARroll.minline = (int)rcmin[0];
////                    BARroll.maxline = (int)rcmax[0];
////
////                    BARpitch.minline = (int)rcmin[1];
////                    BARpitch.maxline = (int)rcmax[1];
////
////                    BARthrottle.minline = (int)rcmin[2];
////                    BARthrottle.maxline = (int)rcmax[2];
////
////                    BARyaw.minline = (int)rcmin[3];
////                    BARyaw.maxline = (int)rcmax[3];
////
////                    BAR5.minline = (int)rcmin[4];
////                    BAR5.maxline = (int)rcmax[4];
////
////                    BAR6.minline = (int)rcmin[5];
////                    BAR6.maxline = (int)rcmax[5];
////
////                    BAR7.minline = (int)rcmin[6];
////                    BAR7.maxline = (int)rcmax[6];
////
////                    BAR8.minline = (int)rcmin[7];
////                    BAR8.maxline = (int)rcmax[7];
////                }
//				handlerCalibration.postDelayed(runnableCalibration, 10);
//			}
//		}
//	};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		app = (VrPadStationApp) getActivity().getApplication();
		super.onCreate(savedInstanceState);
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calibration_radio_settings_fragment, container, false);

		progressRoll = (RangeProgressBar) view.findViewById(R.id.progressRoll);
		progressPitch = (RangeProgressBar) view.findViewById(R.id.progressPitch);
		progressThrottle = (RangeProgressBar) view.findViewById(R.id.progressThrottle);
		progressYaw = (RangeProgressBar) view.findViewById(R.id.progressYaw);
		progressRadio5 = (RangeProgressBar) view.findViewById(R.id.progressRadio5);
		progressRadio6 = (RangeProgressBar) view.findViewById(R.id.progressRadio6);
		progressRadio7 = (RangeProgressBar) view.findViewById(R.id.progressRadio7);
		progressRadio8 = (RangeProgressBar) view.findViewById(R.id.progressRadio8);
		txtProgressRoll = (TextView) view.findViewById(R.id.txtProgressRoll);
		txtProgressPitch = (TextView) view.findViewById(R.id.txtProgressPitch);
		txtProgressYaw = (TextView) view.findViewById(R.id.txtProgressYaw);
		txtProgressThrottle = (TextView) view.findViewById(R.id.txtProgressThrottle);
		txtProgressRadio5 = (TextView) view.findViewById(R.id.txtProgressRadio5);
		txtProgressRadio6 = (TextView) view.findViewById(R.id.txtProgressRadio6);
		txtProgressRadio7 = (TextView) view.findViewById(R.id.txtProgressRadio7);
		txtProgressRadio8 = (TextView) view.findViewById(R.id.txtProgressRadio8);
		btnStartCalibration = (Button) view.findViewById(R.id.btnStartCalibration);
		
		btnStartCalibration.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressRoll.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressPitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressYaw.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressThrottle.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressRadio5.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressRadio6.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressRadio7.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		txtProgressRadio8.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		
		txtProgressRoll.setText("Roll: " + app.settings.RC1_TRIM);
		txtProgressPitch.setText("Pitch: " + app.settings.RC2_TRIM);
		txtProgressThrottle.setText("Throttle: " + app.settings.RC3_TRIM);
		txtProgressYaw.setText("Yaw: " + app.settings.RC4_TRIM);
		txtProgressRadio5.setText("Radio5: " + app.settings.RC5_TRIM);
		txtProgressRadio6.setText("Radio6: " + app.settings.RC6_TRIM);
		txtProgressRadio7.setText("Radio7: " + app.settings.RC7_TRIM);
		txtProgressRadio8.setText("Radio8: " + app.settings.RC8_TRIM);
				
		progressRoll.setBounds(def_rc_max, def_rc_min);
		progressPitch.setBounds(def_rc_max, def_rc_min);
		progressThrottle.setBounds(def_rc_max, def_rc_min);
		progressYaw.setBounds(def_rc_max, def_rc_min);
		progressRadio5.setBounds(def_rc_max, def_rc_min);
		progressRadio6.setBounds(def_rc_max, def_rc_min);
		progressRadio7.setBounds(def_rc_max, def_rc_min);
		progressRadio8.setBounds(def_rc_max, def_rc_min);
		setRangeBarShowRange(false);
		
		initArrays();
		
		btnStartCalibration.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
	            if (run) {
	            	btnStartCalibration.setText("Completed");
	                run = false;
	                saveTrim = true;
	                calibrationCompletedDialog();
	                return;
	            }
				calibrationAlertDialog();
			}
		});
	
		return view;
	}	

	private void initArrays() {
		rcmin = new int[8];
		rcmax = new int[8];
		rctrim = new int[8];
        for (int a = 0; a < rcmin.length; a++)
        {
            rcmin[a] = 3000;
            rcmax[a] = 0;
            rctrim[a] = 1500;
        }
	}
	
	private void setRangeBarShowRange(boolean showRange) {
		progressRoll.showRange(showRange);
		progressPitch.showRange(showRange);
		progressThrottle.showRange(showRange);
		progressYaw.showRange(showRange);
		progressRadio5.showRange(showRange);
		progressRadio6.showRange(showRange);
		progressRadio7.showRange(showRange);
		progressRadio8.showRange(showRange);
	}
	
	private void calibrationAlertDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setMessage("Ensure your transmitter is on and receiver is powered and connected.\nEnsure your motor does not have power/no props!!!");
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				app.setupMavlinkStreamRateForRadioCalibration();
				btnStartCalibration.setText("Click when done.");
				launchCalibrationDialog();
			}
		});
		alertDialog.show();
	}
	
	private void launchCalibrationDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setMessage("Click OK and move all RC sticks and switches to their extreme\npositions so the red bars hit the limits.");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				run = true;
				initArrays();
//				handlerCalibration.postDelayed(runnableCalibration, 10);
				setRangeBarShowRange(true);
			}
		});
		alertDialog.show();
	}
	
	private void calibrationCompletedDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setMessage("Ensure all your sticks are centered and throttle is down, and click ok to continue.");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				saveTrim = false;
				launchSummaryDialog();
			}
		});
		alertDialog.show();
	}
	
	private void launchSummaryDialog() {
		if (app.MAVClient.isConnected()) {
			app.parameterMananger.sendParameter(new Parameter("RC1_MIN", (double) rcmin[0]));
			app.parameterMananger.sendParameter(new Parameter("RC2_MIN", (double) rcmin[1]));
			app.parameterMananger.sendParameter(new Parameter("RC3_MIN", (double) rcmin[2]));
			app.parameterMananger.sendParameter(new Parameter("RC4_MIN", (double) rcmin[3]));
			app.parameterMananger.sendParameter(new Parameter("RC5_MIN", (double) rcmin[4]));
			app.parameterMananger.sendParameter(new Parameter("RC6_MIN", (double) rcmin[5]));
			app.parameterMananger.sendParameter(new Parameter("RC7_MIN", (double) rcmin[6]));
			app.parameterMananger.sendParameter(new Parameter("RC8_MIN", (double) rcmin[7]));
	
			app.parameterMananger.sendParameter(new Parameter("RC1_MAX", (double) rcmax[0]));
			app.parameterMananger.sendParameter(new Parameter("RC2_MAX", (double) rcmax[1]));
			app.parameterMananger.sendParameter(new Parameter("RC3_MAX", (double) rcmax[2]));
			app.parameterMananger.sendParameter(new Parameter("RC4_MAX", (double) rcmax[3]));
			app.parameterMananger.sendParameter(new Parameter("RC5_MAX", (double) rcmax[4]));
			app.parameterMananger.sendParameter(new Parameter("RC6_MAX", (double) rcmax[5]));
			app.parameterMananger.sendParameter(new Parameter("RC7_MAX", (double) rcmax[6]));
			app.parameterMananger.sendParameter(new Parameter("RC8_MAX", (double) rcmax[7]));
			
			app.parameterMananger.sendParameter(new Parameter("RC1_TRIM", (double) rctrim[0]));
			app.parameterMananger.sendParameter(new Parameter("RC2_TRIM", (double) rctrim[1]));
			app.parameterMananger.sendParameter(new Parameter("RC3_TRIM", (double) rctrim[2]));
			app.parameterMananger.sendParameter(new Parameter("RC4_TRIM", (double) rctrim[3]));
			app.parameterMananger.sendParameter(new Parameter("RC5_TRIM", (double) rctrim[4]));
			app.parameterMananger.sendParameter(new Parameter("RC6_TRIM", (double) rctrim[5]));
			app.parameterMananger.sendParameter(new Parameter("RC7_TRIM", (double) rctrim[6]));
			app.parameterMananger.sendParameter(new Parameter("RC8_TRIM", (double) rctrim[7]));
		
			String result = "\nCH1: " + rcmin[0] + " | " + rcmax[0] + " Trim1: " + rctrim[0] +
							"\nCH2: " + rcmin[1] + " | " + rcmax[1] + " Trim2: " + rctrim[1] +
							"\nCH3: " + rcmin[2] + " | " + rcmax[2] + " Trim3: " + rctrim[2] +
							"\nCH4: " + rcmin[3] + " | " + rcmax[3] + " Trim4: " + rctrim[3] +
							"\nCH5: " + rcmin[4] + " | " + rcmax[4] + " Trim5: " + rctrim[4] +
							"\nCH6: " + rcmin[5] + " | " + rcmax[5] + " Trim6: " + rctrim[5] +
							"\nCH7: " + rcmin[6] + " | " + rcmax[6] + " Trim7: " + rctrim[6] +
							"\nCH8: " + rcmin[7] + " | " + rcmax[7] + " Trim8: " + rctrim[7];
			
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.setMessage("Results:\n(Normal values are around 1100 | 1900)\n" + result);
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					setRangeBarShowRange(false);
					btnStartCalibration.setText("Start Calibration");
					app.resetMavlinkStreamRate();
					app.channelManager.updateChannels(app.settings, -1);
				}
			});
			alertDialog.show();
		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.setMessage("MAVLink not connected!");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					setRangeBarShowRange(false);
					btnStartCalibration.setText("Start Calibration");
					app.resetMavlinkStreamRate();
				}
			});
		}
	}
		
	private boolean viewCreated = false;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewCreated = true;
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
	
	
	private int[] rcmin = new int[8];
	private int[] rcmax = new int[8];
	private int[] rctrim = new int[8];
	public void processMessage(MAVLinkMessage msg) {
		if (viewCreated && msg.msgid ==  msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW)
		{
			msg_rc_channels_raw m_rc = (msg_rc_channels_raw) msg;
			
			txtProgressRoll.setText("Roll: " + m_rc.chan1_raw);
			txtProgressPitch.setText("Pitch: " + m_rc.chan2_raw);
			txtProgressThrottle.setText("Throttle: " + m_rc.chan3_raw);
			txtProgressYaw.setText("Yaw: " + m_rc.chan4_raw);
			txtProgressRadio5.setText("Radio5: " + m_rc.chan5_raw);
			txtProgressRadio6.setText("Radio6: " + m_rc.chan6_raw);
			txtProgressRadio7.setText("Radio7: " + m_rc.chan7_raw);
			txtProgressRadio8.setText("Radio8: " + m_rc.chan8_raw);
			
			progressRoll.setProgress(m_rc.chan1_raw);
			progressPitch.setProgress(m_rc.chan2_raw);
			progressThrottle.setProgress(m_rc.chan3_raw);
			progressYaw.setProgress(m_rc.chan4_raw);
			progressRadio5.setProgress(m_rc.chan5_raw);
			progressRadio6.setProgress(m_rc.chan6_raw);
			progressRadio7.setProgress(m_rc.chan7_raw);
			progressRadio8.setProgress(m_rc.chan8_raw);
			
			if (run) {
				rcmin[0] = Math.min(rcmin[0], m_rc.chan1_raw);
				rcmax[0] = Math.max(rcmax[0], m_rc.chan1_raw);

				rcmin[1] = Math.min(rcmin[1], m_rc.chan2_raw);
				rcmax[1] = Math.max(rcmax[1], m_rc.chan2_raw);

				rcmin[2] = Math.min(rcmin[2], m_rc.chan3_raw);
				rcmax[2] = Math.max(rcmax[2], m_rc.chan3_raw);

				rcmin[3] = Math.min(rcmin[3], m_rc.chan4_raw);
				rcmax[3] = Math.max(rcmax[3], m_rc.chan4_raw);

				rcmin[4] = Math.min(rcmin[4], m_rc.chan5_raw);
				rcmax[4] = Math.max(rcmax[4], m_rc.chan5_raw);

				rcmin[5] = Math.min(rcmin[5], m_rc.chan6_raw);
				rcmax[5] = Math.max(rcmax[5], m_rc.chan6_raw);

				rcmin[6] = Math.min(rcmin[6], m_rc.chan7_raw);
				rcmax[6] = Math.max(rcmax[6], m_rc.chan7_raw);

				rcmin[7] = Math.min(rcmin[7], m_rc.chan8_raw);
				rcmax[7] = Math.max(rcmax[7], m_rc.chan8_raw);
			} else if (saveTrim) {
				rctrim[0] = m_rc.chan1_raw;
				rctrim[1] = m_rc.chan2_raw;
				rctrim[2] = m_rc.chan3_raw;
				rctrim[3] = m_rc.chan4_raw;
				rctrim[4] = m_rc.chan5_raw;
				rctrim[5] = m_rc.chan6_raw;
				rctrim[6] = m_rc.chan7_raw;
				rctrim[7] = m_rc.chan8_raw;				
			}
			
		}
	}
	

}
