package com.lg.mobility.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.android.gms.internal.cu;
import com.google.android.gms.maps.model.LatLng;
import com.lg.mobility.data.MapDrawing.DrawingType;
import com.lg.mobility.exceptions.NoLineException;
import com.lg.mobility.exceptions.NoRouteException;


public class DataHandler {

	private TrafficJamModel[] jams;
	private String currentGpsSamples;//TODO: Should be set by map activity
	private volatile ArrayList<LatLng> currentRoute;
	private LineModel currentLineModel;
	private String android_id;
	private List<LineModel> lines;
	public ArrayList<MapDrawing> trafficLayer;
	private MapDrawing currentLineDrawing;
	private MapDrawing currentUserRouteDrawing;
	private MapDrawing[] personalizedLayer;
	private StopModel jammedStop;
	private UserInfo userInfo;
	private Integer jammedStopIndex;
	private Double jammedStopDelay;
	private String jammedStopTitle;
	public boolean lineModelHasChanged;
	public String sldRequest;
	public String sldResponse;
	private boolean stopsStored;
	private List<StaticStopModel> nearestStops;
	private List<ARStop> arStops;
	private List<ARDeparture> currentStopDepartures;
	public DataHandler(){
		trafficLayer = new ArrayList<MapDrawing>();
		lineModelHasChanged = true;
		userInfo = new UserInfo();
		stopsStored = false;
	}
	
	public TrafficJamModel[] getJams() {return jams;}
	public void setJams(TrafficJamModel[] _jams) {jams = _jams;}
	
	public String getGpsSamples() {return currentGpsSamples;}
	public void setGpsSamples(String _gpsSamples) {currentGpsSamples = _gpsSamples;}
	
	public ArrayList<LatLng> getCurrentRoute(){return currentRoute;}
	public void setCurrentRoute(ArrayList<LatLng> _currentRoute) { currentRoute = _currentRoute;}
	
	public void setCurrentRouteDrawing() throws NoRouteException
	{
		if(currentRoute == null) throw new NoRouteException();
		if(currentRoute.size() == 0) throw new NoRouteException();
		currentUserRouteDrawing = new MapDrawing(currentRoute, MapDrawing.DrawingType.USER_ROUTE);
	}
	public MapDrawing getCurrentRouteDrawing() { return currentUserRouteDrawing;}
	
	public String getAndroidID() {return android_id;}
	public void setAndroidID(String _android_id){android_id = _android_id;}
	
	public StopModel getJammedStop() {return jammedStop;}
	public void setJammedStop(StopModel _jammedStop){jammedStop = _jammedStop;}
	
	public LineModel getCurrentLineModel() { return currentLineModel;}
	public void setCurrentLineModel(LineModel _currentLineModel) {
		if(currentLineModel != null){
			if(currentLineModel.id.equals(_currentLineModel.id) && currentLineModel.direction == _currentLineModel.direction){
				lineModelHasChanged = false;
			}
			else
			{
				lineModelHasChanged = true;
			}
		}
		else
		{
			lineModelHasChanged = true;
		}
		currentLineModel = _currentLineModel;
	}
	public void setCurrentLineDrawing() throws NoLineException
	{
		if(currentLineModel == null) throw new NoLineException("Current line is not set in DataHandler");
		List<LatLng> decodedRoute= VehicleModel.getDecodedRoute(currentLineModel.id, currentLineModel.direction);
		currentLineDrawing = new MapDrawing(decodedRoute, DrawingType.SERVICE_LINE);
	}
	public MapDrawing getCurrentLineDrawing(){return currentLineDrawing;}
	public List<LineModel> getLines() { return lines;}
	public void setLines(List<LineModel> _lines) {lines = _lines;}
	
	public ArrayList<MapDrawing> getTrafficJamLayer()
	{
		return trafficLayer;
	}
	
	public MapDrawing[] getPersonalizedLayer()
	{
		return personalizedLayer;
	}
	
	public UserInfo getUserInfo() {return userInfo;}
	
	public Integer getJammedStopIndex() { return jammedStopIndex;}
	public void setJammedStopIndex(Integer _jammedStopIndex){ jammedStopIndex = _jammedStopIndex;}
	
	public Double getJammedStopDelay() { return jammedStopDelay;}
	public void setJammedStopDelay(Double _jammedStopDelay) { jammedStopDelay = _jammedStopDelay;}
	
	public String getJammedStopTitle() { return jammedStopTitle;}
	public void setJammedStopTitle(String _jammedStopTitle) { jammedStopTitle = _jammedStopTitle;}
	
	public String getSLDRequest() { return sldRequest;}
	public void setSLDRequest(String _sldRequest) { sldRequest = _sldRequest;}
	
	public String getSLDResponse() { return sldResponse;}
	public void setSLDResponse(String _sldResponse) {sldResponse = _sldResponse;}
	
//	public List<StaticStopModel> getAllStops() { return allStops;}
//	public void setAllStops(List<StaticStopModel> _allStops) { allStops = _allStops;}
	
	public boolean getStopsStored() { return stopsStored;}
	public void setStopsStored(boolean _stopsStored) {stopsStored = _stopsStored;}
	
	public List<StaticStopModel> getNearestStops(Context ctx, LatLng userCoords) { 
		DatabaseHandler dbHandler = new DatabaseHandler(ctx);
		return dbHandler.getNearestStops(userCoords.latitude, userCoords.longitude);
	}
	public void setNearestStops(List<StaticStopModel> _nearestStops) { nearestStops = _nearestStops;}
	
	public List<ARStop> getARStops() { return arStops;}
	public void setARStops(List<ARStop> _arStops) { arStops = _arStops;}
	
	public List<ARDeparture> getARDepartures() { return currentStopDepartures;}
	public void setARDepartures(List<ARDeparture> _currentStopDepartures){ currentStopDepartures = _currentStopDepartures;}
	
	public LineModel getLineByCode(String _code)
	{
		String c = _code.split("\\s+")[0];
		for(LineModel line : lines)
		{
			if(line.id.equals(c))
				return line.setDirection(Integer.parseInt(_code.split("\\s+")[1]));
		}
		return null;
	}
}
