package com.example.nilss.friendsintheworld.Pojos;

public class Group {
    private String groupName;
    private boolean isUserMember;

    public Group(String groupName, boolean isUserMember) {
        this.groupName = groupName;
        this.isUserMember = isUserMember;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isUserMember() {
        return isUserMember;
    }

    public void setUserMember(boolean userMember) {
        isUserMember = userMember;
    }
}
