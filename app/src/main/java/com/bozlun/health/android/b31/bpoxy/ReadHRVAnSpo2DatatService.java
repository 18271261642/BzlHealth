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
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.b31.model.B31HRVBean;
import com.bozlun.health.android.b31.model.B31Spo2hBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.LocalizeTool;
import com.google.gson.Gson;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IHRVOriginDataListener;
import com.veepoo.protocol.listener.data.ISpo2hOriginDataListener;
import com.veepoo.protocol.model.datas.HRVOriginData;
import com.veepoo.protocol.model.datas.Spo2hOriginData;


/**
 * Created by Admin
 * Date 2018/12/25
 */
public class ReadHRVAnSpo2DatatService extends IntentService {

    private static final String TAG = "ReadHRVAnSpo2DatatServi";


    Gson gson = new Gson();

    private boolean isToday = true;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 1001:
                    HRVOriginData hrvOriginData = (HRVOriginData) msg.obj;
                    //Log.e(TAG,"------hrvOriginData="+hrvOriginData.getmTime().getDateADC()+"==="+hrvOriginData.toString());
                    //读取完成了
                    B31HRVBean hrvBean = new B31HRVBean();
                    hrvBean.setBleMac(MyApp.getInstance().getMacAddress());
                    hrvBean.setDateStr(hrvOriginData.getDate());
                    hrvBean.setCurrHrvDate(hrvOriginData.getmTime().getDateAndClockAndSecondForDb());
                    hrvBean.setHrvDataStr(gson.toJson(hrvOriginData));
                    B30HalfHourDao.getInstance().saveB31HRVData(hrvBean);

                    break;
                case 1002:
                    Spo2hOriginData spo2hOriginData = (Spo2hOriginData) msg.obj;
                    //Log.e(TAG,"-----------spo2hOriginData="+spo2hOriginData.getmTime().getDateADC()+"==="+spo2hOriginData.toString());
                    B31Spo2hBean b31Spo2hBean = new B31Spo2hBean();
                    b31Spo2hBean.setDateStr(spo2hOriginData.getDate());
                    b31Spo2hBean.setBleMac(MyApp.getInstance().getMacAddress());
                    b31Spo2hBean.setSpo2hOriginData(gson.toJson(spo2hOriginData));
                    b31Spo2hBean.setSpo2currDate(spo2hOriginData.getmTime().getDateAndClockAndSecondForDb());

                    B30HalfHourDao.getInstance().saveB31Spo2hData(b31Spo2hBean);
                    break;
            }


        }
    };



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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"----------启动服务了======");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WatchUtils.B31_HRV_COMPLETE);
        intentFilter.addAction(WatchUtils.B31_SPO2_COMPLETE);
        registerReceiver(broadcastReceiver,intentFilter);
        mLocalTool = new LocalizeTool(MyApp.getContext());
        String date = mLocalTool.getUpdateDate();// 最后更新总数据的日期
        if(WatchUtils.isEmpty(date))
            date = WatchUtils.obtainFormatDate(1);
        if(date.equals(WatchUtils.getCurrentDate())){   //今天
            isToday = true;
        }else{
            isToday = false;
        }


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


        Thread thread2 = new MyThread2();
        thread2.start();
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


    private void readSpo2Data(){
        //读取血氧的数据
        MyApp.getInstance().getVpOperateManager().readSpo2hOrigin(bleWriteResponse, new ISpo2hOriginDataListener() {
            @Override
            public void onReadOriginProgress(float v) {

            }

            @Override
            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {

            }

            @Override
            public void onSpo2hOriginListener(Spo2hOriginData spo2hOriginData) {
                if(spo2hOriginData == null)
                    return;
                // Log.e(TAG,"---血氧---日期="+spo2hOriginData.toString());
                Message message = handler.obtainMessage();
                message.what = 1002;
                message.obj = spo2hOriginData;
                handler.sendMessage(message);

            }

            @Override
            public void onReadOriginComplete() {
                Intent intent = new Intent();
                intent.setAction(WatchUtils.B31_SPO2_COMPLETE);
                sendBroadcast(intent);
            }
        }, isToday ? 1 : 3);
    }



    private void readDeviceData() {
        //Log.e(TAG,"------isToday="+isToday);
        MyApp.getInstance().getVpOperateManager().readHRVOrigin(bleWriteResponse, new IHRVOriginDataListener() {
            @Override
            public void onReadOriginProgress(float v) {

            }

            @Override
            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {

            }

            @Override
            public void onHRVOriginListener(HRVOriginData hrvOriginData) {
                //Log.e(TAG,"----------读取HRV数据="+hrvOriginData.toString());
                Message message = handler.obtainMessage();
                message.what = 1001;
                message.obj = hrvOriginData;
                handler.sendMessage(message);

            }

            @Override
            public void onDayHrvScore(int i, String s, int i1) {

            }

            @Override
            public void onReadOriginComplete() {
                Intent intent = new Intent();
                intent.setAction(WatchUtils.B31_HRV_COMPLETE);
                sendBroadcast(intent);
            }
        }, isToday ? 1 : 3);

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
