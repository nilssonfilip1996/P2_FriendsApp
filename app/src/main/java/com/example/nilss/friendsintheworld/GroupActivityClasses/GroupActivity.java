package com.example.nilss.friendsintheworld.GroupActivityClasses;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.R;

public class GroupActivity extends AppCompatActivity {
    GroupController groupController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        groupController = new GroupController(this, R.id.fragmentContainer, savedInstanceState);
    }



    @Override
    protected void onDestroy() {
        groupController.onDestroy();
        super.onDestroy();
    }
}
