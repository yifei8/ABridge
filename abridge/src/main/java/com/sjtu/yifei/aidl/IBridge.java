package com.sjtu.yifei.aidl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 类描述：
 * 创建人：yifei
 * 创建时间：2018/5/15
 * 修改人：
 * 修改时间：
 * 修改备注：
 */

public final class IBridge {

    private static final String BIND_SERVICE_ACTION = "android.intent.action.ICALL_AIDL_YIFEI";
    private static final String BIND_SERVICE_COMPONENT_NAME_CLS = "com.sjtu.yifei.service.ABridgeService";

    private IBridge() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;
    private static String sServicePkgName;

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param app application
     */
    public static void init(@NonNull final Application app, String servicePkgName) {
        if (sApplication == null) {
            sApplication = app;
            if (!TextUtils.isEmpty(servicePkgName)) {
                sServicePkgName = servicePkgName;
            }
            handler = new Handler(sApplication.getMainLooper());
            sApplication.registerActivityLifecycleCallbacks(mCallbacks);
        }
    }

    private static void init(@NonNull final Application app) {
        init(app, null);
    }

    private static Handler handler;

    private static final LinkedList<Activity> ACTIVITY_LIST = new LinkedList<>();

    private static ActivityLifecycleCallbacks mCallbacks = new ActivityLifecycleCallbacks() {

        private List<Activity> activities = new ArrayList<>();
        private IBinder sBinder = new Binder();
        
        private ISenderAidlInterface iRemoteCall;
        
        private IReceiverAidlInterface iRemoteCallback = new IReceiverAidlInterface.Stub() {

            @Override
            public void receiveMessage(final String json) throws RemoteException {
                handler.post(new Runnable() {
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
                iRemoteCall = ISenderAidlInterface.Stub.asInterface(iBinder);
                if (activities.get(0) instanceof IReceiver) {
                    ((IReceiver) activities.get(0)).setSender(new ISenderImp(iRemoteCall));
                }
                try {
                    iRemoteCall.join(sBinder);
                    iRemoteCall.registerCallback(iRemoteCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                iRemoteCall = null;
            }
        };

        private void startAndBindService() {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(BIND_SERVICE_ACTION);
            serviceIntent.setComponent(new ComponentName(sServicePkgName, BIND_SERVICE_COMPONENT_NAME_CLS));
            activities.get(0).startService(serviceIntent);
            activities.get(0).bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        private void unBindService() {
            try {
                iRemoteCall.unregisterCallback(iRemoteCallback);
                iRemoteCall.leave(sBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            activities.get(0).unbindService(serviceConnection);
        }
        
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            setTopActivity(activity);
            if (activity instanceof IReceiver) {
                activities.add(activity);
                startAndBindService();
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isFinishing() && ACTIVITY_LIST.size() == 1) {
                unBindService();
                activities.remove(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            ACTIVITY_LIST.remove(activity);
        }
    };

    static class ISenderImp implements ISender {
        private ISenderAidlInterface iRemoteCall;

        private ISenderImp(ISenderAidlInterface iRemoteCall) {
            this.iRemoteCall = iRemoteCall;
        }

        @Override
        public void sendMessage(String message) {
            try {
                iRemoteCall.sendMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return the context of Application object.
     *
     * @return the context of Application object
     */
    private static Application getApp() {
        if (sApplication != null) return sApplication;
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(at);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            init((Application) app);
            return sApplication;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    private static void setTopActivity(final Activity activity) {
        if (ACTIVITY_LIST.contains(activity)) {
            if (!ACTIVITY_LIST.getLast().equals(activity)) {
                ACTIVITY_LIST.remove(activity);
                ACTIVITY_LIST.addLast(activity);
            }
        } else {
            ACTIVITY_LIST.addLast(activity);
        }
    }

    static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIST;
    }

    private static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity topActivity = getTopActivity();
            return topActivity == null ? IBridge.getApp() : topActivity;
        } else {
            return IBridge.getApp();
        }
    }

    private static boolean isAppForeground() {
        ActivityManager am =
                (ActivityManager) IBridge.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName.equals(IBridge.getApp().getPackageName());
            }
        }
        return false;
    }

    private static Activity getTopActivity() {
        if (!ACTIVITY_LIST.isEmpty()) {
            final Activity topActivity = ACTIVITY_LIST.getLast();
            if (topActivity != null) {
                return topActivity;
            }
        }
        // using reflect to get top activity
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            if (activities == null) return null;
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    IBridge.setTopActivity(activity);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

}
