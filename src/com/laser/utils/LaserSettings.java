package com.laser.utils;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;

import com.laser.helpers.file.DirectoryPath;


public class LaserSettings implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1076227725463994261L;
	public static String settingsDirectory = DirectoryPath.settingsDirectory; //Environment.getExternalStorageDirectory() + "/VrPadStation/";
	public static String profilesDirectory = settingsDirectory + "Profiles/";
	public static String settingsFilePath = settingsDirectory + "AppSettings.props";
	public static String logFileName = "FollowMeLog.log";
	
	private SharedPreferences mPreferences;	
	
	public LaserSettings(Context ctx)
	{
		mPreferences = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
		TRANSMISSION_RATE = mPreferences.getInt("TRANSMISSION_RATE", TRANSMISSION_RATE);
		MIN_PULSE_WIDTH = mPreferences.getInt("MIN_PULSE_WIDTH", MIN_PULSE_WIDTH);
		MAX_PULSE_WIDTH = mPreferences.getInt("MAX_PULSE_WIDTH", MAX_PULSE_WIDTH);
		RC_MODE = mPreferences.getInt("RC_MODE", RC_MODE);
		TIMER = mPreferences.getInt("TIMER", TIMER);
		ROLL_NEG_DR = mPreferences.getInt("ROLL_NEG_DR", ROLL_NEG_DR);
		ROLL_POS_DR = mPreferences.getInt("ROLL_POS_DR", ROLL_POS_DR);
		ROLL_NEG_EXP = mPreferences.getInt("ROLL_NEG_EXP", ROLL_NEG_EXP);
		ROLL_POS_EXP = mPreferences.getInt("ROLL_POS_EXP", ROLL_POS_EXP);
		PITCH_NEG_DR = mPreferences.getInt("PITCH_NEG_DR", PITCH_NEG_DR);		
		PITCH_POS_DR = mPreferences.getInt("PITCH_POS_DR", PITCH_POS_DR);		
		PITCH_NEG_EXP = mPreferences.getInt("PITCH_NEG_EXP", PITCH_NEG_EXP);		
		PITCH_POS_EXP = mPreferences.getInt("PITCH_POS_EXP", PITCH_POS_EXP);		
		YAW_NEG_DR = mPreferences.getInt("YAW_NEG_DR", YAW_NEG_DR);		
		YAW_POS_DR = mPreferences.getInt("YAW_POS_DR", YAW_POS_DR);		
		YAW_NEG_EXP = mPreferences.getInt("YAW_NEG_EXP", YAW_NEG_EXP);		
		YAW_POS_EXP = mPreferences.getInt("YAW_POS_EXP", YAW_POS_EXP);		
		THROTTLE_NEG_DR = mPreferences.getInt("THROTTLE_NEG_DR", THROTTLE_NEG_DR);		
		THROTTLE_POS_DR = mPreferences.getInt("THROTTLE_POS_DR", THROTTLE_POS_DR);		
		THROTTLE_NEG_EXP = mPreferences.getInt("THROTTLE_NEG_EXP", THROTTLE_NEG_EXP);		
		THROTTLE_POS_EXP = mPreferences.getInt("THROTTLE_POS_EXP", THROTTLE_POS_EXP);		
		ANALOG_BASE_RADIUS = mPreferences.getInt("ANALOG_BASE_RADIUS", ANALOG_BASE_RADIUS);
		ANALOG_STICK_RADIUS = mPreferences.getInt("ANALOG_STICK_RADIUS", ANALOG_STICK_RADIUS);
		
		ANALOG_1_X = mPreferences.getFloat("ANALOG_1_X", ANALOG_1_X);
		ANALOG_1_Y = mPreferences.getFloat("ANALOG_1_Y", ANALOG_1_Y);
		ANALOG_2_X = mPreferences.getFloat("ANALOG_2_X", ANALOG_2_X);
		ANALOG_2_Y = mPreferences.getFloat("ANALOG_2_Y", ANALOG_2_Y);
		
		ReverseSignal = mPreferences.getBoolean("ReverseSignal", ReverseSignal);
		SWITCH_PPMSUM_CHANNEL = mPreferences.getBoolean("SWITCH_PPMSUM_CHANNEL", SWITCH_PPMSUM_CHANNEL);
		AUDIO_SIGNAL = mPreferences.getBoolean("AUDIO_SIGNAL", AUDIO_SIGNAL);
		MAVLINK = mPreferences.getBoolean("MAVLINK", MAVLINK);
		PPMSUM = mPreferences.getBoolean("PPMSUM", PPMSUM);
//		VIBRATION = mPreferences.getBoolean("VIBRATION", VIBRATION);
//		YAW_VIBRO = mPreferences.getBoolean("YAW_VIBRO", YAW_VIBRO);
//		ROLL_VIBRO = mPreferences.getBoolean("ROLL_VIBRO", ROLL_VIBRO);
		AUDIO_FEEDBACK = mPreferences.getBoolean("AUDIO_FEEDBACK", AUDIO_FEEDBACK);
		SET_TIMER = mPreferences.getBoolean("SET_TIMER", SET_TIMER);
		SHOW_FPS = mPreferences.getBoolean("SHOW_FPS", SHOW_FPS);
		LOCK_ANALOG_STICKS = mPreferences.getBoolean("LOCK_ANALOG_STICKS", LOCK_ANALOG_STICKS);
		SHOW_TRIMS = mPreferences.getBoolean("SHOW_TRIMS", SHOW_TRIMS);
		SHOW_POTS = mPreferences.getBoolean("SHOW_POTS", SHOW_POTS);
		SHOW_TIMER = mPreferences.getBoolean("SHOW_TIMER", SHOW_TIMER);
		GOPRO_ENABLED = mPreferences.getBoolean("GOPRO_ENABLED", GOPRO_ENABLED);
		GOPRO_STARTUP = mPreferences.getBoolean("GOPRO_STARTUP", GOPRO_STARTUP);
		
		GOPRO_SSID = mPreferences.getString("GOPRO_SSID", GOPRO_SSID);
		GOPRO_PASSWORD = mPreferences.getString("GOPRO_PASSWORD", GOPRO_PASSWORD);	
		CAMERA_ADDRESS = mPreferences.getString("CAMERA_ADDRESS", CAMERA_ADDRESS);	
		
		RC1_MIN = MIN_PULSE_WIDTH;
		RC2_MIN = MIN_PULSE_WIDTH;
		RC3_MIN = MIN_PULSE_WIDTH;
		RC4_MIN = MIN_PULSE_WIDTH;
		RC5_MIN = MIN_PULSE_WIDTH;
		RC6_MIN = MIN_PULSE_WIDTH;
		RC7_MIN = MIN_PULSE_WIDTH;
		RC8_MIN = MIN_PULSE_WIDTH;
		
		RC1_MAX = MAX_PULSE_WIDTH;
		RC2_MAX = MAX_PULSE_WIDTH;
		RC3_MAX = MAX_PULSE_WIDTH;
		RC4_MAX = MAX_PULSE_WIDTH;
		RC5_MAX = MAX_PULSE_WIDTH;
		RC6_MAX = MAX_PULSE_WIDTH;
		RC7_MAX = MAX_PULSE_WIDTH;
		RC8_MAX = MAX_PULSE_WIDTH;
		
//		RC1_MIN = mPreferences.getInt("RC1_MIN", RC1_MIN);
		RC1_MAX = mPreferences.getInt("RC1_MAX", RC1_MAX);
		RC1_TRIM = mPreferences.getInt("RC1_TRIM", RC1_TRIM);
		RC1_REV = mPreferences.getInt("RC1_REV", RC1_REV);
		
//		RC2_MIN = mPreferences.getInt("RC2_MIN", RC2_MIN);
//		RC2_MAX = mPreferences.getInt("RC2_MAX", RC2_MAX);
		RC2_TRIM = mPreferences.getInt("RC2_TRIM", RC2_TRIM);
		RC2_REV = mPreferences.getInt("RC2_REV", RC2_REV);
		
//		RC3_MIN = mPreferences.getInt("RC3_MIN", RC3_MIN);
//		RC3_MAX = mPreferences.getInt("RC3_MAX", RC3_MAX);
		RC3_TRIM = mPreferences.getInt("RC3_TRIM", RC3_TRIM);
		RC3_REV = mPreferences.getInt("RC3_REV", RC3_REV);
		
//		RC4_MIN = mPreferences.getInt("RC4_MIN", RC4_MIN);
//		RC4_MAX = mPreferences.getInt("RC4_MAX", RC4_MAX);
		RC4_TRIM = mPreferences.getInt("RC4_TRIM", RC4_TRIM);
		RC4_REV = mPreferences.getInt("RC4_REV", RC4_REV);
		
//		RC5_MIN = mPreferences.getInt("RC5_MIN", RC5_MIN);
//		RC5_MAX = mPreferences.getInt("RC5_MAX", RC5_MAX);
		RC5_TRIM = mPreferences.getInt("RC5_TRIM", RC5_TRIM);
		RC5_REV = mPreferences.getInt("RC5_REV", RC5_REV);
		
//		RC6_MIN = mPreferences.getInt("RC6_MIN", RC6_MIN);
//		RC6_MAX = mPreferences.getInt("RC6_MAX", RC6_MAX);
		RC6_TRIM = mPreferences.getInt("RC6_TRIM", RC6_TRIM);
		RC6_REV = mPreferences.getInt("RC6_REV", RC6_REV);
		
//		RC7_MIN = mPreferences.getInt("RC7_MIN", RC7_MIN);
//		RC7_MAX = mPreferences.getInt("RC7_MAX", RC7_MAX);
		RC7_TRIM = mPreferences.getInt("RC7_TRIM", RC7_TRIM);
		RC7_REV = mPreferences.getInt("RC7_REV", RC7_REV);
		
//		RC8_MIN = mPreferences.getInt("RC8_MIN", RC8_MIN);
//		RC8_MAX = mPreferences.getInt("RC8_MAX", RC8_MAX);
		RC8_TRIM = mPreferences.getInt("RC8_TRIM", RC8_TRIM);
		RC8_REV = mPreferences.getInt("RC8_REV", RC8_REV);
	}

		
	public int TRANSMISSION_RATE = 50;
	public int MIN_PULSE_WIDTH = 1000;
	public int MAX_PULSE_WIDTH = 2000;

	public int RC1_MIN = 1000;
	public int RC1_MAX = 2000;
	public int RC1_TRIM = 1500;
	public int RC1_REV = 0;
	public int RC2_MIN = 1000;
	public int RC2_MAX = 2000;
	public int RC2_TRIM = 1500;
	public int RC2_REV = 0;
	public int RC3_MIN = 1000;
	public int RC3_MAX = 2000;
	public int RC3_TRIM = 1000;
	public int RC3_REV = 0;
	public int RC4_MIN = 1000;
	public int RC4_MAX = 2000;
	public int RC4_TRIM = 1500;
	public int RC4_REV = 0;
	public int RC5_MIN = 1000;
	public int RC5_MAX = 2000;
	public int RC5_TRIM = 1500;
	public int RC5_REV = 0;
	public int RC6_MIN = 1000;
	public int RC6_MAX = 2000;
	public int RC6_TRIM = 1500;
	public int RC6_REV = 0;
	public int RC7_MIN = 1000;
	public int RC7_MAX = 2000;
	public int RC7_TRIM = 1500;
	public int RC7_REV = 0;
	public int RC8_MIN = 1000;
	public int RC8_MAX = 2000;
	public int RC8_TRIM = 1500;
	public int RC8_REV = 0;	

	public int RC_MODE = 3;

	public int TIMER = 60;

	public int ROLL_NEG_DR = 100;
	public int ROLL_POS_DR = 100;
	public int ROLL_NEG_EXP = 0;
	public int ROLL_POS_EXP = 0;
	public int PITCH_NEG_DR = 100;
	public int PITCH_POS_DR = 100;
	public int PITCH_NEG_EXP = 0;
	public int PITCH_POS_EXP = 0;
	public int YAW_NEG_DR = 100;
	public int YAW_POS_DR = 100;
	public int YAW_NEG_EXP = 0;
	public int YAW_POS_EXP = 0;
	public int THROTTLE_NEG_DR = 100;
	public int THROTTLE_POS_DR = 100;
	public int THROTTLE_NEG_EXP = 0;
	public int THROTTLE_POS_EXP = 0;

	public int ANALOG_BASE_RADIUS = 110;
	public int ANALOG_STICK_RADIUS = 30;

	public float ANALOG_1_X = -1.0f;
	public float ANALOG_1_Y = -1.0f;
	public float ANALOG_2_X = -1.0f;
	public float ANALOG_2_Y = -1.0f;
	
	public boolean ReverseSignal = false;
	public boolean SWITCH_PPMSUM_CHANNEL = false;
	public boolean AUDIO_SIGNAL = true;
	public boolean MAVLINK = false;
	public boolean PPMSUM = false;	

//	public boolean VIBRATION = false;
//	public boolean YAW_VIBRO = false;
//	public boolean ROLL_VIBRO = false;
	public boolean AUDIO_FEEDBACK = true;
	
	public boolean SET_TIMER = false;	
	
	public boolean SHOW_FPS = true;
	public boolean LOCK_ANALOG_STICKS = false;
	public boolean SHOW_TRIMS = true;
	public boolean SHOW_POTS = true;
	public boolean SHOW_TIMER = true;
	
	public boolean GOPRO_ENABLED = false;
	public boolean GOPRO_STARTUP = false;
	
	public String GOPRO_SSID = "";
	public String GOPRO_PASSWORD = "";
	public String CAMERA_ADDRESS = "";
								   
}
