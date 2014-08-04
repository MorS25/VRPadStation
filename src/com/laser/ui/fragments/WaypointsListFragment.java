package com.laser.ui.fragments;


import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.MAVLink.waypoint;
import com.laser.MAVLink.Drone;
import com.laser.VrPadStation.R;
import com.laser.ui.layers.MapLayer;
import com.laser.ui.widgets.WaypointsAdapter;
import com.laser.utils.LaserConstants;
import com.laser.utils.LaserConstants.MapClickModes;

public class WaypointsListFragment extends Fragment implements OnClickListener {
	

	private ListView listViewWaypoints;
	private WaypointsAdapter waypointsAdapter;
	private List<waypoint> listWaypoints = new ArrayList<waypoint>();

	private Switch switchToggleWaypointMode;
	private Switch switchTogglePolygonMode;
	private Button btnSetModeAuto;
	
	
	private OnListWaypointsListener listener;
	public interface OnListWaypointsListener {
		public void onListWaypointsClick(int position);
		public void onListWaypointsLongClick(int position);
		public void setModeAuto();
		public void enablePolygonMode(boolean polygonModeEnabled);
		public void addWaypointMode(boolean enabled);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.waypoints_fragment3, container, false);
		switchToggleWaypointMode = (Switch) view.findViewById(R.id.switchToggleWaypointMode);
		switchTogglePolygonMode = (Switch) view.findViewById(R.id.switchTogglePolygonMode);	
		btnSetModeAuto = (Button) view.findViewById(R.id.btnSetModeAuto);
		
		switchTogglePolygonMode.setChecked(false);
		switchTogglePolygonMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked && !switchToggleWaypointMode.isChecked())
					switchToggleWaypointMode.setChecked(true);
				listener.enablePolygonMode(isChecked);		
			}
		});
		switchToggleWaypointMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
				{
					LaserConstants.MAP_CLICK_MODE = MapClickModes.ADD_WAYPOINTS;
					listener.addWaypointMode(true);
				}
				else
				{
					if (!isChecked && switchTogglePolygonMode.isChecked())
						switchTogglePolygonMode.setChecked(false);
					LaserConstants.MAP_CLICK_MODE = MapClickModes.NONE;
					listener.addWaypointMode(false);
				}					
			}
		});
		
		waypointsAdapter = new WaypointsAdapter(getActivity(), R.layout.waypoint_list_item, listWaypoints);
		listViewWaypoints = (ListView) (view.findViewById(R.id.listViewWaypoints));
		listViewWaypoints.setAdapter(waypointsAdapter);		
		listViewWaypoints.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				listener.onListWaypointsLongClick(position);
				return true;
			}
		});
		listViewWaypoints.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				listener.onListWaypointsClick(position);
			}
		});
		
		switchToggleWaypointMode.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		switchTogglePolygonMode.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		btnSetModeAuto.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		btnSetModeAuto.setOnClickListener(this);
		
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		update();
	}

	private Drone drone;
	public void setListeners(MapLayer listener, Drone drone)
	{
		this.drone = drone;
		this.listener = listener;
	}

	public void update() {
		if (waypointsAdapter != null && drone != null)
		{
			waypointsAdapter.clear();
			waypointsAdapter.add(drone.getHome());
			waypointsAdapter.addAll(drone.getWaypoints());
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnSetModeAuto)
			listener.setModeAuto();
//		else if (v == btnDeleteWp) {
//			if (selectedWp > 0) {
//				drone.getWaypoints().remove(selectedWp - 1);
//				update();
//				listener.onRemoveWaypoint();
//			}
//		} else if (v == btnChangeWpAltitude) {
//			if (selectedWp > 0) {
//				listener.onChangeWpAlt(drone.getWaypoints().get(selectedWp - 1).coord);
//			}
//		}
	}

	@Override
	public void onDestroyView() {
		if (switchToggleWaypointMode.isChecked())
			switchToggleWaypointMode.setChecked(false);
		super.onDestroyView();
	}

	public void updateSwitch() {
		if (LaserConstants.MAP_CLICK_MODE == MapClickModes.ADD_WAYPOINTS)
			switchToggleWaypointMode.setChecked(true);
		else
			switchToggleWaypointMode.setChecked(false);
	}
}
