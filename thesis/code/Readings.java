package com.discovergy.apiclient;

import java.io.FileWriter;

import com.discovergy.apiclient.DiscovergyApiClient;
import com.github.scribejava.core.model.Verb;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;


public class Readings {
    public static String METER_ID_FILE_PATH = "MeterIDs.txt";
    public static boolean VERBOSE = true;

	
	public static long secToMilliSec(long x) {
		return(x * 1000);
	}
	public static void main(String[] args) throws Exception {
		
		
		int lineNumber = 0;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(METER_ID_FILE_PATH));
			String meterID = null;
			
			while ((meterID = in.readLine()) != null) {
				lineNumber ++;
				if (VERBOSE)
					System.out.println("Meter progress: Meter no. "
										+lineNumber+" requested");
				
				String jsonObject = "{\""+meterID+"\":[";
				
				int counter = 0;
				int timeFrom = 1483225200;	 // 1483225200 = 2017-01-01
				int timeTo = 1514764800;	 // 1514764800 = 2018-01-01
				while ((timeFrom < timeTo)) {
					DiscovergyApiClient apiClient 
					= new DiscovergyApiClient("exampleApiClient");
					String response 
					= apiClient.executeRequest(apiClient.createRequest(Verb.GET, "/readings"
							+ "?meterId="+meterID+"&"
							+ "fields=energy,power,energyOut&"
							+ "from="+secToMilliSec(timeFrom)+"&"
							+ "to="+secToMilliSec(timeFrom + 10 * 60 * 60 * 24)+"&"
							+ "resolution=three_minutes"), 200).getBody();
					
					jsonObject 
					= jsonObject.concat(response.substring(1, response.length() -1)) + ",";
					
					counter ++;
					if (VERBOSE)
						System.out.println("Time intervall progress: "
										   +counter+" out of 37 intervalls received");
					timeFrom = timeFrom + 10 * 60 * 60 * 24;
				}
				jsonObject = jsonObject.substring(0, jsonObject.length() -1) + "]}";
				FileWriter fileWriter = null;
				try {
					String filename = ""+meterID+"_"+lineNumber+".json";
					fileWriter = new FileWriter(filename);
					fileWriter.write(jsonObject);
					fileWriter.flush();
					if (VERBOSE)
						System.out.println("Success: "+filename+" saved");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					fileWriter.close();
				}
		
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		
	}		
}