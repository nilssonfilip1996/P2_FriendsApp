package com.example.nilss.friendsintheworld.Pojos;

import android.util.Log;

import java.util.ArrayList;


public class User {
    private static final String TAG = "User";
    private String name;
    private ArrayList<String> groupNames;
    private ArrayList<String> groupIDs;
    public User(String name, ArrayList<String> groupName, ArrayList<String> groupID){
        this.name=name;
        this.groupNames =groupName;
        this.groupIDs =groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbrOfGroups(){
        return groupNames.size();
    }

    public void setGroupNames(ArrayList<String> groupNames){
        this.groupNames = groupNames;
    }

    public String getGroupName(int i) {
        return groupNames.get(i);
    }

    public void addGroupName(String groupName) {
        this.groupNames.add(groupName);
    }

    public void removeGroup(String id) {
        for(int i = 0; i< groupIDs.size(); i++){
            if(groupIDs.get(i).equals(id)){
                this.groupIDs.remove(i);
                this.groupNames.remove(i);
                return;
            }
        }
        Log.d(TAG, "removeGroup: Couldn´t remove group");

    }

    public void setGroupIDs(ArrayList<String> groupIDs){
        this.groupIDs = groupIDs;
    }

    public String getGroupID(int i) {
        return groupIDs.get(i);
    }

    public void addGroupID(String groupID) {
        this.groupIDs.add(groupID);
    }

    @Override
    public String toString() {
        //return super.toString();
        String temp = "";
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i< groupIDs.size(); i++){
            stringBuilder.append("GroupName: " + groupNames.get(i) + "groupId: " + groupIDs.get(i) + "\n");
        }
        return stringBuilder.toString();
    }
}
