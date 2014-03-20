package com.lg.mobility.data;


public class TimetableModel {
	public LineModel line;
	public String departureTime;
	
	public TimetableModel(LineModel _line, String _departureTime)
	{
		line = _line;
		departureTime = _departureTime;
	}
}
