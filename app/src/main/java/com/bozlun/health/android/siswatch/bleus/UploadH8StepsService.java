package com.bozlun.health.android.siswatch.bleus;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.siswatch.dataserver.UploadStepsPressent;
import com.bozlun.health.android.siswatch.dataserver.UploadStepsView;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 上传步数
 */
public class UploadH8StepsService{

    private static volatile UploadH8StepsService uploadH8StepsService;
    private static UploadStepsPressent uploadStepsPressent;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    public static synchronized UploadH8StepsService getUploadH8StepsService(){
        if(uploadH8StepsService == null){
            synchronized (UploadH8StepsService.class){
                if(uploadH8StepsService== null){
                    uploadH8StepsService = new UploadH8StepsService();
                    uploadStepsPressent = new UploadStepsPressent();
                    uploadStepsPressent.attach(uploadStepsView);
                }
            }
        }
        return uploadH8StepsService;
    }


    public void syncUserStepsData(Map<String,Integer> stpMap){
        String height = (String) SharedPreferencesUtils.getParam(MyApp.getContext(),"userheight","");
        int userHeight = Integer.valueOf(height);   //身高
        String tagNum = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), "settagsteps", "");
        int targetStep = Integer.valueOf(tagNum.trim());   //目标步数
        int targetStatus;   //是否达标
        //用户ID
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
        //设备地址
        String deviceCode = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);


        //当天的数据
        int todaySteps = stpMap.get("today");   //当天的步数
        //当天的路程
        double todayDisc = WatchUtils.getDistants(Integer.valueOf(todaySteps), WatchUtils.getStepLong(userHeight));
        //当天的卡路里
        double todayKcal = WatchUtils.mul(todayDisc, WatchUtils.kcalcanstanc);
        int todayStatus = todaySteps - targetStep;
        if(todayStatus >=0){
            targetStatus = 1;
        }else{
            targetStatus = 0;
        }
        //上传当天的数据
        uploadTodaySteps(userId,deviceCode,targetStatus,todaySteps,todayDisc,todayKcal, WatchUtils.getCurrentDate()); //上传当天的数据

        //昨天的数据
        int yestodaySteps = stpMap.get("yestoday");   //昨天的步数
        //昨天的路程
        double yestodayDisc = WatchUtils.getDistants(Integer.valueOf(yestodaySteps), WatchUtils.getStepLong(userHeight));
        //昨天的卡路里
        double yestodayKcal = WatchUtils.mul(yestodayDisc, WatchUtils.kcalcanstanc);
        int yestodayStatus = yestodaySteps - targetStep;
        if(yestodayStatus >=0){
            targetStatus = 1;
        }else{
            targetStatus = 0;
        }
        //上传昨天的数据
        uploadYesTodayStepsToServer(userId,deviceCode,yestodaySteps,yestodayDisc,yestodayKcal,targetStatus);


        //前天的数据
        int thirddaySteps = stpMap.get("qiantian");   //昨天的步数
        //昨天的路程
        double thirddayDisc = WatchUtils.getDistants(Integer.valueOf(thirddaySteps), WatchUtils.getStepLong(userHeight));
        //昨天的卡路里
        double thirddayKcal = WatchUtils.mul(thirddayDisc, WatchUtils.kcalcanstanc);
        int thirddayStatus = thirddaySteps - targetStep;
        if(thirddayStatus >=0){
            targetStatus = 1;
        }else{
            targetStatus = 0;
        }
        //上传前天数据
        uploadThirdStepsToServer(userId,deviceCode,thirddaySteps,thirddayDisc,thirddayKcal,targetStatus);


        //前后天数据
        int fourthdaySteps = stpMap.get("fourthDay");   //昨天的步数
        //昨天的路程
        double fourthdayDisc = WatchUtils.getDistants(Integer.valueOf(fourthdaySteps), WatchUtils.getStepLong(userHeight));
        //昨天的卡路里
        double fourthdayKcal = WatchUtils.mul(fourthdayDisc, WatchUtils.kcalcanstanc);
        int fourthStatus = fourthdaySteps - targetStep;
        if(fourthStatus >=0){
            targetStatus = 1;
        }else{
            targetStatus = 0;
        }
        uploadForuthDaysToServier(userId,deviceCode,fourthdaySteps,fourthdayDisc,fourthdayKcal,targetStatus);
    }

    //上传当天的数据
    private void uploadTodaySteps(String userId, String deviceCode, int targetStatus, int todaySteps, double todayDisc, double todayKcal, String currentDate) {
        String syncUrl = URLs.HTTPs + URLs.upSportData;
        Map<String,Object> map = new HashMap<>();
        map.put("stepNums",todaySteps);
        map.put("userId",userId);
        map.put("deviceCode",deviceCode);
        map.put("targetStatus",targetStatus);
        map.put("distance",""+todayDisc);
        map.put("calories",""+todayKcal);
        map.put("currentDate",currentDate);
        uploadStepsPressent.pressentUploadData(MyApp.getContext(),syncUrl,map);
    }

    //上传前后天的数据
    private void uploadForuthDaysToServier(String userId, String deviceCode, int fourthdaySteps, double fourthdayDisc, double fourthdayKcal, int targetStatus) {
        String syncUrl = URLs.HTTPs + URLs.upSportData;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("stepNums", fourthdaySteps);
            map.put("userId", userId);
            map.put("deviceCode", deviceCode);
            map.put("targetStatus", targetStatus);
            map.put("distance", "" + fourthdayDisc);
            map.put("calories", "" + fourthdayKcal);
            map.put("currentDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), 2)) + "");
            uploadStepsPressent.pressentUploadData(MyApp.getContext(), syncUrl, map);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //上传前天的数据
    private void uploadThirdStepsToServer(String userId, String deviceCode, int thirddaySteps, double thirddayDisc, double thirddayKcal,int targetStatus) {
        String syncUrl = URLs.HTTPs + URLs.upSportData;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("stepNums", thirddaySteps);
            map.put("userId", userId);
            map.put("deviceCode", deviceCode);
            map.put("targetStatus", targetStatus);
            map.put("distance", "" + thirddayDisc);
            map.put("calories", "" + thirddayKcal);
            map.put("currentDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), 2)) + "");
            uploadStepsPressent.pressentUploadData(MyApp.getContext(), syncUrl, map);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //上传昨天的数据
    private void uploadYesTodayStepsToServer(String userId,String deviceCode,int yestodaySteps, double yestodayDisc, double yestodayKcal, int targetStatus) {
        String syncUrl = URLs.HTTPs + URLs.upSportData;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("stepNums", yestodaySteps);
            map.put("userId", userId);
            map.put("deviceCode", deviceCode);
            map.put("targetStatus", targetStatus);
            map.put("distance", "" + yestodayDisc);
            map.put("calories", "" + yestodayKcal);
            map.put("currentDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), 1)) + "");
            uploadStepsPressent.pressentUploadData(MyApp.getContext(), syncUrl, map);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static UploadStepsView uploadStepsView = new UploadStepsView() {
        @Override
        public void uploadResultData(Object object) {

        }
    };

}
