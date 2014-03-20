package com.lg.mobility.data;

import com.lg.mobility.R;
import com.lg.mobility.data.LinesAdapter.LinesHolder;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HARAdapter extends ArrayAdapter<HARActivityModel> {
	
	HARActivityModel[] data;
	Context ctx;
	public HARAdapter(Context context, int resource, Drawable[] _drawables) {
		super(context, resource);
		ctx = context;
		HARActivityModel.setDrawables(_drawables);
		data = HARActivityModel.getActivities();
	}
	
	static class ActivityHolder
	{
		ImageView icon;
		TextView name;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ActivityHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.activity_list_item, null);
			 holder = new ActivityHolder();
			 holder.icon = (ImageView) row.findViewById(R.id.activity_icon);
			 holder.name = (TextView) row.findViewById(R.id.activity_desc);
		     row.setTag(holder);			
		}
		else {
			holder = (ActivityHolder) row.getTag();
		}
		holder.icon.setBackgroundDrawable(data[position].type.getIcon());
		holder.name.setText(data[position].name);
		row.setEnabled(false);
		return row;
	}
	
	@Override
	public int getCount() {
		return data.length;
	}
}
