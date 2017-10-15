package laochanlam.app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.List;


public class GlobalTouchService extends Service implements View.OnTouchListener{

    private String TAG = this.getClass().getSimpleName();
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;

    private String attractionString;
    private JSONArray attractionArray;
    private BroadcastReceiver screenReceiver = new ScreenReceiver();
    public boolean screenOff;
    private SharedPreferences timeData;
    private SharedPreferences.Editor editor;
    private int[] imageId=new int[23];

    private long prevTime = 0;
    private long currentTime = 0;
    private long changetime = 0;
    private float totaltime = 0;
    private float fbTime = 0;
    private float igTime = 0;
    private float totalDist_pixel = 0;
    private boolean reset;
    private boolean[] flag = new boolean[23];


    @Override
    public int onStartCommand(Intent i, int flag, int id) {
        if(i != null) {
            if(attractionString == null) {
                Bundle b = i.getExtras();
                attractionString = b.getString("attraction_key");
            }
        }

        screenOff = i.getBooleanExtra("screen_state", false);
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

        reset = false;
        fbTime = 0;
        igTime = 0;
        totaltime = 0;
        timeData = PreferenceManager.getDefaultSharedPreferences(this);
        editor = timeData.edit();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

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

        for(int n = 0; n < 23;n++) {
            imageId[n] = getResources().getIdentifier("a" + n, "mipmap", this.getPackageName());
            flag[n] = false;
        }

    }

    @Override
    public void onDestroy(){
        if (mWindowManager != null){
            if (touchLayout != null)
                mWindowManager.removeView(touchLayout);
        }
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }


    Message msg;

    private void checkPoints(float total, String foregroundAppName) {

        long[] points = {0, 5000,10000,15000,20000,25000,30000,35000,40000,45000,50000,55000,60000,65000,70000,75000,80000,85000,90000,95000,100000,105000,110000,115000,10000000};

        for (int i = 1; i < points.length - 1; i++) {

            if (total > points[i] && total < points[i+1]) {

                /**********************************Send Message to Activity****************************/
                msg = new Message();
                msg.what = (int) total / 1000;
                MainActivity.handler.sendMessage(msg);
                /**********************************Send Message to Activity****************************/

                /**********************************AlertDialog****************************/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                try {
                    for (int a = 1; a < 23; a++) {

                        if ((totalDist_pixel * 1.5) >= attractionArray.getJSONObject(a).getDouble("distance") * 100000 && (totalDist_pixel * 1.5) <= attractionArray.getJSONObject(a+1).getDouble("distance") * 100000 && flag[a] == false) {
                            flag[a] = true;

                            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View v = inflater.inflate(R.layout.custom_dialog,null);
                            ImageView image = (ImageView)v.findViewById(R.id.dialog_image);
                            image.setImageResource(imageId[a]);
                            builder.setTitle(R.string.app_name);
                            builder.setPositiveButton("關閉", null);
                            builder.setIcon(R.drawable.ic_launcher);
                            builder.setView(v);
                            builder.setMessage("Now using  " + foregroundAppName + "\nfor  " + msg.what + " s\ndistance  " + msg.what * 5 + " km。\narriving  " + attractionArray.getJSONObject(a).getString("Title"));
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

    @Override
    public boolean onTouch (View v , MotionEvent event){

        ActivityManager manager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();

        getAttractionObjects();

        currentTime = SystemClock.elapsedRealtime();
        changetime = currentTime - prevTime;

        if(changetime < 21205)
        {
            totalDist_pixel += 0.01*changetime + 167.5;
            Log.e("1", ""+0.01*changetime + 167.5);
        }
        else if(changetime > 21205 && changetime < 52525)
        {
            totalDist_pixel += 0.02*changetime +327.65;
        }
        else if(changetime > 52525)
        {
            totalDist_pixel += 1306.69;
        }

        if(prevTime != 0) {
            if(reset == false)
            {
                editor.putFloat(tasks.get(0).processName, 0);
                editor.commit();
                fbTime = timeData.getFloat("com.facebook.katana", 0);
                igTime = timeData.getFloat("com.instagram.android", 0);
                reset = true;
            }
            else {
                float prevAppTime = timeData.getFloat(tasks.get(0).processName, 0);

                editor.putFloat(tasks.get(0).processName, prevAppTime + changetime);
                editor.commit();
                fbTime = timeData.getFloat("com.facebook.katana", 0);
                igTime = timeData.getFloat("com.instagram.android", 0);

                totaltime = fbTime + igTime;
                checkPoints(totaltime, tasks.get(0).processName);
            }
        }
        prevTime = currentTime;

        Log.e("dis",""+totalDist_pixel);
        return false;
    }
}