package com.sjtu.yifei.aidl;


import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
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

public class AidlActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final String BIND_SERVICE_ACTION = "android.intent.action.ICALL_AIDL_YIFEI";
    private static final String BIND_SERVICE_COMPONENT_NAME_CLS = "com.sjtu.yifei.service.ABridgeService";

    private List<Activity> activities = new ArrayList<>();
    private IBinder sBinder = new Binder();

    private ISenderAidlInterface iSenderAidlInterface;

    private IReceiverAidlInterface iReceiverAidlInterface = new IReceiverAidlInterface.Stub() {

        @Override
        public void receiveMessage(final String json) throws RemoteException {
            IBridge.handler.post(new Runnable() {
                @Override
                public void run() {
                    if (activities.get(0) instanceof IReceiver) {
                        ((IReceiver) activities.get(0)).receiveMessage(json);
                    }
                }
            });
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iSenderAidlInterface = ISenderAidlInterface.Stub.asInterface(iBinder);
            if (activities.get(0) instanceof IReceiver) {
                ((IReceiver) activities.get(0)).setSender(new ISenderImp(iSenderAidlInterface));
            }
            try {
                iSenderAidlInterface.join(sBinder);
                iSenderAidlInterface.registerCallback(iReceiverAidlInterface);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iSenderAidlInterface = null;
        }
    };

    private void startAndBindService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(BIND_SERVICE_ACTION);
        serviceIntent.setComponent(new ComponentName(IBridge.sServicePkgName, BIND_SERVICE_COMPONENT_NAME_CLS));
        activities.get(0).startService(serviceIntent);
        activities.get(0).bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        try {
            iSenderAidlInterface.unregisterCallback(iReceiverAidlInterface);
            iSenderAidlInterface.leave(sBinder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        activities.get(0).unbindService(serviceConnection);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof IReceiver) {
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
        if (activity.isFinishing() && activity instanceof IReceiver) {
            unBindService();
            activities.remove(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    static class ISenderImp implements ISender {
        private ISenderAidlInterface iSenderAidlInterface1;

        private ISenderImp(ISenderAidlInterface iSenderAidlInterface1) {
            this.iSenderAidlInterface1 = iSenderAidlInterface1;
        }

        @Override
        public void sendMessage(String message) {
            try {
                iSenderAidlInterface1.sendMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
