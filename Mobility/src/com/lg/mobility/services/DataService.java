package com.lg.mobility.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.lg.mobility.R;
import com.lg.mobility.data.ARDeparture;
import com.lg.mobility.data.ARStop;
import com.lg.mobility.data.DataHandler;
import com.lg.mobility.data.DatabaseHandler;
import com.lg.mobility.data.LineModel;
import com.lg.mobility.data.MapDrawing;
import com.lg.mobility.data.MobilityApplication;
import com.lg.mobility.data.StaticStopModel;
import com.lg.mobility.data.StopModel;
import com.lg.mobility.data.TrafficJamModel;
import com.lg.mobility.exceptions.NoLineException;
import com.lg.mobility.exceptions.NoRouteException;
import com.lg.mobility.utilities.TrafficJamUpdate;

public class DataService extends IntentService {

	public DataService() {
		super("DataService");
	}

	private static final String JAM_URL = "http://213.157.92.101/jamdetector/JamService.svc/jams/hsl";
	private static final String SLD_URL = "http://mobile-sensing.west.uni-koblenz.de:8080/backend/ServiceLineDetection";
	private static final String REITTIOPAS_API_USERNAME = "nikolopo";
	private static final String REITTIOPAS_API_PASSWORD = "snnhsl";
	
	
	/**
	 * Get parameters:
	 * </br>
	 * <ul>
	 * <li> center_coordinate=2548196,6678528 </li>
	 * <li>limit=20</li>
	 * <li>diameter=1500 (max 5000 meters)</li>
	 * </ul>
	 */
//	private static final String REITTIOPAS_STOPS_IN_AREA_URL = "http://api.reittiopas.fi/hsl/prod/?request=stops_area&user=nikolopo&pass=snnhsl&format=json&center_coordinate=2548196,6678528&limit=20&diameter=1500&epsg_out=wgs84";
//	private static final String REITTIOPAS_STOPS_IN_AREA_URL = "http://api.reittiopas.fi/hsl/prod/?request=stops_area&user=nikolopo&pass=snnhsl&format=json&center_coordinate=2548196,6678528&limit=20&diameter=1500";
	//testing
//	private static final String REITTIOPAS_STOPS_IN_AREA_URL = "http://api.reittiopas.fi/hsl/prod/?request=stops_area&user=nikolopo&pass=snnhsl&format=json&center_coordinate=27.870083820596,60.216968121444&limit=20&diameter=1500&epsg_out=wgs84&limit=10&diameter=1500&epsg_in=wgs84";
	private static final String REITTIOPAS_STOPS_IN_AREA_URL = " http://api.reittiopas.fi/hsl/prod/?request=stops_area&user=nikolopo&pass=snnhsl&format=json&center_coordinate=24.870083820596,60.216968121444&limit=20&diameter=1500&epsg_out=wgs84&limit=10&diameter=1500&epsg_in=wgs84";
	/**
	 * Get parameters:
	 * - code=222222
	 */
	private static final String REITTIOPAS_STOPS_SEARCH_URL = "http://api.reittiopas.fi/hsl/prod/?request=stop&user=nikolopo&pass=snnhsl&format=json";
//	private static final String REITTIOPAS_STOPS_SEARCH_URL = "http://api.reittiopas.fi/hsl/prod/?request=stop&user=nikolopo&pass=snnhsl&format=json&code=2222222";
	
	//http://api.reittiopas.fi/hsl/prod/?request=stop&user=nikolopo&pass=snnhsl&format=json&code=2222222&epsg_in=wgs84&epsg_out=wgs84
//	private static final String SLD_URL = "http://mobile-sensing.west.uni-koblenz.de:8080/backend/ServiceLineDetectionTestAPI";
	//TODO: declare actions on manifest
	public static final String ACTION_JAM_UPDATE = "com.lg.mobility.intent.action.JAM_UPDATE";
	public static final String ACTION_SLD_UPDATE = "com.lg.mobility.intent.action.SLD_UPDATE";
	public static final String ACTION_LINES_UPDATE = "com.lg.mobility.intent.action.LINES_UPDATE";
	public static final String ACTION_STOPS_UPDATE = "com.lg.mobility.intent.action.STOPS_UPDATE";
	public static final String ACTION_JAM_DETECTED = "com.lg.mobility.intent.action.JAM_DETECTED";
	public static final String ACTION_PERS_UPDATE = "com.lg.mobility.intent.action.PERS_UPDATE";
	public static final String ACTION_RETURN_USER_SELECTED_LINE = "com.lg.mobility.intent.action.RETURN_USER_SELECTED_LINE";
	public static final String ACTION_RETURN_AR_STOPS = "com.lg.mobility.intent.action.RETURN_AR_STOPS";
	public static final String ACTION_RETURN_STOP_DEPARTURES = "com.lg.mobility.intent.action.RETURN_STOP_DEPARTURES";
	
	public static final String ACTION_GET_JAMS = "com.lg.mobility.intent.action.GET_JAMS";
	public static final String ACTION_GET_SLD = "com.lg.mobility.intent.action.GET_SLD";
	public static final String ACTION_GET_LINES = "com.lg.mobility.intent.action.GET_LINES";
	public static final String ACTION_GET_PERS = "com.lg.mobility.intent.action.GET_PERS";
	public static final String ACTION_GET_STOPS = "com.lg.mobility.intent.action.GET_STOPS";
	public static final String ACTION_GET_AR_STOPS = "com.lg.mobility.intent.action.GET_AR_STOPS";
	public static final String ACTION_GET_AR_DEPARTURES = "com.lg.mobility.intent.action.GET_AR_DEPARTURES";
	public static final String ACTION_REQUEST_USER_SELECTED_LINE = "com.lg.mobility.intent.action.REQUEST_USER_SELECTED_LINE";
	
	public static final String INTENT_USER_LATITUDE_TAG = "INTENT_USER_LATITUDE_TAG";
	public static final String INTENT_USER_LONGITUDE_TAG = "INTENT_USER_LONGITUDE_TAG";
	public static final String INTENT_AR_DEPARTURES_CODE_TAG = "INTENT_AR_DEPARTURES_CODE_TAG";
	public static final String INTENT_AR_DEPARTURES_STOP_NAME_TAG = "INTENT_AR_DEPARTURES_STOP_NAME_TAG";
	
	
	public TrafficJamModel[] trafficJams;
	
	public void getJams()
	{
		Log.i("DataService", "jams requested");
		List jams;
		String response;
		try{
			response = HttpRequest.get(JAM_URL).contentType("application/json").readTimeout(10000).body();
		}
		catch (Exception e)
		{
			return;
		}
		if(response.equals("[]")){
			broadcastAction(ACTION_JAM_UPDATE);
			return;
		}
		if(response.length() < 3) 
		{
			Log.e("DataService", "Jams invalid response: " + response);  
			return;
		}
		jams = new Gson().fromJson(response, List.class);
		trafficJams = new TrafficJamModel[jams.size()];
		final MobilityApplication app = (MobilityApplication) getApplication();
		app.dHandler.trafficLayer.clear();
		for (int i = 0; i < jams.size(); i++) {
			LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>) jams.get(i);
			trafficJams[i] = TrafficJamModel.fromLinkedTreeMap(map,
					new TrafficJamUpdate() {

						@Override
						public void drawOnMap(MapDrawing drawing) {
							Log.i("DataService", "adding traffic object");
							app.dHandler.trafficLayer.add(drawing);
						}
					});
		}
		app.dHandler.setJams(trafficJams);
		Log.i("DataService", "sending action jam update");
		broadcastAction(ACTION_JAM_UPDATE);
	}
	public void getServiceLine()
	{
		Log.i("DataService", "sld requested");
		MobilityApplication app = (MobilityApplication) getApplication();
		String gpsSamples = app.dHandler.getGpsSamples();
		String userId = app.dHandler.getUserInfo().getUserID();
		List<LineModel> lines = app.dHandler.getLines();
		if(userId == null|| gpsSamples == null || lines == null) {Log.i("DataService", "lines or samples or android_id = null");return;		}

		String postdata = getLineDetectionFormattedString(gpsSamples);
		if(postdata == null) { return;}
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SLD_URL);
		httppost.addHeader("username", userId);
		//TODO: add username provided by the Service Centre
	
		HttpResponse response = null;
		try {
			httppost.setEntity(new StringEntity(postdata));
			app.dHandler.setSLDRequest(postdata);
			response = httpclient.execute(httppost);
			String responseString =  EntityUtils.toString(response.getEntity());
			app.dHandler.setSLDResponse(responseString);
			try {
				LineModel cLine = getLineModelFromServerResponse(responseString, lines);
				app.dHandler.setCurrentLineModel(cLine);
				app.dHandler.setCurrentLineDrawing();
				broadcastAction(ACTION_SLD_UPDATE);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} catch (ClientProtocolException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}
	
	
	public void getPersonalized()
	{
		DataHandler dh = ((MobilityApplication) getApplication()).dHandler;
		if(dh.getCurrentLineModel() == null || dh.getJams() == null)
		{
			try {
				dh.setCurrentRouteDrawing();
				broadcastAction(ACTION_PERS_UPDATE);
			} catch (NoRouteException e) {
				Log.w("DataService", "No route exception");
				e.printStackTrace();
				return;
			}
			return;
		}
		MutableInt jammedStopIndex = new MutableInt();
		MutableDouble jammedStopDelay = new MutableDouble();
		String[] jammedStopTitle = new String[1];
		StopModel jammedStop = (TrafficJamModel.searchJams(dh.getJams(), dh.getCurrentLineModel().id, dh.getCurrentLineModel().direction, jammedStopIndex, jammedStopDelay, jammedStopTitle));
		dh.setJammedStopIndex(jammedStopIndex.getValue());
		dh.setJammedStopDelay(jammedStopDelay.getValue());
		dh.setJammedStopTitle(jammedStopTitle[0]);
		
		if(jammedStopIndex.getValue() == null) Log.i("DataService", "jammedStopIndex = null");
		else Log.i("DataService", "jammedStopIndex = " + jammedStopIndex.intValue());
		if(jammedStop != null)
		{
			dh.setJammedStop(jammedStop);
			broadcastAction(ACTION_JAM_DETECTED);
		}
		else
		{
			dh.setJammedStopIndex(null);
		}
		try {
			dh.setCurrentRouteDrawing();
			broadcastAction(ACTION_PERS_UPDATE);
		}
		catch (NoRouteException e)
		{
			Log.w("DataService", "No route exception");
			e.printStackTrace();
		}
	}
	public void getRoutes()
	{
		Log.i("DataService", "lines requested");
		MobilityApplication app = (MobilityApplication) getApplication();
		if(app.dHandler.getLines() != null) return;
		ArrayList<LineModel> lines = new ArrayList<LineModel>(716);
		InputStream is = getResources().openRawResource(R.raw.routes);
		InputStreamReader inputreader = new InputStreamReader(is);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		boolean skipFirstLine = true;
		try {
			while (( line = buffreader.readLine()) != null) {
		     	if(skipFirstLine)
		       	{
		      		skipFirstLine = false;
		       		continue;
		       	}
		        lines.add(LineModel.fromString(line));
		    }
			app.dHandler.setLines(lines);
			broadcastAction(ACTION_LINES_UPDATE);
		} catch (IOException e) {
			return;
		}
	}
	public void getStops()
	{
		Log.i("DataService", "stops started parsing");
		MobilityApplication app = (MobilityApplication) getApplication();
		DatabaseHandler dbHandler = new DatabaseHandler(this);
//		app.dHandler.setNearestStops(dbHandler.getNearestStops(DatabaseHandler.TEST_LAT, DatabaseHandler.TEST_LON));
		Log.i("DataService", "stops count = " + dbHandler.getStopsCount());
		if(dbHandler.getStopsCount() > 0)
		{
			app.dHandler.setStopsStored(true);
			Log.i("DataService", "table exists");
			broadcastAction(ACTION_STOPS_UPDATE);
		}
		else{
			Log.i("DataService", "table not exists");
			InputStream is = getResources().openRawResource(R.raw.stops);
			InputStreamReader inputReader;
			try {
				inputReader = new InputStreamReader(is, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				return;
			}
			BufferedReader buffreader = new BufferedReader(inputReader);
			String line;
			ArrayList<StaticStopModel> stops = new ArrayList<StaticStopModel>(7681);
			try {
				while ((line = buffreader.readLine()) != null) {
					String[] chunks = line.split(";");
					Log.i("DataService", "name : " + chunks[1]);
					Log.i("DataService", "id = " + chunks[0]);
					Log.i("DataService", "latitude = " + chunks[2]);
					Log.i("DataService", "longitude = " + chunks[3]);
					stops.add(new StaticStopModel(
							Integer.parseInt(chunks[0]),
							chunks[1],
							Double.parseDouble(chunks[2]),
							Double.parseDouble(chunks[3])
							));  
				}
			}
			catch (IOException e) {
				return;
			}
			dbHandler.addStops(stops, this);
			app.dHandler.setStopsStored(true);
			broadcastAction(ACTION_STOPS_UPDATE);
			Log.i("DataService", "stops parsing finished, size = " + stops.size());
		}
	}
	
	public void getUserSelectedLine()
	{
		Log.i("DataService", "user line requested");
		MobilityApplication app = (MobilityApplication) getApplication();
		try {
			app.dHandler.setCurrentLineDrawing();
			broadcastAction(ACTION_RETURN_USER_SELECTED_LINE);
		} catch (NoLineException e) {
			Log.w("DataService", "no line exception in get user selected line");
			e.printStackTrace();
		}
	}
	
	public void getARStops(String latitude, String longitude)
	{
		
		//TODO: timestamp of request to associate with departure time
		Log.i("DataService", "ar stops requested");
		DataHandler dh = ((MobilityApplication) getApplication()).dHandler;
		String json;
		try{
//			json = HttpRequest.get(REITTIOPAS_STOPS_IN_AREA_URL + "&center_coordinate=2548196,6678528&limit=10&diameter=1500").readTimeout(6000).body();
			json = HttpRequest.get(REITTIOPAS_STOPS_IN_AREA_URL).readTimeout(10000).body();
			Log.i("DataService", "call url = " + REITTIOPAS_STOPS_IN_AREA_URL);
			
		}
		catch(Exception e)
		{
			//connection error
			return;
		}		
		Type listType = new TypeToken<List<LinkedTreeMap<String, String>>>() {}.getType();
		List<LinkedTreeMap<String, String>> stops;
		try{
			stops = new Gson().fromJson(json, listType);
		}
		catch(Exception e)
		{
			Log.e("DataService", "getARStops() parsing error");
			return;
		}
		if(stops == null) return;
		ArrayList<ARStop> arstops = new ArrayList<ARStop>();
		for(LinkedTreeMap<String, String> stop : stops)
		{
			arstops.add(new ARStop(stop.get("code"), stop.get("name"), stop.get("coords"), stop.get("dist")));
//			if(arstops.size() == 1)
//			{
//				try{
//					json = HttpRequest.get(REITTIOPAS_STOPS_SEARCH_URL + "&code=" + stop.get("code")).readTimeout(3000).body();
//				}
//				catch(Exception e)
//				{
//					continue;
//				}
//				JsonArray fields = new Gson().fromJson(json, JsonArray.class);
//				JsonObject object = fields.get(0).getAsJsonObject();
//				JsonArray departures = object.get("departures").getAsJsonArray();
//				ArrayList<ARDeparture> arDepartures = new ArrayList<ARDeparture>();
//				for(int i = 0 ; i < departures.size(); i++)
//				{
//					JsonObject departure = departures.get(i).getAsJsonObject();
//					arDepartures.add(new ARDeparture(departure.get("code").getAsString(), departure.get("time").getAsString(), departure.get("date").getAsString(), stop.get("name"),dh));
//				}
//				JsonObject departure1 = departures.get(0).getAsJsonObject();
//				dh.setARDepartures(arDepartures);
//				broadcastAction(DataService.ACTION_RETURN_STOP_DEPARTURES);
//				Log.i("DataService", "time = " + departure1.get("time").getAsInt());
//			}
		}
		dh.setARStops(arstops);
		broadcastAction(ACTION_RETURN_AR_STOPS);
	}
	
	private void getARDepartures(String stopCode)
	{
		String code = stopCode.split(",")[0];
		String name = stopCode.split(",")[1];
		String json;
		DataHandler dh = ((MobilityApplication) getApplication()).dHandler;
		try{
			json = HttpRequest.get(REITTIOPAS_STOPS_SEARCH_URL + "&code=" + code).readTimeout(10000).body();
		}
		catch(Exception e)
		{
			return;
		}
		if(json != null)
		{
			JsonArray fields = new Gson().fromJson(json, JsonArray.class);
			JsonObject object = fields.get(0).getAsJsonObject();
			JsonArray departures = object.get("departures").getAsJsonArray();
			ArrayList<ARDeparture> arDepartures = new ArrayList<ARDeparture>();
			for(int i = 0; i < departures.size(); i++)
			{
				JsonObject departure = departures.get(i).getAsJsonObject();
				arDepartures.add(new ARDeparture(departure.get("code").getAsString(), departure.get("time").getAsString(), departure.get("date").getAsString(), name ,dh));
			}
			JsonObject departure1 = departures.get(0).getAsJsonObject();
			dh.setARDepartures(arDepartures);
			broadcastAction(DataService.ACTION_RETURN_STOP_DEPARTURES);
		}
	}
	
	private void broadcastAction(String action)
	{
		Log.i("DataService", "Sending broadcast with action : " + action);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(action);
		sendBroadcast(broadcastIntent);
	}
	
	private String getLineDetectionFormattedString(String gpsSamples) {
		ArrayList<LatLng> gpsData;
		ArrayList<String> timeStamps;
		if (gpsSamples != null) {
			if (gpsSamples.length() > 0) {
				String[] lines = gpsSamples.split("\\n");
				gpsData = new ArrayList<LatLng>(lines.length);
				timeStamps = new ArrayList<String>(lines.length);
				for (String line : lines) {
					String[] data = line.split(",");
					if (data.length > 2) {
						String[] gpsParts = data[3].split("\\s+");
						timeStamps.add(data[1]);
						gpsData.add(new LatLng(Double.parseDouble(gpsParts[0]),
								Double.parseDouble(gpsParts[1])));
					} else {
						break;
					}
				}
				if (gpsData.size() > 1) {
					return getLineDetectionFormattedString(gpsData, timeStamps);
				}
			}
		}
		return null;
	}

	private String getLineDetectionFormattedString(List<LatLng> gpsSamples,
			List<String> unixTimestamps) {
		String formattedStr = "";
		if (gpsSamples.size() > 1
				&& (gpsSamples.size() == unixTimestamps.size())) {
			for (int i = 0; i < gpsSamples.size(); i++) {
				formattedStr += gpsSamples.get(i).latitude;
				formattedStr += ",";
				formattedStr += gpsSamples.get(i).longitude;
				formattedStr += ",";
				long timeStamp = Long.parseLong(unixTimestamps.get(i));
				java.util.Date date = new java.util.Date(timeStamp);
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss,EEE", Locale.ENGLISH);
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
				formattedStr += formatter.format(date);
				formattedStr += "\n";
			}
		}
		return formattedStr;
	}
	
	private LineModel getLineModelFromServerResponse(String response, List<LineModel> lines) throws Exception
	{
		String _currentServiceId = null;
		int _currentDirection;
		JsonObject object = new Gson().fromJson(response,JsonObject.class);
		if(object == null)
		{
			return LineModel.getInvalid();
		}
		JsonArray arr = object.get("routes").getAsJsonArray();
		if(arr == null)
		{
			return LineModel.getInvalid();
		}
		if(arr.size()==0)
		{
			return LineModel.getInvalid();
		}
		else
		{
			JsonObject obj = arr.get(0).getAsJsonObject();
			String line = obj.get("route_id").getAsString();
			_currentServiceId = line;
			String shape = obj.get("shape_id").getAsString();
			_currentDirection = Character.getNumericValue(shape.charAt(shape.length()-1));
			return LineModel.searchLineModel(lines, _currentServiceId).setDirection(_currentDirection);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent.getAction().equals(ACTION_GET_JAMS))
			getJams();
		else if(intent.getAction().equals(ACTION_GET_SLD))
			getServiceLine();
		else if(intent.getAction().equals(ACTION_GET_PERS))
			getPersonalized();
		else if(intent.getAction().equals(ACTION_GET_LINES))
			getRoutes();				
		else if(intent.getAction().equals(ACTION_GET_STOPS))
			getStops();
		else if(intent.getAction().equals(ACTION_REQUEST_USER_SELECTED_LINE))
			getUserSelectedLine();
		else if(intent.getAction().equals(ACTION_GET_AR_STOPS))
		{
			String latitude = "" + intent.getDoubleExtra(INTENT_USER_LATITUDE_TAG, 0.0);
			String longitude = "" + intent.getDoubleExtra(INTENT_USER_LONGITUDE_TAG, 0.0);
			getARStops(latitude, longitude);
		}
		else if(intent.getAction().equals(ACTION_GET_AR_DEPARTURES))
		{
			String stopCode = intent.getStringExtra(INTENT_AR_DEPARTURES_CODE_TAG);
			getARDepartures(stopCode);
		}
			
	}
}
