package com.feng.ye.earthquake;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by FengLianhai on 2015/5/18.
 */
public class UserPreferencesNew extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        //return super.isValidFragment(fragmentName);
        if(UserPreferenceFragment.class.getName().equals(fragmentName)){
            return true;
        }else
            return false;
    }
}
