package com.bozlun.health.android.friend;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.utils.W30SBleUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrendDataActivity
        extends WatchBaseActivity
        implements RequestView {
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
//        if (see.equals("1")) {
//            //替换三个点
//            mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_close_frend));
//            setSupportActionBar(mNormalToolbar);
//        } else {
//            //替换三个点
//            mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_open_frend));
//            setSupportActionBar(mNormalToolbar);
//        }
//        String stepNumber = intent.getStringExtra("stepNumber");//步数
//        String frendHeight = intent.getStringExtra("frendHeight");
//        if (WatchUtils.isEmpty(frendHeight)) frendHeight = "170cm";
//        int height = Integer.valueOf(frendHeight.substring(0, frendHeight.length() - 2).trim());//用户身高
//        if (!TextUtils.isEmpty(stepNumber)) {
//            frendStepNumber.setText("步数(STEP)：" + stepNumber);
//            double distants = WatchUtils.getDistants(Integer.valueOf(stepNumber), height);//计算距离
//            double kcal = WatchUtils.getKcal(Integer.valueOf(stepNumber), height);//计算卡路里
//            frendStepDis.setText("距离(KM)：1.25" + distants);
//            frendStepKcl.setText("卡路里(KCAL):43" + kcal);
//        }

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

//        if (id == R.id.action_add_new_frend) {
//            //展示
//            if (!TextUtils.isEmpty(applicant) && !TextUtils.isEmpty(userId)) {
//                informationIsVisbFrend(userId, applicant, 1);
//                //替换三个点
//                mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_close_frend));
//                setSupportActionBar(mNormalToolbar);
//            }
//
//            setBack();
//            return true;
//        }
//        if (id == R.id.action_new_frend_apply) {
//            //屏蔽
//            if (!TextUtils.isEmpty(applicant) && !TextUtils.isEmpty(userId)) {
//                informationIsVisbFrend(userId, applicant, 0);
//                //替换三个点
//                mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_open_frend));
//                setSupportActionBar(mNormalToolbar);
//            }
//            setBack();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


//    /**
//     * 详细资料是否对好友可见
//     *
//     * @param userId
//     */
//    public void informationIsVisbFrend(String userId, String applicant, int see) {
//        String sleepUrl = URLs.HTTPs + Commont.FrendDetailedIsVis;
//        JSONObject sleepJson = new JSONObject();
//        try {
//            sleepJson.put("userId", userId);
//            sleepJson.put("applicant", applicant);
//            sleepJson.put("see", see);
//            Log.d("-----------朋友--", " 设置详细资料是否对好友可见参数--" + sleepJson.toString());
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//
//        if (requestPressent != null) {
//            requestPressent.getRequestJSONObject(0x02, sleepUrl, this, sleepJson.toString(), 0);
//        }
//    }

    /**
     * 好友首页：昨日的睡眠，心率，步数
     *
     * @param applicant
     */
    public void getFrendlatDdayData(String applicant) {
        String sleepUrl = URLs.HTTPs + Commont.FrendLastData;
        JSONObject sleepJson = new JSONObject();
        try {
            if (WatchUtils.isEmpty(applicant)) applicant = intent.getStringExtra("applicant");
            String userId = (String) SharedPreferencesUtils.readObject(this, "userId");
            if (!TextUtils.isEmpty(userId)) sleepJson.put("userId", userId);
            sleepJson.put("applicant", applicant);
            Log.d("-----------朋友--", " 好友首页：昨日的睡眠，心率，步数--" + sleepJson.toString());
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
        Message message = new Message();
        message.what = what;
        message.obj = object;
        if (handler != null) handler.sendMessage(message);
    }

    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }


    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0x01:
                    LogTestUtil.e("----------好友首页数据--", message.obj.toString());
                    FrendDataBean frendDataBean = new Gson().fromJson(message.obj.toString(), FrendDataBean.class);
                    if (frendDataBean != null) {

                        String resultCode = frendDataBean.getResultCode();
                        if (Commont.ReturnCode(resultCode)) {
                            /**
                             * 步数
                             */
                            int user_height = 170;
                            int user_weight = 65;
                            FrendDataBean.FriendInfoBean infoBean = frendDataBean.getFriendInfo();
                            if (infoBean != null) {
                                String weight = infoBean.getWeight().trim().replace(" ", "");
                                String height = infoBean.getHeight().trim().replace(" ", "");
                                if (weight.contains("kg")) {
                                    user_weight = Integer.valueOf(weight.trim().substring(0, weight.trim().length() - 2));
                                } else {
                                    user_weight = Integer.valueOf(weight.trim());
                                }
                                if (height.contains("cm")) {
                                    user_height = Integer.valueOf(height.trim().substring(0, height.trim().length() - 2));
                                } else {
                                    user_height = Integer.valueOf(height.trim());
                                }
                            }

                            int stepNumber = Integer.valueOf(StepNumber.trim());
//                                String distance = deviceDataMovement.getDistance();
                            if (frendStepNumber != null)
                                frendStepNumber.setText(getResources().getString(R.string.step) + "(STEP)：" + stepNumber);
                            if (frendStepDis != null) {
                                float distance = W30SBleUtils.getDistance((float) user_height, stepNumber);
//                                    String format = df.format(Double.valueOf(distance));
                                BigDecimal setScale = new BigDecimal((distance / 1000)).setScale(1, BigDecimal.ROUND_DOWN);
                                frendStepDis.setText(getResources().getString(R.string.mileage) + "(KM)：" + setScale);
                            }
                            if (frendStepKcl != null) {
                                float calory = W30SBleUtils.getCalory((float) user_height, (float) user_weight, stepNumber);
//                                    String format = df.format(Double.valueOf(calories));
                                BigDecimal setScale = new BigDecimal(calory).setScale(1, BigDecimal.ROUND_DOWN);
                                frendStepKcl.setText(getResources().getString(R.string.calories) + "(KCAL):" + setScale);
                            }
//                            FrendDataBean.DeviceDataMovementBean deviceDataMovement = frendDataBean.getDeviceDataMovement();
//                            if (deviceDataMovement != null) {
////                                DecimalFormat df = new DecimalFormat("0.0");
////                                int calories = deviceDataMovement.getCalories();
////                                int stepNumber = deviceDataMovement.getStepNumber();
//                                int stepNumber = Integer.valueOf(StepNumber.trim());
////                                String distance = deviceDataMovement.getDistance();
//                                if (frendStepNumber != null)
//                                    frendStepNumber.setText("步数(STEP)：" + stepNumber);
//                                if (frendStepDis != null) {
//                                    float distance = W30SBleUtils.getDistance((float) user_height, stepNumber);
////                                    String format = df.format(Double.valueOf(distance));
//                                    BigDecimal setScale = new BigDecimal(distance).setScale(1, BigDecimal.ROUND_DOWN);
//                                    frendStepDis.setText("距离(KM)：" + setScale);
//                                }
//                                if (frendStepKcl != null) {
//                                    float calory = W30SBleUtils.getCalory((float) user_height, (float) user_weight, stepNumber);
////                                    String format = df.format(Double.valueOf(calories));
//                                    BigDecimal setScale = new BigDecimal(calory).setScale(1, BigDecimal.ROUND_DOWN);
//                                    frendStepKcl.setText("卡路里(KCAL):" + setScale);
//                                }
//                            }
                            /**
                             * 睡眠
                             */
                            FrendDataBean.SleepDayBean sleepDay = frendDataBean.getSleepDay();
                            if (sleepDay != null) {
                                stringJson = new Gson().toJson(sleepDay);
                                int deepSleep = sleepDay.getDeepSleep();
                                int shallowSleep = sleepDay.getShallowSleep();
                                int soberLen = sleepDay.getSoberLen();
                                int sleepLen = sleepDay.getSleepLen();
                                //int sleepLen = sleepDay.getSleepLen();
                                if (frendSleeDeep != null) {
                                    double deepSleeps = (double) deepSleep / 60.0;
//                                    BigDecimal setScale = new BigDecimal(deepSleeps).setScale(1, BigDecimal.ROUND_DOWN);
                                    BigDecimal setScale = new BigDecimal(deepSleeps).setScale(1, BigDecimal.ROUND_DOWN);
                                    frendSleeDeep.setText(getResources().getString(R.string.sleep_deep) + "(HOUR)：" + setScale.floatValue() + "");
                                }
                                if (frendSleepShallow != null) {
                                    double shallowSleeps = (double) shallowSleep / 60.0;
                                    BigDecimal setScale = new BigDecimal(shallowSleeps).setScale(1, BigDecimal.ROUND_DOWN);
                                    frendSleepShallow.setText(getResources().getString(R.string.sleep_light) + "(HOUR)：" + setScale.floatValue() + "");
                                }

                                DecimalFormat df = new DecimalFormat("0.0");
                                if (frendSleepTime != null) {
                                    double shallowSleeps = (double) (shallowSleep + deepSleep) / 60.0;
//                                    double shallowSleeps = (double) (sleepLen) / 60.0;
                                    BigDecimal setScaleS = new BigDecimal(shallowSleeps).setScale(1, BigDecimal.ROUND_DOWN);
//                                    String format = df.format((setScaleD.floatValue() + setScaleS.floatValue()));
                                    frendSleepTime.setText(getResources().getString(R.string.long_when) + "(HOUR)：" + setScaleS.floatValue() + "");
                                }
                            }

                            /**
                             * 心率
                             */
                            FrendDataBean.HeartRateDayBean heartRateDay = frendDataBean.getHeartRateDay();
                            if (heartRateDay != null) {
                                int maxHeartRate = heartRateDay.getMaxHeartRate();
                                int minHeartRate = heartRateDay.getMinHeartRate();
                                int avgHeartRate = heartRateDay.getAvgHeartRate();
                                if (frendHrartMax != null)
                                    frendHrartMax.setText(getResources().getString(R.string.zuigaoxinlv) + "(BPM）：" + maxHeartRate + "");
                                if (frendHeartMin != null)
                                    frendHeartMin.setText(getResources().getString(R.string.zuidixinlv) + "(BPM）：" + minHeartRate + "");
                                if (frendHreatAverage != null)
                                    frendHreatAverage.setText(getResources().getString(R.string.pinjunxin) + "(BPM）：" + avgHeartRate + "");
                            }

                            /**
                             * 血压
                             */
                            FrendDataBean.BloodPressureDayBean bloodPressureDay = frendDataBean.getBloodPressureDay();
                            if (bloodPressureDay != null) {
//                                int systolic = bloodPressureDay.getAvgSystolic();//收缩压
//                                int diastolic = bloodPressureDay.getAvgDiastolic();//舒张压

                                int systolic = bloodPressureDay.getAvgDiastolic();//收缩压
                                int diastolic = bloodPressureDay.getAvgSystolic();//舒张压

                                if (frendBpMax != null)
                                    frendBpMax.setText(getResources().getString(R.string.string_systolic) + "(mmHg)：" + systolic);
                                if (frendBpMin != null) {
                                    frendBpMin.setText(getResources().getString(R.string.string_diastolic) + "(mmHg)：" + diastolic);
                                }
                                if (frendBpAverage != null) {
                                    /**
                                     * 收缩压（systolic）（高值）   舒张压（diastolic）（低值）
                                     *
                                     * 低血压  < 90    < 60
                                     * 理想血压  < 120    < 80
                                     * 正常血压  120~139    80~89
                                     *
                                     * 高血压  >=140    >=90
                                     *    轻度高血压  140~159    90~99
                                     *    中度高血压  160~179    100~109
                                     *    高度高血压  >=180    >=110
                                     *  单纯收缩期高血压  >=140   < 90
                                     *
                                     *
                                     *  此对照表为一般对照表，若有其他疾病，如糖尿病、肾病等，
                                     *  以临床医生建议为准；50岁以后或者有心脑血管病史的人，
                                     *  建议每天按时测量血压并记录，应做到心中有数，
                                     *  有效管控自己的血压，把高血压这个慢性病的危害程度降到最低。
                                     */
                                    if (systolic < 90 && diastolic < 60) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_di));

                                    } else if (systolic >= 90 && systolic < 120
                                            && diastolic >= 60 && diastolic < 80) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_lixiang));

                                    } else if (systolic >= 120 && systolic <= 139
                                            && diastolic >= 80 && diastolic <= 89) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_zhengchang));

                                    } else if (systolic >= 140 && systolic <= 159
                                            && diastolic >= 90 && diastolic <= 99) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_qingdugao));

                                    } else if (systolic >= 160 && systolic <= 179
                                            && diastolic >= 100 && diastolic <= 109) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_zhongdugao));
                                    } else if (systolic >= 180 && diastolic >= 110) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "  :  " + getResources().getString(R.string.string_bloop_zzhongdugao));
                                    } else if (systolic >= 140 && diastolic < 90) {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "   :   " + "--");
                                    } else {
                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res)
                                                + "   :   " + "--");
                                    }
                                }
                            }
                            /**
                             * 设备类型以及  隐私状态
                             */
                            FrendDataBean.FriendInfoBean friendInfo = frendDataBean.getFriendInfo();
                            if (friendInfo != null) {
                                Log.d("----------AAAfriendInfo", friendInfo.toString());
                                String equipment = (String) friendInfo.getEquipment();
                                Log.d("----------AAAfriendInfo", friendInfo.getEquipment() + "");
                                if (!WatchUtils.isEmpty(equipment) && equipment.equals("B30")) {
                                    if (rela_bp != null) rela_bp.setVisibility(View.VISIBLE);
                                } else {
                                    if (rela_bp != null) rela_bp.setVisibility(View.GONE);
                                }
//                              Log.d("----------AAAfriendInfo", friendInfo.getExInfoSetList().size() + "--" + friendInfo.getExInfoSetList().toString());
                                List<FrendDataBean.FriendInfoBean.ExInfoSetListBean> exInfoSetList = friendInfo.getExInfoSetList();
                                if (exInfoSetList != null) {
                                    for (int i = 0; i < exInfoSetList.size(); i++) {
                                        FrendDataBean.FriendInfoBean.ExInfoSetListBean exInfoSetListBean = exInfoSetList.get(i);
//                                        Log.d("-------AAA", exInfoSetListBean.getSetType() + "==" + exInfoSetListBean.getExhibition());
                                        switch (exInfoSetListBean.getSetType()) {
                                            case 3://心率
                                                FrendSeeToMeHeart = exInfoSetListBean.getExhibition();//好友对我的隐私状态
                                                if (FrendSeeToMeHeart == 0) {
                                                    if (frendHrartMax != null)
                                                        frendHrartMax.setText(getResources().getString(R.string.zuigaoxinlv) + "(BPM）：" + "***");
                                                    if (frendHeartMin != null)
                                                        frendHeartMin.setText(getResources().getString(R.string.zuidixinlv) + "(BPM）：" + "***");
                                                    if (frendHreatAverage != null)
                                                        frendHreatAverage.setText(getResources().getString(R.string.pinjunxin) + "(BPM）：" + "***");
                                                }
                                                break;
                                            case 1://步数
                                                FrendSeeToMeStep = exInfoSetListBean.getExhibition();//好友对我的隐私状态
                                                if (FrendSeeToMeStep == 0) {
                                                    if (frendStepNumber != null)
                                                        frendStepNumber.setText(getResources().getString(R.string.step) + "(STEP)：" + "***");
                                                    if (frendStepDis != null) {
                                                        frendStepDis.setText(getResources().getString(R.string.mileage) + "(KM)：" + "***");
                                                    }
                                                    if (frendStepKcl != null) {
                                                        frendStepKcl.setText(getResources().getString(R.string.calories) + "(KCAL):" + "***");
                                                    }
                                                }
                                                break;
                                            case 4://血压
                                                FrendSeeToMeBlood = exInfoSetListBean.getExhibition();//好友对我的隐私状态
                                                if (FrendSeeToMeBlood == 0) {
                                                    if (frendBpMax != null)
                                                        frendBpMax.setText(getResources().getString(R.string.string_systolic) + "(mmHg)：" + "***");
                                                    if (frendBpMin != null) {
                                                        frendBpMin.setText(getResources().getString(R.string.string_diastolic) + "(mmHg)：" + "***");
                                                    }
                                                    if (frendBpAverage != null) {
                                                        frendBpAverage.setText(getResources().getString(R.string.string_cankao_res) + ":" + "  ***");
                                                    }
                                                }
                                                break;
                                            case 5://血氧
//                                            FrendSeeToMeHeart =mExInfoSetListBean.getExhibition();//好友对我的隐私状态
//                                            if (exhibition == 0) {
//
//                                            }
                                                break;
                                            case 2://睡眠
                                                FrendSeeToMeSleep = exInfoSetListBean.getExhibition();//好友对我的隐私状态
                                                if (FrendSeeToMeSleep == 0) {
                                                    if (frendSleeDeep != null)
                                                        frendSleeDeep.setText(getResources().getString(R.string.sleep_deep) + "(HOUR)：" + "***");
                                                    if (frendSleepShallow != null)
                                                        frendSleepShallow.setText(getResources().getString(R.string.sleep_light) + "(HOUR)：" + "***");
                                                    if (frendSleepTime != null)
                                                        frendSleepTime.setText(getResources().getString(R.string.long_when) + "(HOUR)：" + "***");
                                                }
                                                break;
                                        }
                                    }


                                }


                            }

                        }
                    }
                    break;
//                case 0x02:
//                    Log.d("----------设置好友可见我资料返回--", message.obj.toString());
//                    try {
//                        JSONObject jsonObject = new JSONObject(message.obj.toString());
//                        if (jsonObject.has("resultCode")) {
//                            String resultCode1 = jsonObject.getString("resultCode");
//                            boolean b = Commont.ReturnCode(resultCode1);
//                            if (b) {
//                                ToastUtil.showShort(FrendDataActivity.this, "成功");//发送成功待验证
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    break;
            }
            return false;
        }
    });


    @OnClick({R.id.rela_step, R.id.rela_sleep, R.id.rela_heart, R.id.rela_bp})
    public void onViewClicked(View view) {
        if (WatchUtils.isEmpty(applicant)) applicant = intent.getStringExtra("applicant");
        switch (view.getId()) {
            case R.id.rela_step:
                Log.d("-------AA-", "FrendSeeToMeStep:" + FrendSeeToMeStep + "");
                if (FrendSeeToMeStep == 0) return;
                startActivity(FrendStepActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
            case R.id.rela_sleep:
                Log.d("-------AA-", "FrendSeeToMeSleep:" + FrendSeeToMeSleep + "");
                if (FrendSeeToMeSleep == 0) return;
                if (!WatchUtils.isEmpty(stringJson))
                    startActivity(FrendSleepActivity.class, new String[]{"applicant", "stringJson"},
                            new String[]{applicant, stringJson});
                break;
            case R.id.rela_heart:
                Log.d("-------AA-", "FrendSeeToMeHeart:" + FrendSeeToMeHeart + "");
                if (FrendSeeToMeHeart == 0) return;
                startActivity(FrendHeartActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
            case R.id.rela_bp:
                if (FrendSeeToMeBlood == 0) return;
                startActivity(FrendBpActivity.class, new String[]{"applicant"},
                        new String[]{applicant});
                break;
        }

    }
}
