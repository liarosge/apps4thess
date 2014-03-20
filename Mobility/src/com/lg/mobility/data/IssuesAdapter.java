package com.lg.mobility.data;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lg.mobility.R;

public class IssuesAdapter extends ArrayAdapter<IssuesModel> {
	Context ctx;
	int layoutResourceId;
	List<IssuesModel> data;
	
	static class IssuesHolder
	{
		TextView issueTitle;
		TextView issueDescription;
	}
	public IssuesAdapter(Context context, int textViewResourceId,
			List<IssuesModel> objects) {
		super(context, textViewResourceId, objects);
		ctx = context;
		layoutResourceId = textViewResourceId;
		data = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		IssuesHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.issue_list_item, null);
			 holder = new IssuesHolder();
			 holder.issueTitle = (TextView) row.findViewById(R.id.issue_title);
			 holder.issueDescription = (TextView) row.findViewById(R.id.issue_description);
		     row.setTag(holder);			
		}
		else {
			holder = (IssuesHolder) row.getTag();
		}
		IssuesModel curModel;
		curModel = data.get(position);
		holder.issueTitle.setText(curModel.title);
		holder.issueDescription.setText(curModel.description);
		row.setEnabled(false);
		return row;
	}
}
