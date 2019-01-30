package com.bozlun.health.android.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.B30HomeActivity;
import com.bozlun.health.android.b31.B31HomeActivity;
import com.bozlun.health.android.b31.model.B31HRVBean;
import com.bozlun.health.android.h9.H9HomeActivity;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.h8.H8HomeActivity;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.w30s.W30SHomeActivity;
import com.suchengkeji.android.w30sblelibrary.W30SBLEManage;
import com.veepoo.protocol.model.datas.Spo2hOriginData;
import com.veepoo.protocol.model.datas.TimeData;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 启动页
 */
public class LaunchActivity extends WatchBaseActivity {

    private static final String TAG = "LaunchActivity";

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
//                    boolean isGuid = (boolean) msg.obj;
//                    Log.e(TAG,"---isGuid="+isGuid);
//                    if(isGuid){
//                        switchLoginUser();
//                    }else{
//                        startActivity(NewGuidActivity.class);
//                        finish();
//                    }


                    switchLoginUser();
                   // finish();

                    break;

            }

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_layout);


        initData();


        //final boolean isGuide = (boolean) SharedPreferencesUtils.getParam(LaunchActivity.this,"isGuide",false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1001);
//                Message message = handler.obtainMessage();
//                message.what = 1001;
//                message.obj = isGuide;
//                handler.sendMessage(message);
            }
        }, 3 * 1000);

    }

    @Override
    public void hideTitleStute() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private void initData() {

        //B30目标步数 默认8000
        int goalStep = (int) SharedPreferencesUtils.getParam(MyApp.getContext(),"b30Goal",0);
        if(goalStep==0){
            SharedPreferencesUtils.setParam(MyApp.getContext(),"b30Goal",8000);
        }
        String b30SleepGoal = (String) SharedPreferencesUtils.getParam(MyApp.getContext(),"b30SleepGoal","");
        if(WatchUtils.isEmpty(b30SleepGoal)){
            SharedPreferencesUtils.setParam(MyApp.getContext(),"b30SleepGoal","8.0");
        }
        //B30的默认密码
        String b30Pwd = (String) SharedPreferencesUtils.getParam(MyApp.getContext(),Commont.DEVICESCODE,"");
        if(WatchUtils.isEmpty(b30Pwd)){
            SharedPreferencesUtils.setParam(MyApp.getContext(),Commont.DEVICESCODE,"0000");
        }

        SharedPreferencesUtils.setParam(LaunchActivity.this, "w30sunit", true);

        //H8默认目标步数
        String defaultTagStep = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), "settagsteps", "");
        if(WatchUtils.isEmpty(defaultTagStep))
            SharedPreferencesUtils.setParam(MyApp.getContext(), "settagsteps", "10000");

        //H8的公英制，默认公制
        String h8Unit = (String) SharedPreferencesUtils.getParam(LaunchActivity.this,Commont.H8_UNIT,"km");
        if(WatchUtils.isEmpty(h8Unit))
            SharedPreferencesUtils.setParam(LaunchActivity.this,Commont.H8_UNIT,"km");


        //H8手表初始化消息提醒开关
        String isFirst = (String) SharedPreferencesUtils.getParam(LaunchActivity.this, "msgfirst", "");
        if (WatchUtils.isEmpty(isFirst)) {
            SharedPreferencesUtils.setParam(LaunchActivity.this, "msgfirst", "isfirst");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "weixinmsg", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "msg", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "qqmsg", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "Viber", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "Twitteraa", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "facebook", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "Whatsapp", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "Instagrambutton", "1");
            SharedPreferencesUtils.saveObject(LaunchActivity.this, "laidian", "0");
            SharedPreferencesUtils.setParam(MyApp.getContext(), "laidianphone", "off");
        }



    }

    //判断进入的页面
    private void switchLoginUser() {
        String userId = (String) SharedPreferencesUtils.readObject(LaunchActivity.this, "userId");
        //判断有没有登录
        if (!WatchUtils.isEmpty(userId)) {
            Log.e("GuideActivity", "--------蓝牙---" + SharedPreferencesUtils.readObject(LaunchActivity.this,  Commont.BLENAME));
            String btooth = (String) SharedPreferencesUtils.readObject(LaunchActivity.this,  Commont.BLENAME);
            if (!WatchUtils.isEmpty(btooth) ) {
                if ("bozlun".equals(btooth)) {    //H8 手表
                    MyApp.getInstance().h8BleManagerInstance();
                    //handler.sendEmptyMessage(1002);
                    startActivity(new Intent(LaunchActivity.this, H8HomeActivity.class));
                } else if ("H9".equals(btooth)) {   // H9 手表
                    startActivity(new Intent(LaunchActivity.this, H9HomeActivity.class));
                } else if ("W30".equals(btooth) || "w30".equals(btooth)) {
                    W30SBLEManage instance = W30SBLEManage.getInstance(MyApp.getContext());
                    MyApp.getInstance().setmW30SBLEManage(instance);
                    MyApp.getInstance().getmW30SBLEManage().openW30SBLEServices();
                    startActivity(new Intent(LaunchActivity.this, W30SHomeActivity.class));
                } else if ("B30".equals(btooth) || "B36".equals(btooth)) {      //B30和B36
                    startActivity(new Intent(LaunchActivity.this, B30HomeActivity.class));
                }else if("Ringmii".equals(btooth)){
                    startActivity(new Intent(LaunchActivity.this, B30HomeActivity.class));
                } else if("B31".equals(btooth)){
                    startActivity(new Intent(LaunchActivity.this,B31HomeActivity.class));
                }

                else {
                    startActivity(new Intent(LaunchActivity.this, NewSearchActivity.class));
                }
            } else {
                startActivity(new Intent(LaunchActivity.this, NewSearchActivity.class));
            }
            finish();
        } else {
//            startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
            startActivity(new Intent(LaunchActivity.this, NewLoginActivity.class));
        }
    }
}
