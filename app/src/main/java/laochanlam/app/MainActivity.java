package laochanlam.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {

    Intent globalService;
    public static Handler handler;
    private String TAG = this.getClass().getSimpleName();
    boolean ServiceFlag = false;
    private Button startButton;
    private Button moreButton;
    private TextView textMsg;
    public String attraction = "";
    private long escapeTime = 0;
    private Chronometer chronometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.Startbutton);
        moreButton = (Button) findViewById(R.id.more);
        textMsg = (TextView) findViewById(R.id.textMsg);
        chronometer = (Chronometer)findViewById(R.id.chronometer) ;

        read_attraction();//read json file


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                textMsg.append("\n您已經滑動手機 " + msg.what + " 秒。");
            }
        };//thread

        if (Build.VERSION.SDK_INT >= 23) { // Version
            if (!Settings.canDrawOverlays(getApplicationContext()))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);

                Bundle attraction_bundle = new Bundle();//pass json data to service
                attraction_bundle.putString("attraction_key", attraction);
                globalService.putExtras(attraction_bundle);

            }
            else
            {
                globalService = new Intent(this, GlobalTouchService.class);
                Bundle attraction_bundle = new Bundle();
                attraction_bundle.putString("attraction_key", attraction);
                globalService.putExtras(attraction_bundle);
            }
        }
        else
        {   globalService = new Intent(this,GlobalTouchService.class);
            Bundle attraction_bundle = new Bundle();
            attraction_bundle.putString("attraction_key", attraction);
            globalService.putExtras(attraction_bundle);
        }

    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public void read_attraction()
    {
        BufferedReader reader=null;
        try{
            reader = new BufferedReader(new InputStreamReader(getAssets().open("attraction.json")));
            String attractionLine = reader.readLine();

            while (attractionLine != null){
                attraction += attractionLine;
                attractionLine = reader.readLine();
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }finally {
            if(reader != null)
            {
                try {
                    reader.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void moreButtonClicked(View v){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, Information.class);//to information page
        startActivity(intent);
    }

    public void startButtonClicked(View v) {
        ServiceFlag = !ServiceFlag;
        if (ServiceFlag) {
            Log.i(TAG, "Start");
            startService(globalService);
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
            startButton.setText("Stop");

            chronometer.setBase(SystemClock.elapsedRealtime() + escapeTime);//start timer
            chronometer.start();

        } else {
            Log.i(TAG, "Stop");
            stopService(globalService);
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            startButton.setText("Start");

            escapeTime = chronometer.getBase() - SystemClock.elapsedRealtime();//reset base
            chronometer.stop();

        }
    }


}