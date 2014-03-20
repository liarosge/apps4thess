package com.lg.mobility.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class ServiceCentreUtils {

	public static final String CLIENT = "Basic " + base64Encode("mobility_mobile_client_user:certh");
	
	public static String createAnonymousUser(String android_ID) throws UnsupportedEncodingException
	{
		HttpPost request = new HttpPost("https://testservicecenter.yucat.com/servicecenter/api/user/anonymous");
//		URI uri = URI.create("https://testserviceCenter.yucat.com/ServiceCenter/api/user/anonymous");
//		HttpPost request = new HttpPost("https://testserviceCenter.yucat.com/ServiceCenter/api/account/permission");
		request.setHeader("Authorization", CLIENT);
		request.setHeader(HTTP.CONTENT_TYPE,"application/json");
		Gson gson = new Gson();
		String json = gson.toJson(new AnonymousUserCreateInput(android_ID));
		Log.i("ServiceCentre", "client = " + CLIENT);
		Log.i("ServiceCentre", "input json = " + json);
		request.setEntity(new StringEntity(json, "UTF-8"));
		String response = executeRequest(request, 3000, 3000);
		LinkedTreeMap<String, Double> map = gson.fromJson(response, LinkedTreeMap.class);
		if(map == null) return null;
		Double userid = map.get("anonymoususerid");
		Log.i("ServiceCentre", "parse user id = " + userid);
		Log.i("ServiceCentre", "user id to string = " + String.valueOf(userid.intValue()));
		return String.valueOf(userid.intValue());
	}
	
	public static String getAccountInfo()
	{
		String output = null;
		try {
		    DefaultHttpClient httpClient = new DefaultHttpClient();
		    HttpGet httpGet = new HttpGet("https://testservicecenter.yucat.com/servicecenter/api/account/permission");
		    httpGet.addHeader("Authorization", CLIENT);
		    HttpResponse httpResponse = httpClient.execute(httpGet);
		    HttpEntity httpEntity = httpResponse.getEntity();
		    output = EntityUtils.toString(httpEntity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Log.i("Utils", "output = " + output);
		return output;
		
	}
	private static String base64Encode(String s)
	{
		return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
//		return Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
	}
	
	private static class AnonymousUserCreateInput
	{
//		public int[] customer_ids = new int[]{4}; 
		public String unique;
		//Mobility
		public AnonymousUserCreateInput(String android_ID)
		{
			unique = android_ID;
		}
	}
	private static String executeRequest( HttpUriRequest request, int soTime, int connTime )
	{
	HttpClient client = new DefaultHttpClient();
		
//		 ----- Set timeout --------------
		 HttpParams httpParameters = new BasicHttpParams();
//		
//		 // Set the timeout in milliseconds until a connection is established.
//		 // The default value is zero, that means the timeout is not used.
		 int timeoutConnection = 1000;
		 HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//		 // Set the default socket timeout (SO_TIMEOUT)
//		 // in milliseconds which is the timeout for waiting for data.
		 int timeoutSocket = 1000;
		 HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		client.getParams().setParameter( "http.socket.timeout", soTime );
		client.getParams().setParameter( "http.connection.timeout", connTime );
		
		// ----------------------------------
		HttpResponse httpResponse = null;
		try
		{
		    httpResponse = client.execute( request );
		    
		} catch ( ClientProtocolException e )
		{
		    client.getConnectionManager().shutdown();
		    e.printStackTrace();
		} catch ( IOException e )
		{
		    client.getConnectionManager().shutdown();
		    e.printStackTrace();
		}
		String responseJson = null;
		try {
			responseJson = EntityUtils.toString(httpResponse.getEntity(), HttpRequest.CHARSET_UTF8);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		Log.d("ServiceCentre", "response json = " + responseJson);
		return responseJson;
	}
}





//HttpPost request = new HttpPost( url );
//
////add headers
//for ( NameValuePair h : headers )
//{
// request.addHeader( h.getName(), h.getValue() );
//}
//if ( !_json.isEmpty() )
//{
// request.addHeader( "Content-type", "application/json" );
// request.setEntity( new StringEntity( _json, "UTF8" ) );
//} else if ( !params.isEmpty() )
//{
// request.setEntity( new UrlEncodedFormEntity( params, "UTF-8" ) );
//}
//	
//executeRequest( request, url, soTime, connTime );
//

//}
