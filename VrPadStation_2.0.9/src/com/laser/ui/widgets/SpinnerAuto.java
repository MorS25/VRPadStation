package com.laser.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class SpinnerAuto extends Spinner {
	public interface OnSpinnerItemSelectedListener {
		void onSpinnerItemSelected(Spinner spinner, int position, String text);
	}

	OnSpinnerItemSelectedListener listener;
	public void setOnSpinnerItemSelectedListener( OnSpinnerItemSelectedListener listener) {
		this.listener = listener;
	}

	public SpinnerAuto(Context context) {
		super(context, Spinner.MODE_DROPDOWN);
	}

	public SpinnerAuto(Context context, int mode) {
		super(context, mode);
	}

	public SpinnerAuto(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SpinnerAuto(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public SpinnerAuto(Context context, AttributeSet attrs, int defStyle, int mode) {
		super(context, attrs, defStyle, mode);
	}	
	
	@Override
	public void setSelection(int position) {
		super.setSelection(position);
		if (listener != null)
			listener.onSpinnerItemSelected(this, position, getItemAtPosition(position).toString());
	}

}
