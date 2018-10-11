package com.example.nilss.friendsintheworld;

public class User {
    private String name;
    private String groupName;
    private String groupID;
    public User(String name, String groupName, String groupID){
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
}
