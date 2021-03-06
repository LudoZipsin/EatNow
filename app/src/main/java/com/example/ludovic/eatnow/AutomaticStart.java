package com.example.ludovic.eatnow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Ludovic Zipsin on 30/10/16.
 * Mail: ludovic.j.r.zipsin@gmail.com
 * Github: https://github.com/LudoZipsin
 */
public class AutomaticStart extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent){
        Intent serviceIntent = new Intent(context, BGService.class);
        context.startService(serviceIntent);
        Log.i("AutomaticStart", "BGService started");
    }

}
