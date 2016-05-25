package com.example.autofan;

import java.util.ArrayList;
import java.util.Map;

import com.example.autofan.storage.Store;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

// This adapter is used to provide fan list to the 
// list of fans.
public class FansAdapter extends BaseAdapter {

	private ArrayList< Map<String, String> > fans;
	private Context context;
	private LayoutInflater inflater;
	
	public FansAdapter(Context context, ArrayList< Map<String, String>> fans) {
		this.context = context;
		this.fans = fans;
		this.inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE
		);
	}
	@Override
	public int getCount() {
		return this.fans.size();
	}

	@Override
	public Object getItem(int index) {
		return this.fans.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		View view = inflater.inflate(R.layout.single_fan, null);
		TextView controllerIP  = (TextView) view.findViewById(R.id.controllerIP);
		TextView controllerPort =(TextView) view.findViewById(R.id.controllerPort);
		Button button = (Button) view.findViewById(R.id.button1);
		this.addFanSelectListener(button, index);
		
		controllerIP.setText( this.fans.get(index).get("address") );
		controllerPort.setText( this.fans.get(index).get("port") );
		
		return view;
	}
	
	public void addFanSelectListener(Button button, int index) {
		final int index_ = index;
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String controllerAddress = "http://" + 
						FansAdapter.this.fans.get(index_).get("address") + ":" + 
						FansAdapter.this.fans.get(index_).get("port");
				Store.put("saved_ip", controllerAddress, FansAdapter.this.context);
				
				((MainActivity) FansAdapter.this.context).connect();	
			}
		});
	}
	
}
