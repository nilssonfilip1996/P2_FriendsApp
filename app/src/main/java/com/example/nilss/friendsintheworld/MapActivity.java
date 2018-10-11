package com.example.nilss.friendsintheworld;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private GoogleMap mMap;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int REQUEST_TAKE_THUMBNAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
/*        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }*/
        initListeners();
    }

    private void initListeners() {
        FloatingActionButton fbMsg = findViewById(R.id.floatingActionButtonMsg);
        fbMsg.setOnClickListener((View v)->{
            Intent intent = new Intent(MapActivity.this, GroupActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void addMarker(LatLng latLng){
        MarkerOptions mo = new MarkerOptions().position(latLng).title("My position");
        mMap.addMarker(mo);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3));
    }
}
