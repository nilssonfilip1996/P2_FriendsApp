package com.example.nilss.friendsintheworld.Pojos;

public class TextMessage implements Message{
    private String groupName;
    private String sender;
    private String textMessage;
    private byte[] imageArray;

    public TextMessage(String groupName, String sender, String textMessage, byte[] imageArray) {
        this.groupName = groupName;
        this.sender = sender;
        this.textMessage = textMessage;
        this.imageArray = imageArray;
    }


    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setSender(String name) {
        this.sender = name;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public void setTextMessage(String text) {
        this.textMessage = text;
    }

    @Override
    public String getTextMessage() {
        return textMessage;
    }

    @Override
    public void setImageArray(byte[] imageArray) {
        this.imageArray = imageArray;
    }

    @Override
    public byte[] getImageArray() {
        return imageArray;
    }
}
