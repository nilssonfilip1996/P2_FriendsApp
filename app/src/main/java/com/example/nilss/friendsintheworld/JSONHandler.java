package com.example.nilss.friendsintheworld;

import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

public class JSONHandler {
    public static final String TYPE_REGISTER = "register";
    public static final String TYPE_UNREGISTER = "unregister";
    public static final String TYPE_MEMBERS = "members";
    public static final String TYPE_GROUPS = "groups";
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_LOCATIONS = "locations";

    public static final String KEY_TYPE = "type";
    public static final String KEY_GROUP = "group";
    public static final String KEY_GROUPS = "groups";
    public static final String KEY_GROUP_ID = "id";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";



    public static JSONArray convertStringToJSONArray(String message){
        JSONArray jsonArray = new JSONArray();
        return jsonArray;
    }


    public static String createJSONRegisterGroup(String groupName, String memberName) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_REGISTER)
                    .name("group").value(groupName)
                    .name("member").value(memberName)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONDeRegisterGroup(String groupName, String id) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_UNREGISTER)
                    .name("ID").value(id)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONRequestMembersInGroup(String groupName) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_MEMBERS)
                    .name("group").value(groupName)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONRequestCurrentGroups() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_GROUPS)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONSetCurrentPosition(String id, String longitude, String latitude) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_LOCATION)
                    .name("id").value(id)
                    .name("longitude").value(longitude)
                    .name("latitude").value(latitude)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}
