package com.lg.mobility.data;

import com.google.android.gms.maps.model.LatLng;

public class StaticStopModel {

	public int id;
	public String name;
	public LatLng coord;
	
	public StaticStopModel(int stop_id, String stop_name, double stop_lat, double stop_lon)
	{
		id = stop_id;
		name = stop_name;
		coord = new LatLng(stop_lat, stop_lon);
	}
}
