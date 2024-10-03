package com.salesforce.marketingcloud.reactnative;

import static com.salesforce.marketingcloud.reactnative.SFMCMessagingSerializer.KEY_MESSAGE_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk;


import java.util.HashMap;

public class SFMCMessagingReceiver extends BroadcastReceiver {

    private static final String TAG = "SFMCMessagingReceiver";
    static HashMap<String, RemoteMessage> notifications = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "broadcast received for message SFMCMessagingReceiver");

        RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());

        if (remoteMessage.getData().containsKey("_sid")) {

            // Add a RemoteMessage if the message contains a notification payload
            if (remoteMessage.getNotification() != null) {
                notifications.put(remoteMessage.getMessageId(), remoteMessage);
            }


            //  |-> ---------------------
            //      App in Foreground
            //   ------------------------
            if (SharedUtils.isAppInForeground(context)) {


                SFMCSdk.requestSdk(sfmcSdk -> {
                    Log.d(TAG, "requestSdk");
                    sfmcSdk.mp(pushModuleInterface -> {
                        Log.d(TAG, "handleMessage");
                        pushModuleInterface.getPushMessageManager().handleMessage(remoteMessage);
                    });
                });

                return;
            }


            //  |-> ---------------------
            //    App in Background
            //   ------------------------

            Data data = new Data.Builder()
                    .putString(KEY_MESSAGE_ID, remoteMessage.getMessageId())
                    .putString("title", remoteMessage.getData().get("title"))
                    .putString("subtitle", remoteMessage.getData().get("subtitle"))
                    .putString("alert", remoteMessage.getData().get("alert"))
                    .putString("_sid", remoteMessage.getData().get("_sid"))
                    .putString("_od", remoteMessage.getData().get("_od"))
                    .putString("_r", remoteMessage.getData().get("_r"))
                    .putString("_m", remoteMessage.getData().get("_m"))
                    .putString("_h", remoteMessage.getData().get("_h"))
                    .build();


            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SFMCWorker.class)
                    .setInputData(data)
                    .build();

            WorkManager.getInstance(context).enqueue(workRequest);


        }


    }


}

