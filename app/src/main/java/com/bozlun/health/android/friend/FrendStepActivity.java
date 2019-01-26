package com.bozlun.health.android.friend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.LogTestUtil;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.friend.bean.FrendStepBean;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.adapters.CommonRecyclerAdapter;
import com.bozlun.health.android.w30s.adapters.MyViewHolder;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrendStepActivity extends WatchBaseActivity implements RequestView {

    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commentB30ShareImg)
    ImageView commentB30ShareImg;

    @BindView(R.id.b30ChartTopRel)
    RelativeLayout b30ChartTopRel;
    @BindView(R.id.b30BarChart)
    BarChart b30BarChart;
    @BindView(R.id.b30SportChartLin1)
    LinearLayout b30SportChartLin1;
    @BindView(R.id.b30StepDetailRecyclerView)
    RecyclerView b30StepDetailRecyclerView;
    @BindView(R.id.countStepTv)
    TextView countStepTv;
    @BindView(R.id.countDisTv)
    TextView countDisTv;
    @BindView(R.id.countKcalTv)
    TextView countKcalTv;
    @BindView(R.id.stepCurrDateTv)
    TextView stepCurrDateTv;
    @BindView(R.id.line_dis)
    LinearLayout line_dis;
    @SuppressLint("SimpleDateFormat")
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * 当前显示的日期(数据根据日期加载)
     */
    private String currDay;
    private RequestPressent requestPressent;
    List<FrendStepBean.FriendStepNumberBean> friendStepNumber = null;

    /**
     * 列表数据源
     */
    private List<FrendStepBean.FriendStepNumberBean> dataList = new ArrayList<>();
    /**
     * 列表适配器
     */
    private MyAdapter b30StepDetailAdapter;
    /**
     * 步数数据
     */
    List<BarEntry> b30ChartList = new ArrayList<>();
    String applicant = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_step_detail_layout);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null) return;
        applicant = intent.getStringExtra("applicant");
        initHandler();
        initViews();
        initData();
    }

    private void initViews() {

        requestPressent = new RequestPressent();
        requestPressent.attach(this);
        currDay = df.format(new Date());

        line_dis.setVisibility(View.GONE);
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(R.string.move_ment);
//        commentB30ShareImg.setVisibility(View.VISIBLE);
        b30ChartTopRel.setVisibility(View.GONE);
        b30SportChartLin1.setBackgroundColor(Color.parseColor("#2594EE"));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, true);
        b30StepDetailRecyclerView.setLayoutManager(layoutManager);
        b30StepDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        b30StepDetailAdapter = new MyAdapter(this, dataList, R.layout.item_b30_step_detail_layout);
        b30StepDetailRecyclerView.setAdapter(b30StepDetailAdapter);
    }


    /**
     * rec---适配器
     */
    class MyAdapter extends CommonRecyclerAdapter<FrendStepBean.FriendStepNumberBean> {

        public MyAdapter(Context context, List<FrendStepBean.FriendStepNumberBean> data, int layoutId) {
            super(context, data, layoutId);
        }

        @Override
        public void convert(MyViewHolder holder, final FrendStepBean.FriendStepNumberBean item) {
            holder.setText(R.id.itemB30StepDetailTimeTv, item.getRtc().substring(11, 16));
            holder.setText(R.id.itemB30StepDetailKcalTv, item.getStepNumber() + "");
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        stepCurrDateTv.setText(currDay);
        findFrendStepItem(currDay);
    }


    /**
     * 查询好友日 步数详细数据
     */
    public void findFrendStepItem(String rtc) {
        String sleepUrl = URLs.HTTPs + Commont.FrendStepToDayData;
        JSONObject sleepJson = new JSONObject();
        try {
            String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
            if (!WatchUtils.isEmpty(userId)) sleepJson.put("userId", userId);
            if (!WatchUtils.isEmpty(applicant)) sleepJson.put("applicant", applicant);
            sleepJson.put("rtc", rtc);

            Log.d("-----------朋友--", "获取好友详日细步数参数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x01, sleepUrl, FrendStepActivity.this, sleepJson.toString(), 0);
        }
    }


    @OnClick({R.id.commentB30BackImg, R.id.commentB30ShareImg, R.id.stepCurrDateLeft,
            R.id.stepCurrDateRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:
                finish();
                break;
            case R.id.commentB30ShareImg:   //分享
                WatchUtils.shareCommData(this);
                break;
            case R.id.stepCurrDateLeft:   //切换上一天数据
                changeDayData(true);
                break;
            case R.id.stepCurrDateRight:   //切换下一天数据
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
        if (mHandler != null) mHandler.sendEmptyMessage(0x02);
        if (object != null || !TextUtils.isEmpty(object.toString().trim())) {
            LogTestUtil.e("-----------朋友--", "获取好友详日细步数返回--" + object.toString());
            Message message = new Message();
            message.what = what;
            message.arg1 = daystag;
            message.obj = object;
            if (mHandler != null) mHandler.sendMessage(message);
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


    /**
     * 显示 图表 和 list
     *
     * @param sportData
     */
    void showChartAndList(List<FrendStepBean.FriendStepNumberBean> sportData) {
        dataList.clear();
        b30ChartList.clear();
        if (sportData == null||sportData.isEmpty()) {
            initBarChart(b30ChartList);
            b30BarChart.invalidate();
            b30StepDetailAdapter.notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < sportData.size(); i++) {
            b30ChartList.add(new BarEntry(i, sportData.get(i).getStepNumber()));
            if (sportData.get(i).getStepNumber() > 0) {
                dataList.add(sportData.get(i));
            }
        }

        if (b30ChartList != null) {
            initBarChart(b30ChartList);
            b30BarChart.invalidate();
        }
        if (dataList != null && !dataList.isEmpty()) {
            Collections.sort(dataList, new Comparator<FrendStepBean.FriendStepNumberBean>() {
                @Override
                public int compare(FrendStepBean.FriendStepNumberBean o1, FrendStepBean.FriendStepNumberBean o2) {
                    return o2.getRtc().compareTo(o1.getRtc());
                }
            });
            b30StepDetailAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 步数图表展示
     */
    private void initBarChart(List<BarEntry> pointBar) {
        BarDataSet barDataSet = new BarDataSet(pointBar, "");
        barDataSet.setDrawValues(false);//是否显示柱子上面的数值
        barDataSet.setColor(Color.WHITE);//设置第一组数据颜色

        Legend mLegend = b30BarChart.getLegend();
        mLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        mLegend.setForm(Legend.LegendForm.CIRCLE);// 样式
        mLegend.setFormSize(15.0f);// 字体
        mLegend.setTextColor(Color.WHITE);// 颜色
        mLegend.setEnabled(false);

        ArrayList<IBarDataSet> threeBarData = new ArrayList<>();//IBarDataSet 接口很关键，是添加多组数据的关键结构，LineChart也是可以采用对应的接口类，也可以添加多组数据
        threeBarData.add(barDataSet);

        BarData bardata = new BarData(threeBarData);
        bardata.setBarWidth(0.5f);  //设置柱子宽度

        b30BarChart.setData(bardata);
        b30BarChart.setDoubleTapToZoomEnabled(false);   //双击缩放
        b30BarChart.getLegend().setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);//设置注解的位置在左上方
        b30BarChart.getLegend().setForm(Legend.LegendForm.CIRCLE);//这是左边显示小图标的形状

        b30BarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的位置
        b30BarChart.getXAxis().setDrawGridLines(false);//不显示网格
        b30BarChart.getXAxis().setEnabled(false);

        b30BarChart.getDescription().setEnabled(false);

        b30BarChart.getAxisRight().setEnabled(false);//右侧不显示Y轴
        b30BarChart.getAxisLeft().setAxisMinValue(0.8f);//设置Y轴显示最小值，不然0下面会有空隙
        b30BarChart.getAxisLeft().setDrawGridLines(false);//不设置Y轴网格
        b30BarChart.getAxisLeft().setEnabled(false);
        b30BarChart.getXAxis().setSpaceMax(0.5f);
        b30BarChart.animateXY(1000, 2000);//设置动画
    }


    private Handler mHandler;

    public void initHandler() {
//        HandlerThread handlerThread = new HandlerThread("FrendStep");
//        handlerThread.start();
//        mHandler = new Handler(handlerThread.getLooper(), mCallback);//Three + handler
        mHandler = new Handler(mCallback);
    }

    Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case 0x01:
                        String res = message.obj.toString();
                        FrendStepBean frendStepBean = new Gson().fromJson(res, FrendStepBean.class);
                        if (!WatchUtils.isEmpty(frendStepBean.getResultCode())
                                && frendStepBean.getResultCode().equals("001")) {
                            friendStepNumber = frendStepBean.getFriendStepNumber();
                            if (friendStepNumber != null && !friendStepNumber.isEmpty()) {
                                showChartAndList(friendStepNumber);
                            }
                        }
                        break;
                    case 0x02:
                        if (friendStepNumber != null) {
                            friendStepNumber.clear();
                        } else {
                            friendStepNumber = new ArrayList<>();
                        }
                        showChartAndList(friendStepNumber);
                        break;
                }
            }catch (Error error){
            }

            return false;
        }
    };

    @Override
    public void finish() {
        if (mHandler!=null)mHandler.removeCallbacksAndMessages(null);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (mHandler!=null)mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
