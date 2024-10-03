package com.salesforce.marketingcloud.reactnative;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReactNativeFirebaseJSON {
    private static ReactNativeFirebaseJSON sharedInstance = new ReactNativeFirebaseJSON();

    private JSONObject jsonObject;

    private ReactNativeFirebaseJSON() {
        try {
            jsonObject = new JSONObject("{\"crashlytics_debug_enabled\":true}");
        } catch (JSONException e) {
            // JSON is validated as part of gradle build - should never error
        }
    }

    public static ReactNativeFirebaseJSON getSharedInstance() {
        return sharedInstance;
    }

    public boolean contains(String key) {
        if (jsonObject == null) return false;
        return jsonObject.has(key);
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        if (jsonObject == null) return defaultValue;
        return jsonObject.optBoolean(key, defaultValue);
    }

    public int getIntValue(String key, int defaultValue) {
        if (jsonObject == null) return defaultValue;
        return jsonObject.optInt(key, defaultValue);
    }

    public long getLongValue(String key, long defaultValue) {
        if (jsonObject == null) return defaultValue;
        return jsonObject.optLong(key, defaultValue);
    }

    public String getStringValue(String key, String defaultValue) {
        if (jsonObject == null) return defaultValue;
        return jsonObject.optString(key, defaultValue);
    }

    public ArrayList<String> getArrayValue(String key) {
        ArrayList<String> result = new ArrayList<String>();
        if (jsonObject == null) return result;

        try {
            JSONArray array = jsonObject.optJSONArray(key);
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    result.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            // do nothing
        }

        return result;
    }

    public String getRawJSON() {
        return "{\"crashlytics_debug_enabled\":true}";
    }

    public WritableMap getAll() {
        WritableMap writableMap = Arguments.createMap();

        JSONArray keys = jsonObject.names();
        for (int i = 0; i < keys.length(); ++i) {
            try {
                String key = keys.getString(i);
                SharedUtils.mapPutValue(key, jsonObject.get(key), writableMap);
            } catch (JSONException e) {
                // ignore
            }
        }

        return writableMap;
    }
}
