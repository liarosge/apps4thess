package com.lg.mobility.data;

public class ARDeparture {
	public String code;
	public String time;
	public String date;
	public String stopName;
	public LineModel lineAssoc;
	
	public ARDeparture(String _code, String _time, String _date, String _stopName, DataHandler dataHandler)
	{
		code = _code;
		time = _time;
		time = _time.charAt(0) + "" +  _time.charAt(1) + ":" + _time.charAt(2) + "" + _time.charAt(3);
		date = _date;
		stopName = _stopName;
		lineAssoc = dataHandler.getLineByCode(_code);
	}
}
