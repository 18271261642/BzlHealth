package com.bozlun.health.android.friend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.LogTestUtil;
import com.bozlun.health.android.R;
import com.bozlun.health.android.friend.bean.FrendDataBean;
import com.bozlun.health.android.friend.bean.FriendDetailHomeBean;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.suchengkeji.android.w30sblelibrary.utils.W30SBleUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrendDataActivity extends WatchBaseActivity implements RequestView {

    private static final String TAG = "FrendDataActivity";

    @BindView(R.id.toolbar_normal)
    Toolbar mNormalToolbar;
    @BindView(R.id.bar_titles)
    TextView barTitles;
    @BindView(R.id.frend_step_number)
    TextView frendStepNumber;
    @BindView(R.id.frend_step_dis)
    TextView frendStepDis;
    @BindView(R.id.frend_step_kcl)
    TextView frendStepKcl;
    @BindView(R.id.frend_slee_deep)
    TextView frendSleeDeep;
    @BindView(R.id.frend_sleep_shallow)
    TextView frendSleepShallow;
    @BindView(R.id.frend_sleep_time)
    TextView frendSleepTime;
    @BindView(R.id.frend_hrart_max)
    TextView frendHrartMax;
    @BindView(R.id.frend_heart_min)
    TextView frendHeartMin;
    @BindView(R.id.frend_hreat_average)
    TextView frendHreatAverage;


    @BindView(R.id.rela_bp)
    RelativeLayout rela_bp;
    @BindView(R.id.frend_bp_max)
    TextView frendBpMax;
    @BindView(R.id.frend_bp_min)
    TextView frendBpMin;
    @BindView(R.id.frend_bp_average)
    TextView frendBpAverage;

    private RequestPressent requestPressent;
    String applicant = "";
    String StepNumber = "0";
    private int FrendSeeToMeStep = 0;
    private int FrendSeeToMeHeart = 0;
    private int FrendSeeToMeSleep = 0;
    private int FrendSeeToMeBlood = 0;
    Intent intent = null;
    String stringJson = "";
    //好友的设备地址
    private String friendBleMac = null;


    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frend_data_activity);
        ButterKnife.bind(this);


        init();

        intent = getIntent();
        if (intent == null) return;
        applicant = intent.getStringExtra("applicant");
        StepNumber = intent.getStringExtra("stepNumber");//步数

        setBack();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(applicant)) {
            getFrendlatDdayData(applicant);
        }
    }

    private void init() {
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
        //设置标题
        barTitles.setText(getResources().getString(R.string.string_frend_datas));
        //深睡
        frendSleeDeep.setText(getResources().getString(R.string.sleep_deep) + "(HOUR)：0.0" );
        //浅睡
        frendSleepShallow.setText(getResources().getString(R.string.sleep_light) + "(HOUR)：0.0" );
        //总睡眠时长
        frendSleepTime.setText(getResources().getString(R.string.long_when) + "(HOUR)：0.0");

        //最高心率
        frendHrartMax.setText(getResources().getString(R.string.zuigaoxinlv) + "(BPM）：0" );
        //最低心率
        frendHeartMin.setText(getResources().getString(R.string.zuidixinlv) + "(BPM）：0");
        //平均心率
        frendHreatAverage.setText(getResources().getString(R.string.pinjunxin) + "(BPM）：0");

    }


    void setBack() {
        mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_close_frend));
        setSupportActionBar(mNormalToolbar);

        mNormalToolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.backs));//设置返回按钮
        mNormalToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });//右边返回按钮点击事件
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.frend_menu_visb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String userId = (String) SharedPreferencesUtils.readObject(this, "userId");
        if (id == R.id.action_new_frend_apply) {
            //屏蔽
            if (!TextUtils.isEmpty(applicant) && !TextUtils.isEmpty(userId)) {
                if (WatchUtils.isEmpty(applicant)) applicant = intent.getStringExtra("applicant");
                startActivity(FrendSettingActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
            }
            setBack();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 好友首页：昨日的睡眠，心率，步数
     *
     * @param applicant
     */
    public void getFrendlatDdayData(String applicant) {
        String sleepUrl = Commont.FRIEND_BASE_URL + Commont.FrendLastData;
        JSONObject sleepJson = new JSONObject();
        try {
            if (WatchUtils.isEmpty(applicant)) applicant = intent.getStringExtra("applicant");
            String userId = (String) SharedPreferencesUtils.readObject(this, "userId");
            if (!TextUtils.isEmpty(userId)) sleepJson.put("userId", userId);
            sleepJson.put("applicant", applicant);
            Log.e("-----------朋友--", " 好友首页：昨日的睡眠，心率，步数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x01, sleepUrl, FrendDataActivity.this, sleepJson.toString(), 0);
        }
    }


    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog(getResources().getString(R.string.dlog));
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        closeLoadingDialog();
        if (object == null || WatchUtils.isEmpty(object + "") || object.toString().contains("<html>"))
            return;
          Log.d("-----------朋友--", object.toString());
        analysisFriendDetail(object.toString());


    }



    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }


    //解析数据
    @SuppressLint("SetTextI18n")
    private void analysisFriendDetail(String str) {
        FriendDetailHomeBean friendDetailHomeBean = new Gson().fromJson(str,FriendDetailHomeBean.class);
        if(friendDetailHomeBean == null)
            return;
        if(friendDetailHomeBean.getCode() == 200){
            //步数数据
            FriendDetailHomeBean.DataBean.SportDayBean sportDayBean = friendDetailHomeBean.getData().getSportDay();
            if(sportDayBean != null){   //有数据

                //步数
                frendStepNumber.setText(getResources().getString(R.string.step) + "(STEP)：" + sportDayBean.getStepnumber());
                //距离
                DecimalFormat decimalFormat = new DecimalFormat("#.00");

                frendStepDis.setText(getResources().getString(R.string.mileage) + "(KM)：" + decimalFormat.format(sportDayBean.getDistance()) );
                //卡路里
                frendStepKcl.setText(getResources().getString(R.string.calories) + "(KCAL):" + (sportDayBean.getCalorie()==null?"0.0":sportDayBean.getCalorie())+"");

            }


            //睡眠数据
            FriendDetailHomeBean.DataBean.SleepDayBean sleepDayBean = friendDetailHomeBean.getData().getSleepDay();
            if(sleepDayBean != null){
                Log.e(TAG,"-------好友睡眠="+sleepDayBean.toString());
                int deepTimeStr = sleepDayBean.getDeepsleep();
                friendBleMac = sleepDayBean.getDevicecode();
                frendSleeDeep.setText(getResources().getString(R.string.sleep_deep) + "(HOUR)："+deepTimeStr/60 +"H"+ deepTimeStr % 60+"mine" );
                //浅睡
                int lowTimeStr = sleepDayBean.getShallowsleep();
                frendSleepShallow.setText(getResources().getString(R.string.sleep_light) + "(HOUR)：" +lowTimeStr/60+"H"+lowTimeStr % 60+"mine");
                //总睡眠时长
                int countTimeStr = sleepDayBean.getSleeplen();
                frendSleepTime.setText(getResources().getString(R.string.long_when) + "(HOUR)："+countTimeStr / 60 +"H" + countTimeStr % 60 +"mine");
            }

            //心率数据
            FriendDetailHomeBean.DataBean.HeartRateDayBean heartRateDayBean = friendDetailHomeBean.getData().getHeartRateDay();
            if(heartRateDayBean != null){
                Log.e(TAG,"-------心率数据="+heartRateDayBean.toString());
                //最高心率
                frendHrartMax.setText(getResources().getString(R.string.zuigaoxinlv) + "(BPM）：" +heartRateDayBean.getMaxheartrate());
                //最低心率
                frendHeartMin.setText(getResources().getString(R.string.zuidixinlv) + "(BPM）："+heartRateDayBean.getMinheartrate());
                //平均心率
                frendHreatAverage.setText(getResources().getString(R.string.pinjunxin) + "(BPM）："+heartRateDayBean.getAvgheartrate());


            }


            //血压数据
            FriendDetailHomeBean.DataBean.BloodPressureDayBean bloodPressureDayBean = friendDetailHomeBean.getData().getBloodPressureDay();
            if(bloodPressureDayBean != null){
                Log.e(TAG,"-------血压数据="+bloodPressureDayBean.toString());
                frendBpMax.setText(getResources().getString(R.string.string_systolic) + "(mmHg)："+bloodPressureDayBean.getAvgsystolic());
                frendBpMin.setText(getResources().getString(R.string.string_diastolic) + "(mmHg)："+bloodPressureDayBean.getAvgdiastolic());
                frendBpAverage.setText("参考结果");
            }


        }


    }


    @OnClick({R.id.rela_step, R.id.rela_sleep, R.id.rela_heart, R.id.rela_bp})
    public void onViewClicked(View view) {
        if (WatchUtils.isEmpty(applicant)) applicant = intent.getStringExtra("applicant");
        switch (view.getId()) {
            case R.id.rela_step:
                Log.d("-------AA-", "FrendSeeToMeStep:" + FrendSeeToMeStep + "");
                //if (FrendSeeToMeStep == 0) return;
                startActivity(FrendStepActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
            case R.id.rela_sleep:
                Log.d("-------AA-", "FrendSeeToMeSleep:" + FrendSeeToMeSleep + "");
                //if (FrendSeeToMeSleep == 0) return;
                startActivity(NewFriendSleepActivity.class, new String[]{"applicant", "friendBleMac"},
                        new String[]{applicant, friendBleMac});
                break;
            case R.id.rela_heart:
                Log.d("-------AA-", "FrendSeeToMeHeart:" + FrendSeeToMeHeart + "");
                //if (FrendSeeToMeHeart == 0) return;
                startActivity(FrendHeartActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
            case R.id.rela_bp:
                //if (FrendSeeToMeBlood == 0) return;
                startActivity(NewFriendBpActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
        }

    }
}
