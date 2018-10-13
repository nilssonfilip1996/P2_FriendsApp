package com.example.nilss.friendsintheworld.Pojos;

import android.util.Log;

import java.util.ArrayList;


public class User {
    private static final String TAG = "User";
    private String name;
    private ArrayList<String> groupName;
    private ArrayList<String> groupID;
    public User(String name, ArrayList<String> groupName, ArrayList<String> groupID){
        this.name=name;
        this.groupName=groupName;
        this.groupID=groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName(int i) {
        return groupName.get(i);
    }

    public void addGroupName(String groupName) {
        this.groupName.add(groupName);
    }

    public void removeGroup(String id) {
        for(int i=0;i<groupID.size(); i++){
            if(groupID.get(i).equals(id)){
                this.groupID.remove(i);
                this.groupName.remove(i);
                break;
            }
        }
        Log.d(TAG, "removeGroup: Couldn´t remove group");

    }

    public String getGroupID(int i) {
        return groupID.get(i);
    }

    public void addGroupID(String groupID) {
        this.groupID.add(groupID);
    }

    @Override
    public String toString() {
        //return super.toString();
        String temp = "";
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<groupID.size(); i++){
            stringBuilder.append("GroupName: " + groupName.get(i) + "groupId: " + groupID.get(i) + "\n");
        }
        return stringBuilder.toString();
    }
}
