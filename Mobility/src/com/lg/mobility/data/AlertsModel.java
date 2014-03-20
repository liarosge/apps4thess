package com.lg.mobility.data;


public class AlertsModel {
	public String title;
	public  AlertType alertType;
	public String fromTime;
	public String toTime;
	
	public AlertsModel(String _title, AlertType _alertType, String _fromTime, String _toTime) {
		title = _title;
		alertType = _alertType;
		fromTime = _fromTime;
		toTime = _toTime;
	}
	public AlertsModel(){
		title = "";
		alertType = AlertType.DISRUPTION;
		fromTime = "";
		toTime = "";
	}
	public static AlertsModel initAlert(){
		return new AlertsModel();
	}
	
	public AlertsModel setTitle(String _title)
	{
		title = _title;
		return this;
	}
	
	public AlertsModel setType(AlertType _type)
	{
		alertType = _type;
		return this;
	}
	
	public AlertsModel setFromTime(String _fromTime)
	{
		fromTime = _fromTime;
		return this;
	}
	
	public AlertsModel setToTime(String _toTime)
	{
		toTime = _toTime;
		return this;
	}
}
