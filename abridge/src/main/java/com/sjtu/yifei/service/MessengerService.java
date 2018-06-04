package com.sjtu.yifei.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class MessengerService extends Service {
    public final static String TAG = "RemoteService";

    @SuppressLint("HandlerLeak")
    private Messenger messenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                msg.replyTo.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    });

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}
