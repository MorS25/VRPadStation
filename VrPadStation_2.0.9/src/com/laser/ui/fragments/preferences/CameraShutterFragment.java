package com.laser.ui.fragments.preferences;


import com.laser.parameters.Parameter;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CameraShutterFragment extends Fragment {

	private VrPadStationApp app;
	
	private EditText editTextServoLimitMin;
	private EditText editTextServoLimitMax;
	private EditText editTextPushed;
	private EditText editTextNotPushed;
	private EditText editTextDuration;
	
	private Spinner spinnerOutputCh;

	private Button btnSave;
	
	private static final int camera_trigger = 10;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.app = (VrPadStationApp)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.camera_gimbal_shutter_fragment, container, false);
		
		editTextServoLimitMin = (EditText) view.findViewById(R.id.editTextServoLimitMin);
		editTextServoLimitMax = (EditText) view.findViewById(R.id.editTextServoLimitMax);
		editTextPushed = (EditText) view.findViewById(R.id.editTextPushed);
		editTextNotPushed = (EditText) view.findViewById(R.id.editTextNotPushed);
		editTextDuration = (EditText) view.findViewById(R.id.editTextDuration);
		
		spinnerOutputCh = (Spinner) view.findViewById(R.id.spinnerOutputCh);

		btnSave = (Button) view.findViewById(R.id.btnSave);
		
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this.getActivity(),
		        R.array.gimbal_output_ch_items,
		        android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinnerOutputCh.setAdapter(adapter2);
				
		// setto i tag
		int index = 0;
		for (int i = 1; i <=8; i++)
		{
			double value = app.parameterMananger.getParameterValue("RC" + i + "_FUNCTION");
			if (value == camera_trigger)
			{
				index = i;
				break;
			}
		}			
		spinnerOutputCh.setTag("RC" + index + "_FUNCTION");		// devo cambiarlo quando cambia l'output channel!!
		editTextServoLimitMin.setTag("RC" + index + "_MIN");	// devo cambiarlo quando cambia l'output channel!!
		editTextServoLimitMax.setTag("RC" + index + "_MAX");	// devo cambiarlo quando cambia l'output channel!!
		editTextPushed.setTag("CAM_SERVO_ON");
		editTextNotPushed.setTag("CAM_SERVO_OFF");
		editTextDuration.setTag("CAM_DURATION");

		// setto i valori
		String[] outputChannels = getResources().getStringArray(R.array.gimbal_output_ch_items);
		int position = 0;
		int outputCh = index;
		for (int i = 0; i < outputChannels.length; i++)
		{
			if (String.valueOf((int)outputCh).equalsIgnoreCase(outputChannels[i]))
			{
				position = i;
				break;
			}
		}
		spinnerOutputCh.setSelection(position);		
		
		editTextServoLimitMin.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextServoLimitMin.getTag())));
		editTextServoLimitMax.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextServoLimitMax.getTag())));
		editTextPushed.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextPushed.getTag())));
		editTextNotPushed.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextNotPushed.getTag())));
		editTextDuration.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextDuration.getTag())));
		
		// setto i listener
		editTextServoLimitMin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextServoLimitMin)) {
					editTextServoLimitMin.setTextColor(Color.WHITE);
				}else{			
					editTextServoLimitMin.setTextColor(Color.RED);
				}
			}
		});
		editTextServoLimitMax.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextServoLimitMax)) {
					editTextServoLimitMax.setTextColor(Color.WHITE);
				}else{			
					editTextServoLimitMax.setTextColor(Color.RED);
				}
			}
		});
		editTextPushed.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextPushed)) {
					editTextPushed.setTextColor(Color.WHITE);
				}else{			
					editTextPushed.setTextColor(Color.RED);
				}
			}
		});
		editTextNotPushed.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextNotPushed)) {
					editTextNotPushed.setTextColor(Color.WHITE);
				}else{			
					editTextNotPushed.setTextColor(Color.RED);
				}
			}
		});		
		editTextDuration.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextDuration)) {
					editTextDuration.setTextColor(Color.WHITE);
				}else{			
					editTextDuration.setTextColor(Color.RED);
				}
			}
		});
		
		btnSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveParams();
			}
		});
		
		return view;
	}


	public void saveParams() {		
		// Output per forza per primi!!!
		checkSpinnerOutputCh();		
		
		if (!isNewValueEqualToDroneParam(editTextServoLimitMin))
		{
			if (app.parameterMananger.contains((String)editTextServoLimitMin.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextServoLimitMin.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextServoLimitMin.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextServoLimitMin.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(editTextServoLimitMax))
		{
			if (app.parameterMananger.contains((String)editTextServoLimitMax.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextServoLimitMax.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextServoLimitMax.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextServoLimitMax.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(editTextPushed))
		{
			if (app.parameterMananger.contains((String)editTextPushed.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextPushed.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextPushed.getText().toString()) * 100;
						app.parameterMananger.getParametersList().set(index, p);
						editTextPushed.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(editTextNotPushed))
		{
			if (app.parameterMananger.contains((String)editTextNotPushed.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextNotPushed.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextNotPushed.getText().toString()) * 100;
						app.parameterMananger.getParametersList().set(index, p);
						editTextNotPushed.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(editTextDuration))
		{
			if (app.parameterMananger.contains((String)editTextDuration.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextDuration.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextDuration.getText().toString()) * 100;
						app.parameterMananger.getParametersList().set(index, p);
						editTextDuration.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void checkSpinnerOutputCh() 
	{
		// leggo il nuovo canale
		int nuovoCanale = 0;
		String spinnerVal = spinnerOutputCh.getSelectedItem().toString();
		if (spinnerVal.equalsIgnoreCase("Disable"))
			nuovoCanale = 0;
		else
			nuovoCanale = (int) Double.parseDouble(spinnerVal);
		
		// azzero il vecchio canale
		for (int i = 1; i <=8; i++)
		{
			double value = app.parameterMananger.getParameterValue("RC" + i + "_FUNCTION");
			if (value == camera_trigger)	
			{
				int indiceParametro = app.parameterMananger.indexOf("RC" + i + "_FUNCTION");
				Parameter p = app.parameterMananger.getParametersList().get(indiceParametro);
				p.value = 0;
				app.parameterMananger.sendParameter(p);
				app.parameterMananger.getParametersList().set(indiceParametro, p);				
				break;
			}
		}			
		
		// setto il nuovo canale
		int indiceParametro = app.parameterMananger.indexOf("RC" + nuovoCanale + "_FUNCTION");
		if (indiceParametro >= 0 && indiceParametro < app.parameterMananger.getParametersList().size())
		{
			Parameter p = app.parameterMananger.getParametersList().get(indiceParametro);
			p.value = camera_trigger;
			app.parameterMananger.sendParameter(p);
			app.parameterMananger.getParametersList().set(indiceParametro, p);	
			
			// aggiorno i tag
			spinnerOutputCh.setTag("RC" + nuovoCanale + "_FUNCTION");	
			editTextServoLimitMin.setTag("RC" + nuovoCanale + "_MIN");	
			editTextServoLimitMax.setTag("RC" + nuovoCanale + "_MAX");
		}
	}

	public boolean isNewValueEqualToDroneParam(View v) 
	{	
		// Non controllo lo spinner output channel perchè funziona diversamente.
		// è legato al nome del parametro e non al valore
		try {
			String name = (String)v.getTag();
			double val = app.parameterMananger.getParameterValue(name);
			if (v instanceof EditText)
			{
				EditText et = (EditText)v;
				double newVal = Double.valueOf(et.getText().toString());
				return val == newVal;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		return true;
	}


}
