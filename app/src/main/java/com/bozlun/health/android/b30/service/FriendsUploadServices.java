package com.bozlun.health.android.b30.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b30.bean.B30HalfHourDB;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.commdbserver.SyncDbUrls;
import com.bozlun.health.android.commdbserver.detail.CommBloodDetailDb;
import com.bozlun.health.android.commdbserver.detail.CommHeartDetailDb;
import com.bozlun.health.android.commdbserver.detail.CommSleepDetailDb;
import com.bozlun.health.android.commdbserver.detail.CommStepDetailDb;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.OkHttpTool;
import com.bozlun.health.android.util.URLs;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.veepoo.protocol.model.datas.HalfHourBpData;
import com.veepoo.protocol.model.datas.HalfHourRateData;
import com.veepoo.protocol.model.datas.HalfHourSportData;
import com.veepoo.protocol.model.datas.SleepData;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 上传好友详细数据的service,维亿魄系列使用，详细数据
 * Created by Admin
 * Date 2019/5/5
 */
public class FriendsUploadServices extends IntentService {

    private static final String TAG = "FriendsUploadServices";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.USER_ID_DATA);

    private Gson gson = new Gson();

    String currDayStr = WatchUtils.getCurrentDate();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param
     */
    public FriendsUploadServices() {
        super("FriendsUploadServices");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String bleMac = WatchUtils.getSherpBleMac(MyApp.getContext());
        Log.e(TAG, "---------启动详细数据上传-----" + bleMac);
        if (WatchUtils.isEmpty(bleMac))
            return;
        if (WatchUtils.isEmpty(userId))
            return;
        //上传当天的详细数据
        uploadStepDetailToday(bleMac);

        //上传昨天和前天的详细步数数据
        uploadStepDetailByDay(bleMac, B30HalfHourDao.TYPE_SPORT, 1);
        //上传昨天和前天的详细睡眠数据
        uploadSleepDetailByDay(bleMac, B30HalfHourDao.TYPE_SLEEP, 1);
        //上传心率详细数据
        uploadHeartDetailByDay(bleMac, B30HalfHourDao.TYPE_RATE, 1);
        //上传血压的详细数据
        uploadBloodDetailByDay(bleMac, B30HalfHourDao.TYPE_BP, 1);
    }

    //上传当天的步数详细数据
    private void uploadStepDetailToday(final String bleMac) {
        Log.e(TAG, "----------上传今天的数据---");
        List<B30HalfHourDB> stepDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac,
                B30HalfHourDao.TYPE_SPORT, currDayStr);
        if (stepDayList != null) {
            B30HalfHourDB b30HalfHourDB = stepDayList.get(0);
            List<HalfHourSportData> halfHourSportDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                    new TypeToken<List<HalfHourSportData>>() {
                    }.getType());
            List<CommStepDetailDb> upStepList = new ArrayList<>();

            //计算累计的总步数和总卡路里，里程
            int countStep = 0;
            double countKcal = 0.0;
            double countDis = 0.0;

            //目标步数
            int goalStep = (int) SharedPreferencesUtils.getParam(MyApp.getContext(), "b30Goal", 8000);


            for (HalfHourSportData stepLt : halfHourSportDataList) {
                countStep += stepLt.getStepValue();
                countDis += stepLt.getDisValue();
                countKcal += stepLt.getCalValue();
                CommStepDetailDb commStepDetailDb = new CommStepDetailDb();
                commStepDetailDb.setUserid(userId);
                commStepDetailDb.setDevicecode(bleMac);
                commStepDetailDb.setRtc(currDayStr);
                commStepDetailDb.setStepnumber(stepLt.stepValue);
                commStepDetailDb.setDistance(stepLt.getDisValue() + "");
                //kcal是int类型
                commStepDetailDb.setCalories(StringUtils.substringBefore(String.valueOf(stepLt.getCalValue()), ".") + "");
                commStepDetailDb.setStartdate(stepLt.getTime().getColck());
                commStepDetailDb.setEnddate("11");
                commStepDetailDb.setSpeed(1);
                commStepDetailDb.setAction(1);
                commStepDetailDb.setStatus(1);
                upStepList.add(commStepDetailDb);

            }


            //汇总的数据
            List<Map<String,String>> countStepList = new ArrayList<>();
            Map<String, String> params = new HashMap<>();
            params.put("userid", userId);
            params.put("stepnumber", countStep + "");
            params.put("date", currDayStr);
            params.put("devicecode", bleMac);
            params.put("count", "111");
            params.put("distance",countDis+"");
            params.put("calorie",countKcal+"");
            params.put("reach",(goalStep<=countStep?1:0)+"");
            countStepList.add(params);
            //Log.e(TAG,"--------上传当天汇总的步数="+gson.toJson(countStepList));
            OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadCountStepUrl(), gson.toJson(countStepList), "55", new OkHttpTool.HttpResult() {
                @Override
                public void onResult(String result) {
                    Log.e(TAG, "-------上传是否达标=" + result);
                }
            });

            List<Map<String, Object>> jsonObjectList = new ArrayList<>();
            Map<String, Object> jsonObject = new HashMap<>();
            jsonObject.put("deviceCode", bleMac);
            jsonObject.put("rtc", currDayStr);
            jsonObject.put("userId", userId);
            jsonObject.put("stepNumberList", upStepList);
            jsonObjectList.add(jsonObject);

            String jsonStr = gson.toJson(jsonObjectList);
            // Log.e(TAG, "-----当天步数详细数据jsonStr=" + jsonStr);
            OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailStepUrl(), jsonStr, "11",
                    new OkHttpTool.HttpResult() {
                        @Override
                        public void onResult(String result) {
                            Log.e(TAG, "--------步数当天详细数据上传=" + result);

                            uploadHeartDetailToDay(bleMac);   //上传当天的心率数据
                        }
                    });
        }
    }


    //上传当天心率的详细数据
    private void uploadHeartDetailToDay(final String bleMac) {
        List<CommHeartDetailDb> commHeartDetailDbsLt = new ArrayList<>();
        List<B30HalfHourDB> heartDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, B30HalfHourDao.TYPE_RATE, currDayStr);
        if (heartDayList == null)
            return;
        B30HalfHourDB b30HalfHourDB = heartDayList.get(0);
        List<HalfHourRateData> hourRateDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                new TypeToken<List<HalfHourRateData>>() {
                }.getType());
        if (hourRateDataList == null || hourRateDataList.isEmpty()) {
            uploadSleepDetailToday(bleMac);
            return;
        }
        for (HalfHourRateData hourRateData : hourRateDataList) {
            CommHeartDetailDb commHeartDetailDb = new CommHeartDetailDb();
            commHeartDetailDb.setUserid(userId);
            commHeartDetailDb.setDevicecode(bleMac);
            commHeartDetailDb.setHeartrate(hourRateData.getRateValue());
            commHeartDetailDb.setStatus(0); //0为自动测量，1为手动测量
            commHeartDetailDb.setRtc(currDayStr);
            commHeartDetailDb.setTime(hourRateData.getTime().getColck());
            commHeartDetailDbsLt.add(commHeartDetailDb);
        }


        //详细心率的上传参数
        List<Map<String,Object>> heartLit = new ArrayList<>();
        Map<String, Object> detailHeartMap = new HashMap<>();
        detailHeartMap.put("deviceCode", bleMac);
        detailHeartMap.put("rtc", currDayStr);
        detailHeartMap.put("userId", userId);
        detailHeartMap.put("heartRateList",commHeartDetailDbsLt);
        heartLit.add(detailHeartMap);
        String jsonStr = gson.toJson(heartLit);
        //Log.e(TAG,"----当天心率---jsonStr="+jsonStr);
        OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailHeartUrl(), jsonStr, "33", new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                Log.e(TAG, "-----------上传心率详细数据返回=" + result);
                uploadSleepDetailToday(bleMac);
            }
        });

    }


    //上传当天睡眠的详细数据
    private void uploadSleepDetailToday(final String bleMac) {
        /**
         *维亿魄系列手环详细的数据保存数据库时是往后+了一天，为何IOS统一，上传时再往后减一天
         */
        String dayStr = WatchUtils.getCurrentDate();
        List<B30HalfHourDB> sleepDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, B30HalfHourDao.TYPE_SLEEP, dayStr);
        String uploadDayStr = WatchUtils.obtainAroundDate(dayStr, true);
        if (sleepDayList != null) {
            B30HalfHourDB b30HalfHourDB = sleepDayList.get(0);
            List<CommSleepDetailDb> commSleepDetailDbList = new ArrayList<>();
            try {
                //睡眠一天只有一条数据
                SleepData sleepData = gson.fromJson(b30HalfHourDB.getOriginData(), SleepData.class);
                //睡眠的表现形式
                String sleepStr = sleepData.getSleepLine();
                Log.e(TAG, "-------sleepStr=" + sleepStr);
                //入睡时间
                String startSleepDate = sleepData.getSleepDown().getDateAndClockForSleepSecond();
                long longStartDate = sdf.parse(startSleepDate).getTime() / 1000;
                /**
                 * 012 0-浅睡；1-深睡；2-清醒
                 */
                for (int i = 0; i < sleepStr.length(); i++) {
                    CommSleepDetailDb commSleepDetailDb = new CommSleepDetailDb();
                    commSleepDetailDb.setDay(uploadDayStr);
                    commSleepDetailDb.setDevicecode(bleMac);
                    commSleepDetailDb.setUserid(userId);
                    String slType = sleepStr.charAt(i) + "";
                    int changeType = Integer.valueOf(slType);
                    int resultType = 0;
                    switch (changeType) {
                        case 0:
                            resultType = 2;
                            break;
                        case 1:
                            resultType = 3;
                            break;
                        case 2:
                            resultType = 1;
                            break;
                    }
                    //Log.e(TAG, "-------转换后的数据=" + resultType);
                    commSleepDetailDb.setSleepType(resultType + "");
                    //时间
                    commSleepDetailDb.setStarttime(WatchUtils.getLongToDate("HH:mm", (longStartDate + (5 * i * 60)) * 1000));
                    //Log.e(TAG, "---------commSleepDetailDb=" + commSleepDetailDb.toString());
                    commSleepDetailDbList.add(commSleepDetailDb);

                }


                //详细睡眠的上传参数
                List<Map<String,Object>> sleepListMap = new ArrayList<>();
                Map<String, Object> detailSleepMap = new HashMap<>();
                detailSleepMap.put("deviceCode", bleMac);
                detailSleepMap.put("rtc", uploadDayStr);
                detailSleepMap.put("userId", userId);
                detailSleepMap.put("sleepSlotList", commSleepDetailDbList);
                sleepListMap.add(detailSleepMap);

                String jsonStr = gson.toJson(sleepListMap);
                //Log.e(TAG, "-----当天睡眠详细数据参数jsonStr=" + jsonStr);
                OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailSleepUrl(), jsonStr, "22",
                        new OkHttpTool.HttpResult() {
                            @Override
                            public void onResult(String result) {
                                Log.e(TAG, "--------睡眠详细数据上传=" + result);
                                uploadBloodDetailToday(bleMac);
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    //上传当天血压的详细数据，不做限制，可上传多次
    private void uploadBloodDetailToday(final String bleMac) {
        List<CommBloodDetailDb> commBloodDetailDbList = new ArrayList<>();
        List<B30HalfHourDB> heartDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, B30HalfHourDao.TYPE_BP, currDayStr);
        if (heartDayList == null)
            return;
        B30HalfHourDB b30HalfHourDB = heartDayList.get(0);
        List<HalfHourBpData> hourBpDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                new TypeToken<List<HalfHourBpData>>() {
                }.getType());

        for (HalfHourBpData hourBpData : hourBpDataList) {
            CommBloodDetailDb commBloodDetailDb = new CommBloodDetailDb();
            commBloodDetailDb.setUserid(userId);
            commBloodDetailDb.setRtc(currDayStr);
            commBloodDetailDb.setTime(hourBpData.getTime().getColck());
            commBloodDetailDb.setDiastolic(hourBpData.getLowValue()); //舒张压，低压
            commBloodDetailDb.setSystolic(hourBpData.getHighValue());
            commBloodDetailDb.setDevicecode(bleMac);
            commBloodDetailDbList.add(commBloodDetailDb);
        }


        //详细血压的上传参数
        List<Map<String,Object>> bpListMap = new ArrayList<>();
        Map<String, Object> detailBppMap = new HashMap<>();
        detailBppMap.put("deviceCode", bleMac);
        detailBppMap.put("rtc", currDayStr);
        detailBppMap.put("userId", userId);
        detailBppMap.put("bloodPressureList", commBloodDetailDbList);
        bpListMap.add(detailBppMap);

        String jsonStr = gson.toJson(bpListMap);
        //Log.e(TAG, "-------jsonStr=" + jsonStr);
        OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailBloodUrl(), jsonStr, "44", new OkHttpTool.HttpResult() {
            @Override
            public void onResult(String result) {
                Log.e(TAG, "-----------上传血压详细数据返回=" + result);

            }
        });

    }


    //上传步数的详细数据
    private void uploadStepDetailByDay(final String bleMac, final String type, final int position) {
        if (position == 3)
            return;
        String dayStr = WatchUtils.obtainFormatDate(position);
        List<B30HalfHourDB> stepDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, type, dayStr);

        if (stepDayList != null) {
            B30HalfHourDB b30HalfHourDB = stepDayList.get(0);
            Log.e(TAG, "-------上传的标识=" + b30HalfHourDB.getUpload());
            //未上传的
            if (b30HalfHourDB.getUpload() == 0) { //0是未上传的
                List<HalfHourSportData> halfHourSportDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                        new TypeToken<List<HalfHourSportData>>() {
                        }.getType());
                List<CommStepDetailDb> upStepList = new ArrayList<>();
                for (HalfHourSportData stepLt : halfHourSportDataList) {
                    CommStepDetailDb commStepDetailDb = new CommStepDetailDb();
                    commStepDetailDb.setUserid(userId);
                    commStepDetailDb.setDevicecode(bleMac);
                    commStepDetailDb.setRtc(dayStr);
                    commStepDetailDb.setStepnumber(stepLt.stepValue);
                    commStepDetailDb.setDistance(stepLt.getDisValue() + "");
                    commStepDetailDb.setCalories(StringUtils.substringBefore(String.valueOf(stepLt.getCalValue()), ".") + "");
                    commStepDetailDb.setStartdate(stepLt.getTime().getColck());
                    commStepDetailDb.setEnddate("11");
                    commStepDetailDb.setSpeed(1);
                    commStepDetailDb.setAction(1);
                    commStepDetailDb.setStatus(1);
                    upStepList.add(commStepDetailDb);
                }

                //详细步数的上传参数
                List<Map<String, Object>> jsonObjectList = new ArrayList<>();
                Map<String, Object> jsonObject = new HashMap<>();
                jsonObject.put("deviceCode", bleMac);
                jsonObject.put("rtc", dayStr);
                jsonObject.put("userId", userId);
                jsonObject.put("stepNumberList", upStepList);
                jsonObjectList.add(jsonObject);

                String jsonStr = gson.toJson(jsonObjectList);
                //Log.e(TAG, "-----jsonStr=" + jsonStr);
                OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailStepUrl(), jsonStr, "11",
                        new OkHttpTool.HttpResult() {
                            @Override
                            public void onResult(String result) {
                                Log.e(TAG, "--------步数详细数据上传=" + position + "----" + result);
                                if (WatchUtils.isNetRequestSuccess(result)) { //上传成功
                                    updateStepDetailStatus(bleMac, type, position);
                                }

                            }
                        });
            }

        }


    }


    //步数上传成功后修改状态
    private void updateStepDetailStatus(String mac, String type, int posi) {
        B30HalfHourDB b30HalfHourDB = new B30HalfHourDB();
        String whereStr = "address = ? and date = ? and type = ?";
        String day = WatchUtils.obtainFormatDate(posi);
        b30HalfHourDB.setUpload(1);
        boolean isSave = b30HalfHourDB.saveOrUpdate(whereStr, mac, day, type);
        Log.e(TAG, "-------修改步数返回=" + isSave);

        //后一天
        int currPosition = posi + 1;
        uploadStepDetailByDay(mac, type, currPosition);

    }


    //上传睡眠的详细数据
    private void uploadSleepDetailByDay(final String bleMac, final String type, final int position) {
        if (position == 3)
            return;
        String dayStr = WatchUtils.obtainFormatDate(position);
        String upDayStr = WatchUtils.obtainAroundDate(dayStr, true);

        List<B30HalfHourDB> sleepDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, type, dayStr);

        if (sleepDayList != null) {
            B30HalfHourDB b30HalfHourDB = sleepDayList.get(0);
            //未上传的
            if (b30HalfHourDB.getUpload() == 0) { //0是未上传的
                List<CommSleepDetailDb> commSleepDetailDbList = new ArrayList<>();
                try {
                    //睡眠一天只有一条数据
                    SleepData sleepData = gson.fromJson(b30HalfHourDB.getOriginData(), SleepData.class);
                    //睡眠的表现形式
                    String sleepStr = sleepData.getSleepLine();
                    //Log.e(TAG, "-------sleepStr=" + sleepStr);
                    //入睡时间
                    String startSleepDate = sleepData.getSleepDown().getDateAndClockForSleepSecond();
                    long longStartDate = sdf.parse(startSleepDate).getTime() / 1000;
                    /**
                     * 012 0-浅睡；1-深睡；2-清醒
                     */
                    for (int i = 0; i < sleepStr.length(); i++) {
                        CommSleepDetailDb commSleepDetailDb = new CommSleepDetailDb();
                        commSleepDetailDb.setDay(upDayStr);
                        commSleepDetailDb.setDevicecode(bleMac);
                        commSleepDetailDb.setUserid(userId);
                        String slType = sleepStr.charAt(i) + "";
                        int changeType = Integer.valueOf(slType);
                        int resultType = 0;
                        switch (changeType) {
                            case 0:
                                resultType = 2;
                                break;
                            case 1:
                                resultType = 3;
                                break;
                            case 2:
                                resultType = 1;
                                break;
                        }
                        commSleepDetailDb.setSleepType(resultType + "");
                        //时间
                        commSleepDetailDb.setStarttime(WatchUtils.getLongToDate("HH:mm", (longStartDate + (5 * i * 60)) * 1000));
                        //Log.e(TAG, "---------commSleepDetailDb=" + commSleepDetailDb.toString());
                        commSleepDetailDbList.add(commSleepDetailDb);

                    }

                    //详细睡眠的上传参数
                    List<Map<String,Object>> sleepLtMap = new ArrayList<>();
                    Map<String, Object> detailSleepMap = new HashMap<>();
                    detailSleepMap.put("deviceCode", bleMac);
                    detailSleepMap.put("rtc", upDayStr);
                    detailSleepMap.put("userId", userId);
                    detailSleepMap.put("sleepSlotList", commSleepDetailDbList);
                    sleepLtMap.add(detailSleepMap);

                    String jsonStr = gson.toJson(sleepLtMap);

                    //Log.e(TAG, "-----详细睡眠参数=" + jsonStr);
                    OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailSleepUrl(), jsonStr, "22",
                            new OkHttpTool.HttpResult() {
                                @Override
                                public void onResult(String result) {
                                    Log.e(TAG, "--------睡眠详细数据上传=" + result);
                                    //上传成功后修改是否上传的标识
                                    updateSleepDetailStatus(bleMac, type, position);
                                }
                            });


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    //睡眠上传成功后修改数据
    private void updateSleepDetailStatus(String bleMac, String type, int position) {
        B30HalfHourDB b30HalfHourDB = new B30HalfHourDB();
        String whereStr = "address = ? and date = ? and type = ?";
        String day = WatchUtils.obtainFormatDate(position);
        b30HalfHourDB.setUpload(1);
        boolean isSave = b30HalfHourDB.saveOrUpdate(whereStr, bleMac, day, type);
        Log.e(TAG, "-------修改步数返回=" + isSave);

        int currPosition = position + 1;
        uploadSleepDetailByDay(bleMac, type, currPosition);

    }


    //上传心率的详细数据
    private void uploadHeartDetailByDay(final String bleMac, final String type, final int position) {
        if (position == 3)
            return;
        List<CommHeartDetailDb> commHeartDetailDbsLt = new ArrayList<>();
        String dayStr = WatchUtils.obtainFormatDate(position);
        List<B30HalfHourDB> heartDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, type, dayStr);
        if (heartDayList == null)
            return;
        B30HalfHourDB b30HalfHourDB = heartDayList.get(0);
        if (b30HalfHourDB.getUpload() == 0) {
            List<HalfHourRateData> hourRateDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                    new TypeToken<List<HalfHourRateData>>() {
                    }.getType());

            for (HalfHourRateData hourRateData : hourRateDataList) {
                CommHeartDetailDb commHeartDetailDb = new CommHeartDetailDb();
                commHeartDetailDb.setUserid(userId);
                commHeartDetailDb.setDevicecode(bleMac);
                commHeartDetailDb.setHeartrate(hourRateData.getRateValue());
                commHeartDetailDb.setStatus(0); //0为自动测量，1为手动测量
                commHeartDetailDb.setRtc(dayStr);
                commHeartDetailDb.setTime(hourRateData.getTime().getColck());
                commHeartDetailDbsLt.add(commHeartDetailDb);
            }

            //详细心率的上传参数
            List<Map<String,Object>> htLtMap = new ArrayList<>();
            Map<String, Object> detailHeartMap = new HashMap<>();
            detailHeartMap.put("deviceCode", bleMac);
            detailHeartMap.put("rtc", dayStr);
            detailHeartMap.put("userId", userId);
            detailHeartMap.put("heartRateList", commHeartDetailDbsLt);
            htLtMap.add(detailHeartMap);
            String jsonStr = gson.toJson(htLtMap);
            //Log.e(TAG,"-------jsonStr="+jsonStr);
            OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailHeartUrl(), jsonStr, "33", new OkHttpTool.HttpResult() {
                @Override
                public void onResult(String result) {
                    Log.e(TAG, "-----------上传心率详细数据返回=" + result);
                    if (WatchUtils.isNetRequestSuccess(result))
                        updateHeartDetailStatus(bleMac, type, position);
                }
            });


        }

    }

    //心率详细数据上传成功后修改数据
    private void updateHeartDetailStatus(String bleMac, String type, int position) {
        B30HalfHourDB b30HalfHourDB = new B30HalfHourDB();
        String whereStr = "address = ? and date = ? and type = ?";
        String day = WatchUtils.obtainFormatDate(position);
        b30HalfHourDB.setUpload(1);
        boolean isSave = b30HalfHourDB.saveOrUpdate(whereStr, bleMac, day, type);
        Log.e(TAG, "-------修改心率返回=" + isSave);

        int currPosition = position + 1;
        uploadHeartDetailByDay(bleMac, type, currPosition);
    }


    //上传血压的详细数据
    private void uploadBloodDetailByDay(final String bleMac, final String type, final int position) {
        if (position == 3)
            return;
        List<CommBloodDetailDb> commBloodDetailDbList = new ArrayList<>();
        String dayStr = WatchUtils.obtainFormatDate(position);
        List<B30HalfHourDB> heartDayList = B30HalfHourDao.getInstance().findNotUpDataByDay(bleMac, type, dayStr);
        if (heartDayList == null)
            return;
        B30HalfHourDB b30HalfHourDB = heartDayList.get(0);
        if (b30HalfHourDB.getUpload() == 0) {
            List<HalfHourBpData> hourBpDataList = gson.fromJson(b30HalfHourDB.getOriginData(),
                    new TypeToken<List<HalfHourBpData>>() {
                    }.getType());

            for (HalfHourBpData hourBpData : hourBpDataList) {
                CommBloodDetailDb commBloodDetailDb = new CommBloodDetailDb();
                commBloodDetailDb.setUserid(userId);
                commBloodDetailDb.setRtc(dayStr);
                commBloodDetailDb.setTime(hourBpData.getTime().getColck());
                commBloodDetailDb.setDiastolic(hourBpData.getLowValue()); //舒张压，低压
                commBloodDetailDb.setSystolic(hourBpData.getHighValue());
                commBloodDetailDb.setDevicecode(bleMac);
                commBloodDetailDbList.add(commBloodDetailDb);
            }


            //详细血压的上传参数
            List<Map<String,Object>> bpLtMap = new ArrayList<>();
            Map<String, Object> detailBppMap = new HashMap<>();
            detailBppMap.put("deviceCode", bleMac);
            detailBppMap.put("rtc", dayStr);
            detailBppMap.put("userId", userId);
            detailBppMap.put("bloodPressureList", commBloodDetailDbList);
            bpLtMap.add(detailBppMap);
            String jsonStr = gson.toJson(bpLtMap);
            //Log.e(TAG, "-------jsonStr=" + jsonStr);
            OkHttpTool.getInstance().doRequest(SyncDbUrls.uploadDetailBloodUrl(), jsonStr, "44", new OkHttpTool.HttpResult() {
                @Override
                public void onResult(String result) {
                    Log.e(TAG, "-----------上传血压详细数据返回=" + result);
                    if (WatchUtils.isNetRequestSuccess(result))
                        updateBloodDetailStatus(bleMac, type, position);
                }
            });

        }
    }

    //上传血压成功后修改标识
    private void updateBloodDetailStatus(String bleMac, String type, int position) {
        B30HalfHourDB b30HalfHourDB = new B30HalfHourDB();
        String whereStr = "address = ? and date = ? and type = ?";
        String day = WatchUtils.obtainFormatDate(position);
        b30HalfHourDB.setUpload(1);
        boolean isSave = b30HalfHourDB.saveOrUpdate(whereStr, bleMac, day, type);
        Log.e(TAG, "-------修改血压返回=" + isSave);

        int currPosition = position + 1;
        uploadBloodDetailByDay(bleMac, type, currPosition);
    }

}
