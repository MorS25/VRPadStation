package com.laser.ui.fragments.preferences;


import java.util.List;

import com.MAVLink.Messages.ApmModes;
import com.laser.parameters.Parameter;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.activities.SettingsActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class QuickModesSettingsFragment extends Fragment implements OnItemSelectedListener {

	
	private VrPadStationApp app;
	
	private Spinner spinnerFlightMode1;
	private Spinner spinnerFlightMode2;
	private Spinner spinnerFlightMode3;
	private Spinner spinnerFlightMode4;
	private Spinner spinnerFlightMode5;
	private Spinner spinnerFlightMode6;
	private Button btnSaveModes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		this.app = (VrPadStationApp)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.quick_modes_settings_fragment, container, false);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ApmModes.getModeList(app.drone.getType()));
		
		spinnerFlightMode1 = (Spinner) v.findViewById(R.id.spinnerFlightMode1);
		spinnerFlightMode1.setAdapter(adapter);
		spinnerFlightMode1.setOnItemSelectedListener(this);

		spinnerFlightMode2 = (Spinner) v.findViewById(R.id.spinnerFlightMode2);
		spinnerFlightMode2.setAdapter(adapter);
		spinnerFlightMode2.setOnItemSelectedListener(this);

		spinnerFlightMode3 = (Spinner) v.findViewById(R.id.spinnerFlightMode3);
		spinnerFlightMode3.setAdapter(adapter);
		spinnerFlightMode3.setOnItemSelectedListener(this);

		spinnerFlightMode4 = (Spinner) v.findViewById(R.id.spinnerFlightMode4);
		spinnerFlightMode4.setAdapter(adapter);
		spinnerFlightMode4.setOnItemSelectedListener(this);

		spinnerFlightMode5 = (Spinner) v.findViewById(R.id.spinnerFlightMode5);
		spinnerFlightMode5.setAdapter(adapter);
		spinnerFlightMode5.setOnItemSelectedListener(this);

		spinnerFlightMode6 = (Spinner) v.findViewById(R.id.spinnerFlightMode6);
		spinnerFlightMode6.setAdapter(adapter);
		spinnerFlightMode6.setOnItemSelectedListener(this);
		
		btnSaveModes = (Button) v.findViewById(R.id.btnSaveModes);
		btnSaveModes.setOnClickListener(new OnClickListener() 
		{				
			@Override
			public void onClick(View v) 
			{
				List<Parameter> paramsList = app.parameterMananger.getParametersList();
				for (Parameter p : paramsList)
				{
					if (p.name.equals("FLTMODE1"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode1.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}
					else if (p.name.equals("FLTMODE2"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode2.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}
					else if (p.name.equals("FLTMODE3"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode3.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}
					else if (p.name.equals("FLTMODE4"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode4.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}
					else if (p.name.equals("FLTMODE5"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode5.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}
					else if (p.name.equals("FLTMODE6"))
					{
						p.value = ApmModes.getMode(spinnerFlightMode6.getSelectedItem().toString(), app.drone.getType()).getNumber();
						app.parameterMananger.sendParameter(p);
					}								
				}
				
				SettingsActivity.bEdited = true;
            }      
		});			
		
		spinnerFlightMode1.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE1"), app.drone.getType()).getName()));
		spinnerFlightMode2.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE2"), app.drone.getType()).getName()));
		spinnerFlightMode3.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE3"), app.drone.getType()).getName()));
		spinnerFlightMode4.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE4"), app.drone.getType()).getName()));
		spinnerFlightMode5.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE5"), app.drone.getType()).getName()));
		spinnerFlightMode6.setSelection(adapter.getPosition(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE6"), app.drone.getType()).getName()));			
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		parent.getItemAtPosition(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }

}