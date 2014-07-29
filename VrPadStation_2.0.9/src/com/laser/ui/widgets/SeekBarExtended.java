package com.laser.ui.widgets;

import android.R.attr;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarExtended extends LinearLayout implements OnSeekBarChangeListener {

	private TextView textView;
	private SeekBar seekBar;
	private double min = 0;
	private double increment = 1;
	private String text = "";
	private String unit = "";
	
	private boolean isInteger = false;
	
	
	private SeekBarExtendedChangedListener listener;
	public interface SeekBarExtendedChangedListener {
		public void onSeekBarChangedListener(SeekBarExtended seekBar);
	}
	public void setOnSeekBarExtendedChangedListener(SeekBarExtendedChangedListener listener) {
		this.listener = listener;
	}

	public SeekBarExtended(Context context) {
		super(context);
		createView(context);
	}

	public SeekBarExtended(Context context, AttributeSet attrs) {
		super(context, attrs);
		createView(context);
	}

	public SeekBarExtended(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createView(context);
	}

	private void createView(Context context) 
	{
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(VERTICAL);
		
		textView = new TextView(context);
		textView.setTextAppearance(context, attr.textAppearanceMedium);
		
		seekBar = new SeekBar(context);
		seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		seekBar.setOnSeekBarChangeListener(this);
		
		addView(textView);
		addView(seekBar);
	}
	
	public void setText(CharSequence text, boolean isInteger) {
		this.isInteger = isInteger;
		this.text = text.toString();
		updateText();
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public void setMinMaxInc(double min, double max, double increment) {
		this.min = min;
		this.increment = increment;
		seekBar.setMax((int) ((max - min) / increment));
	}
	
	private void updateText() {
		if (isInteger)
			textView.setText(String.format("%s\t%d %s", text, (int)getValue(), unit));
		else
			textView.setText(String.format("%s\t%2.1f %s", text, getValue(), unit));
	}

	public double getValue() {
		return (seekBar.getProgress() * increment + min);
	}

	public void setValue(double value) {
		seekBar.setProgress((int) ((value - min) / increment));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		updateText();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (listener != null)
			listener.onSeekBarChangedListener(this);
	}

}
