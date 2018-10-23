package com.example.nilss.friendsintheworld;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int TAG_REQUEST = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int REQUEST_TAKE_THUMBNAIL = 2;
    private ArrayList<String> coordinateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initMap();
        initListeners();
        //coordinateList = getArrayList("pins");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(coordinateList!=null) {
            saveArrayList(coordinateList, "pins");
        }

    }

    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
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
        if(requestCode==TAG_REQUEST && resultCode== Activity.RESULT_OK){
            coordinateList = data.getStringArrayListExtra("pins");
            if(coordinateList.size()==0){
                return;
            }
            placePins();
        }
    }

    private void placePins(){
        boolean moreThanOneCoordinate = false;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        coordinateList = getArrayList("pins");
        if((coordinateList!=null) && coordinateList.size()!=0){
            placePins();
        }
    }
}
