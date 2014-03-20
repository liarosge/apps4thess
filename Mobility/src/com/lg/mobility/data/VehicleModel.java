package com.lg.mobility.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.internal.LinkedTreeMap;
import com.lg.mobility.utilities.PolyLineDecoder;

public class VehicleModel {
	public double delay;
	public String id, time, timestamp;
	public double latitude, longitude;
	public String lineID;
	public RouteModel route;
	public double direction;
	public List<LatLng> decodedPoly;
	public static File cacheDir;
	
	public VehicleModel(double _delay, String _id, String _time, String _timestamp,
			double _latitude, double _longitude)
	{
		delay = _delay;
		id = _id;
		time = _time;
		timestamp = _timestamp;
		latitude = _latitude;
		longitude = _longitude;
		getLineID();
		getRoute();
	}
	
	public VehicleModel(double _delay, String _id, String _time, String _timestamp,
			double _latitude, double _longitude, String _lineID, double _direction)
	{
		delay = _delay;
		id = _id;
		time = _time;
		timestamp = _timestamp;
		latitude = _latitude;
		longitude = _longitude;
		lineID = _lineID;
		direction = _direction;
		getRoute();
	}
	
	public static VehicleModel fromLinkedTreeMap(LinkedTreeMap<String, Object> treeMap)
	{
		Double _delay = (Double) treeMap.get("Delay");
		String _id = (String) treeMap.get("Id");
		Double _latitude = (Double) treeMap.get("Latitude");
		Double _longitude = (Double) treeMap.get("Longitude");
		String _time = (String) treeMap.get("Time");
		String _timestamp = (String) treeMap.get("Timestamp");
		String _lineID = (String) treeMap.get("LineId");
//		Log.i("VehicleModel", "line ID =  " + _lineID);
		Double _direction = (Double) treeMap.get("LineDirection");
		return new VehicleModel(_delay.doubleValue(), _id, _time, _timestamp,
				_latitude.doubleValue(), _longitude.doubleValue(), _lineID, _direction.doubleValue());
	}
	
	private void getRoute()
	{
		if(lineID != null)
		{
//			Log.i("VehicleModel", "lineID = " + lineID + " direction = " + direction);
//			Log.i("VehicleModel", "route link = " + "http://83.145.232.209:10001/?type=route&line=" + lineID + "&direction=" + (int)direction);
			try {
				route = RouteModel.getRoute(lineID, (int) direction, cacheDir);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			if(route == null) return;
//			encodedRoute = HttpRequest.get("http://83.145.232.209:10001/?type=route&line=" + lineID + "&direction=" + (int) direction).body();
			decodedPoly = PolyLineDecoder.decode(route.hexEncoded);
//			Log.i("VehicleModel", "decodedPoly length = " + decodedPoly.size());
		}
	}
	private void getLineID()
	{
		String response =  HttpRequest.get("http://83.145.232.209:10001/?type=vehicles&lng1=23&lat1=60&lng2=26&lat2=61&ids=" + id).body();
		String[] data = response.split(";");
		lineID = data[1];
	}
	
	public static List<LatLng> getDecodedRoute(String _lineID, int _direction)
	{
		RouteModel route;
		try {
			route = RouteModel.getRoute(_lineID, _direction, cacheDir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;			
		}
		if(route == null) return null;
		Log.i("VehicleModel", "ptt response = " + route.hexEncoded);
		List<LatLng> returnValue = PolyLineDecoder.decode(route.hexEncoded);
		Log.i("VehicleModel", "ptt returnValue size = " + returnValue.size());
		return PolyLineDecoder.decode(route.hexEncoded);
	}
}
