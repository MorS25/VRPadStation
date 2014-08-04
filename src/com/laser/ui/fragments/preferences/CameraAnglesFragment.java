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
import android.widget.Button;
import android.widget.EditText;

public class CameraAnglesFragment extends Fragment implements OnClickListener {

	private VrPadStationApp app;
	
	private EditText editTextRetractX;
	private EditText editTextRetractY;
	private EditText editTextRetractZ;
	private EditText editTextNeutralX;
	private EditText editTextNeutralY;
	private EditText editTextNeutralZ;
	private EditText editTextControlX;
	private EditText editTextControlY;
	private EditText editTextControlZ;
	
	private Button btnSave;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.app = (VrPadStationApp)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.camera_gimbal_angles_fragment, container, false);
		
		btnSave = (Button) v.findViewById(R.id.btnSave);

    	btnSave.setOnClickListener(this);;
		
		editTextRetractX = (EditText) v.findViewById(R.id.editTextRetractX);
		editTextRetractY = (EditText) v.findViewById(R.id.editTextRetractY);
		editTextRetractZ = (EditText) v.findViewById(R.id.editTextRetractZ);
		editTextNeutralX = (EditText) v.findViewById(R.id.editTextNeutralX);
		editTextNeutralY = (EditText) v.findViewById(R.id.editTextNeutralY);
		editTextNeutralZ = (EditText) v.findViewById(R.id.editTextNeutralZ);
		editTextControlX = (EditText) v.findViewById(R.id.editTextControlX);
		editTextControlY = (EditText) v.findViewById(R.id.editTextControlY);
		editTextControlZ = (EditText) v.findViewById(R.id.editTextControlZ);
				
		editTextRetractX.setTag("MNT_RETRACT_X");
		editTextRetractY.setTag("MNT_RETRACT_Y");
		editTextRetractZ.setTag("MNT_RETRACT_Z");
		editTextNeutralX.setTag("MNT_NEUTRAL_X");
		editTextNeutralY.setTag("MNT_NEUTRAL_Y");
		editTextNeutralZ.setTag("MNT_NEUTRAL_Z");
		editTextControlX.setTag("MNT_CONTROL_X");
		editTextControlY.setTag("MNT_CONTROL_Y");
		editTextControlZ.setTag("MNT_CONTROL_Z");
				
		editTextRetractX.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextRetractX.getTag())));
		editTextRetractY.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextRetractY.getTag())));
		editTextRetractZ.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextRetractZ.getTag())));		
		editTextNeutralX.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextNeutralX.getTag())));
		editTextNeutralY.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextNeutralY.getTag())));
		editTextNeutralZ.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextNeutralZ.getTag())));
		editTextControlX.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextControlX.getTag())));
		editTextControlY.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextControlY.getTag())));
		editTextControlZ.setText(String.valueOf(app.parameterMananger.getParameterValue((String)editTextControlZ.getTag())));
		
		editTextRetractX.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextRetractX)) {
					editTextRetractX.setTextColor(Color.WHITE);
				}else{			
					editTextRetractX.setTextColor(Color.RED);
				}
			}
		});
		editTextRetractY.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextRetractY)) {
					editTextRetractY.setTextColor(Color.WHITE);
				}else{			
					editTextRetractY.setTextColor(Color.RED);
				}
			}
		});
		editTextRetractZ.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextRetractZ)) {
					editTextRetractZ.setTextColor(Color.WHITE);
				}else{			
					editTextRetractZ.setTextColor(Color.RED);
				}
			}
		});
		editTextNeutralX.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextNeutralX)) {
					editTextNeutralX.setTextColor(Color.WHITE);
				}else{			
					editTextNeutralX.setTextColor(Color.RED);
				}
			}
		});
		editTextNeutralY.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextNeutralY)) {
					editTextNeutralY.setTextColor(Color.WHITE);
				}else{			
					editTextNeutralY.setTextColor(Color.RED);
				}
			}
		});
		editTextNeutralZ.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextNeutralZ)) {
					editTextNeutralZ.setTextColor(Color.WHITE);
				}else{			
					editTextNeutralZ.setTextColor(Color.RED);
				}
			}
		});
		editTextControlX.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextControlX)) {
					editTextControlX.setTextColor(Color.WHITE);
				}else{			
					editTextControlX.setTextColor(Color.RED);
				}
			}
		});
		editTextControlY.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextControlY)) {
					editTextControlY.setTextColor(Color.WHITE);
				}else{			
					editTextControlY.setTextColor(Color.RED);
				}
			}
		});
		editTextControlZ.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (isNewValueEqualToDroneParam(editTextControlZ)) {
					editTextControlZ.setTextColor(Color.WHITE);
				}else{			
					editTextControlZ.setTextColor(Color.RED);
				}
			}
		});

		return v;
	}
	

	@Override
	public void onClick(View v) {
		if (v == btnSave)
			saveParams();
	}

	public boolean isNewValueEqualToDroneParam(EditText et) {
		try {
			String name = (String)et.getTag();
			double val = app.parameterMananger.getParameterValue(name);
			double newVal = Double.valueOf(et.getText().toString());
			return val == newVal;
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
	}
	
	private void saveParams()
	{
		if (!isNewValueEqualToDroneParam(editTextRetractX))
		{
			if (app.parameterMananger.contains((String)editTextRetractX.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextRetractX.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextRetractX.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextRetractX.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} 
		if (!isNewValueEqualToDroneParam(editTextRetractY))
		{
			if (app.parameterMananger.contains((String)editTextRetractY.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextRetractY.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextRetractY.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextRetractY.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} 
		if (!isNewValueEqualToDroneParam(editTextRetractZ))
		{
			if (app.parameterMananger.contains((String)editTextRetractZ.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextRetractZ.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextRetractZ.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextRetractZ.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} 
		if (!isNewValueEqualToDroneParam(editTextNeutralX))
		{
			if (app.parameterMananger.contains((String)editTextNeutralX.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextNeutralX.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextNeutralX.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextNeutralX.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} 
		if (!isNewValueEqualToDroneParam(editTextNeutralY))
		{
			if (app.parameterMananger.contains((String)editTextNeutralY.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextNeutralY.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextNeutralY.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextNeutralY.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
  		} 
		if (!isNewValueEqualToDroneParam(editTextNeutralZ))
  		{
			if (app.parameterMananger.contains((String)editTextNeutralZ.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextNeutralZ.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextNeutralZ.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextNeutralZ.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
  		} 
		if (!isNewValueEqualToDroneParam(editTextControlX))
  		{
			if (app.parameterMananger.contains((String)editTextControlX.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextControlX.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextControlX.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextControlX.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
  		} 
		if (!isNewValueEqualToDroneParam(editTextControlY))
  		{
			if (app.parameterMananger.contains((String)editTextControlY.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextControlY.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextControlY.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextControlY.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
  		} 
		if (!isNewValueEqualToDroneParam(editTextControlZ))
  		{
			if (app.parameterMananger.contains((String)editTextControlZ.getTag()))
			{
				try {
					int index = app.parameterMananger.indexOf((String)editTextControlZ.getTag());
					if (index >= 0 && index < app.parameterMananger.getParametersList().size())
					{
						Parameter p = app.parameterMananger.getParametersList().get(index);
						p.value = Double.parseDouble(editTextControlZ.getText().toString());
						app.parameterMananger.getParametersList().set(index, p);
						editTextControlZ.setTextColor(Color.WHITE);
						app.parameterMananger.sendParameter(p);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
  		}
	}

}

