package com.feng.ye.earthquake;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by FengLianhai on 2015/5/26.
 */
public class EarthquakeProvider extends ContentProvider {
    public static final String TAG = EarthquakeProvider.class.getSimpleName();

    public static final Uri CONTENT_URI = Uri.parse("content://com.feng.ye.earthquakeprovider/earthquakes");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    private EarthquakeDatabaseHelper dbHelper;

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.feng.ye.earthquakeprovider", "earthquakes", QUAKES);
        uriMatcher.addURI("com.feng.ye.earthquakeprovider", "earthquakes/#", QUAKE_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new EarthquakeDatabaseHelper(getContext(),
                EarthquakeDatabaseHelper.DATABASE_NAME, null, EarthquakeDatabaseHelper.DATABASE_VERSION);
        Log.i(EarthquakeActivity.TAG, TAG + " ====> onCreate dpHelper=" + dbHelper);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> query");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(EarthquakeDatabaseHelper.DATABASE_TABLE);
        //如果是行查询，就把结果集限制为传入的行
        switch(uriMatcher.match(uri)){
            case QUAKE_ID:
                queryBuilder.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:break;
        }
        //如果没有指定排序，就按照日期/时间排序
        String orderBy;
        if(TextUtils.isEmpty(sortOrder)){
            orderBy = KEY_DATE;
        }else {
            orderBy = sortOrder;
        }
        //对底层数据库执行查询
        Cursor c = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, orderBy);
        //注册当游标结果集改变时将通知的上下文ContentResolver
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> getType");
        switch (uriMatcher.match(uri)){
            case QUAKES:
                return "vnd.android.cursor.dir/vnd.feng.ye.earthquake";
            case QUAKE_ID:
                return "vnd.android.cursor.item/vnd.feng.ye.earthquake";
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> insert");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //插入新行.如果插入成功，就返回行号
        long id = db.insert(EarthquakeDatabaseHelper.DATABASE_TABLE, "quake", values);
        if(id > 0){
            Uri insertedUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> delete");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)){
            case QUAKES:
                count = db.delete(EarthquakeDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String rowID = uri.getPathSegments().get(1);
                count = db.delete(EarthquakeDatabaseHelper.DATABASE_TABLE, KEY_ID + "=" + rowID +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsuppoted Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> update");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)){
            case QUAKES:
                count = db.update(EarthquakeDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String rowID = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ?
                            " AND (" + selection + ')' : "");
                count = db.update(EarthquakeDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper{
        private static final String TAG = EarthquakeDatabaseHelper.class.getSimpleName();

        private static final String DATABASE_NAME = "earthquakes.db";
        private static final String DATABASE_TABLE = "earthquakes";
        private static final int DATABASE_VERSION = 1;

        private static final String EARTHQUAKE_CREATE = "create table " + DATABASE_TABLE + " (" +
                KEY_ID + " integer primary key autoincrement, " +
                KEY_DATE + " INTEGER, " +
                KEY_DETAILS + " TEXT, " +
                KEY_SUMMARY + " TEXT, " +
                KEY_LOCATION_LAT + " FLOAT, " +
                KEY_LOCATION_LNG + " FLOAT, " +
                KEY_MAGNITUDE + " FLOAT, " +
                KEY_LINK + " TEXT);";

        public EarthquakeDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(EarthquakeActivity.TAG, TAG + " ====> onCreate1111");
            db.execSQL(EARTHQUAKE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(EarthquakeActivity.TAG, TAG + " ====> onUpgrade1111");
            Log.w(TAG, "Upgrading database from " + oldVersion + " to " +
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
