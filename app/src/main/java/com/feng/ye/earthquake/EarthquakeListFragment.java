package com.feng.ye.earthquake;


import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Administrator on 2015/5/11.
 */
public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String TAG = EarthquakeListFragment.class.getSimpleName();
    //ArrayAdapter<Quake> aa;
    //ArrayList<Quake> earthquakes = new ArrayList<>();

    //地震已经存储到ContentProvider中，应该使用SimpleCursorAdapter
    SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(EarthquakeActivity.TAG, TAG + " ====> onActivityCreated");

        int layoutID = android.R.layout.simple_list_item_1;
        //aa = new ArrayAdapter<Quake>(getActivity(), layoutID, earthquakes);
        adapter = new SimpleCursorAdapter(getActivity(), layoutID, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY}, new int[]{android.R.id.text1}, 0);
        //setListAdapter(aa);
        setListAdapter(adapter);

        //启动Loader
        getLoaderManager().initLoader(0, null, this);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthquakes();
            }
        });
        t.start();
    }

    private Handler handler = new Handler();

    public void refreshEarthquakes() {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> refreshEarthquakes");
        //在主线程重新启动Loader
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
            }
        });*/
        URL url;
        String quakeUrl = getString(R.string.quake_feed);
        try {
            url = new URL(quakeUrl);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConn = (HttpURLConnection) urlConnection;
            int responseCode = httpURLConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpURLConn.getInputStream();

                //dom解析
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                //earthquakes.clear();

                NodeList entryList = docEle.getElementsByTagName("entry");
                if (entryList != null && entryList.getLength() > 0) {
                    for (int i = 0; i < entryList.getLength(); i++) {
                        Element entry = (Element) entryList.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element updated = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);
                        Element gp = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Log.d(EarthquakeActivity.TAG, TAG + " ====> gp ="+gp);
                        if(gp != null){
                            String titleStr = title.getFirstChild().getNodeValue();
                            String gpStr = gp.getFirstChild().getNodeValue();
                            String dtStr = updated.getFirstChild().getNodeValue();

                            //����
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                            Date qdate = new GregorianCalendar(0, 0, 0).getTime();
                            qdate = sdf.parse(dtStr);

                            //����
                            String[] location = gpStr.split(" ");
                            Location l = new Location("quakeGPS");
                            l.setLatitude(Double.parseDouble(location[0]));
                            l.setLongitude(Double.parseDouble(location[1]));

                            //����
                            String linkStr = link.getAttribute("href");

                            //��С
                            String magnitudeStr = titleStr.split(" ")[1];
                            magnitudeStr = magnitudeStr.substring(0, magnitudeStr.length() - 1);
                            Double magnitude = Double.parseDouble(magnitudeStr);

                            //�ط�
                            titleStr = titleStr.split(",")[1].trim();

                            final Quake quake = new Quake(qdate, titleStr, l, magnitude, linkStr);
                            //�����·��ֵĵ���
                            /*handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addNewQuake(quake);
                                }
                            });*/
                            addNewQuake(quake);
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.d(EarthquakeActivity.TAG, TAG + " ====> MalformedURL Exception!");
        } catch (IOException e) {
            Log.d(EarthquakeActivity.TAG, TAG + " ====> IO Exception !");
        } catch (ParserConfigurationException e) {
            Log.d(EarthquakeActivity.TAG, TAG + " ====> Parser Configuration Exception");
        } catch (SAXException e) {
            Log.d(EarthquakeActivity.TAG, TAG + " ====> SAX Exception");
        } catch (ParseException e) {
            Log.d(EarthquakeActivity.TAG, TAG + " ====> Parse Exception");
        }

    }

    //添加新的地震信息
    private void addNewQuake(Quake quake) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> addNewQuake");

        //使用ContentResolver向提供程序添加新的地震项
        ContentResolver cr = getActivity().getContentResolver();
        //Log.d(TAG, " ====> ContentResolver cr = " + cr);
        //构造一条where子句，保证现有的提供程序中没有这个地震项
        long time = quake.getDate().getTime();
        String w = EarthquakeProvider.KEY_DATE + " = " + time;
        //执行查询并返回结果集，判断结果集的数量
        Cursor c = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);
        Log.d(TAG, " ====> cursor c = " + c);
        if(c.getCount() == 0){
            //地震项是新的，插入到提供程序中
            ContentValues newValues = new ContentValues();
            newValues.put(EarthquakeProvider.KEY_DATE, time);
            newValues.put(EarthquakeProvider.KEY_DETAILS, quake.getDetails());
            newValues.put(EarthquakeProvider.KEY_SUMMARY, quake.toString());
            newValues.put(EarthquakeProvider.KEY_LOCATION_LAT, quake.getLocation().getLatitude());
            newValues.put(EarthquakeProvider.KEY_LOCATION_LNG, quake.getLocation().getLongitude());
            newValues.put(EarthquakeProvider.KEY_MAGNITUDE, quake.getMagnitude());
            newValues.put(EarthquakeProvider.KEY_LINK, quake.getLink());

            cr.insert(EarthquakeProvider.CONTENT_URI, newValues);
        }
        c.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> onCreateLoader");
        //构建并返回Loader,该Loader会查询EarthquakeProviderz中的所有元素
        //结果急中包含的列
        String projection[] = new String[]{
                EarthquakeProvider.KEY_ID,
                EarthquakeProvider.KEY_SUMMARY,
        };
        EarthquakeActivity eqActivity = (EarthquakeActivity) getActivity();
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + eqActivity.minimumMagnitude;
        CursorLoader loader = new CursorLoader(eqActivity, EarthquakeProvider.CONTENT_URI, projection, where, null, null);
        Log.i(EarthquakeActivity.TAG, TAG + " ====> CursorLoader loader =" + loader);
        return loader;
    }

    //当Loader的查询完成后，结果Cursor将返回给onLoadFinished处理程序
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> onLoadFinished");
        //用新的结果交换出原来的cursor
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(EarthquakeActivity.TAG, TAG + " ====> onLoaderReset");
        //类似地,当Loader重置时,删除对Cursor的引用
        adapter.swapCursor(null);
    }
}
