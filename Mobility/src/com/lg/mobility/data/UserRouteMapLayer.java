package com.lg.mobility.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class UserRouteMapLayer {
	public LatLng startPosition;
	public LatLng stopPosition;
	public PolylineOptions route;
	public PolylineOptions lineRoute;
	
	public UserRouteMapLayer(){}
	
	
	public LatLng setStartPosition(LatLng _startPosition)
	{
		startPosition = _startPosition;
		return startPosition;
	}
	public LatLng setStopPosition(LatLng _stopPosition)
	{
		stopPosition = _stopPosition;
		return stopPosition;
	}
	public PolylineOptions setNewLineRoute(PolylineOptions polyOpt)
	{
		lineRoute = polyOpt;
		return lineRoute;
	}
	public PolylineOptions setNewRoute(PolylineOptions polyOpt)
	{
		route = polyOpt;
		return route;
	}
}
