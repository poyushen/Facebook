package laochanlam.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {

    Intent globalService;
    public static Handler handler;
    RelativeLayout relativeLayout;
    private String TAG = this.getClass().getSimpleName();
    RelativeLayout.LayoutParams ViewLayoutParams;
    boolean ServiceFlag = false;
    Stack ViewItem = new Stack();
    private Button startButton;
    private TextView textMsg;
    public String jjarray="";
    public GlobalTouchService touchService;


    TextView view_for_5sec,view_for_10sec,view_for_30sec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.Startbutton);
        textMsg = (TextView) findViewById(R.id.textMsg);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                textMsg.append("\n您已經滑動手機 " + msg.what + " 秒。");
            }
        };

        if (Build.VERSION.SDK_INT >= 23) { // Version
            if (!Settings.canDrawOverlays(getApplicationContext()))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
            else
                globalService = new Intent(this, GlobalTouchService.class);
        } else globalService = new Intent(this,GlobalTouchService.class);

        readfile();
        
        //Intent serviceIntent=new Intent(this,GlobalTouchService.class);
        //this.bindService(serviceIntent,connection,Context.BIND_AUTO_CREATE);

    }

   /* public ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            touchService=((GlobalTouchService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };*/



    public void readfile()
    {
        BufferedReader reader=null;
        try{
            reader =new BufferedReader(new InputStreamReader(getAssets().open("jarray.json")));
            String mLine=reader.readLine();

            while (mLine!=null){
                jjarray+=mLine;
                mLine=reader.readLine();
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }finally {
            if(reader!=null)
            {
                try {
                    reader.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        Log.e("read",jjarray);

        try {
            JSONArray jsonArray = new JSONArray(jjarray);

        }catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    public void StartbuttonClicked(View v){
        ServiceFlag = !ServiceFlag;
        if (ServiceFlag) {
            Log.i(TAG, "Start");
            startService(globalService);
            Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
            startButton.setText("Stop");
        } else {
            Log.i(TAG, "Stop");
            stopService(globalService);
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            startButton.setText("Start");
        }
    }

    public void ResetButtonClicked(View v){
        if (ServiceFlag) {
            ServiceFlag = false;
            while (!ViewItem.empty())
            {
                relativeLayout.removeView((View)ViewItem.peek());
                ViewItem.pop();
            }
            Log.i(TAG, "Stop");
            stopService(globalService);
            Toast.makeText(this, "Stop Service", Toast.LENGTH_SHORT).show();
            startButton.setText("Start");
            textMsg.setText("Hello");
        }
    }

}
