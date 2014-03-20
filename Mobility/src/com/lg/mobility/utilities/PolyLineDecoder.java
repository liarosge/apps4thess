package com.lg.mobility.utilities;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class PolyLineDecoder {
	
	public static List<LatLng> decode(String encoded) {
		List<LatLng> track = new ArrayList<LatLng>();
		int index = 0;
		int lat = 0, lng = 0;

		while (index < encoded.length()) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				try
				{
					b = encoded.charAt(index++) - 63;
				}
				catch (StringIndexOutOfBoundsException e)
				{
					break;
				}
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			track.add(p);
		}
		return track;
	}
	
	public static List<LatLng> decodeWithLimits(double upLatitudeLimit, double upLongitudeLimit,
			double downLatitudeLimit, double downLongitudeLimit, String encodedPoly)
	{
		List<LatLng> track = new ArrayList<LatLng>();
		int index = 0;
		int lat = 0, lng = 0;

		while (index < encodedPoly.length()) {
			int b, shift = 0, result = 0;
			do {
				b = encodedPoly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				try
				{
					b = encodedPoly.charAt(index++) - 63;
				}
				catch (StringIndexOutOfBoundsException e)
				{
					break;
				}
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;
			
			LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			track.add(p);
		}
		if(track.size() > 0)
		{
			int upLimit = getIndexOfClosestPoint(upLatitudeLimit, upLongitudeLimit, track);
			int downLimit = getIndexOfClosestPoint(downLatitudeLimit, downLongitudeLimit, track, upLimit);
			if(upLimit > downLimit)
			{
				return track.subList(downLimit, upLimit);
			}
			else if(upLimit == downLimit)
			{
				return null;
			}
			else{
				return track.subList(upLimit, downLimit);
			}
		}
		else
		{
			return null;
		}
	}
	
	private static int getIndexOfClosestPoint(double lat, double lon, List<LatLng> points)
	{
		double curLatPoint = (double) points.get(0).latitude;
		double curLonPoint = (double) points.get(0).longitude;
		double distanceLA = Math.abs(lat - curLatPoint);
		double distanceLO = Math.abs(lon - curLonPoint);
		double distance = distanceLA + distanceLO;
		int index = 0;
		for(int i = 1; i < points.size(); i++)
		{
			distanceLA = Math.abs(lat - (double) points.get(i).latitude);
			distanceLO = Math.abs(lon - (double) points.get(i).longitude);
			if((distanceLA + distanceLO) < distance)
			{
				distance = distanceLA + distanceLO;
				index = i;
			}
		}
		return index;
	}
	private static int getIndexOfClosestPoint(double lat, double lon, List<LatLng> points, int limit)
	{
		double curLatPoint = (double) points.get(0).latitude;
		double curLonPoint = (double) points.get(0).longitude;
		double distanceLA = Math.abs(lat - curLatPoint);
		double distanceLO = Math.abs(lon - curLonPoint);
		double distance = distanceLA + distanceLO;
		int index = 0;
		for(int i = 1; i < limit; i++)
		{
			distanceLA = Math.abs(lat - (double) points.get(i).latitude);
			distanceLO = Math.abs(lon - (double) points.get(i).longitude);
			if((distanceLA + distanceLO) < distance)
			{
				distance = distanceLA + distanceLO;
				index = i;
			}
		}
		return index;
	}
}
