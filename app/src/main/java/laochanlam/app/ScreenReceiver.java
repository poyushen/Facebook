package laochanlam.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by poyushen on 2017/4/15.
 */

public class ScreenReceiver extends BroadcastReceiver {
    public boolean screenoff;

    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            screenoff=true;
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            screenoff=false;
        }
        Intent i=new Intent(context,GlobalTouchService.class);
        i.putExtra("screen_state",screenoff);
        if(screenoff==false) {
            context.startService(i);
        }
        if(screenoff==true)
        {
            context.stopService(i);
        }
    }
}
