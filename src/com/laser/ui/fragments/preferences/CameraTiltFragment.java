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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class CameraTiltFragment extends Fragment implements OnCheckedChangeListener {

	private VrPadStationApp app;
	
	private ImageView image;
	private CheckBox checkBoxStabilize;
	private CheckBox checkBoxInvert;
	
	private EditText editTextServoLimitMin;
	private EditText editTextServoLimitMax;
	private EditText editTextAngleLimitMin;
	private EditText editTextAngleLimitMax;
	
	private Spinner spinnerInputCh;
	private Spinner spinnerOutputCh;

	private Button btnSave;
	private TextView textViewName;
	
	private static final int mount_tilt = 7;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.app = (VrPadStationApp)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.camera_gimbal_attitude_fragment, container, false);
		image = (ImageView) view.findViewById(R.id.imageViewComponent);
		image.setImageDrawable(getResources().getDrawable(R.drawable.camera_gimbal_pitch1));
		
		checkBoxStabilize = (CheckBox) view.findViewById(R.id.checkBoxStabilize);
		checkBoxInvert = (CheckBox) view.findViewById(R.id.checkBoxInvert);
		
		editTextServoLimitMin = (EditText) view.findViewById(R.id.editTextServoLimitMin);
		editTextServoLimitMax = (EditText) view.findViewById(R.id.editTextServoLimitMax);
		editTextAngleLimitMin = (EditText) view.findViewById(R.id.editTextAngleLimitMin);
		editTextAngleLimitMax = (EditText) view.findViewById(R.id.editTextAngleLimitMax);
		
		spinnerInputCh = (Spinner) view.findViewById(R.id.spinnerInputCh);
		spinnerOutputCh = (Spinner) view.findViewById(R.id.spinnerOutputCh);

		btnSave = (Button) view.findViewById(R.id.btnSave);
		
		textViewName = (TextView) view.findViewById(R.id.textViewName);
		textViewName.setText("TILT");
		
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource( this.getActivity(),
		        R.array.gimbal_output_ch_items,
		        android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinnerOutputCh.setAdapter(adapter2);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( this.getActivity(),
		        R.array.gimbal_input_ch_items,
		        android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
		spinnerInputCh.setAdapter(adapter);
				
		// setto i tag
		int index = 0;
		for (int i = 1; i <=8; i++)
		{
			double value = app.parameterMananger.getParameterValue("RC" + i + "_FUNCTION");
			if (value == mount_tilt)
			{
				index = i;
				break;
			}
		}			
		spinnerOutputCh.setTag("RC" + index + "_FUNCTION");		// devo cambiarlo quando cambia l'output channel!!
		checkBoxInvert.setTag("RC" + index + "_REV");			// devo cambiarlo quando cambia l'output channel!!
		editTextServoLimitMin.setTag("RC" + index + "_MIN");	// devo cambiarlo quando cambia l'output channel!!
		editTextServoLimitMax.setTag("RC" + index + "_MAX");	// devo cambiarlo quando cambia l'output channel!!
		spinnerInputCh.setTag("MNT_RC_IN_TILT");
		checkBoxStabilize.setTag("MNT_STAB_TILT");
		editTextAngleLimitMin.setTag("MNT_ANGMIN_TIL");
		editTextAngleLimitMax.setTag("MNT_ANGMAX_TIL");

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
		
		String[] inputChannels = getResources().getStringArray(R.array.gimbal_input_ch_items);
		position = 0;
		double inputCh = app.parameterMananger.getParameterValue((String)spinnerInputCh.getTag());
		for (int i = 0; i < inputChannels.length; i++)
		{
			if (String.valueOf((int)inputCh).equalsIgnoreCase(inputChannels[i]))
			{
				position = i;
				break;
			}
		}
		spinnerInputCh.setSelection(position);
		
		double invert = app.parameterMananger.getParameterValue((String)checkBoxInvert.getTag());
		if (invert == 1)
			checkBoxInvert.setChecked(false);
		else
			checkBoxInvert.setChecked(true);
		
		double stab = app.parameterMananger.getParameterValue((String)checkBoxStabilize.getTag());
		if (stab == 0)
			checkBoxStabilize.setChecked(false);
		else
			checkBoxStabilize.setChecked(true);
		
		editTextServoLimitMin.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextServoLimitMin.getTag())));
		editTextServoLimitMax.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextServoLimitMax.getTag())));
		editTextAngleLimitMin.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextAngleLimitMin.getTag())/100));
		editTextAngleLimitMax.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextAngleLimitMax.getTag())/100));

		
		// setto i listener
//		spinnerOutputCh.setOnItemSelectedListener(this);
//		spinnerInputCh.setOnItemSelectedListener(this);
		checkBoxInvert.setOnCheckedChangeListener(this);
		checkBoxStabilize.setOnCheckedChangeListener(this);
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
		editTextAngleLimitMin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextAngleLimitMin)) {
					editTextAngleLimitMin.setTextColor(Color.WHITE);
				}else{			
					editTextAngleLimitMin.setTextColor(Color.RED);
				}
			}
		});
		editTextAngleLimitMax.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextAngleLimitMax)) {
					editTextAngleLimitMax.setTextColor(Color.WHITE);
				}else{			
					editTextAngleLimitMax.setTextColor(Color.RED);
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

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) 
	{
		if (view == checkBoxInvert)
		{
			if (isNewValueEqualToDroneParam(checkBoxInvert)) {
				checkBoxInvert.setTextColor(Color.WHITE);
			}else{			
				checkBoxInvert.setTextColor(Color.RED);
			}
		}
		else if (view == checkBoxStabilize)
		{
			if (isNewValueEqualToDroneParam(checkBoxStabilize)) {
				checkBoxStabilize.setTextColor(Color.WHITE);
			}else{			
				checkBoxStabilize.setTextColor(Color.RED);
			}
		}
	}


	public void saveParams() {		
		// Output per forza per primi!!!
		checkSpinnerOutputCh();		
		
		if (!isNewValueEqualToDroneParam(spinnerInputCh))
		{
			if (app.parameterMananger.contains((String)spinnerInputCh.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)spinnerInputCh.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						String val = spinnerInputCh.getSelectedItem().toString();
						if (val.equalsIgnoreCase("Disable"))
							p.value = 0.0;
						else
							p.value = Double.parseDouble(val);
						app.parameterMananger.getParametersList().set(index, p);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}		
		if (!isNewValueEqualToDroneParam(checkBoxStabilize))
		{
			if (app.parameterMananger.contains((String)checkBoxStabilize.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)checkBoxStabilize.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						if (checkBoxStabilize.isChecked())
							p.value = 1.0;
						else
							p.value = 0.0;
						app.parameterMananger.getParametersList().set(index, p);
						checkBoxStabilize.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(checkBoxInvert))
		{
			if (app.parameterMananger.contains((String)checkBoxInvert.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)checkBoxInvert.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						if (checkBoxInvert.isChecked())
							p.value = 1.0;
						else
							p.value = 0.0;
						app.parameterMananger.getParametersList().set(index, p);
						checkBoxInvert.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
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
		if (!isNewValueEqualToDroneParam(editTextAngleLimitMin))
		{
			if (app.parameterMananger.contains((String)editTextAngleLimitMin.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextAngleLimitMin.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextAngleLimitMin.getText().toString()) * 100;
						app.parameterMananger.getParametersList().set(index, p);
						editTextAngleLimitMin.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!isNewValueEqualToDroneParam(editTextAngleLimitMax))
		{
			if (app.parameterMananger.contains((String)editTextAngleLimitMax.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextAngleLimitMax.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextAngleLimitMax.getText().toString()) * 100;
						app.parameterMananger.getParametersList().set(index, p);
						editTextAngleLimitMax.setTextColor(Color.WHITE);
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
			if (value == mount_tilt)	
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
			p.value = mount_tilt;
			app.parameterMananger.sendParameter(p);
			app.parameterMananger.getParametersList().set(indiceParametro, p);	
			
			// aggiorno i tag
			spinnerOutputCh.setTag("RC" + nuovoCanale + "_FUNCTION");	
			checkBoxInvert.setTag("RC" + nuovoCanale + "_REV");			
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
			else if (v instanceof CheckBox)
			{
				CheckBox cb = (CheckBox)v;
				if (cb.isChecked())
				{
					double newVal = 1.0;
					return val == newVal;
				}
				else
				{
					double newVal = 0.0;
					return val == newVal;
				}
			}
			else if (v ==  spinnerInputCh)
			{
				String strNewVal = spinnerInputCh.getSelectedItem().toString();
				double newVal = val;
				if (strNewVal.equalsIgnoreCase("Disable"))
					newVal = 0;
				else
					newVal = Double.parseDouble(strNewVal);
				return val == newVal;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		return true;
	}


}
