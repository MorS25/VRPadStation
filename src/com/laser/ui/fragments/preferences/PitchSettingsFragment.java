package com.laser.ui.fragments.preferences;

import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.activities.SettingsActivity;
import com.laser.ui.widgets.GraphWidget;
import com.laser.ui.widgets.VerticalSeekBar;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PitchSettingsFragment extends Fragment implements OnSeekBarChangeListener, OnLongClickListener {


	private VrPadStationApp app;
	private TextView txtChannelName;
	
	private TextView txtReverse;
	private String strReverse = "Reverse";
	private CheckBox chkReverse;
	
	private TextView txtNegDR;
	private String strNegDR = "Negative D/R (%): ";
	private SeekBar seekBarNegDR;
	
	private TextView txtPosDR;
	private String strPosDR = "Positive D/R (%): ";
	private SeekBar seekBarPosDR;
	
	private TextView txtNegEXP;
	private String strNegEXP = "Negative EXP (%): ";
	private SeekBar seekBarNegEXP;
	
	private TextView txtPosEXP;
	private String strPosEXP = "Positive EXP (%): ";
	private SeekBar seekBarPosEXP;
	
	private GraphWidget surfaceViewGraph;
	private VerticalSeekBar vsbChannel;
	private TextView txtChannelVal;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (VrPadStationApp)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.channel_setting_fragment, container, false);
		
		if (app.settings != null)
		{
			txtChannelName = (TextView)v.findViewById(R.id.txtChannelName);
			txtChannelName.setText("PITCH");
			
			txtReverse = (TextView)v.findViewById(R.id.txtReverse);
			txtReverse.setText(strReverse);
			chkReverse = (CheckBox)v.findViewById(R.id.checkBoxReverse);
			chkReverse.setOnCheckedChangeListener(onCheckReverseChanged);
			chkReverse.setChecked(app.settings.RC2_REV == 1 ? true : false);
			
			txtNegDR = (TextView)v.findViewById(R.id.txtNegDR);
			txtNegDR.setText(strNegDR + app.settings.PITCH_NEG_DR);
			txtNegDR.setOnLongClickListener(this);
			seekBarNegDR = (SeekBar)v.findViewById(R.id.seekBarNegDR);
			seekBarNegDR.setMax(200);
			seekBarNegDR.setOnSeekBarChangeListener(this);
			seekBarNegDR.setProgress(app.settings.PITCH_NEG_DR);
			
			txtPosDR = (TextView)v.findViewById(R.id.txtPosDR);
			txtPosDR.setText(strPosDR + app.settings.PITCH_POS_DR);
			txtPosDR.setOnLongClickListener(this);
			seekBarPosDR = (SeekBar)v.findViewById(R.id.seekBarPosDR);
			seekBarPosDR.setMax(200);
			seekBarPosDR.setOnSeekBarChangeListener(this);
			seekBarPosDR.setProgress(app.settings.PITCH_POS_DR);
			
			txtNegEXP = (TextView)v.findViewById(R.id.txtNegEXP);
			txtNegEXP.setText(strNegEXP + app.settings.PITCH_NEG_EXP);
			txtNegEXP.setOnLongClickListener(this);
			seekBarNegEXP = (SeekBar)v.findViewById(R.id.seekBarNegEXP);
			seekBarNegEXP.setMax(200);
			seekBarNegEXP.setOnSeekBarChangeListener(this);
			seekBarNegEXP.setProgress(app.settings.PITCH_NEG_EXP + 100);
			
			txtPosEXP = (TextView)v.findViewById(R.id.txtPosEXP);
			txtPosEXP.setText(strPosEXP + app.settings.PITCH_POS_EXP);
			txtPosEXP.setOnLongClickListener(this);
			seekBarPosEXP = (SeekBar)v.findViewById(R.id.seekBarPosEXP);
			seekBarPosEXP.setMax(200);
			seekBarPosEXP.setOnSeekBarChangeListener(this);
			seekBarPosEXP.setProgress(app.settings.PITCH_POS_EXP + 100);
			
			RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.rlGraph);
			surfaceViewGraph = new GraphWidget(getActivity(), app.settings.PITCH_NEG_DR, app.settings.PITCH_POS_DR, app.settings.PITCH_NEG_EXP, app.settings.PITCH_POS_EXP);
			rl.addView(surfaceViewGraph);
	
			txtChannelVal = (TextView)v.findViewById(R.id.txtChannelVal);
			vsbChannel = (VerticalSeekBar)v.findViewById(R.id.vsbChannel);
			vsbChannel.setMax(app.settings.RC2_MAX - app.settings.RC2_MIN);
			vsbChannel.setOnSeekBarChangeListener(this);
			vsbChannel.setProgress(vsbChannel.getMax() / 2);
		}
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private OnCheckedChangeListener onCheckReverseChanged = new OnCheckedChangeListener() {		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (app.settings != null)
			{
				SettingsActivity.bEdited = true;
				app.settings.RC2_REV = (isChecked == true ? 1 : 0);

				SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
	    	    SharedPreferences.Editor editor = prefs.edit();
	    	    editor.putInt("RC2_REV", app.settings.RC2_REV);
	    	    editor.commit();
			}
		}
	};
	
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (app.settings != null)
		{
			switch (seekBar.getId())
			{
			case R.id.seekBarNegDR:
				SettingsActivity.bEdited = true;
				app.settings.PITCH_NEG_DR = progress;
				txtNegDR.setText(strNegDR + app.settings.PITCH_NEG_DR);
				break;
			case R.id.seekBarPosDR:
				SettingsActivity.bEdited = true;
				app.settings.PITCH_POS_DR = progress;
				txtPosDR.setText(strPosDR + app.settings.PITCH_POS_DR);
				break;
			case R.id.seekBarNegEXP:
				SettingsActivity.bEdited = true;
				app.settings.PITCH_NEG_EXP = progress - 100;
				txtNegEXP.setText(strNegEXP + app.settings.PITCH_NEG_EXP);
				break;
			case R.id.seekBarPosEXP:
				SettingsActivity.bEdited = true;
				app.settings.PITCH_POS_EXP = progress - 100;
				txtPosEXP.setText(strPosEXP + app.settings.PITCH_POS_EXP);
				break;
			case R.id.vsbChannel:
				float valNorm = normalize(progress, app.settings.RC2_MAX - app.settings.RC2_MIN);
				if (valNorm != 0)
					surfaceViewGraph.updateCursor(valNorm);
				int x = progress - ((app.settings.RC2_MAX - app.settings.RC2_MIN)/2);
				int y = calculateY(valNorm);
				txtChannelVal.setText("X: " + x + "\nY: " + y);
				break;
			}		
			if (surfaceViewGraph != null)
				surfaceViewGraph.update(app.settings.PITCH_NEG_DR, app.settings.PITCH_POS_DR, app.settings.PITCH_NEG_EXP, app.settings.PITCH_POS_EXP);
		}
	}
	
	private static float MAXEXP = 4.0f;
	private int calculateY(float x)
	{
		float negativeDR = app.settings.PITCH_NEG_DR / 100.0f;
		float positiveDR = app.settings.PITCH_POS_DR / 100.0f;
		float negativeEXP = app.settings.PITCH_NEG_EXP;
		float fnegativeEXP = negativeEXP / 100.0f;
		float positiveEXP = app.settings.PITCH_POS_EXP;
		float fpositiveEXP = positiveEXP / 100.0f;
		
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
		
		float fval = 0;
		if (x == 0)
			fval = 0;
		else if (x > 0 )
		{
			{
				fval = positiveDR * (float)(Math.pow(Math.abs(x),fpositiveEXP ));
			}
		}
		else
		{
			fval = - negativeDR * (float)(Math.pow(Math.abs(x),fnegativeEXP ));
		}		
		return (int) ((app.settings.RC2_MIN+((app.settings.RC2_MAX-app.settings.RC2_MIN)/2)) + ((app.settings.RC2_MAX-app.settings.RC2_MIN)/2) * fval);
	}
	
	private float normalize(float coord, int max)
    {
		return (float) (((coord - 0) / (max - 0) - 0.5 ) * 2);
    }
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (app.settings != null)
		{
			SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
    	    SharedPreferences.Editor editor = prefs.edit();
    	    
			switch (seekBar.getId())
			{
			case R.id.seekBarNegDR:	  	    	
	    	    editor.putInt("PITCH_NEG_DR", app.settings.PITCH_NEG_DR);
	    	    editor.commit();
				break;
			case R.id.seekBarPosDR:	  	    	
	    	    editor.putInt("PITCH_POS_DR", app.settings.PITCH_POS_DR);
	    	    editor.commit();
				break;
			case R.id.seekBarNegEXP:	  	    	
	    	    editor.putInt("PITCH_NEG_EXP", app.settings.PITCH_NEG_EXP);
	    	    editor.commit();
				break;
			case R.id.seekBarPosEXP:	  	    	
	    	    editor.putInt("PITCH_POS_EXP", app.settings.PITCH_POS_EXP);
	    	    editor.commit();
				break;
			case R.id.vsbChannel:
				vsbChannel.setProgress(vsbChannel.getMax() / 2);
				break;
			}
			surfaceViewGraph.update(app.settings.PITCH_NEG_DR, app.settings.PITCH_POS_DR, app.settings.PITCH_NEG_EXP, app.settings.PITCH_POS_EXP);
		}
	}

	@Override
	public boolean onLongClick(View arg0) {
		if (app.settings != null)
		{
			SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
    	    SharedPreferences.Editor editor = prefs.edit();
    	    
			switch (arg0.getId())
			{
			case R.id.txtNegDR:
				seekBarNegDR.setProgress(seekBarNegDR.getMax() / 2);
				
	    	    editor.putInt("PITCH_NEG_DR", app.settings.PITCH_NEG_DR);
	    	    editor.commit();
				break;
			case R.id.txtPosDR:
				seekBarPosDR.setProgress(seekBarPosDR.getMax() / 2);

	    	    editor.putInt("PITCH_POS_DR", app.settings.PITCH_POS_DR);
	    	    editor.commit();
				break;
			case R.id.txtNegEXP:
				seekBarNegEXP.setProgress(seekBarNegEXP.getMax() / 2);

	    	    editor.putInt("PITCH_NEG_EXP", app.settings.PITCH_NEG_EXP);
	    	    editor.commit();
				break;
			case R.id.txtPosEXP:
				seekBarPosEXP.setProgress(seekBarPosEXP.getMax() / 2);

	    	    editor.putInt("PITCH_POS_EXP", app.settings.PITCH_POS_EXP);
	    	    editor.commit();
				break;
			}
		}
		return false;
	}

	public void OnFragmentChanged(boolean current) {
		if (surfaceViewGraph != null)
		{
			if (current)
				surfaceViewGraph.setVisibility(View.VISIBLE);
			else
				surfaceViewGraph.setVisibility(View.INVISIBLE);
		}
	}
}