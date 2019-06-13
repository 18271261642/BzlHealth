package com.bozlun.health.android.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.commdbserver.CommSleepDb;
import com.bozlun.health.android.commdbserver.SyncDbUrls;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.bozlun.health.android.w30s.views.W30S_SleepChart;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suchengkeji.android.w30sblelibrary.bean.servicebean.W30S_SleepDataItem;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Admin
 * Date 2019/5/17
 */
public class NewFriendSleepActivity extends WatchBaseActivity implements RequestView {

    private static final String TAG = "NewFriendSleepActivity";


    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.detailSleepQuitRatingBar)
    RatingBar detailSleepQuitRatingBar;
    @BindView(R.id.detailCusSleepView)
    W30S_SleepChart detailCusSleepView;
    @BindView(R.id.text_sleep_nodata)
    TextView textSleepNodata;
    @BindView(R.id.detailAllSleepTv)
    TextView detailAllSleepTv;
    @BindView(R.id.detailAwakeNumTv)
    TextView detailAwakeNumTv;
    @BindView(R.id.detailStartSleepTv)
    TextView detailStartSleepTv;
    @BindView(R.id.detailAwakeTimeTv)
    TextView detailAwakeTimeTv;
    @BindView(R.id.detailDeepTv)
    TextView detailDeepTv;
    @BindView(R.id.detailHightSleepTv)
    TextView detailHightSleepTv;
    @BindView(R.id.sleepCurrDateTv)
    TextView sleepCurrDateTv;




    private RequestPressent requestPressent;
    private String currDay = WatchUtils.getCurrentDate();
    //好友的id
    private String applicant = "";
    //好友的设备地址
    private String friendBleMac = "";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frend_sleep_activity);
        ButterKnife.bind(this);


        initViews();
        Intent intent = getIntent();
        applicant = intent.getStringExtra("applicant");
        friendBleMac = intent.getStringExtra("friendBleMac");
        initData();


    }

    private void initViews() {
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.sleep));
//        commentB30ShareImg.setVisibility(View.VISIBLE);






    }

    private void initData() {
        sleepCurrDateTv.setText(currDay);
        findFrendStepItem(currDay);
    }

    /**
     * 查询好友日 睡眠详细数据
     */
    public void findFrendStepItem(String rtc) {
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
        String sleepUrl = URLs.HTTPs + Commont.FrendSLeepToDayData;
        JSONObject sleepJson = new JSONObject();
        try {

            if (!WatchUtils.isEmpty(userId)) sleepJson.put("userId", userId);
            if (!WatchUtils.isEmpty(applicant)) sleepJson.put("applicant", applicant);
            sleepJson.put("rtc", WatchUtils.obtainAroundDate(rtc,true));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        //获取汇总的睡眠数据，总睡眠时长等
        Map<String, String> map = new HashMap<>();
        map.put("userId", applicant+"");
        map.put("startDate", WatchUtils.obtainAroundDate(rtc,true,0));
        map.put("endDate", WatchUtils.obtainAroundDate(rtc,true,0));
        map.put("deviceCode", friendBleMac+"");
        String commParams = new Gson().toJson(map);


        if (requestPressent != null) {
            //获取睡眠详细信息
            requestPressent.getRequestJSONObject(0x01, sleepUrl, NewFriendSleepActivity.this, sleepJson.toString(), 0);
            //获取汇总的睡眠信息
            requestPressent.getRequestJSONObject(0x02, SyncDbUrls.downloadSleepUrl(),NewFriendSleepActivity.this,commParams,1);
        }
    }




    @OnClick({R.id.commentB30BackImg, R.id.sleepCurrDateLeft,
            R.id.sleepCurrDateRight})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg: //返回
                finish();
                break;
            case R.id.sleepCurrDateLeft:    //前一天
                changeDayData(true);
                break;
            case R.id.sleepCurrDateRight:   //后一天
                changeDayData(false);
                break;
        }
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
        initData();
    }




    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog(getResources().getString(R.string.dlog));
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        closeLoadingDialog();
        if(WatchUtils.isEmpty(object+""))
            return;
        if(what == 0x01){   //详细睡眠
            analysisDetailSleepData(object);
        }else if(what == 0x02){ //汇总睡眠
            Log.e(TAG,"----------汇总睡眠="+object.toString());
            analysisCountSleep(object);
        }
    }




    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }

    //解析汇总睡眠
    private void analysisCountSleep(Object object) {
        try {
            JSONObject jsonObject = new JSONObject(object.toString());
            String dayStr = jsonObject.getString("day");
            List<CommSleepDb> commSleepDbList = new Gson().fromJson(dayStr,new TypeToken<List<CommSleepDb>>(){}.getType());
            if(commSleepDbList == null || commSleepDbList.isEmpty()){
                setNoDataShow();
                return;
            }

            CommSleepDb commSleepDb = commSleepDbList.get(0);
            //总睡眠时长
            int allSleepL = commSleepDb.getSleeplen();
            String re = (allSleepL / 60) + "H" +
                    (allSleepL % 60) + "m";
            detailAllSleepTv.setText(re);

            //苏醒次数
            detailAwakeNumTv.setText(commSleepDb.getWakecount()+"");
            //入睡时间
            detailStartSleepTv.setText(commSleepDb.getSleeptime());
            //清醒时间
            detailAwakeTimeTv.setText(commSleepDb.getWaketime());
            //深度睡眠
            String deepSL = commSleepDb.getDeepsleep()/60+"H"+commSleepDb.getDeepsleep()%60+"m";
            detailDeepTv.setText(deepSL);
            //浅睡
            String lowSL = commSleepDb.getShallowsleep()/60+"H"+commSleepDb.getShallowsleep()%60+"m";
            detailHightSleepTv.setText(lowSL);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //解析详细睡眠数据
    private void analysisDetailSleepData(Object object) {
        try {
            JSONObject jsonObject = new JSONObject(object.toString());
            String resultCode = jsonObject.getString("resultCode");
            if(!WatchUtils.isEmpty(resultCode) && resultCode.equals("001")){
                String slListStr = jsonObject.getString("sslist");
                List<W30S_SleepDataItem> sleepList = new Gson().fromJson(slListStr,new TypeToken<List<W30S_SleepDataItem>>(){}.getType());
                if(sleepList != null && !sleepList.isEmpty()){
                    detailCusSleepView.setBeanList(sleepList);
                }else{
                    detailCusSleepView.setBeanList(new ArrayList<W30S_SleepDataItem>());
                }

            }else{
                detailCusSleepView.setBeanList(new ArrayList<W30S_SleepDataItem>());
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }



    //无数据时显示
    private void setNoDataShow(){

        //总睡眠时长
        detailAllSleepTv.setText("--");

        //苏醒次数
        detailAwakeNumTv.setText("--");
        //入睡时间
        detailStartSleepTv.setText("--");
        //清醒时间
        detailAwakeTimeTv.setText("--");
        //深度睡眠
        detailDeepTv.setText("--");
        //浅睡
        detailHightSleepTv.setText("--");


    }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(requestPressent != null)
            requestPressent.detach();
    }



    class FriendDetailSleepDb{

        private String addTime;
        private int sleep_type;
        private String startTime;
        private String updateTime;
        private int id;
        private String deviceCode;
        private String userId;
        private String day;

        public String getAddTime() {
            return addTime;
        }

        public void setAddTime(String addTime) {
            this.addTime = addTime;
        }

        public int getSleep_type() {
            return sleep_type;
        }

        public void setSleep_type(int sleep_type) {
            this.sleep_type = sleep_type;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDeviceCode() {
            return deviceCode;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }
    }
}
