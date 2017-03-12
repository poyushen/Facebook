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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private JsonArray jsonArray;


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

        jsonArray=new JsonArray();

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
