package com.lg.mobility.data;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

public class PersonalizedTrafficInputFormat {
	public LineModel serviceLine;
	public ArrayList<LatLng> currentRoute;
	public int direction;
	public PersonalizedTrafficInputFormat(LineModel _serviceLine, ArrayList<LatLng> _currentRoute, int _direction)
	{
		serviceLine = _serviceLine;
		currentRoute = _currentRoute;
		direction = _direction;
	}
}
