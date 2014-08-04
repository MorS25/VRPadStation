package com.laser.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.laser.MAVLink.Drone;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.widgets.HUDwidget;

public class HudFragment extends Fragment {
	
	private Drone drone;
	private HUDwidget hudWidget;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.hud_fragment, container, false);		
		hudWidget = (HUDwidget) view.findViewById(R.id.hudWidget);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.drone = ((VrPadStationApp)getActivity().getApplication()).drone;
		if (drone != null)
		{
			hudWidget.setDrone(drone);
			hudWidget.onDroneUpdate();
		}
	}
	
//	public void setDrone(Drone drone)
//	{
//		if (hudWidget != null)
//		{
//			hudWidget.setDrone(drone);
//			hudWidget.onDroneUpdate();
//		}
//		else
//			this.drone = drone;
//	}

	public void changeSurface() {
		if (hudWidget != null)
			hudWidget.update();
	}
	
}
