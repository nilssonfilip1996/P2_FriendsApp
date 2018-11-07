package com.example.nilss.friendsintheworld.Pojos;

public interface Message {

    void setGroupName(String groupName);
    String getGroupName();
    void setSender(String name);
    String getSender();
    void setTextMessage(String text);
    String getTextMessage();
    public void setImageArray(byte[] imageArray);
    public byte[] getImageArray();
}
