package com.feng.ye.earthquake;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;


public class EarthquakeActivity extends AppCompatActivity {
    public static final String TAG = EarthquakeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG + " ====> onCreate");
        setContentView(R.layout.activity_main);

        updateFromPreferences();

        //获取SearchManager
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchableInfo
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        //将Activity的searchableInfo与搜索视图进行绑定
        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchableInfo);
    }

    static final private int MENU_PREFERENCE = Menu.FIRST + 1;
    static final private int MENU_UPDATE = Menu.FIRST + 2;

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0, MENU_PREFERENCE, Menu.NONE, R.string.menu_preferences);
        return true;
    }

    private static final int SHOW_PREFERENCES = 1;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        switch (id){
            case R.id.action_settings:
                return true;
            case MENU_PREFERENCE:
                Class c = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                        UserPreferences.class : UserPreferencesNew.class;
                Intent i = new Intent(this, c);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public int minimumMagnitude = 0;        //用户首选项的最小地震大小
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    //activity启动时应用该首选项
    private void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //将值转换成整型
        updateFreq = Integer.parseInt(prefs.getString(UserPreferences.PREF_UPDATE_FREQ, "60"));
        minimumMagnitude = Integer.parseInt(prefs.getString(UserPreferences.PREF_MIN_MAG, "2"));

        autoUpdateChecked = prefs.getBoolean(UserPreferences.PREF_AUTO_UPDATE, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHOW_PREFERENCES){

            updateFromPreferences();
            FragmentManager fm = getFragmentManager();
            final EarthquakeListFragment earthquakeList =
                    (EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    earthquakeList.refreshEarthquakes();
                }
            });
            t.start();
            //earthquakeList.refreshEarthquakes();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " ====> onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " ====> onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + " ====> onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, TAG + " ====> onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " ====> onDestroy");
    }
}
