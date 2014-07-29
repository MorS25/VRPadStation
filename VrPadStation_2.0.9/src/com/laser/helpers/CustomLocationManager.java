package com.laser.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class CustomLocationManager implements   GooglePlayServicesClient.ConnectionCallbacks,
												GooglePlayServicesClient.OnConnectionFailedListener,
												LocationListener,
												android.location.GpsStatus.Listener {
	

	private List<CustomLocationManagerListener> locationManagerListenerList = new ArrayList<CustomLocationManagerListener>();
	public interface CustomLocationManagerListener {
		void onLocationChanged(Location location);
		void onLocationManagerConnectionFailed(ConnectionResult connectionResult);
		void onLocationManagerConnected();
		void onLocationManagerDisconnected();		
	}
	public void setCustomLocationManagerListener(CustomLocationManagerListener listener){
		locationManagerListenerList.add(listener);
	}
	public void removeCustomLocationManagerListener(CustomLocationManagerListener listener) {
		if (locationManagerListenerList.contains(listener))
			locationManagerListenerList.remove(listener);		
	}
	
	// GPS
	private LocationManager locationManager;
	private GpsStatus mStatus;
	// Define a request code to send to Google Play services this code is returned in Activity.onActivityResult
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 500; // ms
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 500; // ms
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    
    private LocationClient mLocationClient;
    private boolean mUpdatesRequested;

	private int numSatellitesInFix = 0;

	private Context context;
	    
	
    public CustomLocationManager(Context context) 
    {
    	this.context = context;
    	
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        // Create a new location client, using the enclosing class to handle callbacks.
        mLocationClient = new LocationClient(this.context, this, this);
        // Start with updates turned off
        mUpdatesRequested = false;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(this);

    	connect();
    }
    
    public void connect() {
    	if (!mLocationClient.isConnected())
    		mLocationClient.connect();
    }
    
    public void destroy() {
    	 if (mLocationClient.isConnected()) {
         	mLocationClient.removeLocationUpdates(this);
         }
         mLocationClient.disconnect();        
    }


	@Override
	public void onGpsStatusChanged(int event) {
		mStatus = locationManager.getGpsStatus(mStatus);
	    switch (event) 
	    {
	        case GpsStatus.GPS_EVENT_STARTED:
	            // Do Something with mStatus info
	            break;

	        case GpsStatus.GPS_EVENT_STOPPED:
	            // Do Something with mStatus info
	            break;

	        case GpsStatus.GPS_EVENT_FIRST_FIX:
	            // Do Something with mStatus info
	            break;

	        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	        	getSatellitesFromGpsStatus(mStatus);
	            break;
	    }
	}
	
	public void getSatellitesFromGpsStatus(GpsStatus gpsStatus) {		
		if (gpsStatus != null)
		{
			//numSatellites = 0;
			numSatellitesInFix = 0;
			for (GpsSatellite sat : gpsStatus.getSatellites()) 
			{
		        if(sat.usedInFix()) {
		        	numSatellitesInFix++;              
		        }
		        //numSatellites++;
		    }
		}
	}	

	@Override
	public void onConnected(Bundle arg0) {
		mUpdatesRequested = true;
		 // If already requested, start periodic updates
        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
		for (CustomLocationManagerListener clml : locationManagerListenerList)
			clml.onLocationManagerConnected();
	}

	@Override
	public void onDisconnected() {
		mUpdatesRequested = false;
		for (CustomLocationManagerListener clml : locationManagerListenerList)
			clml.onLocationManagerDisconnected();
	}

	public int getSatellitesNumber() {
		return numSatellitesInFix;
	}
	

	@Override
	public void onLocationChanged(Location location) {
		for (CustomLocationManagerListener clml : locationManagerListenerList)
			clml.onLocationChanged(location);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {	
		mUpdatesRequested = false;
		for (CustomLocationManagerListener clml : locationManagerListenerList)
			clml.onLocationManagerConnectionFailed(connectionResult);
	}

}
