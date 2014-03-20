package com.lg.mobility.data;

import java.io.File;
import java.security.PublicKey;

import com.lg.mobility.activities.AlertsActivity;

import nl.yucat.pushtestclient.service.GCMIntentService;

import android.app.Application;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import eu.liveandgov.wp1.sensor_collector.ServiceSensorControl;
import eu.liveandgov.wp1.sensor_collector.persistence.PublicationPipeline;

public class MobilityApplication extends Application {
	ServiceSensorControl sensorService;
	public DataHandler dHandler;
	
//	TrafficJamMapLayer tjLayer;
//	UserRouteMapLayer urLayer;
	@Override
	public void onCreate() {
//		tjLayer = new TrafficJamMapLayer();
//		urLayer = new UserRouteMapLayer();
		VehicleModel.cacheDir = getCacheDir();
		PublicationPipeline.publishFile = new File(getFilesDir(), "published.ssf");
		GCMIntentService.notificationReceiver = AlertsActivity.class;
		dHandler = new DataHandler();
		dHandler.setAndroidID(Secure.getString(getContentResolver(), Secure.ANDROID_ID));
		super.onCreate();
	}
	
//	public TrafficJamMapLayer getTrafficLayerInstance()
//	{
//		return tjLayer;
//	}
////	
//	public TrafficJamMapLayer getNewTrafficLayerInstance()
//	{
//		tjLayer = new TrafficJamMapLayer();
//		
//		return tjLayer;
//	}
}
