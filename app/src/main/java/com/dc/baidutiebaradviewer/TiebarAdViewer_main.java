package com.dc.baidutiebaradviewer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dongq on 2016/2/4.
 */
/*
* TODO:
* 将链接图片显示在listview上
*
* Finished：
* listview和adapter的显示测试
*
* */
public class TiebarAdViewer_main extends Activity {

    private List<Map<String,String>> Maplist = new ArrayList<>();
    private List<String> adtextarray = new ArrayList<>();
    private List<String> adpicarray = new ArrayList<>();
    @Override
    protected void onCreate(Bundle SavedInstanceState)
    {

        super.onCreate(SavedInstanceState);
        setContentView(R.layout.main);
        if(isNetworkAvailable(this))
        {
            Toast.makeText(TiebarAdViewer_main.this,"Network available",Toast.LENGTH_SHORT).show();
        }
        else{Toast.makeText(TiebarAdViewer_main.this,"Network NOT available",Toast.LENGTH_SHORT).show();}
        new Thread(runnable).start();

    }

    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        else
        {   // 获取所有NetworkInfo对象
            NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;  // 存在可用的网络连接
            }
        }
        return false;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("handler", "Proceed!");
            show();
            // 收到消息后执行handler
        }
    };

    private void show() {

        ListView listview = (ListView) findViewById(R.id.List_view1);
        int i=adtextarray.size();
        String[] ShowStringArray=new String[i];

        //Log.i("adcount",""+i);
        Log.i("adcount",""+Maplist.size());

        /*
        ShowStringArray[0]="Start";
        ShowStringArray[i-1]="End";//安全操作防止数组出错null

        for(int k=0;k<i;k++){//!!!BUG:Index out of bounds
            ShowStringArray[k]=adtextarray.get(k+1);
            Log.i("ArrayContent",ShowStringArray[k]+k);
        }
        */
        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(
                TiebarAdViewer_main.this,
                android.R.layout.simple_list_item_1,
                ShowStringArray);
        */

        SimpleAdapter adapter=new SimpleAdapter(
                TiebarAdViewer_main.this,
                Maplist,
                R.layout.ad_item,
                new String[] {"adtext","adpic"},
                new int[] {R.id.adtitle,R.id.adcontent}
        );

        Log.i("Adapter","Adapter set!");
        listview.setAdapter(adapter);
        Log.i("ListView", "ListView set!");

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                //
                int i = 0;
                String url = "http://tieba.baidu.com/";
                Connection conn = Jsoup.connect(url);
                conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                Document doc = conn.get();
                Elements elements = doc.select("ul[id][alog-alias] li").select(".title").select(".feed-item-link");
                //构建执行第一次连接，得到首页不固定数量的帖子连接

                //
                String Topicurlt;
                ArrayList<String> Topicurl = new ArrayList<>();
                for (Element element : elements)
                {
                    Topicurlt = element.attr("href");
                    Topicurl.add ("http://tieba.baidu.com" + Topicurlt);
                    i++;
                }
                //构建第二次连接的所有连接的链表

                //
                String adpic,adtext;
                //i < Topicurl.size()
                for (i = 1; i < Topicurl.size(); i++) {
                    url = Topicurl.get(i);
                    Log.i("Webbot",url+" "+i);
                    conn = Jsoup.connect(url);
                    conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
                    doc = conn.get();
                    elements = doc.select(".d_post_content").select(".j_click_stats");
                    adtext = elements.text();//选出文字部分
                    elements = elements.select(".BDE_Image");
                    adpic = elements.attr("src");//选出图片连接
                if (!adtext.equals("")&&!adpic.equals("")) {
                    Map<String, String> map = new HashMap<>();
                    adtextarray.add(adtext);
                    adpicarray.add(adpic);

                    map.put("adtext", adtext);
                    map.put("adpic", adpic);
                    Maplist.add(map);
                 }
                }
                //执行第二次连接（不固定次数），得到广告的文字和图片连接，一共adtextarray.size()个

                handler.sendEmptyMessage(0);
                Log.i("Webbot", "Finished!");
                //get bitmap



            }
            catch (IOException e) {

            }
        }
    };

}

