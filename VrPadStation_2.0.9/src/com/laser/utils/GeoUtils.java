package com.laser.utils;

import com.google.android.gms.maps.model.LatLng;

public class GeoUtils {
	
	public static double	m_R = 6371007.0;	// m
	private static double	m_dDegToRad = Math.atan(1.0) / 45.0;
	private static double	m_dRadToDeg = 45.0 / Math.atan(1.0);
	
	public static LatLng DegreesToLambert(LatLng pos, LatLng refPos)
	{
		double dX = m_R * ((pos.longitude - refPos.longitude) * m_dDegToRad) * Math.cos(pos.latitude * m_dDegToRad);
		double dY = m_R * pos.latitude * m_dDegToRad;
		
		return new LatLng(dY, dX);
	}	

	public static LatLng LambertToDegrees(double X, double Y, double dLam0)
	{
		double phi = Y / m_R;

		double dLat = phi * m_dRadToDeg;
		double dLon = ( dLam0 + X / (m_R * Math.cos(phi)) ) * m_dRadToDeg;
		
		return new LatLng(dLat, dLon);
	}
	
	public static double CalculateHeading(LatLng refPos, LatLng pos)
	{
		double lat1 = Math.toRadians(refPos.latitude);
		double lat2 = Math.toRadians(pos.latitude);
		double lon1 = Math.toRadians(refPos.longitude);
		double lon2 = Math.toRadians(pos.longitude);
		double dLon = lon2 - lon1;
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		        Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double brng = Math.atan2(y, x);
		brng = Math.toDegrees(brng);
		//brng = (360 - ((brng + 360) % 360));
		return brng;
	}
	
	/* Non sembra funzionare.
	//CalcolaPruaGradi
	public static double CalculateHeading( LatLng refPos, LatLng pos)
	{		
		double dX1, dY1, dX2, dY2;//, dX3, dY3
		LatLng xyRef = DegreesToLambert(refPos,refPos);
		LatLng xyPos = DegreesToLambert(pos,refPos);
		dX1 = xyRef.longitude;
		dY1 = xyRef.latitude;
		dX2 = xyPos.longitude;
		dY2 = xyPos.latitude;

		double r = Math.sqrt(Math.pow(dX2-dX1, 2) + Math.pow(dY2-dY1, 2));
		double alfa = 0;

		if (r == 0) {
			alfa = 0;
		} else {
			if (dX1 < dX2) {
				alfa = Math.acos((dY2 - dY1) / r) * m_dRadToDeg;
			} else if (dX1 == dX2) {
				if (dY2 >= dY1) {
					alfa = 0;
				} else {
					alfa = 180;
				}
			} else {
				alfa = 360.0 - Math.acos((dY2 - dY1) / r) * m_dRadToDeg;
			}
		}
		return alfa;
	}*/
	
	/**
	 * Calculate coords given starting coords, distance and bearing
	 * @param starting position
	 */
	public static LatLng calculateDestinationCoordinates(LatLng pos, double distance, double bearing)
	{								
		double lat = pos.latitude;
		double lon = pos.longitude;
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lon);
		double brng = Math.toRadians(bearing);
		double dr = distance / m_R;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1)
				* Math.sin(dr) * Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
						Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

		return (new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)));
		
		/* nell'andropilot c'era anche questo pezzo
		double lonNorm = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
		targetPos =  new LatLng(Math.toDegrees(lat2), Math.toDegrees(lonNorm));*/
	}

	/**
	 * Calculate distance between two coords
	 * @param startPosition
	 * @param targetPosition
	 * @return distance
	 */
	public static float calculateDistance(LatLng startPosition, LatLng targetPosition) 
	{
		double dDegLat1 = Math.toRadians(startPosition.latitude);
		double dDegLon1 = Math.toRadians(startPosition.longitude);
		double dDegLat2 = Math.toRadians(targetPosition.latitude);
		double dDegLon2 = Math.toRadians(targetPosition.longitude);
		double dRet = m_R * Math.acos( Math.sin(dDegLat1) * Math.sin(dDegLat2) + Math.cos(dDegLat1) * Math.cos(dDegLat2) * Math.cos(dDegLon1 - dDegLon2));
		return (float) dRet;
	}

}
