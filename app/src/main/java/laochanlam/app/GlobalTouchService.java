package laochanlam.app;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
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
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;


public class GlobalTouchService extends Service implements View.OnTouchListener{


    private String TAG = this.getClass().getSimpleName();
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;

    private long TimeCounter = 0;
    private long PrevTime = 0;

    /*public MyBinder mybinder=new MyBinder();

    public class MyBinder extends Binder {
        public GlobalTouchService getService(){
            return GlobalTouchService.this;
        }
    }*/

    private String jarray="[\n" +
            "{\"Id\":\"1\",\"Title\":\"Nagasaki\",\"Latitude\":\"20\",\"Longitude\":\"15\",\"Description\":\"Here is Nagasaki\"},\n" +
            "{\"Id\":\"2\",\"Title\":\"Hakata\",\"Latitude\":\"32\",\"Longitude\":\"24\",\"Description\":\"Here is Hakata\"},\n" +
            "{\"Id\":\"3\",\"Title\":\"Kumamoto\",\"Latitude\":\"40\",\"Longitude\":\"30\",\"Description\":\"Here is Kumamoto\"},\n" +
            "{\"Id\":\"4\",\"Title\":\"Oita\",\"Latitude\":\"80\",\"Longitude\":\"60\",\"Description\":\"Here is Oita\"},\n" +
            "{\"Id\":\"5\",\"Title\":\"Aso\",\"Latitude\":\"120\",\"Longitude\":\"90\",\"Description\":\"Here is Aso\"}\n" +
            "]";

    public JSONArray jsonArray;
    public JSONObject[] jsonObjects=new JSONObject[5];
    public int[] latitude=new int[5];
    public int[] longitude=new int[5];
    public double[] distance=new double[5];
    public String[] id=new String [5];
    public String[] title=new String[5];
    public String[] description=new String[5];


    public void compute(){
        try
        {
            jsonArray = new JSONArray(jarray);
            for(int a=0;a<jsonArray.length();a++) {
                jsonObjects[a]=jsonArray.getJSONObject(a);
                latitude[a]=jsonObjects[a].getInt("Latitude");
                longitude[a]=jsonObjects[a].getInt("Longitude");
                distance[a]=Math.sqrt(Math.pow(latitude[a],2)+Math.pow(longitude[a],2));
                id[a]=jsonObjects[a].getString("Id");
                title[a]=jsonObjects[a].getString("Title");
                description[a]=jsonObjects[a].getString("Description");
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
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
        }

        super.onDestroy();
    }

    Message msg;


    private void checkPoints(long change) {
        long[] points = {5000, 10000, 30000};
        TimeCounter += change;
        Log.i(TAG, TimeCounter + " " + change);
        for (int i = 0; i < points.length; i++) {
            if (TimeCounter < points[i]) return;
            if (TimeCounter - change > points[i]) continue;
            /**********************************Send Message to Activity****************************/
            msg = new Message();
            msg.what = (int) points[i] / 1000;
            MainActivity.handler.sendMessage(msg);
            /**********************************Send Message to Activity****************************/

            /**********************************AlertDialog****************************/

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setPositiveButton("關閉", null);
            builder.setIcon(R.drawable.ic_launcher);
            for(int a=0;a<jsonArray.length();a++) {
                if (distance[a] == msg.what * 5)
                    builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n您已經滑動手機"+msg.what*5+"單位。\n已經到達"+title[a]);
            }
            //builder.setMessage("您已經滑動手機 " + msg.what + " 秒。");
            AlertDialog AlertDialog = builder.create();
            AlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            AlertDialog.show();

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


    @Override
    public boolean onTouch (View v , MotionEvent event){

        long CurrentTime = SystemClock.elapsedRealtime();
        compute();

        if (CurrentTime - PrevTime < 30000) {// session gap = 30s
            checkPoints(CurrentTime - PrevTime);
        }
        PrevTime = CurrentTime;

        Log.i("event",Float.toString(event.getY()));

        Log.i(TAG, "Touch event: " + event.toString());
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            Log.i(TAG, Objects.toString(TimeCounter, null));
            Log.i(TAG, "Action" + event.getAction() + "\t X:" + event.getRawX() + "\t Y:" + event.getRawY());
        }

        return false;
    }



}