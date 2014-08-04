package com.laser.ui.layers;

import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.cellbots.PulseGenerator;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.utils.LaserConstants;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


public class JoystickLayer extends SurfaceView implements SurfaceHolder.Callback{
	
	private JoystickSurfaceThread thread;
	//private Paint debug;
	
	private boolean start = true;
	private int w = 0;
	private int h = 0;
	
    // valori dei canali da mostrare sui trim
	private int rollVal;
	private int pitchVal;
	private int yawVal;
	private int throttleVal;	
	
    // coordinate stick analogici
    private float ana1InitX, ana1InitY;
    private float ana1TargetX, ana1TargetY;
    private float ana2InitX, ana2InitY;
    private float ana2TargetX, ana2TargetY;
    private float /*ana1DiffX, ana2DiffX,*/ ana1DiffY, ana2DiffY;
    
	// stick analogici
	private Bitmap base;
	private int baseRadius;
	public Paint analogBase = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap stick;
	private int stickRadius;
	public Paint analogStick = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Bitmap droneBattery;
	public Paint droneBatteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
//	private Vibrator vibro;
//	
//	private Channel ch1 = null;
//	private Channel ch2 = null;
//	
//	private int impulsiCh1 = 0;
//	private int vibroCh1Counter = 0;
//	private int impulsiCh2 = 0;
//	private int vibroCh2Counter = 0;
//	
//	private float prevInterval1;
//	private float currInterval1;
//	private float prevInterval2;
//	private float currInterval2;
//	
//	private int vibroRestTime = 250;
//	private int vibroRestTime2 = 600;
//	private int vibroLoopTime = 1000;
	
	public interface JoystickListener
	{
		public void onRollChanged();
		public void onPitchChanged();
		public void onYawChanged();
		public void onThrottleChanged();
	}
	
	private JoystickListener listener;
	public void setListener(JoystickListener listener)
	{
		this.listener = listener;
	}
	
//	private Handler mHandlerVibration;
//	private Runnable vibroUpdate = new Runnable() {
//		@Override
//		public void run() {
//			if (RadioActivity.KEEP_RUNNING)
//			{
//				if (vibroCh1Counter > 0)
//				{
//					vibro.vibrate(50);
//					vibroCh1Counter--;
//					if (vibroCh1Counter == 0)
//						mHandlerVibration.postDelayed(vibroUpdate, vibroRestTime2);
//					else
//						mHandlerVibration.postDelayed(vibroUpdate, vibroRestTime);
//				}
//				else if (vibroCh2Counter > 0)
//				{
//					vibro.vibrate(50);
//					vibroCh2Counter--;
//					if (vibroCh2Counter == 0)
//						mHandlerVibration.postDelayed(vibroUpdate, vibroRestTime2);
//					else
//						mHandlerVibration.postDelayed(vibroUpdate, vibroRestTime);
//				} 
//				else
//				{	
//					vibroCh1Counter = impulsiCh1;
//					vibroCh2Counter = impulsiCh2;
//					mHandlerVibration.postDelayed(vibroUpdate, vibroLoopTime);
//				}
//			}
//		}
//	};
	
	
	private long prevTime;      
	private int frameSamplesCollected = 0;
	private int frameSampleTime = 0;
	private int fps = 0;
	private Paint textPaint;
	
	private void calcFps() {
	    long curTime = System.currentTimeMillis();
	
	    if (prevTime != 0) {
	
	    	//Time difference between now and last time we were here
			int time = (int) (curTime - prevTime);
			frameSampleTime += time;
			frameSamplesCollected++;
	
			//After 10 frames
			if (frameSamplesCollected == 10) {
	
				//Update the fps variable
	    		fps = (int) (10000 / frameSampleTime);
	
	    		//Reset the sampletime + frames collected
	    		frameSampleTime = 0;
	    		frameSamplesCollected = 0;
			}
		}
	    prevTime = curTime;
	}
	
	public void updateJoystickLayer()
	{
		baseRadius = app.settings.ANALOG_BASE_RADIUS;
		stickRadius = app.settings.ANALOG_STICK_RADIUS;
		base = getResizedBitmap(base, baseRadius*2, baseRadius*2);
		stick = getResizedBitmap(stick, stickRadius*2, stickRadius*2);	
	}
	
	private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {	
		int width = bm.getWidth();		
		int height = bm.getHeight();		
		
		float scaleWidth = ((float) newWidth) / width;		
		float scaleHeight = ((float) newHeight) / height;	
		
		// create a matrix for the manipulation		
		Matrix matrix = new Matrix();		
		
		// resize the bit map		
		matrix.postScale(scaleWidth, scaleHeight);		
		
		// recreate the new Bitmap		
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		
		return resizedBitmap;
	}
	
	public void destroyRes()
	{
		// libero la memoria allocata dalle bitmap
		if (base != null)
			base.recycle();
		if (stick != null)
			stick.recycle();
		if (droneBattery != null)
			droneBattery.recycle();
	}
	
	private boolean timerExpired = false;
	private boolean droneBatteryUpdate = false;
	private long lastDraw = 0;

	private VrPadStationApp app;
	@Override
	protected void onDraw(Canvas canvas) {
		//	super.onDraw(canvas);		
		if (canvas != null)
		{			
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			
			if (timerExpired)
			{
				if (System.currentTimeMillis() - lastDraw > 500)
				{
					droneBatteryUpdate = !droneBatteryUpdate;
					lastDraw = System.currentTimeMillis();
				}
				if (droneBatteryUpdate)
					canvas.drawBitmap(droneBattery, getWidth()/2 - droneBattery.getWidth() / 2, getHeight()/2 - droneBattery.getHeight() / 2, droneBatteryPaint);
			}
	
			canvas.drawBitmap(base, ana1InitX - baseRadius, ana1InitY - baseRadius, analogBase);
			canvas.drawBitmap(stick, (ana1TargetX) - stickRadius, ana1TargetY - stickRadius, analogStick);
			
			canvas.drawBitmap(base, ana2InitX - baseRadius, ana2InitY - baseRadius, analogBase);
			canvas.drawBitmap(stick, ana2TargetX - stickRadius, ana2TargetY - stickRadius, analogStick);
	
			if (app.settings.SHOW_FPS)
			{
				if (fps < 20)
					textPaint.setARGB(255, 255, 0, 0);
				else
		            textPaint.setARGB(255,255,255,255);
				canvas.drawText("FPS: " + fps, 0, getHeight(), textPaint);
			}
			
			/* Debug delle coordinate su schermo
	
			debug.setColor(Color.WHITE); 
			debug.setTextSize(10); 
			
			canvas.drawText("ana1TargetX = " + ana1TargetX, 50, 50, debug);
			canvas.drawText("ana1TargetY = " + ana1TargetY, 50, 70, debug);
	
			canvas.drawText("ana2TargetX = " + ana2TargetX, 50, 90, debug);
			canvas.drawText("ana2TargetY = " + ana2TargetY, 50, 110, debug);
			
			canvas.drawText("axis X (roll)= " + roll.getVal(), 50, 50, debug);
			canvas.drawText("axis Y (pitch)= " + pitch.getVal(), 50, 70, debug);
			canvas.drawText("axis X (yaw)= " + yaw.getVal(), 50, 90, debug);
			canvas.drawText("axis Y (throttle)= " + throttle.getVal(), 50, 110, debug);
		
			canvas.drawText(msgToSend, 50, 130, debug); */			
		}
	}
	
	private void actionDown(float x, float y)
	{
		switch (app.settings.RC_MODE)
		{
		case 1:
		case 3:
			if (x < w/2)
			{
				ana1InitX = x;
				ana1InitY = y;					
				ana1TargetX = ana1InitX;
				ana1TargetY = ana1InitY;	
			}
			else
			{
				if (start)
				{
					ana2InitX = x;
					ana2InitY = y - baseRadius + stickRadius;					
					ana2TargetX = ana2InitX;
					ana2TargetY = y;
					start = false;
				}
				else
				{
					ana2InitX = x;
					ana2InitY = y - ana2DiffY;					
					ana2TargetX = ana2InitX;
					ana2TargetY = y;
				}					
			}
			break;
		case 2:
		case 4:
			if (x < w/2)
			{					
				if (start)
				{
					ana1InitX = x;
					ana1InitY = y - baseRadius + stickRadius;					
					ana1TargetX = ana1InitX;
					ana1TargetY = y;
					start = false;
				}
				else
				{
					ana1InitX = x;
					ana1InitY = y - ana1DiffY;					
					ana1TargetX = ana1InitX;
					ana1TargetY = y;
				}		
			}
			else
			{
				ana2InitX = x;
				ana2InitY = y;					
				ana2TargetX = ana2InitX;
				ana2TargetY = ana2InitY;				
			}
			break;
		}	
		
		app.settings.ANALOG_1_X = ana1InitX;
		app.settings.ANALOG_1_Y = ana1InitY;
		app.settings.ANALOG_2_X = ana2InitX;
		app.settings.ANALOG_2_Y = ana2InitY;
	}
	
	public void actionUp(float x)
	{
		switch (app.settings.RC_MODE)
		{
		case 1:
		case 3:
	        if (x < w/2)
			{
	        	ana1TargetX = ana1InitX;
	        	ana1TargetY = ana1InitY;
			}else
			{
				ana2TargetX = ana2InitX;
				//ana2TargetY = ana2InitY;
			}
			break;
		case 2:
		case 4:
	        if (x < w/2)
			{
	        	ana1TargetX = ana1InitX;
	        	//ana1TargetY = ana1InitY;
			}else
			{
				ana2TargetX = ana2InitX;
				ana2TargetY = ana2InitY;
			}
			break;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//return super.onTouchEvent(event);
		int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
		{
			if (!app.settings.LOCK_ANALOG_STICKS)
			{
				float x = event.getX();
				float y = event.getY();				
				x = checkHorizontalBounds(x);
				y = checkVerticalBounds(y);
			
				actionDown(x, y);
			}
			break;
		}
		case MotionEvent.ACTION_POINTER_DOWN:
		{
			if (!app.settings.LOCK_ANALOG_STICKS)
			{
				final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
						>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	        
	        	float x = event.getX(pointerIndex);
	        	float y = event.getY(pointerIndex);		
	        	x = checkHorizontalBounds(x);
	        	y = checkVerticalBounds(y);
			
	        	actionDown(x, y);
			}
	        break;
		}
		case MotionEvent.ACTION_MOVE:				
		{
			int numPointers = event.getPointerCount();
		    for (int i = 0; i < numPointers; i++) {
		    	
				float x = event.getX(i);
				float y = event.getY(i);
				
				if (!app.settings.LOCK_ANALOG_STICKS ||
					isTouchInsideAnalogStick(x, y))
				{					
					if (x < w/2 )
					{
						ana1TargetX = x;
						ana1TargetY = y;	        
						calculateAna1StickPosition();
					}
					else 
					{
						ana2TargetX = x;
						ana2TargetY = y;	        
						calculateAna2StickPosition();
					}
				}
		    }
			break;
		}
		case MotionEvent.ACTION_UP:
		{
			float x = event.getX();
			actionUp(x);
			break;	
		}	
	    case MotionEvent.ACTION_POINTER_UP:
	    {
	        final int pointerIndex3 = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			float x = event.getX(pointerIndex3);
			actionUp(x);
	        break;
	    }	
		}
		
		//ana1DiffX = ana1TargetX - ana1InitX;
		ana1DiffY = ana1TargetY - ana1InitY;
		//ana2DiffX = ana2TargetX - ana2InitX;
		ana2DiffY = ana2TargetY - ana2InitY;

		setChannelManagerFromJoysticks();
		
		switch (action & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_MOVE:
		{
//			//Gestione della vibrazione
//			if (settings.VIBRATION)
//			{
//				if (settings.ROLL_VIBRO)
//				{
//					boolean bPitchChanged = channelManager.getPitch().isChanged();
//					boolean bRollChanged = channelManager.getRoll().isChanged();
//					if (bPitchChanged && !bRollChanged)
//					{
//						ch1 = channelManager.getPitch();
//						ch2 = channelManager.getRoll();
//					}
//					else if (bRollChanged && !bPitchChanged)
//					{
//						ch1 = channelManager.getRoll();
//						ch2 = channelManager.getPitch();
//					}	
//					if (ch1 != null && ch2 != null)
//						setVibration(ch1, ch2);
//				}
//				else if (settings.YAW_VIBRO)
//				{
//					boolean bPitchChanged = channelManager.getPitch().isChanged();
//					boolean bYawChanged = channelManager.getYaw().isChanged();
//					if (bPitchChanged && !bYawChanged)
//					{
//						ch1 = channelManager.getPitch();
//						ch2 = channelManager.getYaw();
//					}
//					else if (bYawChanged && !bPitchChanged)
//					{
//						ch1 = channelManager.getYaw();
//						ch2 = channelManager.getPitch();
//					}	
//					if (ch1 != null && ch2 != null)
//						setVibration(ch1, ch2);
//				}
//				else
//					setVibration(channelManager.getPitch(), null);
//			}
				
			// Aggiornamento delle textView dei trim
			updateTrims();			
			
			float freq =  ((float)(app.channelManager.getThrottle().getVal() - app.channelManager.getThrottle().getMin())) / ((float) (app.channelManager.getThrottle().getMax() - app.channelManager.getThrottle().getMin()));
			if (app.channelManager.getThrottle().isReverse())
			{
				freq = 1.0f - freq;
			}
			freq = 100 + 1000 * freq;
			PulseGenerator.GAS_FREQUENCY = freq;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP:
		{
//			if (ch1 != null && !ch1.isChanged())
//			{
//				impulsiCh1 = 0;
//				vibroCh1Counter = impulsiCh1;
//			}
//			if (ch2 != null && !ch2.isChanged())
//			{
//				impulsiCh2 = 0;
//				vibroCh2Counter = impulsiCh2;
//			}
			
			if (app.settings.SHOW_TRIMS)
			{
				listener.onRollChanged();
				listener.onPitchChanged();
				listener.onYawChanged();
				listener.onThrottleChanged();
			}
			break;
		}
		case MotionEvent.ACTION_UP:
		{
//			mHandlerVibration.removeCallbacks(vibroUpdate);
//			impulsiCh1 = 0;
//			vibroCh1Counter = impulsiCh1;
//			impulsiCh2 = 0;
//			vibroCh2Counter = impulsiCh2;
			
			if (app.settings.SHOW_TRIMS)
			{
				listener.onRollChanged();
				listener.onPitchChanged();
				listener.onYawChanged();
				listener.onThrottleChanged();
			}
			break;
		}
		}
		
		return true;
	}

		
	public void calculateAna1StickPosition()
	{
		/* Con questo faccio girare lo stick intorno alla circonferenza della base.
		 * Non lo uso perch� non mi permette di tenere i valori dei 2 assi al loro valore massimo.
		 * Per questo scopo, devo utilizzare un quadrato.
		 * 
		float dx = ana1TargetX - ana1InitX;
		float dy = ana1TargetY - ana1InitY;
		float d = (float) Math.sqrt((dx*dx)+(dy*dy));
		if (d > baseRadius - stickRadius)
		{	
			ana1TargetX = ana1InitX + dx * (baseRadius - stickRadius) / d;
			ana1TargetY = ana1InitY + dy * (baseRadius - stickRadius) / d;
		}
		*/
		
		float dx = ana1TargetX - ana1InitX;
		if (Math.abs(dx) > baseRadius - stickRadius)
		{	
			if (dx >= 0)
				ana1TargetX = ana1InitX + baseRadius - stickRadius;
			else
				ana1TargetX = ana1InitX - baseRadius + stickRadius;
		}
		
		float dy = ana1TargetY - ana1InitY;
		if (Math.abs(dy) > baseRadius - stickRadius)
		{	
			if (dy >= 0)
				ana1TargetY = ana1InitY + baseRadius - stickRadius;
			else
				ana1TargetY = ana1InitY - baseRadius + stickRadius;
		}
	}
	
	public void calculateAna2StickPosition()
	{
		/* Con questo faccio girare lo stick intorno alla circonferenza della base.
		 * Non lo uso perch� non mi permette di tenere i valori dei 2 assi al loro valore massimo.
		 * Per questo scopo, devo utilizzare un quadrato.
		 * 
		float dx = ana2TargetX - ana2InitX;
		float dy = ana2TargetY - ana2InitY;
		float d = (float) Math.sqrt((dx*dx)+(dy*dy));
		if (d > baseRadius - stickRadius)
		{	
			ana2TargetX = ana2InitX + dx * (baseRadius - stickRadius) / d;
			ana2TargetY = ana2InitY + dy * (baseRadius - stickRadius) / d;
		}
		*/
		
		float dx = ana2TargetX - ana2InitX;
		if (Math.abs(dx) > baseRadius - stickRadius)
		{	
			if (dx >= 0)
				ana2TargetX = ana2InitX + baseRadius - stickRadius;
			else
				ana2TargetX = ana2InitX - baseRadius + stickRadius;
		}
		
		float dy = ana2TargetY - ana2InitY;
		if (Math.abs(dy) > baseRadius - stickRadius)
		{	
			if (dy >= 0)
				ana2TargetY = ana2InitY + baseRadius - stickRadius;
			else
				ana2TargetY = ana2InitY - baseRadius + stickRadius;
		}			
	}
	
	private boolean isTouchInsideAnalogStick(float x, float y)
	{
		boolean bInsideAna1 = false;
		boolean bInsideAna2 = false;
		
		if ((x > ana1InitX + baseRadius ||
			x < ana1InitX - baseRadius) ||
			(y > ana1InitY + baseRadius ||
			y < ana1InitY - baseRadius))
		{
			bInsideAna1 = false;
		}
		else
			bInsideAna1 = true;
					
		if ((x > ana2InitX + baseRadius ||
			x < ana2InitX - baseRadius) ||
			(y > ana2InitY + baseRadius ||
			y < ana2InitY - baseRadius))
		{
			bInsideAna2 = false;
		}
		else
			bInsideAna2 = true;
		
		if (bInsideAna1 || bInsideAna2)
			return true;
		else
			return false;
	}
	
	private float checkHorizontalBounds(float x) {
		if (x < baseRadius)	
			return baseRadius;
		else if (Math.abs(getWidth()/2 - x) < baseRadius)
		{	
			if (x < getWidth()/2)
				return getWidth()/2 - baseRadius;
			else
				return getWidth()/2 + baseRadius;
		}
		else if (getWidth() - x < baseRadius)
			return getWidth() - baseRadius;
		else
			return x;
	}
	
	private float checkVerticalBounds(float y) {
		if (y < baseRadius)
			return baseRadius;
		else if (getHeight() - y < baseRadius)
			return getHeight() - baseRadius;
		else
			return y;
	}
	
	private void updateTrims()
	{
		float deltaUpdate = ((app.settings.MAX_PULSE_WIDTH - app.settings.MIN_PULSE_WIDTH) / 2) / 10;
		if (app.settings.SHOW_TRIMS)
		{
			if (Math.abs(rollVal - app.channelManager.getRoll().getVal()) > deltaUpdate)
			{
				listener.onRollChanged();
				rollVal = app.channelManager.getRoll().getVal();
			}
			if (Math.abs(pitchVal - app.channelManager.getPitch().getVal()) > deltaUpdate)
			{
				listener.onPitchChanged();
				pitchVal = app.channelManager.getPitch().getVal();
			}
			if (Math.abs(yawVal - app.channelManager.getYaw().getVal()) > deltaUpdate)
			{
				listener.onYawChanged();
				yawVal = app.channelManager.getYaw().getVal();
			}
			if (Math.abs(throttleVal - app.channelManager.getThrottle().getVal()) > deltaUpdate)
			{
				listener.onThrottleChanged();
				throttleVal = app.channelManager.getThrottle().getVal();
			}
		}
	}
	
//	private void setVibration(ChannelAnalog ch1, ChannelAnalog ch2)
//	{
//		prevInterval1 = currInterval1;
//		prevInterval2 = currInterval2;
//		
//		float delta = ((app.settings.MAX_PULSE_WIDTH - app.settings.MIN_PULSE_WIDTH) / 2) / 5; //100
//		float startVibro = delta / 2; //50	
//		float interval0 = 0;
//		float interval1 = startVibro; //50
//		float interval2 = delta + startVibro; //150
//		float interval3 = delta*2 + startVibro; //250
//		float interval4 = delta*3 + startVibro; //350
//		float interval5 = delta*4 + startVibro; //450
//		
//		// canale 1
//		if (ch1 != null)
//		{
//			if ((ch1.getVal() <= ch1.getDefault() + interval1) && (ch1.getVal() >= ch1.getDefault() - interval1))						
//				currInterval1 = interval0;
//			else if ((ch1.getVal() <= ch1.getDefault() + interval2) && (ch1.getVal() >= ch1.getDefault() - interval2))
//				currInterval1 = interval1;
//			else if ((ch1.getVal() <= ch1.getDefault() + interval3) && (ch1.getVal() >= ch1.getDefault() - interval3))
//				currInterval1 = interval2;
//			else if ((ch1.getVal() <= ch1.getDefault() + interval4) && (ch1.getVal() >= ch1.getDefault() - interval4))
//				currInterval1 = interval3;
//			else if ((ch1.getVal() <= ch1.getDefault() + interval5) && (ch1.getVal() >= ch1.getDefault() - interval5))
//				currInterval1 = interval4;
//			else if ((ch1.getVal() >  ch1.getDefault() + interval5) || (ch1.getVal() < ch1.getDefault() - interval5))
//				currInterval1 =interval5;
//		}
//		
//		// canale 2
//		if (ch2 != null)
//		{
//			if ((ch2.getVal() <= ch2.getDefault() + interval1) && (ch2.getVal() >= ch2.getDefault() - interval1))						
//				currInterval2 = interval0;
//			else if ((ch2.getVal() <= ch2.getDefault() + interval2) && (ch2.getVal() >= ch2.getDefault() - interval2))
//				currInterval2 = interval1;
//			else if ((ch2.getVal() <= ch2.getDefault() + interval3) && (ch2.getVal() >= ch2.getDefault() - interval3))
//				currInterval2 = interval2;
//			else if ((ch2.getVal() <= ch2.getDefault() + interval4) && (ch2.getVal() >= ch2.getDefault() - interval4))
//				currInterval2 = interval3;
//			else if ((ch2.getVal() <= ch2.getDefault() + interval5) && (ch2.getVal() >= ch2.getDefault() - interval5))
//				currInterval2 = interval4;
//			else if ((ch2.getVal() >  ch2.getDefault() + interval5) || (ch2.getVal() < ch2.getDefault() - interval5))
//				currInterval2 =interval5;
//		}
//		
//		// controllo i cambi di intervallo
//		boolean bChanged = false;
//		if (prevInterval1 != currInterval1)
//		{
//			mHandlerVibration.removeCallbacks(vibroUpdate);
//			impulsiCh1 = 0;
//			
//			if (currInterval1 == interval0)
//				impulsiCh1 = 0;
//			else if (currInterval1 == interval1)
//				impulsiCh1 = 1;
//			else if (currInterval1 == interval2)
//				impulsiCh1 = 2;
//			else if (currInterval1 == interval3)
//				impulsiCh1 = 3;
//			else if (currInterval1 == interval4)
//				impulsiCh1 = 4;
//			else if (currInterval1 == interval5)
//				impulsiCh1 = 5;
//			
//			vibroCh1Counter = impulsiCh1;
//			bChanged = true;
//		}
//		if (prevInterval2 != currInterval2)
//		{
//			mHandlerVibration.removeCallbacks(vibroUpdate);
//			impulsiCh2 = 0;
//			
//			if (currInterval2 == interval0)
//				impulsiCh2 = 0;
//			else if (currInterval2 == interval1)
//				impulsiCh2 = 1;
//			else if (currInterval2 == interval2)
//				impulsiCh2 = 2;
//			else if (currInterval2 == interval3)
//				impulsiCh2 = 3;
//			else if (currInterval2 == interval4)
//				impulsiCh2 = 4;
//			else if (currInterval2 == interval5)
//				impulsiCh2 = 5;
//			
//			vibroCh2Counter = impulsiCh2;
//			bChanged = true;
//		}
//		if (bChanged)
//			mHandlerVibration.post(vibroUpdate);
//	}
	
	public JoystickLayer(VrPadStationApp app) {
		super(app);
		this.app = app;
		init();
	}

	private void init(){
		
		baseRadius = app.settings.ANALOG_BASE_RADIUS;
		stickRadius = app.settings.ANALOG_STICK_RADIUS;
		
//		vibro = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE) ;
		
		getHolder().addCallback(this);
					
		thread = new JoystickSurfaceThread(getHolder(), this);
		
		setFocusable(true); // make sure we get key events
	
		setZOrderOnTop(true);    // necessario per la trasparenza
		getHolder().setFormat(PixelFormat.TRANSPARENT);
			
	    WindowManager wm = (WindowManager) app.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    w = size.x;
	    h = size.y;

		rollVal = 0;
		pitchVal = 0;
		yawVal = 0;
		throttleVal = 0;
	    
	    if (app.settings.ANALOG_1_X == -1.0)
	    	app.settings.ANALOG_1_X = (float) (w * 0.25);
	    if (app.settings.ANALOG_1_Y == -1.0)
	    	app.settings.ANALOG_1_Y = (float) (h * 0.55);
	    if (app.settings.ANALOG_2_X == -1.0)
	    	app.settings.ANALOG_2_X = (float) (w * 0.75);
	    if (app.settings.ANALOG_2_Y == -1.0)
	    	app.settings.ANALOG_2_Y = (float) (h * 0.55);
	    
	    switch (app.settings.RC_MODE)
	    {
	    case 1:
	    case 3:
		    ana1InitX = ana1TargetX = (float) (app.settings.ANALOG_1_X);
		    ana1InitY = ana1TargetY = (float) (app.settings.ANALOG_1_Y);
		    ana2InitX = ana2TargetX = (float) (app.settings.ANALOG_2_X);
		    ana2InitY = /*ana2TargetY =*/ (float) (app.settings.ANALOG_2_Y);
		    //ana2TargetY deve partire dal valore minimo
	    	ana2TargetY = ana2InitY + baseRadius - stickRadius;
	    	setJoysticksFromRcChannels();	
	    	break;
	    case 2:
	    case 4:
		    ana1InitX = ana1TargetX = (float) (app.settings.ANALOG_1_X);
		    ana1InitY = /*ana1TargetY =*/ (float) (app.settings.ANALOG_1_Y);
	    	//ana1TargetY deve partire dal valore minimo
		    ana1TargetY = ana1InitY + baseRadius - stickRadius;	  
		    ana2InitX = ana2TargetX = (float) (app.settings.ANALOG_2_X);
		    ana2InitY = ana2TargetY = (float) (app.settings.ANALOG_2_Y);
	    	setJoysticksFromRcChannels();	
	    	break;
	    }		
	
		Resources res = getResources();			
		base = BitmapFactory.decodeResource(res, R.drawable.analog_base);
		stick = BitmapFactory.decodeResource(res, R.drawable.analog_stick);
		base = getResizedBitmap(base, baseRadius*2, baseRadius*2);
		stick = getResizedBitmap(stick, stickRadius*2, stickRadius*2);	
		
		droneBattery = BitmapFactory.decodeResource(res, R.drawable.battery_low);
		droneBattery = getResizedBitmap(droneBattery, LaserConstants.DRONE_BATTERY_WIDTH, LaserConstants.DRONE_BATTERY_WIDTH);
					
//		BitmapFactory.Options o=new BitmapFactory.Options();
//		o.inSampleSize = 2;
//		o.inDither=false;                     
//		o.inPurgeable=true;     
//		bgBitmap = BitmapFactory.decodeResource(res, R.drawable.hud, o);
//		bgBitmap = getResizedBitmap(bgBitmap, h, w);
							
//		mHandlerVibration = new Handler();
		
	    textPaint = new Paint();
	    textPaint.setARGB(255,255,255,255);
	    textPaint.setTextSize(16);

	    setChannelManagerFromJoysticks();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
			int arg3) {
		Log.d("SURFACE", "mysurface changed");
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("SURFACE", "mysurface created");
		if (thread.getState() == Thread.State.TERMINATED) {
	        thread = new JoystickSurfaceThread(getHolder(), this);
	        thread.setRunning(true);
	        thread.start();
	    }
	    else {
	        thread.setRunning(true);
	        thread.start();
	    }
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("SURFACE", "mysurface destroyed");
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {}
		}
	}
	
	public class JoystickSurfaceThread extends Thread {		
		
		private SurfaceHolder myThreadSurfaceHolder;
		private JoystickLayer myThreadSurfaceView;
		private boolean myThreadRun = false;
		
		public JoystickSurfaceThread(SurfaceHolder surfaceHolder, JoystickLayer surfaceView) {
			myThreadSurfaceHolder = surfaceHolder;
			myThreadSurfaceView = surfaceView;
		}
		
		public void setRunning(boolean b) {
			myThreadRun = b;
		}
		
		
		@Override
		public void run() {
			//super.run();
			while (myThreadRun) {
				Canvas c = null;
				try {
					c = myThreadSurfaceHolder.lockCanvas(null);
					synchronized (myThreadSurfaceHolder) {
						if (app.settings.SHOW_FPS)
							myThreadSurfaceView.calcFps();
						myThreadSurfaceView.onDraw(c);
					}
//					sleep(2);             
//				} catch (InterruptedException e) {
//					e.printStackTrace();
				} finally {
					//  do this in a finally so that if an exception is thrown
					// 	during the above, we don't leave the Surface in an
					// 	inconsistent state
					if (c != null) {
						myThreadSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}	
	}

	public void timerExpired() {
	}

	public void setTimerExpired(boolean expired) {
		timerExpired = expired;
	}

	public void onTrimClick() {
		setChannelManagerFromJoysticks();
	}
	
	private float getNormalizedValueFromCoords(float target, float init)
	{
		float ret = (target-init) / (baseRadius - stickRadius);
		return ret;
	}
	private float getCoordsFromNormalizedValue(float normalizedValue, float init)
	{
		float target = init + (normalizedValue * (baseRadius - stickRadius));
		return target;
	}
	
	private void setJoysticksFromRcChannels()
	{
		if (app.isMavlinkConnected()) 
		{
			msg_rc_channels_raw rc = app.drone.getRcChannelsRaw();			
			float rollNorm = app.channelManager.getRoll().getNormalizedValue(rc.chan1_raw);
			float rollPitch = app.channelManager.getPitch().getNormalizedValue(rc.chan2_raw);
			float rollYaw = app.channelManager.getYaw().getNormalizedValue(rc.chan4_raw);
			float rollThrottle = app.channelManager.getThrottle().getNormalizedValue(rc.chan3_raw);
			switch(app.settings.RC_MODE)
			{
			case 1:
				ana2TargetX = getCoordsFromNormalizedValue(rollNorm, ana2InitX);
				ana1TargetY = getCoordsFromNormalizedValue(rollPitch, ana1InitY);
				ana1TargetX = getCoordsFromNormalizedValue(rollYaw, ana1InitX);
				ana2TargetY = getCoordsFromNormalizedValue(-rollThrottle, ana2InitY);
				break;
			case 2:
				ana2TargetX = getCoordsFromNormalizedValue(rollNorm, ana2InitX);
				ana2TargetY = getCoordsFromNormalizedValue(rollPitch, ana2InitY);
				ana1TargetX = getCoordsFromNormalizedValue(rollYaw, ana1InitX);
				ana1TargetY = getCoordsFromNormalizedValue(-rollThrottle, ana1InitY);
				break;
			case 3:
				ana1TargetX = getCoordsFromNormalizedValue(rollNorm, ana1InitX);
				ana1TargetY = getCoordsFromNormalizedValue(rollPitch, ana1InitY);
				ana2TargetX = getCoordsFromNormalizedValue(rollYaw, ana2InitX);
				ana2TargetY = getCoordsFromNormalizedValue(-rollThrottle, ana2InitY);
				break;
			case 4:
				ana1TargetX = getCoordsFromNormalizedValue(rollNorm, ana1InitX);
				ana2TargetY = getCoordsFromNormalizedValue(rollPitch, ana2InitY);
				ana2TargetX = getCoordsFromNormalizedValue(rollYaw, ana2InitX);
				ana1TargetY = getCoordsFromNormalizedValue(-rollThrottle, ana1InitY);
				break;
			}						
		}
	}
	private void setChannelManagerFromJoysticks()
	{
		switch(app.settings.RC_MODE)
		{
		case 1:
			app.channelManager.getRoll().setVal(getNormalizedValueFromCoords(ana2TargetX, ana2InitX));
			app.channelManager.getPitch().setVal(-getNormalizedValueFromCoords(ana1TargetY, ana1InitY));
			app.channelManager.getYaw().setVal(getNormalizedValueFromCoords(ana1TargetX, ana1InitX));
			app.channelManager.getThrottle().setVal(-getNormalizedValueFromCoords(ana2TargetY, ana2InitY));
			break;
		case 2:
			app.channelManager.getRoll().setVal(getNormalizedValueFromCoords(ana2TargetX, ana2InitX));
			app.channelManager.getPitch().setVal(-getNormalizedValueFromCoords(ana2TargetY, ana2InitY));
			app.channelManager.getYaw().setVal(getNormalizedValueFromCoords(ana1TargetX, ana1InitX));
			app.channelManager.getThrottle().setVal(-getNormalizedValueFromCoords(ana1TargetY, ana1InitY));
			break;
		case 3:
			app.channelManager.getRoll().setVal(getNormalizedValueFromCoords(ana1TargetX, ana1InitX));
			app.channelManager.getPitch().setVal(-getNormalizedValueFromCoords(ana1TargetY, ana1InitY));
			app.channelManager.getYaw().setVal(getNormalizedValueFromCoords(ana2TargetX, ana2InitX));
			app.channelManager.getThrottle().setVal(-getNormalizedValueFromCoords(ana2TargetY, ana2InitY));
			break;
		case 4:
			app.channelManager.getRoll().setVal(getNormalizedValueFromCoords(ana1TargetX, ana1InitX));
			app.channelManager.getPitch().setVal(-getNormalizedValueFromCoords(ana2TargetY, ana2InitY));
			app.channelManager.getYaw().setVal(getNormalizedValueFromCoords(ana2TargetX, ana2InitX));
			app.channelManager.getThrottle().setVal(-getNormalizedValueFromCoords(ana1TargetY, ana1InitY));
			break;
		}
	}

	public void updateChannelsAndTrims() {
		setChannelManagerFromJoysticks();
		updateTrims();
	}
	
	
}