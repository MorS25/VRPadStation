package com.laser.ui.activities;

import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_read;
import com.MAVLink.Messages.ardupilotmega.msg_param_set;
import com.google.android.gms.common.ConnectionResult;
import com.laser.ui.dialogs.OpenFileDialog;
import com.laser.ui.dialogs.OpenParameterDialog;
import com.laser.parameters.Parameter;
import com.laser.parameters.ParameterWriter;
import com.laser.parameters.ParameterManager.OnParameterManagerListener;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.helpers.AccelerometerCalibration;
import com.laser.helpers.CompassCalibration;
import com.laser.helpers.CustomLocationManager;
import com.laser.helpers.CustomLocationManager.CustomLocationManagerListener;
import com.laser.ui.fragments.AccCalibrationFragment;
import com.laser.ui.fragments.MagCalibrationFragment;
import com.laser.ui.fragments.FailsafeFragment;
import com.laser.ui.fragments.ParametersTableFragment;
import com.laser.ui.fragments.TerminalFragment;
import com.laser.ui.fragments.MagCalibrationFragment.MagCalibrationListener;
import com.laser.ui.fragments.AccCalibrationFragment.AccCalibrationListener;
import com.laser.ui.fragments.FailsafeFragment.FailsafeListener;
import com.laser.ui.fragments.ParametersTableFragment.OnListParamsListener;
import com.laser.ui.fragments.RadioCalibrationFragment;
import com.laser.ui.fragments.preferences.MAVLinkPrefsFragment;
import com.laser.ui.fragments.preferences.MapPrefsFragment;
import com.laser.ui.fragments.preferences.QuickModesSettingsFragment;
import com.laser.ui.fragments.SettingsListFragmentGcs;
import com.laser.ui.widgets.ParameterTableRow;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentSender;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class SettingsActivityGcs extends ParentActivity implements SettingsListFragmentGcs.OnFlyListFragmentItemClick,
																  OnParameterManagerListener,
																  MagCalibrationListener,
																  AccCalibrationListener,
																  FailsafeListener,
																  OnListParamsListener,
																  CustomLocationManagerListener {

	private VrPadStationApp app;
	private AccelerometerCalibration accCalibration;
	private CompassCalibration compCalibration;	
	private ParametersTableFragment parametersFragment = null;
	private MagCalibrationFragment magCalibrationFragment = null;
	private RadioCalibrationFragment radioCalibrationFragment = null;
	private FailsafeFragment failsafeFragment = null;
	private TerminalFragment terminalFragment = null;
	private AccCalibrationFragment accCalibrationFragment = null;
	private ProgressDialog dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getActionBar().hide();
		
		app = (VrPadStationApp) getApplication();
		accCalibration = new AccelerometerCalibration(app.MAVClient, app.drone);
		compCalibration = new CompassCalibration(app, this);
		app.setOnParametersChangedListener(this);
		app.locationManager.setCustomLocationManagerListener(this);
        
        // Manca il layout portrait. Lo lascio bloccato in landscape visto che l'applicazione si usa così.
        setContentView(R.layout.activity_settings_gcs);
        setResult(Activity.RESULT_CANCELED);
    }
    
	@Override
	public void mavlinkConnected() {
		super.mavlinkConnected();
	}
	
	@Override
	public void mavlinkDisconnected() {
		super.mavlinkDisconnected();		
	}
	
	@Override
	public void mavlinkReceivedData(MAVLinkMessage m) {
		super.mavlinkReceivedData(m);
		if (accCalibration != null)
			accCalibration.processMessage(m);
		if (compCalibration != null)
			compCalibration.processMessage(m);
		if (magCalibrationFragment != null)
			magCalibrationFragment.processMessage(m);
		if (radioCalibrationFragment != null)
			radioCalibrationFragment.processMessage(m);
		if (failsafeFragment != null)
			failsafeFragment.processMessage(m);
	}

	@Override
	public void serialReceivedData(byte[] readData) {
		super.serialReceivedData(readData);
		if (terminalFragment  != null)
			terminalFragment.processSerialData(readData);
	}
    
    @Override
    protected void onDestroy() {
		app.removeOnParametersChangedListener(this);
		app.locationManager.removeCustomLocationManagerListener(this);
    	super.onDestroy();
    }
    
	@Override
	public void onBackPressed() {
		if (SettingsActivity.bEdited)
		{
			Intent intent = new Intent();
			intent.putExtra("bForceRestart", SettingsActivity.bForceRestart);
			setResult(Activity.RESULT_OK, intent);
		}
		else
			setResult(Activity.RESULT_CANCELED);			
		finish();
	}

    @Override
    public void onClick(int item) {
    	
            switch (item)
            {
            case 0:	// Map
	            {
	            	View detailView = findViewById(R.id.detailContainer);
	                if(detailView==null) {
	                	// Activity per portrait
	                }
	                else {
	                	MapPrefsFragment mapPrefsFragment = new MapPrefsFragment();
	                	FragmentManager fragmentManager = this.getFragmentManager();
	                    fragmentManager.beginTransaction()
	                    .replace(R.id.detailContainer, mapPrefsFragment)
	                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
	                    .commit();
	                }
	            }
	        	break;
            case 1:		// MAVLink
	            {
	            	View detailView = findViewById(R.id.detailContainer);
	                if(detailView==null) {
	                	// Activity per portrait
	                }
	                else {
	                	MAVLinkPrefsFragment mavlinkPrefsFragment = new MAVLinkPrefsFragment();
	                	FragmentManager fragmentManager = this.getFragmentManager();
	                    fragmentManager.beginTransaction()
	                    .replace(R.id.detailContainer, mavlinkPrefsFragment)
	                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
	                    .commit();
	                }
	            }
            break;
            case 2:		// Flight Modes
	            {
	            	View detailView = findViewById(R.id.detailContainer);
	                if(detailView==null) {
	                	// Activity per portrait
	                }
	                else {
	                	QuickModesSettingsFragment modesFragment = new QuickModesSettingsFragment();
	                	FragmentManager fragmentManager = this.getFragmentManager();
	                    fragmentManager.beginTransaction()
	                    .replace(R.id.detailContainer, modesFragment)
	                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
	                    .commit();
	                }
	            }
	        	break;
            case 3:	// Parameters
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	parametersFragment = new ParametersTableFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, parametersFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            		parametersFragment.setListeners(this);
                }
            }
            break;
            case 4:	// Acc Calibration
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	accCalibrationFragment = new AccCalibrationFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, accCalibrationFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                    accCalibrationFragment.setListeners(this);
                }
            }
            break;
            case 5: // Mag Calibration
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	magCalibrationFragment = new MagCalibrationFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, magCalibrationFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                    magCalibrationFragment.setListeners(this);
                }
            }
            break;
            case 6:	// Radio Calibration
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	radioCalibrationFragment = new RadioCalibrationFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, radioCalibrationFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
            break;
            case 7:	// Failsafe
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	failsafeFragment  = new FailsafeFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, failsafeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                    failsafeFragment.setListeners(this);
                }
            }
            break;
            case 8:	// Terminal
            {
            	View detailView = findViewById(R.id.detailContainer);
                if(detailView==null) {
                	// Activity per portrait
                }
                else {
                	terminalFragment  = new TerminalFragment();
                	FragmentManager fragmentManager = this.getFragmentManager();
                    fragmentManager.beginTransaction()
                    .replace(R.id.detailContainer, terminalFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
                }
            }
            break;
        }
        /*
        // Recupero la vista detailContainer
        View detailView = findViewById(R.id.detailContainer);
        if(detailView==null) {
                // Non esiste spazio per la visualizzazione del dattagli, quindi ho necessità di lanciare una nuova activity.
                // Carico gli arguments nell'intent di chiamata.
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtras(arguments);
                startActivity(intent);
        		finish();
        }
        else {
                // Esiste lo spazio, procedo con la creazione del Fragment!
                MyDetailFragment myDetailFragment = new MyDetailFragment();
                // Imposto gli argument del fragment.
                myDetailFragment.setArguments(arguments);
               
                // Procedo con la sostituzione del fragment visualizzato.
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                .replace(R.id.detailContainer, myDetailFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        }*/
    }

	@Override
	public void onParametersReceived() {
		if (parametersFragment != null)
			parametersFragment.onParametersReceived();
		Toast.makeText(this, "Parameters Received", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onParameterReceived(Parameter parameter, short paramIndex) {
		if (parametersFragment != null)
			parametersFragment.onParameterReceived(parameter, paramIndex);
	}

	@Override
	public void onParamsCountReceived(int count) {
		if (parametersFragment != null)
			parametersFragment.setProgressBarMax(count);
	}

	@Override
	public void writeParamsToApm() {
		writeModifiedParametersToDrone();		
	}

	@Override
	public void writeParamsToFile() {
		saveParametersToFile(app.parameterMananger.getParametersList());
	}

	@Override
	public void loadParamsFromApm() {
		if (app.MAVClient.isConnected()) {
			app.parameterMananger.getAllParameters();	
		}else{
			Toast.makeText(this, "Please connect first", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void loadParamsFromFile() {
		openParametersFromFile();
	}
	
	public void writeModifiedParametersToDrone() {
		List<ParameterTableRow> modRows = parametersFragment.getModifiedParametersRows();
		for (ParameterTableRow row : modRows) 
		{
			if (!row.isNewValueEqualToDroneParameter())
			{
				app.parameterMananger.sendParameter(row.getParameterFromRow());
				// Aggiorno la lista parametri del parameter manager
				if (app.parameterMananger.contains(row.getParameterFromRow().name))
				{
					int index = app.parameterMananger.indexOf(row.getParameterFromRow().name);
					app.parameterMananger.getParametersList().set(index, row.getParameterFromRow());
				}
				// Aggiorno la riga della lista parametri
				row.setParam(row.getParameterFromRow());
				row.restoreColor();
			}						
		}	
		Toast.makeText(this, "Write "+modRows.size()+" parameters", Toast.LENGTH_SHORT).show();
	}
	
	public void saveParametersToFile(List<Parameter> parameterList) {
		if (parameterList.size()>0) {
			ParameterWriter parameterWriter = new ParameterWriter(parameterList);
			if(parameterWriter.saveParametersToFile()){
				Toast.makeText(this, "Parameters saved", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "No parameters", Toast.LENGTH_SHORT).show();
		}
	}
		
	public void openParametersFromFile() {
		OpenFileDialog dialog = new OpenParameterDialog() {
			@Override
			public void parameterFileLoaded(List<Parameter> parameters) {
				app.onParamsCountReceivedFromFile(parameters.size());
				for (int i = 0; i < parameters.size(); i++)
				{
					app.onParameterReceivedFromFile(parameters.get(i), (short) i);
				}	
				app.onParametersReceivedFromFile(parameters);
			}
		};
		dialog.launchDialog(this);
	}
	
	@Override
	public void onStartAccCalibration() {
		if (app.isMavlinkConnected())
			accCalibration.startCalibration(this);
		else
			Toast.makeText(this, "MAVLink not connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStartMagCalibration() {
		if (app.isMavlinkConnected())
			compCalibration.MagCalibration();
		else
			Toast.makeText(this, "MAVLink not connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRefreshCalibrationParams() {
		refreshCalibrationParams();
	}

	private void refreshCalibrationParams()
	{
		if (app.isMavlinkConnected())
		{
			String[] params = {	"COMPASS_USE",
								"MAG_ENABLE",
								"COMPASS_LEARN",
								"COMPASS_EXTERNAL",
								"COMPASS_AUTODEC",
								"COMPASS_DEC",
								"COMPASS_ORIENT" };
			
			for (int i = 0; i < params.length; i++)
			{
				msg_param_request_read msg = new msg_param_request_read();
				msg.target_system = app.drone.getsysId();
				msg.target_component = app.drone.getcompId();
				msg.param_index = -1;
				msg.setParam_Id(params[i]);
				app.MAVClient.sendMavPacket(msg.pack());
			}
		}
		else
			Toast.makeText(this, "MAVLink not connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSendCalibrationParam(Parameter parameter) {
		sendParam(parameter);
	}
	
	private void sendParam(Parameter parameter) {
		if (parameter != null)
		{
			msg_param_set msg = new msg_param_set();
			msg.target_system = app.drone.getsysId();
			msg.target_component = app.drone.getcompId();
			msg.setParam_Id(parameter.name);
			msg.param_type = (byte) parameter.type;
			msg.param_value = (float) parameter.value;
			app.MAVClient.sendMavPacket(msg.pack());
		}
	}

	@Override
	public void onRefreshFailsafeParams() {
		refreshFailsafeParams();
	}
	
	private void refreshFailsafeParams()
	{
		if (app.isMavlinkConnected())
		{
			String[] params = {	"FS_THR_ENABLE",
								"FS_BATT_ENABLE",
								"FS_THR_VALUE",
								"FS_BATT_MAH",
								"FS_GCS_ENABLE",
								"LOW_VOLT",
								"FS_BATT_VOLTAGE" };
			
			for (int i = 0; i < params.length; i++)
			{
				msg_param_request_read msg = new msg_param_request_read();
				msg.target_system = app.drone.getsysId();
				msg.target_component = app.drone.getcompId();
				msg.param_index = -1;
				msg.setParam_Id(params[i]);
				app.MAVClient.sendMavPacket(msg.pack());
			}
		}
		else
			Toast.makeText(this, "MAVLink not connected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSendFailsafeParam(Parameter parameter) {
		sendParam(parameter);
	}

	@Override
	public void onCheckYaw() {
		if (app.isMavlinkConnected()) {
			c = 0;
			gpsYaw = new float[4];
			droneYaw = new float[4];
			checkYawDialog("Walk straight until you have a stable direction, "
							+ "with the drone pointing in the same direction.");
		} else
			Toast.makeText(this, "MAVLink not connected", Toast.LENGTH_SHORT).show();
	}
	
	private void checkYawDialog(String message) {
		dialog = new ProgressDialog(this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setTitle(message);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				c++;
				if (c == 1)
					checkYawDialog("Walk right until you have a stable direction, "
							+ "with the drone pointing in the same direction.");
				else if (c == 2)
					checkYawDialog("Walk left until you have a stable direction, "
							+ "with the drone pointing in the same direction.");
				else if (c == 3)
					checkYawDialog("Walk behind until you have a stable direction, "
							+ "with the drone pointing in the same direction.");	
				else if (c == 4)
					checkYawResultsDialog();
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	
	private void checkYawResultsDialog() {
		String message = "1°- Device: " + String.format("%.2f", gpsYaw[0]) + "°  Drone: " + String.format("%.2f", droneYaw[0]) + "°" +
						"\n2°- Device: " + String.format("%.2f", gpsYaw[1]) + "°  Drone: " + String.format("%.2f", droneYaw[1]) + "°" + 
						"\n3°- Device: " + String.format("%.2f", gpsYaw[2]) + "°  Drone: " + String.format("%.2f", droneYaw[2]) + "°" + 
						"\n4°- Device: " + String.format("%.2f", gpsYaw[3]) + "°  Drone: " + String.format("%.2f", droneYaw[3]) + "°" +
						"\n\nIf difference for each measure is above 10°, redoing calibration.";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Results");
		builder.setMessage(message);
		builder.setPositiveButton("Ok", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}
	
	private int c = 0;
	private float[] gpsYaw = new float[4];
	private float[] droneYaw = new float[4];
	@Override
	public void onLocationChanged(Location location) {
		if (c < gpsYaw.length) {
			gpsYaw[c] =location.getBearing();
			droneYaw[c] = (float) app.drone.getYaw();
			if (dialog != null) {
				dialog.setMessage("Device yaw: " + gpsYaw[c] + "\nDrone yaw: " + droneYaw[c]);
			}
		}
	}

	@Override
	public void onLocationManagerConnectionFailed(ConnectionResult connectionResult) {
		if (!paused) {
			showToast("Location service - connection failed - " + connectionResult.getErrorCode());
			/*
	         * Google Play services can resolve some errors it detects.
	         * If the error has a resolution, try sending an Intent to
	         * start a Google Play services activity that can resolve error.
	         */
	        if (connectionResult.hasResolution()) {
	            try {
	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(this, CustomLocationManager.CONNECTION_FAILURE_RESOLUTION_REQUEST);
	                // Thrown if Google Play services canceled the original PendingIntent
	            } catch (IntentSender.SendIntentException e) {
	                // Log the error
	                e.printStackTrace();
	            }
	        } else {
	            // If no resolution is available, display a dialog to the user with the error./
	            //showErrorDialog(connectionResult.getErrorCode());
	        }
		}
	}
	
	private boolean paused = false;
	
	@Override
	protected void onPause() {
		paused = true;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		paused = false;
		super.onResume();
	}

	@Override
	public void onLocationManagerConnected() {
		if (!paused)
			showToast("Location service connected");
	}

	@Override
	public void onLocationManagerDisconnected() {
		if (!paused)
			showToast("Location service disconnected");
	}
	
}
