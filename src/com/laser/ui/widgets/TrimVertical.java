package com.laser.ui.widgets;


import com.laser.VrPadStation.R;
import com.laser.utils.LaserConstants;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrimVertical extends LinearLayout implements View.OnClickListener {

	private TextView txtVal;
	private ImageButton btnPlus;
	private ImageButton btnMinus;
	
	private Channel ch;
	
	
	private TrimVerticalListener listener;
	public void setListener(TrimVerticalListener listener)
	{
		this.listener = listener;
	}
    public interface TrimVerticalListener {
		void onTrimClick();    	
    }
	
	public TrimVertical(Context context, AttributeSet attr, Channel ch) {
	    super(context,attr);
	    this.ch = ch;
	    inflate(context, R.layout.trim_vertical, this);
	    setupViewItems();
    }
	
	private void setupViewItems()
	{
		txtVal = (TextView)findViewById(R.id.txtVal);
		btnPlus = (ImageButton)findViewById(R.id.btnPlus);
		btnMinus = (ImageButton)findViewById(R.id.btnMinus);
		//txtVal.setText(ch.getVal()+"");
		txtVal.setText(ch.getTrim()+"");
		btnPlus.setTag("btnPlus");
		btnPlus.setOnClickListener(this);
		btnMinus.setTag("btnMinus");
		btnMinus.setOnClickListener(this);
		
		txtVal.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
	}

    @Override
    public void onFinishInflate() {
    	// 	this is the right point to do some things with View objects,
    	// 	as example childs of THIS View object
    }

	@Override
	public void onClick(View v) {
		if (v.getTag().equals(btnMinus.getTag()))
		{
			ch.setTrim(ch.getTrim()-1);
			//txtVal.setText(ch.getVal()+"");
			txtVal.setText(ch.getTrim()+"");
		}
		else if (v.getTag().equals(btnPlus.getTag()))
		{
			ch.setTrim(ch.getTrim()+1);
			//txtVal.setText(ch.getVal()+"");
			txtVal.setText(ch.getTrim()+"");
		}
		listener.onTrimClick();
	}
	
	public void updateTrim()
	{
		txtVal.setText(ch.getTrim()+"");
	}

}
