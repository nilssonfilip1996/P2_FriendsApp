package com.example.nilss.friendsintheworld;

import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

public final class JSONHandler {
    public static final String TYPE_EXCEPTION = "exception";
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
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";



    public static JSONArray convertStringToJSONArray(String message){
        JSONArray jsonArray = new JSONArray();
        return jsonArray;
    }

    public static String createJsonString(String type, String[] fields){
        String jsonString = "";
        switch(type){
            case(JSONHandler.TYPE_REGISTER):
                jsonString = createJSONRegisterGroup(fields);
                break;
            case(JSONHandler.TYPE_UNREGISTER):
                jsonString = createJSONDeRegisterGroup(fields);
                break;
            case(JSONHandler.TYPE_MEMBERS):
                jsonString = createJSONRequestMembersInGroup(fields);
                break;
            case(JSONHandler.TYPE_GROUPS):
                //just send an empty string array for this case.
                jsonString = createJSONRequestCurrentGroups();
                break;
            case(JSONHandler.TYPE_LOCATION):
                jsonString = createJSONSetCurrentPosition(fields);
                break;

        }
        return jsonString;
    }


    public static String createJSONRegisterGroup(String[] fields) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_REGISTER)
                    .name("group").value(fields[0])
                    .name("member").value(fields[1])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONDeRegisterGroup(String[] fields) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_UNREGISTER)
                    .name("ID").value(fields[0])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static String createJSONRequestMembersInGroup(String[] fields) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_MEMBERS)
                    .name("group").value(fields[0])
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

    public static String createJSONSetCurrentPosition(String[] fields) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(TYPE_LOCATION)
                    .name("id").value(fields[0])
                    .name("longitude").value(fields[1])
                    .name("latitude").value(fields[2])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}
