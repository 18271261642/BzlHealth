package com.bozlun.health.android.activity.wylactivity.wyl_util.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.bozlun.health.android.bi8i.b18iutils.B18iUtils;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bean.MessageEvent;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.sdk.bluetooth.protocol.command.push.CalanderPush;
import com.sdk.bluetooth.protocol.command.push.MsgCountPush;
import com.sdk.bluetooth.protocol.command.push.SmsPush;
import com.sdk.bluetooth.protocol.command.push.SocialPush;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.sdk.bluetooth.manage.AppsBluetoothManager;
import com.sdk.bluetooth.manage.GlobalVarManager;
import com.sdk.bluetooth.protocol.command.base.BaseCommand;
import com.suchengkeji.android.w30sblelibrary.W30SBLEManage;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.model.enums.ESocailMsg;
import com.veepoo.protocol.model.settings.ContentSetting;
import com.veepoo.protocol.model.settings.ContentSmsSetting;
import com.veepoo.protocol.model.settings.ContentSocailSetting;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Comment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * 提醒服务  MyNotificationListenerService
 * 通过通知获取APP消息内容，需要打开通知功能
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AlertService extends MyNotificationListenerService {
    private static final String TAG = "AlertService";
    private static final String H8_NAME_TAG = "bozlun";

    //QQ
    private static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
    //微信
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    //微博
    private static final String WEIBO_PACKAGENAME = "com.sina.weibo";
    //Facebook
    private static final String FACEBOOK_PACKAGENAME = "com.facebook.katana";

    private static final String FACEBOOK_PACKAGENAME1 = "com.facebook.orca";
    //twitter
    private static final String TWITTER_PACKAGENAME = "com.twitter.android";
    //Whats
    private static final String WHATS_PACKAGENAME = "com.whatsapp";
    //viber
    private static final String VIBER_PACKAGENAME = "com.viber.voip";
    //instagram
    private static final String INSTANRAM_PACKAGENAME = "com.instagram.android";
    //日历
    private static final String CALENDAR_PACKAGENAME = "com.android.calendar";
    //信息 三星手机信息
    private static final String SAMSUNG_MSG_PACKNAME = "com.samsung.android.messaging";
    private static final String SAMSUNG_MSG_SRVERPCKNAME = "com.samsung.android.communicationservice";
    private static final String MSG_PACKAGENAME = "com.android.mms";//短信--- 小米
    private static final String SYS_SMS = "com.android.mms.service";//短信 --- vivo Y85A
    private static final String SKYPE_PACKAGENAME = "com.skype.raider";
    private static final String SKYPE_PACKNAME = "com.skype.rover";
    //line
    private static final String LINE_PACKAGENAME = "jp.naver.line.android";

    private String newmsg = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "----------1111-onCreate--");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
        Log.d(TAG, "---------222-onCreate--");
    }

    //当系统收到新的通知后出发回调
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            //获取应用包名
            String packageName = sbn.getPackageName();
            Log.e(TAG, "-----packName-----"+packageName);
            //获取notification对象
            Notification notification = sbn.getNotification();
            //获取消息内容
            CharSequence tickerText = notification.tickerText;
            //Log.e(TAG,"------tickerText="+tickerText);
            if (tickerText != null) {
                if (MyCommandManager.DEVICENAME == null)
                    return;
                String msgCont = tickerText.toString();
                if (WatchUtils.isEmpty(msgCont) || msgCont.equals("[]"))
                    return;
                Log.e(TAG, "-------tickerText----" + tickerText);
                Log.e(TAG, "-------newmsg--2--" + msgCont);
                //line
                if (packageName.equals(LINE_PACKAGENAME)) {
                    boolean isLine = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISLINE, false);
                    if (isLine)
                        sendB30Msg(ESocailMsg.LINE, "Line", msgCont);

                }
                //QQ
                else if (packageName.equals(QQ_PACKAGENAME)) {
                    boolean isQQ = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISQQ, false);
                    if (isQQ)
                        sendB30Msg(ESocailMsg.QQ, "QQ", msgCont);
                    // 微信
                } else if (packageName.equals(WECHAT_PACKAGENAME)) {
                    boolean isWetch = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISWechart, false);
                    if (isWetch)
                        sendB30Msg(ESocailMsg.WECHAT, "Wechat", msgCont);

                }
                //facebook
                else if (packageName.equals(FACEBOOK_PACKAGENAME) || packageName.equals(FACEBOOK_PACKAGENAME1)) {
                    boolean isFaceBook = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISFacebook, false);
                    if (isFaceBook)
                        sendB30Msg(ESocailMsg.FACEBOOK, "FaceBook", msgCont);
                }
                //Twitter
                else if (packageName.equals(TWITTER_PACKAGENAME)) {
                    boolean isTwitter = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISTwitter, false);
                    if (isTwitter)
                        sendB30Msg(ESocailMsg.TWITTER, "Twitter", msgCont);
                }
                //Whats
                else if (packageName.equals(WHATS_PACKAGENAME)) {
                    boolean isWhats = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISWhatsApp, false);
                    if (isWhats)
                        sendB30Msg(ESocailMsg.WHATS, "Whats", msgCont);
                }    //Instagram
                else if (packageName.equals(INSTANRAM_PACKAGENAME)) {
                    sendB30Msg(ESocailMsg.INSTAGRAM, "Instagram", msgCont);
                }
                //sky
                else if (packageName.equals(SKYPE_PACKAGENAME) || packageName.equals(SKYPE_PACKNAME)) {
                    boolean isSkey = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSkype, false);
                    if (isSkey)
                        sendB30Mesage(ESocailMsg.SKYPE, "Skype", msgCont);
                }
                //短信
                else if (packageName.equals(MSG_PACKAGENAME)
                        || packageName.equals(SYS_SMS)
                        || packageName.equals(SAMSUNG_MSG_PACKNAME)
                        || packageName.equals(SAMSUNG_MSG_SRVERPCKNAME)) {

                    boolean isSMS = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(),Commont.ISMsm,false);
                    if (isSMS)
                      sendB30Mesage(ESocailMsg.SMS, "MMS", msgCont);

                } else {
                    //其它
                    boolean isOther = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISOther, false);
                    if (isOther)
                        sendB30Mesage(ESocailMsg.OTHER, "", msgCont);

                }

            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    //推送B30的消息提醒
    private void sendB30Msg(ESocailMsg b30msg, String appName, String context) {
        Log.e(TAG, "------name=" + MyCommandManager.DEVICENAME);
        if (!WatchUtils.isEmpty(MyCommandManager.DEVICENAME)&&WatchUtils.isVPBleDevice(MyCommandManager.DEVICENAME)) {
            ContentSocailSetting contentSocailSetting = new ContentSocailSetting(b30msg, appName, context);
            //ContentSetting contentSetting = new ContentSocailSetting(b30msg, 0, 20, appName, context);
            MyApp.getInstance().getVpOperateManager().sendSocialMsgContent(iBleWriteResponse, contentSocailSetting);
        }
    }


    //B30的短信
    private void sendB30Mesage(ESocailMsg b30msg, String appName, String context) {
        if (MyCommandManager.DEVICENAME != null && WatchUtils.isVPBleDevice(MyCommandManager.DEVICENAME)) {
            ContentSetting msgConn = new ContentSmsSetting(b30msg, appName, context);
            // ContentSetting contentSetting = new ContentSmsSetting(b30msg, 0, 20, appName, context);
            MyApp.getInstance().getVpOperateManager().sendSocialMsgContent(iBleWriteResponse, msgConn);
        }

    }


    /**
     * 日历消息推送
     *
     * @param newmsg
     */
    private void sendCalendarsH9(String newmsg) {
        String mylanya = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
        if (!TextUtils.isEmpty(mylanya) && mylanya.equals("H9")) {
            String[] strings = B18iUtils.stringToArray(String.valueOf(newmsg));//分割出name
            String title = strings[0];
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < strings.length; i++) {
                sb.append(strings[i]);
            }
            Log.d(TAG, title + "===" + sb.toString() + "时间为：" + B18iUtils.H9TimeData());
            sendCalendar(title + "" + sb.toString(), B18iUtils.H9TimeData(), MsgCountPush.SCHEDULE_TYPE, 1);
            return;
        }
    }

    public void sendMsg(String pakage, String msg) {

        //qq
        if (pakage.equals(QQ_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "qqmsg")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "qqmsg"))) {
                    sendTo("qq", msg);
                }
            }
            // 微信
        } else if (pakage.equals(WECHAT_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "weixinmsg")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "weixinmsg"))) {
                    sendTo("wechat", msg);
                }
            }
        }
        //facebook
        else if (pakage.equals(FACEBOOK_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "facebook")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "facebook"))) {
                    sendTo("facebook", msg);
                }
            }
        }
        //Twitter
        else if (pakage.equals(TWITTER_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "Twitteraa")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "Twitteraa"))) {
                    sendTo("twitter", msg);
                }
            }
        }
        //Whats
        else if (pakage.equals(WHATS_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "Whatsapp")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "Whatsapp"))) {
                    sendTo("whats", msg);
                }
            }
        }    //Instagram
        else if (pakage.equals(INSTANRAM_PACKAGENAME)) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "Instagrambutton")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "Instagrambutton"))) {
                    sendTo("instagram", msg);
                }
            }
        }
        //viber
        else if (pakage.equals("com.viber.voip")) {
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "Viber")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "Viber"))) {
                    sendTo("viber", msg);
                }
            }
        } else if (pakage.equals(MSG_PACKAGENAME) || pakage.equals(SAMSUNG_MSG_PACKNAME) || pakage.equals(SAMSUNG_MSG_SRVERPCKNAME)) {  //短信
            if (null != SharedPreferencesUtils.readObject(MyApp.getContext(), "msg")) {
                if ("0".equals(SharedPreferencesUtils.readObject(MyApp.getContext(), "msg"))) {
                    sendTo("mms", msg);
                }
            }
            String h8OnorOff = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), "messagealert", "");
            if (!WatchUtils.isEmpty(h8OnorOff) && h8OnorOff.equals("on")) {
                EventBus.getDefault().post(new MessageEvent("smsappalert"));
            }

        }

    }


    void dddd() {
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_ANTI_LOST", GlobalVarManager.getInstance().isAntiLostSwitch());//同步
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_CALENDAR", GlobalVarManager.getInstance().isCalendarSwitch());//日历
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_SEDENTARY", GlobalVarManager.getInstance().isSedentarySwitch());//久坐提醒

        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_SKYPE", GlobalVarManager.getInstance().isSkypeSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_WHATSAPP", GlobalVarManager.getInstance().isWhatsappSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_FACEBOOK", GlobalVarManager.getInstance().isFacebookSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_TWTTER", GlobalVarManager.getInstance().isTwitterSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_INSTAGRAM", GlobalVarManager.getInstance().isInstagamSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_LINE", GlobalVarManager.getInstance().isLineSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_WECTH", GlobalVarManager.getInstance().isWechatSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_QQ", GlobalVarManager.getInstance().isQqSwitch());
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_SMS", GlobalVarManager.getInstance().isSmsSwitch());//短信
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "H9_INCOME_CALL", GlobalVarManager.getInstance().isIncomePhoneSwitch());//来电
    }

    /**
     * H9发送社交消息
     */
    private void sendMessH9(byte socal, String newmsg, byte countType) {
        try {
            String mylanya = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
            if (!TextUtils.isEmpty(mylanya) && mylanya.equals("H9")) {
                if (TextUtils.isEmpty(newmsg)) {
                    sendSocialCommands(getResources().getString(R.string.news),
                            getResources().getString(R.string.messages), B18iUtils.H9TimeData(), socal, 1, countType);
                    return;
                }
                String[] strings = B18iUtils.stringToArray(String.valueOf(newmsg));//分割出name
                String title = strings[0];
                StringBuffer sb = new StringBuffer();
                for (int i = 1; i < strings.length; i++) {
                    sb.append(strings[i]);
                }
                Log.e(TAG, title + "===" + sb.toString() + "时间为：" + B18iUtils.H9TimeData());
                sendSocialCommands(title, sb.toString(), B18iUtils.H9TimeData(), socal, 1, countType);
                return;
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }

    /**
     * H9 短信
     *
     * @param from
     * @param content
     * @param date      (格式为年月日‘T’时分秒)
     * @param countType
     * @param count
     */
    private void sendSmsCommands(String from, String content, String date, byte countType, int count) {
        SmsPush smsPushName = null, smsPushContent = null, smsPushDate = null;
        try {
            if (!TextUtils.isEmpty(from)) {
                byte[] bName = from.getBytes("utf-8");
                smsPushName = new SmsPush(commandResultCallback, SmsPush.SMS_NAME_TYPE, bName);
            }
            if (!TextUtils.isEmpty(content)) {
                byte[] bContent = content.getBytes("utf-8");
                smsPushContent = new SmsPush(commandResultCallback, SmsPush.SMS_CONTENT_TYPE, bContent);
            }
            if (!TextUtils.isEmpty(date)) {
                byte[] bDate = date.getBytes("utf-8");
                smsPushDate = new SmsPush(commandResultCallback, SmsPush.SMS_DATE_TYPE, bDate);
            }
        } catch (UnsupportedEncodingException e) {
        }
        MsgCountPush countPush = new MsgCountPush(commandResultCallback, countType, (byte) count);
        ArrayList<BaseCommand> sendList = new ArrayList<>();
//        Log.d(TAG, "smsPushName=" + smsPushName + "smsPushContent=" + smsPushContent + "smsPushDate=" + smsPushDate);
        if (smsPushName != null) {
            sendList.add(smsPushName);
        }
        if (smsPushContent != null) {
            sendList.add(smsPushContent);
        }
        if (smsPushDate != null) {
            sendList.add(smsPushDate);
        }
        sendList.add(countPush);
        AppsBluetoothManager.getInstance(MyApp.getContext()).sendCommands(sendList);
    }


    String phoneName = "";
    String people = "";

    public String callPhoneNumber(String sender) {
        people = sender;
        if (sender.length() == 11) {
            phoneName = getPeopleNameFromPerson(sender);
            //getPeople(sender.substring(0, sender.length()));//电话转联系人
        } else if (sender.length() == 13) {
            phoneName = getPeopleNameFromPerson(sender.substring(2, sender.length()));
        } else if (sender.length() == 14) {
            phoneName = getPeopleNameFromPerson(sender.substring(3, sender.length()));
        }
//            String people = getPeople(sender.substring(3, sender.length()));//电话转联系人
        if (WatchUtils.isEmpty(phoneName)) {
            phoneName = people;
        }
        return phoneName;
    }

    // 通过address手机号关联Contacts联系人的显示名字
    private String getPeopleNameFromPerson(String address) {
        if (address == null || address == "") {
            return "( no address )\n";
        }

        String strPerson = "null";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        Uri uri_Person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);  // address 手机号过滤
        Cursor cursor = MyApp.getContext().getContentResolver().query(uri_Person, projection, null, null, null);

        if (cursor.moveToFirst()) {
            int index_PeopleName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String strPeopleName = cursor.getString(index_PeopleName);
            strPerson = strPeopleName;
        }
        cursor.close();

        return strPerson;
    }


    //H8手表发送指令
    public static void sendTo(String apptags, String msg) {
        Log.e(TAG, "------msg----" + msg + "----" + apptags);
        String bleName = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
        if (!WatchUtils.isEmpty(bleName) && bleName.equals(H8_NAME_TAG)) {
            if (apptags.equals("phone")) {    //电话
                MyApp.getInstance().h8BleManagerInstance().setPhoneAlert();
            } else if (apptags.equals("mms")) {    //短信
                MyApp.getInstance().h8BleManagerInstance().setSMSAlert();
            } else {
                MyApp.getInstance().h8BleManagerInstance().setAPPAlert();
            }
        }
    }

    /**
     * 分包发送数据
     *
     * @param bs
     * @param currentPack
     * @return
     */


    private byte[] getContent(byte[] bs, int currentPack) {
        byte[] xxx = new byte[20];
        xxx[0] = Integer.valueOf(0xc2).byteValue();
        xxx[1] = Integer.valueOf(00).byteValue();
        //获取总包数 = total+1
        int total = bs.length / 14;
        if (total > 3) {
            xxx[2] = Integer.valueOf(0x0D).byteValue();
            xxx[3] = Integer.valueOf(04).byteValue();
            xxx[4] = Integer.valueOf(currentPack).byteValue();
            xxx[5] = Integer.valueOf(01).byteValue();
            System.arraycopy(bs, 14 * (currentPack - 1), xxx, 6, 14 * currentPack - 1);
        } else {
            if (currentPack * 14 > bs.length) {
                xxx[2] = Integer.valueOf(bs.length - (currentPack - 1) * 14).byteValue();
            } else {
                xxx[2] = Integer.valueOf(0x0D).byteValue();
            }
            xxx[3] = Integer.valueOf(total + 1).byteValue();
            xxx[4] = Integer.valueOf(currentPack).byteValue();
            xxx[5] = Integer.valueOf(01).byteValue();
            if (bs.length > 14 * currentPack) {
                System.arraycopy(bs, 14 * (currentPack - 1), xxx, 6, 14);
            } else {
                System.arraycopy(bs, 14 * (currentPack - 1), xxx, 6, bs.length - (14 * (currentPack - 1)));
            }
        }
        return xxx;
    }

    /**
     * 判断数组的包数，，，
     *
     * @param current
     * @param total
     * @return
     */
    private boolean isExit(int current, int total) {
        float a = (float) total / 14;
        //超过4包就退出
        if (current > 4) {
            return false;
        }
        //不足4包的时候，当已发送完就退出
        if (current >= a + 1) {
            return false;
        }

        return true;
    }


    //当系统通知被删掉后出发回调
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

//
//    /**
//     * B18I发送社交化消息
//     *
//     * @param tickerText
//     * @param head
//     */
//    public void setSocialSMS(CharSequence tickerText, String head) {
//        try {
//            String mylanya = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "mylanya");
//            if (!TextUtils.isEmpty(mylanya) && mylanya.equals("B18I")) {
//                if ((boolean) SharedPreferencesUtils.readObject(MyApp.getContext(), "SOCIAL")) {
//                    String[] strings = B18iUtils.stringToArray(String.valueOf(tickerText));//分割出name
//                    String title = strings[0];
//                    StringBuffer sb = new StringBuffer();
//                    for (int i = 1; i < strings.length; i++) {
//                        sb.append(strings[i]);
//                    }
//                    if (!TextUtils.isEmpty(tickerText)) {
//                        BluetoothSDK.sendSocial(B18iResultCallBack.getB18iResultCallBack(), head + "-" + title, sb.toString(), new Date());
//                    }
//                }
//                return;
//            }
//        } catch (Exception e) {
//            e.getMessage();
//        }
//
//    }


    /**
     * H9推送日程消息
     *
     * @param content   内容
     * @param date      时间
     * @param countType
     * @param count     目前日程titile 固定为Schedule
     */
    public void sendCalendar(String content, String date, byte countType, int count) {
        CalanderPush calanderPushTitle = null, calendarPushContent = null, calendarPushDate = null;
        try {
            calanderPushTitle = new CalanderPush(commandResultCallbackAll);
            if (!TextUtils.isEmpty(content)) {
                byte[] bContent = content.getBytes("utf-8");
                calendarPushContent = new CalanderPush(commandResultCallbackAll, CalanderPush.CONTENT_TYPE, bContent);
            }
            if (!TextUtils.isEmpty(date)) {
                byte[] bDate = date.getBytes("utf-8");
                calendarPushDate = new CalanderPush(commandResultCallbackAll, CalanderPush.DATE_TYPE, bDate);
            }
        } catch (UnsupportedEncodingException e) {
        }
        MsgCountPush countPush = new MsgCountPush(commandResultCallbackAll, countType, (byte) count);
        ArrayList<BaseCommand> sendList = new ArrayList<>();
        Log.i(TAG, "calanderPushTitle=" + calanderPushTitle + "calendarPushContent=" + calendarPushContent + "calendarPushDate=" + calendarPushDate);
        sendList.add(calanderPushTitle);
        if (calendarPushContent != null) {
            sendList.add(calendarPushContent);
        }
        if (calendarPushDate != null) {
            sendList.add(calendarPushDate);
        }
        sendList.add(countPush);
        AppsBluetoothManager.getInstance(MyApp.getContext()).sendCommands(sendList);
    }

    /**
     * h9推送社交消息
     *
     * @param from       联系人
     * @param content    内容
     * @param date       data_time  (格式为年月日‘T’时分秒)
     * @param socialType 类型  FACEBOOK TWITTER INSTAGRAM QQ WECHAT WHATSAPP LINE SKYPE
     * @param count      数量
     */
    private void sendSocialCommands(String from, String content, String date, byte socialType, int count, byte countType) {
        SocialPush pushType = null, pushName = null, pushContent = null, pushDate = null;
        try {
            // 发送社交内容
            // 1、社交平台(QQ等)
            // 2、名称
            // 3、内容
            // 4、时间
            // 5、推送条数

            pushType = new SocialPush(commandResultCallbackAll, socialType);

            if (!TextUtils.isEmpty(from)) {
                byte[] bName = from.getBytes("utf-8");
                pushName = new SocialPush(commandResultCallbackAll, SocialPush.NAME_TYPE, bName);
            }
            if (!TextUtils.isEmpty(content)) {
                byte[] bContent = content.getBytes("utf-8");
                pushContent = new SocialPush(commandResultCallbackAll, SocialPush.CONTENT_TYPE, bContent);
            }
            if (!TextUtils.isEmpty(date)) {
                byte[] bDate = date.getBytes("utf-8");
                pushDate = new SocialPush(commandResultCallbackAll, SocialPush.DATE_TYPE, bDate);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        MsgCountPush countPush = new MsgCountPush(commandResultCallbackAll, (byte) 0x08, (byte) count);
        MsgCountPush countPush = new MsgCountPush(commandResultCallbackAll, (byte) countType, (byte) count);
        ArrayList<BaseCommand> sendList = new ArrayList<>();
        sendList.add(pushType);
        if (pushName != null) {
            sendList.add(pushName);
        }
        if (pushContent != null) {
            sendList.add(pushContent);
        }
        if (pushDate != null) {
            sendList.add(pushDate);
        }
        sendList.add(countPush);
        AppsBluetoothManager.getInstance(MyApp.getContext()).sendCommands(sendList);
    }

    private void sendMsgW30S(String h9Msg, int type) {
        try {
            Log.e(TAG, "----w30s---" + newmsg + "---w30sMsg-=" + h9Msg + "---=" + type);
            boolean w30sswitch_skype = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Skype", false);
            boolean w30sswitch_whatsApp = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_WhatsApp", false);
            boolean w30sswitch_facebook = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Facebook", false);
            boolean w30sswitch_linkendIn = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_LinkendIn", false);
            boolean w30sswitch_twitter = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Twitter", false);
            boolean w30sswitch_viber = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Viber", false);
            boolean w30sswitch_line = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_LINE", false);
            boolean w30sswitch_weChat = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_WeChat", false);
            boolean w30sswitch_qq = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_QQ", false);
            boolean w30sswitch_msg = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Msg", false);
            boolean w30sswitch_Phone = (boolean) SharedPreferencesUtils.getParam(AlertService.this, "w30sswitch_Phone", false);

            switch (type) {
                case W30SBLEManage.NotifaceMsgQq:
                    if (w30sswitch_qq) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgWx:
                    if (w30sswitch_weChat) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgFacebook:
                    if (w30sswitch_facebook) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgTwitter:
                    if (w30sswitch_twitter) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgWhatsapp:
                    if (w30sswitch_whatsApp) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgViber:
                    if (w30sswitch_viber) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgSkype:
                    if (w30sswitch_skype) sendW30SApplicationMsg(h9Msg, type);
                    break;
                case W30SBLEManage.NotifaceMsgMsg:  //短信
                    if (w30sswitch_msg) sendW30SApplicationMsg(h9Msg, type);
                    break;
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public void sendW30SApplicationMsg(String h9Msg, int type) {
        String w30SBleName = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
        if ((w30SBleName != null && !TextUtils.isEmpty(w30SBleName)) || w30SBleName.equals("W30")) {
            MyApp.getInstance().getmW30SBLEManage().notifacePhone(h9Msg, type);
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


    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };


    BaseCommand.CommandResultCallback commandResultCallbackAll = new BaseCommand.CommandResultCallback() {
        @Override
        public void onSuccess(BaseCommand baseCommand) {

        }

        @Override
        public void onFail(BaseCommand baseCommand) {

        }
    };
}
