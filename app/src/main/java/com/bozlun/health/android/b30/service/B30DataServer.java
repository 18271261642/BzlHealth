package com.bozlun.health.android.b30.service;


import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.h9.utils.H9TimeUtil;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class B30DataServer implements RequestView {

    private static final String TAG = "B30DataServer";

    private volatile static B30DataServer b30DataServer;

    private RequestPressent requestPressent;

    public B30DataServerListener b30DataServerListener;

    public B30DataServer() {
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
    }

    public void detchView(){
        if(requestPressent != null){
            requestPressent.detach();
        }
    }


    public static B30DataServer getB30DataServer(){
        if(b30DataServer == null){
            synchronized (B30DataServer.class){
                if(b30DataServer == null){
                    b30DataServer = new B30DataServer();
                }
            }
        }
        return b30DataServer;
    }


    public void getHistoryData(int day){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.CHINA);
        try{
            String baseUrl = URLs.HTTPs;
            JSONObject jsonObect = new JSONObject();
            String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
            String deviceCode = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);
            jsonObect.put("userId",userId);
            jsonObect.put("deviceCode",deviceCode);
            jsonObect.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate3()), day)));
            Date dateBeforess = H9TimeUtil.getDateBefore(new Date(), 1);
            String nextDay = H9TimeUtil.getValidDateStr2(dateBeforess);
//
            jsonObect.put("endDate",nextDay.substring(0, 10));
            Log.d("-------获取参数=",jsonObect.toString());
            if (requestPressent != null) {
                //获取步数
                requestPressent.getRequestJSONObject(1, baseUrl + URLs.GET_WATCH_DATA_DATA, MyApp.getContext(), jsonObect.toString(), day);
                //获取心率
                requestPressent.getRequestJSONObject(2, baseUrl + "/data/getHeartRateByTime", MyApp.getContext(), jsonObect.toString(), day);
                //获取睡眠
                requestPressent.getRequestJSONObject(3, baseUrl + "/sleep/getSleepByTime", MyApp.getContext(), jsonObect.toString(), day);
                //获取血压
                //获取血压 B36无血压功能
                if(!WatchUtils.isB36Device(MyApp.getInstance().getApplicationContext()))
                    requestPressent.getRequestJSONObject(0x04,baseUrl+"/data/getBloodPressureByTime",MyApp.getContext(),jsonObect.toString(),day);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void showLoadDialog(int what) {
        if(b30DataServerListener != null){
            b30DataServerListener.showProDialog(what);
        }
    }

    @Override
    public void successData(int what, Object object, int daystag) {

        if(b30DataServerListener != null){
            b30DataServerListener.closeProDialog(what);
            if(what == 0x01){  //步数
                b30DataServerListener.b30StepData(object.toString(),daystag);
            }
            if(what == 0x02){  //心率
                b30DataServerListener.b30HeartData(object.toString(),daystag);
            }
            if(what == 0x03){  //睡眠
                b30DataServerListener.b30SleepData(object.toString(),daystag);
            }
            if(what == 0x04){  //血压
                b30DataServerListener.b30BloadData(object.toString(),daystag);
            }

        }
    }

    @Override
    public void failedData(int what, Throwable e) {
        if(b30DataServerListener != null){
            b30DataServerListener.closeProDialog(what);
        }
    }

    @Override
    public void closeLoadDialog(int what) {

    }


    public interface B30DataServerListener{
        void showProDialog(int what);
        void closeProDialog(int what);
        void b30StepData(String stepStr, int tag);
        void b30HeartData(String heartStr, int tag);
        void b30BloadData(String bloadStr, int tag);
        void b30SleepData(String sleepStr, int tag);
    }

    public void setB30DataServerListener(B30DataServerListener b30DataServerListener) {
        this.b30DataServerListener = b30DataServerListener;
    }
}
