package com.laser.ui.widgets;


import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.utils.LaserConstants;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BatteryTimer extends LinearLayout implements View.OnClickListener {

	private TextView txtTimer;
	private ImageButton btnStart;
	private ImageButton btnStop;
	
	private boolean bTimerStarted = false;
	private long timer = 0;

	private VrPadStationApp app;	
	
	public BatteryTimer(Context context, AttributeSet attr, VrPadStationApp app) {
	    super(context,attr);
	    this.app = app;
	    inflate(context, R.layout.battery_timer, this);
	    setupViewItems();
    }
	
	private void setupViewItems()
	{
		txtTimer = (TextView)findViewById(R.id.txtTimer);
		btnStart = (ImageButton)findViewById(R.id.btnStart);
		btnStop = (ImageButton)findViewById(R.id.btnStop);
		txtTimer.setText("00:00");
//		txtTimer.getBackground().setAlpha(175);
		btnStart.setTag("btnStart");
		btnStart.setOnClickListener(this);
		btnStart.getBackground().setAlpha(150);
		btnStop.setTag("btnStop");
		btnStop.setEnabled(false);
		btnStop.setOnClickListener(this);
		btnStop.getBackground().setAlpha(150);

		txtTimer.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
	}

    @Override
    public void onFinishInflate() {
    	// 	this is the right point to do some things with View objects,
    	// 	as example childs of THIS View object
    }

	@Override
	public void onClick(View v) {
		if (v.getTag().equals(btnStop.getTag()))
		{
			bTimerStarted = false;
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			if (isSetTimerOn())
				saveVal();
		}
		else if (v.getTag().equals(btnStart.getTag()))
		{
			bTimerStarted = true;
			timer = System.currentTimeMillis();
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
		}
	}

	public boolean isTimerStarted()
	{
		return bTimerStarted;
	}
	
	public long getStartTime()
	{
		return timer;
	}
	
	public void setVal(int val)
	{
		int min = val/60;
		int sec = val%60;
		String strMin = "";
		if (min < 10)
			strMin = "0"+min;
		else
			strMin = String.valueOf(min);
		String strSec = "";
		if (sec < 10)
			strSec = "0"+sec;
		else
			strSec = String.valueOf(sec);
		txtTimer.setText(strMin+":"+strSec);
	}
	
	public boolean isSetTimerOn()
	{
		return app.settings.SET_TIMER;
	}
	
	public void saveVal()
	{
		String time = txtTimer.getText().toString();
		String[] tokens = time.split(":");
		int val = Integer.parseInt(tokens[0]) + Integer.parseInt(tokens[1]);
		app.settings.TIMER = val;
//TODO:    	FileUtils.WriteIniFile("TIMER", settings.TIMER+"", settings.path);
	}
}
