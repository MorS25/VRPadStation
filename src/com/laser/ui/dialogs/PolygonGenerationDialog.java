package com.laser.ui.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.laser.helpers.Polygon;
import com.google.android.gms.maps.model.LatLng;
import com.laser.ui.widgets.SeekBarExtended;

public abstract class PolygonGenerationDialog {
	
	public abstract void onPolygonGenerated(List<waypoint> list);

	private Polygon poly;
	private LatLng origin;
	private SeekBarExtended seekBarAltitude;
	private SeekBarExtended seekBarDistance;
	private SeekBarExtended seekBarAngle;

	public void generatePolygon(double defaultHatchAngle,
								double defaultHatchDistance, 
								Polygon polygon, 
								LatLng originPoint, 
								double defaultPolygonAltitude, 
								Context context) {
		this.poly = polygon;
		this.origin = originPoint;

		if (!polygon.isPolygonValid()) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT).show();
			return;
		}

		AlertDialog dialog = buildDialog(context);
		seekBarAltitude.setValue(defaultPolygonAltitude);
		seekBarDistance.setValue(defaultHatchDistance);
		seekBarAngle.setValue(defaultHatchAngle);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Polygon Generator");
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		seekBarAltitude = new SeekBarExtended(context);
		seekBarAltitude.setMinMaxInc(0, 200, 1);
		seekBarAltitude.setText("Altitude:", true);
		seekBarAltitude.setUnit("m");
		
		seekBarAngle = new SeekBarExtended(context);
		seekBarAngle.setMinMaxInc(0, 180, 0.1);
		seekBarAngle.setText("Hatch angle:", false);
		seekBarAngle.setUnit("°");
		
		seekBarDistance = new SeekBarExtended(context);
		seekBarDistance.setMinMaxInc(5, 500, 5);
		seekBarDistance.setText("Distance between lines:", true);
		seekBarDistance.setUnit("m");

		layout.addView(seekBarAltitude);
		layout.addView(seekBarAngle);
		layout.addView(seekBarDistance);
		builder.setView(layout);
		
		builder.setNegativeButton("Cancel", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
		
		builder.setPositiveButton("Ok", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onPolygonGenerated(poly.hatchfill(seekBarAngle.getValue(), seekBarDistance.getValue(), origin, seekBarAltitude.getValue()));
			}
		});
		AlertDialog dialog = builder.create();
		
		return dialog;
	}

}
