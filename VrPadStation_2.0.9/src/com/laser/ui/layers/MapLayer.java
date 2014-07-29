package com.laser.ui.layers;

import java.util.List;
import java.util.Vector;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.laser.MAVLink.Drone;
import com.google.android.gms.maps.model.LatLng;
import com.laser.VrPadStation.R;
import com.laser.airports.Airport;
import com.laser.app.VrPadStationApp;
import com.laser.helpers.Polygon;
import com.laser.ui.fragments.CustomMapFragment;
import com.laser.ui.fragments.FlightModesListFragment;
import com.laser.ui.fragments.HudFragment;
import com.laser.ui.fragments.OverviewFragment.OverviewListener;
import com.laser.ui.fragments.FlightModesListFragment.OnListFlightModeListener;
import com.laser.ui.fragments.OverviewFragment;
import com.laser.ui.fragments.WaypointsListFragment;
import com.laser.ui.fragments.CustomMapFragment.OnFlighDataListener;
import com.laser.ui.fragments.CustomMapFragment.OnMapInteractionListener;
import com.laser.ui.fragments.WaypointsListFragment.OnListWaypointsListener;
import com.laser.ui.widgets.CustomViewPager;
import com.laser.ui.widgets.MyPagerAdapter;
import com.laser.ui.widgets.MyPagerAdapter.PagerAdapterListener;
import com.laser.utils.LaserConstants;
import com.laser.utils.LaserConstants.MapClickModes;
import com.laser.utils.LaserConstants.MissionModes;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.PagerTabStrip;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapLayer implements OnFlighDataListener,
								 OnMapInteractionListener,
								 OverviewListener,
								 OnListWaypointsListener,
								 OnListFlightModeListener,
								 PagerAdapterListener,
								 OnClickListener {

	private Activity act;
	private Context context;	
	private Drone drone;
	private Polygon polygon;
	
	private LinearLayout mapLayer;		//500
	private CustomMapFragment mapFragment;	//501
	private LinearLayout droneDataLayout;	//502
	private MyPagerAdapter mPagerAdapter;
	private CustomViewPager mPager;
	private OverviewFragment overviewFragment;
	private HudFragment hudFragment;
	private WaypointsListFragment waypointsFragment;
	private FlightModesListFragment flightModesFragment;
	private ImageView btnHideFragments;

	private ImageButton btnTakeOff;
	private ImageButton btnFollowMe;
	private ImageButton btnGuidedMode;
	private ImageButton btnLoiter;
	private ImageButton btnRtl;
	private ImageButton btnLand;
	//private LatLng guidedPoint;   
	
	private LinearLayout layoutWaypoinsButtons;
	private ImageButton btnLoadFromApm;
	private ImageButton btnLoadFromFile;
	private ImageButton btnWriteToApm;
	private ImageButton btnWriteToFile;
	private ImageButton btnClearWaypoints;
	private ImageButton btnGeneratePolygon;
	private ImageButton btnClearPolygon;	
	
	
	private MissionModes missionMode;
	
	public interface MapLayerListener{
//		public void onParamsCountReceivedFromFile(int count);
//		public void onParameterReceivedFromFile(Parameter parameter, short paramIndex);
//		public void onParametersReceivedFromFile();
//		public void onSendParameter(Parameter parameterFromRow);
		public void onSetGuidedMode(LatLng point);
		public void onSetCurrentWaypoint(short position);
		public void onChangeFlightMode(String mode);
//		public void writeParamsToApm();
//		public void writeParamsToFile();
//		public void loadParamsFromApm();
//		public void loadParamsFromFile();
//		public void onStartAccCalibration();
//		public void onStartMagCalibration();
//		public void onRefreshCalibrationParams();
//		public void onSendCalibrationParam(Parameter parameter);
		public void writeWaypointsToApm();
		public void writeWaypointsToFile();
		public void loadWaypointsFromApm();
		public void loadWaypointsFromFile();
		public void clearWaypoints();
		public void enablePolygonMode(boolean isEnabled);
		public void generatePolygon();
		public void clearPolygon();
		public void toggleArmed();
		public void clearTrack();
		public void zoom();
		public void modeFollowMe();
		public void setModeAuto();
		public void editWaypoint(LatLng point);
		public void takeOff();
		public void setAltitude(double offset);
		public void modeGuided();
		public void addWaypointMode();
	}
	
	private MapLayerListener listener;
	public void setListener(MapLayerListener listener)
	{
		this.listener = listener;
	}
    
    public MapLayer(Activity act, Drone drone, Polygon polygon, boolean forcePan) // forcePan per quando sono nella radio
    {
    	this.act = act;
    	this.context = act.getApplicationContext();
    	this.drone = drone;
    	this.polygon = polygon;
    	
    	createMapLayer(forcePan);
    }
    
    public void setVisibility(int visible)
    {
		mapLayer.setVisibility(visible);
		if (mapFragment != null)
			mapFragment.getView().setVisibility(visible);
    }

	private void createMapLayer(boolean forcePan)	
	{
		missionMode = MissionModes.MISSION;
		
		mapLayer = (LinearLayout)act.findViewById(R.id.mapLayer);
		mapFragment = ((CustomMapFragment)act.getFragmentManager().findFragmentById(R.id.flightMapFragment));
		if (mapFragment != null && mapFragment.getMap() == null)
			mapFragment = null;
		
//		if (mapFragment != null)
//			mapFragment.forcePan(forcePan);
		
		overviewFragment = new OverviewFragment();
		hudFragment = new HudFragment();
        waypointsFragment = new WaypointsListFragment();	
        flightModesFragment = new FlightModesListFragment();
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(overviewFragment);
        fragments.add(hudFragment);
        fragments.add(waypointsFragment);
        fragments.add(flightModesFragment);
        List<String> fragmentTitles = new Vector<String>();
        fragmentTitles.add("Overview");
        fragmentTitles.add("HUD");
        fragmentTitles.add("Waypoints");
        fragmentTitles.add("Flight Modes");
		mPagerAdapter = new MyPagerAdapter(act.getFragmentManager(), fragments, fragmentTitles);
		mPagerAdapter.RegisterListener(MapLayer.this);
		mPager = (CustomViewPager) act.findViewById(R.id.fragmentMain);
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(5);
		View pagerStrip = act.findViewById(R.id.pagerTabStrip);
        if (pagerStrip instanceof PagerTabStrip) {
            PagerTabStrip pagerTabStrip = (PagerTabStrip) pagerStrip;
            pagerTabStrip.setDrawFullUnderline(true);
            pagerTabStrip.setTabIndicatorColorResource(android.R.color.white);
            pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
        }
        
		btnHideFragments = (ImageView)act.findViewById(R.id.btnHidePager);
		btnTakeOff = (ImageButton) act.findViewById(R.id.btnTakeOff);
		btnFollowMe = (ImageButton) act.findViewById(R.id.btnFollowMe);
		btnGuidedMode = (ImageButton) act.findViewById(R.id.btnGuided);
		btnLoiter = (ImageButton) act.findViewById(R.id.btnLoiter);
		btnRtl = (ImageButton) act.findViewById(R.id.btnRtl);
		btnLand = (ImageButton) act.findViewById(R.id.btnLand);
		
		layoutWaypoinsButtons = (LinearLayout) act.findViewById(R.id.layoutWaypoinsButtons);
		btnGeneratePolygon = (ImageButton) act.findViewById(R.id.btnGeneratePolygon);
		btnClearPolygon = (ImageButton) act.findViewById(R.id.btnClearPolygon);
		btnLoadFromApm = (ImageButton) act.findViewById(R.id.btnLoadFromApm);
		btnLoadFromFile = (ImageButton) act.findViewById(R.id.btnLoadFromFile);
		btnWriteToApm = (ImageButton) act.findViewById(R.id.btnWriteToApm);
		btnWriteToFile = (ImageButton) act.findViewById(R.id.btnWriteToFile);
		btnClearWaypoints = (ImageButton) act.findViewById(R.id.btnClearWaypoints);	
		
		droneDataLayout = (LinearLayout)act.findViewById(R.id.droneDataLayout);
		droneDataLayout.setVisibility(View.VISIBLE);
		hudFragment.changeSurface();
				
		btnHideFragments.setImageResource(R.drawable.tab_in);
		btnHideFragments.setClickable(true);
		btnHideFragments.setOnClickListener(this);
		btnTakeOff.setOnClickListener(this);
		btnFollowMe.setOnClickListener(this);
		btnGuidedMode.setOnClickListener(this);
		btnLoiter.setOnClickListener(this);
		btnRtl.setOnClickListener(this);
		btnLand.setOnClickListener(this);
		
		btnLoadFromApm.setOnClickListener(this);
		btnLoadFromFile.setOnClickListener(this);
		btnWriteToApm.setOnClickListener(this);
		btnWriteToFile.setOnClickListener(this);
		btnClearWaypoints.setOnClickListener(this);
		btnGeneratePolygon.setOnClickListener(this);
		btnClearPolygon.setOnClickListener(this);
			
		if (mapFragment != null)
		{			
			mapFragment.updateHomeToMap(drone);	
			mapFragment.setListeners(this);
		}
		
		overviewFragment.setListeners(this);
		waypointsFragment.setListeners(this, drone);
		flightModesFragment.setListeners(this);

        update();
	}	

	private void resizeMap(float weight) {
		FrameLayout mapContainer = (FrameLayout) act.findViewById(R.id.mapContainer);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
		mapContainer.setLayoutParams(lp);
	}

	public void receiveData(MAVLinkMessage m) {
		if (mapFragment != null)
			mapFragment.receiveData(m, drone);
		
		overviewFragment.receivedData();
	}

	public void onWaypointsReceived() {
		update();			
		if (mapFragment != null)
		{
			mapFragment.updateHomeToMap(drone);
			mapFragment.zoomToExtents(drone.getAllCoordinates());
		}
	}
	
//	public void updateCalibrationFragment(MAVLinkMessage m)
//	{
//		calibrationFragment.processMessage(m);
//	}

	public void updateOverviewFragment() {
		overviewFragment.updateArmButton(drone.isArmed());
	}
	
	public void updateFlightModesFragment(MAVLinkMessage m)
	{
		flightModesFragment.processMessage(m, drone);
	}
	
//	public void onParametersReceived()
//	{
//		parametersFragment.onParametersReceived();
//	}
//
//	public void onParameterReceived(Parameter parameter, short paramIndex) {
//		parametersFragment.onParameterReceived(parameter, paramIndex);
//	}
//	
//	public void onParamsCountReceived(int count)
//	{
//		parametersFragment.setProgressBarMax(count);
//	}

	public void onDestroy() {
		if (mPagerAdapter != null)
			mPagerAdapter.UnRegisterListener(MapLayer.this);
	}

	public void waypointFileLoaded() {
		update();			
		if (mapFragment != null)
			mapFragment.zoomToExtents(drone.getAllCoordinates());
	}

	public void checkIntent() {
		update();
		if (mapFragment != null)
			mapFragment.zoomToExtents(drone.getAllCoordinates());
	}
	
//	public void saveParametersToFile(List<Parameter> parameterList) {
//		if (parameterList.size()>0) {
//			ParameterWriter parameterWriter = new ParameterWriter(parameterList);
//			if(parameterWriter.saveParametersToFile()){
//				Toast.makeText(context, "Parameters saved", Toast.LENGTH_SHORT).show();
//			}
//		} else {
//			Toast.makeText(context, "No parameters", Toast.LENGTH_SHORT).show();
//		}
//	}
//	
//	public void openParametersFromFile() {
//		OpenFileDialog dialog = new OpenParameterDialog() {
//			@Override
//			public void parameterFileLoaded(List<Parameter> parameters) {
//				listener.onParamsCountReceivedFromFile(parameters.size());
//				for (int i = 0; i < parameters.size(); i++)
//				{
//					listener.onParameterReceivedFromFile(parameters.get(i), (short) i);
//				}	
//				listener.onParametersReceivedFromFile();
//			}
//		};
//		dialog.openDialog(context);
//	}	
//	
//	public void writeModifiedParametersToDrone() {
//		List<ParamRow> modRows = parametersFragment.getModifiedParametersRows();
//		for (ParamRow row : modRows) 
//		{
//			if (!row.isNewValueEqualToDroneParam())
//			{
//				listener.onSendParameter(row.getParameterFromRow());
//				// Aggiorno la lista parametri del parameter manager
//				VrPadStationApp app = (VrPadStationApp)act.getApplication();
//				if (app.parameterMananger.contains(row.getParameterFromRow().name))
//				{
//					int index = app.parameterMananger.indexOf(row.getParameterFromRow().name);
//					app.parameterMananger.getParamsList().set(index, row.getParameterFromRow());
//				}
//				// Aggiorno la riga della lista parametri
//				row.setParam(row.getParameterFromRow());
//				row.restoreColor();
//			}						
//		}	
//		Toast.makeText(context, "Write "+modRows.size()+" parameters", Toast.LENGTH_SHORT).show();
//	}

	public void clearFlightPath() {
		if (mapFragment != null)
			mapFragment.clearFlightPath();
	}

	public double openPolygonGenerateDialog() {
		if (mapFragment != null)
			return (mapFragment.getMapRotation() + 90) % 180;
		return 0;
	}
	
	public void onDroneModeChanged(Drone drone) {
		if (flightModesFragment != null)
			flightModesFragment.initialize(drone);

		if ((drone.getMode() != ApmModes.ROTOR_GUIDED && drone.getMode() != ApmModes.FIXED_WING_GUIDED) &&
			(LaserConstants.MAP_CLICK_MODE == MapClickModes.FOLLOW_ME || LaserConstants.MAP_CLICK_MODE == MapClickModes.SET_GUIDED_POINT)) {
			resetGuidedButton();
			resetFollowButton();
		}
	}

//	Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//	public void onDroneTypeChanged() {
//		if (mapFragment != null)
//			mapFragment.updateDroneMarkers();
//	}

	@Override
	public void onSetGuidedMode(LatLng point) {
		Toast.makeText(context, "Guided Mode", Toast.LENGTH_SHORT).show();
		//changeDefaultAlt();		
		//guidedPoint = point;
		listener.onSetGuidedMode(point);
	}

	@Override
	public void onAddPoint(LatLng point) {
		switch (missionMode) {
		default:
		case MISSION:
			listener.editWaypoint(point);
			break;
		case POLYGON:
			if (polygon != null)
				polygon.addWaypoint(point);
			update();	
			break;
		}
		//update();	
	}

	@Override
	public void onMoveHome(LatLng coord) {
		drone.setHome(coord);	
		update();
	}

	@Override
	public void onMoveWaypoint(LatLng coord, int Number) {
		drone.moveWaypoint(coord, Number);
		update();
	}

	@Override
	public void onMovePolygonPoint(LatLng coord, int Number) {
		if (polygon != null)
			polygon.movePolygonPoint(coord,  Number);
		update();
	}
	
	public void update() {
		VrPadStationApp app = (VrPadStationApp)act.getApplication();
		if (mapFragment != null)
			mapFragment.update(drone, polygon, app.getAirportsList());
		waypointsFragment.update();
	}
	
	@Override
	public void OnFragmentChanged(int position) {
		//listener.onFragmentChanged(position);
	}
	
	public void updateMapPreferences()
	{
		if (mapFragment != null)
			mapFragment.updateMapPreferences();
	}

	public void onZoom() {
		if (mapFragment != null)
			mapFragment.zoomToLastKnowPosition();
	}

	public void clearWaypointsAndUpdate() {
		Log.d("WPSS", drone.getWaypoints().size() + "");
		update();
	}

	public void setMissionMode(MissionModes mode) {
		missionMode = mode;
	}

	@Override
	public void onListFlightModeClick(String mode) {
		listener.onChangeFlightMode(mode);
	}

//	@Override
//	public void writeParamsToApm() {
//		listener.writeParamsToApm();
//	}
//
//	@Override
//	public void writeParamsToFile() {
//		listener.writeParamsToFile();
//	}
//
//	@Override
//	public void loadParamsFromApm() {
//		listener.loadParamsFromApm();
//	}
//
//	@Override
//	public void loadParamsFromFile() {
//		listener.loadParamsFromFile();
//	}

//	@Override
//	public void onStartAccCalibration() {
//		listener.onStartAccCalibration();
//	}
//
//	@Override
//	public void onStartMagCalibration() {
//		listener.onStartMagCalibration();
//	}
//
//	@Override
//	public void onRefreshCalibrationParams() {
//		listener.onRefreshCalibrationParams();
//	}
//
//	@Override
//	public void onSendCalibrationParam(Parameter parameter) {
//		listener.onSendCalibrationParam(parameter);
//	}

	@Override
	public void enablePolygonMode(boolean isEnabled) {
		listener.enablePolygonMode(isEnabled);
	}
	
	public void updateBattery(int level, boolean isAc) {
		overviewFragment.updateBattery(level, isAc);
	}

	@Override
	public void toggleArmed() {
		listener.toggleArmed();
	}

	@Override
	public void clearTrack() {
		listener.clearTrack();
	}
	
	@Override
	public void zoom() {
		listener.zoom();
	}

	public void init() {
		flightModesFragment.initialize(drone);
		update();
	}

	@Override
	public void setModeAuto() {
		listener.setModeAuto();
	}

	@Override
	public void setAltitude(double offset) {
		listener.setAltitude(offset);
	}

	public void updateAltitude(double targetAltitude) {
		overviewFragment.updateAltitude(targetAltitude);
	}

	private void resetGuidedButton() {
		btnGuidedMode.getBackground().clearColorFilter();
		LaserConstants.MAP_CLICK_MODE = MapClickModes.NONE;
	}
	
	private void resetFollowButton() {
		btnFollowMe.getBackground().clearColorFilter();
		LaserConstants.MAP_CLICK_MODE = MapClickModes.NONE;
	}
	
	@Override
	public void onClick(View v) {
		if (v == btnHideFragments)
		{
			if (droneDataLayout.getVisibility() == View.GONE)
			{
				droneDataLayout.setVisibility(View.VISIBLE);
				btnHideFragments.setImageResource(R.drawable.tab_in);					
				resizeMap(12);
			}
			else if (droneDataLayout.getVisibility() == View.VISIBLE)
			{
				droneDataLayout.setVisibility(View.GONE);
				btnHideFragments.setImageResource(R.drawable.tab_out);					
				resizeMap(mapLayer.getWeightSum());
			}
		}
		else if (v == btnTakeOff)
		{
			listener.takeOff();
		}
		else if (v == btnGuidedMode)
		{
			btnFollowMe.getBackground().clearColorFilter();
			if (LaserConstants.MAP_CLICK_MODE == MapClickModes.SET_GUIDED_POINT)
			{
				resetGuidedButton();
			}
			else
			{
				btnGuidedMode.getBackground().setColorFilter(Color.CYAN, Mode.MULTIPLY);		
				LaserConstants.MAP_CLICK_MODE = MapClickModes.SET_GUIDED_POINT;
			}
			listener.modeGuided();
			waypointsFragment.updateSwitch();
		}
		else if (v == btnFollowMe)
		{
			btnGuidedMode.getBackground().clearColorFilter();
			if (LaserConstants.MAP_CLICK_MODE == MapClickModes.FOLLOW_ME)
			{
				resetFollowButton();
			}
			else
			{
				btnFollowMe.getBackground().setColorFilter(Color.CYAN, Mode.MULTIPLY);		
				LaserConstants.MAP_CLICK_MODE = MapClickModes.FOLLOW_ME;
			}
			listener.modeFollowMe();
			waypointsFragment.updateSwitch();
		}
		else if (v == btnLoiter)
		{
			switch (drone.getType())
			{
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
				listener.onChangeFlightMode(ApmModes.ROTOR_LOITER.getName());
				break;
			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				listener.onChangeFlightMode(ApmModes.FIXED_WING_LOITER.getName());
				break;
			default:
				listener.onChangeFlightMode(ApmModes.UNKNOWN.getName());
				break;
			}
		}
		else if (v == btnRtl)
		{
			switch (drone.getType())
			{
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
				listener.onChangeFlightMode(ApmModes.ROTOR_RTL.getName());
				break;
			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				listener.onChangeFlightMode(ApmModes.FIXED_WING_RTL.getName());
				break;
			default:
				listener.onChangeFlightMode(ApmModes.UNKNOWN.getName());
				break;
			}
		}
		else if (v == btnLand)
		{
			switch (drone.getType())
			{
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
				listener.onChangeFlightMode(ApmModes.ROTOR_LAND.getName());
				break;
			case MAV_TYPE.MAV_TYPE_FIXED_WING:
			default:
				listener.onChangeFlightMode(ApmModes.UNKNOWN.getName());
				break;
			}
		}
		else if (v == btnWriteToApm)
		{
			listener.writeWaypointsToApm();
		}
		else if (v == btnWriteToFile)
		{
			listener.writeWaypointsToFile();
		}
		else if (v == btnLoadFromApm)
		{
			listener.loadWaypointsFromApm();
		}
		else if (v == btnLoadFromFile)
		{
			listener.loadWaypointsFromFile();
		}
		else if (v == btnClearWaypoints)
		{
			listener.clearWaypoints();
		}
		else if (v == btnGeneratePolygon)
		{
			listener.generatePolygon();
		}
		else if (v == btnClearPolygon)
		{
			listener.clearPolygon();
		}
	}

	@Override
	public void addWaypointMode(boolean enabled) {
		if (enabled)
		{
			layoutWaypoinsButtons.setVisibility(View.VISIBLE);
			if (LaserConstants.MAP_CLICK_MODE == MapClickModes.ADD_WAYPOINTS)
			{
				btnFollowMe.getBackground().clearColorFilter();
				btnGuidedMode.getBackground().clearColorFilter();
			}
			listener.addWaypointMode();
		}
		else
			layoutWaypoinsButtons.setVisibility(View.GONE);
	}

//	@Override
//	public void onRemoveWaypoint() {
//		update();
//		mapFragment.zoomToExtents(drone.getAllCoordinates());
//	}
//
//	@Override
//	public void onChangeWpAlt(LatLng coord) {
//		listener.changeWaypointsAltitude(coord);
//	}

	@Override
	public void onListWaypointsLongClick(int position) {
		if (position > 0)
			listener.editWaypoint(drone.getWaypoints().get(position - 1).coord);
	}
	
	@Override
	public void onListWaypointsClick(int position) {
		listener.onSetCurrentWaypoint((short) position);
		if (drone.getWaypoints().size() > 0)
		{
			if (position == 0)
			{
				LatLng point = drone.getHome().coord;
				if (mapFragment != null)
					mapFragment.zoomToPoint(point);
			}
			else
			{
				LatLng point = drone.getWaypoints().get(position - 1).coord;
				if (mapFragment != null)
					mapFragment.zoomToPoint(point);
			}
		}
	}

	public void onRemoveWaypoint() {
		waypointsFragment.update();
		update();
		mapFragment.zoomToExtents(drone.getAllCoordinates());
	}

	public void onAirportsLoaded(List<Airport> airports) {
		if (mapFragment != null) {
			mapFragment.update(drone, polygon, airports);
		}
	}


}
