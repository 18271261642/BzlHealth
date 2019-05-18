package com.bozlun.health.android.b30.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b30.bean.B30HalfHourDB;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.h9.h9monitor.UpDatasBase;
import com.bozlun.health.android.h9.utils.H9TimeUtil;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.CommonSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.OkHttpTool;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.util.VerifyUtil;
import com.bozlun.health.android.w30s.bean.UpHeartBean;
import com.bozlun.health.android.w30s.utils.W30BasicUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.suchengkeji.android.w30sblelibrary.bean.servicebean.W30S_SleepDataItem;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.ILanguageDataListener;
import com.veepoo.protocol.model.datas.HalfHourBpData;
import com.veepoo.protocol.model.datas.HalfHourRateData;
import com.veepoo.protocol.model.datas.HalfHourSportData;
import com.veepoo.protocol.model.datas.LanguageData;
import com.veepoo.protocol.model.datas.SleepData;
import com.veepoo.protocol.model.datas.TimeData;
import com.veepoo.protocol.model.enums.ELanguage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传手环本地数据用的IntentService(上传完成自动销毁的服务)
 *
 * @author XuBo 2018-09-20
 */
public class DateUploadService extends IntentService {
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String TAG = "DateUploadService";
    /**
     * 上传是否完成的状态: 运动数据
     */
    private final int STATE_SPORT = 1;
    /**
     * 上传是否完成的状态: 睡眠数据
     */
    private final int STATE_SLEEP = 2;
    /**
     * 上传是否完成的状态: 心率数据
     */
    private final int STATE_RATE = 3;
    /**
     * 上传是否完成的状态: 血压数据
     */
    private final int STATE_BP = 4;
    /**
     * 当天日期
     */
    private final String CURR_DATE = WatchUtils.getCurrentDate();
    /**
     * 需要上传的数据源: 运动数据
     */
    private List<B30HalfHourDB> sportData;
    /**
     * 需要上传的数据源: 睡眠数据
     */
    private List<B30HalfHourDB> sleepData;
    /**
     * 需要上传的数据源: 心率数据
     */
    private List<B30HalfHourDB> rateData;
    /**
     * 需要上传的数据源: 血压数据
     */
    private List<B30HalfHourDB> bpData;
    /**
     * 处理Json工具类
     */
    private Gson gson;
    /**
     * 手环MAC
     */
    private String deviceCode;
    /**
     * 用户ID
     */
    private String userId;

    public DateUploadService() {
        super("DateUploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MyApp.getInstance().setUploadDate(true);// 正在上传数据,写到全局,保证同时只有一个本服务在运行
        gson = new Gson();
        deviceCode = (String) SharedPreferencesUtils.readObject(this, Commont.BLEMAC);
        userId = (String) SharedPreferencesUtils.readObject(this, "userId");
        if (TextUtils.isEmpty(deviceCode) || TextUtils.isEmpty(userId)) return;
        findNotUploadData();// 1.找出要上传的所有数据
    }

    /**
     * 查找本地数据所有没上传的数据
     */
    private void findNotUploadData() {
        sportData = B30HalfHourDao.getInstance().findNotUploadData(deviceCode, B30HalfHourDao.TYPE_SPORT);
        sleepData = B30HalfHourDao.getInstance().findNotUploadData(deviceCode, B30HalfHourDao.TYPE_SLEEP);
        rateData = B30HalfHourDao.getInstance().findNotUploadData(deviceCode, B30HalfHourDao.TYPE_RATE);
        bpData = B30HalfHourDao.getInstance().findNotUploadData(deviceCode, B30HalfHourDao.TYPE_BP);
        if (sportData != null) Log.d(TAG, "未上传数据条数 运动: " + sportData.size());
        if (sleepData != null) Log.d(TAG, "未上传数据条数 睡眠: " + sleepData.size());
        if (rateData != null) Log.d(TAG, "未上传数据条数 心率: " + rateData.size());
        if (bpData != null) Log.d(TAG, "未上传数据条数 血压: " + bpData.size());
        if ((sportData != null && !sportData.isEmpty())
                || (sleepData != null && !sleepData.isEmpty())
                || (rateData != null && !rateData.isEmpty())
                || (bpData != null && !bpData.isEmpty())) {
            //数据库中存在数据------开始上传
            Log.d(TAG, "数据库中存在数据------开始上传");
            uploadSportData(0);// 2.按一个个类型上传: 运动->睡眠->心率->血压
        }

    }

    /**
     * 上传运动数据
     */
    private void uploadSportData(int position) {
        Log.e(TAG,"---------运动数据position="+position);
        if (sportData == null || sportData.isEmpty() || position >= sportData.size()) {
           // Log.d(TAG, "步数上传完成，开始上传睡眠");
            uploadSleepData(0);// 运动数据上传完了,换着上传睡眠数据
            return;
        }
        B30HalfHourDB dbData = sportData.get(position);
        String date = dbData.getDate();
        String originData = dbData.getOriginData();
        //LogTestUtil.e(TAG, "---------步数数据" + originData);

        setStep(originData,position);


        for (int i = 0; i < sportData.size(); i++) {
            B30HalfHourDB dbData1 = sportData.get(position);
            String date1 = dbData1.getDate();
            String originData1 = dbData1.getOriginData();
            SportVo sportVo1 = totalSportByDay(date1, originData1);
            //submitSportData2(sportVo, position);
            submitSportData2(sportVo1, i);

           // LogTestUtil.e(TAG, "---------一天的原始数据" + sportVo1.toString());
        }

    }


    /**
     * 请求提交运动步数数据到后台
     *
     * @param sportVo  整理好要提交的步数数据
     * @param position 记录当前传的是哪个下标,以便上传完后更新数据库
     */
    private void submitSportData2(SportVo sportVo, int position) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("deviceCode", deviceCode);
        params.put("stepNumber", sportVo.step);
        params.put("distance", sportVo.dis);
        params.put("calories", sportVo.kcal);
        params.put("timeLen", sportVo.timeLen);
        params.put("date", sportVo.date);
        params.put("status", sportVo.status);
        //Log.d(TAG, "------AAA-步数上传参数" + params.toString());
        requestStep2(URLs.HTTPs + URLs.upSportData, params);
    }


    /**
     * 步数上传返回
     *
     * @param path
     * @param params
     */
    private void requestStep2(String path, Map<String, String> params) {
        OkHttpTool.getInstance().doRequest(path, params, this, new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                if (WatchUtils.isEmpty(result))
                    return;
                Log.d(TAG, "------AAA-步数上传返回" + result);
            }
        }, false);
    }

    /**
     * 转换数据格式上传数据-----------好友功能详细数据
     *
     * @param originData
     * @param position
     */
    void setStep(String originData, final int position) {
        List<HalfHourSportData> dataList = gson.fromJson(originData, new TypeToken<List<HalfHourSportData>>() {
        }.getType());
        if (dataList == null || dataList.isEmpty()) return;
        String date = dataList.get(0).getDate();
        //Log.e(TAG, "-----------步数长度" + dataList.size());
        List<UpHeartBean> upHeartBeanList = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        if (!dataList.isEmpty()) {
            int s = 0;
            int s1 = 0;
            int all = 0;
            String times = "00:00";
            for (int i = 0; i < 48; i++) {
                if (i < dataList.size()) {
                    if (i % 2 == 1) {
                        s = dataList.get(i).getStepValue();//非正点的步数
                    } else {
                        s1 = dataList.get(i).getStepValue();//正点的步数
                    }
                }
                all = s + s1;
                times = toTimeStr((i / 2) * 60 * 60 * 1000);
                //根据条件，将48个点转换为24个点，并且把半点的数据加到正点上
                if (i % 2 == 0) {
                    s = 0;
                    s1 = 0;
//                    if ((i / 2) < 9) {
//                        times = "0" + (i / 2) + ":00";
//                    } else {
//                        times = i + ":00";
//                    }
                    //Log.d(TAG, "步数时间--" + date + " " + times);
                    list.add(all);
//                    UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
//                            "00", all + "",
//                            date + " " + times, "00", "0", "00", "00");
//                    upHeartBeanList.add(upHeartBean);

                    UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
                            0 , 0,
                            0, 0,
                            0, date + " " + times,
                            all);
                    upHeartBeanList.add(upHeartBean);
                }

            }
            JSONArray jsonArray1 = ProLogListJson(upHeartBeanList);
//            UpDatasBase.upAllDataSteps(jsonArray1);
           // Log.d(TAG, "步数处理数据长度" + list.size() + "===" + list.toString());
            try {
                JSONObject mapB = new JSONObject();
                mapB.put("data", jsonArray1);
                String mapjson = mapB.toString();
               // LogTestUtil.e("-------------步数上传", jsonArray1.toString());
                SubscriberOnNextListener sb = new SubscriberOnNextListener<String>() {
                    @Override
                    public void onNext(String result) {
                       // LogTestUtil.e("-------------步数上传返回", result);
                        handlerResult(result, STATE_SPORT, position);
                    }
                };
                CommonSubscriber commonSubscriber = new CommonSubscriber(sb, MyApp.getInstance());
                OkHttpObservable.getInstance().getData(commonSubscriber, URLs.HTTPs + URLs.upHeart, mapjson);
            } catch (Exception E) {
                E.printStackTrace();
            }

//            Log.d(TAG, "步数处理数据长度a" + upHeartBeanList.size() + "===" + upHeartBeanList.toString());
        }
    }


    /**
     * 将 list 转换 为 JSONArray
     *
     * @param list
     * @return
     */
    public JSONArray ProLogListJson(List<UpHeartBean> list) {
        JSONArray json = new JSONArray();
        for (UpHeartBean pLog : list) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("userId", pLog.getUserId());
                jo.put("deviceCode", pLog.getDeviceCode());
                jo.put("diastolic", pLog.getDiastolic());
                jo.put("systolic", pLog.getSystolic());
                jo.put("stepNumber", pLog.getStepNumber());
                jo.put("date", pLog.getDate());
                jo.put("heartRate", pLog.getHeartRate());
                jo.put("status", pLog.getStatus());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json.put(jo);
        }
        return json;
    }


    /**
     * 汇总一天的原始运动数据
     *
     * @param originData 当天的运动数据源
     * @return 汇总后的数据
     */
    private SportVo totalSportByDay(String date, String originData) {
        //目标步数
        int goalStep = (int) SharedPreferencesUtils.getParam(MyApp.getInstance().getApplicationContext(), "b30Goal", 0);
        List<HalfHourSportData> dataList = gson.fromJson(originData, new TypeToken<List<HalfHourSportData>>() {
        }.getType());
        int stepValue = 0;
        double calValue = 0;
        double disValue = 0;
        if (dataList != null) {
            for (HalfHourSportData item : dataList) {
                stepValue += item.getStepValue();
                calValue += item.getCalValue();
                disValue += item.getDisValue();
            }
        }
        SportVo sportVo = new SportVo();
        sportVo.date = date;
        sportVo.step = "" + stepValue;
        sportVo.kcal = "" + calValue;
        sportVo.dis = "" + disValue;
        //判断是否达标 1 达标；0 不达标
        int status;
        if (stepValue - goalStep >= 0) {
            status = 1;
        } else {
            status = 0;
        }

        sportVo.status = status + "";
        sportVo.timeLen = "0";
        return sportVo;
    }

    /**
     * 请求提交运动步数数据到后台
     *
     * @param sportVo  整理好要提交的步数数据
     * @param position 记录当前传的是哪个下标,以便上传完后更新数据库
     */
    private void submitSportData(SportVo sportVo, int position) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("deviceCode", deviceCode);
        params.put("stepNumber", sportVo.step);
        params.put("distance", sportVo.dis);
        params.put("calories", sportVo.kcal);
        params.put("timeLen", sportVo.timeLen);
        params.put("date", sportVo.date);
        params.put("status", sportVo.status);
        //Log.d(TAG, "------AAA-步数上传参数" + params.toString());
        requestStep(URLs.HTTPs + URLs.upSportData, params, STATE_SPORT, position);
    }

    /**
     * 上传睡眠数据
     */
    private void uploadSleepData(int position) {
        if (sleepData == null || sleepData.isEmpty() || position >= sleepData.size()) {
            //Log.d(TAG, "睡眠数据上传完成，开始上传心率数据");
            uploadRateData(0);// 睡眠数据上传完了,换着上传心率数据
            return;
        }
        B30HalfHourDB dbData = sleepData.get(position);
        if (dbData != null) {
            String originData = dbData.getOriginData();
            if (TextUtils.isEmpty(originData)) return;
            Log.e(TAG, "数据库睡眠" + originData);
            SleepData sleepData = gson.fromJson(originData, SleepData.class);
//            if(sleepData != null){
//                Log.e(TAG,"---------sleepData-="+sleepData.toString());
//                setSleep(sleepData);
//                submitSleepData(sleepData, position);
//            }

        }
    }


    void setSleep(SleepData sleepData) {

        String sleepDown = sleepData.getSleepDown().getDateAndClockForSleep();
//        String sleepUp = sleepData.getSleepUp().getDateAndClockForSleep();
        List<Integer> listValue = new ArrayList<>();

        String sleepDatas = sleepData.getDate();

//        Log.d(TAG, "睡眠转数据" + sleepDown + "==" + sleepUp+"======"+date);//2018-12-01 07:40==2018-12-01 09:15
        String sleepLine = sleepData.getSleepLine();
        for (int i = 0; i < sleepLine.length(); i++) {
            if (i <= sleepLine.length() - 1) {
                int subStr = Integer.valueOf(sleepLine.substring(i, i + 1));
                listValue.add(subStr);
            }
        }
        String sleep_type = "1";//1清醒  2浅睡   3深睡
        String startTime = sleepDown.substring(11, 16);
        List<W30S_SleepDataItem> w30S_sleepDataList = new ArrayList<>();
        //Log.d(TAG, "睡眠串===" + sleepLine + "==" + listValue.size() + "==" + sleepLine.length());
        long longStart = W30BasicUtils.stringToLong(startTime, "HH:mm");
        for (int i = 0; i < listValue.size(); i++) {
            int integer = listValue.get(i);
            switch (integer) {
                case 0://浅睡
                    sleep_type = "2";
                    break;
                case 1://深睡
                    sleep_type = "3";
                    break;
                case 2://清醒
                    sleep_type = "4";
                    break;
            }
            String mytimes = W30BasicUtils.longToString(longStart, "HH:mm");
           // Log.d(TAG, "睡眠==预测===" + integer + "=" + mytimes);
            W30S_SleepDataItem w30S_sleepData = new W30S_SleepDataItem();
            w30S_sleepData.setSleep_type(sleep_type);
            w30S_sleepData.setStartTime(mytimes);
            w30S_sleepDataList.add(w30S_sleepData);

            longStart = longStart + (5 * 60 * 1000);//转为几时几分
        }


        JSONArray jsonArray = ProLogList2Json(w30S_sleepDataList);
        //Log.d(TAG, "原始时间==预测===" + sleepDatas + "=\n=" + jsonArray);
        String dateStr = W30BasicUtils.getDateStr2(sleepDatas, 1);
        //Log.d(TAG, "正式时间==预测===" + dateStr + "=\n=" + jsonArray);
        UpDatasBase.upDataTodaSleep(dateStr, jsonArray);
    }

    /**
     * 将 list 转换 为 JSONArray
     *
     * @param list
     * @return
     */
    public JSONArray ProLogList2Json(List<W30S_SleepDataItem> list) {
        JSONArray json = new JSONArray();
        for (W30S_SleepDataItem pLog : list) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("sleep_type", pLog.getSleep_type());
                jo.put("startTime", pLog.getStartTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json.put(jo);
        }
        return json;
    }

    /**
     * 请求提交睡眠数据到后台
     *
     * @param sleepData 手环的睡眠数据
     */
    private void submitSleepData(SleepData sleepData, final int position) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("deviceCode", deviceCode);
        String dateAndClockForSleep = sleepData.getSleepDown().getDateAndClockForSleep();
        String dateAndClockForSleep1 = sleepData.getSleepUp().getDateAndClockForSleep();
//        Log.d(TAG, "------AAA-----睡眠数据" + "uploadSleepData==4=="
//                + dateAndClockForSleep + "==" + dateAndClockForSleep);
        String s = dateToStamp(dateAndClockForSleep);
        String s1 = dateToStamp(dateAndClockForSleep1);
      //  Log.d(TAG, "------AAA--睡眠数据--时间戳" + s + "===" + s1);

        try {
            //时间戳转化为Sting或Date
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Long time = new Long(Long.valueOf(s));
            String d = format.format(time);
            Date date = format.parse(d);

            Long time1 = new Long(Long.valueOf(s1));
            String d1 = format.format(time1);
            Date date1 = format.parse(d1);
            //向前1天
            Date dateBeforessddd = H9TimeUtil.getDateBefore(date, 0);
            String nextDayddd = H9TimeUtil.getValidDateStr3(dateBeforessddd);
            Date dateBeforessaaa = H9TimeUtil.getDateBefore(date1, 0);
            String nextDayaaa = H9TimeUtil.getValidDateStr3(dateBeforessaaa);
           // Log.d(TAG, "times--startTime" + nextDayddd + "===endTime" + nextDayaaa);
            params.put("startTime", nextDayddd);
            params.put("endTime", nextDayaaa);
            int deepSleepTime = sleepData.getDeepSleepTime();
            int lowSleepTime = sleepData.getLowSleepTime();
            int allSleepTime = sleepData.getAllSleepTime();
//            params.put("startTime",  sleepData.getSleepDown().getDateAndClockForSleep());
//            params.put("endTime",  sleepData.getSleepUp().getDateAndClockForSleep());
            params.put("count", "" + sleepData.getWakeCount());
            params.put("deepLen", "" + deepSleepTime);
            params.put("shallowLen", "" + lowSleepTime);
            params.put("sleepQuality", "" + sleepData.getSleepQulity());
            params.put("sleepLen", allSleepTime + "");
            params.put("sleepCurveP", "" + sleepData.getSleepLine());
            params.put("sleepCurveS", "8");

            //MyLogUtil.d(TAG, "------AAA--睡眠数据---上传参数---" + params.toString());
            request(URLs.HTTPs + URLs.upSleep, params, STATE_SLEEP, position);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) {
        String res = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = simpleDateFormat.parse(s);
            long ts = date.getTime();
            res = String.valueOf(ts);
            return res;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 上传心率数据
     */
    private void uploadRateData(int position) {
        if (rateData == null || rateData.isEmpty() || position >= rateData.size()) {
           // Log.d(TAG, "心率数据上传完成，开始上传血压数据");
            uploadBpData(0);// 心率数据上传完了,换着上传血压数据
            return;
        }
        B30HalfHourDB b30HalfHourDB = rateData.get(position);
        if (b30HalfHourDB != null) {
            String originData = b30HalfHourDB.getOriginData();
            if (TextUtils.isEmpty(originData)) return;
            List<HalfHourRateData> dataList = gson.fromJson(originData, new TypeToken<List<HalfHourRateData>>() {
            }.getType());
            setHeart(dataList, STATE_RATE, position);//心率详细上传
//            LogTestUtil.e(TAG, "心率数据原始" + dataList.size() + "\n" + dataList.toString());
//            HealthParamVo paramVo = new HealthParamVo();
//            paramVo.data = submitRateData(userId, deviceCode, dataList);
//            String rateData = gson.toJson(paramVo);
//            LogTestUtil.e(TAG, "心率数据" + rateData);
//            request(rateData, STATE_RATE, position);
        }
    }

    private String[] StringDate = {
            "00:00", "00:30",
            "01:00", "01:30",
            "02:00", "02:30",
            "03:00", "03:30",
            "04:00", "04:30",
            "05:00", "05:30",
            "06:00", "06:30",
            "07:00", "07:30",
            "08:00", "08:30",
            "09:00", "09:30",
            "10:00", "10:30",
            "11:00", "11:30",
            "12:00", "12:30",
            "13:00", "13:30",
            "14:00", "14:30",
            "15:00", "15:30",
            "16:00", "16:30",
            "17:00", "17:30",
            "18:00", "18:30",
            "19:00", "19:30",
            "20:00", "20:30",
            "21:00", "21:30",
            "22:00", "22:30",
            "23:00", "23:30"};//X轴的标注

    private String to2Str(int i) {
        if (i > 9) {
            return i + "";
        } else {
            return "0" + i;
        }

    }

    private String toTimeStr(int secTotal) {

        String result = null;
        secTotal = secTotal / 1000;
        int hour = secTotal / 3600;
        int min = (secTotal % 3600) / 60;
        int sec = (secTotal % 3600) % 60;
//        result = to2Str(hour)+":"+to2Str(min)+":"+to2Str(sec);
        result = to2Str(hour) + ":" + to2Str(min);

        return result;
    }

    /**
     * 将毫秒转时分秒
     *
     * @param time
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds / 60) % 60;
        int seconds = totalSeconds % 60;
//        return hours > 0 ? String.format("%02dh%02dm%02ds", hours, minutes, seconds) : String.format("%02dm%02ds", minutes, seconds);
        return String.format("%02d:%02d", hours, minutes);
    }


    /**
     * 上传详细心率数------- 好友数据上传
     *
     * @param dataList
     * @param STATE_RATE
     * @param position
     */
    void setHeart(List<HalfHourRateData> dataList, int STATE_RATE, int position) {
        if (dataList == null && dataList.isEmpty()) return;
        String date = dataList.get(0).getDate();
       // Log.d(TAG, "心率数据长度" + dataList.size());
        List<UpHeartBean> upHeartBeanList = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        String times = "00:00";
        for (int i = 0; i < 48; i++) {
            times = StringDate[i].trim();
          //  Log.d(TAG, "添加占位数据 心率时间：" + date + " " + times);
//            UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
//                    "00", "00",
////                    date + " " + times, "0", "0");
//                    "2018-12-10 " + times, "0", "0", "00", "00");

            UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
                    0, 0,
                    0, 0,
                    0, date+" " + times,
                    0);
            upHeartBeanList.add(upHeartBean);
            list.add(0);
        }
       // Log.d(TAG, "位数据  长度" + list.size() + " " + upHeartBeanList.size());
        for (int j = 0; j < StringDate.length; j++) {
            int heartValue = 0;
            for (int i = 0; i < dataList.size(); i++) {
                TimeData time = dataList.get(i).getTime();
                String stringHour = "00";
                String stringMinute = "00";
                int hour = time.getHour();
                int minute = time.getMinute();
                if (hour >= 10) {
                    stringHour = "" + hour;
                } else {
                    stringHour = "0" + hour;
                }
                if (minute >= 10) {
                    stringMinute = "" + minute;
                } else {
                    stringMinute = "0" + minute;
                }
//                String dates = ((int)time.getHour() > 9 ? (int)time.getHour()+"" : "0" + (int)time.getHour())
//                        + ":" + ((int)time.getMinute() > 9 ? (int)time.getMinute()+"" : "0" + (int)time.getMinute());
                String dates = stringHour + ":" + stringMinute;
                if (StringDate[j].trim().equals(dates)) {
                   // Log.d(TAG, "心率时间对比成功    新时间:" + dates + "  存在时间:" + StringDate[j] + "  心率值：" + heartValue);
//                    UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
//                            "00", "00",
//                            date + " " + dates, heartValue + "", "0");
                    heartValue = dataList.get(i).getRateValue();
                }
            }

            UpHeartBean upHeartBean = upHeartBeanList.get(j);
            upHeartBean.setHeartRate(heartValue );
            upHeartBean.setDate(date + " " + StringDate[j]);
            upHeartBean.setStatus(0);
            upHeartBeanList.set(j, upHeartBean);
            list.set(j, heartValue);

        }
//        Log.d(TAG, "最终心率数据" + list.size() + "===" + list.toString());
//        Log.d(TAG, "最终上传心率数据" + upHeartBeanList.size() + "===" + upHeartBeanList.toString());
        JSONArray jsonArray1 = ProLogListJson(upHeartBeanList);
       // LogTestUtil.e(TAG, "上传到服务端的心率详细数据：" + jsonArray1);
//        UpDatasBase.upAllDataHearte(jsonArray1);

        JSONObject mapB = new JSONObject();
        try {
            mapB.put("data", jsonArray1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String mapjson = mapB.toString();
        request(mapjson, STATE_RATE, position);

    }


    /**
     * 获取最后上传的心率数据
     */
    private List<HealthVo> submitRateData(String id, String code, List<HalfHourRateData> data) {
        List<HealthVo> rateList = new ArrayList<>();// 组织心率提交的数据
        for (HalfHourRateData item : data) {
            HealthVo healthVo = new HealthVo();
            healthVo.userId = id;
            healthVo.deviceCode = code;
            healthVo.heartRate = "" + item.getRateValue();
            healthVo.bloodOxygen = "0";
            healthVo.systolic = "0";
            healthVo.diastolic = "0";
            healthVo.status = "1";
            healthVo.date = obtainDate(item.getTime());
            rateList.add(healthVo);
        }
        return rateList;
    }


    IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    ILanguageDataListener iLanguageDataListener = new ILanguageDataListener() {
        @Override
        public void onLanguageDataChange(LanguageData languageData) {
            if (languageData != null && languageData.getLanguage() != null) {
                Log.d(TAG, languageData.getLanguage().toString());
            }
        }
    };

    /**
     * 上传血压数据
     */
    private void uploadBpData(int position) {
        if (bpData == null || bpData.isEmpty() || position >= bpData.size()) {
            // 血压数据上传完了,到此结束
            Log.d(TAG, "全部数据上传完成,改变上传状态，没在上传数据------此处设置设备语言");
            //数据上传完咯，改变设备语言
            //判断设置语言
            boolean zh = VerifyUtil.isZh(MyApp.getInstance());
            if (zh) {
                MyApp.getInstance().getVpOperateManager().settingDeviceLanguage(iBleWriteResponse, iLanguageDataListener, ELanguage.CHINA);
            } else {
                MyApp.getInstance().getVpOperateManager().settingDeviceLanguage(iBleWriteResponse, iLanguageDataListener, ELanguage.ENGLISH);
            }
            MyApp.getInstance().setUploadDate(false);
            return;
        }
        B30HalfHourDB b30HalfHourDB = bpData.get(position);
        if (b30HalfHourDB != null) {
            String originData = b30HalfHourDB.getOriginData();
            if (TextUtils.isEmpty(originData)) return;
            List<HalfHourBpData> dataList = gson.fromJson(originData, new TypeToken<List<HalfHourBpData>>() {
            }.getType());
           // LogTestUtil.e(TAG, "血压数据原始" + dataList.size() + "\n" + dataList.toString());

            setBp(dataList, STATE_BP, position);
//            HealthParamVo paramVo = new HealthParamVo();
//            paramVo.data = submitBpData(userId, deviceCode, dataList);
//            String bpData = gson.toJson(paramVo);
//            LogTestUtil.e(TAG, "血压数据" + bpData);
//            requestBp(bpData, STATE_BP, position);
        }
    }


    /**
     * 上传详细心率数------- 好友数据上传
     *
     * @param dataList
     * @param STATE_RATE
     * @param position
     */
    void setBp(List<HalfHourBpData> dataList, int STATE_RATE, int position) {
        if (dataList == null && dataList.isEmpty()) return;
        String date = dataList.get(0).getDate();
        //Log.d(TAG, "血压数据长度" + dataList.size());
        List<UpHeartBean> upHeartBeanList = new ArrayList<>();
        List<Integer> list = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        String times = "00:00";
        for (int i = 0; i < 48; i++) {
            times = StringDate[i].trim();
            //Log.d(TAG, "添加占位数据 血压时间：" + date + " " + times);
//            UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
//                    "0", "00",
////                    date + " " + times, "0", "0");
//                    "2018-12-10 " + times, "00", "0", "0", "00");

            UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
                    0 , 0,
                    0, 0,
                    0, date + " " + times,
                    0);
            upHeartBeanList.add(upHeartBean);
            list.add(0);
            list2.add(0);
        }
        //Log.d(TAG, "血压位数据  长度" + list.size() + " " + upHeartBeanList.size());
        for (int j = 0; j < StringDate.length; j++) {
            int hightValue = 0;
            int lowValue = 0;
            for (int i = 0; i < dataList.size(); i++) {
                TimeData time = dataList.get(i).getTime();
                String stringHour = "00";
                String stringMinute = "00";
                int hour = time.getHour();
                int minute = time.getMinute();
                if (hour >= 10) {
                    stringHour = "" + hour;
                } else {
                    stringHour = "0" + hour;
                }
                if (minute >= 10) {
                    stringMinute = "" + minute;
                } else {
                    stringMinute = "0" + minute;
                }
//                String dates = ((int)time.getHour() > 9 ? (int)time.getHour()+"" : "0" + (int)time.getHour())
//                        + ":" + ((int)time.getMinute() > 9 ? (int)time.getMinute()+"" : "0" + (int)time.getMinute());
                String dates = stringHour + ":" + stringMinute;
                if (StringDate[j].trim().equals(dates)) {
//                    UpHeartBean upHeartBean = new UpHeartBean(userId, deviceCode,
//                            "00", "00",
//                            date + " " + dates, heartValue + "", "0");
                    hightValue = dataList.get(i).getHighValue();
                    lowValue = dataList.get(i).getLowValue();

                    //Log.d(TAG, "血压时间对比成功    新时间:" + dates + "  存在时间:" + StringDate[j] + "  高压：" + hightValue + "=== 低压：" + lowValue);

                }
            }

            UpHeartBean upHeartBean = upHeartBeanList.get(j);
            upHeartBean.setDate(date + " " + StringDate[j]);
            upHeartBean.setSystolic(lowValue);
            upHeartBean.setDiastolic(hightValue);
            upHeartBeanList.set(j, upHeartBean);
            list.set(j, lowValue);
            list2.set(j, hightValue);

        }
//        Log.d(TAG, "最终血压数据" + list.size() + "===" + list.toString());
//        Log.d(TAG, "最终血压数据" + list2.size() + "===" + list2.toString());
//        Log.d(TAG, "最终上传血压数据" + upHeartBeanList.size() + "===" + upHeartBeanList.toString());
        JSONArray jsonArray1 = ProLogListJson(upHeartBeanList);
//        UpDatasBase.upAllDataHearte(jsonArray1);

        JSONObject mapB = new JSONObject();
        try {
            mapB.put("data", jsonArray1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String mapjson = mapB.toString();
       // LogTestUtil.e(TAG, "上传到服务端的血压详细数据：" + mapjson);
        requestBp(mapjson, STATE_BP, position);

    }

    /**
     * 获取最后上传的血压数据
     */
    private List<HealthVo> submitBpData(String id, String code, List<HalfHourBpData> data) {
        List<HealthVo> bpList = new ArrayList<>();// 组织血压提交的数据
        for (HalfHourBpData item : data) {
            HealthVo healthVo = new HealthVo();
            healthVo.userId = id;
            healthVo.deviceCode = code;
            healthVo.heartRate = "0";
            healthVo.bloodOxygen = "0";
            healthVo.systolic = "" + item.lowValue;
            healthVo.diastolic = "" + item.highValue;
            healthVo.status = "1";
            healthVo.date = obtainDate(item.getTime());
            bpList.add(healthVo);
        }
        return bpList;
    }

    /**
     * 将手环数据的日期转换为提交到后台的格式
     */
    private String obtainDate(TimeData data) {
        String month = data.month < 10 ? "0" + data.month : "" + data.month;
        String day = data.day < 10 ? "0" + data.day : "" + data.day;
        String hour = data.hour < 10 ? "0" + data.hour : "" + data.hour;
        String minute = data.minute < 10 ? "0" + data.minute : "" + data.minute;
        return data.year + "-" + month + "-" + day + " " + hour + ":" + minute;
    }

    /**
     * 执行OkHttp请求操作,上传睡眠和步数
     *
     * @param path     地址
     * @param params   参数
     * @param type     类型
     * @param position 数据源下标,用于上传数据成功后更新本地数据库
     */
    private void request(String path, Map<String, String> params, final int type, final int
            position) {
        OkHttpTool.getInstance().doRequest(path, params, this, new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                if (WatchUtils.isEmpty(result))
                    return;
                Log.d(TAG, "------AAA-睡眠上传返回" + result);
                handlerResult(result, type, position);
            }
        }, false);
    }

    /**
     * 步数上传返回
     *
     * @param path
     * @param params
     * @param type
     * @param position
     */
    private void requestStep(String path, Map<String, String> params, final int type, final int
            position) {
        OkHttpTool.getInstance().doRequest(path, params, this, new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                if (WatchUtils.isEmpty(result))
                    return;
                Log.d(TAG, "------AAA-步数上传返回" + result);
                handlerResult(result, type, position);
            }
        }, false);
    }

    /**
     * 执行OkHttp请求操作,上传血压和心率
     *
     * @param json 参数
     * @param type 3血压,4心率
     */
    private void request(String json, final int type, final int position) {
        String path = URLs.HTTPs + URLs.upHeart;
        OkHttpTool.getInstance().doRequest(path, json, this, new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                if (WatchUtils.isEmpty(result))
                    return;
                Log.d(TAG, "------AAA-心率上传返回" + result);
                handlerResult(result, type, position);
            }
        });
    }

    private void requestBp(String json, final int type, final int position) {
        String path = URLs.HTTPs + URLs.upHeart;
        OkHttpTool.getInstance().doRequest(path, json, this, new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                if (WatchUtils.isEmpty(result))
                    return;
                Log.d(TAG, "------AAA-血压传返回" + result);
                handlerResult(result, type, position);
            }
        });
    }

    /**
     * 处理请求结果
     */
    private void handlerResult(String result, int type, int position) {
        Log.d(TAG, type + ",上传数据结果:" + result + ",position:" + position);
        //if (resultSuccess(result)) changeUpload(type, position);

        switch (type) {
            case STATE_SPORT:
                Log.d(TAG, "------AAA-步数上传第" + position + "条");
                uploadSportData(++position);// 上传下一条运动数据
                break;
            case STATE_SLEEP:
                Log.d(TAG, "------AAA-睡眠上传第" + position + "条");
                uploadSleepData(++position);// 上传下一条睡眠数据
                break;
            case STATE_RATE:
                Log.d(TAG, "------AAA-心率上传第" + position + "条");
                uploadRateData(++position);// 上传下一条心率数据
                break;
            case STATE_BP:
                Log.d(TAG, "------AAA-血压上传第" + position + "条");
                uploadBpData(++position);// 上传下一条血压数据
                break;
        }
        Log.d(TAG, "------AAA--position--=" + position + "");
    }

    /**
     * 请求结果是否是成功的
     *
     * @param result 请求结果
     * @return 是否成功
     */
    private boolean resultSuccess(String result) {
        if (result == null) return false;
        ResultVo resultVo = null;
        try {
            resultVo = gson.fromJson(result, ResultVo.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        final String RESULT_CODE = "001";// 上传数据成功结果码
        return resultVo != null && resultVo.resultCode.equals(RESULT_CODE);
    }

    /**
     * 改变本地数据库上传数据状态
     */
    private void changeUpload(int type, int position) {
        B30HalfHourDB dbData = null;
        switch (type) {
            case STATE_SPORT:
                dbData = sportData.get(position);
                break;
            case STATE_SLEEP:
                dbData = sleepData.get(position);
                break;
            case STATE_RATE:
                dbData = rateData.get(position);
                break;
            case STATE_BP:
                dbData = bpData.get(position);
                break;
        }
        if (dbData != null && !dbData.getDate().equals(CURR_DATE)) {
            dbData.setUpload(1);// 只要不是当天的数据,都把本地状态设为"已上传"
            dbData.save();//因为当天数据还有可能不停的更新和上传
        }
    }

    /**
     * 内部类,请求结果模型
     */
    private class ResultVo {
        /**
         * 结果码
         */
        String resultCode;
    }

    /**
     * 内部类,健康数据最终提交数据模型
     */
    private class HealthParamVo {
        List<HealthVo> data;
    }

    /**
     * 内部类,健康数据模型
     */
    private class HealthVo {
        /**
         * 用户ID
         */
        String userId;
        /**
         * 手环MAC地址
         */
        String deviceCode;
        /**
         * 心率
         */
        String heartRate;
        /**
         * 血氧
         */
        String bloodOxygen;
        /**
         * 低压
         */
        String systolic;
        /**
         * 高压
         */
        String diastolic;
        /**
         * 是否合格,1
         */
        String status;
        /**
         * yyyy-MM-dd HH:mm
         */
        String date;
    }

    /**
     * 内部类,运动数据模型
     */
    private class SportVo {
        /**
         * yyyy-MM-dd
         */
        String date;
        /**
         * 步数
         */
        String step;
        /**
         * 卡路里
         */
        String kcal;
        /**
         * 距离
         */
        String dis;
        /**
         * 时长,0
         */
        String timeLen;
        /**
         * 是否合格,1
         */
        String status;

        @Override
        public String toString() {
            return "SportVo{" +
                    "date='" + date + '\'' +
                    ", step='" + step + '\'' +
                    ", kcal='" + kcal + '\'' +
                    ", dis='" + dis + '\'' +
                    ", timeLen='" + timeLen + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

}
