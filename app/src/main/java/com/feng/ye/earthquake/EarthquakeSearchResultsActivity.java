package com.feng.ye.earthquake;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

/**
 * Created by FengLianhai on 2015/5/28.
 */
public class EarthquakeSearchResultsActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TAG = EarthquakeSearchResultsActivity.class.getSimpleName();

    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.w(TAG, "onCreate");
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
        //获取启动的Intent
        parseIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w(TAG, "onNewIntent");
        parseIntent(intent);
    }

    private static String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";
    private void parseIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            //如果activity已启动，并为搜索查询提供服务，那么提取搜索查询的值
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            //将搜索查询传递给Cursor Loader
            Bundle args = new Bundle();
            args.putString(QUERY_EXTRA_KEY, searchQuery);
            //重新启动Cursor Loader来执行新的查询
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.w(TAG, "onCreateLoader");
        String query = "0";
        if(args != null){
            //从参数里提取搜索查询的内容
            query = args.getString(QUERY_EXTRA_KEY);
        }
        String projection[] = new String[]{EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_SUMMARY};
        String selection = EarthquakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
        String selectionArgs[] = null;
        String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";
        //创建一个新的CursorLoader
        return new CursorLoader(this, EarthquakeProvider.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.w(TAG, "onLoadFinished");
        //用新的结果集替换由Cursor Adapter显示的结果Cursor
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.w(TAG, "onLoaderReset");
        //从List Adapter中删除现有的结果Cursor
        adapter.swapCursor(null);
    }
}
