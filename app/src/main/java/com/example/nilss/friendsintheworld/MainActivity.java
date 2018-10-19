package com.example.nilss.friendsintheworld;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nilss.friendsintheworld.Pojos.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static final String IPADDR = "195.178.227.53";
    public static final String PORTNBR = "7117";
    private static final int NOT_CONNECTED = 0;
    private static final int IS_CONNECTED = 1;
    private TCPConnection connection;
    private ServiceConn serviceConn;
    private boolean TCPServiceIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTCPConnection(savedInstanceState);
        if(isServiceOk()){
            initMap();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case(R.id.item_eng):
                setLocale("en");
                Log.d(TAG, "onOptionsItemSelected: EN selected");
                return true;
            case(R.id.item_swe):
                setLocale("sv-rSE");
                Log.d(TAG, "onOptionsItemSelected: SV selected");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setLocale(String lang) {
/*        Locale locale = new Locale(lang);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());*/
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(lang.toLowerCase())); // API 17+ only.
        // Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onDestroy() {
        if(TCPServiceIsBound) {
            Log.d(TAG, "onDestroy: ");
            connection.disconnect();
            unbindService(serviceConn);
            TCPServiceIsBound =false;
        }
        super.onDestroy();
    }

    private void initMap() {
        //EditText userNameEtv = (EditText) findViewById(R.id.usernameEtv);
        Button btnMap = (Button) findViewById(R.id.proceedBtn);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
/*                if(userNameEtv.getText().equals("")) {
                    Toast.makeText(MainActivity.this, "Please specify a username!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }*/

            }
        });
    }

    private void initTCPConnection(Bundle savedInstanceState){
        Intent intent = new Intent(MainActivity.this, TCPConnection.class);
        intent.putExtra(TCPConnection.IP, IPADDR);
        intent.putExtra(TCPConnection.PORT, PORTNBR);
        if(savedInstanceState==null)
            startService(intent);
        serviceConn = new ServiceConn();
        boolean result = bindService(intent, serviceConn, 0);
        if (!result)
            Log.d("TCPConnection: ", "No binding");
    }

    private class ServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPConnection.LocalService ls = (TCPConnection.LocalService) binder;
            connection = ls.getService();
            connection.connect();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            } else {
                connection.startLocationHandler(MainActivity.this,REQUEST_ACCESS_FINE_LOCATION);
            }

            //connection.startDummy();
            TCPServiceIsBound = true;
            Log.d(TAG, "connection: " + String.valueOf(connection!=null));
        }

        public void onServiceDisconnected(ComponentName arg0) {
            TCPServiceIsBound=false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        connection.startLocationHandler(this,requestCode);
    }

    public boolean isServiceOk(){
        Log.d(TAG, "isServiceOk: checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available== ConnectionResult.SUCCESS){
            Log.d(TAG, "isServiceOk: Google play services are working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServiceOk: An error occured but we can fix it");
            Dialog diablog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            diablog.show();
        }
        else{
            Toast.makeText(this,"We can't make map requests", Toast.LENGTH_SHORT);
        }
        return false;
    }
}
