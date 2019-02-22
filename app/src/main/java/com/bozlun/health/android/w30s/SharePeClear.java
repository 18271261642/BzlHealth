package com.bozlun.health.android.w30s;

import android.content.Context;
import com.bozlun.health.android.bi8i.b18iutils.B18iUtils;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.w30s.bean.W30SAlarmClockBean;
import com.suchengkeji.android.w30sblelibrary.bean.W30S_AlarmInfo;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * @aboutContent:
 * @author： An
 * @crateTime: 2018/3/14 16:32
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class SharePeClear {

    public static void clearDatas(Context context) {
        LitePal.deleteAll(W30SAlarmClockBean.class);//清楚闹钟数据库表
        //上传数据时间间隔
        SharedPreferencesUtils.setParam(context, "upSportTime", "2017-11-02 15:00:00");
        SharedPreferencesUtils.setParam(context, "upSleepTime", "2017-11-02 15:00:00");
        SharedPreferencesUtils.setParam(context, "upHeartTimetwo", "2017-11-02 15:00:00");
        SharedPreferencesUtils.setParam(context, "upHeartTimeone", B18iUtils.getSystemDataStart());
        //目标步数
        SharedPreferencesUtils.setParam(context, "w30stag", "10000");
        //目标睡眠
        SharedPreferencesUtils.setParam(context, "w30ssleep", "8");
        //公英制
        SharedPreferencesUtils.setParam(context, "w30sunit", true);
        //时间格式
        SharedPreferencesUtils.setParam(context, "w30stimeformat", true);
        //久坐提醒
        SharedPreferencesUtils.setParam(context, "w30sSedentaryRemind", false);
        SharedPreferencesUtils.setParam(context, "starTime_sedentary", "00:00");
        SharedPreferencesUtils.setParam(context, "endTime_sedentary", "00:00");
        SharedPreferencesUtils.setParam(context, "IntervalTime_sedentary", "1H");
        //吃药
        SharedPreferencesUtils.setParam(context, "w30sMedicineRemind", false);
        SharedPreferencesUtils.setParam(context, "starTime_medicine", "00:00");
        SharedPreferencesUtils.setParam(context, "endTime_medicine", "00:00");
        SharedPreferencesUtils.setParam(context, "IntervalTime_medicine", "4H");
        //喝水
        SharedPreferencesUtils.setParam(context, "w30sDringRemind", false);
        SharedPreferencesUtils.setParam(context, "starTime_dring", "00:00");
        SharedPreferencesUtils.setParam(context, "endTime_dring", "00:00");
        SharedPreferencesUtils.setParam(context, "IntervalTime_dring", "1H");
        //会议
        SharedPreferencesUtils.setParam(context, "w30sMettingRemind", false);

        //应用通知默认关
        SharedPreferencesUtils.setParam(context, "w30sswitch_Skype", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_WhatsApp", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_Facebook", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_LinkendIn", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_Twitter", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_Viber", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_LINE", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_WeChat", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_QQ", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_Msg", false);
        SharedPreferencesUtils.setParam(context, "w30sswitch_Phone", true);
        //免扰
        SharedPreferencesUtils.setParam(context, "w30sNodisturb", false);
        //抬手亮屏
        SharedPreferencesUtils.setParam(context, "w30sBrightScreen", true);
        //运动心率
        SharedPreferencesUtils.setParam(context, "w30sHeartRate", true);
    }

    public static void sendCmdDatas(Context context) {
        //时间格式
        boolean w30stimeformat = (boolean) SharedPreferencesUtils.getParam(context, "w30stimeformat", true);
        //单位
        boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(context, "w30sunit", true);
        //抬手亮屏
        boolean w30sBrightScreen = (boolean) SharedPreferencesUtils.getParam(context, "w30sBrightScreen", true);
        //免扰
        boolean w30sNodisturb = (boolean) SharedPreferencesUtils.getParam(context, "w30sNodisturb", false);
        //运动心率
        boolean w30sHeartRate = (boolean) SharedPreferencesUtils.getParam(context, "w30sHeartRate", true);
        int a = 1;
        int b = 0;
        int c = 1;
        int d = 1;
        int e = 1;
        if (w30sBrightScreen) {
            a = 1;
        } else {
            a = 0;
        }
        if (w30sNodisturb) {
            b = 1;
        } else {
            b = 0;
        }
        if (w30sHeartRate) {
            c = 1;
        } else {
            c = 0;
        }
        if (w30stimeformat) {
            d = 1;
        } else {
            d = 0;
        }
        if (w30sunit) {
            e = 1;
        } else {
            e = 0;
        }

        /**
         * 设置默认开关指令
         *
         * @param time      = 时钟设置 1=开，0=关
         * @param unit      = 单位设置 1=开，0=关
         * @param bright    = 抬腕亮屏 1=开，0=关
         * @param miandarao = 免打扰开关 1=开，0=关
         * @param woheart   = 运动心率开关 1=开，0=关
         * @return
         */
        MyApp.getInstance().getmW30SBLEManage().setInitSet(d, e, a, b, c);

        /**
         * 设置用户闹钟信息
         */
        List<W30SAlarmClockBean> mAlarmClock = LitePal.findAll(W30SAlarmClockBean.class);
        if (mAlarmClock != null) {
            List<W30S_AlarmInfo> w30S_alarmInfos = new ArrayList<W30S_AlarmInfo>();
            for (int i = 0; i < mAlarmClock.size(); i++) {
                W30S_AlarmInfo w30S_alarmInfo = mAlarmClock.get(i).w30AlarmInfoChange();
                w30S_alarmInfos.add(w30S_alarmInfo);
            }
            MyApp.getInstance().getmW30SBLEManage().setAlarm(w30S_alarmInfos);
        }
    }
}
