package com.bozlun.health.android.bzlmaps.sos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import java.util.ArrayList;

public class SendSMSBroadCast extends BroadcastReceiver {
    String stringpersonOne = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personOne", "");
    String stringpersonTwo = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personTwo", "");
    String stringpersonThree = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personThree", "");

    int ComeInNumber = 0;
    boolean isOne = true;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("----------AA", "定位成功--广播=   要去发短息你的节奏啊" );
        if (!WatchUtils.isEmpty(action)
                && action.equals("com.example.bozhilun.android.sos.SENDSMS")) {
            stringpersonOne = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personOne", "");
            stringpersonTwo = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personTwo", "");
            stringpersonThree = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personThree", "");
            String msm = intent.getStringExtra("msm");
            String gps = intent.getStringExtra("gps");
            Log.e("----------AA", "消息成功--广播2=" + msm);
            Log.e("----------AA", "定位成功--广播2=" + gps);
            if (!WatchUtils.isEmpty(msm) && !WatchUtils.isEmpty(gps)) {
                Message message = new Message();
                message.what = 0x01;

                Bundle bundle = new Bundle();
                bundle.putString("sms", msm);  //往Bundle中存放数据
                bundle.putString("gps", gps);  //往Bundle中put数据
                message.setData(bundle);//mes利用Bundle传递数据

//                message.obj = msm;
                mHandler.sendMessage(message);
            }
        } else if (!WatchUtils.isEmpty(action) && action.equals("com.example.bozhilun.android.b30.service.UploadServiceGD")) {
            if (mHandler != null) mHandler.sendEmptyMessage(0x02);
        }
    }


    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0x01:
                    String sms = message.getData().getString("sms");//接受msg传递过来的参数
                    String gps = message.getData().getString("gps");//接受msg传递过来的参数

                    ComeInNumber++;
//                    Log.d("----------AA", "消息" + ComeInNumber);
                    if (ComeInNumber >= 2 && !isOne) {
                        isOne = true;
                        if (!WatchUtils.isEmpty(stringpersonOne)) {
//                            Log.d("----------AA", "发送给手机一");
//                            SmsManager.getDefault().sendTextMessage(stringpersonOne, null, gps, null, null);

                            sendMS(stringpersonOne,gps);
                        }
                        if (!WatchUtils.isEmpty(stringpersonTwo)) {
//                            Log.d("----------AA", "发送给手机二");
//                            SmsManager.getDefault().sendTextMessage(stringpersonTwo, null, gps, null, null);

                            sendMS(stringpersonTwo,gps);
                        }
                        if (!WatchUtils.isEmpty(stringpersonThree)) {
//                            Log.d("----------AA", "发送给手机三");
//                            SmsManager.getDefault().sendTextMessage(stringpersonThree, null, gps, null, null);

                            sendMS(stringpersonThree,gps);
                        }
                        ComeInNumber = 0;
                    } else if (ComeInNumber >= 2 && isOne) {
                        isOne = false;
                        if (!WatchUtils.isEmpty(stringpersonOne)) {
//                                Log.d("----------AA", "发送给手机一");
//                            SmsManager.getDefault().sendTextMessage(stringpersonOne, null, sms, null, null);

                            sendMS(stringpersonOne,gps);
                        }
                        if (!WatchUtils.isEmpty(stringpersonTwo)) {
//                                Log.d("----------AA", "发送给手机二");
//                            SmsManager.getDefault().sendTextMessage(stringpersonTwo, null, sms, null, null);
                            sendMS(stringpersonTwo,gps);
                        }
                        if (!WatchUtils.isEmpty(stringpersonThree)) {
//                                Log.d("----------AA", "发送给手机三");
//                            SmsManager.getDefault().sendTextMessage(stringpersonThree, null, sms, null, null);

                            sendMS(stringpersonThree,gps);
                        }
                    } else {
                        if (isOne) {
                            isOne = false;
                            if (!WatchUtils.isEmpty(stringpersonOne)) {
//                                Log.d("----------AA", "发送给手机一");
                                SmsManager.getDefault().sendTextMessage(stringpersonOne, null, sms, null, null);
                            }
                            if (!WatchUtils.isEmpty(stringpersonTwo)) {
//                                Log.d("----------AA", "发送给手机二");
                                SmsManager.getDefault().sendTextMessage(stringpersonTwo, null, sms, null, null);
                            }
                            if (!WatchUtils.isEmpty(stringpersonThree)) {
//                                Log.d("----------AA", "发送给手机三");
                                SmsManager.getDefault().sendTextMessage(stringpersonThree, null, sms, null, null);
                            }
                        }
                    }
                    break;
                case 0x02:
                    ToastUtil.showShort(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.wangluo));
                    break;
            }
            return false;
        }
    });

    void sendMS(String phone,String message) {

        if (message.length() > 70) {
            ArrayList<String> msgs = SmsManager.getDefault().divideMessage(message);
            for (String msg : msgs) {
                if (msg != null) {
                    SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
                }
            }
        } else {
            SmsManager.getDefault().sendTextMessage(phone, null, message, null, null);
        }
    }
}