package com.bozlun.health.android.b30.service;


import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b30.bean.B30HalfHourDB;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.commdbserver.CommCalUtils;
import com.bozlun.health.android.commdbserver.CommDBManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.LocalizeTool;
import com.bozlun.health.android.util.MyLogUtil;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.google.gson.Gson;
import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IAlarm2DataListListener;
import com.veepoo.protocol.listener.data.IAllHealthDataListener;
import com.veepoo.protocol.listener.data.IBatteryDataListener;
import com.veepoo.protocol.listener.data.ICustomSettingDataListener;
import com.veepoo.protocol.listener.data.IDeviceFuctionDataListener;
import com.veepoo.protocol.listener.data.ILanguageDataListener;
import com.veepoo.protocol.listener.data.IPersonInfoDataListener;
import com.veepoo.protocol.listener.data.IPwdDataListener;
import com.veepoo.protocol.listener.data.IScreenStyleListener;
import com.veepoo.protocol.listener.data.ISleepDataListener;
import com.veepoo.protocol.listener.data.ISocialMsgDataListener;
import com.veepoo.protocol.listener.data.ISportDataListener;
import com.veepoo.protocol.model.datas.AlarmData2;
import com.veepoo.protocol.model.datas.BatteryData;
import com.veepoo.protocol.model.datas.FunctionDeviceSupportData;
import com.veepoo.protocol.model.datas.FunctionSocailMsgData;
import com.veepoo.protocol.model.datas.HRVOriginData;
import com.veepoo.protocol.model.datas.HalfHourBpData;
import com.veepoo.protocol.model.datas.HalfHourRateData;
import com.veepoo.protocol.model.datas.HalfHourSportData;
import com.veepoo.protocol.model.datas.LanguageData;
import com.veepoo.protocol.model.datas.OriginData;
import com.veepoo.protocol.model.datas.OriginHalfHourData;
import com.veepoo.protocol.model.datas.PersonInfoData;
import com.veepoo.protocol.model.datas.PwdData;
import com.veepoo.protocol.model.datas.ScreenStyleData;
import com.veepoo.protocol.model.datas.SleepData;
import com.veepoo.protocol.model.datas.SportData;
import com.veepoo.protocol.model.enums.EFunctionStatus;
import com.veepoo.protocol.model.enums.ELanguage;
import com.veepoo.protocol.model.enums.EOprateStauts;
import com.veepoo.protocol.model.enums.EPwdStatus;
import com.veepoo.protocol.model.settings.CustomSettingData;
import com.veepoo.protocol.model.settings.ReadSleepSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/7/26.
 */

public class ConnBleHelpService {

    private static final String TAG = "ConnBleHelpService";
    private int count;

    //验证密码
    private  ConnBleHelpListener connBleHelpListener;

    //设备数据
    private ConnBleMsgDataListener connBleMsgDataListener;


    //睡眠处理map
    private Map<String,SleepData> sleepMap = new HashMap<>();


    /**
     * 转换工具
     */
    private Gson gson = new Gson();

    private static volatile ConnBleHelpService connBleHelpService;

    private ConnBleHelpService() {

    }

    public static ConnBleHelpService getConnBleHelpService() {
        if (connBleHelpService == null) {
            synchronized (ConnBleHelpService.class) {
                if (connBleHelpService == null) {
                    connBleHelpService = new ConnBleHelpService();
                }
            }
        }
        return connBleHelpService;
    }


    //    private Dialog dialog;
    //发送广播提示输入密码
    private void showLoadingDialog2(final String b30Mac) {
        Intent intent = new Intent();
        intent.setAction("com.example.bozhilun.android.siswatch.CHANGEPASS");
        intent.putExtra("b30ID", b30Mac);
        MyApp.getContext().sendBroadcast(intent);
    }



    public void doConnOperater(final String bMac) {
        String b30Pwd = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.DEVICESCODE, "0000");
        if(WatchUtils.isEmpty(b30Pwd))
            b30Pwd = "0000";
        final boolean is24Hour = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.IS24Hour, true);//是否为24小时制

        VPOperateManager.getMangerInstance(MyApp.getContext()).confirmDevicePwd(new IBleWriteResponse() {
            @Override
            public void onResponse(int i) {
                Log.e(TAG, "-----密码=" + i);
            }
        }, new IPwdDataListener() {
            @Override
            public void onPwdDataChange(PwdData pwdData) {
                Log.e(TAG, "---111--pwdData=" + pwdData.getmStatus()+"="+pwdData.toString());
                //默认密码不正确，提醒用户输入密码
                if(pwdData.getmStatus() == EPwdStatus.CHECK_FAIL){
                    showLoadingDialog2(bMac);
                }

            }
        }, new IDeviceFuctionDataListener() {    //设备所支持的功能
            @Override
            public void onFunctionSupportDataChange(FunctionDeviceSupportData functionDeviceSupportData) {
                Log.e(TAG, "--111---functionDeviceSupportData--=" + functionDeviceSupportData.toString());
//                Log.e(TAG, "-----contactMsgLength=" + functionDeviceSupportData.getContactMsgLength() + "--all=" + functionDeviceSupportData.getAllMsgLength());

                //判断是否支持血压功能 目前只针对B31
                MyApp.getInstance().setB31HasBP(functionDeviceSupportData.getBp() == EFunctionStatus.SUPPORT);
                //B31带血压功能的标识
                SharedPreferencesUtils.setParam(MyApp.getContext(),Commont.IS_B31_HAS_BP_KEY,functionDeviceSupportData.getBp() == EFunctionStatus.SUPPORT);

            }
        }, new ISocialMsgDataListener() {
            @Override
            public void onSocialMsgSupportDataChange(FunctionSocailMsgData functionSocailMsgData) {
                Log.e(TAG, "-----functionSocailMsgData-=" + functionSocailMsgData);
            }
        }, new ICustomSettingDataListener() {
            @Override
            public void OnSettingDataChange(CustomSettingData customSettingData) {
                Log.e(TAG, "----111-CustomSettingData-=" + customSettingData.toString());

            }
        }, b30Pwd.trim(), is24Hour);

        setCommDevice();


    }

    //验证设备密码
    public void doConnOperater(final String blePwd, final VerB30PwdListener verB30PwdListener) {
        // String b30Pwd = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.DEVICESCODE, "0000");
        final boolean is24Hour = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.IS24Hour, true);//是否为24小时制
        VPOperateManager.getMangerInstance(MyApp.getContext()).confirmDevicePwd(new IBleWriteResponse() {
            @Override
            public void onResponse(int i) {

            }
        }, new IPwdDataListener() {
            @Override
            public void onPwdDataChange(PwdData pwdData) {
                //showLoadingDialog2(mac);
                Log.e(TAG, "-----pwdData=" + pwdData.toString());
                //此方法调用 ，密码不正确
                if(pwdData.getmStatus() == EPwdStatus.CHECK_FAIL){
                    if(verB30PwdListener != null)
                        verB30PwdListener.verPwdFailed();
                }

                //验证密码成功
                if(pwdData.getmStatus() == EPwdStatus.CHECK_AND_TIME_SUCCESS){
                    SharedPreferencesUtils.setParam(MyApp.getContext(), Commont.DEVICESCODE, blePwd);
                    if(verB30PwdListener != null)
                        verB30PwdListener.verPwdSucc();
                }

            }
        }, new IDeviceFuctionDataListener() {
            @Override
            public void onFunctionSupportDataChange(FunctionDeviceSupportData functionDeviceSupportData) {
//                Log.e(TAG, "-----functionDeviceSupportData--=" + functionDeviceSupportData.toString());
//                Log.e(TAG, "-----contactMsgLength=" + functionDeviceSupportData.getContactMsgLength() + "--all=" + functionDeviceSupportData.getAllMsgLength());
                //B31带血压功能的标识
                SharedPreferencesUtils.setParam(MyApp.getContext(),Commont.IS_B31_HAS_BP_KEY,functionDeviceSupportData.getBp() == EFunctionStatus.SUPPORT);


            }
        }, new ISocialMsgDataListener() {
            @Override
            public void onSocialMsgSupportDataChange(FunctionSocailMsgData functionSocailMsgData) {
                //Log.e(TAG, "-----functionSocailMsgData-=" + functionSocailMsgData);
            }
        }, new ICustomSettingDataListener() {
            @Override
            public void OnSettingDataChange(CustomSettingData customSettingData) {
                Log.e(TAG, "---2222--OnSettingDataChange-=" + customSettingData.toString());


            }
        }, blePwd, is24Hour);


        setCommDevice();


    }


    //设置共同的属性
    private void setCommDevice(){

        WatchUtils.setCommSocailMsgSetting(MyApp.getContext());

        //设置开关，佩戴检测、自动心率 血氧测量打开，带血压功能的默认打开血压
        WatchUtils.setSwitchCheck();

        SharedPreferencesUtils.setParam(MyApp.getContext(), Commont.BATTERNUMBER, 0);//每次连接清空电量
        //同步用户信息，设置目标步数
        setDeviceUserData();



        //设置语言，根据系统的语言设置
        ELanguage languageData ;
        String localelLanguage = Locale.getDefault().getLanguage();
        Log.e(TAG,"----------localelLanguage="+localelLanguage);
        if(!WatchUtils.isEmpty(localelLanguage) && localelLanguage.equals("zh")){    //中文
            Locale locales = MyApp.getInstance().getApplicationContext().getResources().getConfiguration().locale;
            String localCountry = locales.getCountry();
            if(localCountry.equals("TW")){  //繁体
                languageData = ELanguage.CHINA_TRADITIONAL;
            }else{
                languageData = ELanguage.CHINA;
            }

        }else{
            languageData = ELanguage.ENGLISH;
        }
        MyApp.getInstance().getVpOperateManager().settingDeviceLanguage(bleWriteResponse, new ILanguageDataListener() {
            @Override
            public void onLanguageDataChange(LanguageData languageData) {
                if (connBleHelpListener != null) {
                    connBleHelpListener.connSuccState();
                }
            }
        }, languageData);
        //设置消息提醒的开关状态


    }






    /**
     * 同步用户信息设置设备的目标步数，
     */
    public  void setDeviceUserData(){

        //目标步数
        int sportGoal = (int) SharedPreferencesUtils.getParam(MyApp.getContext(), "b30Goal", 0);
        PersonInfoData personInfoData = WatchUtils.getUserPerson(sportGoal);
        if (personInfoData == null)
            return;
        MyApp.getInstance().getVpOperateManager().syncPersonInfo(new IBleWriteResponse() {
            @Override
            public void onResponse(int i) {
            }
        }, new IPersonInfoDataListener() {
            @Override
            public void OnPersoninfoDataChange(EOprateStauts eOprateStauts) {
                //同步用户信息成功
//                if (eOprateStauts == EOprateStauts.OPRATE_SUCCESS) {
//                    if (connBleHelpListener != null) {
//                        connBleHelpListener.connSuccState();
//                    }
//                }
            }
        }, personInfoData);


        //设置主界面默认风格为1
//        MyApp.getInstance().getVpOperateManager().settingScreenStyle(bleWriteResponse, new IScreenStyleListener() {
//            @Override
//            public void onScreenStyleDataChange(ScreenStyleData screenStyleData) {
//
//            }
//        },1 );



    }



    /**
     * 获取手环基本数据
     */
    public void getDeviceMsgData() {
        // 获取步数
        MyApp.getInstance().getVpOperateManager().readSportStep(bleWriteResponse, new ISportDataListener() {
            @Override
            public void onSportDataChange(SportData sportData) {
                Log.e(TAG,"----------总步数="+sportData.toString());
                if (connBleMsgDataListener != null) {
                    connBleMsgDataListener.getBleSportData(sportData.getStep());
                }
            }
        });
        //电量
        MyApp.getInstance().getVpOperateManager().readBattery(bleWriteResponse, new IBatteryDataListener() {
            @Override
            public void onDataChange(BatteryData batteryData) {
                if (connBleMsgDataListener != null) {
                    connBleMsgDataListener.getBleBatteryData(batteryData.getBatteryLevel());
                }
            }
        });

        //连接成功后读取一下闹钟
        MyApp.getInstance().getVpOperateManager().readAlarm2(bleWriteResponse, new IAlarm2DataListListener() {
            @Override
            public void onAlarmDataChangeListListener(AlarmData2 alarmData2) {

            }
        });

    }


    /**
     * 读取所有的原始数据(包括睡眠数据)
     *
     * @param today true_只加载今天数据 false_加载三天
     */
    public void readAllHealthData(final boolean today) {
        final long currTime = System.currentTimeMillis()/1000;
        sleepMap.clear();

//        /**
//         * dayInt - 读取位置，0表示今天，1表示昨天，2表示前天，以此类推。 读取顺序为 今天-昨天- 前天，
//         * onlyReadOneDay - true表示只读今天，false表示按顺序读取
//         */
//        ReadSleepSetting readSleepSetting = new ReadSleepSetting(today ? 3:0,false,2);
//
//        MyApp.getInstance().getVpOperateManager().readSleepDataBySetting(bleWriteResponse, new ISleepDataListener() {
//            @Override
//            public void onSleepDataChange(SleepData sleepData) {
//
//            }
//
//            @Override
//            public void onSleepProgress(float v) {
//
//            }
//
//            @Override
//            public void onSleepProgressDetail(String s, int i) {
//
//            }
//
//            @Override
//            public void onReadSleepComplete() {
//
//            }
//        }, readSleepSetting);








        //读取睡眠
        MyApp.getInstance().getVpOperateManager().readSleepData(bleWriteResponse, new ISleepDataListener() {
            @Override
            public void onSleepDataChange(SleepData sleepData) {
                Log.e(TAG, "---------睡眠原始返回数据="+sleepData.toString());
                // 睡眠数据返回,会有多条数据
                saveSleepData(sleepData);
            }

            @Override
            public void onSleepProgress(float v) {

            }

            @Override
            public void onSleepProgressDetail(String s, int i) {

            }

            @Override
            public void onReadSleepComplete() {
                // 读取睡眠数据结束
                Log.e(TAG,"----------睡眠数据读取结束------="+sleepMap.size());
                if(sleepMap != null && !sleepMap.isEmpty()){
                    for(Map.Entry<String,SleepData> mp : sleepMap.entrySet()){
                        //保存详细数据 ，保存详细数据时日期会往后+ 一天
                        B30HalfHourDB db = new B30HalfHourDB();
                        db.setAddress(MyApp.getInstance().getMacAddress());
                        db.setDate(WatchUtils.obtainAroundDate(mp.getValue().getDate(),false));
                        db.setType(B30HalfHourDao.TYPE_SLEEP);
                        db.setOriginData(gson.toJson(mp.getValue()));
                        db.setUpload(0);
                        B30HalfHourDao.getInstance().saveOriginData(db);

                        //保存汇总数据
                        String bleName = "B31";
                        if (!WatchUtils.isEmpty(MyCommandManager.DEVICENAME))
                            bleName = MyCommandManager.DEVICENAME;
                        //保存睡眠数据
                        SleepData sleepData = mp.getValue();
                        //清醒时长=总的睡眠时长-深睡时长-清醒时长
                        int soberlen = sleepData.getAllSleepTime() - sleepData.getDeepSleepTime() - sleepData.getLowSleepTime();
                        CommDBManager.getCommDBManager().saveCommSleepDbData(bleName, WatchUtils.getSherpBleMac(MyApp.getContext()), sleepData.getDate(),
                                sleepData.getDeepSleepTime(), sleepData.getLowSleepTime(), soberlen, sleepData.getAllSleepTime(),
                                sleepData.getSleepDown().getDateAndClockForSleep(), sleepData.getSleepUp().getDateAndClockForSleep(),
                                sleepData.getWakeCount());

                    }
                }

                readAllDeviceData(today);

            }


        }, today ? 2 : 4);


    }

    //读取所有的健康数据，
    private void readAllDeviceData(boolean today) {
        MyApp.getInstance().getVpOperateManager().readAllHealthData(
                new IAllHealthDataListener() {
                    @Override
                    public void onProgress(float progress) {
                        // 读取的进度
                    }

                    @Override
                    public void onSleepDataChange(SleepData sleepData) {

                    }

                    @Override
                    public void onReadSleepComplete() {
                        // 读取睡眠数据结束

                    }

                    @Override
                    public void onOringinFiveMinuteDataChange(OriginData originData) {
                        // 读取5分钟原始数据的回调,每天最多288条,暂时不接收,接收30分钟的统计好的数据就行
                    }

                    @Override
                    public void onOringinHalfHourDataChange(OriginHalfHourData originHalfHourData) {
                        // 多少天就有多少条数据
                        // 30分钟原始数据的回调，来自5分钟的原始数据，只是在内部进行了数据处理
//                        MyLogUtil.d("----------", originHalfHourData.toString());
                        saveHalfHourData(originHalfHourData);
                    }

                    @Override
                    public void onReadOriginComplete() {
                        // 读取取运动,心率,血压数据结束
                        new LocalizeTool(MyApp.getContext()).putUpdateDate(WatchUtils
                                .obtainFormatDate(0));// 更新最后更新数据的时间
                        if (connBleMsgDataListener != null) {
                            connBleMsgDataListener.onOriginData();
                        }
                    }
                }
//                , today ? 1 : 3);// 读取天数的数据
                , today ? 2 : 4);// 读取天数的数据
    }



    /**
     * 保存手环返回的睡眠数据到本地数据库
     *
     * @param sleepData 睡眠数据
     */
    private void saveSleepData(SleepData sleepData) {
        if (sleepData == null) return;
        Log.e("-------设备睡眠数据---", gson.toJson(sleepData) + "");
        if(sleepMap.get(sleepData.getDate()) == null){
            sleepMap.put(sleepData.getDate(),sleepData);
        }else {
            //sleepMap.put(sleepData.getDate(),sleepData);
            Log.e(TAG,"---------sleepMap="+sleepMap.toString());
            if (sleepMap.get(sleepData.getDate()).getDate().equals(sleepData.getDate())) {  //同一天的
                if(sleepMap.get(sleepData.getDate()).getSleepQulity()!=sleepData.getSleepQulity()){
                    //map 中已经保存的
                    SleepData tempSleepData = sleepMap.get(sleepData.getDate());
                    SleepData resultSlee = new SleepData();
                    resultSlee.setDate(sleepData.getDate());    //日期
                    resultSlee.setCali_flag(0);
                    //睡眠质量，取最大值
                    resultSlee.setSleepQulity(tempSleepData.getSleepQulity() >= sleepData.getSleepQulity() ? tempSleepData.getSleepQulity() : sleepData.getSleepQulity());
                    //睡醒次数
                    resultSlee.setWakeCount(tempSleepData.getWakeCount() + sleepData.getWakeCount()+1);
                    //深睡时间
                    resultSlee.setDeepSleepTime(tempSleepData.getDeepSleepTime() + sleepData.getDeepSleepTime());
                    //浅睡时间
                    resultSlee.setLowSleepTime(tempSleepData.getLowSleepTime() + sleepData.getLowSleepTime());
                    //入睡时间 比较时间大小
                    String time1 = tempSleepData.getSleepDown().getDateAndClockForSleepSecond();
                    String time2 = sleepData.getSleepDown().getDateAndClockForSleepSecond();
                    resultSlee.setSleepDown(WatchUtils.comPariDateDetail(time2, time1) ? sleepData.getSleepDown() : tempSleepData.getSleepDown());
                    //清醒时间
                    String sleepUpStr1 = tempSleepData.getSleepUp().getDateAndClockForSleepSecond();
                    String sleepUpStr2 = sleepData.getSleepUp().getDateAndClockForSleepSecond();
                    resultSlee.setSleepUp(WatchUtils.comPariDateDetail(sleepUpStr2, sleepUpStr1) ? tempSleepData.getSleepUp() : sleepData.getSleepUp());
                    //计算两段时间间隔，第二段的入睡时间-第一段的清醒时间
                    int sleepLen = WatchUtils.intervalTimeStr(sleepUpStr1, time2);
                    int sleepStatus = sleepLen / 5;
                    StringBuilder stringBuffer = new StringBuilder();
                    for (int i = 1; i <= sleepStatus; i++) {
                        stringBuffer.append("2");
                    }
                    //所有睡眠时间
                    resultSlee.setAllSleepTime(Integer.valueOf(tempSleepData.getAllSleepTime()) + Integer.valueOf(sleepData.getAllSleepTime())+(sleepStatus * 5));
                    resultSlee.setSleepLine(WatchUtils.comPariDateDetail(time1, time2) ?
                            (tempSleepData.getSleepLine() + stringBuffer + "" + sleepData.getSleepLine()) :
                            (sleepData.getSleepLine() + stringBuffer + "" + tempSleepData.getSleepLine()));
                    Log.e(TAG, "----------结果睡眠---=" + resultSlee.toString());
                    sleepMap.put(sleepData.getDate(), resultSlee);
                }

            }


        }
    }

    /**
     * 保存手环返回的健康数据(步数,运动,心率,血压)到本地数据库
     *
     * @param data 30分钟的数据源
     */
    private void saveHalfHourData(OriginHalfHourData data) {
        if (data == null) return;
        String mac = MyApp.getInstance().getMacAddress();
        MyLogUtil.e("------sport------" + data.getHalfHourSportDatas());
        String dateSport = saveSportData(mac, data.getHalfHourSportDatas());
        MyLogUtil.e("------sport -time" + dateSport);
        saveStepData(mac, dateSport, data.getAllStep());
        saveRateData(mac, data.getHalfHourRateDatas());
        Log.d(TAG, "------------心率="+data.getHalfHourBps().toString());
        saveBpData(mac, data.getHalfHourBps());
    }

    /**
     * 保存手环返回的运动数据到本地数据库
     *
     * @param mac       手环MAC地址
     * @param sportData 当天所有30分钟运动数据
     * @return 日期(保存步数时用, 有运动数据才会有步数)
     */
    private String saveSportData(String mac, List<HalfHourSportData> sportData) {
        if (sportData == null || sportData.isEmpty()) return null;
        String date = sportData.get(0).getDate();
        B30HalfHourDB db = new B30HalfHourDB();
        db.setAddress(mac);
        db.setDate(date);
        db.setType(B30HalfHourDao.TYPE_SPORT);
        db.setOriginData(gson.toJson(sportData));
        db.setUpload(0);
        B30HalfHourDao.getInstance().saveOriginData(db);
        return date;
    }

    /**
     * 保存手环返回的心率数据到本地数据库
     *
     * @param mac      手环MAC地址
     * @param rateData 当天所有30分钟心率数据
     */
    private void saveRateData(String mac, List<HalfHourRateData> rateData) {
        if (rateData == null || rateData.isEmpty()) return;
        B30HalfHourDB db = new B30HalfHourDB();
        db.setAddress(mac);
        db.setDate(rateData.get(0).getDate());
        db.setType(B30HalfHourDao.TYPE_RATE);
        MyLogUtil.d("-----心率数据---", gson.toJson(rateData));
        db.setOriginData(gson.toJson(rateData));
        db.setUpload(0);
        B30HalfHourDao.getInstance().saveOriginData(db);

        String bleName = "B31";
        if (!WatchUtils.isEmpty(MyCommandManager.DEVICENAME)) bleName = MyCommandManager.DEVICENAME;
        //保存心率数据
        Integer[] heartStr = CommCalUtils.calHeartData(rateData);
        CommDBManager.getCommDBManager().saveCommHeartData(bleName, WatchUtils.getSherpBleMac(MyApp.getContext()),
                rateData.get(0).date, heartStr[0], heartStr[1], heartStr[2]);

    }

    /**
     * 保存手环返回的血压数据到本地数据库
     *
     * @param mac    手环MAC地址
     * @param bpData 当天所有30分钟血压数据
     */
    private void saveBpData(String mac, List<HalfHourBpData> bpData) {
        if (bpData == null || bpData.isEmpty()) return;
        B30HalfHourDB db = new B30HalfHourDB();
        db.setAddress(mac);
        db.setDate(bpData.get(0).getDate());
        db.setType(B30HalfHourDao.TYPE_BP);
        db.setOriginData(gson.toJson(bpData));
        db.setUpload(0);
        Log.d("-------血压数据", gson.toJson(bpData));
        B30HalfHourDao.getInstance().saveOriginData(db);

        //保存血压的数据
        Integer[] bloodStr = CommCalUtils.calBloodData(bpData);
        Log.e(TAG, "-------血压平均数据=" + Arrays.toString(bloodStr));
        CommDBManager.getCommDBManager().saveCommBloodDb(WatchUtils.getSherpBleMac(MyApp.getContext()), bpData.get(0).date,
                bloodStr[0], bloodStr[1], bloodStr[2], bloodStr[3]);

    }

    /**
     * 保存手环返回的步数数据到本地数据库
     *
     * @param mac      手环MAC地址
     * @param date     日期(步数没有日期,用运动数据的日期)
     * @param stepCurr 当天全部步数
     */
    private void saveStepData(String mac, String date, int stepCurr) {
        // 当天步数数据要以首页单独获取到的步数为准,因为这里获取到的当天总步数,
        // 有时会大于首页获取的实时步数,所以当天的总步数这里不保存
        MyLogUtil.e("---保存手环返回的步数数据到本地数据库" + mac + "\n" + date + "\n" + stepCurr);
        if (date == null || date.equals(WatchUtils.obtainFormatDate(0))) return;
        // 跟本地的对比一下,以防步数倒流
        String step = B30HalfHourDao.getInstance().findOriginData(mac, date, B30HalfHourDao.TYPE_STEP);
        int stepLocal = 0;
        try {
            if (TextUtils.isEmpty(step)) step = "0";
            stepLocal = Integer.parseInt(step);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        //保存总步数
        CommDBManager.getCommDBManager().saveCommCountStepDate("B31", mac, date, stepCurr);



        if (stepCurr > stepLocal) {
            B30HalfHourDB db = new B30HalfHourDB();
            db.setAddress(mac);
            db.setDate(date);
            db.setType(B30HalfHourDao.TYPE_STEP);
            db.setOriginData("" + stepCurr);
            db.setUpload(0);
            B30HalfHourDao.getInstance().saveOriginData(db);
        }
    }

    //设置密码回调
    public interface ConnBleHelpListener {
        void connSuccState();
    }

    //步数，电量回调
    public interface ConnBleMsgDataListener {
        /**
         * 电量
         */
        void getBleBatteryData(int batteryLevel);

        /**
         * 步数数据
         */
        void getBleSportData(int step);

        /**
         * 原始数据有更新到数据库的通知
         */
        void onOriginData();
    }



    public void setConnBleMsgDataListener(ConnBleMsgDataListener connBleMsgDataListener) {
        this.connBleMsgDataListener = connBleMsgDataListener;
    }

    public void setConnBleHelpListener(ConnBleHelpListener connBleHelpListener) {
        this.connBleHelpListener = connBleHelpListener;
    }

    private IBleWriteResponse bleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {
//            Log.e(TAG, "------bleWriteResponse=" + i);
        }
    };
}
