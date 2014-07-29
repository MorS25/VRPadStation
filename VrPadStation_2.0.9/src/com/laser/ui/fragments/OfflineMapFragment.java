package com.laser.ui.fragments;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.laser.helpers.LocalTileProvider;

public class OfflineMapFragment extends MapFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) 
	{
		View view = super.onCreateView(inflater, viewGroup, bundle);
		initMap();
		return view;
	}

	private void initMap() 
	{
		initMapUI();
		initMapOverlay();
	}

	private void initMapUI()
	{
		GoogleMap map = getMap();
		if (map != null)
		{
			map.setMyLocationEnabled(true);
			map.setBuildingsEnabled(true);
			map.setIndoorEnabled(true);
			UiSettings uiSettings = map.getUiSettings();
			uiSettings.setMyLocationButtonEnabled(true);
			uiSettings.setCompassEnabled(true);
			uiSettings.setTiltGesturesEnabled(true);
		}
	}

	private void initMapOverlay() 
	{
		if (isOfflineMapEnabled()) {
			initOfflineMapOverlay();
		} else {
			initOnlineMapOverlay();
		}
	}

	private boolean isOfflineMapEnabled() 
	{
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_advanced_use_offline_maps", false);
	}

	private void initOnlineMapOverlay()
	{
		GoogleMap map = getMap();
		if (map != null)
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	}

	private void initOfflineMapOverlay() 
	{
		GoogleMap map = getMap();
		if (map != null)
		{
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			map.addTileOverlay(new TileOverlayOptions().tileProvider(new LocalTileProvider()));
		}
	}

	public void zoomToExtents(List<LatLng> pointsList) 
	{
		try {
			if (!pointsList.isEmpty()) 
			{
				LatLngBounds bounds = getBounds(pointsList);
				CameraUpdate animation;
				if (isMapLayoutFinished())
					animation = CameraUpdateFactory.newLatLngBounds(bounds, 30);
				else
					animation = CameraUpdateFactory.newLatLngBounds(bounds, 480, 360, 30);
				getMap().animateCamera(animation);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean isMapLayoutFinished() 
	{
		return getMap() != null;
	}

	private LatLngBounds getBounds(List<LatLng> pointsList)
	{
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng point : pointsList) 
		{
			builder.include(point);
		}
		return builder.build();
	}
	
	public double getMapRotation() 
	{
		if (getMap() != null)
			return getMap().getCameraPosition().bearing;
		else
			return 0;
	}

}
