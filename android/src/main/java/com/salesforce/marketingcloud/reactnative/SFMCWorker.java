package com.salesforce.marketingcloud.reactnative;

import static com.salesforce.marketingcloud.reactnative.SFMCMessagingSerializer.KEY_MESSAGE_ID;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk;

import java.util.HashMap;
import java.util.Map;

public class SFMCWorker extends Worker {
    private static final String TAG = "SFMCWorker";

    public SFMCWorker(Context context, WorkerParameters parameters) {
        super(context, parameters);
    }


    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SFMCWorker");


        String messageId = getInputData().getString(KEY_MESSAGE_ID);
        String title = getInputData().getString("title");
        String subtitle = getInputData().getString("subtitle");
        String alert = getInputData().getString("alert");
        String _sid = getInputData().getString("_sid");
        String _od = getInputData().getString("_od");
        String _r = getInputData().getString("_r");
        String _m = getInputData().getString("_m");
        String _h = getInputData().getString("_h");

        Map<String, String> data = new HashMap<>();

        data.put(KEY_MESSAGE_ID, messageId);
        data.put("title", title);
        data.put("subtitle", subtitle);
        data.put("alert", alert);
        data.put("_sid", _sid);
        data.put("_od", _od);
        data.put("_r", _r);
        data.put("_m", _m);
        data.put("_h", _h);
        data.put("priority", "high");
        data.put("channel", "general");
        data.put("semPriority", "1");

        Log.d(TAG, data.toString());

        RemoteMessage remoteMessage = new RemoteMessage.Builder(messageId)
                .setData(data)
                .build();

        SFMCSdk.requestSdk(sfmcSdk -> {
            Log.d(TAG, "requestSdk");
            sfmcSdk.mp(pushModuleInterface -> {
                Log.d(TAG, "handleMessage");
                pushModuleInterface.getPushMessageManager().handleMessage(remoteMessage);
            });
        });



        return Result.success();
    }
}

