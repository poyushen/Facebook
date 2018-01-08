package laochanlam.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    Intent globalService;
    public static Handler handler;
    public boolean ServiceFlag = false;
    public WebView webView;
    public String attraction = "";
    public String info;
    public String TAG = this.getClass().getSimpleName();
    public TextView textMsg;

    private BroadcastReceiver InfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            info = intent.getExtras().getString("info");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(InfoReceiver,new IntentFilter("info"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(InfoReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView);
        textMsg = (TextView) findViewById(R.id.textMsg);

        read_json("attraction.json");//read json file

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                textMsg.append("\n您已經滑動手機 " + msg.what + " 秒。");
            }
        };//thread

        webView.getSettings().setJavaScriptEnabled(true);
        //webView.loadUrl("https://merry.ee.ncku.edu.tw/~poyushen/test");
        webView.loadUrl("http://merry.ee.ncku.edu.tw:20000/");
        webView.addJavascriptInterface(new WebViewInterface(this), "AndroidInterface");

        if (Build.VERSION.SDK_INT >= 23) { // Version
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                globalService = new Intent(this, GlobalTouchService.class);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);

                Bundle bundle = new Bundle();//pass json data to service
                bundle.putString("attraction_key", attraction);
                globalService.putExtras(bundle);
            } else {
                globalService = new Intent(this, GlobalTouchService.class);
                Bundle bundle = new Bundle();
                bundle.putString("attraction_key", attraction);
                globalService.putExtras(bundle);
            }
        } else {
            globalService = new Intent(this,GlobalTouchService.class);
            Bundle bundle = new Bundle();
            bundle.putString("attraction_key", attraction);
            globalService.putExtras(bundle);
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public void read_json(String filename) { //read attraction json file//
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));
            String line = reader.readLine();

            while (line != null){
                attraction += line;
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null) {
                try {
                    reader.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class WebViewInterface{
        Context mContext;
        WebViewInterface(Context c){
            mContext = c;
        }

        @JavascriptInterface
        public void StartService(boolean b){
            ServiceFlag = b;
            if(ServiceFlag){
                Log.e(TAG, "start");
                startService(globalService);
            }else {
                Log.e(TAG, "stop");
                stopService(globalService);
            }
        }

        @JavascriptInterface
        public String getInfo(){
            return info;
        }
    }

}