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
import com.example.nilss.friendsintheworld.Pojos.Message;
import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.TCPConnection;
import com.example.nilss.friendsintheworld.Pojos.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupController {
    private static final String TAG = "GroupController";
    public static final String groupFragmentTag = "manageGroup";
    public static final String chatFragmentTag = "chat";
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
    private String currentGroupID;
    private String currentGroupName;
    private ArrayList<TextMessage> textMessages;
    private ArrayList<String> currentGroupsList;
    private boolean bound = false;

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
            ft.add(fragmentContainer,chatFragment,chatFragmentTag);
            ft.hide(chatFragment);
            ft.add(fragmentContainer,manageGroupsFragment,groupFragmentTag);
            ft.show(manageGroupsFragment);
            ft.commit();
            currentTag=groupFragmentTag;
        }
        else{
            Log.d(TAG, "GroupController: savedInstance NOT null");
            manageGroupsFragment = (ManageGroupsFragment)fragmentManager.findFragmentByTag(groupFragmentTag);
            chatFragment = (ChatFragment)fragmentManager.findFragmentByTag(chatFragmentTag);
        }
        //Get ref to TCPConnection service.
        getTCPConnection();
        //currentUser = new User("Clas", new ArrayList<String>(),new ArrayList<String>());
        initManageGroupsFragment();
        chatFragment.setGroupController(this);
    }

    private void initManageGroupsFragment() {
        //init recyclerview with available groups
        currentGroupsList = new ArrayList<>();
/*        currentGroupsList.add("Los Amigos"); //TEMP*/
        manageGroupsFragment.setGroupController(this);
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
            currentUser = tcpConnection.getCurrentUser();
            textMessages = tcpConnection.getTextMessages();
            bound = true; //service bound
            Log.d(TAG, "connection: " + String.valueOf(tcpConnection!=null));
            receiveListener = new ReceiveListener();
            receiveListener.start();
            send(JSONHandler.TYPE_GROUPS, null);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    }
    //unbind service when activity is destroyed
    public void onDestroy() {
        if(bound){
            tcpConnection.setCurrentUser(currentUser);
            tcpConnection.setTextMessages(textMessages);
            groupActivity.unbindService(serviceConn);
            receiveListener.stopListener();
            bound = false;
        }
    }
    public void groupInListClicked(String groupName){
        Log.d(TAG, "groupInListClicked: " + groupName);
        boolean registered = false;
        for(int i=0; i<currentUser.getNbrOfGroups();i++){
            if(currentUser.getGroupName(i).equals(groupName)){
                registered = true;
                currentGroupID = currentUser.getGroupID(i);
                currentGroupName = groupName;
                break;
            }
        }
        //Not a member of the group yet!
        if(!registered){
            addGroup(groupName);
        }
        ArrayList<TextMessage> messagesForGroup = getMessagesForGroup(groupName);
        chatFragment.updateList(messagesForGroup);
        show(chatFragmentTag);
    }

    private ArrayList<TextMessage> getMessagesForGroup(String groupName){
        ArrayList<TextMessage> temp = new ArrayList<>();
        TextMessage tempMessage;
        Log.d(TAG, "getMessagesForGroup: test");
        for(int i=0;i<textMessages.size();i++){
            tempMessage= textMessages.get(i);
            if(tempMessage.getGroupName().equals(groupName)){
                Log.d(TAG, "getMessagesForGroup: MATCH");
                temp.add(tempMessage);
            }
        }
        return temp;
    }

    public void send(String type,String[] values){
        String JSONString = JSONHandler.createJsonString(type,values);
        tcpConnection.sendMessage(JSONString);
    }

    public void addGroup(String s) {
        send(JSONHandler.TYPE_REGISTER, new String[]{s,currentUser.getName()});
        send(JSONHandler.TYPE_GROUPS, null);
    }


    //switch active fragment.
    public void show(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentTag);
        if(fragment!=null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if(currentFragment!=null) {
                ft.hide(currentFragment);
                Log.d(TAG, "show: hiding: "+currentTag);
            }
            ft.show(fragment);
            ft.commit();
            currentTag = tag;
        }
    }

    public void sendMessage(String textMessage, byte[] imageArray){
        if((textMessage.equals("")) && (imageArray==null)){
            Toast.makeText(groupActivity, "Message must contain atleast a message of image!", Toast.LENGTH_SHORT).show();
            return;
        }
        //only a textmessage to be sent
        if(imageArray==null){
            send(JSONHandler.TYPE_TEXTCHAT,new String[]{currentGroupID, textMessage});
        }
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
                        currentUser.addGroupName(jsonObject.getString(JSONHandler.KEY_GROUP));
                        currentUser.addGroupID(jsonObject.getString(JSONHandler.KEY_GROUP_ID));
                        currentGroupID = jsonObject.getString(JSONHandler.KEY_GROUP_ID);
                        currentGroupName = jsonObject.getString(JSONHandler.KEY_GROUP);
                        Log.d(TAG, "processIncMessage: users groups:" + currentUser.toString());
                        //Show on the UI which group they currently belong to
                        //Log.d(TAG, "processIncMessage: group: " + currentUser.getGroupID());
                        break;
                    case(JSONHandler.TYPE_UNREGISTER):
                        //remove group from users current groups.
                        currentUser.removeGroup(jsonObject.getString(JSONHandler.KEY_GROUP_ID));
                        //Show on the UI which group they currently belong to
                        break;
                    case(JSONHandler.TYPE_MEMBERS):
                        //unsure of what should happen here
                        jsonArray = jsonObject.getJSONArray(JSONHandler.KEY_MEMBERS);

                        break;
                    case(JSONHandler.TYPE_GROUPS):
                        //----->update users currentgroups here<------
                        ArrayList<String> usersUpdatedGroups = new ArrayList<>();
                        ArrayList<String> usersUpdatedGroupsID = new ArrayList<>();
                        String group = "";
                        //update recyclerview on availablegroups
                        jsonArray = jsonObject.getJSONArray(JSONHandler.KEY_GROUPS);
                        currentGroupsList.clear();
                        for(int i=0; i<jsonArray.length();i++){
                            //Log.d(TAG, "processIncMessage: group"+String.valueOf(i)+" = "+jsonArray.getJSONObject(i).getString(JSONHandler.KEY_GROUP));
                            group = jsonArray.getJSONObject(i).getString(JSONHandler.KEY_GROUP);
                            currentGroupsList.add(group);
                            for(int j=0;j<currentUser.getNbrOfGroups();j++){
                                if(currentUser.getGroupName(j).equals(group)){
                                    usersUpdatedGroups.add(group);
                                    usersUpdatedGroupsID.add(currentUser.getGroupID(j));
                                }
                            }
                        }
                        currentUser.setGroupNames(usersUpdatedGroups);
                        currentUser.setGroupIDs(usersUpdatedGroupsID);
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
                    case(JSONHandler.TYPE_TEXTCHAT):
                        Log.d(TAG, "processIncMessage: incomming textmessage");
                        String groupName = jsonObject.getString(JSONHandler.KEY_GROUP);
                        //if len==3 it means it is a reply from my own message
                        if(jsonObject.length()==3){

                            textMessages.add(new TextMessage(currentGroupName,
                                                            currentUser.getName(),
                                                            jsonObject.getString(JSONHandler.KEY_TEXT),
                                                            null));
                        }
                        //Someone else is sending.
                        else{
                            textMessages.add(new TextMessage(jsonObject.getString(JSONHandler.KEY_GROUP),
                                    jsonObject.getString(JSONHandler.KEY_MEMBER),
                                    jsonObject.getString(JSONHandler.KEY_TEXT),
                                    null));
                        }
                        groupActivity.runOnUiThread(()->{
                            chatFragment.updateList(getMessagesForGroup(groupName));
                        });
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
