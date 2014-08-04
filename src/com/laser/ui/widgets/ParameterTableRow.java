package com.laser.ui.widgets;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.laser.parameters.Parameter;

public class ParameterTableRow extends TableRow implements TextWatcher {

	private Parameter param;
	private TextView textViewName;
	private EditText textViewValue;

	public ParameterTableRow(Context context) {
		super(context);
		createRow(context);
	}

	public ParameterTableRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		createRow(context);
	}

	public void setParam(Parameter param) {
		this.param = param;
		textViewName.setText(param.name);
		textViewValue.setText(param.getValue());
	}

	private void createRow(Context context) {
		textViewName = new TextView(context);
		textViewValue = new EditText(context);
		textViewValue.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		float pxTextViewName = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
		float pxTextViewValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
		
		textViewName.setWidth((int) pxTextViewName);
		textViewValue.setWidth((int) pxTextViewValue);

		addView(textViewName);
		addView(textViewValue);

		textViewValue.addTextChangedListener(this);
	}

	public Parameter getParameterFromRow(){
		return (new Parameter(param.name, getParamValue(), param.type));
	}

	public double getParamValue() {
		try
		{
			return Double.parseDouble(textViewValue.getText().toString());
		}
		catch (NumberFormatException e) {
			NumberFormat nf = NumberFormat.getInstance(Locale.ITALIAN);
			try {
				return (Double) nf.parse(textViewValue.getText().toString());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		return 0;
	}
	
	public String getParameterName(){
		return param.name;
	}
		
	public void restoreColor() {
		textViewValue.setTextColor(Color.WHITE);
	}
	
	@Override
	public void afterTextChanged(Editable s) {			
		if (isNewValueEqualToDroneParameter()) {
			textViewValue.setTextColor(Color.WHITE);
		}else{			
			textViewValue.setTextColor(Color.RED);
		}
	}

	public boolean isNewValueEqualToDroneParameter() {
		return param.getValue().equals(textViewValue.getText().toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
