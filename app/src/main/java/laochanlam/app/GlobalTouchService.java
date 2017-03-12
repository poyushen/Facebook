package laochanlam.app;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class GlobalTouchService extends Service implements View.OnTouchListener{


    private String TAG = this.getClass().getSimpleName();
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;

    private long TimeCounter = 0;
    private long PrevTime = 0;

    private JsonArray jsonArray=new JsonArray();
    private double distance1=0;
    private double distance2=0;
    private double distance3=0;
    private double distance4=0;
    private double distance5=0;

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
    private void compute(JsonArray array)
    {
        int lat1=0;
        int lat2=0;
        int lat3=0;
        int lat4=0;
        int lat5=0;
        int lon1=0;
        int lon2=0;
        int lon3=0;
        int lon4=0;
        int lon5=0;

        try {
            lat1 = jsonArray.jObject1.getInt("Latitude");
            lat2 = jsonArray.jObject2.getInt("Latitude");
            lat3 = jsonArray.jObject3.getInt("Latitude");
            lat4 = jsonArray.jObject4.getInt("Latitude");
            lat5 = jsonArray.jObject5.getInt("Latitude");

            lon1=jsonArray.jObject1.getInt("Longitude");
            lon2=jsonArray.jObject2.getInt("Longitude");
            lon3=jsonArray.jObject3.getInt("Longitude");
            lon4=jsonArray.jObject4.getInt("Longitude");
            lon5=jsonArray.jObject5.getInt("Longitude");

            distance1=Math.sqrt(Math.pow(lat1,2)+Math.pow(lon1,2));
            distance2=Math.sqrt(Math.pow(lat2,2)+Math.pow(lon2,2));
            distance3=Math.sqrt(Math.pow(lat3,2)+Math.pow(lon3,2));
            distance4=Math.sqrt(Math.pow(lat4,2)+Math.pow(lon4,2));
            distance5=Math.sqrt(Math.pow(lat5,2)+Math.pow(lon5,2));

            Log.e("dis",""+distance1);
            Log.e("dis",""+distance2);
            Log.e("dis",""+distance3);
            Log.e("dis",""+distance4);
            Log.e("dis",""+distance5);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

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
            if(msg.what*5==distance1){
                builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n已經滑動您手機" + msg.what * 5 + "單位。\n到達Nagasaki");}
            else if(msg.what*5==distance2){
                builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n已經滑動您手機" + msg.what * 5 + "單位。\n到達Hakata");}
            else if(msg.what*5==distance3){
                builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n已經滑動您手機" + msg.what * 5 + "單位。\n到達Kumamoto");}
            else if(msg.what*5==distance4){
                builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n已經滑動您手機" + msg.what * 5 + "單位。\n到達Oita");}
            else if(msg.what*5==distance5){
                builder.setMessage("您已經滑動手機 " + msg.what + " 秒。\n已經滑動您手機" + msg.what * 5 + "單位。\n到達Aso");}
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
        compute(jsonArray);

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