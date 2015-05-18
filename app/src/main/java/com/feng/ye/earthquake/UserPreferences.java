package com.feng.ye.earthquake;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Administrator on 2015/5/14.
 */
public class UserPreferences extends PreferenceActivity {

    public static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.preferences);
        addPreferencesFromResource(R.xml.user_preferences);

    }

}
