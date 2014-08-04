package com.laser.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.laser.VrPadStation.R;
import com.laser.utils.LaserConstants;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class FlightModesAdapter extends ArrayAdapter{

    private LayoutInflater inflater;
    private List<View> listOfViews = new ArrayList<View>();

    @SuppressWarnings("unchecked")
	public FlightModesAdapter ( Context ctx, int resourceId, List<String> flightModes) {
          super( ctx, resourceId, flightModes );          
          inflater = LayoutInflater.from( ctx );
    }

    @Override
    public View getView ( int position, View convertView, ViewGroup parent ) {

    	  View vi = inflater.inflate(R.layout.flight_mode_list_item, null);

          String mode = (String) getItem( position );

          TextView textViewWaypointData = (TextView) vi.findViewById(R.id.textViewMode);
          textViewWaypointData.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_LARGE);
          textViewWaypointData.setText(mode);

    	  listOfViews.add(vi);
          return vi;
    }

	public void setBackgroundColor(String mode) {
		for (int i = 0; i < listOfViews.size(); i++)
		{
			TextView textView = (TextView) listOfViews.get(i).findViewById(R.id.textViewMode);
			if (textView.getText().toString().equalsIgnoreCase(mode))
			{
				listOfViews.get(i).setBackgroundColor(Color.BLUE);
			}
			else
				listOfViews.get(i).setBackgroundColor(Color.TRANSPARENT);
		}
	}
}