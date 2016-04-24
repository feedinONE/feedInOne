package com.septiadi.rssfetch;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    LinearLayout ll,listRssButton;

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    int cont;
    int rssTotal = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new curlRSS().execute(new String[]{"Detik", "http://rss.detik.com/gads.php/news", "0"});
        new curlRSS().execute(new String[]{"Republika","http://www.republika.co.id/rss","1"});
        new curlRSS().execute(new String[]{"Merdeka","http://www.merdeka.com/feed/","2"});
        new curlRSS().execute(new String[]{"Tempo","https://rss.tempo.co/index.php/teco/news/feed/start/0/limit/30","3"});
        new curlRSS().execute(new String[]{"Sindo","http://www.sindonews.com/feed","4"});
        ll = (LinearLayout)findViewById(R.id.linkList);
        listRssButton = (LinearLayout)findViewById(R.id.listRssButton);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        cont++;
        if(cont % 2 != 0){
            Toast.makeText(getApplicationContext(), "Press once again to exit", Toast.LENGTH_SHORT).show();
        }else if(cont % 2 == 0){
            final AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this);
            confirm.setMessage("Exit application?");
            confirm.setPositiveButton("Y", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                }
            });
            confirm.setNegativeButton("N", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cont=0;
                }
            });

            confirm.show();
        }
    }

    class curlRSS extends AsyncTask<String, String, String> {
        protected String rssTitle;
        protected Integer layoutId;
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[1]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4");
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();

//--------------- parser ---------------

                InputStream stream = conn.getInputStream();

                xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser xmlParser = xmlFactoryObject.newPullParser();

                xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xmlParser.setInput(stream, null);

                String[] links = XMLParserHanlder(xmlParser);
                rssTitle = urls[0];
                layoutId = Integer.valueOf(urls[2]);

                publishProgress(links);
                stream.close();

//--------------- parser ---------------


            }catch (Exception e) {

                System.out.println(e);
            }


            return "";
        }
        @Override
        protected void onPostExecute(String result) {
//            myview.setText("sd");
        }
        @Override
        protected void onPreExecute() {
//            myview.setText("Task Starting...");
        }
        @Override
        protected void onProgressUpdate(String... values) {
            LinearLayout rssList = new LinearLayout(getApplicationContext());
            rssList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            rssList.setOrientation(LinearLayout.VERTICAL);
            rssList.setId(layoutId);
            if(layoutId != 0) {
                rssList.setVisibility(View.GONE);
            }

            Button rssButton = null;
            rssButton = new Button(getApplicationContext());
            rssButton.setText(rssTitle);
            rssButton.setTag(rssTitle);
            rssButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    for (int  i = 0;i < rssTotal; i++){
                        String linId = "" + i;
                        int resID = getResources().getIdentifier(linId, "id", "com.septiadi.rssfetch");
                        LinearLayout hideLin = (LinearLayout) findViewById(resID);

                        if(layoutId != i) {
                            hideLin.setVisibility(View.GONE);
                        }else{
                            hideLin.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            listRssButton.addView(rssButton);




            Button addButton;
            for (String val: values) {//loop generate button here
                JSONObject obj;
                try {
                    obj = new JSONObject(val);


                    addButton = new Button(getApplicationContext());
                    addButton.setText(rssTitle+" " +
                            ": "+obj.getString("title"));
                    addButton.setTag(obj.getString("link"));
//                    addButton.setId(countAllRssLinks + 1);
                    addButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // Perform action on click
                            Button b = (Button) v;
                            String buttonText = b.getTag().toString();
                            loadDetailPage(buttonText);
                        }
                    });

                    rssList.addView(addButton);

//                    countAllRssLinks++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            ll.addView(rssList);
        }
    }

    public String[] XMLParserHanlder(XmlPullParser myParser) {
        int event;
        String text=null;
        ArrayList<String> items = new ArrayList<>();

        JSONObject subitem = new JSONObject();

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();

                switch (event){
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if(name.equals("item")){
                            items.add(subitem.toString());
                        }

                        if(name.equals("title")){
                            subitem.put("title", text);
                        }

                        else if(name.equals("link")){
                            subitem.put("link", text);
                        }

                        else if(name.equals("description")){
                            subitem.put("description", text);
                        }

                        else{
                        }

                        break;
                }

                event = myParser.next();
            }

            parsingComplete = false;
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        String[] itemsArray = items.toArray(new String[items.size()]);
        return itemsArray;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        //show the web page in webview but not in web browser
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl (url);
            return true;
        }
    }

    public void loadDetailPage(String Url) {

        Button closeButton = (Button) findViewById(R.id.closeDetail);
        closeButton.setVisibility(View.VISIBLE);

        WebView mWebView = (WebView)findViewById(R.id.webView);
//        mWebView.getSettings().setBuiltInZoomControls(true);
//        mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
//        mWebView.setBackgroundColor(Color.TRANSPARENT);
//        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_INSET);

        mWebView.getSettings().setJavaScriptEnabled(false);//javascript setting
        mWebView.getSettings().setLoadsImagesAutomatically(false);//disable load image

        mWebView.loadUrl(Url);
        mWebView.setWebViewClient(new MyWebViewClient());//used for opening in this app
        mWebView.setVisibility(View.VISIBLE);

    }

    public void closeDetail(View view) {
        Button closeButton = (Button) findViewById(R.id.closeDetail);
        closeButton.setVisibility(View.GONE);

        WebView mWebView = (WebView)findViewById(R.id.webView);
        mWebView.loadData("", "text/html", null);
        mWebView.setVisibility(View.GONE);
    }

}
