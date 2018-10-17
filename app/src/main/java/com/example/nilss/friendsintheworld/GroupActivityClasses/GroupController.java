package com.example.nilss.friendsintheworld.GroupActivityClasses;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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
import com.example.nilss.friendsintheworld.Pojos.Message;
import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.TCPConnection;
import com.example.nilss.friendsintheworld.Pojos.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private ArrayList<JSONObject> incommingLocationsList;
    private byte[] imageByteTobeSent;
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
        incommingLocationsList = new ArrayList<>();
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
                tcpConnection.setCurrentGroupID(currentGroupID);
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

    public void mapsBtnClicked() {
        Log.d(TAG, "mapsBtnClicked: currentGroupName: "+ currentGroupName);
        ArrayList<String> coordinateList = new ArrayList<>();
        for (int i = 0; i < incommingLocationsList.size(); i++) {
            try {
                Log.d(TAG, "mapsBtnClicked: LocationsList(" + String.valueOf(i)+") = " + incommingLocationsList.get(i).getString(JSONHandler.KEY_GROUP));
                if(incommingLocationsList.get(i).getString(JSONHandler.KEY_GROUP).equals(currentGroupName)){
                    JSONArray jsonArray= incommingLocationsList.get(i).getJSONArray(JSONHandler.KEY_LOCATION);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        coordinateList.add(jsonArray.getJSONObject(j).getString(JSONHandler.KEY_LONGITUDE));
                        coordinateList.add(jsonArray.getJSONObject(j).getString(JSONHandler.KEY_LATITUDE));
                    }
                    Intent returnIntent = new Intent();
                    returnIntent.putStringArrayListExtra("pins",coordinateList);
                    groupActivity.setResult(Activity.RESULT_OK,returnIntent);
                    groupActivity.finish();
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
/*        ArrayList<String> coordinateList = new ArrayList<>();
        for (int i = 0; i < currentMessageList.size(); i++) {
            //coordinateList.add()
        }
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(("pins",result);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();*/
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
        //otherwise a imagemessage!
        else{
            imageByteTobeSent = imageArray;
            //send upload request
            send(JSONHandler.TYPE_IMAGECHAT, new String[]{currentGroupID,
                                                            textMessage,
                                                            tcpConnection.getLongitudeString(),
                                                            tcpConnection.getLatitudeString()});
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
                        tcpConnection.setCurrentGroupID(currentGroupID);
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
                        boolean currentGroupStillpresent = false;
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
                                    if(currentGroupID!=null && currentGroupID.equals(currentUser.getGroupID(j))){
                                        currentGroupStillpresent=true;
                                    }
                                }
                            }
                        }
                        if(!currentGroupStillpresent){
                            currentGroupName=null;
                            currentGroupID=null;
                            tcpConnection.setCurrentGroupID(currentGroupID);
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
                        //NEED TO VERIFY THIS!
                        if(incommingLocationsList.size()==0){
                            incommingLocationsList.add(jsonObject);
                            break;
                        }
                        for (int i = 0; i < incommingLocationsList.size(); i++) {
                            if(incommingLocationsList.get(i).getString(JSONHandler.KEY_GROUP).equals(jsonObject.getString(JSONHandler.KEY_GROUP)))
                                incommingLocationsList.remove(i);
                                incommingLocationsList.add(jsonObject);
                        }
                        //incommingLocationsList.add(jsonObject);

                        break;
                    case(JSONHandler.TYPE_TEXTCHAT):
                        Log.d(TAG, "processIncMessage: incomming textmessage");
                        Log.d(TAG, "processIncMessage: "+ message);
                        //String groupName = jsonObject.getString(JSONHandler.KEY_GROUP);
                        //if len==3 it means it is a reply from my own message
/*                        if(jsonObject.length()==3){
                            Log.d(TAG, "processIncMessage: -----------------1-----------");
                            textMessages.add(new TextMessage(currentGroupName,
                                                            currentUser.getName(),
                                                            jsonObject.getString(JSONHandler.KEY_TEXT),
                                                            null));
                        }
                        //Someone else is sending.
                        else{
                            Log.d(TAG, "processIncMessage: -----------------2-----------");
                            textMessages.add(new TextMessage(jsonObject.getString(JSONHandler.KEY_GROUP),
                                    jsonObject.getString(JSONHandler.KEY_MEMBER),
                                    jsonObject.getString(JSONHandler.KEY_TEXT),
                                    null));
                        }*/
                        if(jsonObject.length()==4) {
                            textMessages.add(new TextMessage(jsonObject.getString(JSONHandler.KEY_GROUP),
                                    jsonObject.getString(JSONHandler.KEY_MEMBER),
                                    jsonObject.getString(JSONHandler.KEY_TEXT),
                                    null));
                        }
                        groupActivity.runOnUiThread(()->{
                            chatFragment.updateList(getMessagesForGroup(currentGroupName));
                        });
                        break;
                    case(JSONHandler.TYPE_UPLOAD):
                        String imageId = jsonObject.getString(JSONHandler.KEY_IMAGE_ID);
                        String uploadPort = jsonObject.getString(JSONHandler.KEY_PORT);
                        UploadImage uploadImage = new UploadImage();
                        uploadImage.execute(imageId,uploadPort);
                        break;
                    case(JSONHandler.TYPE_IMAGECHAT):
                        Log.d(TAG, "processIncMessage: An image is ready to be downloaded!");
                        DownloadImage dlImage = new DownloadImage();
                        dlImage.execute(jsonObject.getString(JSONHandler.KEY_GROUP),
                                        jsonObject.getString(JSONHandler.KEY_MEMBER),
                                        jsonObject.getString(JSONHandler.KEY_TEXT),
                                        jsonObject.getString(JSONHandler.KEY_LONGITUDE),
                                        jsonObject.getString(JSONHandler.KEY_LONGITUDE),
                                        jsonObject.getString(JSONHandler.KEY_IMAGE_ID),
                                        jsonObject.getString((JSONHandler.KEY_PORT)));
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

    private class UploadImage extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String imageId = params[0];
            String uploadPort = params[1];
            try {
                Log.d(TAG, "doInBackground: Trying to upload imageID: "+ imageId + ", on port: "+ uploadPort);
                Socket socket = new Socket(InetAddress.getByName(MainActivity.IPADDR),Integer.parseInt(uploadPort));
                ObjectOutputStream objectOS = new ObjectOutputStream(socket.getOutputStream());
                objectOS.flush();
                objectOS.writeUTF(imageId);
                objectOS.flush();
                objectOS.writeObject(imageByteTobeSent);
                objectOS.flush();
                objectOS.close();
                socket.close();
                Log.d(TAG, "doInBackground: Uploaded image to server!");
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: " + e);
            }
            return null;
        }
    }

    private class DownloadImage extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String groupName = params[0];
            String member = params[1];
            String text = params[2];
            String longitude = params[3];
            String latitude = params[4];
            String imageId = params[5];
            String port = params[6];

            try {
                Log.d(TAG, "doInBackground: Trying to download imageID: "+ imageId + ", on port: "+ port);
                Socket socket = new Socket(InetAddress.getByName(MainActivity.IPADDR),Integer.parseInt(port));
                ObjectOutputStream objectOS = new ObjectOutputStream(socket.getOutputStream());
                objectOS.writeUTF(imageId);
                objectOS.flush();
                ObjectInputStream objectIS = new ObjectInputStream(socket.getInputStream());
                byte[] imageByte = (byte[])objectIS.readObject();
                objectOS.close();
                objectIS.close();
                socket.close();
                Log.d(TAG, "doInBackground: Downloaded image from server!");
                textMessages.add(new TextMessage(groupName, member, text, imageByte));
                groupActivity.runOnUiThread(()->{
                    chatFragment.updateList(getMessagesForGroup(currentGroupName));
                });
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: DownloadImage: " + e);
            }
            return null;
        }
    }

}
