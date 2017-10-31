package laochanlam.app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.AppLaunchChecker;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.TimeUtils;
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class GlobalTouchService extends Service implements View.OnTouchListener{

    private WindowManager mWindowManager;
    private LinearLayout touchLayout;

    private String attractionString;
    private JSONArray attractionArray;
    private SharedPreferences timedisData;
    private SharedPreferences.Editor editor;

    private int[] imageId=new int[23];

    private long prevTime = 0;
    private long currentTime;
    private long changetime;

    @Override
    public int onStartCommand(Intent i, int flag, int id) {
        if(i != null) {
            if(attractionString == null) { //get string from Activity
                Bundle b = i.getExtras();
                attractionString = b.getString("attraction_key");
            }
        }
        return START_REDELIVER_INTENT;
    }

    public void buildArray(){
        try
        {
            attractionArray = new JSONArray(attractionString);//change string to array
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

        timedisData = PreferenceManager.getDefaultSharedPreferences(this);
        editor = timedisData.edit();

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

        for(int n = 0; n < 23; n++) {
            imageId[n] = getResources().getIdentifier("a" + n, "mipmap", this.getPackageName());
        }

    }

    @Override
    public void onDestroy(){
        if (mWindowManager != null){
            if (touchLayout != null)
                mWindowManager.removeView(touchLayout);
        }
        super.onDestroy();
    }


    Message msg;

    private void checkPoints(float totalTime, float totalDis) {

        /**********************************Send Message to Activity****************************/
        msg = new Message();
        msg.what = (int) totalTime / 1000;
        MainActivity.handler.sendMessage(msg);
        /**********************************Send Message to Activity****************************/

        /**********************************AlertDialog****************************/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        try {
            for (int a = 1; a < 23; a++) {
                if ((totalDis / 15000) >= attractionArray.getJSONObject(a).getDouble("distance")  && (totalDis / 15000) <= attractionArray.getJSONObject(a+1).getDouble("distance") && timedisData.getBoolean("imageid"+a, false) == false) {

                    editor.putBoolean("imageid"+a, true);
                    editor.putString("place",attractionArray.getJSONObject(a).getString("Title"));
                    editor.commit();

                    LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.custom_dialog,null);
                    ImageView image = (ImageView)v.findViewById(R.id.dialog_image);
                    image.setImageResource(imageId[a]);
                    builder.setTitle(R.string.app_name);
                    builder.setPositiveButton("關閉", null);
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setView(v);
                    builder.setMessage("Arriving " + attractionArray.getJSONObject(a).getString("Title"));
                    AlertDialog AlertDialog = builder.create();
                    AlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    AlertDialog.show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**********************************AlertDialog****************************/

        int cm = (int)(totalDis/150);
        int km = (int)(cm / 100000);
        int m = (int)((cm - (km * 100000))/100);

        /**********************************Notification****************************/
        final int notifyID = 1;
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.finger)
                .setContentTitle(timedisData.getString("place", ""))
                .setContentText(km + " Km " + m + " M ")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.finger))
                .setAutoCancel(true)
                .build();
        notificationManager.notify(notifyID, notification);
        /**********************************Notification****************************/

        return;

    }

    @Override
    public boolean onTouch (View v , MotionEvent event){

        UsageStatsManager um = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = 0;
        List<UsageStats>stats = um.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, beginTime, endTime);

        String app = "";
        String appName = "";

        if(stats != null){
            SortedMap<Long, UsageStats>sortedMap = new TreeMap<Long, UsageStats>();
            for(UsageStats us : stats){
                sortedMap.put(us.getLastTimeUsed(), us);
            }
            if(sortedMap != null && !sortedMap.isEmpty()){
                app = sortedMap.get(sortedMap.lastKey()).getPackageName();
            }
        }

        if(app.toLowerCase().contains("facebook".toLowerCase()))
            appName = "facebook";
        if(app.toLowerCase().contains("instagram".toLowerCase()))
            appName = "instagram";
        if(app.toLowerCase().contains("line".toLowerCase()))
            appName = "line";
        if(app.toLowerCase().contains("youtube".toLowerCase()))
            appName = "youtube";

        buildArray();

        currentTime = SystemClock.elapsedRealtime();
        changetime = currentTime - prevTime;

        if(prevTime != 0) {

            float prevAppTime = timedisData.getFloat(appName + "Time", 0);
            float prevAppDis = timedisData.getFloat(appName + "Dis", 0);


            if(changetime >= 10*60*1000){
                editor.putFloat(appName + "Time", prevAppTime + 30000);
                editor.putFloat(appName + "Dis", prevAppDis + (float)computeDis(30000));
                editor.putFloat("Time",timedisData.getFloat("Time", 0) + 30000);
                editor.putFloat("Dis", timedisData.getFloat("Dis", 0) + (float)computeDis(30000));
                editor.commit();
            }
            else if(changetime < 10*60*1000){
                editor.putFloat(appName + "Time", prevAppTime + changetime);
                editor.putFloat(appName + "Dis", prevAppDis + (float) computeDis(changetime));
                editor.putFloat("Time", timedisData.getFloat("Time", 0) + changetime);
                editor.putFloat("Dis", timedisData.getFloat("Dis", 0) + (float) computeDis(changetime));
                editor.commit();
            }

        }
        prevTime = currentTime;

        checkPoints(timedisData.getFloat("Time", 0), timedisData.getFloat("Dis", 0));

        sendBroadcast("time",timedisData.getFloat("Time", 0));
        sendBroadcast("dis",timedisData.getFloat("Dis", 0));
        sendBroadcast("fbtime",timedisData.getFloat("facebookTime", 0));
        sendBroadcast("fbdis", timedisData.getFloat("facebookDis", 0));
        sendBroadcast("igtime",timedisData.getFloat("instagramTime", 0));
        sendBroadcast("igdis",timedisData.getFloat("instagramDis", 0));
        sendBroadcast("linetime", timedisData.getFloat("lineTime", 0));
        sendBroadcast("linedis", timedisData.getFloat("lineDis", 0));
        sendBroadcast("yttime", timedisData.getFloat("youtubeTime", 0));
        sendBroadcast("ytdis", timedisData.getFloat("youtubeDis", 0));

        return false;
    }
    public void sendBroadcast(String key,float data){
        Intent intent = new Intent(key);
        intent.putExtra(key, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public double computeDis(double time)
    {
        double dis = 0;
        if(time < 21205)
            dis = time * 0.01 + 167.5;
        else if(time > 21205 && time < 52525)
            dis = time * 0.02 + 327.65;
        else if(time > 52525)
            dis = 1306.69;

        return dis;
    }
}
