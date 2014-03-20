package com.lg.mobility.data;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.internal.LinkedTreeMap;

public class StopModel {
	public double latitude,longitude;
	public String name, code;
	public StopModel nextStop, previousStop;
	public String lineID;
	public StopModel (String _code, double _latitude, double _longitude, String _name)
	{
		code = _code;
		latitude = _latitude;
		longitude = _longitude;
		name = _name;
	}
	
	public StopModel(String _code, double _latitude, double _longitude, String _name,
			StopModel _previousStop, StopModel _nextStop)
	{
		code = _code;
		latitude = _latitude;
		longitude = _longitude;
		name = _name;
		if(_previousStop == null)
			previousStop = this;
		else
			previousStop = _previousStop;
		if(_nextStop == null)
			nextStop = this;
		else
			nextStop = _nextStop;
	}
	
	public StopModel(String _code, double _latitude, double _longitude)
	{
		code = _code;
		latitude = _latitude;
		longitude = _longitude;
		name = "Unnamed Stop";
	}
	
//	public StopModel(String _code, double _latitude, double _longitude, String _name,  StopModel _nextStop, StopModel _previousStop)
//	{
//		code = _code;
//		latitude = _latitude;
//		longitude = _longitude;
//		name = _name;
//		nextStop = _nextStop;
//		previousStop = _previousStop;
//	}
	
	public static StopModel fromLinkedTreeMap(LinkedTreeMap<String, Object> treeMap)
	{
		String _code = (String) treeMap.get("Code");
		String _name = (String) treeMap.get("Name");
		Double _latitude = (Double) treeMap.get("Latitude");
		Double _longitude = (Double) treeMap.get("Longitude");
		return new StopModel(_code, _latitude.doubleValue(), _longitude.doubleValue(), _name);
	}
	
	public static StopModel fromLinkedTreeMaps(LinkedTreeMap<String, Object> stop, 
			LinkedTreeMap<String, Object> previousStop,
			LinkedTreeMap<String, Object> nextStop)
	{
		String _code = (String) stop.get("Code");
		String _name = (String) stop.get("Name");
		Double _latitude = (Double) stop.get("Latitude");
		Double _longitude = (Double) stop.get("Longitude");
		StopModel _previousStop;
		StopModel _nextStop;
		if(previousStop == null)
			Log.i("StopModel", "previousStop == null");
		if(nextStop == null)
			Log.i("StopModel", "nextStop == null");
		try{
			_previousStop = new StopModel((String) previousStop.get("Code"),
					((Double) previousStop.get("Latitude")).doubleValue(),
					((Double) previousStop.get("Longitude")).doubleValue());
			
		}
		catch (NullPointerException e)
		{
			_previousStop = null;
			Log.i("StopModel", "previousStop == null");
		}
		try{
			_nextStop = new StopModel((String) nextStop.get("Code"),
					((Double) nextStop.get("Latitude")).doubleValue(),
					((Double) nextStop.get("Longitude")).doubleValue());
		}
		catch (NullPointerException e)
		{
			_nextStop = null;
			Log.i("StopModel", "nextStop == null");
		}
		return new StopModel(_code, _latitude, _longitude, _name, _previousStop, _nextStop);
	}
//	public static StopModel fromLinkedTreeMapWithNearStops(LinkedTreeMap<String, Object> treeMap)
//	{
//		String _code = (String) treeMap.get("Code");
//		String _name = (String) treeMap.get("Name");
//		Double _latitude = (Double) treeMap.get("Latitude");
//		Double _longitude = (Double) treeMap.get("Longitude");
//		return null;
//		
//	}
	public void setNearestStops()
	{
		if(lineID != null)
		{
			String response = HttpRequest.get("http://83.145.232.209:10001/?type=stoplocations&line=" + lineID + "&direction=1").body();
			Log.e("sns", "response = " + response);
			Log.e("sns", "response length = " + response.length());
			if(response.length()  > 1)
			{
				String[] stops = response.split("\\n");
				int currentStopIndex = getCurrentStopIndex(stops);
				if(stops.length == 0)
				{
					previousStop = this;
					nextStop = this;
				}
				else if(currentStopIndex==0)
				{		
					previousStop = this;
					String[] nextStopStr = stops[currentStopIndex +1].split(";");
					nextStop = new StopModel(nextStopStr[0], Double.valueOf(nextStopStr[1]).doubleValue(),
							Double.valueOf(nextStopStr[2]).doubleValue());
				}
				else if(currentStopIndex >= stops.length - 1)
				{
					String[] previousStopStr = stops[currentStopIndex-1].split(";");
					previousStop = new StopModel(previousStopStr[0], Double.valueOf(previousStopStr[1]).doubleValue(),
							Double.valueOf(previousStopStr[2]).doubleValue());
					nextStop = this;
				}
				else
				{
					Log.e("sns", "currIdx = " + currentStopIndex);
					Log.e("sns", "length = " + stops.length);
					String[] previousStopStr = stops[currentStopIndex-1].split(";");
					String[] nextStopStr = stops[currentStopIndex + 1].split(";");
					previousStop = new StopModel(previousStopStr[0], Double.valueOf(previousStopStr[1]).doubleValue(),
							Double.valueOf(previousStopStr[2]).doubleValue());
					nextStop = new StopModel(nextStopStr[0], Double.valueOf(nextStopStr[1]).doubleValue(),
							Double.valueOf(nextStopStr[2]).doubleValue());
				}
				
//				for(String stop : stops)
//				{
				//				String[] data = stop.split(";");
				//
				//				double curLatitude = Double.valueOf(data[1]).doubleValue();
				//				curLatitude = (double) Math.round(curLatitude * 100000)/100000;
				//				Log.e("data", "latitude = " + String.valueOf(curLatitude));
				//				Log.e("data" , "this latitude = " + String.valueOf(latitude));
				//				if(curLatitude == latitude)
//						Log.e("data", "got it");
//				}
			}
			else
			{
				previousStop = this;
				nextStop = this;
			}
		}
	}	
	
	private int getCurrentStopIndex(String[] stops)
	{
		
		double distanceLA, distanceLO;
		int index = 0;
		String[] data = stops[0].split(";");
		double curLatitude = Double.valueOf(data[1]).doubleValue();
		double curLongitude = Double.valueOf(data[2]).doubleValue();
		distanceLA = Math.abs(latitude - curLatitude);
		distanceLO = Math.abs(longitude - curLongitude);
		for(int i = 1; i < stops.length; i++)
		{
			data = stops[i].split(";");
			curLatitude = Double.valueOf(data[1]).doubleValue();
			curLongitude = Double.valueOf(data[2]).doubleValue();
			double curDistanceLA = Math.abs(latitude - curLatitude);
			double curDistanceLO = Math.abs(longitude - curLongitude);
			if((curDistanceLA + curDistanceLO) < (distanceLA + distanceLO))
			{
				distanceLA = curDistanceLA;
				distanceLO = curDistanceLO;
				index = i;
			}
		}
		return index;
		
	}
	public LatLng getLatLng()
	{
		return new LatLng(latitude, longitude);
	}
	
	
}
