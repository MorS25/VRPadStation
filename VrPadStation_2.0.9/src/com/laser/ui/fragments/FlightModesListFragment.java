package com.laser.ui.fragments;


import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.laser.MAVLink.Drone;
import com.laser.VrPadStation.R;
import com.laser.ui.layers.MapLayer;
import com.laser.ui.widgets.FlightModesAdapter;

public class FlightModesListFragment extends Fragment {
	

	private ListView listViewFlightModes;
	private FlightModesAdapter flightModesAdapter; 
	private List<String> flightModes = new ArrayList<String>();
	private ApmModes currMode = ApmModes.UNKNOWN;
	
	private OnListFlightModeListener mListener;
	public interface OnListFlightModeListener {
		public void onListFlightModeClick(String mode);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.flightmodes_fragment, container, false);
		flightModesAdapter = new FlightModesAdapter(getActivity(), R.layout.flight_mode_list_item, flightModes);
		listViewFlightModes = (ListView) (view.findViewById(R.id.listViewFlightModes));
		listViewFlightModes.setAdapter(flightModesAdapter);		
		listViewFlightModes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long rowId) {
				mListener.onListFlightModeClick(flightModes.get(position));
			}
		});
		return view;
	}

	public void setListeners(MapLayer listener)
	{
		mListener = listener;
	}
	
	public void initialize(Drone drone) {
        flightModes = ApmModes.getModeList(drone.getType());        
		if (flightModesAdapter != null)
		{
			flightModesAdapter.clear();
			flightModesAdapter.addAll(flightModes);
		}
	}
	
	public void processMessage(MAVLinkMessage m, Drone drone) 
	{
		if (m.msgid == msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT && viewCreated) 
		{
			currMode = drone.getMode();
			flightModesAdapter.setBackgroundColor(currMode.getName());
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


}
