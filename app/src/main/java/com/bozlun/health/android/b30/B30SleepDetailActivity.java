package com.bozlun.health.android.b30;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.b30view.B30CusSleepView;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Constant;
import com.google.gson.Gson;
import com.veepoo.protocol.model.datas.SleepData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 睡眠详情
 */
public class B30SleepDetailActivity extends WatchBaseActivity {

    private static final String TAG = "B30SleepDetailActivity";

    @BindView(R.id.sleepSeekBar)
    SeekBar sleepSeekBar;

    /**
     * 跳转到B30SleepDetailActivity,并附带参数
     *
     * @param context 启动源上下文
     * @param date    附带的参数:日期
     */
    public static void startAndParams(Context context, String date) {
        Intent intent = new Intent(context, B30SleepDetailActivity.class);
        intent.putExtra(Constant.DETAIL_DATE, date);
        context.startActivity(intent);
    }

    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commentB30ShareImg)
    ImageView commentB30ShareImg;
    @BindView(R.id.detailSleepQuitRatingBar)
    RatingBar detailSleepQuitRatingBar;
    @BindView(R.id.detailCusSleepView)
    B30CusSleepView detailCusSleepView;
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

    private List<Integer> listValue;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);

    /**
     * 当前显示的日期(数据根据日期加载)
     */
    private String currDay;
    /**
     * Json工具类
     */
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_sleep_detail_layout);
        ButterKnife.bind(this);
        initViews();
        initData();
    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.sleep));
//        commentB30ShareImg.setVisibility(View.VISIBLE);
        detailSleepQuitRatingBar.setMax(5);
        // detailSleepQuitRatingBar.setRating(100);
        listValue = new ArrayList<>();
        gson = new Gson();
        currDay = getIntent().getStringExtra(Constant.DETAIL_DATE);


    }

    private void initData() {
        sleepCurrDateTv.setText(currDay);
        String mac = WatchUtils.getSherpBleMac(B30SleepDetailActivity.this);
        String sleep = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                .TYPE_SLEEP);
        SleepData sleepData = gson.fromJson(sleep, SleepData.class);
        showSleepChartView(sleepData);
        int sleepQulity = sleepData == null ? 1 : sleepData.getSleepQulity();
        detailSleepQuitRatingBar.setMax(5);
        detailSleepQuitRatingBar.setNumStars(sleepQulity);
        //detailSleepQuitRatingBar.setEnabled(false);

        String time = sleepData == null ? "--" : (sleepData.getAllSleepTime() / 60) + "H" +
                (sleepData.getAllSleepTime() % 60) + "m";
        detailAllSleepTv.setText(time);//睡眠时长
        String count = sleepData == null ? "--" : "" + sleepData.getWakeCount();
        detailAwakeNumTv.setText(count);//苏醒次数
        String down = sleepData == null ? "--" : sleepData.getSleepDown().getDateForSleepshow();
        detailStartSleepTv.setText(down);//入睡时间
        String up = sleepData == null ? "--" : sleepData.getSleepUp().getDateForSleepshow();
        detailAwakeTimeTv.setText(up);//苏醒时间
        String deep = sleepData == null ? "--" : sleepData.getDeepSleepTime() / 60 + "H" +
                (sleepData.getDeepSleepTime() % 60) + "m";
        detailDeepTv.setText(deep);//深度睡眠
        String low = sleepData == null ? "--" : sleepData.getLowSleepTime() / 60 + "H" +
                (sleepData.getLowSleepTime() % 60) + "m";
        detailHightSleepTv.setText(low);// 浅度睡眠
    }

    private void showSleepChartView(final SleepData sleepData) {
        listValue.clear();
        if (sleepData != null) {
            String slleepLin = sleepData.getSleepLine();
            for (int i = 0; i < slleepLin.length(); i++) {
                if (i <= slleepLin.length() - 1) {
                    int subStr = Integer.valueOf(slleepLin.substring(i, i + 1));
                    listValue.add(subStr);
                }
            }
            listValue.add(0, 2);
            listValue.add(0);
            listValue.add(2);
//            listValue.set(listValue.size()-1,0);
//            listValue.add(slleepLin.length()+1, 2);
        }
        if (listValue.size() > 0) {
            detailCusSleepView.setShowSeekBar(false);
            detailCusSleepView.setSleepList(listValue);
            //Log.e(TAG,"---11----max="+sleepSeekBar.getMax()+"--="+listValue.size());
            sleepSeekBar.setMax(listValue.size());
            //Log.e(TAG,"---22----max="+sleepSeekBar.getMax()+"--="+listValue.size());
            sleepSeekBar.setProgress(-2);
            //detailCusSleepView.invalidate();
            sleepSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(progress == listValue.size())
                        return;
                   // Log.e(TAG,"-------progress="+progress+"--="+sleepData.getSleepDown().getColck());

                    int sleepHour = sleepData.getSleepDown().getHour() * 60;
                    int sleepMine = sleepData.getSleepDown().getMinute();
                    //入睡时间 分钟
                    int sleepDownT = sleepHour + sleepMine;
                    int currD = sleepDownT+((progress==0?-1:progress-1) * 5);   //当前的分钟
                    //转换成时：分
                    int hour = (int) Math.floor(currD/60);
                    if(hour >= 24)
                        hour = hour-24;
                    int mine = currD % 60;

                    detailCusSleepView.setSleepDateTxt((hour==0?"00":(hour<10?"0"+hour:hour))+":"+(mine==0?"00":(mine<10?"0"+mine:mine))+"");
                    detailCusSleepView.setSeekBarSchdue(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    detailCusSleepView.setShowSeekBar(true,0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });




        } else {
            detailCusSleepView.setSleepList(new ArrayList<Integer>());
        }
    }

    @OnClick({R.id.commentB30BackImg, R.id.commentB30ShareImg, R.id.sleepCurrDateLeft, R.id.sleepCurrDateRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:    //返回
                finish();
                break;
            case R.id.commentB30ShareImg:   //分享
                WatchUtils.shareCommData(B30SleepDetailActivity.this);
                break;
            case R.id.sleepCurrDateLeft:   //切换上一天数据
                changeDayData(true);
                break;
            case R.id.sleepCurrDateRight:   //切换下一天数据
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

}
