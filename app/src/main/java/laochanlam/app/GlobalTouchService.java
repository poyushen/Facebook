package laochanlam.app;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import org.json.JSONObject;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
    private JSONObject info_obj = new JSONObject();

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
        try {
            attractionArray = new JSONArray(attractionString);//change string to array
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
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
    public void onDestroy() {
        if (mWindowManager != null) {
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
    public boolean onTouch (View v , MotionEvent event) {

        //get foreground app
        UsageStatsManager um = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = 0;
        List<UsageStats>stats = um.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, beginTime, endTime);

        String app = "";
        String appName = "";

        if(stats != null) {
            SortedMap<Long, UsageStats>sortedMap = new TreeMap<Long, UsageStats>();
            for(UsageStats us : stats) {
                sortedMap.put(us.getLastTimeUsed(), us);
            }
            if(sortedMap != null && !sortedMap.isEmpty()) {
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

            if(changetime >= 10*60*1000) {
                editor.putFloat(appName + "Time", timedisData.getFloat(appName + "Time", 0) + 30000);
                editor.putFloat(appName + "Dis", timedisData.getFloat(appName + "Dis", 0) + (float)computeDis(30000));
                editor.putFloat("Time",timedisData.getFloat("Time", 0) + 30000);
                editor.putFloat("Dis", timedisData.getFloat("Dis", 0) + (float)computeDis(30000));
                editor.commit();
            }
            else if(changetime < 10*60*1000) {
                editor.putFloat(appName + "Time", timedisData.getFloat(appName + "Time", 0) + changetime);
                editor.putFloat(appName + "Dis", timedisData.getFloat(appName + "Dis", 0) + (float) computeDis(changetime));
                editor.putFloat("Time", timedisData.getFloat("Time", 0) + changetime);
                editor.putFloat("Dis", timedisData.getFloat("Dis", 0) + (float) computeDis(changetime));
                editor.commit();

            }
        }
        prevTime = currentTime;
        checkPoints(timedisData.getFloat("Time", 0), timedisData.getFloat("Dis", 0));

        JSONObject totaltime_obj = new JSONObject();
        JSONObject fbtime_obj = new JSONObject();
        JSONObject igtime_obj = new JSONObject();
        JSONObject linetime_obj = new JSONObject();
        JSONObject yttime_obj = new JSONObject();
        JSONObject time_obj = new JSONObject();

        JSONObject totaldis_obj = new JSONObject();
        JSONObject fbdis_obj = new JSONObject();
        JSONObject igdis_obj = new JSONObject();
        JSONObject linedis_obj = new JSONObject();
        JSONObject ytdis_obj = new JSONObject();
        JSONObject dis_obj = new JSONObject();
        try {
            totaltime_obj.put("hr",convertTime((timedisData.getFloat("Time", 0))/1000)[0]);
            totaltime_obj.put("min",convertTime((timedisData.getFloat("Time", 0))/1000)[1]);
            totaltime_obj.put("sec",convertTime((timedisData.getFloat("Time", 0))/1000)[2]);
            fbtime_obj.put("hr",convertTime((timedisData.getFloat("facebookTime", 0))/1000)[0]);
            fbtime_obj.put("min",convertTime((timedisData.getFloat("facebookTime", 0))/1000)[1]);
            fbtime_obj.put("sec",convertTime((timedisData.getFloat("facebookTime", 0))/1000)[2]);
            igtime_obj.put("hr",convertTime((timedisData.getFloat("instagramTime", 0))/1000)[0]);
            igtime_obj.put("min",convertTime((timedisData.getFloat("instagramTime", 0))/1000)[1]);
            igtime_obj.put("sec",convertTime((timedisData.getFloat("instagramTime", 0))/1000)[2]);
            linetime_obj.put("hr",convertTime((timedisData.getFloat("lineTime", 0))/1000)[0]);
            linetime_obj.put("min",convertTime((timedisData.getFloat("lineTime", 0))/1000)[1]);
            linetime_obj.put("sec",convertTime((timedisData.getFloat("lineTime", 0))/1000)[2]);
            yttime_obj.put("hr",convertTime((timedisData.getFloat("youtubeTime", 0))/1000)[0]);
            yttime_obj.put("min",convertTime((timedisData.getFloat("youtubeTime", 0))/1000)[1]);
            yttime_obj.put("sec",convertTime((timedisData.getFloat("youtubeTime", 0))/1000)[2]);

            time_obj.put("total", totaltime_obj);
            time_obj.put("fb", fbtime_obj);
            time_obj.put("ig", igtime_obj);
            time_obj.put("line", linetime_obj);
            time_obj.put("youtube", yttime_obj);

            totaldis_obj.put("km", convertDis((timedisData.getFloat("Dis", 0))/150)[0]);
            totaldis_obj.put("m", convertDis((timedisData.getFloat("Dis", 0))/150)[1]);
            totaldis_obj.put("cm", convertDis((timedisData.getFloat("Dis", 0))/150)[2]);
            fbdis_obj.put("km", convertDis((timedisData.getFloat("facebookDis", 0))/150)[0]);
            fbdis_obj.put("m", convertDis((timedisData.getFloat("facebookDis", 0))/150)[1]);
            fbdis_obj.put("cm", convertDis((timedisData.getFloat("facebookDis", 0))/150)[2]);
            igdis_obj.put("km", convertDis((timedisData.getFloat("instagramDis", 0))/150)[0]);
            igdis_obj.put("m", convertDis((timedisData.getFloat("instagramDis", 0))/150)[1]);
            igdis_obj.put("cm", convertDis((timedisData.getFloat("instagramDis", 0))/150)[2]);
            linedis_obj.put("km", convertDis((timedisData.getFloat("lineDis", 0))/150)[0]);
            linedis_obj.put("m", convertDis((timedisData.getFloat("lineDis", 0))/150)[1]);
            linedis_obj.put("cm", convertDis((timedisData.getFloat("lineDis", 0))/150)[2]);
            ytdis_obj.put("km", convertDis((timedisData.getFloat("youtubeDis", 0))/150)[0]);
            ytdis_obj.put("m", convertDis((timedisData.getFloat("youtubeDis", 0))/150)[1]);
            ytdis_obj.put("cm", convertDis((timedisData.getFloat("youtubeDis", 0))/150)[2]);

            dis_obj.put("total", totaldis_obj);
            dis_obj.put("fb", fbdis_obj);
            dis_obj.put("ig", igdis_obj);
            dis_obj.put("line", linedis_obj);
            dis_obj.put("youtube", ytdis_obj);

            info_obj.put("time", time_obj);
            info_obj.put("dist", dis_obj);



        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e("obj", ""+info_obj);

        sendBroadcast("info", info_obj.toString());

        return false;
    }
    public void sendBroadcast(String key,String data) {
        Intent intent = new Intent(key);
        intent.putExtra(key, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public double computeDis(double time) {
        double dis = 0;
        if(time < 21205)
            dis = time * 0.01 + 167.5;
        else if(time > 21205 && time < 52525)
            dis = time * 0.02 + 327.65;
        else if(time > 52525)
            dis = 1306.69;

        return dis;
    }
    public int[] convertTime(float time){
        int[] timeArray = new int[3];
        timeArray[0] = (int)(time / 3600);
        timeArray[1] = (int)((time - (timeArray[0]*3600))/60);
        timeArray[2] = (int)(time - (timeArray[0]*3600) - (timeArray[1]*60));
        return timeArray;
    }
    public int[] convertDis(float dist){
        int[] disArray = new int[3];
        disArray[0] = (int)(dist / 100000);
        disArray[1] = (int)((dist - (disArray[0]*100000)) / 100);
        disArray[2] = (int)(dist - (disArray[0]*100000) - (disArray[1]*100));
        return disArray;
    }
}