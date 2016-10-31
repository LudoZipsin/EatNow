package com.example.ludovic.eatnow;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Ludovic Zipsin on 29/10/16.
 * Mail: ludovic.j.r.zipsin@gmail.com
 * Github: https://github.com/LudoZipsin
 */
public class EatNowApplication  extends Application {

    private static final String TAG = "EatNowApp";
    private static EatNowApplication appInstance;

    // db handler
    private DBHelper dbHelper;

    @Override
    public void onCreate(){
        super.onCreate();
        appInstance = this;
        dbHelper = new DBHelper(this);
        if (this.dbHelper.numberOfRowsUser() == 0){
            String userID = UUID.randomUUID().toString().replaceAll("-", "");
            this.dbHelper.insertUserID(userID);
        }
        Intent bgService = new Intent(this, BGService.class);
        startService(bgService);
    }

    public static synchronized EatNowApplication getAppInstance(){
        return appInstance;
    }


}