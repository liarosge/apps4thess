package com.lg.mobility.fragments;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.yucat.pushtestclient.adapter.MessageAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.kevinsawicki.http.HttpRequest;
import com.lg.mobility.R;
import com.lg.mobility.activities.AlertsActivity;
import com.lg.mobility.data.AlertType;
import com.lg.mobility.data.AlertsAdapter;
import com.lg.mobility.data.AlertsModel;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AuthorityInfoFragment extends Fragment{
	
	private static AlertsAdapter dataAdapter;

	public View rootView;
	private ListView list;
	private Context ctx;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d("AlertsActivity", "onCreateView()");
		if(rootView == null) {    	
			rootView = inflater.inflate( R.layout.authority_info_fragment, container, false );
    	} else {
    		ViewGroup parent = (ViewGroup) rootView.getParent();
			if (parent != null) {
				parent.removeView(rootView);
			}
    	}
		((AlertsActivity) ctx).addMessages();
		list = (ListView) rootView.findViewById(R.id.alerts_list);
		MessageAdapter adapter = ((AlertsActivity) ctx).listAdapter;
		list.setAdapter(adapter);
//		if(dataAdapter == null)
//		{
//			dataAdapter = new AlertsAdapter(ctx, R.layout.alert_list_item, new ArrayList<AlertsModel>());
//			new DisruptionInfoTask().execute(new Object[1]);
//		}
//		list.setAdapter(dataAdapter);
		return rootView;
	}
	
	public void clearMessages()
	{
		//bug here in line 76
		if(list == null)
			list = (ListView) rootView.findViewById(R.id.alerts_list);
		MessageAdapter adapter = ((AlertsActivity) ctx).listAdapter;
		list.setAdapter(adapter);
	}
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		ctx = activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
//		((AlertsActivity) ctx).addMessages();
		super.onResume();
	}
	
	private class DisruptionInfoTask extends AsyncTask<Object, AlertsModel, Object>
	{

		@Override
		protected Object doInBackground(Object... params) {
			String xml;
			try
			{	
				xml = HttpRequest.get("http://www.poikkeusinfo.fi/xml/v2").body();
				Document doc = XMLfromString(xml);
				NodeList list = doc.getElementsByTagName("TEXT");
				for(int i = 0 ; i < list.getLength(); i++)
				{
					Node node = list.item(i);
					NamedNodeMap map = node.getAttributes();
					if(map.item(0).getTextContent().equals("en")){
						publishProgress(AlertsModel.initAlert()
								.setTitle(node.getTextContent())
								.setType(AlertType.DISRUPTION)
								.setFromTime("00:00")
								.setToTime("04:00"));
					}
				}
			}
			catch(Exception e)
			{
				Log.e("AuthorityInfoFragment", "error fetching disruption info");
				e.printStackTrace();
				return null;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(AlertsModel... values) {
			dataAdapter.add(values[0]);
			Log.i("AuthorityInfoFragment", "adding " + values[0].title);
			dataAdapter.notifyDataSetChanged();
			super.onProgressUpdate(values);
		}
		
	}
	
	public Document XMLfromString(String v){

        Document doc = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(v));
            doc = db.parse(is); 

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
//            System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;

    }
}
