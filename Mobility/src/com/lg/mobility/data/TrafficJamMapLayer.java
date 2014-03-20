package com.lg.mobility.data;

import java.util.ArrayList;

import com.google.android.gms.maps.model.PolylineOptions;

public class TrafficJamMapLayer {
	public ArrayList<PolylineOptions> routeLines;
	public ArrayList<PolylineOptions> jamLines;
	
	public TrafficJamMapLayer()
	{
		routeLines = new ArrayList<PolylineOptions>();
		jamLines = new ArrayList<PolylineOptions>();
	}
	
	public void addRouteLine(PolylineOptions polyOpt)
	{
		routeLines.add(polyOpt);
	}
	
	public void addJamLine(PolylineOptions polyOpt)
	{
		jamLines.add(polyOpt);
	}
	
}
