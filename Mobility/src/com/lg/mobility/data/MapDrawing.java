package com.lg.mobility.data;

import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

public class MapDrawing {
	/*
	 * Map Objects indices
	 * Route Line  : color = green , zindex = 0 , alpha = 100
	 * Jam Line    : color = red   , zindex = 2 , alpha = 180
	 * User Line   : color = cyan  , zindex = 3 , alpha = 180
	 * Service Line: color = yellow, zindex = 1 , aplha = 180
	 * Marker      : color =   -   , zindex = 4 , alpha =  - 
	 */
	public List<LatLng> poly;
	public LatLng circleCenter;
	public DrawingType drawingType;
	public enum DrawingType
	{
		CIRCLE, JAM_LINE, ROUTE_LINE, USER_ROUTE, SERVICE_LINE;
		
		public int getAlpha()
		{
			switch(this)
			{
			case CIRCLE:
				return 255;
			case ROUTE_LINE:
				return 100;
			case JAM_LINE:
				return 180;
			case USER_ROUTE:
				return 180;
			case SERVICE_LINE:
				return 255;
			default:
				return 0;
			}
		}
		public float getZIndex()
		{
			switch(this)
			{
			case CIRCLE:
				return 0.0f;
			case ROUTE_LINE:
				return 1.0f;
			case JAM_LINE:
				return 3.0f;
			case USER_ROUTE:
				return 4.0f;
			case SERVICE_LINE:
				return 2.0f;
			default:
				return 0;
			}
		}
		
		public int getColor()
		{
			switch(this)
			{
			case CIRCLE:
				return Color.argb(getAlpha(), 255, 0 ,0);
			case ROUTE_LINE:
				return Color.argb(getAlpha(), 0, 255, 0);
			case JAM_LINE:
				return Color.argb(getAlpha(), 255, 0, 0);
			case USER_ROUTE:
				return Color.argb(getAlpha(), 0, 255, 255);
			case SERVICE_LINE:
				return Color.argb(getAlpha(), 255, 255, 0);
			default:
				return Color.alpha(0);
			}
		}
	}
	
	public MapDrawing(List<LatLng> _poly, DrawingType _drawingType) {
		poly = _poly;
		drawingType = _drawingType;
	}
	
	public MapDrawing(LatLng _circleCenter)
	{
		circleCenter = _circleCenter;
		drawingType = DrawingType.CIRCLE;
	}
	
	public int getColor()
	{
		return drawingType.getColor();
	}
	public float getZIndex()
	{
		return drawingType.getZIndex();
	}
}
