package com.laser.ui.widgets;


import com.laser.utils.LaserSettings;

public class Channel {


	private String rc_name = "";
	private int channelId;
	private int rc_min;
	private int rc_max;
	private int rc_trim;
	private int rc_rev;
	
	private float negativeDR = 1;
	private float positiveDR = 1;	
	private static float MAXEXP = 4.0f;
	private float negativeEXP = 0;
	private float positiveEXP = 0;	
	private float fnegativeEXP = 0;
	private float fpositiveEXP = 0;
	
	private int currVal = 0;
	
	
	public void update(LaserSettings settings, int modeValue)
	{
		switch(channelId)
		{
		case 0:
			rc_name = "roll";
			rc_min = settings.RC1_MIN;
			rc_max = settings.RC1_MAX;
			currVal = rc_trim = settings.RC1_TRIM;
			rc_rev = settings.RC1_REV;		
			setRates(settings.ROLL_NEG_DR, settings.ROLL_POS_DR, settings.ROLL_NEG_EXP, settings.ROLL_POS_EXP);
			calcExp();
			break;
		case 1:
			rc_name = "pitch";
			rc_min = settings.RC2_MIN;
			rc_max = settings.RC2_MAX;
			currVal = rc_trim = settings.RC2_TRIM;
			rc_rev = settings.RC2_REV;			
			setRates(settings.PITCH_NEG_DR, settings.PITCH_POS_DR, settings.PITCH_NEG_EXP, settings.PITCH_POS_EXP);
			calcExp();
			break;
		case 2:
			rc_name = "throttle";
			rc_min = settings.RC3_MIN;
			rc_max = settings.RC3_MAX;
			currVal = rc_trim = settings.RC3_TRIM;
			rc_rev = settings.RC3_REV;		
			setRates(settings.THROTTLE_NEG_DR, settings.THROTTLE_POS_DR, settings.THROTTLE_NEG_EXP, settings.THROTTLE_POS_EXP);
			calcExp();
			break;
		case 3:
			rc_name = "yaw";
			rc_min = settings.RC4_MIN;
			rc_max = settings.RC4_MAX;
			currVal = rc_trim = settings.RC4_TRIM;
			rc_rev = settings.RC4_REV;	
			setRates(settings.YAW_NEG_DR, settings.YAW_POS_DR, settings.YAW_NEG_EXP, settings.YAW_POS_EXP);
			calcExp();
			break;
		case 4:
			rc_name = "quickmodes";
			rc_min = settings.RC5_MIN;
			rc_max = settings.RC5_MAX;
			currVal = rc_trim = settings.RC5_TRIM;
			rc_rev = settings.RC5_REV;		
			switch (modeValue)
			{
			case 1:
				currVal = QuickModes.MODE_1_VAL;
				break;
			case 2:
				currVal = QuickModes.MODE_2_VAL;
				break;
			case 3:
				currVal = QuickModes.MODE_3_VAL;
				break;
			case 4:
				currVal = QuickModes.MODE_4_VAL;
				break;
			case 5:
				currVal = QuickModes.MODE_5_VAL;
				break;
			case 6:
				currVal = QuickModes.MODE_6_VAL;
				break;
			}
			break;
		case 5:
			rc_name = "pot1";
			rc_min = settings.RC6_MIN;
			rc_max = settings.RC6_MAX;
			currVal = rc_trim = settings.RC6_TRIM;
			rc_rev = settings.RC6_REV;				
			break;
		case 6:
			rc_name = "pot2";
			rc_min = settings.RC7_MIN;
			rc_max = settings.RC7_MAX;
			currVal = rc_trim = settings.RC7_TRIM;
			rc_rev = settings.RC7_REV;				
			break;
		case 7:
			rc_name = "pot3";
			rc_min = settings.RC8_MIN;
			rc_max = settings.RC8_MAX;
			currVal = rc_trim = settings.RC8_TRIM;
			rc_rev = settings.RC8_REV;				
			break;
		}		
	}
	
	public Channel(LaserSettings settings, int id)
	{
		this.channelId = id;
		update(settings, -1);
	}
	
	public void calcExp()
	{		
		if (positiveEXP <= 0)
		{
			fpositiveEXP = 1 + (-positiveEXP)/100 * (MAXEXP-1);
		}
		else
		{
			fpositiveEXP = 1 + (1/MAXEXP - 1) * (positiveEXP / 100);
		}
		if (negativeEXP <= 0)
		{
			fnegativeEXP = 1 + (-negativeEXP)/100 * (MAXEXP-1);
		}
		else
		{
			fnegativeEXP = 1 + (1/MAXEXP - 1) * (negativeEXP / 100);
		}		
	}
	
	public synchronized void setVal(float val)
	{
		switch (channelId) {
		case 0:
		case 1:
		case 2:
		case 3:
			this.currVal = (int) getValue(val);
			break;
		case 4:
		case 5:
		case 6:
		case 7:
			this.currVal = (int) val;
			break;
		}
	}
	
	public synchronized int getVal()
	{
		return this.currVal;
	}
	
	private float getValue(float normalizedVal) 
	{		
		float fval = 0;
		if (normalizedVal == 0)
			fval = 0;
		else if (normalizedVal > 0 ) {
			fval = positiveDR * (float)(Math.pow(Math.abs(normalizedVal),fpositiveEXP ));
		} else {
			fval = - negativeDR * (float)(Math.pow(Math.abs(normalizedVal),fnegativeEXP ));
		}	
		
		if (channelId == 2)
		{
			if (isReverse())
				return getTrim() - ((getMin()-getMax()) / 2) + ((getMin()-getMax())/2) * fval;
			return getTrim() + ((getMax()-getMin()) / 2) + ((getMax()-getMin())/2) * fval;
		}
		else
		{
			if (isReverse())
				return getTrim() + ((getMin()-getMax())/2) * fval;

//			if (channelId == 0)
//				Log.d("OSTI0", fval +" "+normalizedVal+" "+positiveDR+" "+negativeDR+" "+fpositiveEXP+" "+fnegativeEXP+" "+negativeEXP+" "+positiveEXP);
//			if (channelId == 1)
//				Log.d("OSTI1", fval +" "+normalizedVal+" "+positiveDR+" "+negativeDR+" "+fpositiveEXP+" "+fnegativeEXP+" "+negativeEXP+" "+positiveEXP);
			return getTrim() + ((getMax()-getMin())/2) * fval;
		}
	}
	
	
	public float getNormalizedValue(float currVal)
	{
		float fval = 0;	
		if (channelId == 2)
		{
			if (isReverse())			
				fval = (currVal - getTrim() + ((getMin()-getMax()) / 2)) / ((getMin()-getMax()) / 2);
			else
				fval = (currVal - getTrim() - ((getMax()-getMin()) / 2)) / ((getMax()-getMin()) / 2);
		}
		else
		{
			if (isReverse())
				fval = (currVal - getTrim()) / ((getMin()-getMax())/2);
			else
				fval = (currVal - getTrim()) / ((getMax()-getMin())/2);
		}	
		return fval;
		//TODO: sistemare gli esponenziali, specialmente i segni
//		if (fval == 0) {
//			float normalizedVal = 0;
//			return normalizedVal;
//		} else if (fval > 0) {
//			float tempNormalizedVal = fval / positiveDR;
//			double normalizedVal = Math.pow(tempNormalizedVal, (1 / fpositiveEXP));
//			return (float) normalizedVal;
//		} else {
//			float tempNormalizedVal = fval / (-negativeDR);
//			double normalizedVal = Math.pow(tempNormalizedVal, (1 / fnegativeEXP));
//			return (float) normalizedVal;
//		}
	}
	
	
	public boolean isReverse()
	{
		return (rc_rev == 1 ? true : false);
	}
	
//	public void setReverse(boolean reverse)
//	{
//		if (reverse)
//			rc_rev = 1;
//		else
//			rc_rev = 0;			
//	}
	
	public int getMin()
	{
		return rc_min;
	}
	public int getMax()
	{
		return rc_max;
	}
	
	public int getTrim()
	{
		return rc_trim;
	}
	public void setTrim(int trim)
	{
		rc_trim = trim;
	}
	
	private void setRates(int negDR, int posDR, int negEXP, int posEXP)
	{
		negativeDR = negDR / 100.0f;
		positiveDR = posDR / 100.0f;
		negativeEXP = negEXP;
		fnegativeEXP = negativeEXP / 100.0f;
		positiveEXP = posEXP;
		fpositiveEXP = positiveEXP / 100.0f;
	}
	
}
