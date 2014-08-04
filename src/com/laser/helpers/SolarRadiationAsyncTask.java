package com.laser.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class SolarRadiationAsyncTask extends AsyncTask<Void, Void, Integer> {
	
	private static String NOAA_AK_URL = "http://www.swpc.noaa.gov/ftpdir/lists/geomag/AK.txt";

	@Override
	protected Integer doInBackground(Void... params) {
        for (int i = 0; i < 6; i++)
        {
            try {
            	URL url = new URL(NOAA_AK_URL);
            	BufferedReader buffReader = getBufferFromUrl(url);
            	if (buffReader != null) {
	            	String ak = getStringFromBuffer(buffReader);
	                if (ak != null) {
	                	return ApIndexParser.parse(ak);
	                }
            	}
                Thread.sleep(1000 * i * i);
            } catch (Exception e) {
            }
        }
        return null;
    }
	
	private static String getStringFromBuffer(BufferedReader buffReader)
	{        
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while ((line = buffReader.readLine()) != null) 
            {
            	stringBuilder.append(line + "\n");
            }
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return stringBuilder.toString();
	}

    private static BufferedReader getBufferFromUrl(URL url) 
    {
        URLConnection urlConnection;
        InputStream inputStream = null;
        try {
        	urlConnection = url.openConnection();
        	inputStream = urlConnection.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BufferedReader buffReader = null;
        try {
        	buffReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
        return buffReader;
    }
    
    
	public static class ApIndexParser 
	{
		/** Parses the AK.txt file from: http://www.swpc.noaa.gov/ftpdir/lists/geomag/AK.txt */		
		public static int parse(String string) 
		{
	        int lastNumber = -1;
	        String[] lines = string.split("\n");
	        for (int i = 0; i < lines.length; i++)
	        {	
	        	String currLine = lines[i];
	        	
	            // use only the Planetary line
	            if (currLine.startsWith("Planetary")) 
	            {	
	                // cut the description
	            	currLine = currLine.substring(currLine.indexOf(")") + 1);
	
	                // search for the current value looking at all numbers and finding the last value that is not -1	
	                String[] numbers = currLine.split(" ");
                	for (int j = 0; j < numbers.length; j++)
	                {
                		String currNumber = numbers[j];
	                    try {
	                        int number = Integer.parseInt(currNumber);
	                        if (number == -1) 
	                            return lastNumber;	// founded!	
	                        else
	                        	lastNumber = number; // not founded. Continue searching
	                    } catch (Exception ex) {
	                    	ex.printStackTrace();
	                    }
	                }	
	            }	
	        }
	        return -1; // nothing found
	    }
	}
	
	
}
