package com.laser.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.laser.VrPadStation.R;
import com.laser.utils.LaserConstants;

public class SettingsActivity extends ParentActivity implements OnClickListener {


    public static boolean bEdited = false;
    public static boolean bForceRestart = false;
    
    private ImageButton btnSettingsGcs;
    private ImageButton btnSettingsRadio;
    private ImageButton btnSettingsGimbal;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getActionBar().hide();
        
        // Manca il layout portrait. Lo lascio bloccato in landscape visto che l'applicazione si usa così.
        setContentView(R.layout.activity_settings);
        setResult(Activity.RESULT_CANCELED);
        
        btnSettingsGcs = (ImageButton) findViewById(R.id.btnSettingsGcs);
        btnSettingsRadio = (ImageButton) findViewById(R.id.btnSettingsRadio);
        btnSettingsGimbal = (ImageButton) findViewById(R.id.btnSettingsGimbal);
        
        btnSettingsGcs.setOnClickListener(this);
        btnSettingsRadio.setOnClickListener(this);
        btnSettingsGimbal.setOnClickListener(this);
    }
    
	@Override
	public void onBackPressed() {
		if (bEdited)
		{
			Intent intent = new Intent();
			intent.putExtra("bForceRestart", bForceRestart);
			setResult(Activity.RESULT_OK, intent);
		}
		else
			setResult(Activity.RESULT_CANCELED);			
		finish();
	}
	
	@Override
	public void onClick(View v) {
		if (v == btnSettingsGcs) {
			Intent iSettings = new Intent(this, SettingsActivityGcs.class);
			try {
				startActivityForResult(iSettings, LaserConstants.SETTINGS_CODE);
			}catch (Exception e) {
				e.printStackTrace();
			}
		} else if (v == btnSettingsRadio) {
			Intent iSettings = new Intent(this, SettingsActivityRadio.class);
			try {
				startActivityForResult(iSettings, LaserConstants.SETTINGS_CODE);
			}catch (Exception e) {
				e.printStackTrace();
			}			
		} else if (v == btnSettingsGimbal) {
			Intent iSettings = new Intent(this, SettingsActivityGimbal.class);
			try {
				startActivityForResult(iSettings, LaserConstants.SETTINGS_CODE);
			}catch (Exception e) {
				e.printStackTrace();
			}			
		}
		super.onClick(v);
	}
	
}
