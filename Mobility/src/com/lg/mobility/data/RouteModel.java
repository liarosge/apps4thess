package com.lg.mobility.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

public class RouteModel {
	public String hexEncoded;
	public String lineID;
	public int direction;
	public RouteModel(String _hexEncoded, String _lineID, int _direction)
	{
		hexEncoded = _hexEncoded;
		lineID = _lineID;
		direction = _direction;
	}
	
	public static RouteModel getRoute(String lineID, int direction, File cacheDir) throws IOException
	{
		String filePath = lineID + "_" + direction + ".txt";
		File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + filePath);
		if(cacheFile.exists())
		{
			 BufferedReader br = new BufferedReader(new FileReader(cacheFile));
			    try {
			        StringBuilder sb = new StringBuilder();
			        String line = br.readLine();

			        while (line != null) {
			            sb.append(line);
			            sb.append('\n');
			            line = br.readLine();
			        }
			        String hexEncoded = sb.toString();
			        Log.i("RouteModel", "returning route from cache");
			        return new RouteModel(hexEncoded, lineID, direction);
			    } finally {
			        br.close();
			    }
		}
		else
		{
			String encodedRoute;
			try
			{
				encodedRoute = HttpRequest.get("http://83.145.232.209:10001/?type=route&line=" + lineID + "&direction=" + (int) direction).readTimeout(2000).body();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
			if(encodedRoute == null)
				return null;
			BufferedWriter writer = null;
	        try {
	            writer = new BufferedWriter(new FileWriter(cacheFile));
	            writer.write(encodedRoute);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                writer.close();
	                Log.i("RouteModel", "returning route from web");
	                return new RouteModel(encodedRoute, lineID, direction);
	            } catch (Exception e) {
	            }
	        }
		}
		return null;
	}
}
