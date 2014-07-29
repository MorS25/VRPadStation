package com.laser.ui.widgets;


import com.laser.app.VrPadStationApp;
import com.laser.utils.LaserSettings;

public class ChannelManager {
	
	// canali
	private int[] channelValuesArray;	
	private Channel roll;	
	private Channel pitch;	
	private Channel yaw;	
	private Channel throttle;
	private Channel quickModes;
	private Channel potentiometer1;
	private Channel potentiometer2;
	private Channel potentiometer3;

	private VrPadStationApp app;	

    
    public void updateChannelsArray()
    {
    	channelValuesArray[0] = roll.getVal();
		channelValuesArray[1] = pitch.getVal();
		channelValuesArray[2] = throttle.getVal();
		channelValuesArray[3] = yaw.getVal();
		channelValuesArray[4] = quickModes.getVal();
		channelValuesArray[5] = potentiometer1.getVal();
		channelValuesArray[6] = potentiometer2.getVal();
		channelValuesArray[7] = potentiometer3.getVal();
    }
    
    public int[] getChannelsArray()
    {
    	return channelValuesArray;
    }
	
	public ChannelManager(VrPadStationApp app)
	{
		this.app = app;
		updateSettings(); //qui se uso ppmsum lascio quelli di def, altrimenti aggiorno col mavlink
		createChannels();
	}
	
	private void updateSettings()
	{
		if (app.settings.MAVLINK && app.isMavlinkConnected())
		{
			app.settings.RC1_MIN = (int) app.parameterMananger.getParameterValue("RC1_MIN");
			app.settings.RC2_MIN = (int) app.parameterMananger.getParameterValue("RC2_MIN");
			app.settings.RC3_MIN = (int) app.parameterMananger.getParameterValue("RC3_MIN");
			app.settings.RC4_MIN = (int) app.parameterMananger.getParameterValue("RC4_MIN");
			app.settings.RC5_MIN = (int) app.parameterMananger.getParameterValue("RC5_MIN");
			app.settings.RC6_MIN = (int) app.parameterMananger.getParameterValue("RC6_MIN");
			app.settings.RC7_MIN = (int) app.parameterMananger.getParameterValue("RC7_MIN");
			app.settings.RC8_MIN = (int) app.parameterMananger.getParameterValue("RC8_MIN");
			
			app.settings.RC1_MAX = (int) app.parameterMananger.getParameterValue("RC1_MAX");
			app.settings.RC2_MAX = (int) app.parameterMananger.getParameterValue("RC2_MAX");
			app.settings.RC3_MAX = (int) app.parameterMananger.getParameterValue("RC3_MAX");
			app.settings.RC4_MAX = (int) app.parameterMananger.getParameterValue("RC4_MAX");
			app.settings.RC5_MAX = (int) app.parameterMananger.getParameterValue("RC5_MAX");
			app.settings.RC6_MAX = (int) app.parameterMananger.getParameterValue("RC6_MAX");
			app.settings.RC7_MAX = (int) app.parameterMananger.getParameterValue("RC7_MAX");
			app.settings.RC8_MAX = (int) app.parameterMananger.getParameterValue("RC8_MAX");
			
//			app.settings.RC1_REV = (int) app.parameterMananger.getParameterValue("RC1_REV");
//			app.settings.RC2_REV = (int) app.parameterMananger.getParameterValue("RC2_REV");
//			app.settings.RC3_REV = (int) app.parameterMananger.getParameterValue("RC3_REV");
//			app.settings.RC4_REV = (int) app.parameterMananger.getParameterValue("RC4_REV");
//			app.settings.RC5_REV = (int) app.parameterMananger.getParameterValue("RC5_REV");
//			app.settings.RC6_REV = (int) app.parameterMananger.getParameterValue("RC6_REV");
//			app.settings.RC7_REV = (int) app.parameterMananger.getParameterValue("RC7_REV");
//			app.settings.RC8_REV = (int) app.parameterMananger.getParameterValue("RC8_REV");
			
			app.settings.RC1_TRIM = (int) app.parameterMananger.getParameterValue("RC1_TRIM");
			app.settings.RC2_TRIM = (int) app.parameterMananger.getParameterValue("RC2_TRIM");
			app.settings.RC3_TRIM = (int) app.parameterMananger.getParameterValue("RC3_TRIM");
			app.settings.RC4_TRIM = (int) app.parameterMananger.getParameterValue("RC4_TRIM");
			app.settings.RC5_TRIM = (int) app.parameterMananger.getParameterValue("RC5_TRIM");
			app.settings.RC6_TRIM = (int) app.parameterMananger.getParameterValue("RC6_TRIM");
			app.settings.RC7_TRIM = (int) app.parameterMananger.getParameterValue("RC7_TRIM");
			app.settings.RC8_TRIM = (int) app.parameterMananger.getParameterValue("RC8_TRIM");
		}
		else
		{
			app.settings = new LaserSettings(app.getApplicationContext());
		}
	}
	
	private void createChannels()
	{
		channelValuesArray = new int[8];
		roll = new Channel(app.settings, 0);
		pitch = new Channel(app.settings, 1);
		throttle = new Channel(app.settings, 2);
		yaw = new Channel(app.settings, 3);
		quickModes = new Channel(app.settings, 4);
		potentiometer1 = new Channel(app.settings, 5);
		potentiometer2 = new Channel(app.settings, 6);
		potentiometer3 = new Channel(app.settings, 7);
	}
	
	public void updateChannels(LaserSettings settings, int modeValue)
	{
		app.settings = settings;
		updateSettings();
		roll.update(settings, -1);
		pitch.update(settings, -1);
		throttle.update(settings, -1);
		yaw.update(settings, -1);
		quickModes.update(settings, modeValue);
		potentiometer1.update(settings, -1);
		potentiometer2.update(settings, -1);
		potentiometer3.update(settings, -1);
	}
	
	public Channel getRoll()
	{
		return roll;
	}
	
	public Channel getPitch()
	{
		return pitch;
	}
	
	public Channel getYaw()
	{
		return yaw;
	}
	
	public Channel getThrottle()
	{
		return throttle;
	}
	
	public Channel getModes()
	{
		return quickModes;
	}
	
	public Channel getPotentiometer1()
	{
		return potentiometer1;
	}

	public Channel getPotentiometer2()
	{
		return potentiometer2;
	}

	public Channel getPotentiometer3()
	{
		return potentiometer3;
	}
	
}
