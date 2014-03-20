package com.lg.mobility.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lg.mobility.R;
import com.lg.mobility.data.DataHandler;
import com.lg.mobility.data.HARAdapter;
import com.lg.mobility.data.LineModel;
import com.lg.mobility.data.LinesAdapter;
import com.lg.mobility.data.MapDrawing;
import com.lg.mobility.data.MobilityApplication;
import com.lg.mobility.services.DataService;
import com.lg.mobility.services.ServiceCentreService;

import eu.liveandgov.wp1.sensor_collector.ASCTemplateActivity;
import eu.liveandgov.wp1.sensor_collector.ServiceSensorControl;

/**
 * Map Activity
 */

/*
 * Map Objects indices
 * Route Line  : color = green , zindex = 0 , alpha = 100
 * Jam Line    : color = red   , zindex = 2 , alpha = 180
 * User Line   : color = cyan  , zindex = 3 , alpha = 180
 * Service Line: color = yellow, zindex = 1 , aplha = 180
 * Marker      : color =   -   , zindex = 4 , alpha =  - 
 */

public class MapActivity extends ASCTemplateActivity implements
		 OnClickListener, ConnectionCallbacks, com.google.android.gms.location.LocationListener, OnConnectionFailedListener, DrawerListener {
	private static final int TRAFFIC_UPDATE_INTERVAL = 250000;
	private static final int SERVICE_LINE_UPDATE_INTERVAL = 25000;
	private static final int PERSONALIZED_TRAFFIC_UPDATE_INTERVAL = 10000;
	private static final float MAP_POLYLINE_WIDTH_DP = 5.0f;
	
	
	public static Location lastLoc;
	private SharedPreferences userPrefs;
	private long trafficRequestTimestamp;
	private long sldRequestTimestamp;
	private long personalizedToggleOnTimestamp;
	private long recordingStartedTimestamp;
	private long recordingvsotrecordingTimestamp;
	private DrawerLayout mDrawerLayout;
	public static Context ctx;
	private ScrollView mDrawerSideBarLayout;
	private final int recordingNotificationID = 1;
	private final int alertNotificationID = 2;
	private volatile boolean killTraff;
	private volatile boolean killPer;
	private volatile boolean killSLD;
	private NotificationManager mNotificationManager;
	private LinearLayout mTopView;
	private View currentAlertView;
	private Dialog lineSearchDialog;
	private Dialog activitySelectDialog;
	private Dialog directionSelectDialog;
	private SupportMapFragment mMapfragment;
	private LocationClient mLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    
//    private boolean startedFromAlertNotification;//not passed through bundle
    private String alertTitle;
    private String alertDescription;
    private boolean trafficSidebarTogglePressed;
    private boolean personalizedSidebarTogglePressed;
    private boolean satteliteSidebarTogglePressed;
    private Bundle previousCameraPosition;
    private Bundle timingVariables;
    private boolean dismissedTrafficNotification;
    private boolean automaticActivityRecognitionRunning;
    private int selectedActivityRecognitionIndex;
    private boolean automaticSLDRunning;
    private String selectedLineID;
    private boolean recordingStartedAndCameraMoved;
    private boolean shouldGetLocationForFirstTime;
    private boolean alertHasBeenDismissed;
//    private static ArrayList<Polyline> trafficJamLayer;
    private static ArrayList<Polyline> tramRoutesLayer;
    private static ArrayList<Polyline> jammedSectionsLayer;
    private Polyline serviceLineLayer;
    private Polyline userRoute;
    private Polyline detectedJamSection;
    private Marker startPositionMarker;
    private Marker stopPositionMarker;
    private Marker myLocationMarker;
    private Marker jamMarker;
    private Circle myLocationAccuracyCircle;
    private static BitmapDescriptor markerIcon;
    private static BitmapDescriptor markerStopIcon;
    private static LatLng recordingStartPosition;
    private static LatLng recordingStopPosition;
    //SavedInstanceState names
    private static final String ALERT_TITLE = "ALERT_TITLE";
    private static final String ALERT_DESCRIPTION = "ALERT_DESCRIPTION";
    private static final String TRAFFIC_SIDEBAR_TOGGLE_PRESSED = "TRAFFIC_SIDEBAR_TOGGLE_PRESSED";
    private static final String PERSONALIZED_SIDEBAR_TOGGLE_PRESSED = "PERSONALIZED_SIDEBAR_TOGGLE_PRESSED";
    private static final String SATTELITE_SIDEBAR_TOGGLE_PRESSED = "SATTELITE_SIDEBAR_TOGGLE_PRESSED";
    private static final String PREVIOUS_CAMERA_POSITION_BUNDLE = "PREVIOUS_CAMERA_POSITION_BUNDLE";
    private static final String PREVIOUS_LATITUDE = "LATITUDE";
    private static final String PREVIOUS_LONGITUDE = "LONGITUDE";
    private static final String PREVIOUS_ZOOM = "ZOOM";
    private static final String DISMISSED_TRAFFIC_NOTIFICATION = "DISMISSED_TRAFFIC_NOTIFICATION";
    private static final String AUTOMATIC_ACTIVITY_RECOGNITION_RUNNING = "AUTOMATIC_ACTIVITY_RECOGNITION_RUNNING";
    private static final String SELECTED_ACTIVITY_RECOGNITION_INDEX = "SELECTED_ACTIVITY_RECOGNITION_INDEX";
    private static final String AUTOMATIC_SLD_RUNNING = "AUTOMATIC_SLD_RUNNING";
    private static final String SELECTED_LINE_ID = "SELECTED_LINE_ID";
    private static final String RECORDING_STARTED_AND_CAMERA_MOVED = "RECORDING_STARTED_AND_CAMERA_MOVED";
    private static final String ALERT_HAS_BEEN_DISMISSED = "ALERT_HAS_BEEN_DISMISSED";
    private static final String SHOULD_GET_LOCATION_FOR_FIRST_TIME = "SHOULD_GET_LOCATION_FOR_FIRST_TIME";
    private static final String STARTED_FROM_RECORDING_NOTIFICATION = "STARTED_FROM_NOTIFICATION";
    private static final String STARTED_FROM_ALERT_NOTIFICATION = "STARTED_FROM_ALERT_NOTIFICATION";
    private static final String TIMING_VARIABLES_BUNDLE= "TIMING_VARIABLES";
    private static final String PERSONALIZED_VIEW_START_TIME = "PERSONALIZED_VIEW_START_TIME";
    private static final String RECORDING_STARTED_TIMESTAMP = "RECORDING_STARTED_TIMESTAMP";
    private static final String RECORDING_VS_NOT_RECORDING_TIMESTAMP = "RECORDING_VS_NOT_RECORDING_TIMESTAMP";
    private static final String SAVED_INSTANCE_STATE_BUNDLE = "SAVED_INSTANCE_STATE_BUNDLE";//Passed via intent from notification
    private float polyWidth;
    private enum RecordingState {
		IDLE, RECORDING, TRANSFERING;
	}
    private RecordingState state;
    
	private GoogleMap mGoogleMap;
	private Drawable topStartRecordingState, topOnRecordingState,
			topOnRecordingCompletedState, circle, rect, upload;
	private BroadcastReceiver _receiver;
	
	private DataHandler dataHandler;
	
	// data updates
	final Handler mHandler = new Handler();
	
	
	private boolean bound = false;
	private Runnable jamUpdates, SLDUpdates, persUpdates, lineUpdates, statusUpdates;
	
	
	// data
//	private static ArrayList<LineModel> lines;
	private static LinesAdapter linesAdapter;
	private ServiceSensorControl sensorService;
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			ServiceSensorControl.LocalBinder binder = (ServiceSensorControl.LocalBinder) service;
			sensorService = binder.getService();
			((MobilityApplication) getApplication()).dHandler.setCurrentRoute(sensorService.getCurrentRoute());
			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};
	
	// Activity Lifecycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(getIntent().getBooleanExtra("AR", false)){
			//if comes from AR change activity transition
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			getIntent().putExtra("AR", false);//reset
		}
		//set the context 
		ctx = this;
		//get user preferences
		userPrefs = getSharedPreferences(ServiceCentreService.USER_INFO_PREFS, 0);
		//get width of polylines in map depending on the screen of the device
		float density = getResources().getDisplayMetrics().density;
		polyWidth = MAP_POLYLINE_WIDTH_DP * density;
		//store marker icons for reuse if not already stored
		if(markerIcon == null)
			markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_a);
		if(markerStopIcon == null)
			markerStopIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_b);
		//flags to kill updates
		killPer = false;
		killSLD = false;
		killTraff = false;
		if(savedInstanceState != null)
		{
			//get variables from previous instance state if existing
			trafficSidebarTogglePressed = savedInstanceState.getBoolean(TRAFFIC_SIDEBAR_TOGGLE_PRESSED);
			personalizedSidebarTogglePressed = savedInstanceState.getBoolean(PERSONALIZED_SIDEBAR_TOGGLE_PRESSED);
			satteliteSidebarTogglePressed = savedInstanceState.getBoolean(SATTELITE_SIDEBAR_TOGGLE_PRESSED);
			previousCameraPosition = savedInstanceState.getBundle(PREVIOUS_CAMERA_POSITION_BUNDLE);
			dismissedTrafficNotification = savedInstanceState.getBoolean(DISMISSED_TRAFFIC_NOTIFICATION);
			automaticActivityRecognitionRunning = savedInstanceState.getBoolean(AUTOMATIC_ACTIVITY_RECOGNITION_RUNNING);
			selectedActivityRecognitionIndex = savedInstanceState.getInt(SELECTED_ACTIVITY_RECOGNITION_INDEX);
			automaticSLDRunning = savedInstanceState.getBoolean(AUTOMATIC_SLD_RUNNING);
			selectedLineID = savedInstanceState.getString(SELECTED_LINE_ID);
			recordingStartedAndCameraMoved = savedInstanceState.getBoolean(RECORDING_STARTED_AND_CAMERA_MOVED);
			shouldGetLocationForFirstTime = savedInstanceState.getBoolean(SHOULD_GET_LOCATION_FOR_FIRST_TIME);
			alertHasBeenDismissed = savedInstanceState.getBoolean(ALERT_HAS_BEEN_DISMISSED);
			timingVariables = savedInstanceState.getBundle(TIMING_VARIABLES_BUNDLE);
			personalizedToggleOnTimestamp = timingVariables.getLong(PERSONALIZED_VIEW_START_TIME);
			recordingStartedTimestamp = timingVariables.getLong(RECORDING_STARTED_TIMESTAMP);
			recordingvsotrecordingTimestamp = timingVariables.getLong(RECORDING_VS_NOT_RECORDING_TIMESTAMP);
		}
		else
		{
			//init variables
			trafficSidebarTogglePressed = true;
			personalizedSidebarTogglePressed = false;
			satteliteSidebarTogglePressed = false;
			previousCameraPosition = null;
			dismissedTrafficNotification = false;
			automaticActivityRecognitionRunning = true;
			selectedActivityRecognitionIndex = 0;
			automaticSLDRunning = true;
			selectedLineID = null;
			recordingStartedAndCameraMoved =false;
			shouldGetLocationForFirstTime = true;
			alertHasBeenDismissed = false;
			timingVariables = null;
			personalizedToggleOnTimestamp = 0;
			recordingStartedTimestamp = 0;
			recordingvsotrecordingTimestamp = System.currentTimeMillis();
		}
		//set the DataHandler object
		MobilityApplication app = (MobilityApplication) getApplication();
		dataHandler = app.dHandler;
		//communicate android id with sensor collector
		//in case of an error the id '0' is communicated
		if(dataHandler.getUserInfo().getUserID() != null)
		{
			sendID(dataHandler.getUserInfo().getUserID());
		}
		else{
			SharedPreferences userPrefs = getSharedPreferences(ServiceCentreService.USER_INFO_PREFS, 0);
			String id = userPrefs.getString(ServiceCentreService.USER_INFO_TAG_USER_ID, "0");
			sendID(id);
		}
		//initialize updates receiver to be started onResume()
		initUpdates();
		//TODO: create a new blue dot in fireworks
		//TODO: delete stops
		if(!dataHandler.getStopsStored())
			requestData(DataService.ACTION_GET_STOPS);
		//setup map and location client
		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
		mMapfragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);
		setupMapIfNeeded();
		setUpLocationClientIfNeeded();
		//setup views
		setupViews();
		permissionCheck();
	}
	@Override
	protected void onStart() {
		Intent intent = new Intent(this, ServiceSensorControl.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onResume() {
		startUpdates();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		killUpdates();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		unbindService(mConnection);
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			SharedPreferences prefs = getSharedPreferences("camera", 0);
			SharedPreferences.Editor editor = prefs.edit();
			CameraPosition cameraPos = mGoogleMap.getCameraPosition();
			editor.putFloat(PREVIOUS_LATITUDE, (float)cameraPos.target.latitude);
			editor.putFloat(PREVIOUS_LONGITUDE, (float) cameraPos.target.longitude);
			editor.putFloat(PREVIOUS_ZOOM, cameraPos.zoom);
			editor.commit();
			if(isRecording)
				startRecording();
			if(personalizedToggleOnTimestamp != 0)
				EasyTracker.getInstance(this).send(MapBuilder.createTiming("view_time", (System.currentTimeMillis() - personalizedToggleOnTimestamp), "personalized_view_time", "disabled_by_activity_destroy").build());
		}
		if(startPositionMarker != null){
			startPositionMarker.remove();
			startPositionMarker = null;
		}
		if(stopPositionMarker != null){
			stopPositionMarker.remove();
			stopPositionMarker = null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    switch(keyCode)
	    {
	        case KeyEvent.KEYCODE_BACK:
	            moveTaskToBack(true);
	            return true;
	    }
	    return false;
	}
	
	private void setupMapIfNeeded()
	{
		if (mGoogleMap == null) {
			mMapfragment.setRetainInstance(false);
			mGoogleMap = mMapfragment.getMap();
			if(mGoogleMap == null){
				Log.i("MapActivity", "map is null");
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setupMapIfNeeded();
						
					}
				}, 2000);
				return;
			}
			if(satteliteSidebarTogglePressed)
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			UiSettings settings = mGoogleMap.getUiSettings();
			settings.setCompassEnabled(true);
			settings.setZoomControlsEnabled(false);
			settings.setMyLocationButtonEnabled(true);
			if(previousCameraPosition != null)
			{
				mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(previousCameraPosition.getDouble(PREVIOUS_LATITUDE), previousCameraPosition.getDouble(PREVIOUS_LONGITUDE)), previousCameraPosition.getFloat(PREVIOUS_ZOOM)));
				if(tramRoutesLayer != null)
					for(Polyline poly : tramRoutesLayer)
						poly.setVisible(trafficSidebarTogglePressed);
				if(jammedSectionsLayer != null)
					for(Polyline poly : jammedSectionsLayer)
						poly.setVisible(trafficSidebarTogglePressed);
				if(serviceLineLayer != null)
					serviceLineLayer.setVisible(personalizedSidebarTogglePressed);
				if(personalizedSidebarTogglePressed && recordingStartPosition != null && startPositionMarker == null)
				{
					startPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStartPosition).icon(markerIcon));
				}
				if(personalizedSidebarTogglePressed && recordingStopPosition != null && stopPositionMarker == null)
				{
					stopPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStopPosition).icon(markerStopIcon));
				}
			}
			else
			{
				SharedPreferences prefs = getSharedPreferences("camera", 0);
				float prevLatitude = prefs.getFloat(PREVIOUS_LATITUDE, 0.0f);
				float prevLongitude = prefs.getFloat(PREVIOUS_LONGITUDE, 0.0f);
				float prevZoom = prefs.getFloat(PREVIOUS_ZOOM, 0.0f);
				if(prevLatitude != 0.0f)
				{
					mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(prevLatitude,prevLongitude), prevZoom));
				}
			}
		}
	}
	
    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }
    
	private void requestData(String action)
	{
		if(action.equals(DataService.ACTION_GET_JAMS)){
			if(!(userPrefs.getString(ServiceCentreService.MOB_CLIENT_GETTRAFFICINFO, "0").equals("1") && userPrefs.getString(ServiceCentreService.TJD_API_GETJAMS_HSL, "0").equals("1")))//check for jams permission
				return;
		}
		else if(action.equals(DataService.ACTION_GET_SLD)){
			if(!userPrefs.getString(ServiceCentreService.SDM_API_SERVICE_LINE_DETECTION, "0").equals("1"))//check for sld permission
				return;
		}
		else if(action.equals(DataService.ACTION_GET_PERS)){
			if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_GETTRAFFICINFO, "0").equals("1"))// check for pers permission
				return;
		}
		if(action.equals(DataService.ACTION_GET_JAMS))
			trafficRequestTimestamp = System.currentTimeMillis();
		else if(action.equals(DataService.ACTION_GET_SLD))
			sldRequestTimestamp =  System.currentTimeMillis();
		Intent intent = new Intent(this, DataService.class);
		intent.setAction(action);
		startService(intent);
	}
	private void onJamsReceived()
	{
		EasyTracker.getInstance(this).send(MapBuilder.createEvent("traffic_jam_detector", "jams_received", null, null).build());
		@SuppressWarnings("unchecked")
		List<MapDrawing> objects = (List<MapDrawing>) dataHandler.getTrafficJamLayer().clone();
		try{
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("traffic_jam_detector", "number_jams", null, (long) objects.size()/2).build());
		}
		catch(Exception e){}
//		if(trafficJamLayer!= null)
//		{
//			for(Polyline poly : trafficJamLayer)
//				poly.remove();
//		}
		if(tramRoutesLayer != null)
			for(Polyline poly : tramRoutesLayer)
				poly.remove();
		if(jammedSectionsLayer != null)
			for(Polyline poly : jammedSectionsLayer)
				poly.remove();
		else
		{
//			trafficJamLayer = new ArrayList<Polyline>();
			tramRoutesLayer = new ArrayList<Polyline>();
			jammedSectionsLayer = new ArrayList<Polyline>();
		}
		if(objects.size() == 0)
			Toast.makeText(this, "No problems in traffic", Toast.LENGTH_LONG).show();
		for(MapDrawing object : objects)
		{
			if(object.poly == null) continue;
			Polyline poly = mGoogleMap.addPolyline(new PolylineOptions().addAll(object.poly).color(object.getColor()).zIndex(object.getZIndex()).width(polyWidth));
			poly.setVisible(trafficSidebarTogglePressed);
			if(object.drawingType == MapDrawing.DrawingType.ROUTE_LINE)
			{
				tramRoutesLayer.add(poly);
			}
			else if(object.drawingType == MapDrawing.DrawingType.JAM_LINE)
			{
				jammedSectionsLayer.add(poly);
			}
//			trafficJamLayer.add(poly);
		}
		if(trafficRequestTimestamp != 0)
			EasyTracker.getInstance(ctx).send(MapBuilder.createTiming("traffic_jam_detector", System.currentTimeMillis() - trafficRequestTimestamp , "jam_update_time", null).build());
	}
	private void onLinesReceived()
	{
		Log.i("MapActivity", "lines received");
		List<LineModel> lines = dataHandler.getLines();
		linesAdapter = new LinesAdapter(this, R.layout.line_list_item, lines, new Drawable[]{getResources().getDrawable(R.drawable.ic_bus_gray), getResources().getDrawable(R.drawable.ic_ferry_gray), getResources().getDrawable(R.drawable.ic_tram_gray), getResources().getDrawable(R.drawable.ic_unknown_gray), getResources().getDrawable(R.drawable.ic_rail_gray)});
		ListView lineList = (ListView) lineSearchDialog.findViewById(R.id.line_search_dialog_listview);
		lineList.setAdapter(linesAdapter);
	}
	
	private void onStopsReceived(){
		//pass
	}
	private void onSLDReceived()
	{
		Log.i("MapActivity", "sld received");
		try{
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("service_line_detection", "received_line_id", dataHandler.getCurrentLineModel().id, null).build());
		}
		catch(Exception e){}
		if(sldRequestTimestamp != 0)
			EasyTracker.getInstance(this).send(MapBuilder.createTiming("service_line_detection", (System.currentTimeMillis() - sldRequestTimestamp), "sld_request_time", null).build());
		updateServiceLine(dataHandler.getCurrentLineModel());
		try{
			sendAnnotation("{\"request\": \"" + dataHandler.getSLDRequest() + "\", \"response\": " + dataHandler.getSLDResponse()+ "}");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateServiceLine(LineModel line)
	{
		TextView tv = (TextView) findViewById(R.id.recording_service_line_text);
		tv.setText(line.shortName);
		ImageView iv = (ImageView) findViewById(R.id.recording_service_line);
		switch(line.type)
		{
		case TRAM:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_tram));
			break;
		case BUS:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_bus_white));
			break;
		case FERRY:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_ferry));
			break;
		case RAIL:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_rail_white));
			break;
		case UKNOWN:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_unknown_white));
			break;
		default:
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_unknown_gray));
			break;
		}
		MapDrawing lineDrawing = dataHandler.getCurrentLineDrawing();
		//TODO: potential bug drawing is updated here and in "onLineReceived" at the same time, when user selects sld manually
		if(lineDrawing!= null && lineDrawing.poly != null)
		{
			if(serviceLineLayer != null)
				serviceLineLayer.remove();
			if(jamMarker != null){
				jamMarker.remove();
				jamMarker = null;
			}
			if(detectedJamSection != null)
			{
				detectedJamSection.setVisible(trafficSidebarTogglePressed);
			}
			serviceLineLayer = mGoogleMap.addPolyline(new PolylineOptions().addAll(lineDrawing.poly).color(lineDrawing.getColor()).zIndex(lineDrawing.getZIndex()).width(polyWidth));
			serviceLineLayer.setVisible(personalizedSidebarTogglePressed);
		}
	}
	
    @Override
    public void recreate()
    {
    	getIntent().putExtra(STARTED_FROM_RECORDING_NOTIFICATION, false);
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            super.recreate();
        }
        else
        {
            startActivity(getIntent());
            finish();
        }
    }
	private void onPersReceived()
	{
		Log.i("MapActivity", "pers received");
		dataHandler.setCurrentRoute(sensorService.getCurrentRoute());
		ArrayList<LatLng> currentRouteList = dataHandler.getCurrentRoute();
		if(userRoute != null)
			userRoute.remove();
		if(recordingStartPosition == null){
			recordingStartPosition = currentRouteList.get(0);
			if(startPositionMarker == null)
				startPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStartPosition).icon(markerIcon));
		}
		recordingStopPosition = currentRouteList.get(currentRouteList.size() - 1);
		if(stopPositionMarker == null)
			stopPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStopPosition).icon(markerStopIcon));
		else
		{
			stopPositionMarker.setPosition(recordingStopPosition);
		}
		MapDrawing currentRoute = dataHandler.getCurrentRouteDrawing();
		userRoute = mGoogleMap.addPolyline(new PolylineOptions().addAll(currentRoute.poly).color(currentRoute.getColor()).zIndex(currentRoute.getZIndex()).width(polyWidth));
		userRoute.setVisible(personalizedSidebarTogglePressed);
		if(dataHandler.getJammedStopIndex() != null){
			detectedJamSection = jammedSectionsLayer.get(dataHandler.getJammedStopIndex());
			detectedJamSection.setVisible(personalizedSidebarTogglePressed);
		}
	}
	
	private void onJamDetected()
	{
		Log.i("MapActivity", "jam detected");
		try{
			Log.i("MapActivity", "on jam detected: size = " + jammedSectionsLayer.size());
			Log.i("MapActivity", "on jam detected: 1 " +  jammedSectionsLayer.get(dataHandler.getJammedStopIndex()));
			Log.i("MapActivity", "on jam detected: 2 ");
			Log.i("MapActivity", "on jam detected: 3 index = " + dataHandler.getJammedStopIndex());
//			jammedSectionsLayer.get(dataHandler.getJammedStopIndex()).setVisible(true);
			detectedJamSection = jammedSectionsLayer.get(dataHandler.getJammedStopIndex());
			detectedJamSection.setVisible(personalizedSidebarTogglePressed);
			if(jamMarker == null)
				jamMarker = mGoogleMap.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_alert_2))
					.position(new LatLng(dataHandler.getJammedStop().latitude, dataHandler.getJammedStop().longitude))
					.draggable(false)
					.title(" Delay: " +  dataHandler.getJammedStopDelay().intValue() + "s" ));
		}
		catch(NullPointerException e)
		{
			Log.i("MapActivity", "on jam detected : null pointer exception");
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			Log.i("MapActivity", "on jam detected : array index out of bounds");
		}
		if(dataHandler.lineModelHasChanged)
			Log.i("MapActivity", "line model has changed");
		alertHasBeenDismissed = dataHandler.lineModelHasChanged ? false : alertHasBeenDismissed;
		if(alertHasBeenDismissed) return;
		LineModel currentLine = dataHandler.getCurrentLineModel();
		String description;
		if(currentLine.direction == 1)
			description = "Traffic jam detected in line " + currentLine.shortName + " towards " + currentLine.stopName;
		else
			description = "Traffic jam detected in line " + currentLine.shortName + " towards " + currentLine.startName;
		try {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("traffic_jam_detector", "jam_detected", currentLine.shortName, null).build());
		}
		catch (Exception e){}
		presentAlert("Traffic Jam", description);
		
	}
	
	private void onLineReceived()
	{
		Log.i("MapActivity", "line received");
		MapDrawing lineDrawing = dataHandler.getCurrentLineDrawing();
		if(lineDrawing!= null && lineDrawing.poly != null)
		{
			if(serviceLineLayer != null)
				serviceLineLayer.remove();
			if(jamMarker != null)
			{
				jamMarker.remove();
				jamMarker = null;
			}
			if(detectedJamSection != null)
			{
				detectedJamSection.setVisible(trafficSidebarTogglePressed);
			}
			serviceLineLayer = mGoogleMap.addPolyline(new PolylineOptions().addAll(lineDrawing.poly).color(lineDrawing.getColor()).zIndex(lineDrawing.getZIndex()).width(polyWidth));
			serviceLineLayer.setVisible(personalizedSidebarTogglePressed);
		}
	}
	
	//Activity Sensor Collector overriden methods
	@Override
	public void startRecording() {
		if (isRecording) {
			mNotificationManager.cancel(recordingNotificationID);
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("sensor_collector", "action", "stop_recording", null).build());
			if(recordingStartedTimestamp != 0)
				EasyTracker.getInstance(this).send(MapBuilder.createTiming("sensor_collector", (System.currentTimeMillis() - recordingStartedTimestamp), "recording_time", null).build());
			if(recordingvsotrecordingTimestamp != 0)
			{
				Log.i("MapActivity", "timestamp not 0");
				EasyTracker.getInstance(this).send(MapBuilder.createTiming("view_time", (System.currentTimeMillis() - recordingvsotrecordingTimestamp), "recording_view_time", null).build());
				EasyTracker.getInstance(this).send(MapBuilder.createEvent("traffic_jam_detector", "recording_view_time", null, (System.currentTimeMillis() - recordingvsotrecordingTimestamp)).build());
				recordingvsotrecordingTimestamp = System.currentTimeMillis();
			}
		} else {
			checkForGps();
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("sensor_collector", "action", "start_recording", null).build());
			recordingStartedTimestamp = 0;
			showRecordingNotification();
			if(recordingvsotrecordingTimestamp != 0)
			{
				Log.i("MapActivity", "timestamp not 0");
				EasyTracker.getInstance(this).send(MapBuilder.createTiming("view_time", (System.currentTimeMillis() - recordingvsotrecordingTimestamp), "non_recording_view_time", null).build());
				EasyTracker.getInstance(this).send(MapBuilder.createEvent("traffic_jam_detector", "non_recording_view_time", null, (System.currentTimeMillis() - recordingvsotrecordingTimestamp)).build());
				recordingvsotrecordingTimestamp = System.currentTimeMillis();
			}
		}
		super.startRecording();
	}
	
	@Override
	protected void startHAR() {
		if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_ACTIVITYRECOGNITION, "0").equals("1"))
			return;
		if(isHAR)
			EasyTracker.getInstance(this).send(MapBuilder.createEvent(" ", "action", "start_har", null).build());
		else
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("sensor_collector", "action", "stop_har", null).build());
		super.startHAR();
	}
	
	@Override
	protected void sendAnnotation(String annotation) {
		EasyTracker.getInstance(this).send(MapBuilder.createEvent("sensor_collector", "action", "send_annotation", null).build());
		super.sendAnnotation(annotation);
	}
	

//	@Override
//	protected void updateActivity(String activityName) {
//		super.updateActivity(activityName);
//		Log.i("MapActivity", "updating activity : " + activityName);
//		ImageView activityView = (ImageView) findViewById(R.id.recording_activity);
//		if (activityName.equals("running")) {
//			activityView.setImageResource(R.drawable.ic_running);
//			activityView.setVisibility(View.VISIBLE);
//		} else if (activityName.equals("walking")) {
//			activityView.setImageResource(R.drawable.ic_walking);
//			activityView.setVisibility(View.VISIBLE);
//		} else if (activityName.equals("sitting")) {
//			activityView.setImageResource(R.drawable.ic_sitting);
//			activityView.setVisibility(View.VISIBLE);
//		} else if (activityName.equals("cycling")) {
//			activityView.setImageResource(R.drawable.ic_bicycle);
//			activityView.setVisibility(View.VISIBLE);
//		} else if (activityName.equals("standing")) {
//			activityView.setImageResource(R.drawable.ic_standing);
//			activityView.setVisibility(View.VISIBLE);
//		} else if (activityName.equals("driving")) {
//			activityView.setImageResource(R.drawable.ic_car);
//			activityView.setVisibility(View.VISIBLE);
//		} else {
//			activityView.setImageResource(R.drawable.ic_unknown_white);
//			activityView.setVisibility(View.VISIBLE);
//		}
//	}
	
	@Override
	protected void updateActivity(String activityName) {
		super.updateActivity(activityName);
		Log.i("MapActivity", "updating activity : " + activityName);
		ImageView activityView = (ImageView) findViewById(R.id.recording_activity);
		if (activityName.equals("running")) {
			activityView.setImageResource(R.drawable.ic_running);
			activityView.setVisibility(View.VISIBLE);
		} else if(activityName.equals("walking")) {
			activityView.setImageResource(R.drawable.ic_walking);
			activityView.setVisibility(View.VISIBLE);
		} else if(activityName.equals("sitting")) {
			activityView.setImageResource(R.drawable.ic_sitting);
			activityView.setVisibility(View.VISIBLE);
		} else if(activityName.equals("standing")) {
			activityView.setImageResource(R.drawable.ic_standing);
			activityView.setVisibility(View.VISIBLE);
		} else if(activityName.equals("on table")) {
			activityView.setImageResource(R.drawable.ic_ontable);
			activityView.setVisibility(View.VISIBLE);
		} else {
			activityView.setImageResource(R.drawable.ic_unknown_white);
			activityView.setVisibility(View.VISIBLE);
		}
	}
	public void goToHelsinki(View v)
	{
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
				new LatLng(60.16, 24.96), 13);
		if (mGoogleMap == null) {
			return;
		} else {
			mGoogleMap.animateCamera(cameraUpdate);
		}
	}
	@Override
	protected void updateStatus(Intent intent) {
		super.updateStatus(intent);
		if (isRecording) {
			state = RecordingState.RECORDING;
			if(automaticActivityRecognitionRunning && (!isHAR))
				startHAR();
//			else if (!automaticActivityRecognitionRunning && isHAR)
//				startHAR();
		} else if (isTransferring) {
			state = RecordingState.TRANSFERING;
		} else {

			mNotificationManager.cancel(recordingNotificationID);
			state = RecordingState.IDLE;
		}
		setTopButtonState(state);
	}

	@Override
	protected void onGpsSamplesReceived(String gpsSamples) {
		super.onGpsSamplesReceived(gpsSamples);
		dataHandler.setGpsSamples(gpsSamples);
	}

	@Override
	protected void onGpsSampleReceived(String gpsSample) {
		super.onGpsSampleReceived(gpsSample);
	}

	private void showRecordingNotification() {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_stat_logo)
				.setContentTitle("Recording")
				.setContentText("Your current route is being recorded")
				.setUsesChronometer(true); 
		Intent intent = new Intent(this, MapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
	            intent, 0);
	    mBuilder.setContentIntent(pendingIntent);
	    
		mBuilder.setOngoing(true);
		mNotificationManager.notify(recordingNotificationID, mBuilder.build());
	}
	
	private void showTrafficJamNotification(String title, String description) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_stat_logo)
				.setContentTitle(title)
				.setWhen(System.currentTimeMillis())
				.setContentText(description);
		Intent resultIntent = new Intent(this, MapActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MapActivity.class);
		resultIntent.putExtra("ALERT_ACTIVE", true);
		resultIntent.putExtra("ALERT_TITLE", title);
		resultIntent.putExtra("ALERT_DESCRIPTION", description);
		CameraPosition cameraPosition = mMapfragment.getMap().getCameraPosition();
		Bundle gMapsCameraPosition = new Bundle();
		gMapsCameraPosition.putDouble("LATITUDE",
				cameraPosition.target.latitude);
		gMapsCameraPosition.putDouble("LONGITUDE",
				cameraPosition.target.longitude);
		gMapsCameraPosition.putFloat("ZOOM", cameraPosition.zoom);
		resultIntent.putExtra("GMAPS_CAMERA_POSITION", gMapsCameraPosition);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setOngoing(false);
		mNotificationManager.notify(alertNotificationID, mBuilder.build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		// mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_main);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        	setContentView(R.layout.activity_main);         
        }
		super.onConfigurationChanged(newConfig);
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.recording_imageview || v.getId() == R.id.recording_textview) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_top_button", null).build());
			switch (state) {
			case IDLE:
				if (!isRecording){
					automaticSLDRunning = true;
					killSLD = false;
					mHandler.post(SLDUpdates);
					updateServiceLine(LineModel.getInvalid());
					if(automaticSLDRunning)
					{
						mHandler.post(SLDUpdates);
					}
					killPer = false;
					killSLD = false;
					mHandler.post(persUpdates);
					((MobilityApplication) getApplication()).dHandler.setCurrentRoute(sensorService.getCurrentRoute());
					if(!personalizedSidebarTogglePressed)
						togglePersTrafficLayer();
					startRecording();
				}
				if (!isHAR){
					startHAR();
					automaticActivityRecognitionRunning = true;
				}
				break;
			case RECORDING:
				if (isRecording)
				{
					killPer = true;
					killSLD = true;
					mHandler.removeCallbacks(SLDUpdates);
					mHandler.removeCallbacks(persUpdates);
					if(startPositionMarker != null){
						startPositionMarker.remove();
						startPositionMarker = null;
					}
					if(stopPositionMarker != null) {
						stopPositionMarker.remove();
						stopPositionMarker = null;
					}
					if(recordingStartPosition != null)
					{
						recordingStartPosition = null;
					}
					if(recordingStopPosition != null)
					{
						recordingStopPosition = null;
					}
					if(serviceLineLayer != null)
					{
						serviceLineLayer.remove();
						serviceLineLayer = null;
					}
					if(userRoute != null)
					{
						userRoute.remove();
						userRoute = null;
					}
					
					if(personalizedSidebarTogglePressed)
						togglePersTrafficLayer();
					startRecording();
				}
				stopHAR();
				break;
			}
		} else if (v.getId() == R.id.recording_service_line_container){
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_top_service_line", null).build());
			lineSearchDialog.show();
		} else if (v.getId() == R.id.recording_activity_container){
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_top_activity", null).build());
			activitySelectDialog.show();
		} else if (v.getId() == R.id.drawerButton) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_drawer", null).build());
			mDrawerLayout.openDrawer(mDrawerSideBarLayout);
		} else if (v.getId() == R.id.alert_close_button) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_traffic_alert_dismiss", null).build());
			alertHasBeenDismissed = true;
			dataHandler.lineModelHasChanged = false;
			FrameLayout vg = (FrameLayout) findViewById(R.id.content_layout);
			AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
			animation1.setDuration(1000);
			animation1.setFillAfter(true);
			currentAlertView.startAnimation(animation1);
			vg.removeView(currentAlertView);
			mNotificationManager.cancel(alertNotificationID);
			currentAlertView = null;
		} else if (v.getId() == R.id.all_traffic_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_all_traffic_toggle", null).build());
			toggleGenTrafficLayer();
		} else if (v.getId() == R.id.pers_traffic_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_personalized_toggle", null).build());
			if(isRecording)
				togglePersTrafficLayer();
		} else if (v.getId() == R.id.sattelite_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_satellite_toggle", null).build());
			if (satteliteSidebarTogglePressed) {
				satteliteSidebarTogglePressed = false;
				v.setBackgroundColor(getResources().getColor(
						android.R.color.transparent));
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			} else {
				satteliteSidebarTogglePressed = true;
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.toggle_button_sidebar));
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			}
		} else if (v.getId() == R.id.recordings_intent_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_recordings", null).build());
			Intent intent = new Intent(MapActivity.this,
					RecordingsActivity.class);
			MapActivity.this.startActivity(intent);
		} else if (v.getId() == R.id.alerts_intent_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_alerts", null).build());
			Intent intent = new Intent(MapActivity.this, AlertsActivity.class);
			MapActivity.this.startActivity(intent);
		} else if (v.getId() == R.id.report_intent_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_reports", null).build());
			Bundle bundle = new Bundle();
			bundle.putInt("userId", 1);
			bundle.putBoolean("ir_client_newreport_photo", true);
			Intent intent = new Intent(MapActivity.this, ReportActivity.class);
			intent.putExtras(bundle);
			MapActivity.this.startActivity(intent);
		} else if (v.getId() == R.id.settings_intent_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_settings", null).build());
			Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
			MapActivity.this.startActivity(intent);
		} else if (v.getId() == R.id.about_intent_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sidebar_about", null).build());
			Intent intent = new Intent(MapActivity.this, AboutActivity.class);
			MapActivity.this.startActivity(intent);
		} else if (v.getId() == R.id.my_location_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_my_location", null).build());
			Location lastLoc = mLocationClient.getLastLocation();
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude())));
		} else if (v.getId() == R.id.refresh_btn) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "map_sync_button", null).build());
			recreate();
		} else if (v.getId() == R.id.alert_route_planner) {
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "route_planner_link", null).build());
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://beta.reittiopas.fi/"));
			startActivity(browserIntent);
			alertHasBeenDismissed = true;
			dataHandler.lineModelHasChanged = false;
			FrameLayout vg = (FrameLayout) findViewById(R.id.content_layout);
			AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
			animation1.setDuration(1000);
			animation1.setFillAfter(true);
			currentAlertView.startAnimation(animation1);
			vg.removeView(currentAlertView);
			mNotificationManager.cancel(alertNotificationID);
			currentAlertView = null;
		} else if (v.getId() == R.id.ar_btn) {
			startActivity(new Intent(this, ARActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//			if(!isOnAR){
//				if(arFragment == null)
//				{//TODO: disable button until stops are stored
//					if(dataHandler.getStopsStored() == true)
//					{
//						List<StaticStopModel> stops = dataHandler.getNearestStops(ctx, new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()));
//						ArrayList<Entity> entitiesLBS = new ArrayList<Entity>(stops.size());
//						for(StaticStopModel stop : stops)
//						{
//							Location stopLocation = new Location("");
//							stopLocation.setLatitude(stop.coord.latitude);
//							stopLocation.setLongitude(stop.coord.longitude);
//							entitiesLBS.add(new Entity(Integer.toString(stop.id), stop.name, "", "", stopLocation, "", "", "", "", "", ""));
//						}
//					
//					//	String id, title, iconurl, iconfile, Location location, String description, modelurl, modelfile, type, trackingurl, nModels3d						
//						arFragment = new Fragment_AR_Mobility(entitiesLBS, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
//					}
//				}
//				android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
////				ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
//				arFragment.setRetainInstance(true);
//				ft.add(R.id.ar_container_view, arFragment);
//				ft.commit();
//				isOnAR = true;
//			}
			
//		} else if (v.getId() == R.id.report_overview_btn) {
//			Bundle bundle = new Bundle();
//			bundle.putInt("userId", 1);
//			bundle.putBoolean("ir_client_newreport_photo", true);
//			Intent intent = new Intent(MapActivity.this, ReportOverviewActivity.class);
//			intent.putExtras(bundle);
//			MapActivity.this.startActivity(intent);
		}
	}
	public void presentAlert(String title, String description)
	{
		if(currentAlertView == null)
		{
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "popup", "traffic_alert_shown", null).build());
			currentAlertView = getLayoutInflater().inflate(R.layout.alert_layout, null);
			FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lparams.setMargins(20, 150, 20, 20);
			currentAlertView.setLayoutParams(lparams);
			TextView titleView = (TextView) currentAlertView.findViewById(R.id.alert_title);
			titleView.setText(title);
			TextView descriptionView = (TextView) currentAlertView.findViewById(R.id.alert_description);
			descriptionView.setText(description);
			FrameLayout vg = (FrameLayout) findViewById(R.id.content_layout);
			vg.addView(currentAlertView, lparams);
			AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
			animation.setDuration(1000);
			animation.setFillAfter(true);
			currentAlertView.startAnimation(animation);
			ScaleAnimation scaleAnimation = new ScaleAnimation(0.4f, 1.0f, 0.4f, 1.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(300);
			animation.setFillAfter(true);
			currentAlertView.startAnimation(scaleAnimation);
			currentAlertView.findViewById(R.id.alert_close_button).setOnClickListener(this);
			TextView tv = (TextView) currentAlertView.findViewById(R.id.alert_route_planner);
			tv.setOnClickListener(this);
		}
	}
	
	public boolean shouldPresentAlert()
	{
		return true;
	}
	private void setTopButtonState(RecordingState state) {
		mTopView.setVisibility(View.VISIBLE);
		switch (state) {
		case IDLE:
			mTopView.setBackgroundDrawable(topStartRecordingState);
			TextView tv1 = (TextView) mTopView
					.findViewById(R.id.recording_textview);
			tv1.setText("Start Recording");
			tv1.setTextColor(getResources().getColor(android.R.color.black));
			ImageView iv1 = (ImageView) mTopView
					.findViewById(R.id.recording_imageview);
			iv1.setImageDrawable(circle);
			ImageView iv_line = (ImageView) mTopView
					.findViewById(R.id.recording_service_line);
			iv_line.setVisibility(View.GONE);
			ImageView iv_activity = (ImageView) mTopView
					.findViewById(R.id.recording_activity);
			iv_activity.setVisibility(View.GONE);
			LinearLayout ll1 = (LinearLayout) findViewById(R.id.pers_traffic_btn);
			personalizedSidebarTogglePressed = false;
			ll1.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));

			View v1 = (View) findViewById(R.id.recording_separator);
			v1.setVisibility(View.GONE);
			TextView tv3 = (TextView) findViewById(R.id.recording_service_line_text);
			tv3.setVisibility(View.GONE);
			break;
		case RECORDING:
			mTopView.setBackgroundDrawable(topOnRecordingState);
			TextView tv = (TextView) mTopView
					.findViewById(R.id.recording_textview);
			tv.setText("Stop Recording");
			tv.setTextColor(getResources().getColor(android.R.color.white));
			ImageView iv = (ImageView) mTopView
					.findViewById(R.id.recording_imageview);
			iv.setImageDrawable(rect);
			ImageView iv_line1 = (ImageView) mTopView
					.findViewById(R.id.recording_service_line);
			iv_line1.setVisibility(View.VISIBLE);
			LinearLayout ll = (LinearLayout) findViewById(R.id.pers_traffic_btn);
			View v = (View) findViewById(R.id.recording_separator);
			v.setVisibility(View.VISIBLE);
			TextView tv2 = (TextView) findViewById(R.id.recording_service_line_text);
			tv2.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		dataHandler.lineModelHasChanged = true;
		alertHasBeenDismissed = false;
		CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
		previousCameraPosition = new Bundle();
		previousCameraPosition.putDouble(PREVIOUS_LATITUDE, cameraPosition.target.latitude);
		previousCameraPosition.putDouble(PREVIOUS_LONGITUDE, cameraPosition.target.longitude);
		previousCameraPosition.putFloat(PREVIOUS_ZOOM, cameraPosition.zoom);
		outState.putBundle(PREVIOUS_CAMERA_POSITION_BUNDLE, previousCameraPosition);
			
		outState.putBoolean(TRAFFIC_SIDEBAR_TOGGLE_PRESSED, trafficSidebarTogglePressed);
		outState.putBoolean(PERSONALIZED_SIDEBAR_TOGGLE_PRESSED, personalizedSidebarTogglePressed);
		outState.putBoolean(SATTELITE_SIDEBAR_TOGGLE_PRESSED, satteliteSidebarTogglePressed);
		outState.putBoolean(DISMISSED_TRAFFIC_NOTIFICATION, dismissedTrafficNotification);
		outState.putBoolean(AUTOMATIC_ACTIVITY_RECOGNITION_RUNNING, automaticActivityRecognitionRunning);
		outState.putInt(SELECTED_ACTIVITY_RECOGNITION_INDEX, selectedActivityRecognitionIndex);
		outState.putBoolean(AUTOMATIC_SLD_RUNNING, automaticSLDRunning);
		outState.putString(SELECTED_LINE_ID, selectedLineID);
		outState.putBoolean(RECORDING_STARTED_AND_CAMERA_MOVED, recordingStartedAndCameraMoved);
		outState.putBoolean(SHOULD_GET_LOCATION_FOR_FIRST_TIME, shouldGetLocationForFirstTime);
		outState.putBoolean(ALERT_HAS_BEEN_DISMISSED, alertHasBeenDismissed);
		
		timingVariables = new Bundle();
		timingVariables.putLong(PERSONALIZED_VIEW_START_TIME, personalizedToggleOnTimestamp);
		timingVariables.putLong(RECORDING_STARTED_TIMESTAMP, recordingStartedTimestamp);
		timingVariables.putLong(RECORDING_VS_NOT_RECORDING_TIMESTAMP, recordingvsotrecordingTimestamp);
		outState.putBundle(TIMING_VARIABLES_BUNDLE, timingVariables);
	}
	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		mLocationClient.requestLocationUpdates(REQUEST, this);
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean checkForGps()
	{
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Gps not enabled");
			alert.setCancelable(false);
			alert.setMessage("The GPS is not enabled. The accuracy of the recorded route may be inaccurate.\n\nTurn on gps now?");
			alert.setPositiveButton("GPS Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			});
			alert.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					
				}
			});
			alert.show();
			return false;
		}
		return true;
	}
	@Override
	public void onLocationChanged(Location location) {
		//TODO: add state when to go to my location
		lastLoc = location;
		try{
			ImageButton arBtn = (ImageButton) findViewById(R.id.ar_btn);
			arBtn.setImageResource(R.drawable.ic_arbuttonenabled5);
			arBtn.setEnabled(true);
		}
		catch(NullPointerException e)
		{
			Log.w("MapActivity", "np locationchanged");
		}
		Log.i("MapActivity", "last location = " + lastLoc.getLatitude() + lastLoc.getLongitude());
		if(myLocationMarker == null)
		{
			myLocationMarker = mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_dot_blue)).position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(false));
		}
		else
		{
			myLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
		}
//		if(myLocationAccuracyCircle == null)
//		{
//			myLocationAccuracyCircle = mGoogleMap.addCircle(new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude())).radius(location.getAccuracy()).strokeWidth(0.0f).fillColor(Color.argb(50, 0, 0, 110)));
//		}
//		else{
//			myLocationAccuracyCircle.setRadius(location.getAccuracy());
//			myLocationAccuracyCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
//		}
		myLocationMarker.setVisible(!personalizedSidebarTogglePressed);
//		myLocationAccuracyCircle.setVisible(!personalizedSidebarTogglePressed);
		if(shouldGetLocationForFirstTime){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13.0f));
			shouldGetLocationForFirstTime = false;
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, "Location services are not enabled, application will not work properly", Toast.LENGTH_LONG).show();
		
	}
	@Override
	public void onDrawerClosed(View arg0) {
		arg0.invalidate();
		mMapfragment.getView().setVisibility(View.VISIBLE);
	}
	@Override
	public void onDrawerOpened(View arg0) {
	     mDrawerLayout.bringChildToFront(arg0);
	     mDrawerLayout.requestLayout();
	     mDrawerLayout.setScrimColor(Color.TRANSPARENT);
		
	}
	@Override
	public void onDrawerSlide(View arg0, float arg1) {
		mDrawerLayout.bringChildToFront(arg0);
		
	}
	@Override
	public void onDrawerStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	// -------------------- Utils --------------------
	//TODO: disable sidebar toggle buttons when an asynchronous task is running
	private void toggleGenTrafficLayer()
	{
		View v = findViewById(R.id.all_traffic_btn);
		if (trafficSidebarTogglePressed) {
			v.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
		} else {
			v.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.toggle_button_sidebar));
		}
		trafficSidebarTogglePressed = !trafficSidebarTogglePressed;
//		if(trafficJamLayer == null) return;
//		for(Polyline poly : trafficJamLayer)
//			poly.setVisible(trafficSidebarTogglePressed);
		if(tramRoutesLayer != null)
			for(Polyline poly : tramRoutesLayer)
				poly.setVisible(trafficSidebarTogglePressed);
		if(jammedSectionsLayer != null)
			for(Polyline poly : jammedSectionsLayer)
				poly.setVisible(trafficSidebarTogglePressed);
	}
	
	@SuppressLint("NewApi")
	private void permissionCheck()
	{
		if(!((userPrefs.getString(ServiceCentreService.IR_API, "0").equals("1")) && (userPrefs.getString(ServiceCentreService.MOB_CLIENT_ISSUEREPORTING, "0").equals("1"))))
		{
			Button reportButton = (Button) findViewById(R.id.report_intent_btn);
			reportButton.setVisibility(View.GONE);
		}
		if(!((userPrefs.getString(ServiceCentreService.MOB_CLIENT_GETALERTS, "0").equals("1")) && (userPrefs.getString(ServiceCentreService.MOB_CLIENT_GETISSUES, "0").equals("1")))) 
		{
			Button alertButton = (Button) findViewById(R.id.alerts_intent_btn);
			alertButton.setVisibility(View.GONE);
		}
		if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_CREATERECORDING, "0").equals("1"))
		{
			Button recordingsButton = (Button) findViewById(R.id.recordings_intent_btn);
			recordingsButton.setVisibility(View.GONE);
			ImageView iv = (ImageView) findViewById(R.id.recording_imageview);
			iv.setOnClickListener(null);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				iv.setAlpha(0.3f);
			}
			else
			{
				iv.setAlpha(50);
			}
			TextView tv = (TextView) findViewById(R.id.recording_textview);
			tv.setOnClickListener(null);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				tv.setAlpha(0.3f);
			}
		}
		if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_ACTIVITYRECOGNITION, "0").equals("1"))
		{
			LinearLayout activityContainer = (LinearLayout) findViewById(R.id.recording_activity_container);
			activityContainer.setOnClickListener(null);
		}
			
	}
	private void togglePersTrafficLayer()
	{
		View v = findViewById(R.id.pers_traffic_btn);
		if(personalizedSidebarTogglePressed)
		{
			if(personalizedToggleOnTimestamp != 0){
				EasyTracker.getInstance(this).send(MapBuilder.createTiming("view_time", (System.currentTimeMillis() - personalizedToggleOnTimestamp), "personalized_view_time", "disabled_by_toggle_button").build());
				personalizedToggleOnTimestamp = 0;
			}
			v.setBackgroundColor(getResources().getColor(
				android.R.color.transparent));
		} else {
			personalizedToggleOnTimestamp = System.currentTimeMillis();
			v.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.toggle_button_sidebar));	
		}
		personalizedSidebarTogglePressed = !personalizedSidebarTogglePressed;
		if(userRoute!= null)
			userRoute.setVisible(personalizedSidebarTogglePressed);
		if(serviceLineLayer !=null)
			serviceLineLayer.setVisible(personalizedSidebarTogglePressed);
		if(startPositionMarker != null){
			startPositionMarker.setVisible(personalizedSidebarTogglePressed);
		}
		if(stopPositionMarker != null) {
			stopPositionMarker.setVisible(personalizedSidebarTogglePressed);
		}
		if(myLocationAccuracyCircle != null){
			myLocationAccuracyCircle.setVisible(!personalizedSidebarTogglePressed);
		}
		if(myLocationMarker != null) {
			myLocationMarker.setVisible(!personalizedSidebarTogglePressed);
		}
		if(detectedJamSection != null){
			detectedJamSection.setVisible(personalizedSidebarTogglePressed);
		}
		if(jamMarker != null){
			jamMarker.setVisible(personalizedSidebarTogglePressed);
		}
//		if(jammedSectionsLayer != null)
//			for(Polyline poly : jammedSectionsLayer)
//				poly.setVisible(trafficSidebarTogglePressed);
	}
	
	@SuppressLint("NewApi")
	private void setupViews()
	{
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerListener(this);
		mDrawerSideBarLayout = (ScrollView) findViewById(R.id.left_drawer);
		mDrawerLayout.bringChildToFront(mDrawerSideBarLayout);
	    mDrawerLayout.requestLayout();
	    //set transparent scrim color to support some older devices
	    mDrawerLayout.setScrimColor(Color.TRANSPARENT);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		ImageButton ib = (ImageButton) findViewById(R.id.drawerButton);
		ib.setOnClickListener(this);
		mTopView = (LinearLayout) findViewById(R.id.topView);
		if (topStartRecordingState == null)
			topStartRecordingState = getResources().getDrawable(
					R.drawable.recording_graphic);
		if (topOnRecordingCompletedState == null)
			topOnRecordingCompletedState = getResources().getDrawable(
					R.drawable.graphic_recording_blue);
		if (topOnRecordingState == null)
			topOnRecordingState = getResources().getDrawable(
					R.drawable.graphic_recording_red);
		if (circle == null)
			circle = getResources().getDrawable(R.drawable.ic_recording_circle);
		if (rect == null)
			rect = getResources().getDrawable(R.drawable.ic_stop_recording);
		if (upload == null)
			upload = getResources().getDrawable(R.drawable.ic_upload_recording);
		LinearLayout layout = (LinearLayout) findViewById(R.id.all_traffic_btn);
		if (trafficSidebarTogglePressed)
			layout.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.toggle_button_sidebar));
		else
			layout.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
		layout.setOnClickListener(this);
		layout = (LinearLayout) findViewById(R.id.pers_traffic_btn);
		if (personalizedSidebarTogglePressed)
			layout.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.toggle_button_sidebar));
		else
			layout.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
		layout.setOnClickListener(this);
		layout = (LinearLayout) findViewById(R.id.sattelite_btn);
		if (satteliteSidebarTogglePressed)
			layout.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.toggle_button_sidebar));
		else
			layout.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
		layout.setOnClickListener(this);
		Button button = (Button) findViewById(R.id.recordings_intent_btn);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.alerts_intent_btn);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.report_intent_btn);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.settings_intent_btn);
		button.setOnClickListener(this);
		button = (Button) findViewById(R.id.about_intent_btn);
		button.setOnClickListener(this);
		ImageButton b = (ImageButton) findViewById(R.id.my_location_btn);
		b.setOnClickListener(this);
		b = (ImageButton) findViewById(R.id.ar_btn);
		b.setOnClickListener(this);
		b = (ImageButton) findViewById(R.id.refresh_btn);
		b.setOnClickListener(this);
		ImageView iv = (ImageView) findViewById(R.id.recording_imageview);
		iv.setOnClickListener(this);
		TextView tv = (TextView) findViewById(R.id.recording_textview);
		tv.setOnClickListener(this);
		
		LinearLayout activityLayout = (LinearLayout) findViewById(R.id.recording_activity_container);
		activityLayout.setOnClickListener(this);
		LinearLayout serviceLineLayout = (LinearLayout) findViewById(R.id.recording_service_line_container);
		serviceLineLayout.setOnClickListener(this);
		if(activitySelectDialog == null)
		{
			activitySelectDialog = new Dialog(this);
			activitySelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			activitySelectDialog.setContentView(R.layout.activity_select_dialog);
			final ListView list = (ListView) activitySelectDialog.findViewById(R.id.activity_select_dialog_listview);
			list.setAdapter(new HARAdapter(this, R.layout.activity_list_item, new Drawable[]{getResources().getDrawable(R.drawable.ic_unknown_gray),getResources().getDrawable(R.drawable.ic_running_gray),getResources().getDrawable(R.drawable.ic_walking_gray),getResources().getDrawable(R.drawable.ic_sitting_gray),getResources().getDrawable(R.drawable.ic_standing_gray),getResources().getDrawable(R.drawable.ic_ontable_gray)}));
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					selectedActivityRecognitionIndex = arg2;
					if(arg2 == 0)
					{
							automaticActivityRecognitionRunning = true;
							sendAnnotation("Re-enabled activity recognition");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "auto_har", null).build());
							startHAR();
					}
					else
					{
						automaticActivityRecognitionRunning = false;
						stopHAR();
						switch(arg2)
						{
						case 1:
							updateActivity("running");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_sitting", null).build());
							sendAnnotation("Selected activity : running");
							break;
						case 2:
							updateActivity("walking");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_standing", null).build());
							sendAnnotation("Selected activity : walking");
							break;
						case 3:
							updateActivity("sitting");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_walking", null).build());
							sendAnnotation("Selected activity : sitting");
							break;
						case 4:
							updateActivity("standing");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_running", null).build());
							sendAnnotation("Selected activity : standing");
							break;
						case 5:
							updateActivity("on table");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_cycling", null).build());
							sendAnnotation("Selected activity : on table");
							break;
//						case 6:
//							updateActivity("driving");
//							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_driving", null).build());
//							sendAnnotation("Selected activity : driving");
//							break;
						default:
							updateActivity("unknown");
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_har_activity_unknown", null).build());
							sendAnnotation("Selected activity : unknown");
							break;
						}
					}
					activitySelectDialog.dismiss();
				}
			});
		}
		if(lineSearchDialog == null)
		{
			lineSearchDialog = new Dialog(this);
			lineSearchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			lineSearchDialog.setContentView(R.layout.line_search_dialog);
			final ListView linesDialogList = (ListView) lineSearchDialog.findViewById(R.id.line_search_dialog_listview);
			EditText searchField = (EditText) lineSearchDialog.findViewById(R.id.line_search_dialog_textview);
			if(dataHandler.getLines() == null)
				requestData(DataService.ACTION_GET_LINES);
			else
				linesDialogList.setAdapter(linesAdapter);
			linesDialogList.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					if(arg2 == 0)
					{
						sendAnnotation("Re-enabled auto line detection");
						EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "auto_sld", null).build());
						automaticSLDRunning = true;
						killSLD = false;
						mHandler.post(SLDUpdates);
						lineSearchDialog.dismiss();
						updateServiceLine(LineModel.getInvalid());
						selectedLineID = null;
					}
					else
					{
						final LineModel selectedModel = ((LinesAdapter) linesDialogList.getAdapter()).getModelOnFilteredPosition(arg2);
						EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "selected_line_id", null).build());
						try {
							EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("service_line_detection", "manually_selected_line_id", selectedModel.id, null).build());
						}
						catch(Exception e){}

						lineSearchDialog.dismiss();
						directionSelectDialog = new Dialog(ctx);
						directionSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						directionSelectDialog.setContentView(R.layout.direction_select_dialog);
						directionSelectDialog.show();
						RadioButton r1 = (RadioButton) directionSelectDialog.findViewById(R.id.direction_1);
						r1.setText(selectedModel.stopName);
						r1.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								automaticSLDRunning = false;
								mHandler.removeCallbacks(SLDUpdates);
								killSLD = true;
								dataHandler.setCurrentLineModel(selectedModel.setDirection(1));
								sendAnnotation("Manually selected line with id: " +  selectedModel.id + "direction : 1");
								requestData(DataService.ACTION_REQUEST_USER_SELECTED_LINE);
								updateServiceLine(selectedModel.setDirection(1));
								selectedLineID = selectedModel.id;
								directionSelectDialog.dismiss();
							}
						});
						RadioButton r2 = (RadioButton) directionSelectDialog.findViewById(R.id.direction_2);
						r2.setText(selectedModel.startName);
						r2.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								automaticSLDRunning = false;
								mHandler.removeCallbacks(SLDUpdates);
								killSLD = true;
								dataHandler.setCurrentLineModel(selectedModel.setDirection(2));
								sendAnnotation("Manually selected line with id: " +  selectedModel.id + "direction : 2");
								requestData(DataService.ACTION_REQUEST_USER_SELECTED_LINE);
								updateServiceLine(selectedModel.setDirection(2));
								selectedLineID = selectedModel.id;
								directionSelectDialog.dismiss();
							}
						});
					}
					alertHasBeenDismissed = false;
				}
			});
			searchField.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					try{
						linesAdapter.getFilter().filter(s);
					}
					catch(NullPointerException e)
					{
						EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("debugging", "onTextChanged nullpointerexception caught", null, null).build());
					}
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					
				}
			});
		}

		// 	Data
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().hide();
		}
		if(!automaticActivityRecognitionRunning)
		{
			switch(selectedActivityRecognitionIndex)
			{
				case 1:
					updateActivity("sitting");
					sendAnnotation("Selected activity : sitting");
					break;
				case 2:
					updateActivity("standing");
					sendAnnotation("Selected activity : standing");
					break;
				case 3:
					updateActivity("walking");
					sendAnnotation("Selected activity : walking");
					break;
				case 4:
					updateActivity("running");
					sendAnnotation("Selected activity : running");
					break;
				case 5:
					updateActivity("cycling");
					sendAnnotation("Selected activity : cycling");
					break;
				case 6:
					updateActivity("driving");
					sendAnnotation("Selected activity : driving");
					break;
				default:
					updateActivity("unknown");
					sendAnnotation("Selected activity : unknown");
					break;
			}
		}
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if(recordingStartPosition!= null && startPositionMarker == null)//TODO: More checks needed
		{
			startPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStartPosition).icon(markerIcon));
		}
		if(recordingStopPosition != null && stopPositionMarker ==  null)
		{
			stopPositionMarker = mGoogleMap.addMarker(new MarkerOptions().position(recordingStopPosition).icon(markerIcon));
		}
		if(selectedLineID != null && dataHandler.getLines()!= null)
		{
			updateServiceLine(LineModel.getLineModel(dataHandler.getLines(), selectedLineID).setDirection(1));
		}
		if(lastLoc == null)
		{
			ImageButton arBtn = (ImageButton) findViewById(R.id.ar_btn);
			arBtn.setImageResource(R.drawable.ic_arbuttondisabled5);
			arBtn.setEnabled(false);
		}
	}
	
	//should be called onCreate
	private void initUpdates()
	{
		_receiver = new BroadcastReceiver() {	
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals(DataService.ACTION_JAM_UPDATE)){
					onJamsReceived();
				} else if(action.equals(DataService.ACTION_LINES_UPDATE)){
					onLinesReceived();
				} else if (action.equals(DataService.ACTION_PERS_UPDATE)) {
					onPersReceived();
				} else if (action.equals(DataService.ACTION_JAM_DETECTED)) {
					onJamDetected();
				} else if (action.equals(DataService.ACTION_SLD_UPDATE)) {
					onSLDReceived();
				} else if (action.equals(DataService.ACTION_RETURN_USER_SELECTED_LINE)) {
					onLineReceived();
				} else if (action.equals(DataService.ACTION_STOPS_UPDATE)) { 
					onStopsReceived();
				}
			}
		};
		//register jam detection updates
		jamUpdates = new Runnable() {
			@Override
			public void run() {
				requestData(DataService.ACTION_GET_JAMS);
				if(killTraff) return;
				mHandler.postDelayed(this, TRAFFIC_UPDATE_INTERVAL);
			}
		};
		//register service line detection updates
		SLDUpdates = new Runnable() {
			@Override
			public void run() {
				requestData(DataService.ACTION_GET_SLD);
				requestGPSSamples();
				if(killSLD) return;
				mHandler.postDelayed(this, SERVICE_LINE_UPDATE_INTERVAL);
			}
		};
		//register personalized traffic updates
		persUpdates = new Runnable() {
			@Override
			public void run() {
				requestData(DataService.ACTION_GET_PERS);
				if(killPer) return;
				mHandler.postDelayed(this, PERSONALIZED_TRAFFIC_UPDATE_INTERVAL);
			}
		};
		//register status updates (from sensor collector)
		statusUpdates = new Runnable(){
			@Override
			public void run() {
				requestStatus();
				if(killTraff) return;
				mHandler.postDelayed(this, 4000);	
			}
		};
		
	}
	
	//should be called onResume()
	private void startUpdates()
	{
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DataService.ACTION_JAM_DETECTED);
		intentFilter.addAction(DataService.ACTION_JAM_UPDATE);
		intentFilter.addAction(DataService.ACTION_LINES_UPDATE);
		intentFilter.addAction(DataService.ACTION_SLD_UPDATE);
		intentFilter.addAction(DataService.ACTION_PERS_UPDATE);
		intentFilter.addAction(DataService.ACTION_RETURN_USER_SELECTED_LINE);
		intentFilter.addAction(DataService.ACTION_STOPS_UPDATE);
		registerReceiver(_receiver, intentFilter);
		mHandler.post(jamUpdates);
		mHandler.post(statusUpdates);
		if(automaticSLDRunning)
			mHandler.post(SLDUpdates);
		mHandler.post(persUpdates);
		mLocationClient.connect();
	}
	//shoudl be called onCreate()
	private void killUpdates()
	{
		unregisterReceiver(_receiver);
		killTraff = true;
		killPer = true;
		killSLD = true;
		mHandler.removeCallbacks(jamUpdates);
		mHandler.removeCallbacks(SLDUpdates);
		mHandler.removeCallbacks(persUpdates);
		mHandler.removeCallbacks(statusUpdates);
		mLocationClient.disconnect();
	}
}