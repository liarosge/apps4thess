package com.lg.mobility.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.analytics.tracking.android.EasyTracker;
import com.lg.mobility.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.MenuItem;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_about);
		InputStream is = getResources().openRawResource(R.raw.about);
		WebView wv = (WebView) findViewById(R.id.about_webview);
		PackageInfo pInfo;
		String version;
		try {
			pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			version = "N/A";
			e.printStackTrace();
		}
		String template = readTxt();
		String android_id = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
		template = template.replace("[Version]", version);
//		template = template.replace("[AndroidID]", android_id);
//		wv.loadData(template, "text/html", "UTF-8");
		wv.loadDataWithBaseURL("file:///android_asset/", template, "text/html", "UTF-8", null);
//		webView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", null);
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
	    {
	    case android.R.id.home: 
	    	onBackPressed();
	        break;
	    default:
	    	return super.onOptionsItemSelected(item);
		}
	    return true;
	}

    private String readTxt(){

        InputStream inputStream = getResources().openRawResource(R.raw.about);
        System.out.println(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
     try {
      i = inputStream.read();
      while (i != -1)
         {
          byteArrayOutputStream.write(i);
          i = inputStream.read();
         }
         inputStream.close();
     } catch (IOException e) {
      e.printStackTrace();
     }

        return byteArrayOutputStream.toString();
       }
}
