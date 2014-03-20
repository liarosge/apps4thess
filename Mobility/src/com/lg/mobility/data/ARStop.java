package com.lg.mobility.data;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.util.Log;

/*
 * 			Log.i("DataService", "stop code = " + stop.get("code"));
			Log.i("DataService", "stop code = " + stop.get("name"));
			Log.i("DataService", "stop code = " + stop.get("city"));
			Log.i("DataService", "stop code = " + stop.get("coords"));
			Log.i("DataService", "stop code = " + stop.get("dist"));
			Log.i("DataService", "stop code = " + stop.get("codeShort"));
			Log.i("DataService", "stop code = " + stop.get("address"));
 */
public class ARStop {
	public String code;
	public String name;
	public LatLng coords;
	public String dist;
	public ArrayList<ARDeparture> departures;
	public ARStop (String _code, String _name, String _coords, String _dist)
	{
		code = _code;
		name = _name;
		String[] coordsSplit = _coords.split(",");
//		String latitudeStr = coordsSplit[0].substring(0, 2) + "." + coordsSplit[0].substring(2, coordsSplit[0].length());
//		String longitudeStr = coordsSplit[1].substring(0, 2) + "." + coordsSplit[1].substring(2, coordsSplit[1].length());
		coords = new LatLng(Double.parseDouble(coordsSplit[0]), Double.parseDouble(coordsSplit[1]));
		Log.d("ARStop", "pre coordsSplit[0] = " + coordsSplit[0] + " coordsSplit[1] = " + coordsSplit[1]);
		Log.d("ARStop", "chunk 0 = " + coordsSplit[0] + " chunk 1 = " + coordsSplit[1]);
		Log.d("ARStop", " latitude = " + coords.latitude + " longitude = " + coords.longitude);
		dist = _dist;
	}

}
