package com.laser.ui.widgets;

import java.util.List;

import com.MAVLink.waypoint;
import com.laser.VrPadStation.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class WaypointsAdapter extends ArrayAdapter{

    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
	public WaypointsAdapter ( Context ctx, int resourceId, List<waypoint> waypoints) {
          super( ctx, resourceId, waypoints );          
          inflater = LayoutInflater.from( ctx );
    }

    @Override
    public View getView ( int position, View convertView, ViewGroup parent ) {

    	  View vi = inflater.inflate(R.layout.waypoint_list_item, null);

          waypoint point = (waypoint) getItem( position );

          /* Take the TextView from layout and set the city's name */
          TextView textViewWaypointData = (TextView) vi.findViewById(R.id.textViewWaypointData);
          //textViewWaypointData.setTextSize(DroneActivity.TEXT_SIZE);
          String command = waypoint.parseCommand(point.cmd);
          textViewWaypointData.setText("WP " + Integer.toString(position) + "	Alt:" + point.Height + "	" + command);

          return vi;
    }

}