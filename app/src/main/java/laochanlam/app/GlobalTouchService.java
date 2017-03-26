package laochanlam.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Objects;


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

    @Override
    public int onStartCommand(Intent i, int flag, int id) {
        Bundle b = i.getExtras();
        attractionString=b.getString("attraction_key");
        return START_STICKY;
    }

    public void getAttractionObjects(){
        try
        {
            attractionArray = new JSONArray(attractionString);
            for(int a=0;a<attractionArray.length();a++) {
                attractionObjects[a]=attractionArray.getJSONObject(a);
            }
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

        /**********************************Fake View****************************/
        touchLayout = new LinearLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams( 1,1);
        touchLayout.setLayoutParams(lp);
        touchLayout.setBackgroundColor(Color.GREEN);
        touchLayout.setOnTouchListener(this);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                1,
                1,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);

        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        Log.i(TAG, "add View");

        mWindowManager.addView(touchLayout , mParams);
        /**********************************Fake View****************************/

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

    private void checkPoints(long total,String foregroundAppName) {
        long[] points = {5000, 10000, 30000,1000000000};

        for (int i = 0; i < points.length; i++) {

            if (total>points[i] && total<points[i+1]) {

                /**********************************Send Message to Activity****************************/
                msg = new Message();
                msg.what = (int) total / 1000;
                MainActivity.handler.sendMessage(msg);
                /**********************************Send Message to Activity****************************/

                /**********************************AlertDialog****************************/

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                Log.e("totaltime", "" + msg.what);
                try {
                    for (int a = 0; a < attractionArray.length(); a++) {
                        if ((msg.what * 5) == Math.sqrt(Math.pow(attractionArray.getJSONObject(a).getInt("Latitude"), 2) + Math.pow(attractionArray.getJSONObject(a).getInt("Longitude"), 2))) {
                            builder.setTitle(R.string.app_name);
                            builder.setPositiveButton("關閉", null);
                            builder.setIcon(R.drawable.ic_launcher);
                            builder.setMessage("正在使用"+foregroundAppName+"\n您已經滑動手機 " + msg.what + " 秒。\n您已經滑動手機" + msg.what * 5 + "單位。\n已經到達" + attractionArray.getJSONObject(a).getString("Title"));
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

    private long changetime=0;
    private long totaltime=0;
    @Override
    public boolean onTouch (View v , MotionEvent event){

        ActivityManager manager=(ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> tasks=manager.getRunningAppProcesses();
        Log.e("serviceappname",tasks.get(0).processName);

        CurrentTime = SystemClock.elapsedRealtime();
        getAttractionObjects();
        changetime=CurrentTime-PrevTime;

        if(PrevTime!=0) {
            totaltime += changetime;
            checkPoints(totaltime,tasks.get(0).processName);
        }
        PrevTime=CurrentTime;

        Log.i("event",Float.toString(event.getY()));

        Log.i(TAG, "Touch event: " + event.toString());
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            Log.i(TAG, Objects.toString(TimeCounter, null));
            Log.i(TAG, "Action" + event.getAction() + "\t X:" + event.getRawX() + "\t Y:" + event.getRawY());
        }
        return false;
    }
}