package com.laser.ui.widgets;


import com.laser.VrPadStation.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class Pot extends ImageView  {

    private float angle = 0f;
    private float theta_old=0f;
    private float lastAngle = 0f;
    
    private float max_range = 120f;
  
    private PotListener listener;
    
	private Channel ch;
    
    public interface PotListener {
      public void onKnobChanged(int pot, int arg, int val);
      public void onKnobStop();
    }
    
    public int getPotId()
    {
    	return getId();
    }
    
    public void setKnobListener(PotListener l )
    {
      listener = l;
    }
    
    public Pot(Context context, Channel ch) {
      super(context);
      this.ch = ch;
      initialize();
    }
    
    public Pot(Context context, AttributeSet attrs, Channel ch)
    {
      super(context, attrs);
      this.ch = ch;
      initialize();
    }
    
    public Pot(Context context, AttributeSet attrs, int defStyle, Channel ch)
    {
      super(context, attrs, defStyle);
      this.ch = ch;
      initialize();
    }
    
    private float normalizeAngle(float a)
    {
    	if (a > 180.0)
    	{
    		while (a > 180.0)
    			a -= 360.0;
    	} 
    	else if (a < -180.0)
    	{
    		while (a < -180.0)
    			a += 360.0;
    	}
    	return a;
    }
    
    private float getTheta(float x, float y)
    {
      float sx = x - (getWidth() / 2.0f);
      float sy = y - (getHeight() / 2.0f);
 
      float length = (float)Math.sqrt( sx*sx + sy*sy);
      float nx = sx / length;
      float ny = sy / length;
      float theta = (float)Math.atan2( -ny, nx );
      
 
      final float rad2deg = (float)(180.0/Math.PI);
      float theta2 = theta*rad2deg;
      theta2 = 90 - theta2;
 
      return normalizeAngle(theta2);
    }
    
    private void setAngle(float theta, boolean notify)
    {
    	int dir = 0;
    	
		angle = theta;
		if (( angle >= -max_range && angle <= max_range))
		{
			dir = ((angle > lastAngle) ? 1 : -1);
			lastAngle = angle;
			invalidate();
		} else {
			angle = lastAngle;
		}
		 
		if (notify)
		{
			//Log.d("KNOB", ""+angle);
			notifyListener(dir);
		}
    }
    
    public void setValue(int val)
    {
    	float newangle = (2* max_range * val / ch.getMin()) - ch.getMin();
    	newangle -= max_range;
    	setAngle(normalizeAngle(newangle), false);
    }
    
    public int getValue()
    {
    	return (ch.getMin()) + (int)((Math.abs(ch.getMax() - ch.getMin())) * (lastAngle + max_range) / (2*max_range));
    }
    
    public void initialize()
    { 
      this.setImageResource(R.drawable.knob);
            
      setOnTouchListener(new OnTouchListener()
      {
        public boolean onTouch(View v, MotionEvent event) {
          int action = event.getAction();
          int actionCode = action & MotionEvent.ACTION_MASK;
          if (actionCode == MotionEvent.ACTION_POINTER_DOWN)
          {
            float x = event.getX(0);
            float y = event.getY(0);
            theta_old = getTheta(x, y);
          }
          else if (actionCode == MotionEvent.ACTION_MOVE)
          {
            //invalidate();
       
            float x = event.getX(0);
            float y = event.getY(0);
       
            float theta = getTheta(x,y);
            
            /*
            float delta_theta = theta - theta_old;

            theta_old = theta;
            
            int direction = (delta_theta > 0) ? 1 : -1;
            //angle += 3*direction;
            
            //Laser
            //angle = theta - 270;
            angle = theta;
            		
            //angle += 4*direction;      */      
            
            setAngle(theta, true);
          }
          if (actionCode == MotionEvent.ACTION_UP)
          {
        	  if (listener != null)
        		  listener.onKnobStop();
          }
          return true;
        }
     });
    }
    
    private void notifyListener(int arg)
    {
      if (null!=listener)
    	  listener.onKnobChanged(getId(), arg, getValue());
    }
    
    @Override
    public void setEnabled(boolean enabled) {
    	if (enabled)
    		clearColorFilter();
    	else {
    		LightingColorFilter cf = new LightingColorFilter(Color.DKGRAY, 0);
    		//setColorFilter(Color.GRAY, Mode.DARKEN);
    		setColorFilter(cf);
    	}
    	super.setEnabled(enabled);
    }
    
    protected void onDraw(Canvas c)
    {    	
    	c.rotate(lastAngle,(getWidth()/2),(getHeight()/2));
    	super.onDraw(c);
    }
    
}