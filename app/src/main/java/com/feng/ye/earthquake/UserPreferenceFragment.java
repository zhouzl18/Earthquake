package com.feng.ye.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by FengLianhai on 2015/5/18.
 */
public class UserPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_preferences);
    }
}
