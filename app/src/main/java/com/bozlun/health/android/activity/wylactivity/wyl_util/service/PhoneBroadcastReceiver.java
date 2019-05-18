package com.bozlun.health.android.activity.wylactivity.wyl_util.service;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.utils.PhoneUtils;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.sdk.bluetooth.protocol.command.push.MsgCountPush;
import com.sdk.bluetooth.protocol.command.push.PhoneNamePush;
import com.sdk.bluetooth.utils.BackgroundThread;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.sdk.bluetooth.manage.AppsBluetoothManager;
import com.sdk.bluetooth.protocol.command.base.BaseCommand;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IDeviceControlPhone;
import com.veepoo.protocol.model.enums.ESocailMsg;
import com.veepoo.protocol.model.settings.ContentPhoneSetting;
import com.veepoo.protocol.model.settings.ContentSetting;
import com.yanzhenjie.permission.AndPermission;
import java.util.ArrayList;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by admin on 2017/5/14.\
 * 6.0 广播接收来电
 */

public class PhoneBroadcastReceiver extends BroadcastReceiver {

    private static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;//启动时就开启来电监听
    private static final String TAG = "PhoneBroadcastReceiver";
    private static final String H9_NAME_TAG = "H9";    //H9手表


    OnCallPhoneListener onClallListener;

    public void setOnClallListener(OnCallPhoneListener onClallListener) {
        this.onClallListener = onClallListener;
    }


    String phoneNumber = "";
    private String bleName;


    public PhoneBroadcastReceiver() {
        super();
        bleName = (String) SharedPreferencesUtils.readObject(MyApp.getContext().getApplicationContext(), Commont.BLENAME);
        if(!WatchUtils.isEmpty(bleName) && WatchUtils.isVPBleDevice(bleName)){
            //设置维亿魄系列的来电静音和挂断电话功能
//            setVPPhoneSetting();
            MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(MyApp.getPhoneSosOrDisPhone());
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //Log.e(TAG, "---------action---" + action);

        if(!AndPermission.hasPermissions(MyApp.getContext(),Manifest.permission.READ_CONTACTS)){
            AndPermission.with(MyApp.getContext()).runtime().permission(Manifest.permission.READ_CONTACTS,Manifest.permission.READ_CALL_LOG).start();
        }
        if(WatchUtils.isEmpty(action))
            return;

        //判断一下是否为空
        if(MyCommandManager.DEVICENAME == null)
            return;

        //呼入电话
        if (action.equals(B_PHONE_STATE) || action.equals("android.intent.action.PHONE_STATE")) {
            //Log.d(TAG, "---1111----");
            doReceivePhone(context, intent);
        }
    }

    /**
     * 处理电话广播.
     *
     * @param context
     * @param intent
     */
    public void doReceivePhone(Context context, Intent intent) {
        phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        //Log.d(TAG, "---phoneNumber--111--" + phoneNumber);
        if (!WatchUtils.isEmpty(phoneNumber)){
            //Log.d(TAG, "---phoneNumber----" + phoneNumber);
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            if (telephony == null)
                return;
            int state = telephony.getCallState();
            Log.d(TAG, "-----state-----" + state);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING://"[Broadcast]等待接电话="
                    Log.d(TAG, "------收到了来电广播---" + phoneNumber);
                    if (!WatchUtils.isEmpty(phoneNumber) && !WatchUtils.isEmpty(bleName)) {
                        if (onClallListener != null) {
                            onClallListener.callPhoneAlert(phoneNumber);
                        }
                        if (bleName.equals("bozlun")) {
                            sendH8PhoneAlert();
                        }

                        if (bleName.equals(H9_NAME_TAG)) {
                            getPeople(phoneNumber, context);
                        }
                    }


                    //W30
                    if(!WatchUtils.isEmpty(bleName) &&(bleName.equals("w30") || bleName.equals("W30"))){
                        if(!WatchUtils.isEmpty(phoneNumber)){
                            boolean isOn = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(),"w30sswitch_Phone",true);
                            if(isOn){
                                sendPhoneAlertData(phoneNumber,"W30");
                            }
                        }
                    }
                    //维亿魄系列
                    if(!WatchUtils.isEmpty(bleName) && WatchUtils.isVPBleDevice(bleName)){   //B30手环
                        sendPhoneAlertData(phoneNumber,"B30");
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:// "[Broadcast]挂断电话
                    Log.d(TAG, "------挂断电话--");
                    if (!WatchUtils.isEmpty(bleName) && bleName.equals("bozlun")||bleName.equals(H9_NAME_TAG)) {
                        missCallPhone();    //挂断电话
                    }
                    if(!WatchUtils.isEmpty(bleName) &&(bleName.equals("w30") || bleName.equals("W30"))){
                        disW30Phone();
                    }
                    if(!WatchUtils.isEmpty(bleName) && WatchUtils.isVPBleDevice(bleName)){
                        setB30DisPhone();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://"[Broadcast]通话中="
                    Log.d(TAG, "------通话中--");
                    if (!WatchUtils.isEmpty(bleName) && bleName.equals("bozlun")) {
                        missCallPhone();    //挂断电话
                    }
                    if(!WatchUtils.isEmpty(bleName) &&(bleName.equals("w30") || bleName.equals("W30"))){
                        disW30Phone();
                    }
                    if(!WatchUtils.isEmpty(bleName) && WatchUtils.isVPBleDevice(bleName)){
                        setB30DisPhone();
                    }
                    break;
            }
        }



    }

    //发送电话号码
    private void sendPhoneAlertData(String phoneNumber, String tag) {

        //判断是否有读取联系人和通讯录的权限
        if(!AndPermission.hasPermissions(MyApp.getContext(),Manifest.permission.READ_CONTACTS,Manifest.permission.READ_CALL_LOG)){
            AndPermission.with(MyApp.getContext()).runtime().permission(Manifest.permission.READ_CONTACTS
                    ,Manifest.permission.READ_CALL_LOG).start();
        }else{
            getPhoneContacts(phoneNumber, tag);
        }


    }

    private void disW30Phone() {
        MyApp.getInstance().getmW30SBLEManage().notifyMsgClose();
    }

    private void sendH8PhoneAlert() {
        MyApp.getInstance().h8BleManagerInstance().setPhoneAlert();
    }



    //挂断电话
    private void missCallPhone() {
        if (MyCommandManager.DEVICENAME == null)
            return;
        MyApp.getInstance().h8BleManagerInstance().canclePhoneAlert();

        if (bleName != null && !TextUtils.isEmpty(bleName)) {
            if (H9_NAME_TAG.equals(bleName)) {   //H9挂掉电话
                sendPhoneCallCommands("", "", PhoneNamePush.HangUp_Call_type, MsgCountPush.HANGUP_CALL_TYPE, 0);

            }
        }
    }

    /**
     * 通过输入获取电话号码
     */
    public void getPeople(String nunber, Context context) {
        boolean w30sswitch_Phone = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(),"w30sswitch_Phone",true);
        Log.d(TAG, "------收到了来电广播3---"  + "=====" + w30sswitch_Phone);
        try {
            Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI, nunber), new String[]{
                    ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.TYPE, ContactsContract.PhoneLookup.LABEL}, null, null, null);

            if (cursor != null && !WatchUtils.isEmpty("" + cursor.getCount() + "")) {
                if (cursor.getCount() == 0) {
//                    //没找到电话号码
//                    if (w30SBleName != null && !TextUtils.isEmpty(w30SBleName) && w30SBleName.equals("W30")) {
//                        if (w30sswitch_Phone) {
//                            Log.d(TAG, "------收到了来电广播4---" + w30SBleName + "=====" + w30sswitch_Phone + "===" + nunber);
//                            MyApp.getmW30SBLEManage().notifacePhone(nunber, 0x01);
//                        }
//                        return;
//                    }
                    if (bleName != null && !TextUtils.isEmpty(bleName) && bleName.equals(H9_NAME_TAG))
                        phonesig(nunber, 1);
                } else if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    if (bleName != null && !TextUtils.isEmpty(bleName) && bleName.equals(H9_NAME_TAG))
                        phonesig(cursor.getString(2), 2);
                }
            } else {
                if (bleName != null && !TextUtils.isEmpty(bleName) && bleName.equals(H9_NAME_TAG))
                    phonesig(phoneNumber, 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送电话号码或者昵称的方法
    public void phonesig(String num, int id) {
//        Log.d(TAG, "------num---" + num + "--id-" + id);
        if (!WatchUtils.isEmpty(bleName)) {
            try {
                if (H9_NAME_TAG.equals(bleName)) { //H9手表
//                    Log.d(TAG, "-----h9--" + bleName);
                    /**
                     * 判断H9是否打开电话提醒
                     */
                    boolean income_call = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext().getApplicationContext(), "H9_INCOME_CALL",false);
                    if (income_call) {
                        if (id == 2) {    //有名字
//                            notifyInfo = new NotifyInfo(num,phoneNumber,"");
                            Log.d(TAG, "---11--h9--" + bleName);
                            sendPhoneCallCommands(num, phoneNumber, PhoneNamePush.Incoming_Call_type, MsgCountPush.ICOMING_CALL_TYPE, 1);
                        } else {
                            Log.d(TAG, "---22--h9--" + SharedPreferencesUtils.readObject(MyApp.getContext().getApplicationContext(), "mylanya"));
                            sendPhoneCallCommands(num, num, PhoneNamePush.Incoming_Call_type, MsgCountPush.ICOMING_CALL_TYPE, 1);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 推送来电
     *
     * @param name
     * @param incomingNumber
     * @param callType
     * @param countType
     */
    private void sendPhoneCallCommands(String name, String incomingNumber, byte callType, byte countType, long delayTime) {
        byte[] bName = new byte[0x00];
        if (incomingNumber != null) {
            bName = incomingNumber.getBytes();
            if (name != null) {
                bName = name.getBytes();
            }
        }
        PhoneNamePush phonePush = new PhoneNamePush(commandResultCallback, callType, bName);
        MsgCountPush countPush = new MsgCountPush(commandResultCallback, countType, (byte) 0x01);
        ArrayList<BaseCommand> sendList = new ArrayList<>();
        sendList.add(phonePush);
        sendList.add(countPush);
        if (delayTime > 0) {
            final ArrayList<BaseCommand> commands = sendList;
            BackgroundThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppsBluetoothManager.getInstance(MyApp.getContext()).sendCommands(commands);
                }
            }, delayTime);
        } else {
            AppsBluetoothManager.getInstance(MyApp.getContext()).sendCommands(sendList);
        }
    }

    private BaseCommand.CommandResultCallback commandResultCallback = new BaseCommand.CommandResultCallback() {
        @Override
        public void onSuccess(BaseCommand baseCommand) {

        }

        @Override
        public void onFail(BaseCommand baseCommand) {

        }
    };

    public interface OnCallPhoneListener {
        void callPhoneAlert(String phoneTag);
    }

    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * 头像ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**
     * 联系人的ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    boolean isPhone = true;

    /**
     * 得到手机通讯录联系人信息
     **/
    private void getPhoneContacts(String pName, String tag) {

//        Log.e(TAG,"----pname-="+pName);
        ContentResolver resolver = MyApp.getInstance().getContentResolver();
        if (resolver == null) return;
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);

        if (phoneCursor != null) {
//            Log.e(TAG,"---phoneCursor.size="+phoneCursor.getCount());
            while (phoneCursor.moveToNext()) {
                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
//                Log.e(TAG,"----ph--="+phoneNumber);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (!WatchUtils.isEmpty(phoneNumber)) {
                    if(phoneNumber.contains("-")){
                        phoneNumber = phoneNumber.replace("-", "");
                    }
                    if(phoneNumber.contains(" ")){
                        phoneNumber = phoneNumber.replace(" ", "");
                    }

                    if (pName.equals(phoneNumber)) {
                        isPhone = false;
                        String contactNames = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX) + "";
                        if (WatchUtils.isEmpty(contactNames)) contactNames = "null";
//                        Log.e(TAG,"-----相等---="+contactNames);
                        if (tag.equals("B30")) {
                            setB30PhoneMsg(contactNames);
                        } else if (tag.equals("W30")) {
                            MyApp.getInstance().getmW30SBLEManage().notifacePhone(contactNames, 0x01);
                        }

                        return;
                    }
                }
//                Log.e(TAG,"-----phoneNum-="+phoneNumber);
                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
//                Log.e(TAG,"----contactName--="+contactName);

            }
            if (isPhone) {
                if (tag.equals("B30")) {
                    setB30PhoneMsg("");
                } else if (tag.equals("W30")) {
                    MyApp.getInstance().getmW30SBLEManage().notifacePhone(pName, 0x01);
                }

            }

            phoneCursor.close();
        } else {
            if (tag.equals("B30")) {
                setB30PhoneMsg("");
            } else if (tag.equals("W30")) {
                MyApp.getInstance().getmW30SBLEManage().notifacePhone(pName, 0x01);
            }
            // MyApp.getmW30SBLEManage().notifacePhone(pName,0x01);
        }
    }

    //发送来电指令
    private void setB30PhoneMsg(String peopleName) {
        if(MyCommandManager.DEVICENAME != null){
            boolean callPhone = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISCallPhone, true);//来电
            if(!callPhone)
                return;
            ContentSetting contentSetting = new ContentPhoneSetting(ESocailMsg.PHONE,peopleName, phoneNumber);
            MyApp.getInstance().getVpOperateManager().sendSocialMsgContent(iBleWriteResponse, contentSetting);
        }

    }

    /**
     * B30电话挂断
     */
    private void setB30DisPhone() {
        if(MyCommandManager.DEVICENAME != null){
            MyApp.getInstance().getVpOperateManager().offhookOrIdlePhone(iBleWriteResponse);
            getDoNotDisturb();
            boolean isTrue = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(),"phone_status",false);
            if(isTrue){
                try {
                    AudioManager audioManager = (AudioManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    if(audioManager != null){
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        // audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        Log.d("SilentListenerService", "RINGING 取消静音");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }



    }

    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };




    private void setVPPhoneSetting() {
        MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(new IDeviceControlPhone() {
            //挂断电话提醒
            @Override
            public void rejectPhone() {// 挂电话还没弄好
                try {
//                    TelephonyManager tm = (TelephonyManager) MyApp.getContext()
//                            .getSystemService(Service.TELEPHONY_SERVICE);
//                    PhoneUtils.endPhone(MyApp.getContext(),tm);
                    PhoneUtils.dPhone();
                    PhoneUtils.endCall(MyApp.getContext());
                    //PhoneUtils.endcall();
                    Log.d("call---", "rejectPhone: " + "电话被挂断了");
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            //手环静音提示
            @Override
            public void cliencePhone() {
                getDoNotDisturb();
                //正常模式静音
                if(WatchUtils.getPhoneStatus() == AudioManager.RINGER_MODE_NORMAL){
                    SharedPreferencesUtils.setParam(MyApp.getContext(),"phone_status",true);
                    AudioManager audioManager = (AudioManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        Log.d("call---", "RINGING 已被静音");
                    }
                }

            }

            //手环敲击提醒
            @Override
            public void knocknotify(int i) {

            }

            @Override
            public void sos() {

            }


        });
    }

    //获取Do not disturb权限,才可进行音量操作
    private void getDoNotDisturb(){
      NotificationManager notificationManager =
                (NotificationManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            MyApp.getInstance().getApplicationContext().startActivity(intent);
        }

    }


}