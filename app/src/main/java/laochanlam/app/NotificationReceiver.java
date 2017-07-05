package laochanlam.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by poyushen on 2017/7/6.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c,Intent i)
    {
        Bundle receiveBundle=i.getExtras();
        int flag=receiveBundle.getInt("flag");
        if(flag==1)
        {
            c.startService(i);
        }
        if(flag==0)
        {
            c.stopService(i);
        }
    }
}
