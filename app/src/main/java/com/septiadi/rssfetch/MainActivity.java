package com.septiadi.rssfetch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    LinearLayout ll,listRssButton;
    TableLayout rssSettingList;
    TextView textRss;

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    int cont;
    int rssTotal = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ll = (LinearLayout)findViewById(R.id.linkList);
        listRssButton = (LinearLayout)findViewById(R.id.listRssButton);

        fetchingRss();

    }

    private void fetchingRss(){

        ll = (LinearLayout)findViewById(R.id.linkList);
        listRssButton = (LinearLayout)findViewById(R.id.listRssButton);
        rssSettingList = (TableLayout)findViewById(R.id.rssSettingList);

        ll.removeAllViews();
        listRssButton.removeAllViews();
        rssSettingList.removeAllViews();
//        writeToFile("");//used for reset rss
        String rssConf = readFromFile();

        if(rssConf == "") {
            new curlRSS().execute(new String[]{"Republika", "http://www.republika.co.id/rss", "1"});
            new curlRSS().execute(new String[]{"Merdeka", "http://www.merdeka.com/feed/", "2"});
            new curlRSS().execute(new String[]{"Tempo", "https://rss.tempo.co/index.php/teco/news/feed/start/0/limit/30", "3"});
            new curlRSS().execute(new String[]{"Sindo", "http://www.sindonews.com/feed", "4"});
            new curlRSS().execute(new String[]{"Detik", "http://rss.detik.com/gads.php/news", "5"});

            ArrayList<String[]> rssDef = new ArrayList<String[]>();
            rssDef.add(new String[]{"Republika", "http://www.republika.co.id/rss", "1"});
            rssDef.add(new String[]{"Merdeka", "http://www.merdeka.com/feed/", "2"});
            rssDef.add(new String[]{"Tempo", "https://rss.tempo.co/index.php/teco/news/feed/start/0/limit/30", "3"});
            rssDef.add(new String[]{"Sindo", "http://www.sindonews.com/feed", "4"});
            rssDef.add(new String[]{"Detik", "http://rss.detik.com/gads.php/news", "5"});

            String listString = "";

            for (String[] sub : rssDef)
            {
                for (String subs : sub)
                {

                    listString += subs + "\t";
                }

                listString += "\t";
            }
            writeToFile(listString);
        }else {

            String[] rssRow = rssConf.split("\t\t");
            for (String rssSubs : rssRow)
            {

                String[] subs = rssSubs.split("\t");

                rssTotal = Integer.parseInt(subs[2]);

                appendRssSetting(subs);
                new curlRSS().execute(subs);
            }
        }

//        textRss = (TextView)findViewById(R.id.textRss);
//        textRss.setText(rssConf);

    }

    public void appendRssSetting(final String[] subs){
        rssSettingList = (TableLayout)findViewById(R.id.rssSettingList);

        TableRow row= new TableRow(this);

        int resID = getResources().getIdentifier("rssSetting"+subs[2], "id", "com.septiadi.rssfetch");
        row.setId(resID);

        TextView text = new TextView(this);
        text.setText(subs[0]);
        text.setPadding(3, 3, 3, 3);
        row.addView(text);

        TextView text2 = new TextView(this);
        text2.setText(subs[1]);
        text2.setPadding(3, 3, 3, 3);
        row.addView(text2);


        Button delrss= null;
        delrss = new Button(this);
        delrss.setText("del");
        delrss.setTextColor(Color.WHITE);
        delrss.setTag(subs[2]);
        delrss.setWidth(50);
        delrss.setHeight(35);

        delrss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                delRss(subs[2]);
            }
        });
        row.addView(delrss);


        rssSettingList.addView(row);

    }

    public void delRss(String id){

        //deleting button
        TableRow row;
        int resID = getResources().getIdentifier("rssSetting"+id, "id", "com.septiadi.rssfetch");
        row = (TableRow)findViewById(resID);
        row.setVisibility(View.GONE);


        //get rss
        String rssConf = readFromFile();
        String toSave = "";
        String[] rssRow = rssConf.split("\t\t");
        for (String rssSubs : rssRow)
        {
            String[] subs = rssSubs.split("\t");

            if(String.valueOf(subs[2]).equals(String.valueOf(id))) {
            }else {

                String t1 = String.valueOf(subs[2]);
                String t2 = String.valueOf(id);
                Log.e("Exception", "File write failed: " + t1 + t2);
                toSave += rssSubs + "\t\t";
            }
        }

        writeToFile(toSave);
//        Log.e("Exception", "File write failed: " + toSave);

    }

    public void addRss(View view){

        String rssConf = readFromFile();
        String rssId = "0";
        String toSave = rssConf;

        if(rssConf != "") {
            String[] rssRow = rssConf.split("\t\t");
            String[] rssSubRow = rssRow[rssRow.length-1].split("\t");
            rssId = String.valueOf(Integer.parseInt(rssSubRow[2]) + 1);
        }

        EditText rssTitleInput   = (EditText)findViewById(R.id.rssTitleInput);
        EditText rssUrlInput   = (EditText)findViewById(R.id.rssUrlInput);

        if(rssTitleInput.getText().toString() != "" && rssUrlInput.getText().toString() != "") {
            toSave += rssTitleInput.getText().toString() + "\t" + rssUrlInput.getText().toString() + "\t" + rssId + "\t\t";
            writeToFile(toSave);

            String[] subs = new String[3];
            subs[0] = rssTitleInput.getText().toString();
            subs[1] = rssUrlInput.getText().toString();
            subs[2] = rssId;

            appendRssSetting(subs);
            rssTitleInput.setText("");
            rssUrlInput.setText("");
        }

    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("rss.conf", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("rss.conf");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
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


//            String linId = "" + layoutId;
//            int resID = getResources().getIdentifier(linId, "id", "com.septiadi.rssfetch");

            rssList.setId(layoutId);
            if(layoutId != 1) {
                rssList.setVisibility(View.GONE);
            }

            Button rssButton = null;
            rssButton = new Button(getApplicationContext());
            rssButton.setText(rssTitle);
            rssButton.setTag(rssTitle);

            rssButton.setBackgroundResource(R.drawable.button2);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams2.setMargins(10, 10, 10, 10);rssButton.setLayoutParams(layoutParams2);
            rssButton.setPadding(25, 25, 25, 25);

            rssButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    for (int  i = 1;i <= rssTotal; i++){
//                        String linId = "" + i;
//                        int resID = getResources().getIdentifier(linId, "id", "com.septiadi.rssfetch");

                        try {
                            LinearLayout hideLin = (LinearLayout) findViewById(i);

                            int t1 = layoutId;
                            int t2 = i;

                            if (layoutId != i) {
                                hideLin.setVisibility(View.GONE);
                            } else {
                                hideLin.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
                    addButton.setText(rssTitle + " : " + obj.getString("title"));
                    addButton.setTag(obj.getString("link"));

                    addButton.setBackgroundResource(R.drawable.button);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10, 10, 10, 0);addButton.setLayoutParams(layoutParams);
                    addButton.setPadding(25,25,25,25);

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

    public void openSetting(View view){
        Button closeButton = (Button) findViewById(R.id.closeDetail);
        closeButton.setVisibility(View.GONE);

        WebView mWebView = (WebView)findViewById(R.id.webView);
        mWebView.loadData("", "text/html", null);
        mWebView.setVisibility(View.GONE);

        LinearLayout ll = (LinearLayout) findViewById(R.id.setting);
        if(ll.getVisibility() == View.VISIBLE) {
            fetchingRss();
            ll.setVisibility(View.GONE);
        }else
            ll.setVisibility(View.VISIBLE);
    }

}
