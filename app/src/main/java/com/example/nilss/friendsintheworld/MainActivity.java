package com.example.nilss.friendsintheworld;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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
    protected void onDestroy() {
        if(TCPServiceIsBound) {
            connection.disconnect();
            unbindService(serviceConn);
            TCPServiceIsBound =false;
        }
        super.onDestroy();
    }

    private void initMap() {
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
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
            TCPServiceIsBound = true;
            Log.d(TAG, "connection: " + String.valueOf(connection!=null));
        }

        public void onServiceDisconnected(ComponentName arg0) {
            TCPServiceIsBound=false;
        }
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
