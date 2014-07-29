package com.laser.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
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
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.laser.parameters.Parameter;
import com.laser.VrPadStation.R;
import com.laser.ui.widgets.SpinnerAuto;
import com.laser.ui.widgets.SpinnerAuto.OnSpinnerItemSelectedListener;
import com.laser.utils.LaserConstants;

public class MagCalibrationFragment extends Fragment implements OnSpinnerItemSelectedListener,
															 OnClickListener {
	
	
	private CheckBox chkUse;
	private CheckBox chkEnabled;
	private CheckBox chkLearn;
	private CheckBox chkExternal;
	private CheckBox chkAutoDec;
	private EditText txtDecDeg;
	private EditText txtDecMin;
	private SpinnerAuto spinOrient;
	private int selectedOrient = -1;
	private Parameter pUse,
					  pEnabled, 
					  pLearn,
					  pExternal, 
					  pAutoDec,
					  pDec,
					  pOrient;
	
	private MagCalibrationListener listener;
	public interface MagCalibrationListener {
		void onStartMagCalibration();
		void onRefreshCalibrationParams();
		void onSendCalibrationParam(Parameter parameter);
		void onCheckYaw();		
	}
	public void setListeners(MagCalibrationListener listener)
	{
		this.listener = listener;
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calibration_mag_settings_fragment, container, false);		
		spinOrient = (SpinnerAuto) view.findViewById(R.id.cmp_spinOrient);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this.getActivity(),
		        R.array.cmp_items_orient,
		        android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinOrient.setAdapter(adapter2);
		spinOrient.setOnSpinnerItemSelectedListener(this);
		
		final TextView cmp_lblUse = (TextView) view.findViewById(R.id.cmp_lblUse);
		final TextView cmp_lblEnabled = (TextView) view.findViewById(R.id.cmp_lblEnabled);
		final TextView cmp_lblLearn = (TextView) view.findViewById(R.id.cmp_lblLearn);
		final TextView cmp_lblExternal = (TextView) view.findViewById(R.id.cmp_lblExternal);
		final TextView cmp_lblAutoDec = (TextView) view.findViewById(R.id.cmp_lblAutoDec);
		final TextView cmp_lblDec = (TextView) view.findViewById(R.id.cmp_lblDec);
		final TextView cmp_lblOrient = (TextView) view.findViewById(R.id.cmp_lblOrient);
		
		chkUse = (CheckBox) view.findViewById(R.id.cmp_chkUse);
		chkEnabled = (CheckBox) view.findViewById(R.id.cmp_chkEnabled);
		chkLearn = (CheckBox) view.findViewById(R.id.cmp_chkLearn);
		chkExternal = (CheckBox) view.findViewById(R.id.cmp_chkExternal);
		chkAutoDec = (CheckBox) view.findViewById(R.id.cmp_chkAutoDec);
		txtDecDeg = (EditText) view.findViewById(R.id.cmp_txtDecDeg);
		txtDecMin = (EditText) view.findViewById(R.id.cmp_txtDecMin);
		
		final Button btnRefresh = (Button) view.findViewById(R.id.cmp_btnRefresh);
		final Button btnSend = (Button) view.findViewById(R.id.cmp_btnSend);
		final Button btnCheckYaw = (Button) view.findViewById(R.id.btnCheckYaw);
		final Button btnMagCalib = (Button) view.findViewById(R.id.btnMagCalib);
		
		btnRefresh.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnCheckYaw.setOnClickListener(this);
		btnMagCalib.setOnClickListener(this);
		
		cmp_lblUse.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblEnabled.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblLearn.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblExternal.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblAutoDec.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblDec.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		cmp_lblOrient.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnRefresh.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnSend.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnCheckYaw.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnMagCalib.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
				
		return view;
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		selectedOrient = position;
	}
	
	private boolean viewCreated = false;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewCreated = true;
		listener.onRefreshCalibrationParams();
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
	
	boolean bDisableRefresh = false;
	private void SendParams()
	{
		bDisableRefresh = true;
		//leggo i valori dall'interfaccia
		if (pUse != null)
			pUse.value = ( chkUse.isChecked() ? 1  : 0);
		if (pEnabled != null)
			pEnabled.value = ( chkEnabled.isChecked() ? 1  : 0);
		if (pLearn != null)
			pLearn.value = ( chkLearn.isChecked() ? 1  : 0);
		if (pExternal != null)
			pExternal.value = ( chkExternal.isChecked() ? 1  : 0);
		if (pAutoDec != null)
			pAutoDec.value = ( chkAutoDec.isChecked() ? 1  : 0);		
		if (pDec != null)
		{
			try {
			double deg = Double.parseDouble(txtDecDeg.getText().toString());
			double min = Double.parseDouble(txtDecMin.getText().toString());
			
			pDec.value = Math.toRadians(deg + min / 60.0);
			} catch (Exception ex) {}
		}
		if (pOrient != null)
		{
			if (selectedOrient >= 0)
				pOrient.value = selectedOrient;
		}
		
		//invio
		listener.onSendCalibrationParam(pUse);
		listener.onSendCalibrationParam(pEnabled);
		listener.onSendCalibrationParam(pLearn);
		listener.onSendCalibrationParam(pExternal);
		listener.onSendCalibrationParam(pAutoDec);
		listener.onSendCalibrationParam(pDec);
		listener.onSendCalibrationParam(pOrient);
		
		bDisableRefresh = false;
		listener.onRefreshCalibrationParams();
	}
	
	
	public void processMessage(MAVLinkMessage msg) {
		if (bDisableRefresh)
			return;
		
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE && viewCreated) 
		{
			msg_param_value vv = (msg_param_value) msg;
			Parameter param = new Parameter(vv);
			
			if (param.name.equalsIgnoreCase("COMPASS_USE"))
			{
				pUse = param;
				chkUse.setChecked(pUse.value != 0);
			}
			if (param.name.equalsIgnoreCase("MAG_ENABLE"))
			{
				pEnabled = param;
				chkEnabled.setChecked(pEnabled.value != 0);
			}
			if (param.name.equalsIgnoreCase("COMPASS_LEARN"))
			{
				pLearn = param;
				chkLearn.setChecked(pLearn.value != 0);
			}			
			if (param.name.equalsIgnoreCase("COMPASS_EXTERNAL"))
			{
				pExternal = param;
				chkExternal.setChecked(pExternal.value != 0);
			}
			if (param.name.equalsIgnoreCase("COMPASS_AUTODEC"))
			{
				pAutoDec = param;
				chkAutoDec.setChecked(pAutoDec.value != 0);
			}
			if (param.name.equalsIgnoreCase("COMPASS_DEC"))
			{
				pDec = param;
				double sgn = Math.signum(pDec.value);
				double adeg = Math.toDegrees(Math.abs(pDec.value));
				int deg = (int) Math.floor(adeg);
				int min = (int) Math.floor(60 * (adeg - deg));
				txtDecDeg.setText(deg + "");
				txtDecMin.setText(min + "");
			}
			if (param.name.equalsIgnoreCase("COMPASS_ORIENT"))
			{
				pOrient = param;
				selectedOrient = (int)pOrient.value;
				if (spinOrient != null)
					spinOrient.setSelection(selectedOrient);				
			}
		}
	}
		
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.cmp_btnSend:
			SendParams();
			break;
		case R.id.cmp_btnRefresh:
			listener.onRefreshCalibrationParams();
			break;
		case R.id.btnCheckYaw:
			listener.onCheckYaw();
			break;
		case R.id.btnMagCalib:
			listener.onStartMagCalibration();
			break;
		}
	}
	

}
