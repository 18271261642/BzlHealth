package com.bozlun.health.android.b31;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.b30view.CustomCircleProgressBar;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.ISpo2hDataListener;
import com.veepoo.protocol.model.datas.Spo2hData;
import com.veepoo.protocol.model.enums.EDeviceStatus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * B31血氧测试
 * Created by Admin
 * Date 2018/12/18
 */
public class B31ManSpO2Activity extends WatchBaseActivity {

    private static final String TAG = "B31ManSpO2Activity";


    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commentB30ShareImg)
    ImageView commentB30ShareImg;
    @BindView(R.id.b31MeaureSpo2ProgressView)
    CustomCircleProgressBar b31MeaureSpo2ProgressView;
    @BindView(R.id.b31MeaureStartImg)
    ImageView b31MeaureStartImg;
    @BindView(R.id.showSpo2ResultTv)
    TextView showSpo2ResultTv;
    @BindView(R.id.spo2ShowGifImg)
    ImageView spo2ShowGifImg;


    //开始或者停止测量的标识
    private boolean isStart = false;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("CheckResult")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                Spo2hData spo2hData = (Spo2hData) msg.obj;
                if (spo2hData == null)
                    return;
                if(spo2hData.getDeviceState() != EDeviceStatus.FREE){
                    showSpo2ResultTv.setText(WatchUtils.setBusyDesicStr());
                    isStart = true;
                    startOrStopManSpo2();
                    return;
                }

               try {
                   if (spo2hData.getCheckingProgress() == 0x00 && !spo2hData.isChecking()) {
                       b31MeaureSpo2ProgressView.setTmpTxt(spo2hData.getValue() + "%");
                       showSpo2ResultTv.setText(verSpo2Status(spo2hData.getValue()));
                       b31MeaureSpo2ProgressView.setOxyDexcStr("血氧浓度");
                       Log.e(TAG,"----------进度="+spo2hData.getCheckingProgress()+"%");
                       RequestOptions options = new RequestOptions()
                               .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                       Glide.with(B31ManSpO2Activity.this).asGif().load(R.drawable.spgif).apply(options).into(spo2ShowGifImg);


                   }
               }catch (Exception e){
                    e.printStackTrace();
               }

            }

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b31_man_spo2);
        ButterKnife.bind(this);

        initViews();


    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.spo2_test));
        //圆环背景色
        b31MeaureSpo2ProgressView.setInsideColor(Color.parseColor("#72CBEE"));
        //进度色
        b31MeaureSpo2ProgressView.setOutsideColor(Color.WHITE);
        spo2ShowGifImg.setImageResource(R.drawable.spgif);
        b31MeaureSpo2ProgressView.setOxyDexcStr(getResources().getString(R.string.spo2_calibration_pro));
        b31MeaureSpo2ProgressView.setOxyCh(true);

    }

    @OnClick({R.id.commentB30BackImg, R.id.commentB30ShareImg,
            R.id.b31MeaureStartImg})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:    //返回
                finish();
                break;
            case R.id.commentB30ShareImg:   //分享
                WatchUtils.shareCommData(B31ManSpO2Activity.this);
                break;
            case R.id.b31MeaureStartImg:    //开始或停止测量
                if (MyCommandManager.DEVICENAME == null) {
                    showSpo2ResultTv.setText(getResources().getString(R.string.string_devices_disconnected));
                    return;
                }
                startOrStopManSpo2();
                break;
        }
    }

    private void startOrStopManSpo2() {
        if (!isStart) {   //开始测量
            isStart = true;
            b31MeaureSpo2ProgressView.setTmpTxt(null);
            b31MeaureStartImg.setImageResource(R.drawable.detect_sp_stop);
            b31MeaureSpo2ProgressView.stopAnim();
            b31MeaureSpo2ProgressView.setScheduleDuring(4 * 1000);
            b31MeaureSpo2ProgressView.setProgress(100);
            MyApp.getInstance().getVpOperateManager().startDetectSPO2H(iBleWriteResponse, new ISpo2hDataListener() {
                @Override
                public void onSpO2HADataChange(Spo2hData spo2hData) {
                    Log.e(TAG, "----------spo2hData=" + spo2hData.toString());
                    Message message = handler.obtainMessage();
                    message.what = 1001;
                    message.obj = spo2hData;
                    handler.sendMessage(message);
                }
            });

        } else {  //停止测量
            isStart = false;
            Glide.with(B31ManSpO2Activity.this).clear(spo2ShowGifImg);
            spo2ShowGifImg.setImageResource(R.drawable.spgif);
            b31MeaureStartImg.setImageResource(R.drawable.detect_sp_start);
            b31MeaureSpo2ProgressView.setOxyDexcStr(getResources().getString(R.string.spo2_calibration_pro));
            b31MeaureSpo2ProgressView.stopAnim(0);
            MyApp.getInstance().getVpOperateManager().stopDetectSPO2H(iBleWriteResponse, new ISpo2hDataListener() {
                @Override
                public void onSpO2HADataChange(Spo2hData spo2hData) {

                }
            });
           // b31MeaureSpo2ProgressView.setTmpTxt("0%");

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isStart){
            MyApp.getInstance().getVpOperateManager().stopDetectSPO2H(iBleWriteResponse, new ISpo2hDataListener() {
                @Override
                public void onSpO2HADataChange(Spo2hData spo2hData) {

                }
            });
        }

    }

    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };


    //判断血氧浓度是否正常
    //范围是[0-99], [0-79]=血氧远低于正常值，警告用户要重视,[80-89]=血氧浓度低，提醒用户重视，
    // [90-95]=血氧浓度偏低,[95-99]=血氧正常
    private String verSpo2Status(int spo2V){

        if(spo2V >= 95 && spo2V <=99){
            return "血氧浓度正常";
        }else if(spo2V >= 90 && spo2V <= 95){
            return "血氧浓度偏低";
        }else if(spo2V >= 80 && spo2V <= 89){
            return "血氧浓度低，请重视!";
        }else if(spo2V <= 79){
            return "警告！血氧远低于正常值！";
        }else{
            return null;
        }
    }
}
