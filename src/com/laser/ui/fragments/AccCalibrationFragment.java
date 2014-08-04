package com.laser.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.laser.VrPadStation.R;
import com.laser.utils.LaserConstants;

public class AccCalibrationFragment extends Fragment {
	

	
	private AccCalibrationListener listener;
	public interface AccCalibrationListener {
		void onStartAccCalibration();	
	}
	public void setListeners(AccCalibrationListener listener)
	{
		this.listener = listener;
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calibration_acc_settings_fragment, container, false);
		
		final Button btnAccCalib = (Button) view.findViewById(R.id.btnAccCalib);		
		btnAccCalib.setTextSize(LaserConstants.TEXT_SIZE_MEDIUM);
		btnAccCalib.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				listener.onStartAccCalibration();
			}
		});
		
		return view;
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

	

}
