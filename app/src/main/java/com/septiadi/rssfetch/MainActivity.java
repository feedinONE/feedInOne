package com.septiadi.rssfetch;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    LinearLayout ll;

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new curlRSS().execute("http://www.merdeka.com/feed/");
        ll = (LinearLayout)findViewById(R.id.linkList);
    }

    class curlRSS extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
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

                publishProgress(links);
                stream.close();

//--------------- parser ---------------

//                int status = conn.getResponseCode();
//                System.out.println(status);
//
//                String content = conn.getContentEncoding();
//                System.out.println(content);
//
//                switch (status) {
//                    case 200:
//                    case 201:
//                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        StringBuilder sb = new StringBuilder();
//                        String line;
//                        while ((line = br.readLine()) != null) {
//                            sb.append(line+"\n");
//                        }
//                        br.close();
//                        System.out.println(sb.toString());
//
//                        publishProgress(sb.toString());
//
//                        return sb.toString();
//
//                }

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
            Button addButton;
            int i = 0;
            for (String val: values) {//loop generate button here

                addButton = new Button(getApplicationContext());
                addButton.setText(val);
                addButton.setId(i + 1);
                addButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Perform action on click
                        Button b = (Button)v;
                        String buttonText = b.getText().toString();
                        loadDetailPage(buttonText);
                    }
                });

                ll.addView(addButton);

                i++;
            }
        }
    }

    public String[] XMLParserHanlder(XmlPullParser myParser) {
        int event;
        String text=null;
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();

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

                        if(name.equals("title")){
                            titles.add(text);
                        }

                        else if(name.equals("link")){
                            links.add(text);

                            System.out.println(links);
                        }

                        else if(name.equals("description")){
                            descriptions.add(text);
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

        String[] linksArray = links.toArray(new String[links.size()]);
        return linksArray;
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

//        TextView mytext = (TextView) findViewById(R.id.mytext);
//        mytext.setText(Url);

        Button closeButton = (Button) findViewById(R.id.closeDetail);
        closeButton.setVisibility(View.VISIBLE);

        WebView mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_INSET);

        mWebView.loadUrl(Url);
        mWebView.setWebViewClient(new MyWebViewClient());
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
