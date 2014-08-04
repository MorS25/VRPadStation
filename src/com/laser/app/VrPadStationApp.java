package com.laser.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_read;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_override;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.laser.MAVLink.Drone;
import com.laser.MAVLink.Drone.DroneListener;
import com.laser.MAVLink.MAVLinkMessageHandler;
import com.laser.helpers.CustomLocationManager;
import com.laser.helpers.TTS;
import com.laser.parameters.Parameter;
import com.laser.parameters.ParameterManager;
import com.laser.parameters.ParameterManager.OnParameterManagerListener;
import com.laser.service.MAVLinkClient;
import com.laser.service.MAVLinkClient.OnMavlinkClientListener;
import com.laser.waypoints.WaypointMananger;
import com.laser.waypoints.WaypointMananger.OnWaypointManagerListener;
import com.laser.VrPadStation.R;
import com.laser.airports.Airport;
import com.laser.airports.Airports;
import com.laser.airports.Airports.AirportsListener;
import com.laser.connections.MAVLinkConnection;
import com.laser.helpers.FollowMe;
import com.laser.ui.widgets.ChannelManager;
import com.laser.utils.ErrorReporter;
import com.laser.utils.LaserConstants;
import com.laser.utils.LaserSettings;

public class VrPadStationApp extends Application implements OnMavlinkClientListener,
															OnWaypointManagerListener, 
															OnParameterManagerListener,
															AirportsListener,
															DroneListener {


    private final String TAG = VrPadStationApp.class.getSimpleName();

	public MAVLinkClient MAVClient;
	public Drone drone;
	private MAVLinkMessageHandler mavLinkMsgHandler;
	public TTS tts;
	public LibVLC mLibVLC;
	public WaypointMananger waypointMananger;
	public ParameterManager parameterMananger;
    private boolean bMavlinkConnected = false;
    public LaserSettings settings;	
    public ChannelManager channelManager;
	public FollowMe followMe;
	public CustomLocationManager locationManager;

	private double prev_SYSID_MYGCS = -1;
	private double rc1Trim = 0;
	private double rc2Trim = 0;
	private double rc3Trim = 0;
	private double rc4Trim = 0;
	private double rc3Min = 0;
	private double rc3Max = 0;
	
	private List<Airport> airports = new ArrayList<Airport>();
	
	private int total = 6 + 20 + 30 + 30;
	private int count = 0;
    
	private List<ConnectionStateListener> connectionListenerList = new ArrayList<ConnectionStateListener>();
	private List<OnParameterManagerListener> parameterListenerList = new ArrayList<OnParameterManagerListener>();
	private List<OnWaypointReceivedListener> waypointsListenerList = new ArrayList<OnWaypointReceivedListener>();
	private List<OnDroneUpdateListener> droneListenerList = new ArrayList<OnDroneUpdateListener>();
	private List<OnAirportsLoadedListener> airportsListenerList = new ArrayList<OnAirportsLoadedListener>();

	
	public interface OnAirportsLoadedListener {
		public void onAirportsLoaded(List<Airport> airports);
	}
	
	public interface OnDroneUpdateListener{
//		Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//		void onDroneTypeChanged();
		void onDroneArmedUpdate();
		void onDroneModeChanged();
	}
	
	public interface OnWaypointReceivedListener{
		public void onWaypointsReceived(List<waypoint> waypoints);
	}
	
	public interface ConnectionStateListener{
		public void mavlinkConnected();
		public void mavlinkDisconnected();
		public void mavlinkReceivedData(MAVLinkMessage msg);
		public void serialReceivedData(byte[] readData);
	}
	
	public boolean isMavlinkConnected()
	{
		return bMavlinkConnected;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
        ErrorReporter err = new ErrorReporter();
        err.Init(this);

		settings = new LaserSettings(this);
		channelManager = new ChannelManager(this);
		tts = new TTS(this);
		drone = new Drone(tts);
		MAVClient = new MAVLinkClient(this,this);
		waypointMananger = new WaypointMananger(MAVClient,this, drone);
		parameterMananger = new ParameterManager(MAVClient, this, drone);
		followMe = new FollowMe(MAVClient, this, drone);
		mavLinkMsgHandler = new MAVLinkMessageHandler(drone);
		locationManager = new CustomLocationManager(this);
		drone.setDroneActivityListener(this);

		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MAVLinkConnection.RECEIVE_NOTIFICATION);
		bManager.registerReceiver(bReceiver, intentFilter);

		BGLoadAirports();
		try {
            mLibVLC = LibVLC.getLibVlcInstance(getApplicationContext());
        } catch (LibVlcException e) {
            Log.d(TAG, "LibVLC initialisation failed");
            return;
        }
	}
	
	
	private boolean connectionStarted = false;
	@Override
	public void mavlinkReceivedData(MAVLinkMessage msg) {	
		
		if (drone.getsysId() == -1 && msg.msgid ==  msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT) 
		{
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			drone.setId(msg.sysid, msg.compid);
			drone.setType(msg_heart.type);			
		}	
		mavLinkMsgHandler.receiveData(msg);
		if (drone.getsysId() > 0) 
		{
			if (!connectionStarted) 
			{
				connectionStarted = true;
				setupMavlinkStreamRate();
				for (ConnectionStateListener csl : connectionListenerList)
					csl.mavlinkConnected();
				
				count = 0;
				getBaseParams();
				getRadioBaseParams();
				getGimbalParams();
				getOtherBaseParams();
			}
			waypointMananger.processMessage(msg);
			parameterMananger.processMessage(msg);
			for (ConnectionStateListener csl : connectionListenerList)
				csl.mavlinkReceivedData(msg);
			updateRcParams(msg);
		}
	}
	
	@Override
	public void serialReceivedData(byte[] readData) {
		for (ConnectionStateListener csl : connectionListenerList)
			csl.serialReceivedData(readData);
	}

	@Override
	public void mavlinkDisconnected() {
		bMavlinkConnected = false;
		for (ConnectionStateListener csl : connectionListenerList)
			csl.mavlinkDisconnected();
		tts.speak("Disconnected");
		count = 0;
	}

	@Override
	public void mavlinkConnected() {
		drone.invalidateId();
		bMavlinkConnected = true;
		connectionStarted = false;
		tts.speak("Connected");
	}
	
	
	private void setupMavlinkStreamRate()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		requestMavlinkDataStream(
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_ext_stat",//"0")));
						"2")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra1",//"0")));
						"10")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra2",//"0")));
						"2")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3",//"0")));
						"2")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_position",//"0")));
						"3")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS,
				Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors",//"0")));
						"0")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS,
				50);
	}

	private void requestMavlinkDataStream(int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate>0){
			msg.start_stop = 1;
		}else{
			msg.start_stop = 0;
		}
		MAVClient.sendMavPacket(msg.pack());
	}
	

	@Override
	public void onWaypointsReceived(List<waypoint> waypoints) {
		if (waypoints != null) {
			Toast.makeText(getApplicationContext(),
					"Waypoints received from Drone", Toast.LENGTH_SHORT).show();
			tts.speak("Waypoints received");
			drone.setHome(waypoints.get(0));
			waypoints.remove(0); // Remove Home waypoint
			drone.clearWaypoints();
			drone.addWaypoints(waypoints);
			for (OnWaypointReceivedListener wrl : waypointsListenerList)
				wrl.onWaypointsReceived(waypoints);
		}
	}

	@Override
	public void onWriteWaypoints(msg_mission_ack msg) {
		Toast.makeText(getApplicationContext(), "Waypoints sent",
				Toast.LENGTH_SHORT).show();
		tts.speak("Waypoints saved to Drone");
	}
	
	public void setConnectionStateListener(ConnectionStateListener listener) {
		connectionListenerList.add(listener);
	}
	
	public void setWaypointReceivedListener(OnWaypointReceivedListener listener){
		waypointsListenerList.add(listener);
	}
	
	public void setAirportsLoadedListener(OnAirportsLoadedListener listener) {
		airportsListenerList.add(listener);
	}
	
	public void setDroneUpdateListener(OnDroneUpdateListener listener){
		droneListenerList.add(listener);
	}
	
	public void setOnParametersChangedListener(OnParameterManagerListener listener){
		parameterListenerList.add(listener);
	}
	
	public void removeConnectionStateListener(ConnectionStateListener listener) {
		if (connectionListenerList.contains(listener))
			connectionListenerList.remove(listener);
	}

	public void removeWaypointReceivedListener(OnWaypointReceivedListener listener) {
		if (waypointsListenerList.contains(listener))
			waypointsListenerList.remove(listener);		
	}
	
	public void removeAirportsLoadedListener(OnAirportsLoadedListener listener) {
		if (airportsListenerList.contains(listener))
			airportsListenerList.remove(listener);
	}
	
	public void removeDroneUpdateListener(OnDroneUpdateListener listener) {
		if (droneListenerList.contains(listener))
			droneListenerList.remove(listener);
	}

	public void removeOnParametersChangedListener(OnParameterManagerListener listener) {
		if (parameterListenerList.contains(listener))
			parameterListenerList.remove(listener);		
	}


	@Override
	public void onParametersReceived() {
		for (OnParameterManagerListener pml : parameterListenerList)
		{
//			parameterMananger.sortParamsList();
			pml.onParametersReceived();
		}
	}

	@Override
	public void onParameterReceived(Parameter parameter, short paramIndex) {
		for (OnParameterManagerListener pml : parameterListenerList)
			pml.onParameterReceived(parameter, paramIndex);
		count++;
	}


	@Override
	public void onParamsCountReceived(int count) {
		for (OnParameterManagerListener pml : parameterListenerList)
			pml.onParamsCountReceived(count);
	}	

	public void toggleConnectionState() {
		if (MAVClient.isConnected()) {
			MAVClient.closeMavLink();
		}else{
			MAVClient.initMavLink();
		}		
	}
	
	public void setGuidedMode(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2;					// use guided mode enum
		msg.frame = 0; 						//  use correct parameter
		msg.command = (short) wp.cmd; //16;	//  use correct parameter
		msg.param1 = 0; 					//  use correct parameter
		msg.param2 = 0; 					//  use correct parameter
		msg.param3 = 0; 					//  use correct parameter
		msg.param4 = 0; 					//  use correct parameter
		msg.x = (float) wp.coord.latitude;
		msg.y = (float) wp.coord.longitude;
		msg.z = wp.Height.floatValue();
		msg.autocontinue = 1; 				//  use correct parameter
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAVClient.sendMavPacket(msg.pack());
	}
	
	public void changeFlightMode(ApmModes mode) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = drone.getsysId();
		msg.base_mode = 1; // use meaningful constant
		msg.custom_mode = mode.getNumber();
		MAVClient.sendMavPacket(msg.pack());			
	}
	
	private static final int StatusBarNotification = 1;
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(MAVLinkConnection.RECEIVE_NOTIFICATION))
    		{
	            String text = intent.getStringExtra("MAVLinkConnection");

	    		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
	    				VrPadStationApp.this).setSmallIcon(R.drawable.logo_laser_bianco_128)
	    				.setContentTitle(getResources().getString(R.string.app_name))
	    				.setContentText(text);
	    		PendingIntent contentIntent = PendingIntent.getActivity(VrPadStationApp.this, 0,
	    				new Intent(), 0);
	    		mBuilder.setContentIntent(contentIntent);
	    
	    		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    		mNotificationManager.notify(StatusBarNotification, mBuilder.build());
	        }
	    }
	};
	
	/********************************************************************************************
	 Queste sono le notifiche che ricevo quando leggo i parametri da file invece che dal drone
	 ********************************************************************************************/
	
	public void onParameterReceivedFromFile(Parameter parameter,
			short paramIndex) {
		for (OnParameterManagerListener pml : parameterListenerList)
			pml.onParameterReceived(parameter, paramIndex);
	}

	public void onParametersReceivedFromFile(List<Parameter> parameters) {
		for (OnParameterManagerListener pml : parameterListenerList)
		{
			parameterMananger.updateParametersListFromFile(parameters);
//			parameterMananger.sortParamsList();
			pml.onParametersReceived();
		}
	}

	public void onParamsCountReceivedFromFile(int count) {
		for (OnParameterManagerListener pml : parameterListenerList)
			pml.onParamsCountReceived(count);
	}

	/*********************************************************************************************/

//	Uso sempre la stessa immagine, quindi non mi serve sapere il tipo
//	@Override
//	public void onDroneTypeChanged() {
//		for (OnDroneUpdateListener dul : droneListenerList)
//			dul.onDroneTypeChanged();
//	}

	@Override
	public void onDroneArmedUpdate() {
		for (OnDroneUpdateListener dul : droneListenerList)
			dul.onDroneArmedUpdate();
	}

	@Override
	public void onDroneModeChanged() {
		for (OnDroneUpdateListener dul : droneListenerList)
			dul.onDroneModeChanged();
	}

	long lastSend = 0;
	public void sendRcOverrideMsg(short[] channelValuesArray)
	{		
		// SYSID_MYGCS = 255 altrimenti non funziona!!!!		
		msg_rc_channels_override msg = new msg_rc_channels_override();
		msg.chan1_raw = (short) channelValuesArray[0];
		msg.chan2_raw = (short) channelValuesArray[1];
		msg.chan3_raw = (short) channelValuesArray[2];
		msg.chan4_raw = (short) channelValuesArray[3];
		msg.chan5_raw = (short) channelValuesArray[4];
		msg.chan6_raw = (short) channelValuesArray[5];
		msg.chan7_raw = (short) channelValuesArray[6];
		msg.chan8_raw = (short) channelValuesArray[7];
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		MAVClient.sendMavPacket(msg.pack());
		
		if (LaserConstants.DEBUG)
		{
			Log.d(TAG + " RC", System.currentTimeMillis() - lastSend+"");
			lastSend = System.currentTimeMillis();
		}
	}
	
	// count = 30
	private void getGimbalParams()
	{
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RETRACT_X");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RETRACT_Y");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RETRACT_Z");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_NEUTRAL_X");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_NEUTRAL_Y");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_NEUTRAL_Z");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_CONTROL_X");
		MAVClient.sendMavPacket(msg.pack());

		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_CONTROL_Y");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_CONTROL_Z");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_REV");
		MAVClient.sendMavPacket(msg.pack());

		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_REV");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_REV");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_REV");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC10_REV");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC11_REV");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RC_IN_PAN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_STAB_PAN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMIN_PAN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMAX_PAN");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RC_IN_ROLL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_STAB_ROLL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMIN_ROL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMAX_ROL");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_RC_IN_TIL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_STAB_TILT");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMIN_TIL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMAX_TIL");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("CAM_SERVO_ON");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("CAM_SERVO_OFF");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("CAM_DURATION");
		MAVClient.sendMavPacket(msg.pack());
	}

	// count = 6
	private void getBaseParams()
	{
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("SYSID_MYGCS");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC1_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC2_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC4_TRIM");
		MAVClient.sendMavPacket(msg.pack());

		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC3_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC3_MAX");
		MAVClient.sendMavPacket(msg.pack());		
	}
	
	// count = 20
	private void getOtherBaseParams()
	{
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());

		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC10_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC11_FUNCTION");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC10_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC11_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC10_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC11_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMAX_TIL");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MNT_ANGMIN_TIL");
		MAVClient.sendMavPacket(msg.pack());
	}
	
	// count = 30
	public void getRadioBaseParams()
	{		
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC1_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC2_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC3_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC4_TRIM");
		MAVClient.sendMavPacket(msg.pack());
				
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_TRIM");
		MAVClient.sendMavPacket(msg.pack());
				
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_TRIM");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC1_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC2_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC3_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC4_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_MIN");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC1_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC2_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC3_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC4_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC5_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC6_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC7_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("RC8_MAX");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE1");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE2");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE3");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE4");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE5");
		MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = drone.getsysId();
		msg.target_component = drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("FLTMODE6");
		MAVClient.sendMavPacket(msg.pack());
	}
	
	
	
	private void updateRcParams(MAVLinkMessage msg)
	{
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			msg_param_value vv = (msg_param_value) msg;
			Parameter param = new Parameter(vv);
			
			if (param.name.equalsIgnoreCase("SYSID_MYGCS")) {
				prev_SYSID_MYGCS = param.value;
			}
			if (param.name.equalsIgnoreCase("RC1_TRIM")) {
				rc1Trim = param.value;
			}
			if (param.name.equalsIgnoreCase("RC2_TRIM")) {
				rc2Trim = param.value;
			}
			if (param.name.equalsIgnoreCase("RC4_TRIM")) {
				rc4Trim = param.value;
			}
			if (param.name.equalsIgnoreCase("RC3_MIN")) {
				rc3Min = param.value;
			}
			if (param.name.equalsIgnoreCase("RC3_MAX")) {
				rc3Max = param.value;
			}
			if (rc3Min != 0 && rc3Max != 0)
				rc3Trim = rc3Min + ((rc3Max - rc3Min) / 2);
		}
	}
	
	public void sendTakeOffMsg() 
	{
		//updateRcParams();
		
		// devo settarlo a 255 altrimenti non funziona
		//prev_SYSID_MYGCS = parameterMananger.getParamValue("SYSID_MYGCS");
		parameterMananger.sendParameter(new Parameter("SYSID_MYGCS", 255.0, 4));

		short[] channelsArray = new short[8];
		//int MID_PULSE = (settings.MAX_PULSE_WIDTH - settings.MIN_PULSE_WIDTH) + (settings.MAX_PULSE_WIDTH - settings.MIN_PULSE_WIDTH) / 2;
		channelsArray[0] = (short) rc1Trim;//MID_PULSE;
		channelsArray[1] = (short) rc2Trim;//MID_PULSE;
		channelsArray[2] = (short) rc3Trim;//MID_PULSE; // lo metto al 50% perchè se sto sotto il drone scende
		channelsArray[3] = (short) rc4Trim;//MID_PULSE;
		channelsArray[4] = (short) 0;
		channelsArray[5] = (short) 0;
		channelsArray[6] = (short) 0;
		channelsArray[7] = (short) 0;
		
		sendRcOverrideMsg(channelsArray);
		sendRcOverrideMsg(channelsArray);
		sendRcOverrideMsg(channelsArray);

		// ripristino il valore precedente
		if (prev_SYSID_MYGCS != -1)
			parameterMananger.sendParameter(new Parameter("SYSID_MYGCS", prev_SYSID_MYGCS, 4));
	}

	
	private ScheduledExecutorService scheduleTaskExecutor;

	public void enableRcOverride()
	{		
		// devo settarlo a 255 altrimenti non funziona
		//prev_SYSID_MYGCS = parameterMananger.getParamValue("SYSID_MYGCS");
		parameterMananger.sendParameter(new Parameter("SYSID_MYGCS", 255.0, 4));
		
		if (!isRcOverrided()) {
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					short[] channelsArray = new short[8];
					channelsArray[0] = (short) channelManager.getRoll().getVal();
					channelsArray[1] = (short) channelManager.getPitch().getVal();
					channelsArray[2] = (short) channelManager.getThrottle().getVal();
					channelsArray[3] = (short) channelManager.getYaw().getVal();
					channelsArray[4] = (short) channelManager.getModes().getVal();
					channelsArray[5] = (short) channelManager.getPotentiometer1().getVal();
					channelsArray[6] = (short) channelManager.getPotentiometer2().getVal();
					channelsArray[7] = (short) channelManager.getPotentiometer3().getVal();
					sendRcOverrideMsg(channelsArray);

					if (LaserConstants.DEBUG) {
						short[] chArr = channelsArray;
						Log.d("CHANNELS", chArr[0]+","+
										chArr[1]+","+
										chArr[2]+","+
										chArr[3]+","+
										chArr[4]+","+
										chArr[5]+","+
										chArr[6]+","+
										chArr[7]+"\n");
					}
				}
			}, 0, (long)(1000.0f/(float)settings.TRANSMISSION_RATE), TimeUnit.MILLISECONDS);
		}
	}
	
	private static final int DISABLE_OVERRIDE = 0;
	public void disableRcOverride() 
	{		
		if (isRcOverrided()) {
			scheduleTaskExecutor.shutdownNow();
			scheduleTaskExecutor = null;
		}
		
		short[] channelValuesArray = new short[8];
		channelValuesArray[0] = DISABLE_OVERRIDE;
		channelValuesArray[1] = DISABLE_OVERRIDE;
		channelValuesArray[2] = DISABLE_OVERRIDE;
		channelValuesArray[3] = DISABLE_OVERRIDE;
		channelValuesArray[4] = DISABLE_OVERRIDE;
		channelValuesArray[5] = DISABLE_OVERRIDE;
		channelValuesArray[6] = DISABLE_OVERRIDE;
		channelValuesArray[7] = DISABLE_OVERRIDE;
		sendRcOverrideMsg(channelValuesArray); 														
		sendRcOverrideMsg(channelValuesArray);
		sendRcOverrideMsg(channelValuesArray);

		// ripristino il valore precedente
		if (prev_SYSID_MYGCS != -1)
			parameterMananger.sendParameter(new Parameter("SYSID_MYGCS", prev_SYSID_MYGCS, 4));
	}
	
	public boolean isRcOverrided(){
		return (scheduleTaskExecutor!=null);
	}

	public boolean areBaseParamsReceived() {
		if (LaserConstants.DEBUG)
			Log.d("PARAMSCOUNT", count + " " + total);		
		if (count >= total)
			return true;
		return false;
	}
	
	public void setupMavlinkStreamRateForRadioCalibration() {
		// From MissionPlanner
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS, 0);
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, 0);
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2, 0);
		//requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, );
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, 0);
		//requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, );
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS, 10);
	}
	public void resetMavlinkStreamRate() {
		setupMavlinkStreamRate();
	}
		
    private void BGLoadAirports()
    {
        try {
//          Airports.ReadOurairports(this);
//          Airports.checkDuplicates = true;
//          Airports.ReadOpenflights(this);
        	Airports airports = new Airports();
        	airports.setListener(this);
        	airports.LoadAirports(this);
        }
        catch(Exception ex) {
        	ex.printStackTrace();
        }
    }

    @Override
    public void onAirportsLoaded(List<Airport> airports) {
    	this.airports = airports;
		for (OnAirportsLoadedListener all : airportsListenerList)
			all.onAirportsLoaded(airports);
    }

	public List<Airport> getAirportsList() {
		return airports;
	}



	
}
