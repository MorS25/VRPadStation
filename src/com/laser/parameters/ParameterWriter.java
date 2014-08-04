package com.laser.parameters;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.laser.helpers.file.FileManager;
import com.laser.helpers.file.FileStream;

public class ParameterWriter {
	
	private List<Parameter> parametersList;

	public ParameterWriter(List<Parameter> param) {
		this.parametersList = param;
	}

	public boolean saveParametersToFile() 
	{
		try {
			if (!FileManager.isExternalStorageAvaliable())
				return false;

			FileOutputStream outputStream = FileStream.getParameterFileStream();
			writeFirstParametersLine(outputStream);
			writeParametersLines(outputStream);
			outputStream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeFirstParametersLine(FileOutputStream outputStream) throws IOException 
	{
		outputStream.write((new String("#NOTE: " + FileManager.getTimeStamp()+"\n").getBytes()));
	}
	private void writeParametersLines(FileOutputStream outputStream) throws IOException
	{
		for (Parameter param : parametersList) 
		{
			outputStream.write(String.format(Locale.ENGLISH, "%s , %f\n", param.name, param.value).getBytes());
		}
	}
}
