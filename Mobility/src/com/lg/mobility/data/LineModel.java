package com.lg.mobility.data;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class LineModel {
	public String id;
	public String shortName;
	public String longName;
	public String startName;
	public String stopName;
	public int direction;
	
	public enum Type
	{
		TRAM, SUBWAY, RAIL, BUS, FERRY, CABLE_CAR, GONDOLA, FUNICULAR, UKNOWN;
	}
	public Type type;
	
	public LineModel(String _id, String _shortName, 
			String _longName, String _startName, 
			String _stopName, Type _type)
	{
		id = _id;
		shortName = _shortName;
		longName = _longName;
		startName = _startName;
		stopName = _stopName;
		type = _type;
	}
	
	public static LineModel fromString(String line)
	{
		String[] data = line.split(",");
		String[] path = data[3].split("-");
		String _startName = path[0].trim();
		String _stopName = path[path.length-1].trim();
		Type _type;
		switch(Character.getNumericValue((data[5].charAt(0))))
		{
		case 0:
			_type = Type.TRAM;
			break;
		case 1:
//			_type = Type.SUBWAY;
			_type = Type.RAIL;
			break;
		case 109:
			_type = Type.RAIL;
			break;
		case 2:
			_type = Type.RAIL;
			break;
		case 3:
			_type = Type.BUS;
			break;
		case 4:
			_type = Type.FERRY;
			break;
		case 5:
			_type = Type.CABLE_CAR;
			break;
		case 6:
			_type = Type.GONDOLA;
			break;
		case 7:
			_type = Type.FUNICULAR;
			break;
		default:
			_type = Type.UKNOWN;
			break;
		}
		
		return new LineModel(data[0], data[2], data[3], _startName, _stopName, _type);
	}
	
	public LineModel setDirection(int _direction)
	{
		direction = _direction;
		return this;
	}
	
	public static LineModel getLineModel(List<LineModel> lines, String id)
	{
		for(LineModel line : lines)
			if(line.id.equals(id))
				return line.setDirection(1);
		return null;
	}
	public static LineModel getLineModelFromShort(List<LineModel> lines, String shortName)
	{
		for(LineModel line : lines)
			if(line.shortName.equals(shortName))
				return line.setDirection(1);
		return null;
	}
	
	public static LineModel searchLineModel(List<LineModel> lines, String id)
	{
		for(LineModel line : lines)
			if(line.id.equals(id))
				return line.setDirection(1);
		return null;
	}
	
	public static LineModel getInvalid()
	{
		String _id = "";
		String _shortName = "";
		String _longName = "";
		String _startName = "";
		String _stopName = "";
		Type _type = Type.UKNOWN;
		return new LineModel(_id, _shortName, _longName, _startName, _stopName, _type);
	}
}
