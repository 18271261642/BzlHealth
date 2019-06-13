package com.bozlun.health.android.b31.bpoxy;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b31.model.B31HRVBean;
import com.bozlun.health.android.b31.model.B31Spo2hBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.LocalizeTool;
import com.bozlun.health.android.util.ToastUtil;
import com.google.gson.Gson;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IHRVOriginDataListener;
import com.veepoo.protocol.listener.data.ISpo2hOriginDataListener;
import com.veepoo.protocol.model.datas.HRVOriginData;
import com.veepoo.protocol.model.datas.Spo2hOriginData;
import com.veepoo.protocol.model.settings.ReadOriginSetting;

import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Admin
 * Date 2018/12/25
 */
public class ReadHRVAnSpo2DatatService extends IntentService {

    private static final String TAG = "ReadHRVAnSpo2DatatServi";


    Gson gson = new Gson();

    private boolean isToday = false;


    //血氧的进度
    float spo2DataProgress = -1;
    //HRV的进度
    float hrvDataProgress = -1;



    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 888:   //HRV读取 完了
                    final Map<String,List<B31HRVBean>> resultHrvMap = (Map<String, List<B31HRVBean>>) msg.obj;
                    if(resultHrvMap == null || resultHrvMap.isEmpty())
                        return;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveHRVToDBServer(resultHrvMap);
                        }
                    }).start();
                    break;
                case 999:   //血氧
                    final Map<String,List<B31Spo2hBean>> resultMap = (Map<String, List<B31Spo2hBean>>) msg.obj;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveSpo2Data(resultMap);
                        }
                    }).start();
                    break;
            }


        }
    };

    //保存HRV的数据
    private void saveHRVToDBServer(Map<String,List<B31HRVBean>> resultHrvMap) {
        try {
            String where = "bleMac = ? and dateStr = ?";
            String bleMac = WatchUtils.getSherpBleMac(MyApp.getContext());
            String currDayStr = WatchUtils.getCurrentDate();
            if(isToday){
                List<B31HRVBean> todayHrvList = resultHrvMap.get("today");
                List<B31HRVBean> currHrvLt = LitePal.where(where,
                        bleMac,currDayStr).find(B31HRVBean.class);
                //Log.e(TAG,"-------今天currHrvLtsez="+currHrvLt.size());
                if(currHrvLt == null || currHrvLt.isEmpty()){
                    LitePal.saveAll(todayHrvList);
                }else{
                    if(currHrvLt.size() != 420){
                        int delCode = LitePal.deleteAll(B31HRVBean.class,"dateStr=? and bleMac=?",currDayStr
                                ,bleMac);
                        //Log.e(TAG,"----hrv--111--delCode="+delCode);
                        LitePal.saveAll(todayHrvList);
                    }
                }

            }else{
                //今天的 直接保存
                List<B31HRVBean> todayHrvList = resultHrvMap.get("today");

                if(todayHrvList != null && !todayHrvList.isEmpty())
                    LitePal.saveAll(todayHrvList);

                //昨天的
                List<B31HRVBean> yesDayResultHrvLt = resultHrvMap.get("yesDay");
                //保存的 是否存在
                List<B31HRVBean> yesDayHrvLt = LitePal.where(where,
                        WatchUtils.getSherpBleMac(MyApp.getContext()),WatchUtils.obtainFormatDate(1)).find(B31HRVBean.class);
                if(yesDayHrvLt == null || yesDayHrvLt.isEmpty())
                    LitePal.saveAll(yesDayResultHrvLt);

                //前天的
                List<B31HRVBean> threeDayResultHrvLt = resultHrvMap.get("threeDay");
                List<B31HRVBean> threeDayHrvLt = LitePal.where(where,
                        WatchUtils.getSherpBleMac(MyApp.getContext()),WatchUtils.obtainFormatDate(2)).find(B31HRVBean.class);
                if(threeDayHrvLt == null || threeDayHrvLt.isEmpty())
                    LitePal.saveAll(threeDayResultHrvLt);

            }

            //发送广播，通知更新UI
            Intent intent = new Intent();
            intent.setAction(WatchUtils.B31_HRV_COMPLETE);
            sendBroadcast(intent);


            Thread thread2 = new MyThread2();
            thread2.start();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    //保存spo2数据
    private void saveSpo2Data(Map<String,List<B31Spo2hBean>> resultMap) {
        try {
            if (resultMap != null && !resultMap.isEmpty()) {
                String where = "bleMac = ? and dateStr = ?";
                String bleMac = WatchUtils.getSherpBleMac(MyApp.getContext());
                String currDayStr = WatchUtils.getCurrentDate();
                if (isToday) {    //今天
                    List<B31Spo2hBean> todayLt = resultMap.get("today");
//            Log.e(TAG, "---todayLt=" + todayLt.size());
                    //查询一下是否存在
                    List<B31Spo2hBean> currList = LitePal.where(where, WatchUtils.getSherpBleMac(MyApp.getContext()),
                            WatchUtils.getCurrentDate()).find(B31Spo2hBean.class);
                    //Log.e(TAG,"-----------11今天="+currList.size());
                    if (currList == null || currList.isEmpty()) {
                        LitePal.saveAll(todayLt);
                    } else {
                        if (currList.size() != 420) {
                            int delCode = LitePal.deleteAll(B31Spo2hBean.class, "dateStr=? and bleMac=?", currDayStr
                                    , bleMac);
//                    Log.e(TAG, "--------delCode=" + delCode);

                            LitePal.saveAll(todayLt);

                        }
                    }
                } else {
                    //今天
                    List<B31Spo2hBean> todayLt = resultMap.get("today");
                    if (todayLt != null && !todayLt.isEmpty()) {
                        LitePal.saveAll(todayLt);
                    }

                    //昨天
                    List<B31Spo2hBean> yesDayResult = resultMap.get("yesToday");
                    if (yesDayResult != null && !yesDayResult.isEmpty()) {
                        //查询一下是否存在
                        List<B31Spo2hBean> yesDayList = LitePal.where(where, WatchUtils.getSherpBleMac(MyApp.getContext()),
                                WatchUtils.obtainFormatDate(1)).find(B31Spo2hBean.class);
                        //Log.e(TAG,"-----------22昨天="+todayLt.size());
                        if (yesDayList == null || yesDayList.isEmpty()) {
                            LitePal.saveAll(yesDayResult);
                        }
                    }


                    //前天
                    List<B31Spo2hBean> threeDayResult = resultMap.get("threeDay");
                    if (threeDayResult != null && !threeDayResult.isEmpty()) {
                        //查询一下是否存在
                        List<B31Spo2hBean> threeDayList = LitePal.where(where, WatchUtils.getSherpBleMac(MyApp.getContext()),
                                WatchUtils.obtainFormatDate(2)).find(B31Spo2hBean.class);
                        //Log.e(TAG,"--------333-threeDayList="+threeDayList.size());
                        if (threeDayList == null || threeDayList.isEmpty()) {
                            LitePal.saveAll(threeDayResult);
                        }
                    }

                }
            }

            Intent intent = new Intent();
            intent.setAction(WatchUtils.B31_SPO2_COMPLETE);
            sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public ReadHRVAnSpo2DatatService() {
        super("ReadHRVAnSpo2DatatService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ReadHRVAnSpo2DatatService(String name) {
        super(name);
    }


    private LocalizeTool mLocalTool;


    //HRV
    private List<B31HRVBean> b31HRVBeanList;

    //血氧
    private List<B31Spo2hBean> b31Spo2hBeanList;

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e(TAG,"----------启动服务了======");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WatchUtils.B31_HRV_COMPLETE);
        intentFilter.addAction(WatchUtils.B31_SPO2_COMPLETE);
        registerReceiver(broadcastReceiver,intentFilter);
        mLocalTool = new LocalizeTool(MyApp.getContext());
        String date = mLocalTool.getSpo2AdHRVUpdateDate();// 血氧和HRV最后更新总数据的日期
        if(WatchUtils.isEmpty(date))
            date = WatchUtils.obtainFormatDate(1);
        /**
         * true 今天，false三天
         */
        isToday = date.equals(WatchUtils.getCurrentDate());

    }



    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        //isToday = intent != null && intent.getBooleanExtra("isToday",false);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Thread thread1 = new MyThread();
        thread1.start();

    }


    class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            readDeviceData();
        }
    }


    class MyThread2 extends Thread{
        @Override
        public void run() {
            super.run();
            readSpo2Data();
        }
    }

    //读取血氧的数据
    private void readSpo2Data(){
        Log.e(TAG,"-------isToday="+isToday);
        if(b31Spo2hBeanList == null)
            b31Spo2hBeanList = new ArrayList<>();
        b31Spo2hBeanList.clear();

        final List<B31Spo2hBean> yesSpo2List = new ArrayList<>();
        final List<B31Spo2hBean> threeSpo2List = new ArrayList<>();
        final Map<String,List<B31Spo2hBean>> spo2Map = new HashMap<>();
        //读取血氧的数据
        MyApp.getInstance().getVpOperateManager().readSpo2hOrigin(bleWriteResponse, new ISpo2hOriginDataListener() {
            @Override
            public void onReadOriginProgress(float v) {
                spo2DataProgress =  v;
                if(String.valueOf(v).equals("1.0")){
                    if(isToday){
                        spo2Map.put("today",b31Spo2hBeanList);
                    }else{
                        spo2Map.put("today",b31Spo2hBeanList);
                        spo2Map.put("yesToday",yesSpo2List);
                        spo2Map.put("threeDay",threeSpo2List);
                    }

                    Message message = handler.obtainMessage();
                    message.what = 999;
                    message.obj = spo2Map;
                    handler.sendMessage(message);
                    mLocalTool.putSpo2AdHRVUpdateDate(WatchUtils.getCurrentDate());
                }
            }

            @Override
            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {

            }

            @Override
            public void onSpo2hOriginListener(Spo2hOriginData spo2hOriginData) {
                if(spo2hOriginData == null)
                    return;
                try {
                    if(isToday){    //只获取当天的，当天
                        B31Spo2hBean b31Spo2hBean = new B31Spo2hBean();
                        b31Spo2hBean.setBleMac(MyApp.getInstance().getMacAddress());
                        b31Spo2hBean.setDateStr(spo2hOriginData.getDate());
                        b31Spo2hBean.setSpo2hOriginData(gson.toJson(spo2hOriginData));
                        b31Spo2hBeanList.add(b31Spo2hBean);
                    }else{  //今天 昨天 前天
                        if(spo2hOriginData.getDate().equals(WatchUtils.getCurrentDate())){  //今天
                            B31Spo2hBean b31Spo2hBean = new B31Spo2hBean();
                            b31Spo2hBean.setBleMac(MyApp.getInstance().getMacAddress());
                            b31Spo2hBean.setDateStr(spo2hOriginData.getDate());
                            b31Spo2hBean.setSpo2hOriginData(gson.toJson(spo2hOriginData));
                            b31Spo2hBeanList.add(b31Spo2hBean);
                        }else if(spo2hOriginData.getDate().equals(WatchUtils.obtainFormatDate(1))){ //昨天
                            B31Spo2hBean b31Spo2hBean = new B31Spo2hBean();
                            b31Spo2hBean.setBleMac(MyApp.getInstance().getMacAddress());
                            b31Spo2hBean.setDateStr(spo2hOriginData.getDate());
                            b31Spo2hBean.setSpo2hOriginData(gson.toJson(spo2hOriginData));
                            yesSpo2List.add(b31Spo2hBean);
                        }else if(spo2hOriginData.getDate().equals(WatchUtils.obtainFormatDate(2))){ //前天
                            B31Spo2hBean b31Spo2hBean = new B31Spo2hBean();
                            b31Spo2hBean.setBleMac(MyApp.getInstance().getMacAddress());
                            b31Spo2hBean.setDateStr(spo2hOriginData.getDate());
                            b31Spo2hBean.setSpo2hOriginData(gson.toJson(spo2hOriginData));
                            threeSpo2List.add(b31Spo2hBean);
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onReadOriginComplete() {


            }
        },  isToday ? 1 : 3);
    }



    private void readDeviceData() {
        Log.e(TAG,"--11----isToday="+isToday);
        if(b31HRVBeanList == null)
            b31HRVBeanList = new ArrayList<>();
        b31HRVBeanList.clear();
        final List<B31HRVBean> yesHrvList = new ArrayList<>();
        final List<B31HRVBean> threeDayHrvList = new ArrayList<>();
        final Map<String,List<B31HRVBean>> hrvMap = new HashMap<>();

        try {
            MyApp.getInstance().getVpOperateManager().readHRVOrigin(bleWriteResponse, new IHRVOriginDataListener() {
                @Override
                public void onReadOriginProgress(float v) {
                    hrvDataProgress =  v;
                    if(String.valueOf(v).equals("1.0")){
                        if(isToday){
                            hrvMap.put("today",b31HRVBeanList);
                        }else{
                            hrvMap.put("today",b31HRVBeanList);
                            hrvMap.put("yesDay",yesHrvList);
                            hrvMap.put("threeDay",threeDayHrvList);
                        }
                        Message message = handler.obtainMessage();
                        message.what = 888;
                        message.obj = hrvMap;
                        handler.sendMessage(message);
                    }
                }

                @Override
                public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {

                }

                @Override
                public void onHRVOriginListener(HRVOriginData hrvOriginData) {
                    if(isToday){    //当天的
                        B31HRVBean hrvBean = new B31HRVBean();
                        hrvBean.setBleMac(MyApp.getInstance().getMacAddress());
                        hrvBean.setDateStr(hrvOriginData.getDate());
                        hrvBean.setHrvDataStr(gson.toJson(hrvOriginData));
                        b31HRVBeanList.add(hrvBean);
                    }else{
                        if(hrvOriginData.getDate().equals(WatchUtils.getCurrentDate())){    //当天
                            B31HRVBean hrvBean = new B31HRVBean();
                            hrvBean.setBleMac(MyApp.getInstance().getMacAddress());
                            hrvBean.setDateStr(hrvOriginData.getDate());
                            hrvBean.setHrvDataStr(gson.toJson(hrvOriginData));
                            b31HRVBeanList.add(hrvBean);
                        }else if(hrvOriginData.getDate().equals(WatchUtils.obtainFormatDate(1))){   //昨天
                            B31HRVBean hrvBean = new B31HRVBean();
                            hrvBean.setBleMac(MyApp.getInstance().getMacAddress());
                            hrvBean.setDateStr(hrvOriginData.getDate());
                            hrvBean.setHrvDataStr(gson.toJson(hrvOriginData));
                            yesHrvList.add(hrvBean);
                        }else if(hrvOriginData.getDate().equals(WatchUtils.obtainFormatDate(2))){   //前天
                            B31HRVBean hrvBean = new B31HRVBean();
                            hrvBean.setBleMac(MyApp.getInstance().getMacAddress());
                            hrvBean.setDateStr(hrvOriginData.getDate());
                            hrvBean.setHrvDataStr(gson.toJson(hrvOriginData));
                            threeDayHrvList.add(hrvBean);
                        }
                    }

                }

                @Override
                public void onDayHrvScore(int i, String s, int i1) {

                }

                @Override
                public void onReadOriginComplete() {

                }
            },isToday? 1 : 3);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private IBleWriteResponse bleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.e(TAG,"-----------销毁了---------");
        if(broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }
}
