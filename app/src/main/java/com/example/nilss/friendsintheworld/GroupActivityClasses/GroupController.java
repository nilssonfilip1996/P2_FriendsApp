package com.example.nilss.friendsintheworld.GroupActivityClasses;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.example.nilss.friendsintheworld.GroupActivityClasses.ChatFragmentClasses.ChatFragment;
import com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses.ManageGroupsFragment;
import com.example.nilss.friendsintheworld.JSONHandler;
import com.example.nilss.friendsintheworld.MainActivity;
import com.example.nilss.friendsintheworld.TCPConnection;
import com.example.nilss.friendsintheworld.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupController {
    private static final String TAG = "GroupController";
    private int fragmentContainer;
    private FragmentManager fragmentManager;
    private GroupActivity groupActivity;
    private ManageGroupsFragment manageGroupsFragment;
    private ChatFragment chatFragment;
    private ReceiveListener receiveListener;
    private String currentTag;
    private TCPConnection tcpConnection;
    private ServiceConn serviceConn;
    private User currentUser;
    private ArrayList<String> currentGroupsList;

    public GroupController(GroupActivity groupActivity, int fragmentContainer, Bundle savedInstanceState) {
        this.groupActivity = groupActivity;
        this.fragmentContainer = fragmentContainer;
        fragmentManager = groupActivity.getSupportFragmentManager();
        if(savedInstanceState==null){
            Log.d(TAG, "GroupController: savedInstance IS null");
            //create new instances of fragments
            manageGroupsFragment = new ManageGroupsFragment();
            chatFragment = new ChatFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(fragmentContainer,chatFragment,"chat");
            ft.hide(chatFragment);
            ft.add(fragmentContainer,manageGroupsFragment,"manageGroup");
            ft.show(manageGroupsFragment);
            ft.commit();
        }
        else{
            Log.d(TAG, "GroupController: savedInstance NOT null");
            manageGroupsFragment = (ManageGroupsFragment)fragmentManager.findFragmentByTag("manageGroup");
            chatFragment = (ChatFragment)fragmentManager.findFragmentByTag("chat");
        }
        //Get ref to TCPConnection service.
        getTCPConnection();
        currentUser = new User("Filip", "","");
        initManageGroupsFragment();
    }

    private void initManageGroupsFragment() {
        //init recyclerview with available groups
        currentGroupsList = new ArrayList<>();
        manageGroupsFragment.setGroupController(this);
        currentGroupsList.add("test1");
        currentGroupsList.add("test2");
        manageGroupsFragment.onInit(()->{
            manageGroupsFragment.updateList(currentGroupsList);
        });
    }

    //get ref to tcp service
    private void getTCPConnection() {
        Intent intent = new Intent(groupActivity,TCPConnection.class);
        serviceConn = new ServiceConn();
        boolean result = groupActivity.bindService(intent, serviceConn, 0);
        if (!result)
            Log.d("Controller-constructor", "No binding");
    }

    private class ServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPConnection.LocalService ls = (TCPConnection.LocalService) binder;
            tcpConnection = ls.getService();
            Log.d(TAG, "connection: " + String.valueOf(tcpConnection!=null));
            receiveListener = new ReceiveListener();
            receiveListener.start();
            //tcpConnection.sendMessage("blabla");
            tcpConnection.sendMessage(JSONHandler.createJSONRegisterGroup("losamigos", currentUser.getName()));
            tcpConnection.sendMessage(JSONHandler.createJSONRequestCurrentGroups());
            /*bound = true;
            listener = new Listener();
            listener.start();*/
        }

        public void onServiceDisconnected(ComponentName arg0) {
            /*bound = false;*/
        }
    }
    //unbind service when activity is destroyed
    public void onDestroy() {
        groupActivity.unbindService(serviceConn);

    }
    //switch active fragment.
    public String show(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentTag);
        if(fragment!=null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if(currentFragment!=null)
                ft.hide(currentFragment);
            ft.show(fragment);
            ft.commit();
            currentTag = tag;
        }
        return currentTag;
    }

    public void send(String type,String values){

    }

    public void addGroup(String s) {

    }

    private class ReceiveListener extends Thread {
        private String message;
        public void stopListener() {
            interrupt();
            receiveListener = null;
        }

        private void processIncMessage(){
            try {
                JSONObject jsonObject = new JSONObject(message);
                JSONArray jsonArray;
                String type = jsonObject.getString(JSONHandler.KEY_TYPE);
                switch(type){
                    case(JSONHandler.TYPE_EXCEPTION):
                        groupActivity.runOnUiThread(()-> {
                            try {
                                Toast.makeText(groupActivity, "Exception! " + jsonObject.getString(JSONHandler.KEY_MESSAGE), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                        break;
                    case(JSONHandler.TYPE_REGISTER):
                        //FOR VG, a user can be part of multiple groups. TBC
                        currentUser.setGroupName(jsonObject.getString(JSONHandler.KEY_GROUP));
                        currentUser.setGroupID(jsonObject.getString(JSONHandler.KEY_GROUP_ID));
                        //Show on the UI which group they currently belong to
                        Log.d(TAG, "processIncMessage: group: " + currentUser.getGroupID());
                        break;
                    case(JSONHandler.TYPE_UNREGISTER):
                        //update the users currentgroup to be null.
                        currentUser.setGroupName("");
                        currentUser.setGroupID("");
                        //Show on the UI which group they currently belong to
                        break;
                    case(JSONHandler.TYPE_MEMBERS):
                        //unsure of what should happen here
                        jsonArray = jsonObject.getJSONArray(JSONHandler.KEY_MEMBERS);

                        break;
                    case(JSONHandler.TYPE_GROUPS):
                        //update recyclerview on availablegroups
                        jsonArray = jsonObject.getJSONArray(JSONHandler.KEY_GROUPS);
                        currentGroupsList.clear();
                        for(int i=0; i<jsonArray.length();i++){
                            //Log.d(TAG, "processIncMessage: group"+String.valueOf(i)+" = "+jsonArray.getJSONObject(i).getString(JSONHandler.KEY_GROUP));
                            currentGroupsList.add(jsonArray.getJSONObject(i).getString(JSONHandler.KEY_GROUP));
                        }
                        groupActivity.runOnUiThread(()->{
                            manageGroupsFragment.updateList(currentGroupsList);
                        });

                        break;
                    case(JSONHandler.TYPE_LOCATION):
                        //probably not doing anything here
                        break;
                    case(JSONHandler.TYPE_LOCATIONS):
                        //update the pins of the map.
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Exception exception;
            while (receiveListener != null) {
                try {
                    message = tcpConnection.receive();
                    Log.d(TAG, "run: incoming: "+message);
                    processIncMessage();
                    //JSONObject jsonObject = null;
                    /*JSONObject jsonObject = new JSONObject(message);
                    Log.d(TAG, "run: JSON: "+ jsonObject.toString());
                    Log.d(TAG, "run: received message: type:" + jsonObject.getString("type"));*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    receiveListener = null;
                } /*catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }

}
