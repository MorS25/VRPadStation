package com.laser.ui.fragments;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.laser.MAVLink.Drone;
import com.laser.MAVLink.Drone.MapUpdatedListner;
import com.laser.ui.fragments.OfflineMapFragment;
import com.laser.helpers.Polygon;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.laser.VrPadStation.R;
import com.laser.airports.Airport;
import com.laser.app.VrPadStationApp;
import com.laser.ui.layers.MapLayer;
import com.laser.utils.LaserConstants;

public class CustomMapFragment extends OfflineMapFragment implements OnMapLongClickListener, OnMarkerDragListener, MapUpdatedListner {
	
	private GoogleMap mMap;
	private Marker droneMarker;
	private Marker guidedMarker;
	private Polyline flightPath;

	private int maxFlightPathSize;
	private boolean isAutoPanEnabled;
	private int autoPanZoom = 17;
	
	private boolean showAirports = false;
	
	private boolean hasBeenZoomed = false;
	private Marker homeMarker;
	private Drone drone;
	
	private HashMap<Integer, Marker> waypointMarkers = new HashMap<Integer, Marker>();
	private HashMap<Integer, Marker> polygonMarkers = new HashMap<Integer, Marker>();
	private HashMap<Integer, CircleOptions> airportsMarkers = new HashMap<Integer, CircleOptions>();
	private Marker home;

	private OnFlighDataListener mListener;
	private OnMapInteractionListener mListener2;

	private static final String homeMarkerTitle = "Home";
	
	private Handler handlerMapPan = new Handler();
	

	public interface OnMapInteractionListener {
		public void onAddPoint(LatLng point);
		public void onMoveHome(LatLng coord);
		public void onMoveWaypoint(LatLng coord, int Number);
		public void onMovePolygonPoint(LatLng coord, int Number);
	}	
	
	public interface OnFlighDataListener {
		public void onSetGuidedMode(LatLng point);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		if (mMap != null)
		{
			drone = new Drone(((VrPadStationApp) getActivity().getApplication()).tts);
			drone.setMapListner(this);	
			addDroneMarkersToMap(drone.getType());		
			addFlightPathToMap();	
			getPreferences();
			mMap.setOnMarkerDragListener(this);
			mMap.setOnMapLongClickListener(this);
		}
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		handlerMapPan.postDelayed(runnableMapPan, 1000);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		handlerMapPan.removeCallbacks(runnableMapPan);
	}
	
	public void updateMapPreferences()
	{
		getPreferences();
	}
	
	public void setListeners(MapLayer listener)
	{
		mListener = listener;
		mListener2 = listener;
	}
	
	public void update(final Drone drone, final Polygon polygon, final List<Airport> airports) {
		mMap.clear();
		addDroneMarkersToMap(drone.getType());		
		
		waypointMarkers.clear();
		polygonMarkers.clear();
		//airportsMarkers.clear();
		
		// Tutto questo per ridurre al minimo le operazioni e velocizzare il disegno
		if (showAirports) {
			if (airportsMarkers.size() <= 0) {
				if (airports != null && airports.size() > 0) {
					LatLng tempDroneLocation = null;
					if (drone != null)
						tempDroneLocation = drone.getPosition();
					final LatLng droneLocation = tempDroneLocation;
					final LatLng myLocation = getMyLocation(); // La salvo qua perchè nel thread non me lo fa fare perchè non è quello principale
					
					if (myLocation != null || droneLocation != null) {
						Thread t = new Thread(new Runnable() {						
							@Override
							public void run() {
								LatLng location = null;
								if (myLocation != null)
									location = myLocation;
								else if (droneLocation != null)
									location = droneLocation;
								else
									return;

								int c = 0;
								for (Airport ap : airports) {
									if (ap.GetDistance(location) < 100000) 	//100km  						
									{
										airportsMarkers.put(c, new CircleOptions().
																	center(ap.position).
																	radius(8000).
																	fillColor(0x20ff0000).
																	strokeColor(Color.WHITE).
																	strokeWidth(2).
																	visible(true));
									}
									c++;
								}
								if (airportsMarkers.size() > 0)
									getActivity().runOnUiThread(new Runnable() {									
										@Override
										public void run() {
											update(drone, polygon, airports);
										}
									});
							}
						});
						t.start();
					}
				}
			} else {
				try {
					int i = 0;
					for (CircleOptions co : airportsMarkers.values()) {
						mMap.addCircle(co);
						i++;
					}
	
					if (LaserConstants.DEBUG)
						Log.d("AIRPORTS", "Added " + i + " circles on map.");
				} catch (ConcurrentModificationException cme) {
					cme.printStackTrace();
				}
			}
		}
		
		home = mMap.addMarker(getHomeIcon(drone));
		int i =0;
		for (MarkerOptions waypoint : getMissionMarkers(drone)) {
			waypointMarkers.put(i++,mMap.addMarker(waypoint));
		}
		mMap.addPolyline(getMissionPath(drone));

		i = 0;
		if (polygon != null)
		{
			for (MarkerOptions point : getPolygonMarkers(polygon)) {
				polygonMarkers.put(i++,mMap.addMarker(point));
			}
			mMap.addPolyline(getPolygonPath(polygon));
		}
	}
	
	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}
	
	@Override
	public void onMarkerDragEnd(Marker marker) {
		checkForHomeMarker(marker);
		checkForWaypointMarker(marker);
		checkForPolygonMarker(marker);
	}

	private void checkForHomeMarker(Marker marker) {
		if(home.equals(marker)){
			mListener2.onMoveHome(marker.getPosition());
		}
	}

	private void checkForWaypointMarker(Marker marker) {
		if (waypointMarkers.containsValue(marker)) {
			int number = 0;
			for (HashMap.Entry<Integer, Marker> e : waypointMarkers.entrySet()) {
				if (marker.equals(e.getValue())) {
					number = e.getKey();
					break;
				}
			}
			mListener2.onMoveWaypoint(marker.getPosition(), number);
		}
	}

	private void checkForPolygonMarker(Marker marker) {
		if (polygonMarkers.containsValue(marker)) {
			int number = 0;
			for (HashMap.Entry<Integer, Marker> e : polygonMarkers.entrySet()) {
				if (marker.equals(e.getValue())) {
					number = e.getKey();
					break;
				}
			}
			mListener2.onMovePolygonPoint(marker.getPosition(), number);
		}
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

	private MarkerOptions getHomeIcon(Drone drone) {
		return (new MarkerOptions()
				.position(drone.getHome().coord)
				.snippet(String.format(Locale.ENGLISH, "%.2f", drone.getHome().Height))
				.draggable(true)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_home))
				.title(homeMarkerTitle));
	}

	private List<MarkerOptions> getMissionMarkers(Drone drone) {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (waypoint point : drone.getWaypoints()) {
			MarkerList
					.add(new MarkerOptions()
							.position(point.coord)
							.draggable(true)
							.title("WP" + Integer.toString(i))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue_small))
							.snippet(String.format(Locale.ENGLISH, "%.2f", point.Height)));
			i++;
		}
		return MarkerList;
	}

	public List<MarkerOptions> getPolygonMarkers(Polygon poly) {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (LatLng point : poly.getWaypoints()) {
			MarkerList.add(new MarkerOptions()
					.position(point)
					.draggable(true)
					.title("Poly" + Integer.toString(i))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_green_small)));
			i++;
		}
		return MarkerList;
	}

	private PolylineOptions getMissionPath(Drone drone) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.WHITE).width(4);
	
		flightPath.add(drone.getHome().coord);
		for (waypoint point : drone.getWaypoints()) {
			flightPath.add(point.coord);
		}
		return flightPath;
	}

	public PolylineOptions getPolygonPath(Polygon poly) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.BLACK).width(3);
	
		for (LatLng point : poly.getWaypoints()) {
			flightPath.add(point);
		}
		if (poly.getWaypoints().size() > 2) {
			flightPath.add(poly.getWaypoints().get(0));
		}
	
		return flightPath;
	}
	
	private void getPreferences() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		maxFlightPathSize =Integer.valueOf(prefs.getString("pref_max_fligth_path_size", "100"));//0"));
		isAutoPanEnabled =prefs.getBoolean("pref_auto_pan_enabled", false);
		autoPanZoom =Integer.valueOf(prefs.getString("pref_auto_pan_zoom", "17"));
		showAirports = prefs.getBoolean("pref_show_airports", false);
		String mapType = prefs.getString("map_type", "Satellite");
		int mapTypeValue = GoogleMap.MAP_TYPE_SATELLITE;
		if (mapType.equals("Terrain"))
			mapTypeValue = GoogleMap.MAP_TYPE_TERRAIN;
		else if (mapType.equals("Hybrid"))
			mapTypeValue = GoogleMap.MAP_TYPE_HYBRID;
		else if (mapType.equals("Normal"))
			mapTypeValue = GoogleMap.MAP_TYPE_NORMAL;
		else if (mapType.equals("Satellite"))
			mapTypeValue = GoogleMap.MAP_TYPE_SATELLITE;
		getMap().setMapType(mapTypeValue);
	}
	
	public void receiveData(MAVLinkMessage msg, Drone drone) {
		if(msg.msgid == msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT)
		{
			LatLng position = new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7);
			float heading = (0x0000FFFF & ((int)((msg_global_position_int)msg).hdg))/100f;
			if (drone != null)
				heading = (float) drone.getYaw();		
			
			updateDronePosition(heading, position);
			addFligthPathPoint(position);
		}
	}
	
	private synchronized void addFligthPathPoint(LatLng position) {
		if (maxFlightPathSize > 0)
		{
			PolylineOptions flightPathOptions = new PolylineOptions();
			flightPathOptions.color(Color.CYAN).width(6).zIndex(0);	
			
			List<LatLng> oldFlightPath = flightPath.getPoints();
			if (oldFlightPath.size() > maxFlightPathSize) 
			{
				oldFlightPath.remove(0);
			}
			oldFlightPath.add(position);
			
			flightPathOptions.addAll(oldFlightPath);
			flightPath.remove();
			flightPath = mMap.addPolyline(flightPathOptions);
		}		
	}

	public synchronized void clearFlightPath() {
		List<LatLng> oldFlightPath = flightPath.getPoints();
		oldFlightPath.clear();
		flightPath.setPoints(oldFlightPath);
		
		flightPath.remove();
	}

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.BLUE).width(6).zIndex(0);
		flightPath = mMap.addPolyline(flightPathOptions);		
	}
		
	private void updateGuidedMarker(LatLng point) {
		if(guidedMarker == null){
			guidedMarker = mMap.addMarker(new MarkerOptions()
			.anchor((float) 0.5, (float) 0.5)
			.position(point)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_purple_small)));
		} else {
			guidedMarker.setPosition(point);
		}	
	}

	@Override
	public void onResume() {		
		if (guidedMarker != null) {		// when onPause() is called, it becomes invisible
			LatLng p = guidedMarker.getPosition();
			guidedMarker = null;
			updateGuidedMarker(p);
		}
		super.onResume();
	}

	private void updateDronePosition(double yaw, LatLng coord) {
		double correctHeading = (yaw - getMapRotation() + 360) % 360;	// This ensure the 0 to 360 range

		try{
			droneMarker.setPosition(coord);
			droneMarker.setRotation((float)correctHeading);
			
			if(!hasBeenZoomed){
				hasBeenZoomed = true;
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 16));
			}
		}catch(Exception e){}
	}
	
	private Runnable runnableMapPan = new Runnable() {		
		@Override
		public void run() {
			if(isAutoPanEnabled)
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(droneMarker.getPosition(), autoPanZoom));
			handlerMapPan.postDelayed(runnableMapPan, 2000);
		}
	};
	
	public void zoomToLastKnowPosition() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(droneMarker.getPosition(), 16));
	}
	
	public void zoomToPoint(LatLng point) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 20));
	}	
	
	public void updateHomeToMap(Drone drone) {
		if(homeMarker== null){
			homeMarker = mMap.addMarker(new MarkerOptions()
			.position(drone.getHome().coord)
			.snippet(String.format(Locale.ENGLISH, "%.2f", drone.getHome().Height))
			.draggable(true)
			.anchor((float) 0.5, (float) 0.5)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_home))
			.title("Home"));
		}else {
			homeMarker.setPosition(drone.getHome().coord);
			homeMarker.setSnippet(String.format(Locale.ENGLISH, "%.2f", drone.getHome().Height));
		}
	}
	
//	Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//	public void updateDroneMarkers(){
//		droneMarker.remove();
//		addDroneMarkersToMap(drone.getType());		
//	}
	
	private void addDroneMarkersToMap(int type) {
		BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.position);
		droneMarker = mMap.addMarker(new MarkerOptions()
						.anchor((float) 0.5, (float) 0.5)
						.position(new LatLng(0, 0))
						.icon(bd)
						.visible(true)
						.flat(true));
	}
	
//	private BitmapDescriptor generateDroneIcon(float heading,int type) {
//		Bitmap planeBitmap = getDroneBitmap(type);
//		Matrix matrix = new Matrix();
//		matrix.postRotate(heading - mMap.getCameraPosition().bearing);
//		Bitmap bmp = Bitmap.createBitmap(planeBitmap, 0, 0, planeBitmap.getWidth(),
//						planeBitmap.getHeight(), matrix, true);
//		BitmapDescriptor bmpDsc = BitmapDescriptorFactory.fromBitmap(bmp);
//		return bmpDsc;
//	}
//	
//	private Bitmap getDroneBitmap(int type) {
//		switch (type) {
//		case MAV_TYPE.MAV_TYPE_TRICOPTER:
//		case MAV_TYPE.MAV_TYPE_QUADROTOR:
//		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
//		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
//		case MAV_TYPE.MAV_TYPE_HELICOPTER:
//		case MAV_TYPE.MAV_TYPE_FIXED_WING:
//		default:
//			return BitmapFactory.decodeResource(getResources(), R.drawable.position);
//		}
//	}
	
	@Override
	public void onMapLongClick(LatLng point) {
		switch (LaserConstants.MAP_CLICK_MODE)
		{
		case ADD_WAYPOINTS:
			mListener2.onAddPoint(point);
			guidedMarker = null;
			break;
		case SET_GUIDED_POINT:
			mListener.onSetGuidedMode(point);	
			updateGuidedMarker(point);
			break;
		default:
			break;
		}
	}

	@Override
	public void onDronePositionUpdate() {
		updateDronePosition(drone.getYaw(), drone.getPosition());
		addFligthPathPoint(drone.getPosition());
	}


}
