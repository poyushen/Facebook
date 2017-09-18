package laochanlam.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.action;


public class GlobalTouchService extends Service implements View.OnTouchListener{

    private String TAG = this.getClass().getSimpleName();
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;
    private long TimeCounter = 0;
    private long PrevTime = 0;
    private long CurrentTime=0;
    private String attractionString;
    private JSONArray attractionArray;
    private JSONObject[] attractionObjects=new JSONObject[5];
    private BroadcastReceiver mReceiver=new ScreenReceiver();
    public boolean screenOff;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private String[] imageIdString=new String[16];
    private int[] imageId=new int[16];


    @Override
    public int onStartCommand(Intent i, int flag, int id) {
        if(i!=null) {
            if(attractionString==null) {
                Bundle b = i.getExtras();
                attractionString = b.getString("attraction_key");
            }
        }
        screenOff=i.getBooleanExtra("screen_state",false);
        return START_REDELIVER_INTENT;
    }

    public void getAttractionObjects(){
        try
        {
            attractionArray = new JSONArray(attractionString);
        }catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate(){
        super.onCreate();
        spref=PreferenceManager.getDefaultSharedPreferences(this);
        editor=spref.edit();
        IntentFilter filter=new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver,filter);

        /**********************************Fake View****************************/
        touchLayout = new LinearLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams( WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        touchLayout.setLayoutParams(lp);
        touchLayout.setOnTouchListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                1,
                1,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);

        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowManager.addView(touchLayout , mParams);
        /**********************************Fake View****************************/


        for(int m=0;m<16;m++)
        {
            imageIdString[m]="a"+m;
        }

        for(int n=0;n<16;n++)
        {
            imageId[n]=getResources().getIdentifier(imageIdString[n],"drawable",this.getPackageName());
        }

    }

    @Override
    public void onDestroy(){
        if (mWindowManager != null){
            if (touchLayout != null)
                mWindowManager.removeView(touchLayout);
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    Message msg;
    private Button close;


    private void checkPoints(float total,String foregroundAppName) {
        long[] points={5000,10000,15000,20000,25000,30000,35000,40000,45000,50000,55000,60000,65000,70000,75000,80000,10000000};

        for (int i = 0; i < points.length; i++) {

            if (total>points[i] && total<points[i+1]) {

                /**********************************Send Message to Activity****************************/
                msg = new Message();
                msg.what = (int) total / 1000;
                MainActivity.handler.sendMessage(msg);
                /**********************************Send Message to Activity****************************/

                /**********************************AlertDialog****************************/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                try {
                    for (int a = 0; a < 16; a++) {

                        if ((msg.what * 70) == distance(attractionArray.getJSONObject(a).getDouble("Latitude"),attractionArray.getJSONObject(a).getDouble("Longitude"),24,121,"K")) {

                            LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View v=inflater.inflate(R.layout.custom_dialog,null);
                            ImageView image=(ImageView)v.findViewById(R.id.dialog_image);
                            image.setImageResource(imageId[a]);
                            builder.setTitle(R.string.app_name);
                            builder.setPositiveButton("關閉", null);
                            builder.setIcon(R.drawable.ic_launcher);
                            builder.setView(v);
                            builder.setMessage("Now using  "+foregroundAppName+"\nfor  " + msg.what + " s\ndistance  " + msg.what * 5 + " km。\narriving  " + attractionArray.getJSONObject(a).getString("Title"));
                            AlertDialog AlertDialog = builder.create();
                            AlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            AlertDialog.show();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /**********************************AlertDialog****************************/

                /**********************************Notification****************************/
                final int notifyID = 1;
                final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                final Notification notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("溫馨提示")
                        .setContentText("您已經滑動手機 " + msg.what + " 秒。")
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .setAutoCancel(true)
                        .build();
                notificationManager.notify(notifyID, notification);
                /**********************************Notification****************************/

                return;
            }
        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit){
        double theta=lon1-lon2;
        double dist=Math.sin(deg_to_rad(lat1))*Math.sin(deg_to_rad(lat2))+Math.cos(deg_to_rad(lat1))*Math.cos(deg_to_rad(lat2))*Math.cos(deg_to_rad(theta));
        dist=Math.acos(dist);
        dist=rad_to_deg(dist);
        dist=dist * 60 * 1.1515;
        if(unit=="K"){
            dist=dist*1.109344;
        }
        if(unit=="N"){
            dist=dist*0.8684;
        }
        return dist;
    }
    private static double deg_to_rad(double deg)
    {
        return (deg*Math.PI / 180.0);
    }
    private static double rad_to_deg(double rad)
    {
        return (rad*180.0 / Math.PI);
    }

    private long changetime=0;
    private float totaltime=0;

    @Override
    public boolean onTouch (View v , MotionEvent event){


        ActivityManager manager=(ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks=manager.getRunningAppProcesses();

        getAttractionObjects();

        CurrentTime = SystemClock.elapsedRealtime();
        changetime=CurrentTime-PrevTime;

        if(PrevTime!=0) {

            float prevAppTime = spref.getFloat(tasks.get(0).processName, 0);
            Log.e("prevAppTime", "" + prevAppTime);
            Log.e("changeAppTime", "" + changetime);

            editor.putFloat(tasks.get(0).processName, prevAppTime + changetime);
            editor.commit();
            float fbTime = spref.getFloat("com.facebook.katana", 0);
            float igTime = spref.getFloat("com.instagram.android", 0);

            totaltime = fbTime+igTime;
            Log.e("fb time", "" + fbTime);
            Log.e("ig time", "" + igTime);
            Log.e("total time",""+totaltime);
            checkPoints(totaltime, tasks.get(0).processName);
        }

        PrevTime=CurrentTime;

        return false;
    }
}