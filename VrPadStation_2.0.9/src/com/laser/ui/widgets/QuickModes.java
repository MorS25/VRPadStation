package com.laser.ui.widgets;


import java.util.ArrayList;

import com.MAVLink.Messages.ApmModes;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.utils.LaserConstants;

import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class QuickModes extends LinearLayout implements View.OnClickListener {

	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button button6;
	private ArrayList<Button> listBtns = new ArrayList<Button>();
	
	private Channel ch;
	private VrPadStationApp app;	

	public static final int MODE_1_VAL = 1115;
	public static final int MODE_2_VAL = 1295;
	public static final int MODE_3_VAL = 1425;
	public static final int MODE_4_VAL = 1555;
	public static final int MODE_5_VAL = 1685;
	public static final int MODE_6_VAL = 1875;
	
	public QuickModes(VrPadStationApp app, AttributeSet attr) {
	    super(app,attr);
	    this.ch = app.channelManager.getModes();
	    this.app = app;
	    //setId(ch.getId());
	    inflate(app, R.layout.quick_modes, this);
	    setupViewItems();
	    updateLabels();
    }
	
	int selected = 1;
	
	private void setupViewItems()
	{
		button1 = (Button)findViewById(R.id.button1);
		button1.setTag("button1");
		button1.setEnabled(true);
		button1.setOnClickListener(this);
		button1.getBackground().setAlpha(180);
		button1.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		button2 = (Button)findViewById(R.id.button2);
		button2.setTag("button2");
		button2.setEnabled(true);
		button2.setOnClickListener(this);
		button2.getBackground().setAlpha(180);
		button2.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		button3 = (Button)findViewById(R.id.button3);
		button3.setTag("button3");
		button3.setEnabled(true);
		button3.setOnClickListener(this);
		button3.getBackground().setAlpha(180);
		button3.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		button4 = (Button)findViewById(R.id.button4);
		button4.setTag("button4");
		button4.setEnabled(true);
		button4.setOnClickListener(this);
		button4.getBackground().setAlpha(180);
		button4.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		button5 = (Button)findViewById(R.id.button5);
		button5.setTag("button5");
		button5.setEnabled(true);
		button5.setOnClickListener(this);
		button5.getBackground().setAlpha(180);
		button5.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		button6 = (Button)findViewById(R.id.button6);
		button6.setTag("button6");
		button6.setEnabled(true);
		button6.setOnClickListener(this);
		button6.getBackground().setAlpha(180);
		button6.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		listBtns.add(button1);
		listBtns.add(button2);
		listBtns.add(button3);
		listBtns.add(button4);
		listBtns.add(button5);
		listBtns.add(button6);
	}

    @Override
    public void onFinishInflate() {
    	// 	this is the right point to do some things with View objects,
    	// 	as example childs of THIS View object
    }

	@Override
	public void onClick(View v) {
		for (int i = 0; i < listBtns.size(); i++)
		{
			if (listBtns.get(i) == v)
			{
				listBtns.get(i).setEnabled(false);
				selected = i+1;
			}
			else
				listBtns.get(i).setEnabled(true);
		}

		if (v == button1)
			ch.setVal(MODE_1_VAL);
		else if (v == button2)
			ch.setVal(MODE_2_VAL);
		else if (v == button3)
			ch.setVal(MODE_3_VAL);
		else if (v == button4)
			ch.setVal(MODE_4_VAL);
		else if (v == button5)
			ch.setVal(MODE_5_VAL);
		else if (v == button6)
			ch.setVal(MODE_6_VAL);
	}


	public void updateLabels()
	{		
		button1.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE1"), app.drone.getType()).getName());
		button2.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE2"), app.drone.getType()).getName());
		button3.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE3"), app.drone.getType()).getName());
		button4.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE4"), app.drone.getType()).getName());
		button5.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE5"), app.drone.getType()).getName());
		button6.setText(ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE6"), app.drone.getType()).getName());
		
		if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE1"), app.drone.getType())))
		{
			button1.setEnabled(false);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(true);
			button6.setEnabled(true);
		}
		else if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE2"), app.drone.getType())))
		{
			button1.setEnabled(true);
			button2.setEnabled(false);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(true);
			button6.setEnabled(true);
		}
		else if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE3"), app.drone.getType())))
		{
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(false);
			button4.setEnabled(true);
			button5.setEnabled(true);
			button6.setEnabled(true);
		}
		else if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE4"), app.drone.getType())))
		{
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(false);
			button5.setEnabled(true);
			button6.setEnabled(true);
		}
		else if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE5"), app.drone.getType())))
		{
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(false);
			button6.setEnabled(true);
		}
		else if (app.drone.getMode() == (ApmModes.getMode((int) app.parameterMananger.getParameterValue("FLTMODE6"), app.drone.getType())))
		{
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(true);
			button6.setEnabled(false);
		}
		else
		{
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(true);
			button6.setEnabled(true);
		}
	}
	
	public int getSelected()
	{
		switch (selected) {
		case 1:
			return MODE_1_VAL;
		case 2:
			return MODE_2_VAL;
		case 3:
			return MODE_3_VAL;
		case 4:
			return MODE_4_VAL;
		case 5:
			return MODE_5_VAL;
		case 6:
			return MODE_6_VAL;
		default:
			return app.settings.RC5_TRIM;
		}
	}
}
