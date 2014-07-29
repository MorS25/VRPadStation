package com.laser.ui.widgets;

import com.laser.VrPadStation.R;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
	
	private final String TAG = getClass().getName();
	
	private float mMinValue      = 0;
	private float mInterval      = 1;
	private float mCurrentValue  = 0;
	private String mUnitsRight = "";
	private SeekBar mSeekBar;
	
	private TextView mStatusText;
	
	private boolean isInteger = false;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs) {
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setOnSeekBarChangeListener(this);
		
		setWidgetLayoutResource(R.layout.seek_bar_preference);
	}
	
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		View view = super.onCreateView(parent);
		
		// The basic preference layout puts the widget frame to the right of the title and summary,
		// so we need to change it a bit - the seekbar should be under them.
		LinearLayout layout = (LinearLayout) view;
		layout.setOrientation(LinearLayout.VERTICAL);
		
		return view;
	}
	
	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		try {
			// move our seekbar to the new view we've been given
			ViewParent oldContainer = mSeekBar.getParent();
			ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
			
			if (oldContainer != newContainer) {
				// remove the seekbar from the old view
				if (oldContainer != null) {
					((ViewGroup) oldContainer).removeView(mSeekBar);
				}
				// remove the existing seekbar (there may not be one) and add ours
				newContainer.removeAllViews();
				newContainer.addView(mSeekBar, ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
			}
		}
		catch(Exception ex) {
			Log.e(TAG, "Error binding view: " + ex.toString());
		}
		
		//if dependency is false from the beginning, disable the seek bar
		if (view != null && !view.isEnabled())
		{
			mSeekBar.setEnabled(false);
		}
		
		updateView(view);
	}
    
    	/**
	 * Update a SeekBarPreference view with our current state
	 * @param view
	 */
	protected void updateView(View view) {

		try {
			mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
			if (isInteger)
				mStatusText.setText(String.valueOf((int)mCurrentValue));
			else
				mStatusText.setText(String.format("%2.1f", mCurrentValue));
			//mSeekBar.setProgress((int) ((mCurrentValue - mMinValue) / mInterval));

			TextView unitsRight = (TextView)view.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);
			
		}
		catch(Exception e) {
			Log.e(TAG, "Error updating seek bar preference", e);
		}
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//		float newValue = progress + mMinValue;
//		
//		newValue = newValue / mInterval * mInterval;  
		
		// change rejected, revert to the previous value
		if(!callChangeListener(getValue())){
			seekBar.setProgress((int) ((mCurrentValue - mMinValue) / mInterval));
			return; 
		}

		// change accepted, store it
		mCurrentValue = getValue();
		if (mStatusText != null)
		{
			if (isInteger)
				mStatusText.setText(String.valueOf((int)getValue()));
			else
				mStatusText.setText(String.format("%2.1f", getValue()));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (isInteger)
			persistInt((int)getValue());
		else
			persistFloat(getValue());
		notifyChanged();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mSeekBar.setEnabled(enabled);
	}
	
	
	public void setMinMaxInc(float min, float max, float inc, boolean isInteger) {
		this.isInteger = isInteger;
		this.mMinValue = min;
		this.mInterval = inc;
		mSeekBar.setMax((int) ((max - min) / inc));
	}

	public void setUnit(String unit) {
		mUnitsRight = unit;
	}

	public float getValue() {
		return (mSeekBar.getProgress() * mInterval + mMinValue);
	}

	public void setValue(float value) {
		mSeekBar.setProgress((int) ((value - mMinValue) / mInterval));
	}
}

