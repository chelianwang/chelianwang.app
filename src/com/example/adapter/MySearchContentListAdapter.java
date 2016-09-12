package com.example.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.beans.SearchInfo;
import com.example.iov.R;



import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MySearchContentListAdapter extends BaseAdapter{
	private ArrayList<SearchInfo>infoList;
	private Context mContext;
	public MySearchContentListAdapter() {
		super();
	}

	public MySearchContentListAdapter(ArrayList<SearchInfo> infoList,Context context) {
		super();
		this.infoList = infoList;
		this.mContext=context;
	}

	@Override
	public int getCount() {
		return infoList.size();
	}

	@Override
	public Object getItem(int position) {
		return infoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		SearchInfo info= infoList.get(position);
		if (convertView==null) {
			holder=new ViewHolder();
			convertView=View.inflate(mContext, R.layout.search_list_item, null);
			holder.searchName=(TextView) convertView.findViewById(R.id.search_desname);
			holder.searchAddress=(TextView) convertView.findViewById(R.id.search_address);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		holder.searchName.setText(info.getDesname());
		holder.searchAddress.setText(info.getAddress());
		return convertView;
	}
	public final class ViewHolder{
		public TextView searchName;
		public TextView searchAddress;
	}
}
