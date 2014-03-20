package com.lg.mobility.data;

import java.util.List;

import com.lg.mobility.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlertsAdapter extends ArrayAdapter<AlertsModel> {
	Context ctx;
	int layoutResourceId;
	List<AlertsModel> data;
	
	static class AlertsHolder
	{
		TextView alertTitle;
		TextView alertDescription;
		TextView alertTime;
	}
	public AlertsAdapter(Context context, int textViewResourceId,
			List<AlertsModel> objects) {
		super(context, textViewResourceId, objects);
		ctx = context;
		layoutResourceId = textViewResourceId;
		data = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		AlertsHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.alert_list_item, null);
			 holder = new AlertsHolder();
			 holder.alertDescription = (TextView) row.findViewById(R.id.alert_description);
			 holder.alertTitle = (TextView) row.findViewById(R.id.alert_type);
			 holder.alertTime = (TextView) row.findViewById(R.id.alert_time);
		     row.setTag(holder);			
		}
		else {
			holder = (AlertsHolder) row.getTag();
		}
		AlertsModel curModel = data.get(position);
		switch (curModel.alertType) {
		case ACCIDENT:
			holder.alertTitle.setText("Accident");
			break;
		case DISRUPTION:
			holder.alertTitle.setText("Disruption");
			break;
		default:
			holder.alertTitle.setText("Accident");
			break;
		}
		holder.alertDescription.setText(curModel.title);
		holder.alertTime.setText("");
		row.setEnabled(false);
		return row;
	}
}
