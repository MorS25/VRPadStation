package com.laser.airports;

import com.google.android.gms.maps.model.LatLng;

public class Airport {

	public String name;
	public LatLng position;
	
	public Airport(String name, LatLng position) {
		this.name = name;
		this.position = position;
	}		

	public Airport(String name, double lat, double lng) {
		this.name = name;
		this.position = new LatLng(lat, lng);
	}
	
    public double GetDistance(LatLng ap2)
    {
        double d = position.latitude * 0.017453292519943295;
        double num2 = position.longitude * 0.017453292519943295;
        double num3 = ap2.latitude * 0.017453292519943295;
        double num4 = ap2.longitude * 0.017453292519943295;
        double num5 = num4 - num2;
        double num6 = num3 - d;
        double num7 = Math.pow(Math.sin(num6 / 2.0), 2.0) + ((Math.cos(d) * Math.cos(num3)) * Math.pow(Math.sin(num5 / 2.0), 2.0));
        double num8 = 2.0 * Math.atan2(Math.sqrt(num7), Math.sqrt(1.0 - num7));
        return (6371 * num8) * 1000.0; // M
    }
}
