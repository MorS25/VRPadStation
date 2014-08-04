package com.laser.helpers;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_read;
import com.MAVLink.Messages.ardupilotmega.msg_param_set;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;
import com.laser.app.VrPadStationApp;
import com.laser.parameters.Parameter;

public class CompassCalibration {

	private VrPadStationApp app;
	private Context context;
	
	private ProgressDialog progressDialog;
	private double[] ans;
	
	private int RATE_SENSORS = 2;
	
	private boolean bAbort = false;	

	private float ofs_x = 0;
	private float ofs_y = 0;
	private float ofs_z = 0;
	
	private int ofs_flag = 0;
	
	private Parameter pOfsX, pOfsY, pOfsZ, pCompassLearn, pMagEnable;
	
	private boolean bParamsSended = false;

	private long startTime = -1;
	
	private short oldmx = 0;
	private short oldmy = 0;
	private short oldmz = 0;

	private ArrayList<Tuple> data = new ArrayList<Tuple>();
	
	private int backupratesens = RATE_SENSORS;
	
	private Handler handler;
	private Runnable r = new Runnable() {	
		@Override
		public void run() {
			if (!bAbort)
			{
				boolean stop = false;
				if (ofs_flag == 7)
				{
					if (startTime == -1)
					{
						progressDialog.setMessage("Data will be collected for 60 seconds, Please click ok and move the apm around all axises");
						collectDataStart();
					}
	
					if (System.currentTimeMillis() - startTime <= 60000)
						collectData();
					else 
						stop = true;
				}
				if (!stop)
					handler.postDelayed(r, 100);
				else {
					boolean ret = collectDataStop();
					progressDialog.dismiss();
				}
			} else {
				collectDataAbort();
				progressDialog.dismiss();
			}
		}
	};

	public CompassCalibration(VrPadStationApp app, Context context) {
		this.app = app;
		this.context = context;
		handler = new Handler();
	}
	
	public void MagCalibration()
	{	
		resetOffsets();
		showDialogCalibration();
	}
	
	private void resetOffsets() {
		app.parameterMananger.sendParameter(new Parameter("COMPASS_OFS_X", 0.0));
		app.parameterMananger.sendParameter(new Parameter("COMPASS_OFS_Y", 0.0));
		app.parameterMananger.sendParameter(new Parameter("COMPASS_OFS_Z", 0.0));
	}

	public void processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			msg_param_value vv = (msg_param_value) msg;
			Parameter param = new Parameter(vv);
			
			if (param.name.equalsIgnoreCase("COMPASS_OFS_X"))
			{
				ofs_x = (float) param.value;
				ofs_flag |= 1;
				pOfsX = param;
			}
			if (param.name.equalsIgnoreCase("COMPASS_OFS_Y"))
			{
				ofs_y = (float) param.value;
				ofs_flag |= 2;
				pOfsY = param;
			}
			if (param.name.equalsIgnoreCase("COMPASS_OFS_Z"))
			{
				ofs_z = (float) param.value;
				ofs_flag |= 4;
				pOfsZ = param;
			}
			if (param.name.equalsIgnoreCase("COMPASS_LEARN"))
			{
				pCompassLearn = param;
			}
			if (param.name.equalsIgnoreCase("MAG_ENABLE"))
			{
				pMagEnable = param;
			}
			
			if (bParamsSended)
			{
				if (ofs_flag == 7)					
				{
					bParamsSended = false;
					showDialogCalibrationCompleted(ofs_x, ofs_y, ofs_z);
				}
			}
		}
	}

	private void collectData()
	{		        		
		if (app.drone.m_last_imu_raw != null)
		{
			if ((oldmx != app.drone.m_last_imu_raw.xmag) &&
	            (oldmy != app.drone.m_last_imu_raw.ymag) &&
	            (oldmz != app.drone.m_last_imu_raw.zmag))
	        {
	            data.add(new Tuple(
	            		app.drone.m_last_imu_raw.xmag - ofs_x,
	            		app.drone.m_last_imu_raw.ymag - ofs_y,
	            		app.drone.m_last_imu_raw.zmag - ofs_z ));
	            
	            
	            oldmx = app.drone.m_last_imu_raw.xmag;
	            oldmy = app.drone.m_last_imu_raw.ymag;
	            oldmz = app.drone.m_last_imu_raw.zmag;
	        }		
		}
		
		long timeRemaining = 60  - (System.currentTimeMillis() - startTime) / 1000;
        progressDialog.setMessage("Data will be collected for 60 seconds.\nPlease click OK and move the APM around all axises \n\nCollected " + data.size() + " samples\nRemaining time: " + timeRemaining + " seconds");

	}

	private void collectDataStart()
	{		
		startTime = System.currentTimeMillis();
		oldmx = 0;
	    oldmy = 0;
	    oldmz = 0;
	    data.clear();
	    
	    if (pMagEnable != null)
	    {
	    	pMagEnable.value = 1;
	    	sendParameter(pMagEnable);
	    }
		
		backupratesens = RATE_SENSORS;
		RATE_SENSORS = 10;
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, RATE_SENSORS);
	}
	
	private boolean collectDataStop()
	{
		boolean ret = false;
		RATE_SENSORS = backupratesens;
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, RATE_SENSORS);
		
		if (data.size() < 10)
        {
			showDialogCalibrationFailed();
            ans = null;
        } else {
        	double[] x = new double[data.size()];
        	double[] y = new double[data.size()];
        	double[] z = new double[data.size()];
        	for (int i = 0; i < data.size(); i++)
        	{
        		x[i] = data.get(i).x;
        		y[i] = data.get(i).y;
        		z[i] = data.get(i).z;
        	}
	        ans = LeastSq(x, y, z, data.size(), 0, 0.0000000001);
	
			if (ans != null)
			{
				SaveOffsets(ans);
	        	ret = true;
	        	bParamsSended = true;
			}
        }
		
		startTime = -1;
		oldmx = 0;
	    oldmy = 0;
	    oldmz = 0;
	    data.clear();
		ofs_x = 0;
		ofs_y = 0;
		ofs_z = 0;
		ofs_flag = 0;
        return ret;
	}
	
	private void SaveOffsets(double[] ofs)
	{
		if (pCompassLearn != null)
		{
			pCompassLearn.value = 0;
			sendParameter(pCompassLearn);
		}
		if (pOfsX != null)
		{
			pOfsX.value = -ofs[0];
			sendParameter(pOfsX);
		}
		if (pOfsY != null)
		{
			pOfsY.value = -ofs[1];
			sendParameter(pOfsY);
		}
		if (pOfsZ != null)
		{
			pOfsZ.value = -ofs[2];
			sendParameter(pOfsZ);
		}		
	}
	
	public void sendParameter(Parameter parameter) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		app.MAVClient.sendMavPacket(msg.pack());
	}
		
	private void collectDataAbort()
	{
		startTime = -1;
		oldmx = 0;
	    oldmy = 0;
	    oldmz = 0;
	    data.clear();
		ofs_x = 0;
		ofs_y = 0;
		ofs_z = 0;
		ofs_flag = 0;
	}
	
	private void readOffsets()
	{	
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, RATE_SENSORS);
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, RATE_SENSORS);
		
		ofs_flag = 0;

		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("MAG_ENABLE");
		app.MAVClient.sendMavPacket(msg.pack());

		msg = new msg_param_request_read();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("COMPASS_LEARN");
		app.MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("COMPASS_OFS_X");
		app.MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("COMPASS_OFS_Y");
		app.MAVClient.sendMavPacket(msg.pack());
		
		msg = new msg_param_request_read();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		msg.param_index = -1;
		msg.setParam_Id("COMPASS_OFS_Z");
		app.MAVClient.sendMavPacket(msg.pack());
				
		handler.postDelayed(r, 100);
	}
	
	public static class Tuple
	{
		public float x, y, z;
	    public Tuple(float x, float y, float z)
	    {
	        this.x = x;
	        this.y = y;
	        this.z = z;
	    }
	}
	
	public void requestMavlinkDataStream(int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate>0){
			msg.start_stop = 1;
		}else{
			msg.start_stop = 0;
		}
		app.MAVClient.sendMavPacket(msg.pack());
	}
	
	private void showDialogCalibration()
	{
		bAbort = false;
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.setMessage("Click OK to start calibration");
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				readOffsets();
				showProgressDialog();
			}
		});
		alertDialog.show();
	}
	
	private void showDialogCalibrationCompleted(float ofs_x, float ofs_y, float ofs_z)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.setTitle("Calibration Completed");
		alertDialog.setMessage("New offset X: " + ofs_x + 
							   "\nNew offset Y: " + ofs_y +
							   "\nNew offset Z: " + ofs_z);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				resetStreamRates();
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}
	
	private void showDialogCalibrationFailed()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setCanceledOnTouchOutside(true);
		alertDialog.setTitle("Calibration Failed!");
		alertDialog.setMessage("Log does not contain enough data");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				resetStreamRates();
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}
	
	private void resetStreamRates()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
		
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_extra3", "2")));
		requestMavlinkDataStream(MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, Integer.parseInt(prefs.getString("pref_mavlink_stream_rate_raw_sensors", "0")));
	}
	
	private void showProgressDialog()
	{
		bAbort = false;
		progressDialog = new ProgressDialog(context);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage("Reading offsets...");
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {				
			public void onClick(DialogInterface dialog, int which) {
				bAbort = true;
				resetStreamRates();
			}
		});
		progressDialog.show();
	}

	 private double[] LeastSq(double x[], double y[], double z[],
                              int size, int max_iterations, double delta)
	 {

         double x_sumplain = 0.0f;
         double x_sumsq = 0.0f;
         double x_sumcube = 0.0f;

         double y_sumplain = 0.0f;
         double y_sumsq = 0.0f;
         double y_sumcube = 0.0f;

         double z_sumplain = 0.0f;
         double z_sumsq = 0.0f;
         double z_sumcube = 0.0f;

         double xy_sum = 0.0f;
         double xz_sum = 0.0f;
         double yz_sum = 0.0f;

         double x2y_sum = 0.0f;
         double x2z_sum = 0.0f;
         double y2x_sum = 0.0f;
         double y2z_sum = 0.0f;
         double z2x_sum = 0.0f;
         double z2y_sum = 0.0f;

         for (int i = 0; i < size; i++) {

                 double x2 = x[i] * x[i];
                 double y2 = y[i] * y[i];
                 double z2 = z[i] * z[i];

                 x_sumplain += x[i];
                 x_sumsq += x2;
                 x_sumcube += x2 * x[i];

                 y_sumplain += y[i];
                 y_sumsq += y2;
                 y_sumcube += y2 * y[i];

                 z_sumplain += z[i];
                 z_sumsq += z2;
                 z_sumcube += z2 * z[i];

                 xy_sum += x[i] * y[i];
                 xz_sum += x[i] * z[i];
                 yz_sum += y[i] * z[i];

                 x2y_sum += x2 * y[i];
                 x2z_sum += x2 * z[i];

                 y2x_sum += y2 * x[i];
                 y2z_sum += y2 * z[i];

                 z2x_sum += z2 * x[i];
                 z2y_sum += z2 * y[i];
         }

         //
         //Least Squares Fit a sphere A,B,C with radius squared Rsq to 3D data
         //
         //    P is a structure that has been computed with the data earlier.
         //    P.npoints is the number of elements; the length of X,Y,Z are identical.
         //    P's members are logically named.
         //
         //    X[n] is the x component of point n
         //    Y[n] is the y component of point n
         //    Z[n] is the z component of point n
         //
         //    A is the x coordiante of the sphere
         //    B is the y coordiante of the sphere
         //    C is the z coordiante of the sphere
         //    Rsq is the radius squared of the sphere.
         //
         //This method should converge; maybe 5-100 iterations or more.
         //
         double x_sum = x_sumplain / size;        //sum( X[n] )
         double x_sum2 = x_sumsq / size;    //sum( X[n]^2 )
         double x_sum3 = x_sumcube / size;    //sum( X[n]^3 )
         double y_sum = y_sumplain / size;        //sum( Y[n] )
         double y_sum2 = y_sumsq / size;    //sum( Y[n]^2 )
         double y_sum3 = y_sumcube / size;    //sum( Y[n]^3 )
         double z_sum = z_sumplain / size;        //sum( Z[n] )
         double z_sum2 = z_sumsq / size;    //sum( Z[n]^2 )
         double z_sum3 = z_sumcube / size;    //sum( Z[n]^3 )

         double XY = xy_sum / size;        //sum( X[n] * Y[n] )
         double XZ = xz_sum / size;        //sum( X[n] * Z[n] )
         double YZ = yz_sum / size;        //sum( Y[n] * Z[n] )
         double X2Y = x2y_sum / size;    //sum( X[n]^2 * Y[n] )
         double X2Z = x2z_sum / size;    //sum( X[n]^2 * Z[n] )
         double Y2X = y2x_sum / size;    //sum( Y[n]^2 * X[n] )
         double Y2Z = y2z_sum / size;    //sum( Y[n]^2 * Z[n] )
         double Z2X = z2x_sum / size;    //sum( Z[n]^2 * X[n] )
         double Z2Y = z2y_sum / size;    //sum( Z[n]^2 * Y[n] )

         //Reduction of multiplications
         double F0 = x_sum2 + y_sum2 + z_sum2;
         double F1 =  0.5f * F0;
         double F2 = -8.0f * (x_sum3 + Y2X + Z2X);
         double F3 = -8.0f * (X2Y + y_sum3 + Z2Y);
         double F4 = -8.0f * (X2Z + Y2Z + z_sum3);

         //Set initial conditions:
         double A = x_sum;
         double B = y_sum;
         double C = z_sum;

         //First iteration computation:
         double A2 = A * A;
         double B2 = B * B;
         double C2 = C * C;
         double QS = A2 + B2 + C2;
         double QB = -2.0f * (A * x_sum + B * y_sum + C * z_sum);

         //Set initial conditions:
         double Rsq = F0 + QB + QS;

         //First iteration computation:
         double Q0 = 0.5f * (QS - Rsq);
         double Q1 = F1 + Q0;
         double Q2 = 8.0f * (QS - Rsq + QB + F0);
         double aA, aB, aC, nA, nB, nC, dA, dB, dC;

         //Iterate N times, ignore stop condition.
         int n = 0;

         while (n < max_iterations) {
                 n++;

                 //Compute denominator:
                 aA = Q2 + 16.0f * (A2 - 2.0f * A * x_sum + x_sum2);
                 aB = Q2 + 16.0f * (B2 - 2.0f * B * y_sum + y_sum2);
                 aC = Q2 + 16.0f * (C2 - 2.0f * C * z_sum + z_sum2);
                 aA = (aA == 0.0f) ? 1.0f : aA;
                 aB = (aB == 0.0f) ? 1.0f : aB;
                 aC = (aC == 0.0f) ? 1.0f : aC;

                 //Compute next iteration
                 nA = A - ((F2 + 16.0f * (B * XY + C * XZ + x_sum * (-A2 - Q0) + A * (x_sum2 + Q1 - C * z_sum - B * y_sum))) / aA);
                 nB = B - ((F3 + 16.0f * (A * XY + C * YZ + y_sum * (-B2 - Q0) + B * (y_sum2 + Q1 - A * x_sum - C * z_sum))) / aB);
                 nC = C - ((F4 + 16.0f * (A * XZ + B * YZ + z_sum * (-C2 - Q0) + C * (z_sum2 + Q1 - A * x_sum - B * y_sum))) / aC);

                 //Check for stop condition
                 dA = (nA - A);
                 dB = (nB - B);
                 dC = (nC - C);

                 if ((dA * dA + dB * dB + dC * dC) <= delta) { break; }

                 //Compute next iteration's values
                 A = nA;
                 B = nB;
                 C = nC;
                 A2 = A * A;
                 B2 = B * B;
                 C2 = C * C;
                 QS = A2 + B2 + C2;
                 QB = -2.0f * (A * x_sum + B * y_sum + C * z_sum);
                 Rsq = F0 + QB + QS;
                 Q0 = 0.5f * (QS - Rsq);
                 Q1 = F1 + Q0;
                 Q2 = 8.0f * (QS - Rsq + QB + F0);
         }

         double ret[] = {A, B, C, (double) Math.sqrt(Rsq)}; 
//         *sphere_x = A;
//         *sphere_y = B;
//         *sphere_z = C;
//         *sphere_radius = sqrtf(Rsq);

         return ret;
	 }
	
}
