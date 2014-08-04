package com.laser.ui.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.laser.parameters.Parameter;
import com.laser.parameters.ParameterMetadata;
import com.laser.parameters.ParameterMetadataMapReader;
import com.laser.parameters.ParametersComparator;
import com.laser.VrPadStation.R;
import com.laser.app.VrPadStationApp;
import com.laser.ui.widgets.ParameterTableRow;
import com.laser.utils.LaserConstants;

public class ParametersTableFragment extends Fragment implements OnClickListener{
	

	private TableLayout parameterTable;
	private List<ParameterTableRow> rowList = new ArrayList<ParameterTableRow>();
	
	private ProgressBar progressBarLoadingParams;
	private TextView txtViewLoadingParams;
	
	private ImageButton btnLoadFromApm;
	private ImageButton btnLoadFromFile;
	private ImageButton btnWriteToApm;
	private ImageButton btnWriteToFile;
	private TextView textViewParamName;
	private TextView textViewDescription;

    private Map<String, ParameterMetadata> metadataMap;
	
	private OnListParamsListener listener;
	public interface OnListParamsListener {
		void writeParamsToApm();
		void writeParamsToFile();
		void loadParamsFromApm();
		void loadParamsFromFile();
		
	}
	public void setListeners(OnListParamsListener listener)
	{
		this.listener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.parameters_fragment, container, false);
		textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
		textViewParamName = (TextView) view.findViewById(R.id.textViewParamName);
		btnLoadFromApm = (ImageButton) view.findViewById(R.id.btnLoadFromApm);
		btnLoadFromFile = (ImageButton) view.findViewById(R.id.btnLoadFromFile);
		btnWriteToApm = (ImageButton) view.findViewById(R.id.btnWriteToApm);
		btnWriteToFile = (ImageButton) view.findViewById(R.id.btnWriteToFile);		
		parameterTable = (TableLayout) view.findViewById(R.id.parametersTable);
		txtViewLoadingParams = (TextView) view.findViewById(R.id.txtViewLoadingParams);
		progressBarLoadingParams = (ProgressBar) view.findViewById(R.id.progressBarLoadingParams);
		Rect bounds = progressBarLoadingParams.getProgressDrawable().getBounds();	
		progressBarLoadingParams.setProgressDrawable(getResources().getDrawable(R.drawable.blue_progress_bar));
		progressBarLoadingParams.getProgressDrawable().setBounds(bounds);
		
		btnLoadFromApm.setOnClickListener(this);
		btnLoadFromFile.setOnClickListener(this);
		btnWriteToApm.setOnClickListener(this);
		btnWriteToFile.setOnClickListener(this);
		
		textViewParamName.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		textViewDescription.setTextSize(LaserConstants.TEXT_SIZE_SMALL);
		txtViewLoadingParams.setTextSize(TypedValue.COMPLEX_UNIT_SP, LaserConstants.TEXT_SIZE_MEDIUM);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		updateParamsTable();
	}
	
	private void updateParamsTable()
	{
		clearParamsTable();
		loadMetadata();
		VrPadStationApp app = (VrPadStationApp)getActivity().getApplication();
		List<Parameter> paramsList = app.parameterMananger.getParametersList();
		Collections.sort(paramsList, new ParametersComparator());
		for (Parameter p : paramsList)
		{
			try {
				Parameter.checkParameterName(p.name);
				ParameterTableRow row = findRowByName(p.name);
				if (row != null) {
					row.setParam(p);
				} else {
					addParameterRow(p);
				}
			} catch (Exception e) {}
		}
	}
	
	private void clearParamsTable()
	{
		rowList.clear();
		parameterTable.removeAllViews();
	}
	
	private void loadMetadata()
	{
		metadataMap = null;

        try {
            metadataMap = ParameterMetadataMapReader.load(getActivity(), "Params");
            Log.d("METADATA", metadataMap.toString());
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
    private ParameterMetadata getMetadata(String name) {
    	if (metadataMap == null)
    		return null;
    	else
    		return metadataMap.get(name);
    }
	
	public void onParametersReceived()
	{
		if (progressBarLoadingParams != null)
			progressBarLoadingParams.setProgress(progressBarLoadingParams.getMax());
		if (txtViewLoadingParams != null)
		{
			txtViewLoadingParams.setText("100%");
			txtViewLoadingParams.append(" | " + progressBarLoadingParams.getMax() + "/" + progressBarLoadingParams.getMax());
		}

		updateParamsTable();		
	}
	
	public void setProgressBarMax(int count)
	{
		if (progressBarLoadingParams != null)
			progressBarLoadingParams.setMax(count);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		rowList.clear();
	}

	public void onParameterReceived(Parameter parameter, short paramIndex) {
		if (progressBarLoadingParams != null)
			progressBarLoadingParams.incrementProgressBy(1);
		if (txtViewLoadingParams != null)
		{
			txtViewLoadingParams.setText((int)((progressBarLoadingParams.getProgress() * 100) / progressBarLoadingParams.getMax()) + "%");
			txtViewLoadingParams.append(" | " + progressBarLoadingParams.getProgress() + "/" + progressBarLoadingParams.getMax());
		}
	}

	private ParameterTableRow findRowByName(String name) {
		for (ParameterTableRow row : rowList) {
			if(row.getParameterName().equals(name)){
				return row;
			}				
		}
		return null;
	}

	private void addParameterRow(Parameter param){
		final ParameterTableRow pRow = new ParameterTableRow(this.getActivity());
		pRow.setParam(param);	
		pRow.setGravity(Gravity.CENTER);
		pRow.setClickable(true);
		pRow.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				ParameterMetadata pm = getMetadata(pRow.getParameterName());
				if (pm != null)
				{
					textViewParamName.setText(pm.getName());
					textViewDescription.setText(pm.getDescription() + "\n");
					if (pm.getRange() != null)
						textViewDescription.append("\nRange: " + pm.getRange());
					if (pm.getUnits() != null)
						textViewDescription.append("\nUnits: " + pm.getUnits());
					if (pm.getValues() != null)
						textViewDescription.append("\nValues: " + pm.getValues());
				}
			}
		});
		rowList.add(pRow);
		parameterTable.addView(pRow);
	}
	
	public List<Parameter> getParameterListFromTable(){
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		for (ParameterTableRow row : rowList) {
			parameters.add(row.getParameterFromRow());
		}
		return parameters;
	}
	
	public List<ParameterTableRow> getModifiedParametersRows(){
		ArrayList<ParameterTableRow> modParameters = new ArrayList<ParameterTableRow>();
		for (ParameterTableRow row : rowList) {
			if (!row.isNewValueEqualToDroneParameter()){
				modParameters.add(row);
			}
		}
		return modParameters;
	}
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.btnWriteToApm:
			listener.writeParamsToApm();
			break;
		case R.id.btnWriteToFile:
			listener.writeParamsToFile();
			break;
		case R.id.btnLoadFromApm:
			if (txtViewLoadingParams != null)
				txtViewLoadingParams.setText("");
			if (progressBarLoadingParams != null)
				progressBarLoadingParams.setProgress(0);
			listener.loadParamsFromApm();
			break;
		case R.id.btnLoadFromFile:
			listener.loadParamsFromFile();
			break;
		}
	}

}
