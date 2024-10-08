package com.salesforce.marketingcloud.reactnative;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class SharedUtils {


    private static final String TAG = "Utils";
    private static final String EXPO_REGISTRY_CLASS = "ModuleRegistry";
    private static final String EXPO_CORE_PACKAGE = "expo.core";

    private static final String REACT_NATIVE_REGISTRY_CLASS = "NativeModuleRegistry";
    private static final String REACT_NATIVE_CORE_PACKAGE = "com.facebook.react.bridge";



    /**
     * We need to check if app is in foreground otherwise the app will crash.
     *
     * @param context Context
     * @return boolean
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        // Check if current activity is a background activity
        ReactNativeFirebaseJSON json = ReactNativeFirebaseJSON.getSharedInstance();
        if (json.contains("android_background_activity_names")) {
            ArrayList<String> backgroundActivities =
                    json.getArrayValue("android_background_activity_names");

            if (backgroundActivities.size() != 0) {
                String currentActivity = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    List<ActivityManager.AppTask> taskInfo = activityManager.getAppTasks();
                    if (taskInfo.size() > 0) {
                        ActivityManager.RecentTaskInfo task = taskInfo.get(0).getTaskInfo();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            currentActivity = Objects.requireNonNull(task.baseActivity).getShortClassName();
                        } else {
                            currentActivity =
                                    task.origActivity != null
                                            ? task.origActivity.getShortClassName()
                                            : Objects.requireNonNull(task.baseIntent.getComponent()).getShortClassName();
                        }
                    }
                } else {
                    List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                    if (taskInfo.size() > 0) {
                        currentActivity = taskInfo.get(0).topActivity.getShortClassName();
                    }
                }

                if (!"".equals(currentActivity) && backgroundActivities.contains(currentActivity)) {
                    return false;
                }
            }
        }

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                ReactContext reactContext;

                try {
                    reactContext = (ReactContext) context;
                } catch (ClassCastException exception) {
                    // Not react context so default to true
                    return true;
                }

                return reactContext.getLifecycleState() == LifecycleState.RESUMED;
            }
        }

        return false;
    }


    public static WritableMap jsonObjectToWritableMap(JSONObject jsonObject) throws JSONException {
        Iterator<String> iterator = jsonObject.keys();
        WritableMap writableMap = Arguments.createMap();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof Float || value instanceof Double) {
                writableMap.putDouble(key, jsonObject.getDouble(key));
            } else if (value instanceof Number) {
                writableMap.putInt(key, jsonObject.getInt(key));
            } else if (value instanceof String) {
                writableMap.putString(key, jsonObject.getString(key));
            } else if (value instanceof JSONObject) {
                writableMap.putMap(key, jsonObjectToWritableMap(jsonObject.getJSONObject(key)));
            } else if (value instanceof JSONArray) {
                writableMap.putArray(key, jsonArrayToWritableArray(jsonObject.getJSONArray(key)));
            } else if (value == JSONObject.NULL) {
                writableMap.putNull(key);
            }
        }

        return writableMap;
    }

    public static WritableArray jsonArrayToWritableArray(JSONArray jsonArray) throws JSONException {
        WritableArray writableArray = Arguments.createArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof Float || value instanceof Double) {
                writableArray.pushDouble(jsonArray.getDouble(i));
            } else if (value instanceof Number) {
                writableArray.pushInt(jsonArray.getInt(i));
            } else if (value instanceof String) {
                writableArray.pushString(jsonArray.getString(i));
            } else if (value instanceof JSONObject) {
                writableArray.pushMap(jsonObjectToWritableMap(jsonArray.getJSONObject(i)));
            } else if (value instanceof JSONArray) {
                writableArray.pushArray(jsonArrayToWritableArray(jsonArray.getJSONArray(i)));
            } else if (value == JSONObject.NULL) {
                writableArray.pushNull();
            }
        }
        return writableArray;
    }

    public static WritableMap mapToWritableMap(Map<String, Object> value) {
        WritableMap writableMap = Arguments.createMap();

        for (Map.Entry<String, Object> entry : value.entrySet()) {
            mapPutValue(entry.getKey(), entry.getValue(), writableMap);
        }

        return writableMap;
    }

    private static WritableArray listToWritableArray(List<Object> objects) {
        WritableArray writableArray = Arguments.createArray();
        for (Object object : objects) {
            arrayPushValue(object, writableArray);
        }
        return writableArray;
    }

    @SuppressWarnings("unchecked")
    public static void arrayPushValue(@Nullable Object value, WritableArray array) {
        if (value == null || value == JSONObject.NULL) {
            array.pushNull();
            return;
        }

        String type = value.getClass().getName();
        switch (type) {
            case "java.lang.Boolean":
                array.pushBoolean((Boolean) value);
                break;
            case "java.lang.Long":
                Long longVal = (Long) value;
                array.pushDouble((double) longVal);
                break;
            case "java.lang.Float":
                float floatVal = (float) value;
                array.pushDouble((double) floatVal);
                break;
            case "java.lang.Double":
                array.pushDouble((double) value);
                break;
            case "java.lang.Integer":
                array.pushInt((int) value);
                break;
            case "java.lang.String":
                array.pushString((String) value);
                break;
            case "org.json.JSONObject$1":
                try {
                    array.pushMap(jsonObjectToWritableMap((JSONObject) value));
                } catch (JSONException e) {
                    array.pushNull();
                }
                break;
            case "org.json.JSONArray$1":
                try {
                    array.pushArray(jsonArrayToWritableArray((JSONArray) value));
                } catch (JSONException e) {
                    array.pushNull();
                }
                break;
            default:
                if (List.class.isAssignableFrom(value.getClass())) {
                    array.pushArray(listToWritableArray((List<Object>) value));
                } else if (Map.class.isAssignableFrom(value.getClass())) {
                    array.pushMap(mapToWritableMap((Map<String, Object>) value));
                } else {
                    Log.d(TAG, "utils:arrayPushValue:unknownType:" + type);
                    array.pushNull();
                }
        }
    }

    @SuppressWarnings("unchecked")
    public static void mapPutValue(String key, @Nullable Object value, WritableMap map) {
        if (value == null || value == JSONObject.NULL) {
            map.putNull(key);
            return;
        }

        String type = value.getClass().getName();
        switch (type) {
            case "java.lang.Boolean":
                map.putBoolean(key, (Boolean) value);
                break;
            case "java.lang.Long":
                Long longVal = (Long) value;
                map.putDouble(key, (double) longVal);
                break;
            case "java.lang.Float":
                float floatVal = (float) value;
                map.putDouble(key, (double) floatVal);
                break;
            case "java.lang.Double":
                map.putDouble(key, (double) value);
                break;
            case "java.lang.Integer":
                map.putInt(key, (int) value);
                break;
            case "java.lang.String":
                map.putString(key, (String) value);
                break;
            case "org.json.JSONObject$1":
                try {
                    map.putMap(key, jsonObjectToWritableMap((JSONObject) value));
                } catch (JSONException e) {
                    map.putNull(key);
                }
                break;
            case "org.json.JSONArray$1":
                try {
                    map.putArray(key, jsonArrayToWritableArray((JSONArray) value));
                } catch (JSONException e) {
                    map.putNull(key);
                }
                break;
            default:
                if (List.class.isAssignableFrom(value.getClass())) {
                    map.putArray(key, listToWritableArray((List<Object>) value));
                } else if (Map.class.isAssignableFrom(value.getClass())) {
                    map.putMap(key, mapToWritableMap((Map<String, Object>) value));
                } else {
                    Log.d(TAG, "utils:mapPutValue:unknownType:" + type);
                    map.putNull(key);
                }
        }
    }



}
