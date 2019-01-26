package com.bozlun.health.android.util;

import android.content.Context;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by An on 2018/9/19.
 */

public class PhoneUtile {

    /**
     * 从TelephonyManager中实例化ITelephony,并返回
     */
    static public ITelephony getITelephony(TelephonyManager telMgr)
            throws Exception {
        Method getITelephonyMethod = telMgr.getClass().getDeclaredMethod(
                "getITelephony");
        getITelephonyMethod.setAccessible(true);// 私有化函数也能使用
        return (ITelephony) getITelephonyMethod.invoke(telMgr);
    }

    //自动挂断
    public static void endPhone(TelephonyManager tm) {
        try {
            ITelephony iTelephony;
            Method getITelephonyMethod = TelephonyManager.class
                    .getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            iTelephony = (ITelephony) getITelephonyMethod.invoke(tm,
                    (Object[]) null);
            // 挂断电话
            iTelephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endCall() {
        try {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);
            IBinder ibinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            ITelephony iTelephony = ITelephony.Stub.asInterface(ibinder);
            iTelephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 挂断电话
     * @param context
     */
    public static void endCall(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();

                Method endCallMethod = telephonyClass.getMethod("endCall");
                endCallMethod.setAccessible(true);

                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Object getTelephonyObject(Context context) {
        Object telephonyObject = null;
        try {
            // 初始化iTelephony
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // Will be used to invoke hidden methods with reflection
            // Get the current object implementing ITelephony interface
            Class telManager = telephonyManager.getClass();
            Method getITelephony = telManager.getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            telephonyObject = getITelephony.invoke(telephonyManager);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return telephonyObject;
    }



}
