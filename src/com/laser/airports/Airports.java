package com.laser.airports;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.laser.utils.LaserConstants;

import android.content.Context;
import android.util.Log;

public class Airports {
	
	// http://www.unece.org/cefact/locode/welcome.html
	// http://www.partow.net/miscellaneous/airportdatabase/index.html
	// http://ourairports.com/data/
	// http://openflights.org/data.html
		
	public boolean checkDuplicates = false;
	private List<Airport> airports = new ArrayList<Airport>();
	private Context context;	
	
	private AirportsListener listener;
	public interface AirportsListener {
		public void onAirportsLoaded(List<Airport> airports);
	}
	public void setListener(AirportsListener listener) {
		this.listener = listener;
	}
	
	
	public int getAirportsCount() {
		return airports.size();
	}	

	public List<Airport> getAirports() {
		return airports;
	}
	
	private void AddAirport(Airport airport) {
		if (checkDuplicates) 
		{
            for (Airport item : airports)
            {
                if (item.GetDistance(airport.position) < 1000) // 1000m
                    return;
            }
        }
        airports.add(airport);
	}
	
	private void ReadOurairports(Context context) {		 
		String csvFile = "Airports/airports.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";	 
		try {	 
			//br = new BufferedReader(new FileReader(csvFile));
			br = new BufferedReader(new InputStreamReader(context.getAssets().open(csvFile)));
			while ((line = br.readLine()) != null) 
			{	 
		        // use comma as separator
				String[] items = line.split(cvsSplitBy);
				
				if (items.length == 0)
                    continue;
				
				if (items[0] == "\"id\"")
                    continue;

                if (items[1].length() != 6) // "xxxx"
                    continue;

                if (items[2].contains("small_airport") || items[2].contains("heliport") || items[2].contains("closed"))
                    continue;

                String name = items[3];
                int latOffset = 0;
                while (name.charAt(0) == '"' && name.charAt(name.length() - 1) != '"')
                {
                    latOffset += 1;
                    name = name + "," + items[3 + latOffset];
                }
                name = name.trim();
                double lat = Double.parseDouble(items[4 + latOffset].trim());
                double lng = Double.parseDouble(items[5 + latOffset].trim());

                Airport newAirport = new Airport(name, lat, lng);
                AddAirport(newAirport);
				
                if (LaserConstants.DEBUG)
                	Log.d("AIRPORTS", newAirport.name + " " + newAirport.position.latitude + " " + newAirport.position.longitude);										   
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	 
	}

	private void ReadOpenflights(Context context) {
		String datFile = "Airports/airports.dat";
		BufferedReader br = null;
		String line = "";
		String datSplitBy = ",";	 
		try {	 
			//br = new BufferedReader(new FileReader(csvFile));
			br = new BufferedReader(new InputStreamReader(context.getAssets().open(datFile)));
			while ((line = br.readLine()) != null) 
			{	 
		        // use comma as separator
				String[] items = line.split(datSplitBy);
				
				if (items.length == 0)
                    continue;
				
				String name = items[1];
                int latOffset = 0;
                while (name.charAt(0) == '"' && name.charAt(name.length() - 1) != '"')
                {
                    latOffset += 1;
                    name = name + "," + items[2 + latOffset];
                }
                name = name.trim();

                if (items[5 + latOffset].length() != 6)
                    continue;

                double lat = Double.parseDouble(items[6 + latOffset].trim());
                double lng = Double.parseDouble(items[7 + latOffset].trim());
				
                Airport newAirport = new Airport(name, lat, lng);
                AddAirport(newAirport);
                if (LaserConstants.DEBUG)
                	Log.d("AIRPORTS", newAirport.name + " " + newAirport.position.latitude + " " + newAirport.position.longitude);										   
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	public void LoadAirports(Context ctx) {
		context = ctx;
//      Airports.ReadOurairports(context);
//      Airports.checkDuplicates = true;
//      Airports.ReadOpenflights(context);
        //new AirportsLoader().execute();
		AirportsLoader r = new AirportsLoader();
		Thread t = new Thread(r);
		t.start();
	}
	
	private class AirportsLoader implements Runnable {
		public void run() {
	        ReadOurairports(context);
	        checkDuplicates = true;
	        ReadOpenflights(context);
			if (LaserConstants.DEBUG)
				Log.d("AIRPORTS", "Loaded " + getAirportsCount() + " airports");
            if (listener != null)
            	listener.onAirportsLoaded(airports);
		}
	}
	
}