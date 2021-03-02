package com.kontakt.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import static com.kontakt.sample.utils.config.PREFERENCES_VIRTUAL_FAIR;

//import android.preference.PreferenceManager;

public class SharedPreference {
	
	public SharedPreference() {
		super();
	}

	public void save(Context context, String text, String key) {
		SharedPreferences settings;
		Editor editor;
		
		//settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings = context.getSharedPreferences(PREFERENCES_VIRTUAL_FAIR, Context.MODE_PRIVATE); //1
		editor = settings.edit(); //2

		editor.putString(key, text); //3

		editor.commit(); //4
	}

	public String getValue(Context context, String key) {
		SharedPreferences settings;
		String text;

		//settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings = context.getSharedPreferences(PREFERENCES_VIRTUAL_FAIR, Context.MODE_PRIVATE);
		text = settings.getString(key, null);
		return text;
	}
	
	public void clearSharedPreference(Context context, String key) {
		SharedPreferences settings;
		Editor editor;

		//settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings = context.getSharedPreferences(PREFERENCES_VIRTUAL_FAIR, Context.MODE_PRIVATE);
		editor = settings.edit();

		editor.clear();
		editor.commit();
	}

	public void removeValue(Context context, String key) {
		SharedPreferences settings;
		Editor editor;

		settings = context.getSharedPreferences(PREFERENCES_VIRTUAL_FAIR, Context.MODE_PRIVATE);
		editor = settings.edit();

		editor.remove(key);
		editor.commit();
	}


}
