package com.lg.mobility.data;



public class RecordingsModel {
	
	public String title;
	public String duration;
	public TransportationType transportationType;
	public String distance;
	public String speed;
	public int progress;
	public String date;
	
	public RecordingsModel(String _title, String _duration, TransportationType _transportationType, String _distance, int _progress, String _speed, String _date) {
		title = _title;
		duration = _duration;
		transportationType = _transportationType;
		distance = _distance;
		progress = _progress;
		speed = _speed;
		date = _date;
	}
}
