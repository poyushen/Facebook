package laochanlam.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;


public class MainActivity extends AppCompatActivity {

    Intent globalService;
    public static Handler handler;
    private String TAG = this.getClass().getSimpleName();

    public boolean ServiceFlag = false;
    private static MainActivity mainActivity;
    private ImageButton startButton;
    private TextView textMsg;
    public String attraction = "";
    public int[] time = new int[5];
    public int[] distance = new int[5];
    public int[] totalTime= new int[2];
    public int[] totalDis = new int[2];
    public int[] fbTime = new int[2];
    public int[] fbDis = new int[2];
    public int[] igTime = new int[2];
    public int[] igDis = new int[2];
    public int[] lineTime = new int[2];
    public int[] lineDis = new int[2];
    public int[] ytTime = new int[2];
    public int[] ytDis = new int[2];


    TextView TIME,DIS,FBTIME,FBDIS,IGTIME,IGDIS,LINETIME,LINEDIS,YTTIME,YTDIS;

    public static MainActivity getIns(){
        return mainActivity;

    }
    public void updateTextView(final TextView tv, final String text){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(text);
            }
        });
    }



    private BroadcastReceiver TimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            time[0] = (int)intent.getExtras().getFloat("time") / 1000;

            totalTime = convertTime(time[0]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(TIME, "  " + String.valueOf(totalTime[0]) + "             " + String.valueOf(totalTime[1]));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    private BroadcastReceiver DisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance[0] = (int)intent.getExtras().getFloat("dis")/150;

            totalDis = convertDistance(distance[0]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(DIS, String.valueOf(totalDis[0]) + "         " + String.valueOf(totalDis[1]));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    private BroadcastReceiver FbTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            time[1] = (int)intent.getExtras().getFloat("fbtime")/1000;

            fbTime = convertTime(time[1]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(FBTIME, String.valueOf(fbTime[0]) + "Hr    " + String.valueOf(fbTime[1]) + "Min");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver FbDisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance[1] = (int)intent.getExtras().getFloat("fbdis")/150;

            fbDis = convertDistance(distance[1]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(FBDIS, String.valueOf(fbDis[0]) + "Km  " + String.valueOf(fbDis[1]) + "M");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver IgTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            time[2] = (int)intent.getExtras().getFloat("igtime")/1000;


            igTime = convertTime(time[2]);
            Log.e("time",""+time[2]);
            Log.e("hr",""+igTime[0]);
            Log.e("min",""+igTime[1]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(IGTIME, String.valueOf(igTime[0]) + "Hr    " + String.valueOf(igTime[1]) + "Min");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver IgDisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance[2] = (int)intent.getExtras().getFloat("igdis")/150;

            igDis = convertDistance(distance[2]);

            //Log.e("km",""+igDis[0]);
            //Log.e("m",""+igDis[1]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(IGDIS, String.valueOf(igDis[0]) + "Km  " + String.valueOf(igDis[1]) + "M");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver LineTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            time[3] = (int)intent.getExtras().getFloat("linetime")/1000;

            lineTime = convertTime(time[3]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(LINETIME, String.valueOf(lineTime[0]) + "Hr    " + String.valueOf(lineTime[1]) + "Min");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver LineDisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance[3] = (int)intent.getExtras().getFloat("linedis")/150;

            lineDis = convertDistance(distance[3]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(LINEDIS, String.valueOf(lineDis[0]) + "Km  " + String.valueOf(lineDis[1]) + "M");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver YtTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            time[4] = (int)intent.getExtras().getFloat("yttime")/1000;

            ytTime = convertTime(time[4]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(YTTIME, String.valueOf(ytTime[0]) + "Hr    " + String.valueOf(ytTime[1]) + "Min");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private BroadcastReceiver YtDisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance[4] = (int)intent.getExtras().getFloat("ytdis")/150;

            ytDis = convertDistance(distance[4]);

            try{
                if(MainActivity.getIns() != null){
                    MainActivity.getIns().updateTextView(YTDIS, String.valueOf(ytDis[0]) + "Km  " + String.valueOf(ytDis[1]) + "M");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(TimeReceiver,new IntentFilter("time"));
        LocalBroadcastManager.getInstance(this).registerReceiver(DisReceiver,new IntentFilter("dis"));
        LocalBroadcastManager.getInstance(this).registerReceiver(FbTimeReceiver,new IntentFilter("fbtime"));
        LocalBroadcastManager.getInstance(this).registerReceiver(FbDisReceiver,new IntentFilter("fbdis"));
        LocalBroadcastManager.getInstance(this).registerReceiver(IgTimeReceiver,new IntentFilter("igtime"));
        LocalBroadcastManager.getInstance(this).registerReceiver(IgDisReceiver,new IntentFilter("igdis"));
        LocalBroadcastManager.getInstance(this).registerReceiver(LineTimeReceiver,new IntentFilter("linetime"));
        LocalBroadcastManager.getInstance(this).registerReceiver(LineDisReceiver,new IntentFilter("linedis"));
        LocalBroadcastManager.getInstance(this).registerReceiver(YtTimeReceiver,new IntentFilter("yttime"));
        LocalBroadcastManager.getInstance(this).registerReceiver(YtDisReceiver,new IntentFilter("ytdis"));

    }

    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(TimeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DisReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(FbTimeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(FbDisReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(IgTimeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(IgDisReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(LineTimeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(LineDisReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(YtTimeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(YtDisReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        Log.e("create","create");

        startButton = (ImageButton) findViewById(R.id.Startbutton);
        textMsg = (TextView) findViewById(R.id.textMsg);
        TIME = (TextView)findViewById(R.id.totalTime);
        DIS = (TextView)findViewById(R.id.totalDis);
        FBTIME = (TextView)findViewById(R.id.fbTime);
        FBDIS = (TextView)findViewById(R.id.fbDis);
        IGTIME = (TextView)findViewById(R.id.igTime);
        IGDIS = (TextView)findViewById(R.id.igDis);
        LINETIME=(TextView)findViewById(R.id.lineTime);
        LINEDIS=(TextView)findViewById(R.id.lineDis);
        YTTIME=(TextView)findViewById(R.id.ytTime);
        YTDIS=(TextView)findViewById(R.id.ytDis);


        read_json("attraction.json");//read json file


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
                globalService = new Intent(this, GlobalTouchService.class);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                //Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);

                Bundle bundle = new Bundle();//pass json data to service
                bundle.putString("attraction_key", attraction);
                bundle.putBoolean("reset",true);
                globalService.putExtras(bundle);

            }
            else
            {
                globalService = new Intent(this, GlobalTouchService.class);
                Bundle bundle = new Bundle();
                bundle.putString("attraction_key", attraction);
                bundle.putBoolean("reset",true);
                globalService.putExtras(bundle);
            }
        }
        else
        {   globalService = new Intent(this,GlobalTouchService.class);
            Bundle bundle = new Bundle();
            bundle.putString("attraction_key", attraction);
            bundle.putBoolean("reset",true);
            globalService.putExtras(bundle);
        }

    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    public void read_json(String filename) //read attractio json file//
    {
        BufferedReader reader=null;
        try{
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename)));
            String line = reader.readLine();

            while (line != null){
                attraction += line;
                line = reader.readLine();
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

    public void startButtonClicked(View v) {
        ServiceFlag = !ServiceFlag;
        if (ServiceFlag) {
            Log.i(TAG, "Start");
            startService(globalService);
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
            startButton.setImageResource(R.mipmap.stop);

        } else {
            Log.i(TAG, "Stop");
            stopService(globalService);
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            startButton.setImageResource(R.mipmap.start);

        }
    }

    public int[] convertTime(int time){
        int[] timeArray = new int[2];

        /*int day = (int)(time/86400);
        int hr = (int)((time - (day*86400))/3600);
        int min = (int)((time - (day*86400) - (hr*3600))/60);
        int sec = time - (day*86400) - (hr*3600) - (min * 60);*/

        int hr = (int)(time/3600);
        int min = (int)((time - (hr*3600))/60);

        timeArray[0] = hr;
        timeArray[1] = min;

        return timeArray;
    }

    public int[] convertDistance(int dis){
        int[] disArray = new int[2];
        int km = (int)(dis/100000);
        int m = (int)((dis - (km*1000))/100);

        disArray[0] = km;
        disArray[1] = m;

        return disArray;
    }

}