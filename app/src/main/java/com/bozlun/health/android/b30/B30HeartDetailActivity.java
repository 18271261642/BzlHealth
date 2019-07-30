package com.bozlun.health.android.b30;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.adapter.B30HeartDetailAdapter;
import com.bozlun.health.android.b30.b30view.B30CusHeartView;
import com.bozlun.health.android.b30.bean.B30HalfHourDao;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Constant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.veepoo.protocol.model.datas.HalfHourRateData;
import com.veepoo.protocol.model.datas.HalfHourSportData;
import com.veepoo.protocol.model.datas.TimeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * B30心率详情界面
 */
public class B30HeartDetailActivity extends WatchBaseActivity {

    private static final String TAG = "B30HeartDetailActivity";

    @BindView(R.id.commB31TitleLayout)
    Toolbar commB31TitleLayout;

    /**
     * 跳转到B30HeartDetailActivity,并附带参数
     *
     * @param context 启动源上下文
     * @param date    附带的参数:日期
     */
    public static void startAndParams(Context context, String date) {
        Intent intent = new Intent(context, B30HeartDetailActivity.class);
        intent.putExtra(Constant.DETAIL_DATE, date);
        context.startActivity(intent);
    }

    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commentB30ShareImg)
    ImageView commentB30ShareImg;
    @BindView(R.id.b30HeartDetailView)
    B30CusHeartView b30HeartDetailView;
    @BindView(R.id.b30HeartDetailRecyclerView)
    RecyclerView b30HeartDetailRecyclerView;
    @BindView(R.id.rateCurrdateTv)
    TextView rateCurrdateTv;
    private List<HalfHourRateData> halfHourRateDatasList;
    private List<HalfHourSportData> halfHourSportDataList;
    private B30HeartDetailAdapter b30HeartDetailAdapter;

    /**
     * 心率图标数据
     */
    List<Integer> heartList;

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
        setContentView(R.layout.activity_b30_heart_detail_layout);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(Color.parseColor("#FF307E"));
        }



        initViews();
        initData();
    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(R.string.heart_rate);
        commentB30ShareImg.setVisibility(View.INVISIBLE);
        commB31TitleLayout.setBackgroundColor(Color.parseColor("#FF307E"));


        if (WatchUtils.isB36Device(B30HeartDetailActivity.this)) {
            commentB30ShareImg.setBackground(getResources().getDrawable(R.drawable.ic_action_new));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        b30HeartDetailRecyclerView.setLayoutManager(layoutManager);
        b30HeartDetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        halfHourRateDatasList = new ArrayList<>();
        halfHourSportDataList = new ArrayList<>();
        b30HeartDetailAdapter = new B30HeartDetailAdapter(halfHourRateDatasList, halfHourSportDataList, this);
        b30HeartDetailRecyclerView.setAdapter(b30HeartDetailAdapter);

        heartList = new ArrayList<>();
        gson = new Gson();
        currDay = getIntent().getStringExtra(Constant.DETAIL_DATE);
    }

    private void initData() {
        rateCurrdateTv.setText(currDay);
        String mac = WatchUtils.getSherpBleMac(B30HeartDetailActivity.this);
        if(WatchUtils.isEmpty(mac))
            return;
        String rate = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                .TYPE_RATE);
        List<HalfHourRateData> rateData = gson.fromJson(rate, new TypeToken<List<HalfHourRateData>>() {
        }.getType());
        String sport = B30HalfHourDao.getInstance().findOriginData(mac, currDay, B30HalfHourDao
                .TYPE_SPORT);
        List<HalfHourSportData> sportData = gson.fromJson(sport, new TypeToken<List<HalfHourSportData>>() {
        }.getType());
        halfHourRateDatasList.clear();
        halfHourSportDataList.clear();
        //Log.e("------------", "-------rate="+rateData.size() + "========" + sportData.size());

        List<Map<String, Integer>> listMap = new ArrayList<>();
        if (rateData != null && !rateData.isEmpty()) {
            int k = 0;
            for (int i = 0; i < 48; i++) {
                Map<String, Integer> map = new HashMap<>();
                int time = i * 30;
                map.put("time", time);
                TimeData tmpDate = rateData.get(k).getTime();
                int tmpIntDate = tmpDate.getHMValue();

                if (tmpIntDate == time) {
                    map.put("val", rateData.get(k).getRateValue());
                    if (k < rateData.size() - 1) {
                        k++;
                    }
                } else {
                    map.put("val", 0);
                }
                listMap.add(map);
            }
            Collections.sort(rateData, new Comparator<HalfHourRateData>() {
                @Override
                public int compare(HalfHourRateData o1, HalfHourRateData o2) {
                    return o2.getTime().getColck().compareTo(o1.getTime().getColck());
                }
            });

            Collections.sort(sportData, new Comparator<HalfHourSportData>() {
                @Override
                public int compare(HalfHourSportData o1, HalfHourSportData o2) {
                    return o2.getTime().getColck().compareTo(o1.getTime().getColck());
                }
            });

//            Set<HalfHourRateData> set = new HashSet<>(rateData);
//            Log.e(TAG,"--------set="+set.toString());

            halfHourRateDatasList.addAll(rateData);
            halfHourSportDataList.addAll(sportData);
        }
        b30HeartDetailAdapter.notifyDataSetChanged();
        heartList.clear();
        for (int i = 0; i < listMap.size(); i++) {
            Map<String, Integer> map = listMap.get(i);
            heartList.add(map.get("val"));
        }
        //圆点的半径
//        b30HeartDetailView.setPointRadio(5);
        b30HeartDetailView.setCanvasBeanLin(true);
        b30HeartDetailView.setRateDataList(heartList);


    }

    @OnClick({R.id.commentB30BackImg, R.id.commentB30ShareImg, R.id.rateCurrDateLeft,
            R.id.rateCurrDateRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg: //返回
                finish();
                break;
            case R.id.commentB30ShareImg:
                if (WatchUtils.isB36Device(B30HeartDetailActivity.this)) {
                    startActivity(ManualMeaureHeartActivity.class);
                } else {
                    WatchUtils.shareCommData(B30HeartDetailActivity.this);
                }

                break;
            case R.id.rateCurrDateLeft:   //切换上一天数据
                changeDayData(true);
                break;
            case R.id.rateCurrDateRight:   //切换下一天数据
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
