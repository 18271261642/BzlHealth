package com.bozlun.health.android.commdbserver.detail;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b30.bean.B30HalfHourDB;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.commdbserver.CommConstant;
import com.bozlun.health.android.commdbserver.SyncDbUrls;
import com.bozlun.health.android.commdbserver.W30StepDetailBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.OkHttpTool;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.bean.W30HeartBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 上传W30和W31的详细数据
 * Created by Admin
 * Date 2019/5/16
 */
public class UploadW30DetailService extends IntentService {
    private static final String TAG = "UploadW30DetailService";

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.USER_ID_DATA);

    private Gson gson = new Gson();

    String bleMac = WatchUtils.getSherpBleMac(MyApp.getContext());

    String currDayStr = WatchUtils.getCurrentDate();


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:  //上传当天汇总的数据
                    Map<String, String> countMap = (Map<String, String>) msg.obj;
                    uploadCurrDayStepCount(countMap);
                    break;
                case 0x02:  //上传当天步数详细的数据
                    Map<String, String> stepDetailMap = (Map<String, String>) msg.obj;
                    uploadCurrDayStepDetail(stepDetailMap);
                    break;
                case 0x03:  //心率的详细数据
                    List<W30HeartBean> heartDetailList = (List<W30HeartBean>) msg.obj;
                    uploadHeartDetail(heartDetailList);
                    break;
            }


        }
    };


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadW30DetailService() {
        super("UploadW30DetailService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e(TAG, "--------W30上传数据-----" + bleMac);
        if (WatchUtils.isEmpty(bleMac))
            return;
        if (WatchUtils.isEmpty(userId))
            return;

        //上传当天的详细数据
        uploadStepDetailToday(bleMac);
        uploadHeartDetailToDay();
        uploadSleepDetailToDay();

    }

    //上传心率详细数据
    private void uploadHeartDetailToDay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<B30HalfHourDB> w30HeartList = B30HalfHourDao.getInstance().findW30HeartDetail(WatchUtils.getCurrentDate(),
                        bleMac, B30HalfHourDao.TYPE_RATE);
                if (w30HeartList == null)
                    return;
                //一天只有一条数据
                String heartStr = w30HeartList.get(0).getOriginData();
                Log.e(TAG, "--------heartStr=" + heartStr);
                Map<String, String> htMap = gson.fromJson(heartStr, Map.class);
                Log.e(TAG, "----------htMap=" + htMap.toString());

                String htStr = htMap.get(WatchUtils.getCurrentDate());

                List<W30HeartBean> heartBeanList = gson.fromJson(htStr, new TypeToken<List<W30HeartBean>>() {
                }.getType());

                Message message = handler.obtainMessage();
                message.what = 0x03;
                message.obj = heartBeanList;
                handler.sendMessage(message);

            }

        }).start();


    }

    //上传睡眠的详细数据
    private void uploadSleepDetailToDay() {
        /**
         * 保存睡眠时保存的日期是当天的日期，上传时日期应上传昨天的日期
         */
        try {
            String dayStr = WatchUtils.getCurrentDate();
            List<B30HalfHourDB> w30SleepList = B30HalfHourDao.getInstance().findW30SleepDetail(dayStr, bleMac, B30HalfHourDao.TYPE_SLEEP);
            if (w30SleepList == null)
                return;
            String upDayStr = WatchUtils.obtainAroundDate(dayStr, true);
            //解析所需数据
            String sleepStr = w30SleepList.get(0).getOriginData();
            Map<String, String> allMap = gson.fromJson(sleepStr, Map.class);
            //睡眠表示的字符串
            String sleepLennStr = allMap.get(CommConstant.W30_SLEEP_RESULT_SHOW);
            Log.e(TAG, "--------睡眠表现形式=" + sleepLennStr);
            List<Integer> sleepList = gson.fromJson(sleepLennStr, new TypeToken<List<Integer>>() {
            }.getType());
            //入睡时间
            String startSleepTime = allMap.get(CommConstant.W30_SLEEP_START_DATE);
            long longStartTime = sdf.parse(startSleepTime).getTime() / 1000;
            List<CommSleepDetailDb> commSleepDetailDbList = new ArrayList<>();
            for (int i = 0; i < sleepList.size(); i++) {
                CommSleepDetailDb commSleepDetailDb = new CommSleepDetailDb();
                commSleepDetailDb.setDay(upDayStr);
                commSleepDetailDb.setDevicecode(bleMac);
                commSleepDetailDb.setUserid(userId);
                commSleepDetailDb.setSleepType(sleepList.get(i) + "");
                //时间
                commSleepDetailDb.setStarttime(WatchUtils.getLongToDate("HH:mm", (longStartTime + (1 * i * 60)) * 1000));
                //Log.e(TAG, "---------commSleepDetailDb=" + commSleepDetailDb.toString());
                commSleepDetailDbList.add(commSleepDetailDb);

            }

            //详细睡眠的上传参数
            List<Map<String,Object>> sleepLitMap = new ArrayList<>();
            Map<String, Object> detailSleepMap = new HashMap<>();
            detailSleepMap.put("deviceCode", bleMac);
            detailSleepMap.put("rtc", upDayStr);
            detailSleepMap.put("userId", userId);
            detailSleepMap.put("heartRateList", commSleepDetailDbList);
            sleepLitMap.add(detailSleepMap);

            String jsonStr = gson.toJson(sleepLitMap);
            Log.e(TAG, "-----睡眠详细数据参数jsonStr=" + jsonStr);
            OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailSleepUrl(), jsonStr, "22",
                    new OkHttpTool.HttpResult() {
                        @Override
                        public void onResult(String result) {
                            Log.e(TAG, "--------睡眠详细数据上传=" + result);
                            //uploadBloodDetailToday(bleMac);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //上传步数数据
    private void uploadStepDetailToday(String bleMac) {
        List<B30HalfHourDB> w30List = B30HalfHourDao.getInstance().findW30SportData(WatchUtils.getCurrentDate(), bleMac, B30HalfHourDao.TYPE_SPORT);

        if (w30List == null || w30List.isEmpty()) {

            return;
        }
        Log.e(TAG, "--------w30List=" + w30List.size());
        B30HalfHourDB b30HalfHourDB = w30List.get(0);
        final Map<String, String> allMap = gson.fromJson(b30HalfHourDB.getOriginData(), Map.class);
        //详细数据
        final String stepDetailStr = allMap.get("stepDetail");

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<W30StepDetailBean> tempList = new Gson().fromJson(stepDetailStr, new TypeToken<List<W30StepDetailBean>>() {
                }.getType());
                Map<String, String> stepMap = new HashMap<>();
                stepMap.put("count_step", allMap.get("countSteps"));
                stepMap.put("count_discance", allMap.get("countDiscance"));
                stepMap.put("count_kcal", allMap.get("countKcal"));

                Message message1 = handler.obtainMessage();
                message1.what = 0x01;
                message1.obj = stepMap;
                handler.sendMessage(message1);


                //遍历集合，整合为每半个小时一条，W30和W31为每一个小时一条数据
                Map<String, String> stepMaps = WatchUtils.setHalfDateMap();
                for (int i = 0; i < tempList.size(); i++) {
                    stepMaps.put(tempList.get(i).getStepDate(), tempList.get(i).getStepValue());
                }

                Message message = handler.obtainMessage();
                message.what = 0x02;
                message.obj = stepMaps;
                handler.sendMessage(message);
            }
        }).start();

    }


    //上传当天汇总的步数数据
    private void uploadCurrDayStepCount(Map<String, String> map) {
        //目标步数
        int goalStep = (int) SharedPreferencesUtils.getParam(MyApp.getContext(), "b30Goal", 8000);
        int countStep = Integer.valueOf(map.get("count_step"));
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("deviceCode", bleMac);
        params.put("stepNumber", countStep + "");
        params.put("distance", map.get("count_discance") + "");
        params.put("calories", map.get("count_kcal") + "");
        params.put("timeLen", "11");
        params.put("date", WatchUtils.getCurrentDate());
        params.put("status", (countStep >= goalStep ? 1 : 0) + "");

        Log.e(TAG, "----------当天汇总的数据=" + gson.toJson(params));

        OkHttpTool.getInstance().doRequest(URLs.HTTPs + URLs.upSportData, gson.toJson(params), "55", new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                Log.e(TAG, "-------上传是否达标=" + result);
            }
        });
    }


    //上传详细的步数数据
    private void uploadCurrDayStepDetail(Map<String, String> stepDetailMap) {
        String dayStr = WatchUtils.getCurrentDate();
        List<CommStepDetailDb> upStepList = new ArrayList<>();
        for (Map.Entry<String, String> mp : stepDetailMap.entrySet()) {
            CommStepDetailDb commStepDetailDb = new CommStepDetailDb();
            commStepDetailDb.setUserid(userId);
            commStepDetailDb.setDevicecode(bleMac);
            commStepDetailDb.setRtc(dayStr);
            commStepDetailDb.setStepnumber(Integer.valueOf(mp.getValue()));
            commStepDetailDb.setDistance("22");
            //kcal是int类型
            commStepDetailDb.setCalories("1");
            commStepDetailDb.setStartdate(mp.getKey());
            commStepDetailDb.setEnddate("11");
            commStepDetailDb.setSpeed(1);
            commStepDetailDb.setAction(1);
            commStepDetailDb.setStatus(1);
            upStepList.add(commStepDetailDb);
        }

        //详细步数的上传参数
        List<Map<String,Object>> sleepLtMap = new ArrayList<>();
        Map<String, Object> detailStepMap = new HashMap<>();
        detailStepMap.put("deviceCode", bleMac);
        detailStepMap.put("rtc", dayStr);
        detailStepMap.put("userId", userId);
        detailStepMap.put("stepNumberList", upStepList);
        sleepLtMap.add(detailStepMap);

        String jsonStr = gson.toJson(sleepLtMap);
        Log.e(TAG, "-----当天步数详细数据jsonStr=" + jsonStr);
        OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailStepUrl(), jsonStr, "11",
                new OkHttpTool.HttpResult() {
                    @Override
                    public void onResult(String result) {
                        Log.e(TAG, "--------步数当天详细数据上传=" + result);

                        //uploadHeartDetailToDay(bleMac);   //上传当天的心率数据
                    }
                });

    }


    //上传心率
    private void uploadHeartDetail(List<W30HeartBean> heartBeanList) {
        List<CommHeartDetailDb> commHeartDetailDbsLt = new ArrayList<>();
        for (W30HeartBean w30HeartBean : heartBeanList) {
            CommHeartDetailDb commHeartDetailDb = new CommHeartDetailDb();
            commHeartDetailDb.setUserid(userId);
            commHeartDetailDb.setDevicecode(bleMac);
            commHeartDetailDb.setHeartrate(w30HeartBean.getHeartValues());
            commHeartDetailDb.setStatus(0); //0为自动测量，1为手动测量
            commHeartDetailDb.setRtc(WatchUtils.getCurrentDate());
            commHeartDetailDb.setTime(w30HeartBean.getTimes());
            commHeartDetailDbsLt.add(commHeartDetailDb);
        }

        //详细心率的上传参数
        List<Map<String,Object>> heartLit = new ArrayList<>();
        Map<String, Object> detailHeartMap = new HashMap<>();
        detailHeartMap.put("deviceCode", bleMac);
        detailHeartMap.put("rtc", currDayStr);
        detailHeartMap.put("userId", userId);
        detailHeartMap.put("heartRateList", commHeartDetailDbsLt);
        heartLit.add(detailHeartMap);
        String jsonStr = gson.toJson(heartLit);
        Log.e(TAG, "-------jsonStr=" + jsonStr);
        OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailHeartUrl(), jsonStr, "33", new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                Log.e(TAG, "-----------上传心率详细数据返回=" + result);
                //uploadSleepDetailToday(bleMac);
            }
        });
    }

}
