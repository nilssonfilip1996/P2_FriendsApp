package com.example.nilss.friendsintheworld;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int TAG_REQUEST = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int REQUEST_TAKE_THUMBNAIL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initMap();
        initListeners();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void initListeners() {
        FloatingActionButton fbMsg = findViewById(R.id.floatingActionButtonMsg);
        fbMsg.setOnClickListener((View v)->{
            Intent intent = new Intent(MapActivity.this, GroupActivity.class);
            startActivityForResult(intent, TAG_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: ACTIVITY RETURNED!");
        mMap.clear(); //remove previous pins
        boolean moreThanOneCoordinate = false;
        if(requestCode==TAG_REQUEST && resultCode== Activity.RESULT_OK){
            ArrayList<String> coordinateList = data.getStringArrayListExtra("pins");
            if(coordinateList.size()==0){
                return;
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            MarkerOptions options = null;
            for (int i = 0; i < coordinateList.size(); i+=3) {
                Log.d(TAG, "onActivityResult: pin"+i+" : LONG: "+ coordinateList.get(i+2)+ ", LAT: "+ coordinateList.get(i+1));
                String memberName = coordinateList.get(i);
                LatLng latLng = new LatLng(Double.valueOf(coordinateList.get(i+2)),Double.valueOf(coordinateList.get(i+1)));
                //Place marker!
                //moveCamera(test,DEFAULT_ZOOM,"Test");
                options = new MarkerOptions()
                        .position(latLng)
                        .title(memberName);
                mMap.addMarker(options);
                builder.include(latLng);
                if(i>=3){
                    moreThanOneCoordinate=true;
                }
            }
            if(moreThanOneCoordinate) {
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (height * 0.20); // offset from edges of the map 10% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                mMap.animateCamera(cu);
            }
            else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(options.getPosition(),12f));
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
