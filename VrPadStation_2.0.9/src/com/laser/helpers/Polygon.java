package com.laser.helpers;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class Polygon {	

	private class Line
	{
		public LatLng p1;
		public LatLng p2;

		public Line(LatLng p1, LatLng p2) 
		{
			this.p1 = p1;
			this.p2 = p2;
		}
	}

	/** Object for holding boundary for a polygon */
	private class Bounds
	{
		public LatLng swBound;
		public LatLng neBound;

		public Bounds(List<LatLng> points) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (LatLng point : points) {
				builder.include(point);
			}
			LatLngBounds bounds = builder.build();
			swBound = bounds.southwest;
			neBound = bounds.northeast;
		}

		public LatLng getMiddle() {
			return (new LatLng((neBound.latitude + swBound.latitude) / 2, (neBound.longitude + swBound.longitude) / 2));
		}

		public double getDiag() {
			return latToMeters(getDistance(neBound, swBound));
		}
	}

	private List<LatLng> waypointsList;

	public Polygon() {
		setWaypoints(new ArrayList<LatLng>());
	}

	private void setWaypoints(ArrayList<LatLng> arrayList) {
		waypointsList = arrayList;
	}

	public void addWaypoint(Double Lat, Double Lng) {
		getWaypoints().add(new LatLng(Lat, Lng));
	}

	public void addWaypoint(LatLng coord) {
		getWaypoints().add(findClosestPair(coord, getWaypoints()), coord);
	}

	public void clearPolygon() {
		getWaypoints().clear();
	}

	public List<LatLng> getWaypoints() {
		return waypointsList;
	}

	public boolean isPolygonValid() 
	{
		// A valid polygon must have at least 3 points
		if(getWaypoints().size()>2)	
			return true;
		else
			return false;
	}

	public void movePolygonPoint(LatLng coord, int number) {
		waypointsList.set(number, coord);		
	}

	public List<waypoint> hatchfill(Double angle, Double lineDist, LatLng lastLocation, Double altitude)
	{
		List<Line> gridLines = generateGrid(getWaypoints(), angle, lineDist);
		List<Line> hatchLines = trimGridLines(getWaypoints(), gridLines);
		List<waypoint> gridPoints = waypointsFromHatch(lastLocation, altitude, hatchLines);
		return gridPoints;
	}

	/**
	 * Generates a list of waypoints from a list of hatch lines, choosing the best way 
	 * to organize the mission. Uses the extreme points of the lines as waypoints.
	 * 
	 * @param lastLocation
	 *            The last location of the mission, used to chose where to start
	 *            filling the polygon with the hatch
	 * @param altitude
	 *            Altitude of the waypoints
	 * @param hatchLines
	 *            List of lines to be ordered and added
	 */
	private List<waypoint> waypointsFromHatch(LatLng lastLocation,
											  Double altitude, 
											  List<Line> hatchLines) 
    {
		List<waypoint> gridPointsList = new ArrayList<waypoint>();
		Line closestLine = findClosestLine(lastLocation, hatchLines);
		LatLng lastPoint;

		if (getDistance(closestLine.p1, lastLocation) < getDistance(closestLine.p2, lastLocation))
			lastPoint = closestLine.p1;
		else 
			lastPoint = closestLine.p2;

		while (hatchLines.size() > 0) 
		{
			if (getDistance(closestLine.p1, lastPoint) < getDistance(closestLine.p2, lastPoint)) 
			{
				gridPointsList.add(new waypoint(closestLine.p1, altitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
				gridPointsList.add(new waypoint(closestLine.p2, altitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));

				lastPoint = closestLine.p2;
				hatchLines.remove(closestLine);
				if (hatchLines.size() == 0)
					break;
				closestLine = findClosestLine(closestLine.p2, hatchLines);
			} else {
				gridPointsList.add(new waypoint(closestLine.p2, altitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));
				gridPointsList.add(new waypoint(closestLine.p1, altitude, MAV_CMD.MAV_CMD_NAV_WAYPOINT));

				lastPoint = closestLine.p1;
				hatchLines.remove(closestLine);
				if (hatchLines.size() == 0)
					break;
				closestLine = findClosestLine(closestLine.p1, hatchLines);
			}
		}
		return gridPointsList;
	}

	/**
	 * Trims a grid of lines for points outside a polygon
	 * 
	 * @param waypoints2: Polygon vertices
	 * @param grid: Array with Grid lines
	 * @return array with the trimmed grid lines
	 */
	private List<Line> trimGridLines(List<LatLng> waypoints2, List<Line> grid) 
	{
		List<Line> hatchLinesList = new ArrayList<Line>();
		// find intersections
		for (Line gridLine : grid)
		{
			double closestDistance = Double.MAX_VALUE;
			double farestDistance = Double.MIN_VALUE;

			LatLng closestPoint = null;
			LatLng farestPoint = null;

			int crosses = 0;

			for (int b = 0; b < waypoints2.size(); b++) {
				LatLng newlatlong;
				if (b != waypoints2.size() - 1) {
					newlatlong = FindLineIntersection(waypoints2.get(b), waypoints2.get(b + 1), gridLine.p1, gridLine.p2);
				} else { // Don't forget the last polygon line
					newlatlong = FindLineIntersection(waypoints2.get(b), waypoints2.get(0), gridLine.p1, gridLine.p2);
				}

				if (newlatlong != null) 
				{
					crosses++;
					if (closestDistance > getDistance(gridLine.p1, newlatlong)) {
						closestPoint = new LatLng(newlatlong.latitude, newlatlong.longitude);
						closestDistance = getDistance(gridLine.p1, newlatlong);
					}
					if (farestDistance < getDistance(gridLine.p1, newlatlong)) {
						farestPoint = new LatLng(newlatlong.latitude, newlatlong.longitude);
						farestDistance = getDistance(gridLine.p1, newlatlong);
					}
				}
			}

			switch (crosses) {
			case 0:
			case 1:
				break;
			default: 
			case 2:
				hatchLinesList.add(new Line(closestPoint, farestPoint));
				break;
			}
		}
		return hatchLinesList;
	}

	/**
	 * Generates a grid over the specified boundary's
	 * 
	 * @param waypoints2
	 *            Array with the polygon points
	 * @param angle
	 *            Angle of the grid in Degrees
	 * @param lineDist
	 *            Distance between lines in meters
	 * @return Returns a array of lines of the generated grid
	 */
	private List<Polygon.Line> generateGrid(List<LatLng> waypoints2, Double angle, Double lineDist)
	{
		List<Polygon.Line> gridLinesList = new ArrayList<Polygon.Line>();

		Bounds bounds = new Bounds(waypoints2);
		LatLng point = new LatLng(bounds.getMiddle().latitude,
				bounds.getMiddle().longitude);

		point = newpos(point, angle - 135, bounds.getDiag());

		// get x y step amount in lat lng from m
		Double y1 = Math.cos(Math.toRadians(angle + 90));
		Double x1 = Math.sin(Math.toRadians(angle + 90));
		LatLng diff = new LatLng(metersTolat(lineDist * y1), metersTolat(lineDist * x1));
		Log.d("Diff", "Lat:" + metersTolat(lineDist * y1) + " Long:" + metersTolat(lineDist * x1));

		// draw grid
		int lines = 0;
		while (lines * lineDist < bounds.getDiag() * 1.5) 
		{
			LatLng pointx = point;
			pointx = newpos(pointx, angle, bounds.getDiag() * 1.5);

			Polygon.Line line = new Polygon.Line(point, pointx);
			gridLinesList.add(line);

			point = addLatLng(point, diff);
			lines++;
		}
		return gridLinesList;
	}


	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 * 
	 * @param start1
	 *            starting point of the first line
	 * @param end1
	 *            ending point of the first line
	 * @param start2
	 *            starting point of the second line
	 * @param end2
	 *            ending point of the second line
	 * @return point of intersection, or null if there is no intersection
	 */
	private LatLng FindLineIntersection(LatLng start1, LatLng end1, LatLng start2, LatLng end2) 
	{
		double denom = ((end1.longitude - start1.longitude) * (end2.latitude - start2.latitude))
				- ((end1.latitude - start1.latitude) * (end2.longitude - start2.longitude));
		
		// AB & CD are parallel
		if (denom == 0)
			return null;
		
		double numer = ((start1.latitude - start2.latitude) * (end2.longitude - start2.longitude))
				- ((start1.longitude - start2.longitude) * (end2.latitude - start2.latitude));
		
		double r = numer / denom;
		
		double numer2 = ((start1.latitude - start2.latitude) * (end1.longitude - start1.longitude))
				- ((start1.longitude - start2.longitude) * (end1.latitude - start1.latitude));
		
		double s = numer2 / denom;
		
		if ((r < 0 || r > 1) || (s < 0 || s > 1))
			return null;
		
		// Find intersection point
		double longitude = start1.longitude + (r * (end1.longitude - start1.longitude));
		double latitude = start1.latitude + (r * (end1.latitude - start1.latitude));
		return (new LatLng(latitude, longitude));
	}

	/**
	 * Finds the line that has the start or tip closest to a point.
	 * 
	 * @param point
	 *            Point to the distance will be minimized
	 * @param list
	 *            A list of lines to search
	 * @return The closest Line
	 */
	private Line findClosestLine(LatLng point, List<Line> list)
	{
		Line answer = list.get(0);
		double shortest = Double.MAX_VALUE;
		for (Line line : list) 
		{
			double ans1 = getDistance(point, line.p1);
			double ans2 = getDistance(point, line.p2);
			LatLng shorterPoint = ans1 < ans2 ? line.p1 : line.p2;

			if (shortest > getDistance(point, shorterPoint)) 
			{
				answer = line;
				shortest = getDistance(point, shorterPoint);
			}
		}
		return answer;
	}

	/**
	 * Finds the closest point in a list to another point
	 * 
	 * @param point: point that will be used as reference
	 * @param list: List of points to be searched
	 * @return The closest point
	 */
	private LatLng findClosestPoint(LatLng point, List<LatLng> list) {
		LatLng closestPoint = null;
		double currentBest = Double.MAX_VALUE;
		for (LatLng pnt : list) 
		{
			double distance = getDistance(point, pnt);

			if (distance < currentBest) 
			{
				closestPoint = pnt;
				currentBest = distance;
			}
		}
		return closestPoint;
	}

	/**
	 * Finds the pair of adjacent points that minimize the distance to a
	 * reference point
	 * 
	 * @param point: point that will be used as reference
	 * @param waypoints2: List of points to be searched
	 * @return Position of the second point in the pair that minimizes the distance
	 */
	private int findClosestPair(LatLng point, List<LatLng> waypoints) 
	{
		int closestPair = 0;
		double currentBest = Double.MAX_VALUE;
		double distance;
		LatLng p1, p2;

		for (int i = 0; i < waypoints.size(); i++) 
		{
			if (i == waypoints.size() - 1) 
			{
				p1 = waypoints.get(i);
				p2 = waypoints.get(0);
			} else {
				p1 = waypoints.get(i);
				p2 = waypoints.get(i + 1);
			}

			distance = pointToLineDistance(p1, p2, point);
			if (distance < currentBest) 
			{
				closestPair = i + 1;
				currentBest = distance;
			}
		}
		return closestPair;
	}

	/**
	 * Provides the distance from a point P to the line segment that passes
	 * through A-B. If the point is not on the side of the line, returns the
	 * distance to the closest point
	 */
	public double pointToLineDistance(LatLng L1, LatLng L2, LatLng P) 
	{
		double A = P.longitude - L1.longitude;
		double B = P.latitude - L1.latitude;
		double C = L2.longitude - L1.longitude;
		double D = L2.latitude - L1.latitude;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;

		double xx, yy;

		if (param < 0) // point behind the segment
		{
			xx = L1.longitude;
			yy = L1.latitude;
		} else if (param > 1) // point after the segment
		{
			xx = L2.longitude;
			yy = L2.latitude;
		} else { // point on the side of the segment
			xx = L1.longitude + param * C;
			yy = L1.latitude + param * D;
		}

		return Math.hypot(xx - P.longitude, yy - P.latitude);
	}

	/**
	 * Adds an offset to a point (in degrees)
	 * 
	 * @param point: the point to be modified
	 * @param offset: offset to be added
	 * @return point with offset
	 */
	private LatLng addLatLng(LatLng point, LatLng offset) 
	{
		return (new LatLng(point.latitude + offset.latitude, point.longitude + offset.longitude));
	}

	/**
	 * Returns the distance between two points
	 * 
	 * @return distance between the points in degrees
	 */
	private Double getDistance(LatLng p1, LatLng p2) 
	{
		return (Math.hypot((p1.latitude - p2.latitude), (p1.longitude - p2.longitude)));
	}

	private Double latToMeters(double lat) 
	{
		double radius_of_earth = 6378100.0;// m
		return Math.toRadians(lat) * radius_of_earth;
	}

	private Double metersTolat(double meters) {
		double radius_of_earth = 6378100.0;// m
		return Math.toDegrees(meters / radius_of_earth);
	}

	/**
	 * Extrapolate latitude/longitude given a heading and distance thanks to
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param origin:Point of origin
	 * @param bearing: bearing to navigate
	 * @param distance: distance to be added
	 * @return New point with the added distance
	 */
	private LatLng newpos(LatLng origin, double bearing, double distance) 
	{
		double earth_radius = 6378100.0;// m

		double lat = origin.latitude;
		double lon = origin.longitude;
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lon);
		double brng = Math.toRadians(bearing);
		double dr = distance / earth_radius;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr) * Math.cos(brng));
		double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1), Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

		return (new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)));
	}
	
	public Double getArea() 
	{
		double sum = 0.0;
		for (int i = 0; i < getWaypoints().size() - 1; i++) 
		{
			sum = sum
					+ (latToMeters(getWaypoints().get(i).longitude) * latToMeters(getWaypoints()
							.get(i + 1).latitude))
					- (latToMeters(getWaypoints().get(i).latitude) * latToMeters(getWaypoints()
							.get(i + 1).longitude));
		}
		return Math.abs(0.5 * sum);
	}


}
