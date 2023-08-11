package com.quectel.appcontroller;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AppController";
    private static final String Settings_Access_Property = "persist.sys.settings.access";
    private static final int REQUEST_NETWORK_CODE = 0x100;
    private Button btn_open,btn_close;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_open = findViewById(R.id.btn_open);
        btn_close = findViewById(R.id.btn_lcose);
        btn_open.setOnClickListener(this);
        btn_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_open:
                //Open access
                EnableOrDisableSettings(true);
                break;
            case R.id.btn_lcose:
                //Close Access
                EnableOrDisableSettings(false);
                break;
        }
    }
    private void EnableOrDisableSettings(boolean bOn){
        set(Settings_Access_Property,bOn?"1":"0");
        Log.d(TAG,"Value:"+get(Settings_Access_Property,"null"));
    }
    public static void set(String key,String value){
        try{
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getMethod("set",String.class,String.class);
            method.invoke(null,key,value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String get(String key,String defValue) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getMethod("get", String.class, String.class);
            return (String) method.invoke(null, key, defValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[]permissions,int[]grantResults){
        if(requestCode == REQUEST_NETWORK_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                checkAvailable();
            }else{
                Toast.makeText(getApplicationContext(),"User canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void checkAvailable(){
        /*
        * Check whether we have permission to access network state.
        * */
        if(checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE},REQUEST_NETWORK_CODE);
            return;
        }
        /*
        * When network is not available,the app will exit.
        * */
        if(!checkNetworkAvailable(this)){
            Toast.makeText(getApplicationContext(),"The network is not available,please check it!",Toast.LENGTH_SHORT).show();
            finish();//Call this function to exit the app.
        }
    }
    private boolean checkNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(null == connectivityManager){
            return false;
        }
        NetworkInfo []networkInfos = connectivityManager.getAllNetworkInfo();
        if(networkInfos != null) {
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        Log.d(TAG, "The network type is WIFI");
                        return true;
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Log.d(TAG, "The network type is MOBILE");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}