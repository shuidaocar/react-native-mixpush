package com.duanglink.huaweipush;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.duanglink.mipush.Content;
import com.duanglink.rnmixpush.MixPushMoudle;
import com.huawei.hms.support.api.push.PushReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.yzy.voice.VoicePlay;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wangheng on 2017/11/22.
 */
public class HuaweiPushMessageReceiver extends PushReceiver {
    public static final String TAG = "HuaweiPushRevicer";
    private static final int MSG = 2;

    public static final String ACTION_UPDATEUI = "action.updateUI";
    private String content;
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        Log.i(TAG, "得到Token为:" + token);
        //Toast.makeText(context, "Token为:" + token, Toast.LENGTH_SHORT).show();
        //MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLIENTID, token);
        //延时1秒后再发送事件，防止RN客户端还未初始化完成时在注册前就发送了事件
        try {
            final String stoken = token;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLIENTID, stoken);
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            //CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            Log.i(TAG, "收到PUSH透传消息,消息内容为:" + content);
            Gson gson = new Gson();
            HuaweiContent data = gson.fromJson(content, HuaweiContent.class);
            Log.i(TAG, "消息内容为:" + data.getContent());

            if (data.getMsg_sub_type().equals("103")) {
                VoicePlay.with(context).play(data.getContent());
            }
            MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_REMOTE_NOTIFICATION, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        Log.i("====>", isFirst+"-==");
        if (isFirst) {
            MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLICK_NOTIFICATION, content);
            handler.removeMessages(MSG);
            return;
        }
        handler.sendEmptyMessageDelayed(MSG, 500);
    }

    public void onEvent(Context context, Event event, Bundle extras) {
        Log.i(TAG, "收到通知栏消息点击事件");
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            String message = extras.getString(BOUND_KEY.pushMsgKey);
            content = parseMessage(message);
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);

            Log.i(TAG, "收到通知栏消息点击事件,notifyId:" + notifyId + ",message:" + content);
//            TimerTask task = new TimerTask() {
//                @Override
//                public void run() {
//                    MixPushMoudle.sendEvent(MixPushMoudle.EVENT_RECEIVE_CLICK_NOTIFICATION, content);
//                }
//            };
//            Timer timer = new Timer();
//            timer.schedule(task, 1000);
            Looper.prepare();
            Message msg = new Message();
            msg.what = MSG;
            handler.sendMessage(msg);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
        }
        super.onEvent(context, event, extras);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
        Log.i(TAG, "Push连接状态为:" + pushState);
        //Toast.makeText(context, "Push连接状态为:" + pushState, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATEUI);
        intent.putExtra("type", 2);
        intent.putExtra("pushState", pushState);
        context.sendBroadcast(intent);
    }

    private String parseMessage(String message) {
        JSONObject info = new JSONObject();
        try {
            JSONArray json = new JSONArray(message);
            if (json.length() > 0) {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject job = json.getJSONObject(i);
                    Iterator<String> sIterator = job.keys();
                    while (sIterator.hasNext()) {
                        String key = sIterator.next();
                        String value = job.getString(key);
                        info.put(key, value);
                    }
                }
            }
        } catch (JSONException e) {

        }
        return info.toString();
    }
}
