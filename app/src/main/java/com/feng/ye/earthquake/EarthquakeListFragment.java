package com.feng.ye.earthquake;


import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Administrator on 2015/5/11.
 */
public class EarthquakeListFragment extends ListFragment {

    ArrayAdapter<Quake> aa;
    ArrayList<Quake> earthquakes = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<Quake>(getActivity(), layoutID, earthquakes);
        setListAdapter(aa);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthquakes();
            }
        });
        t.start();
    }

    private static final String TAG = EarthquakeListFragment.class.getSimpleName();
    private Handler handler = new Handler();

    public void refreshEarthquakes() {
        URL url;
        String quakeUrl = getString(R.string.quake_feed);
        try {
            url = new URL(quakeUrl);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConn = (HttpURLConnection) urlConnection;
            int responseCode = httpURLConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpURLConn.getInputStream();

                //dom����
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                //����ɵ�����
                earthquakes.clear();

                //��ȡÿ����������б�
                NodeList entryList = docEle.getElementsByTagName("entry");
                if (entryList != null && entryList.getLength() > 0) {
                    for (int i = 0; i < entryList.getLength(); i++) {
                        Element entry = (Element) entryList.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element updated = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);
                        Element gp = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Log.d(TAG, "gp ="+gp);
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
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addNewQuake(quake);
                                }
                            });
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURL Exception!");
        } catch (IOException e) {
            Log.d(TAG, "IO Exception!");
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "Parser Configuration Exception!");
        } catch (SAXException e) {
            Log.d(TAG, "SAX Exception!");
        } catch (ParseException e) {
            Log.d(TAG, "Parse Exception!");
        }

    }

    //��ӵ�����
    private void addNewQuake(Quake quake) {
        EarthquakeActivity eqActivity =(EarthquakeActivity) getActivity();
        if(quake.getMagnitude() > eqActivity.minimumMagnitude){
            //将新地震添加到地震列表中
            earthquakes.add(quake);
        }
        //将变化通知给数组适配器
        aa.notifyDataSetChanged();
    }
}
