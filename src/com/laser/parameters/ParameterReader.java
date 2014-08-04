package com.laser.parameters;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.laser.helpers.file.DirectoryPath;
import com.laser.helpers.file.FileList;
import com.laser.helpers.file.FileManager;
import com.laser.ui.dialogs.OpenFileDialog.FileReader;

public class ParameterReader implements FileReader {
	
	private List<Parameter> parametersList;

	public ParameterReader() {
		this.parametersList = new ArrayList<Parameter>();
	}

	public List<Parameter> getParameters() {
		return parametersList;
	}

	public boolean openFile(String itemList) 
	{
		if (!FileManager.isExternalStorageAvaliable())
			return false;
		
		try {
			FileInputStream inputStream = new FileInputStream(itemList);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			if (!isParameterFile(reader)) 
			{
				inputStream.close();
				return false;
			}
			parseParametersLines(reader);
			inputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void parseLine(String line) throws Exception 
	{
		String[] RowData = splitLine(line);
		String name = RowData[0];
		Double value = Double.valueOf(RowData[1]);
		Parameter.checkParameterName(name);
		parametersList.add(new Parameter(name, value));
	}
	
	private void parseParametersLines(BufferedReader bufferedReader) throws IOException {
		String line;
		parametersList.clear();
		while ((line = bufferedReader.readLine()) != null)
		{
			try {
				parseLine(line);
			} catch (Exception ex) {}
		}
	}

	private String[] splitLine(String line) throws Exception {
		String[] RowData = line.split(",");
		
		if (RowData.length != 2) 
			throw new Exception("Invalid Length");
		
		RowData[0] = RowData[0].trim();
		return RowData;
	}

	private static boolean isParameterFile(BufferedReader bufferedReader) throws IOException {
		return bufferedReader.readLine().contains("#NOTE");
	}

	@Override
	public String getPath() {
		return DirectoryPath.getParametersPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getParametersFileList();
	}
}
