package com.lg.mobility.services;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.lg.mobility.data.MobilityApplication;
import com.lg.mobility.utilities.ServiceCentreUtils;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ServiceCentreService extends IntentService {

	public static final String USER_INFO_PREFS = "USER_INFO_PREFS";
	public static final String USER_INFO_TAG_USER_ID = "USER_INFO_TAG_USER_ID";
	public static final String USER_INFO_TAG_TJD_PERMISSION = "USER_INFO_TJD_PERMISSION";
	public static final String USER_INFO_TAG_IR_PERMISSION = "USER_INFO_IR_PERMISSION";
	public static final String ACTION_GET_ANON_USER_ID = "com.lg.mobility.intent.action.GET_ANON_USER_ID";
	public static final String ACTION_GET_PERMISSIONS_LIST = "com.lg.mobility.intent.action.GET_PERMISSIONS_LIST";
	
	public static final String ACTION_RETURN_USER_ID = "com.lg.mobility.intent.action.RETURN_USER_ID";
	public static final String ACTION_RETURN_USER_PERMISSIONS = "com.lg.mobility.intent.action.RETURN_USER_PERMISSIONS";
	
	public static final String IR_API = "ir_api";
	public static final String IR_API_USER_REGISTERFORPUSHMESSAGES = "ir_api_user_registerforpushmessages";
	public static final String MOB_CLIENT_CREATEANONYMOUSSESSION = "mob_client_createAnonymousSession";
	public static final String MOB_CLIENT_GETTRAFFICINFO = "mob_client_getTrafficInfo";
	public static final String MOB_CLIENT_CREATERECORDING = "mob_client_createRecording";
	public static final String MOB_CLIENT_DELETERECORDING = "mob_client_deleteRecording";
	public static final String MOB_CLIENT_EDITRECORDING = "mob_client_editRecording";
	public static final String MOB_CLIENT_SUBMITRECORDING = "mob_client_submitRecording";
	public static final String MOB_CLIENT_ISSUEREPORTING = "mob_client_issueReporting";
	public static final String MOB_CLIENT_GETISSUES = "mob_client_getIssues";
	public static final String MOB_CLIENT_GETALERTS = "mob_client_getAlerts";
	public static final String MOB_CLIENT_ACTIVITYRECOGNITION = "mob_client_activityRecognition";
	public static final String SC_API_USER_CREATEANONYMOUSUSER = "sc_api_user_createanonymoususer";
	public static final String TJD_API_GETJAMS_HSL = "tjd_api_getjams_hsl";
	public static final String SDM_API_SERVICE_LINE_DETECTION = "sdm_api_service_line_detection";
	
	/*
	 * Permissions
	 * ir_api_user_registerforpushmessages
	 * mob_client_createAnonymousSession
	 * mob_client_getTrafficInfo
	 * mob_client_createRecording
	 * mob_client_deleteRecording
	 * mod_client_editRecording
	 * mob_client_submitRecording
	 * mob_client_issueReporting
	 * mob_client_getIssues
	 * mob_client_getAlerts
	 * mob_client_activityRecognition
	 * sc_api_user_createanonymoususer
	 * tjd_api_getjams_hsl
	 * sdm_api_service_line_detection
	 */
//	public static final String ACTION_GET_
	public ServiceCentreService() {
		super("ServiceCentreService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		if(arg0.getAction().equals(ACTION_GET_ANON_USER_ID))
			getAnonUserId();
		else if(arg0.getAction().equals(ACTION_GET_PERMISSIONS_LIST))
			getPermissionsList();
	}
	
	private void broadcastAction(String action)
	{
		Log.i("ServiceCentreService", "Sending broadcast with action : " + action);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(action);
		sendBroadcast(broadcastIntent);
	}
	
	private void getAnonUserId()
	{
		SharedPreferences userPrefs = getSharedPreferences(USER_INFO_PREFS, 0);
		String userId = userPrefs.getString(USER_INFO_TAG_USER_ID, null);
		MobilityApplication app = (MobilityApplication) getApplication();
		if(userId == null)
		{
			try {
				userId = ServiceCentreUtils.createAnonymousUser(app.dHandler.getAndroidID());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		app.dHandler.getUserInfo().setUserID(userId);
		SharedPreferences.Editor editor = userPrefs.edit();
		editor.putString(USER_INFO_TAG_USER_ID, userId);
		editor.commit();
		broadcastAction(ACTION_RETURN_USER_ID);
	}
	
	
	//TODO: encrypt userPrefs
	private void getPermissionsList()
	{
		SharedPreferences userPrefs = getSharedPreferences(USER_INFO_PREFS, 0);
		SharedPreferences.Editor editor = userPrefs.edit();
		String serverResponse = ServiceCentreUtils.getAccountInfo();
		try{
			Gson gson = new Gson();
			List<LinkedTreeMap<String, String>> permissions = gson.fromJson(serverResponse, List.class);
//			ArrayList<String> permissionNames = new ArrayList<String>(permissions.size());
			for(LinkedTreeMap<String, String> permission : permissions){
				Log.i("ServiceCentreService", "adding permission : " + permission.get("name"));
				editor.putString(permission.get("name"), "1");
			}
			editor.commit();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		broadcastAction(ACTION_RETURN_USER_PERMISSIONS);
	}
}
