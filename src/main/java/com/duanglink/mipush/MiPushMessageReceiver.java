package com.duanglink.mipush;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.duanglink.rnmixpush.MixPushMoudle;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONException;

import com.google.gson.Gson;
import com.yzy.voice.VoicePlay;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wangheng on 2017/11/22.
 */
public class MiPushMessageReceiver extends PushMessageReceiver {
    private static final String TAG = "MiPushMessageReceiver";
    private static final int MSG = 1;

    private String mRegId;
    private long mResultCode = -1;
    private String mReason;
    private String mCommand;
    private String mMessage;
    private String mTopic;
    private String mAlias;
    private String mUserAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        Log.i(TAG, "收到透传消息11： " + message.toString());

        Log.i(TAG, "收到透传消息： " + mMessage);
        Gson gson = new Gson();
        Content content = gson.fromJson(mMessage, Content.class);
        if (content.getMsg_sub_type().equals("103")) {
            VoicePlay.with(context).play(content.getAmount());
            Log.i(TAG, "消息： " + content.getAmount());
        }
        MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_REMOTE_NOTIFICATION, mMessage);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG:
                    doPolling(Content.finishStart);
                    break;
            }
        }
    };

    private void doPolling(Boolean isFirst) {
        Log.i("====>doPolling", isFirst+ "");
        if (isFirst) {
            MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLICK_NOTIFICATION, mMessage);
            handler.removeMessages(MSG);
            return;
        }
        handler.sendEmptyMessageDelayed(MSG, 500);
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        try {
            final String extra = mapToJsonString(message.getExtra());
            //JSONObject.
            Log.i(TAG, "点击通知栏消息： " + mMessage + ",透传消息：" + extra);
            //启动应用
            Intent launchIntent = context.getPackageManager().
                    getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            context.startActivity(launchIntent);
            Looper.prepare();
            Message msg = new Message();
            msg.what = MSG;
            handler.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        Log.i(TAG, "收到通知栏消息： " + mMessage);
        Gson gson = new Gson();
        Content content = gson.fromJson(mMessage, Content.class);
        if (content.getMsg_sub_type().equals("103")) {
            VoicePlay.with(context).play(content.getAmount());
            Log.i(TAG, "消息： " + content.getAmount());
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_REMOTE_NOTIFICATION, mMessage);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000);
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
            }
        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        try {
            String command = message.getCommand();
            List<String> arguments = message.getCommandArguments();
            String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
            String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
            if (MiPushClient.COMMAND_REGISTER.equals(command)) {
                if (message.getResultCode() == ErrorCode.SUCCESS) {
                    mRegId = cmdArg1;
                    Log.i(TAG, "得到RegId： " + mRegId);
//                     TimerTask task = new TimerTask() {
//                         @Override
//                         public void run() {
//                             MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLIENTID, mRegId);
//                         }
//                     };
//                     Timer timer = new Timer();
//                     timer.schedule(task, 1000);
                }
            }
        } catch (Exception e) {

        }
    }

    private String mapToJsonString(Map<String, String> map) throws JSONException {
        JSONObject info = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            info.put(entry.getKey(), entry.getValue());
        }
        return info.toString();
    }
}
