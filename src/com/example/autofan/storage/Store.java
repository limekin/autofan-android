package com.example.autofan.storage;

import com.example.autofan.R;

import android.content.Context;
import android.content.SharedPreferences;

// Just wraps up sharedpreferences.
public class Store {
	
	public static String get(String key, Context context) {
		SharedPreferences pref = context.getSharedPreferences(
			context.getString(R.string.pref_file), 
			Context.MODE_PRIVATE
		);
		
		return pref.getString(key, "empty");
	}
	
	public static void put(String key, String value, Context context) {
		SharedPreferences pref = context.getSharedPreferences(
			context.getString(R.string.pref_file), 
			Context.MODE_PRIVATE
		);
		
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
