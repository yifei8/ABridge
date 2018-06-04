package com.sjtu.yifei.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.sjtu.yifei.IBridge;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：
 * 创建人：yifei
 * 创建时间：2018/6/4
 * 修改人：
 * 修改时间：
 * 修改备注：
 */

public class MsgerActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final String BIND_SERVICE_ACTION = "android.intent.action.ICALL_MESSENGER_YIFEI";
    private static final String BIND_MESSENGER_SERVICE_COMPONENT_NAME_CLS = "com.sjtu.yifei.service.MessengerService";

    private List<Activity> activities = new ArrayList<>();

    //客户端的Messnger
    @SuppressLint("HandlerLeak")
    private Messenger replyMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (activities.get(0) instanceof MessengerReceiver) {
                ((MessengerReceiver) activities.get(0)).receiveMessage(msg);
            }
        }
    });

    //服务端传来的Messenger
    private Messenger sMessenger;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMessenger = new Messenger(service);
            if (activities.get(0) instanceof MessengerReceiver) {
                ((MessengerReceiver) activities.get(0)).setSender(new MessengerSenderImp(sMessenger, replyMessenger));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sMessenger = null;
        }
    };

    private void startAndBindService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(BIND_SERVICE_ACTION);
        serviceIntent.setComponent(new ComponentName(IBridge.sServicePkgName, BIND_MESSENGER_SERVICE_COMPONENT_NAME_CLS));
        activities.get(0).startService(serviceIntent);
        activities.get(0).bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        Message message = Message.obtain();
        message.arg1 = 0x0000c1;
        Bundle bundle = new Bundle();
        bundle.putString("MessengerService", "unregisterCallback");
        message.setData(bundle);

        try {
            //消息从客户端发出
            sMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        activities.get(0).unbindService(serviceConnection);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof MessengerReceiver) {
            activities.add(activity);
            startAndBindService();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity.isFinishing() && activity instanceof MessengerReceiver) {
            unBindService();
            activities.remove(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    static class MessengerSenderImp implements MessengerSender {
        private Messenger messenger;
        private Messenger replyMessenger;

        private MessengerSenderImp(Messenger messenger, Messenger replyMessenger) {
            this.messenger = messenger;
            this.replyMessenger = replyMessenger;
        }

        @Override
        public void sendMessage(Message message) {
            try {
                message.replyTo = replyMessenger;
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
