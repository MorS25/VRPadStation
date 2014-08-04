package com.laser.ui.fragments;

import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.utils.LaserConstants;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TerminalFragment extends Fragment implements OnClickListener {

	private ScrollView scrollTerminal;
	private TextView txtTerminal;
	private Button btnSetupShow;
	private Button btnRadioSetup;
	private Button btnTests;
	private Button btnLogsDownload;
	private Button btnLogsBrowse;
	private Button btnRebootApm;
	private Button btnDisconnect;
	private Button btnSendCommand;
	private EditText editTextInput;
	private VrPadStationApp app;
	
		
	@Override
	public void onAttach(Activity activity) {
		app = (VrPadStationApp) getActivity().getApplication();
		super.onAttach(activity);
	}	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.terminal_fragment, container, false);
		
		scrollTerminal = (ScrollView) view.findViewById(R.id.scrollTerminal);
		txtTerminal = (TextView) view.findViewById(R.id.txtTerminal);
		btnSetupShow = (Button) view.findViewById(R.id.btnSetupShow);
		btnRadioSetup = (Button) view.findViewById(R.id.btnRadioSetup);
		btnTests = (Button) view.findViewById(R.id.btnTests);
		btnLogsDownload = (Button) view.findViewById(R.id.btnLogsDownload);
		btnLogsBrowse = (Button) view.findViewById(R.id.btnLogsBrowse);
		btnRebootApm = (Button) view.findViewById(R.id.btnRebootApm);
		btnDisconnect = (Button) view.findViewById(R.id.btnDisconnect);
		btnSendCommand = (Button) view.findViewById(R.id.btnSendCommand);
		editTextInput = (EditText) view.findViewById(R.id.editTextInput);
		
		editTextInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		txtTerminal.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnSetupShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnRadioSetup.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnTests.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnLogsDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnLogsBrowse.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnRebootApm.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnDisconnect.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		btnSendCommand.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		
		btnSetupShow.setOnClickListener(this);
		btnRadioSetup.setOnClickListener(this);
		btnTests.setOnClickListener(this);
		btnLogsDownload.setOnClickListener(this);
		btnLogsBrowse.setOnClickListener(this);
		btnRebootApm.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnSendCommand.setOnClickListener(this);
		
		btnDisconnect.setEnabled(false);
		return view;	
	}
	
	@Override
	public void onResume() {
		scrollTerminal.post(new Runnable() {            
		    @Override
		    public void run() {
		    	scrollTerminal.fullScroll(View.FOCUS_DOWN);              
		    }
		});		
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == btnSetupShow) {
			app.MAVClient.sendSerialData("exit\rsetup\rshow\r".getBytes());			
		} else if (v == btnRadioSetup) {
			app.MAVClient.sendSerialData("exit\rsetup\r\nradio\r".getBytes());			
		} else if (v == btnTests) {
			app.MAVClient.sendSerialData("exit\rtest\r?\r\n".getBytes());			
		} else if (v == btnLogsDownload) {	
			//TODO:
			Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_SHORT).show();
		} else if (v == btnLogsBrowse) {	
			//TODO:		
			Toast.makeText(getActivity(), "Not yet implemented", Toast.LENGTH_SHORT).show();
		} else if (v == btnRebootApm) {
					
	        if (LaserConstants.USB_SERIAL_CONNECTION) {
	            btnDisconnect.setEnabled(true);
	            return;
	        }	        
            startTerminal();
	        
		} else if (v == btnDisconnect) {	          
			disconnectTerminal();
		} else if (v == btnSendCommand) {
			byte[] cmd = editTextInput.getText().toString().getBytes();
			app.MAVClient.sendSerialData(cmd);	
			editTextInput.setText("");
		}
	}
	
	private void disconnectTerminal() {
		app.MAVClient.sendSerialData("reboot\n".getBytes());	
		LaserConstants.USB_SERIAL_CONNECTION = false;
        txtTerminal.append("Closed\n");
	}
	
	private void sendRebootMessage(int param1) {
		msg_command_long msg = new msg_command_long();
		msg.command = (short) MAV_CMD.MAV_CMD_PREFLIGHT_REBOOT_SHUTDOWN;
		msg.param1 = param1; 
		msg.param2 = 0; 
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.target_system = app.drone.getsysId();
		msg.target_component = app.drone.getcompId();
		app.MAVClient.sendMavPacket(msg.pack());
	}
	
	private boolean viewCreated = false;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewCreated = true;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewCreated = false;
		app.MAVClient.sendSerialData("\rexit\rreboot\r".getBytes());	
		LaserConstants.USB_SERIAL_CONNECTION = false;
	}
	
	private void setText(byte[] readData) {
		
		int a = 0;
		byte[] buffer = new byte[readData.length];
		for (int i = 0; i < readData.length; i++)
        {
            if (readData[i] >= 0x20 && readData[i] < 0x7f ||
        		readData[i] == (int)'\n' || 
				readData[i] == 0x1b)
            {
            	buffer[a] = readData[i];
            	a++;
            }
//            if (readData[i] == '\n')
//                break;
        }
		String message = new String(buffer) + "\n";
		editTextInput.setSelection(editTextInput.getText().length());		

        //data = data.TrimEnd('\r'); // else added \n all by itself        
        message = message.replace("\0", "");
        message = message.replace((char)0x1b+"[K",""); // remove control code
		if ((txtTerminal.getText().length() + message.length()) > 9000)
			txtTerminal.setText("");
		txtTerminal.setText(message);   

        if (message.contains("\b"))
        {
        	message.replace("\b", "");
//    		editTextInput.setSelection(editTextInput.getText().length());	
        }
       
//        // erase to end of line. in our case jump to end of line
//        if (message.contains((char)0x1b + "[K"))
//        {
//    		editTextInput.setSelection(editTextInput.getText().length());	
//        }
		
		Log.d("TERMINALLLLL", message);
		//TODO:	scrollTerminal.smoothScrollTo(0, txtTerminal.getBottom());		
	}

	public void processSerialData(byte[] readData) {
		if (viewCreated) {
			if (rebooted)
				dataReceived = true;
			setText(readData);
		}
	}
	
	//TODO
	private boolean rebooted = false;
	private boolean dataReceived = false;
	private long startTime = 0;
	private Handler handler = new Handler();
	private Runnable runnableReboot = new Runnable() {		
		@Override
		public void run() {
			rebooted = true;
			if (System.currentTimeMillis() - startTime < 10000) {
				if (dataReceived) {
					enableCLI();
					rebooted = false;
					dataReceived = false;
				}
				else
					handler.postDelayed(runnableReboot, 200);
			} else {
				rebooted = false;
				dataReceived = false;
			}
		}
	};
	private void enableCLI()
	{
		app.MAVClient.sendSerialData("\n\n\n".getBytes());	
		app.MAVClient.sendSerialData("\r\r\r?\r".getBytes());
        txtTerminal.append("Opened com port\r\n");
	}
	private void startTerminal() 
	{		
        txtTerminal.append("Rebooting via MAVLink\n");
		sendRebootMessage(1);
        txtTerminal.append("Waiting for reboot\n");

		LaserConstants.USB_SERIAL_CONNECTION = true;		
		btnDisconnect.setEnabled(true);
		
		startTime = System.currentTimeMillis();
		
		// wait 7 seconds for px4 reboot
		handler.postDelayed(runnableReboot, 7000);		
	}
	
	
}
