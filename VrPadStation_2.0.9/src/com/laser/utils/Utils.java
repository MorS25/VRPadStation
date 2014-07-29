package com.laser.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Utils {

	public static void hideSystemBar(Window win) {
		win.addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
		{
			win.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}
	
	
	public static void CheckScreenSize(WindowManager wm, Resources res)
	{
		DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        String str_ScreenSize = "The Android Screen is: "
                    + dm.widthPixels
                    + " x "
                    + dm.heightPixels;
        Log.d("LOG", str_ScreenSize);
        
        switch(dm.densityDpi)
        {
        case DisplayMetrics.DENSITY_LOW:
            Log.d("LOG", "DENSITY_LOW");
                   break;
        case DisplayMetrics.DENSITY_MEDIUM:
            Log.d("LOG", "DENSITY_MEDIUM");
                    break;
        case DisplayMetrics.DENSITY_HIGH:
            Log.d("LOG", "DENSITY_HIGH");
                    break;
        case DisplayMetrics.DENSITY_XHIGH:
            Log.d("LOG", "DENSITY_XHIGH");
                    break;
        case DisplayMetrics.DENSITY_XXHIGH:
            Log.d("LOG", "DENSITY_XXHIGH");
                    break;
        case DisplayMetrics.DENSITY_TV:
            Log.d("LOG", "DENSITY_TV");
                    break;
        }
        int screenSize = res.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize)
        {
        	case Configuration.SCREENLAYOUT_SIZE_XLARGE:
        		LaserConstants.SCREEN_SIZE = Configuration.SCREENLAYOUT_SIZE_XLARGE;
        		Log.d("LOG", "XLARGE");
        		break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            	LaserConstants.SCREEN_SIZE = Configuration.SCREENLAYOUT_SIZE_LARGE;
                Log.d("LOG", "LARGE");
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            	LaserConstants.SCREEN_SIZE = Configuration.SCREENLAYOUT_SIZE_NORMAL;
                Log.d("LOG", "NORMAL");
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            	LaserConstants.SCREEN_SIZE = Configuration.SCREENLAYOUT_SIZE_SMALL;
                Log.d("LOG", "SMALL");
                break;
        }
        
        switch(screenSize)
        {
        	case Configuration.SCREENLAYOUT_SIZE_XLARGE:
        	case Configuration.SCREENLAYOUT_SIZE_LARGE:
        		LaserConstants.TEXT_SIZE_SMALL = 16;
        		LaserConstants.TEXT_SIZE_MEDIUM = 20;
        		LaserConstants.TEXT_SIZE_LARGE = 24;
        		break;
        	case Configuration.SCREENLAYOUT_SIZE_NORMAL:
        	case Configuration.SCREENLAYOUT_SIZE_SMALL:
        		LaserConstants.TEXT_SIZE_SMALL = 10;
        		LaserConstants.TEXT_SIZE_MEDIUM = 14;
        		LaserConstants.TEXT_SIZE_LARGE = 18;
        		break;
        }
	}
	
}
