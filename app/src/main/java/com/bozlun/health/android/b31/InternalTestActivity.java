package com.bozlun.health.android.b31;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.google.gson.Gson;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IAllHealthDataListener;
import com.veepoo.protocol.listener.data.ISleepDataListener;
import com.veepoo.protocol.model.datas.OriginData;
import com.veepoo.protocol.model.datas.OriginHalfHourData;
import com.veepoo.protocol.model.datas.SleepData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin
 * Date 2019/3/12
 */
public class InternalTestActivity extends WatchBaseActivity {

    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commArrowDate)
    TextView commArrowDate;
    @BindView(R.id.showDeviceSleepTv)
    TextView showDeviceSleepTv;

    private Gson gson = new Gson();
    private String currDay = WatchUtils.getCurrentDate();

    private int code = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_layout);
        ButterKnife.bind(this);

        initViews();


    }

    private void initViews() {
        commArrowDate.setText(currDay);
        commentB30TitleTv.setText("内部测试用");
        commentB30BackImg.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.commentB30BackImg, R.id.commArrowLeft,
            R.id.commArrowRight, R.id.deviceSleepBtn,
            R.id.sqlSleepBtn,R.id.clearSleepBtn,
            R.id.readDBHeartBtn,R.id.readDBBpBtn,R.id.readDeviceAllSportBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:
                finish();
                break;
            case R.id.commArrowLeft:
                changeDayData(true);
                break;
            case R.id.commArrowRight:
                changeDayData(false);
                break;
            case R.id.clearSleepBtn:
                showDeviceSleepTv.setText("");
                break;
            case R.id.deviceSleepBtn:
                if(MyCommandManager.DEVICENAME == null){
                    showDeviceSleepTv.setText("设备未连接");
                    return;
                }

                readSleepData();
                break;
            case R.id.sqlSleepBtn:  //从数据库中读取睡眠数据
                code = 0;
                findBySql(0);
                break;
            case R.id.readDeviceAllSportBtn:   //从设备中读取所有的健康数据
                readAllDeviceData();
                break;
            case R.id.readDBHeartBtn:   //从数据库中读取心率数据
                code = 1;
                findBySql(1);
                break;
            case R.id.readDBBpBtn:  //从数据库中读取血压数据
                code = 2;
                findBySql(2);
                break;
        }
    }

    private void readAllDeviceData() {
        if(MyCommandManager.DEVICENAME == null)
            return;
        showDeviceSleepTv.setText("数据读取中...");
        final StringBuilder stringBuilder = new StringBuilder();
        MyApp.getInstance().getVpOperateManager().readAllHealthData(new IAllHealthDataListener() {
            @Override
            public void onProgress(float v) {

            }

            @Override
            public void onSleepDataChange(SleepData sleepData) {

            }

            @Override
            public void onReadSleepComplete() {

            }

            @Override
            public void onOringinFiveMinuteDataChange(OriginData originData) {

            }

            //半小时的健康数据
            @Override
            public void onOringinHalfHourDataChange(OriginHalfHourData originHalfHourData) {
               stringBuilder.append("步数 :"+originHalfHourData.getHalfHourSportDatas().toString()+"\n"+"心率："+originHalfHourData.getHalfHourRateDatas().toString()+"\n"+"血压:"+originHalfHourData.getHalfHourBps().toString());

            }

            @Override
            public void onReadOriginComplete() {
                showDeviceSleepTv.setText("所有健康数据："+stringBuilder.toString());
            }
        },4 );



    }


    /**
     * 根据日期切换数据
     */
    private void changeDayData(boolean left) {
        String date = WatchUtils.obtainAroundDate(currDay, left);
        if (date.equals(currDay) || date.isEmpty()) {
            return;// 空数据,或者大于今天的数据就别切了
        }
        currDay = date;
        findBySql(code);
    }

    private void findBySql(int code) {
        commArrowDate.setText(currDay);
        String mac = MyApp.getInstance().getMacAddress();
        if(WatchUtils.isEmpty(mac))
            return;
        switch (code){
            case 0x00:  //数据库中读取睡眠
                String sleep = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                        .TYPE_SLEEP);
                SleepData sleepData = gson.fromJson(sleep, SleepData.class);
                showDeviceSleepTv.setText(sleepData==null?"数据为空":("数据库中的睡眠："+sleepData.toString()));
                break;
            case 0x01:  //数据库中读取心率
                String heartStr = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                        .TYPE_RATE);
//                List<HalfHourRateData> rateData = gson.fromJson(heartStr, new TypeToken<List<HalfHourRateData>>() {
//                }.getType());
                showDeviceSleepTv.setText(heartStr==null?"数据为空":("数据库中的心率数据："+heartStr));
                break;
            case 0x02:  //数据库中读取血压
                String bpStr = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                        .TYPE_BP);
//                List<HalfHourBpData> bpData = gson.fromJson(bpStr, new TypeToken<List<HalfHourBpData>>() {
//                }.getType());
                showDeviceSleepTv.setText(bpStr==null?"数据为空":("数据库中的血压数据："+bpStr));
                break;
        }


    }


    private void readSleepData(){
        final StringBuilder stringBuilder = new StringBuilder();
        MyApp.getInstance().getVpOperateManager().readSleepData(iBleWriteResponse, new ISleepDataListener() {
            @Override
            public void onSleepDataChange(SleepData sleepData) {
                Log.e("内测","---------睡眠="+sleepData.toString());
                stringBuilder.append("日期："+gson.toJson(sleepData)+"====");

            }

            @Override
            public void onSleepProgress(float v) {

            }

            @Override
            public void onSleepProgressDetail(String s, int i) {

            }

            @Override
            public void onReadSleepComplete() {
                Log.e("内测","--------设备中的睡眠数据="+stringBuilder);
                showDeviceSleepTv.setText(stringBuilder);
            }
        }, 4);
    }

    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };
}
