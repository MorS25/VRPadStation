package com.laser.ui.layers;

import com.laser.MAVLink.Drone;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.widgets.BatteryTimer;
import com.laser.ui.widgets.HUDwidget;
import com.laser.ui.widgets.Pot;
import com.laser.ui.widgets.Pot.PotListener;
import com.laser.ui.widgets.TrimHorizontal.TrimHorizontalListener;
import com.laser.ui.widgets.TrimVertical.TrimVerticalListener;
import com.laser.ui.widgets.QuickModes;
import com.laser.ui.widgets.TrimHorizontal;
import com.laser.ui.widgets.TrimVertical;
import com.laser.utils.LaserConstants;
import com.laser.utils.LaserConstants.UnderlayModes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlyInterfaceLayer implements OnClickListener,
										  TrimVerticalListener,
										  TrimHorizontalListener {
	

	private VrPadStationApp app;
	private Context context;
	
	private LinearLayout flyUserInterface;		//100
	private LinearLayout topBar;				//101
	private RelativeLayout bottomBar;			//102
	private LinearLayout centralLayout;			//103
	private LinearLayout centralColumn;			//104
	
	private TrimHorizontal trimRoll;			//111
	private TrimHorizontal trimYaw;				//112
    private BatteryTimer btnTimer;				//113
    private QuickModes btnQuickModes;			//114
    private TrimVertical trimPitch;				//115
    private TrimVertical trimThrottle;			//116
    private Pot pot1;							//117
  	private Pot pot2;							//118
  	private Pot pot3;							//119
	private HUDwidget hudWidget;				//120
	//private TextView txtRc;						//121	
	private ImageView imgBattery;				//122
	private TextView txtBattery;				//123
	private ImageButton btnSwitchUnderlay;		//126
	private RelativeLayout hudLayout;
	private Button btnToggleRcOverride;			//127
	
    	
	public interface FlyInterfaceListener {
		public void onSwitchToMap();
		public void onSwitchToCam();
		public void onStartStream();
		public void onTrimClick();
		public void rcOverrideStateChanged();
	}
	
	private FlyInterfaceListener listener;
	public void setListener(FlyInterfaceListener listener)
	{
		this.listener = listener;
	}
    
    public FlyInterfaceLayer(Context context, VrPadStationApp app)
    {
    	this.app = app;
    	this.context = context;
    	
    	createFlyInterface(app.drone);
    	LaserConstants.UNDERLAY_MODE = UnderlayModes.MAP;
    }
    
    private void createFlyInterface(Drone drone)
	{		
		createBaseLayout();
		createTopBar();
		createBottomBar();
		createCentralLayout();
		createHud(drone);
		
		if (LaserConstants.DEBUG_LAYOUT)
		{	
			topBar.setBackgroundColor(Color.RED);
			bottomBar.setBackgroundColor(Color.RED);	
		}		

		flyUserInterface.addView(topBar);
		flyUserInterface.addView(centralLayout);
		flyUserInterface.addView(bottomBar);
		//flyUserInterface.addView(hudLayer.getHudLayer());
	}

	public View getFlyUserInterface() {
    	return flyUserInterface;
    }
	
    private void createBaseLayout() {
    	flyUserInterface = new LinearLayout(context);
    	flyUserInterface.setId(100);
		LinearLayout.LayoutParams lpFlyUserInterface = new LinearLayout.LayoutParams(  
				LinearLayout.LayoutParams.MATCH_PARENT,  
				LinearLayout.LayoutParams.MATCH_PARENT);  
		flyUserInterface.setWeightSum(10.0f);
		flyUserInterface.setOrientation(LinearLayout.VERTICAL);
		flyUserInterface.setLayoutParams(lpFlyUserInterface);
    }
    
    private void createTopBar() {
    	topBar = new LinearLayout(context);
		topBar.setId(101); 
		LinearLayout.LayoutParams lpTopBar= new LinearLayout.LayoutParams(  
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
		topBar.setWeightSum(10.0f);
		topBar.setClickable(true);
		topBar.setLayoutParams(lpTopBar); 

		createSwitchUnderlayButton();
		createBatteryMonitor();
		createQuickModes();
		createBtnToggleRcOverride();
	}
    
    private void createBtnToggleRcOverride()
    {
		LinearLayout.LayoutParams lpBtnToggleRcOverride = new LinearLayout.LayoutParams(  
				0, LinearLayout.LayoutParams.MATCH_PARENT, 2.0f);
		lpBtnToggleRcOverride.setMargins(2, 2, 2, 2);
		final LayoutInflater inflater = LayoutInflater.from(context);
		btnToggleRcOverride = (Button) inflater.inflate(R.layout.button_template_mainmenu, topBar, false);
		btnToggleRcOverride.setId(127);
		btnToggleRcOverride.setPadding(2, 2, 2, 2);
		btnToggleRcOverride.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		if (app.isRcOverrided())
		{
			btnToggleRcOverride.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
			btnToggleRcOverride.setText("Disable RC");
		}
		else
		{
			btnToggleRcOverride.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
			btnToggleRcOverride.setText("Enable RC");
		}
		btnToggleRcOverride.setLayoutParams(lpBtnToggleRcOverride);
		btnToggleRcOverride.setOnClickListener(this);
		topBar.addView(btnToggleRcOverride);
    }
    
    private void createSwitchUnderlayButton()
    {
		LinearLayout.LayoutParams lpBtnSwitchUnderlay = new LinearLayout.LayoutParams(  
				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		lpBtnSwitchUnderlay.setMargins(2, 2, 2, 2);
		final LayoutInflater inflater = LayoutInflater.from(context);
		btnSwitchUnderlay = (ImageButton) inflater.inflate(R.layout.imagebutton_template_mainmenu, topBar, false);
		btnSwitchUnderlay.setId(125);
		btnSwitchUnderlay.setScaleType(ScaleType.CENTER_INSIDE);
		btnSwitchUnderlay.setPadding(2, 2, 2, 2);
		btnSwitchUnderlay.setLayoutParams(lpBtnSwitchUnderlay);
		btnSwitchUnderlay.setOnClickListener(this);
		topBar.addView(btnSwitchUnderlay);
		updateBtnSwitchUnderlay();
    }
        
//    private void addFirstSpace() {
//		LinearLayout.LayoutParams lpSpace = new LinearLayout.LayoutParams(  
//				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
//		Space space = new Space(context);
//		space.setLayoutParams(lpSpace);
//		topBar.addView(space);
//    }
    
    private void createBatteryMonitor() {
		LinearLayout.LayoutParams lpBatteryLayout = new LinearLayout.LayoutParams(  
				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		FrameLayout batteryLayout = new FrameLayout(context);
		lpBatteryLayout.setMargins(2, 2, 2, 2);
		batteryLayout.setLayoutParams(lpBatteryLayout);	
		topBar.addView(batteryLayout);
		
		FrameLayout.LayoutParams lpBattery = new FrameLayout.LayoutParams(  
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		
		imgBattery = new ImageView(context);
		imgBattery.setId(122);
		imgBattery.setScaleType(ScaleType.CENTER_INSIDE);
		imgBattery.setAlpha(0.8f);
		imgBattery.setImageDrawable(context.getResources().getDrawable(R.drawable.battery));
		imgBattery.setLayoutParams(lpBattery);	
		batteryLayout.addView(imgBattery);
		
		txtBattery = new TextView(context);
		txtBattery.setId(123);
		txtBattery.setTextColor(Color.BLACK);
		txtBattery.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_SMALL);
		txtBattery.setGravity(Gravity.CENTER);
		txtBattery.setLayoutParams(lpBattery);			
		batteryLayout.addView(txtBattery);
    }

	public void updateBattery(int level, boolean ac) {
		if (ac)
			txtBattery.setText(level + "% AC");
		else
			txtBattery.setText(level + "%");
		if (level >= 80)
			imgBattery.setColorFilter(Color.GREEN, Mode.MULTIPLY);
		else if (level < 80 && level >= 40)
			imgBattery.setColorFilter(Color.YELLOW, Mode.MULTIPLY);
		else if (level < 40)
			imgBattery.setColorFilter(Color.RED, Mode.MULTIPLY);
	}
    
    private void createQuickModes()
    {
		LinearLayout.LayoutParams lpBtnModes = new LinearLayout.LayoutParams(  
				0, LinearLayout.LayoutParams.MATCH_PARENT, 6.0f);
		btnQuickModes = new QuickModes(app, null);
	    btnQuickModes.setId(114);
	    btnQuickModes.setLayoutParams(lpBtnModes);			
		topBar.addView(btnQuickModes);
    }
        
    private void createBottomBar() {		
		bottomBar = new RelativeLayout(context);
		bottomBar.setId(102);
		LinearLayout.LayoutParams lpBottomBar = new LinearLayout.LayoutParams(  
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);  
		bottomBar.setClickable(true);
		bottomBar.setLayoutParams(lpBottomBar); 

		createTimer();
		createHorizontalTrims();
    }
    
	private void createTimer()
	{
		RelativeLayout.LayoutParams lpTimer = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT); 
		lpTimer.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		
		btnTimer = new BatteryTimer(context, null, app);
		btnTimer.setId(113);
	    btnTimer.setLayoutParams(lpTimer);
	    
		bottomBar.addView(btnTimer);
	}	
    
    private void createHorizontalTrims() {
		
		RelativeLayout.LayoutParams horTrim1 = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT); 
		horTrim1.addRule(RelativeLayout.LEFT_OF, btnTimer.getId());  

		RelativeLayout.LayoutParams horTrim2 = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT); 
		horTrim2.addRule(RelativeLayout.RIGHT_OF, btnTimer.getId());  
		
	    trimRoll = new TrimHorizontal(context, null, app.channelManager.getRoll());
	    trimYaw = new TrimHorizontal(context, null, app.channelManager.getYaw());
	    trimRoll.setId(111);
	    trimYaw.setId(112);
	    
	    // dispongo i trim in base all'RC mode
		switch (app.settings.RC_MODE)
		{
		case 1:
		case 2:
			bottomBar.addView(trimYaw);
			bottomBar.addView(trimRoll);	    
			trimYaw.setLayoutParams(horTrim1);	
		    trimRoll.setLayoutParams(horTrim2);	
			break;
		case 3:
		case 4:
			bottomBar.addView(trimRoll);
			bottomBar.addView(trimYaw);
		    trimRoll.setLayoutParams(horTrim1);		    
			trimYaw.setLayoutParams(horTrim2);	
			break;
		}
		
		trimRoll.setListener(this);
		trimYaw.setListener(this);
    }
    
    
    
    
    private void createCentralLayout() {				
    	centralLayout = new LinearLayout(context);
    	centralLayout.setId(103);
		LinearLayout.LayoutParams lpCentralLayout = new LinearLayout.LayoutParams(  
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 8.0f); 
		centralLayout.setWeightSum(10.0f);   
		centralLayout.setOrientation(LinearLayout.HORIZONTAL);
		centralLayout.setGravity(Gravity.CENTER);
		centralLayout.setLayoutParams(lpCentralLayout);	
		
		centralColumn = new LinearLayout(context);
		centralColumn.setId(104);		
		LinearLayout.LayoutParams lpCentralColumn = new LinearLayout.LayoutParams(  
				0, LinearLayout.LayoutParams.MATCH_PARENT, 3.0f);    
		centralColumn.setClickable(true);
		centralColumn.setWeightSum(10.0f);
		centralColumn.setOrientation(LinearLayout.VERTICAL);
		centralColumn.setLayoutParams(lpCentralColumn);  

		centralLayout.addView(centralColumn);
		
		createPots();
		createVerticalTrims();
    }
    
	private void createPots() {
		LinearLayout potsLayout = new LinearLayout(context);
		LinearLayout.LayoutParams lpPotsLayout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.0f);
		potsLayout.setClickable(true);
		potsLayout.setLayoutParams(lpPotsLayout);
		potsLayout.setWeightSum(3.0f);
		if (LaserConstants.DEBUG_LAYOUT)
			potsLayout.setBackgroundColor(Color.YELLOW);
		
		centralColumn.addView(potsLayout);			
		
		// Pot 1
		pot1 = new Pot(context, app.channelManager.getPotentiometer1());
		pot1.setId(117);
		pot1.setKnobListener(knobListener);
		LinearLayout.LayoutParams lpPot1 = new LinearLayout.LayoutParams(
				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
  	    pot1.setLayoutParams(lpPot1);
		pot1.setAlpha(0.8f);	
		
		// Pot 2
		pot2 = new Pot(context, app.channelManager.getPotentiometer2());
		pot2.setId(118);
		pot2.setKnobListener(knobListener);
		LinearLayout.LayoutParams lpPot2 = new LinearLayout.LayoutParams(
				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
  	    pot2.setLayoutParams(lpPot2);
		pot2.setAlpha(0.8f);	
		
		// Pot 3
		pot3 = new Pot(context, app.channelManager.getPotentiometer3());
		pot3.setId(119);
		pot3.setKnobListener(knobListener);
		LinearLayout.LayoutParams lpPot3 = new LinearLayout.LayoutParams(
				0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);  
		pot3.setLayoutParams(lpPot3);
		pot3.setAlpha(0.8f);		

  	    potsLayout.addView(pot1);
  	    potsLayout.addView(pot2);
  	    potsLayout.addView(pot3);
	}
	
	private PotListener knobListener = new PotListener() {	
	public void onKnobChanged(int pot, int arg, int val) {
		 if (arg > 0)
	        ; // rotate right 
	      else
	        ; // rotate left 	
		 
		 if (pot == pot1.getPotId())
		 {
			 app.channelManager.getPotentiometer1().setVal(pot1.getValue());
		 }
		 else if (pot == pot2.getPotId())
		 {
			 app.channelManager.getPotentiometer2().setVal(pot2.getValue());
		 }
		 else if (pot == pot3.getPotId())
		 {
			 app.channelManager.getPotentiometer3().setVal(pot3.getValue());
		 }
	}
	public void onKnobStop() {}
};
    
    private void createVerticalTrims() {

		RelativeLayout centralLayout = new RelativeLayout(context);
		LinearLayout.LayoutParams lpTrimsLayout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.0f);
		centralLayout.setClickable(true);
		centralLayout.setLayoutParams(lpTrimsLayout);
		if (LaserConstants.DEBUG_LAYOUT)
			centralLayout.setBackgroundColor(Color.GREEN);
		
		centralColumn.addView(centralLayout);	
				
		RelativeLayout.LayoutParams verTrim1 = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT); 
		verTrim1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, centralLayout.getId());
		verTrim1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, centralLayout.getId());

		RelativeLayout.LayoutParams verTrim2 = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT); 
		verTrim2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, centralLayout.getId());
		verTrim2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, centralLayout.getId());	
		
	    trimPitch = new TrimVertical(context, null, app.channelManager.getPitch());
	    trimThrottle = new TrimVertical(context, null, app.channelManager.getThrottle());
	    trimPitch.setId(115);
	    trimThrottle.setId(116);	    

		RelativeLayout.LayoutParams lpTxtRc = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT);
	    
	    // dispongo i trim in base all'RC mode
		switch (app.settings.RC_MODE)
		{
		case 1:	
		case 3:		
			centralLayout.addView(trimPitch);			
			centralLayout.addView(trimThrottle);
			trimPitch.setLayoutParams(verTrim1);
			trimThrottle.setLayoutParams(verTrim2);

			lpTxtRc.addRule(RelativeLayout.RIGHT_OF, trimPitch.getId());
			lpTxtRc.addRule(RelativeLayout.LEFT_OF, trimThrottle.getId());
			break;
		case 2:
		case 4:
			centralLayout.addView(trimThrottle);
			centralLayout.addView(trimPitch);	
			trimThrottle.setLayoutParams(verTrim1);
			trimPitch.setLayoutParams(verTrim2);

			lpTxtRc.addRule(RelativeLayout.RIGHT_OF, trimThrottle.getId());
			lpTxtRc.addRule(RelativeLayout.LEFT_OF, trimPitch.getId());
			break;
		}	
		

		trimThrottle.setListener(this);
		trimPitch.setListener(this);
			
//		txtRc = new TextView(context);
//		txtRc.setId(121);
//		txtRc.setLayoutParams(lpTxtRc);
//		txtRc.setPadding(2, 2, 2, 2);
//		txtRc.setTextColor(Color.WHITE);
//		txtRc.setTextSize(LaserConstants.TEXT_SIZE - 4);
//		txtRc.setText("");		
//		centralLayout.addView(txtRc);		
    }
    
    private void createHud(Drone drone) {
		hudLayout = new RelativeLayout(context);
		LinearLayout.LayoutParams lpHudLayout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 4.0f);
		hudLayout.setLayoutParams(lpHudLayout);
		if (LaserConstants.DEBUG_LAYOUT)
			hudLayout.setBackgroundColor(Color.BLUE);
		
		centralColumn.addView(hudLayout);
		
		hudWidget = new HUDwidget(context, null, true, true);
		hudWidget.setId(120);
		
		RelativeLayout.LayoutParams lpHudWidget = new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT); 
		hudWidget.setLayoutParams(lpHudWidget);
		hudLayout.addView(hudWidget);

		hudWidget.setDrone(drone);
		hudWidget.onDroneUpdate();
    }
   		
	public void hidePotentiometers()
	{
		if (!app.settings.SHOW_POTS)
		{
			pot1.setVisibility(View.INVISIBLE);
			pot2.setVisibility(View.INVISIBLE);
			pot3.setVisibility(View.INVISIBLE);
		}
		else
		{
			pot1.setVisibility(View.VISIBLE);
			pot2.setVisibility(View.VISIBLE);
			pot3.setVisibility(View.VISIBLE);
		}
	}	
	public void hideTrims()
	{
		if (!app.settings.SHOW_TRIMS)
		{
			trimRoll.setVisibility(View.INVISIBLE);
			trimPitch.setVisibility(View.INVISIBLE);
			trimYaw.setVisibility(View.INVISIBLE);
			trimThrottle.setVisibility(View.INVISIBLE);
		}
		else
		{
			trimRoll.setVisibility(View.VISIBLE);
			trimPitch.setVisibility(View.VISIBLE);
			trimYaw.setVisibility(View.VISIBLE);
			trimThrottle.setVisibility(View.VISIBLE);
		}

		hideBottomBar();
	}	
	public void hideTimer()
	{
		if (!app.settings.SHOW_TIMER)
			btnTimer.setVisibility(View.INVISIBLE);
		else
			btnTimer.setVisibility(View.VISIBLE);

		hideBottomBar();
	}
	
	private void hideBottomBar()
	{		
		if (!app.settings.SHOW_TIMER && !app.settings.SHOW_TRIMS)
		{
			bottomBar.setVisibility(View.GONE);
			
			LinearLayout.LayoutParams lpHudLayout = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 0, 5.0f);
			hudLayout.setLayoutParams(lpHudLayout);
			hudLayout.removeView(hudWidget);
			centralColumn.removeView(hudLayout);
			centralColumn.addView(hudLayout);
			hudLayout.addView(hudWidget);
		}
		else
		{
			bottomBar.setVisibility(View.VISIBLE);
			
			LinearLayout.LayoutParams lpHudLayout = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 0, 4.0f);
			hudLayout.setLayoutParams(lpHudLayout);
			hudLayout.removeView(hudWidget);
			centralColumn.removeView(hudLayout);
			centralColumn.addView(hudLayout);
			hudLayout.addView(hudWidget);
		}
	}

	public void updateFlyInterface() {
		app.channelManager.updateChannels(app.settings, btnQuickModes.getSelected());			
		btnQuickModes.updateLabels();				
		trimRoll.updateTrim();
		trimYaw.updateTrim();
		trimPitch.updateTrim();
		trimThrottle.updateTrim();
	}

	public BatteryTimer getTimer()
	{
		return btnTimer;
	}
	
	public void setRcText(String rc) {
		//txtRc.setText(rc);
	}

	public void changeToMap() {
		LaserConstants.UNDERLAY_MODE = UnderlayModes.MAP;
		updateBtnSwitchUnderlay();
		//listener.onStopStream();
	}

	public void changeToCam() {
		LaserConstants.UNDERLAY_MODE = UnderlayModes.CAM;
		updateBtnSwitchUnderlay();
		listener.onStartStream();
	}
	
	private void updateBtnSwitchUnderlay() {
		if (btnSwitchUnderlay != null)
		{
			if (LaserConstants.UNDERLAY_MODE == UnderlayModes.CAM)
				btnSwitchUnderlay.setImageDrawable(context.getResources().getDrawable(R.drawable.underlay_map));
			else
				btnSwitchUnderlay.setImageDrawable(context.getResources().getDrawable(R.drawable.underlay_cam));
		}
	}

	public void setTxtRcVisibility(int visible) {
		//txtRc.setVisibility(visible);
	}
	
	@Override
	public void onClick(View v) {
		if (v == btnToggleRcOverride)
		{
			if (app.isRcOverrided())
				app.disableRcOverride();
			else
				app.enableRcOverride();
			
			if (app.isRcOverrided())
			{
				btnToggleRcOverride.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
				btnToggleRcOverride.setText("Disable RC");

				if (app.isMavlinkConnected() && !app.areBaseParamsReceived())
					Toast.makeText(app, "Error. Reconnect.", Toast.LENGTH_SHORT).show();
			}
			else
			{
				btnToggleRcOverride.getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
				btnToggleRcOverride.setText("Enable RC");
			}
			listener.rcOverrideStateChanged();
		}
		if (v == btnSwitchUnderlay)
		{
    		if (LaserConstants.UNDERLAY_MODE == UnderlayModes.MAP)
				listener.onSwitchToCam();
			else if (LaserConstants.UNDERLAY_MODE == UnderlayModes.CAM)
				listener.onSwitchToMap();					
		}
	}
	

	public void onRollChanged() {
		//trimRoll.setVal();
	}

	public void onPitchChanged() {
		//trimPitch.setVal();		
	}

	public void onYawChanged() {
		//trimYaw.setVal();
	}

	public void onThrottleChanged() {
		//trimThrottle.setVal();
	}

	@Override
	public void onTrimClick() {
		listener.onTrimClick();
	}


}
