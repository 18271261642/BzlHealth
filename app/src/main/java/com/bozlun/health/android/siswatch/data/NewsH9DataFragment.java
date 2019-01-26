package com.bozlun.health.android.siswatch.data;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bean.AvgHeartRate;
import com.bozlun.health.android.bean.NewsSleepBean;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.CommonSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.H8ShareActivity;
import com.bozlun.health.android.siswatch.bean.WatchDataDatyBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.BaseFragment;
import com.bozlun.health.android.w30s.utils.W30BasicUtils;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lecho.lib.hellocharts.formatter.ColumnChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by sunjianhua on 2017/11/1.
 */

public class NewsH9DataFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, RequestView {

    private static final String TAG = "NewsH9DataFragment";

    View newH9DataView;

    //步数统计图
    @BindView(R.id.newH9DataStepChartView)
    ColumnChartView newH9DataStepChartView;
    //心率统计图
    @BindView(R.id.newH9DataHeartChartView)
    ColumnChartView newH9DataHeartChartView;
    //睡眠统计图
    @BindView(R.id.newH9DataSleepChartView)
    ColumnChartView newH9DataSleepChartView;
    //步数显示tv
    @BindView(R.id.newH9DataStepShowTv)
    TextView newH9DataStepShowTv;
    //心率显示tv
    @BindView(R.id.newH9DataHeartShowTv)
    TextView newH9DataHeartShowTv;
    //睡眠显示tv
    @BindView(R.id.newH9DataSleepShowTv)
    TextView newH9DataSleepShowTv;

    Unbinder unbinder;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //步数的相关
    List<WatchDataDatyBean> stepList;
    @BindView(R.id.newH9DataWeekTv)
    TextView newH9DataWeekTv;
    @BindView(R.id.newH9DataMonthTv)
    TextView newH9DataMonthTv;
    @BindView(R.id.newH9DataYearTv)
    TextView newH9DataYearTv;
    @BindView(R.id.newH9DataSwipe)
    SwipeRefreshLayout newH9DataSwipe;
    @BindView(R.id.h8_data_titleTv)
    TextView h8DataTitleTv;
    @BindView(R.id.h8_data_titleLinImg)
    ImageView h8DataTitleLinImg;
    @BindView(R.id.h8_dataLinChartImg)
    ImageView h8DataLinChartImg;
    @BindView(R.id.newH9DataStepChartView_text)
    TextView newH9DataStepChartViewText;
    @BindView(R.id.newH9DataHeartChartView_text)
    TextView newH9DataHeartChartViewText;
    @BindView(R.id.newH9DataSleepChartView_text)
    TextView newH9DataSleepChartViewText;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.step_text_show)
    TextView stepTextShow;
    @BindView(R.id.heart_text_show)
    TextView heartTextShow;
    @BindView(R.id.sleep_text_show)
    TextView sleepTextShow;
    private ColumnChartData data;   //步数的图表数据源
    //步数数值
    private List<Integer> mValues;
    private List<String> stepXList; //x轴数据
    private Map<String, Integer> stepSumMap; //用于计算年的步数
    private List<String> tempList;  //用于计算年的步数的list

    //心率相关
    private List<AvgHeartRate> heartList;   //数据集合
    private ColumnChartData heartData;  //心率的图表
    private List<Integer> heartValues;  //心率数值
    private List<String> heartXList;    //心率X轴数据
    private List<String> tempHeartList; //心率中间list
    private Map<String, Integer> heartMap;   //计算心率年的map


    //睡眠相关
    private List<NewsSleepBean> newsSleepBeanList;  //数据源
    private ColumnChartData sleepColumnChartData;
    private List<Integer> sleepVlaues;  //睡眠的数值
    private List<String> sleepXList;    //睡眠X轴
    private Map<String, Integer> sumSleepMap;    //保存计算睡眠年的数据
    private List<String> tempSleepList; //

    SubscriberOnNextListener subscriberOnNextListener;
    CommonSubscriber commonSubscriber;

    JSONObject jsonObject;


    private RequestPressent requestPressent;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    if (newH9DataSwipe != null) {
                        if (newH9DataSwipe.isRefreshing()) {
                            newH9DataSwipe.setRefreshing(false);
                        }
                    }
//                    clearClickTvStyle();
//                    newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
//                    newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        newH9DataView = inflater.inflate(R.layout.fragment_new_h9_data, container, false);
        unbinder = ButterKnife.bind(this, newH9DataView);
        initViews();
        clearClickTvStyle();
        newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
        newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
//        getAllChartData(7);
//        getDatas();
//        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//                newH9DataHeartChartView.postInvalidate();
//                newH9DataStepChartView.postInvalidate();
//                newH9DataSleepChartView.postInvalidate();
//                newH9DataSwipe.postInvalidate();
//            }
//        });
        return newH9DataView;
    }


    private int isOne = 0;//界面第一次创建




    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            try {
                getDatas();
            } catch (Exception e) {
                e.getMessage();
            }
            Log.d("===================", "isVisible");
            if (isOne == 1) {
                //initViews();
                //getDatas();
            }
        } else {
            isOne = 1;
            Log.d("===================", "No isVisible");
            if (newH9DataSwipe != null && newH9DataSwipe.isRefreshing()) {
                newH9DataSwipe.setRefreshing(false);
            }
            closeLoadingDialog();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("===================", "onStart");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("===================", "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        isOne = 0;
        Log.d("===================", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        isOne = 0;
        Log.d("===================", "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOne = 0;
        Log.d("===================", "onDestroy");
        if (requestPressent != null) {
            requestPressent.detach();
        }
    }

    public void getDatas() {
        if (newH9DataStepChartViewText != null)
            newH9DataStepChartViewText.setVisibility(View.VISIBLE);
        if (newH9DataHeartChartViewText != null)
            newH9DataHeartChartViewText.setVisibility(View.VISIBLE);
        if (newH9DataSleepChartViewText != null)
            newH9DataSleepChartViewText.setVisibility(View.VISIBLE);
        if (heartList != null) heartList.clear();
        if (stepList != null) stepList.clear();
        clearClickTvStyle();
        if (newH9DataWeekTv != null) {
            newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
            newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
        }
        getAllChartData(7);
    }

    @Override
    protected void onFragmentFirstVisible() {
        Log.d("===================", "onFragmentFirstVisible");
        isOne = 1;
    }

    //获取所有的数据
    private void getAllChartData(int week) {
        String baseurl = URLs.HTTPs;
        JSONObject jsonObect = new JSONObject();
        try {
            jsonObect.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            jsonObect.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(), Commont.BLEMAC));
            jsonObect.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), week)));
            jsonObect.put("endDate", WatchUtils.getCurrentDate());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (requestPressent != null) {
            //获取步数
            requestPressent.getRequestJSONObject(1, baseurl + URLs.GET_WATCH_DATA_DATA, getActivity(), jsonObect.toString(), week);
            //获取心率
            requestPressent.getRequestJSONObject(2, baseurl + "/data/getHeartRateByTime", getActivity(), jsonObect.toString(), week);
            //获取睡眠
            requestPressent.getRequestJSONObject(3, baseurl + "/sleep/getSleepByTime", getActivity(), jsonObect.toString(), week);
        }


//        getStepsData(week); //获取步数
        //getHeartRateData(week); //获取心率
        //getSleepH9Data(week);   //获取睡眠
    }


    private int ValueCount = 0;//计算年平均心率
    private Map<String, Integer> dayMap;

    //获取心率
    private void getHeartRateData(final int week) {
        heartList = new ArrayList<>();
        heartXList = new ArrayList<>();
        dayMap = new HashMap<>();
        String heartUrl = URLs.HTTPs + "/data/getHeartRateByTime";
        JSONObject heartJson = new JSONObject();
        try {
            heartJson.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            heartJson.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(), Commont.BLEMAC));
            heartJson.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), week)));
            heartJson.put("endDate", WatchUtils.getCurrentDate());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                //W30BasicUtils.e(TAG, "----心率返回----" + result);
                ValueCount = 0;
                if (result != null) {
                    try {
                        JSONObject heartJson = new JSONObject(result);
                        if (heartJson.getString("resultCode").equals("001")) {
                            String heartRate = heartJson.getString("heartRate");
                            //W30BasicUtils.e(TAG, "----heartRate---" + heartRate);
                            if (!heartRate.equals("[]")) {
                                heartList = new Gson().fromJson(heartRate, new TypeToken<List<AvgHeartRate>>() {
                                }.getType());

                                if (week == 365) {    //年
                                    heartMap = new HashMap<>();
                                    heartMap.clear();
                                    heartValues = new ArrayList<>();
                                    heartValues.clear();
                                    int heartSum = 0;
                                    int Day = 0;
                                    dayMap.clear();
                                    for (int i = 0; i < heartList.size(); i++) {
                                        String strDate = heartList.get(i).getRtc().substring(2, 7);

                                        if (heartMap.get(strDate) != null) {
                                            heartSum += heartList.get(i).getAvgHeartRate();
                                            if (heartList.get(i).getAvgHeartRate() > 0) {
                                                dayMap.put(strDate, Day++);
                                            }
                                        } else {
                                            heartSum = heartList.get(i).getAvgHeartRate();
                                            Day = 0;
                                            if (heartList.get(i).getAvgHeartRate() > 0) {
                                                dayMap.put(strDate, Day++);
                                            }
                                        }
                                        //W30BasicUtils.e(TAG, "----heartSum---" + heartSum);
                                        if (heartSum > 0) {
                                            ValueCount++;
                                        }
                                        heartMap.put(strDate, heartSum);
                                    }
                                    tempHeartList = new ArrayList<>();
                                    tempHeartList.clear();
                                    for (Map.Entry<String, Integer> maps : heartMap.entrySet()) {
                                        tempHeartList.add(maps.getKey().trim());
                                    }
                                    //排序时间
                                    Collections.sort(tempHeartList, new Comparator<String>() {
                                        @Override
                                        public int compare(String o1, String o2) {
                                            return o1.compareTo(o2);
                                        }
                                    });
                                    heartXList.clear();
                                    //W30BasicUtils.e(TAG, "----heartSum---" + tempHeartList.size());
                                    for (int i = 0; i < tempHeartList.size(); i++) {
                                        heartValues.add(heartMap.get(tempHeartList.get(i)));
                                        heartXList.add(tempHeartList.get(i));
//                                        W30BasicUtils.e(TAG, "------心率值----" + heartMap.get(tempHeartList.get(i))
//                                                + "====心率对应X====" + tempHeartList.get(i));
                                    }
                                    heartTextShow.setText(getResources().getString(R.string.string_heart_year));
                                    showHeartChartData(13);
                                } else {  //周或者月
                                    heartTextShow.setText(getResources().getString(R.string.string_heart_day));
                                    heartValues = new ArrayList<>();
                                    heartValues.clear();
                                    for (AvgHeartRate avgHeart : heartList) {
                                        heartValues.add(avgHeart.getAvgHeartRate()); //2017-10-10
                                        //Log.e(TAG, "----xxx----" + avgHeart.getRtc().substring(5, avgHeart.getRtc().length()));
                                        heartXList.add(avgHeart.getRtc().substring(8, avgHeart.getRtc().length()));
                                    }
                                    //newH9DataHeartShowTv.setText(heartList.get(heartList.size()-1).getAvgHeartRate());
                                    showHeartChartData(heartList.size());
                                }

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        commonSubscriber = new CommonSubscriber(subscriberOnNextListener, getActivity());
        OkHttpObservable.getInstance().getData(commonSubscriber, heartUrl, heartJson.toString());
    }

    //展示心率的图表
    private void showHeartChartData(int count) {
        int numberTime = 0;
        int numberData = 0;
        if (heartList != null && heartList.size() > 0) {
            if (heartValues != null) {
                for (int i = 0; i < heartValues.size(); i++) {
                    if (heartValues.get(i) != 0) {
                        numberTime++;
                        numberData += heartValues.get(i);
                    }
                }
                int max = Collections.max(heartValues);
                if (max <= 0) {
                    newH9DataHeartChartViewText.setVisibility(View.VISIBLE);
                } else {
                    newH9DataHeartChartViewText.setVisibility(View.GONE);
                }
            }
        }

        // 使用的 8列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = count + 1;
        //定义一个圆柱对象集合
        final List<Column> columns = new ArrayList<>();
        //子列数据集合
        List<SubcolumnValue> values;

        List<AxisValue> axisValues = new ArrayList<>();
        //遍历列数numColumns
        for (int i = 0; i < numColumns; i++) {
            values = new ArrayList<>();
            //遍历每一列的每一个子列
            for (int j = 0; j < numSubcolumns; j++) {
                if (i == (numColumns - 1)) {
                    //为每一柱图添加颜色和数值
                    float f = 0;
                    values.add(new SubcolumnValue(f, Color.WHITE));
                } else {
                    if (ValueCount == 0) {
                        ValueCount = 1;
                    }
                    float f = 0;
                    if ((numColumns - 1) == 13) {
                        int Count = 1;
                        if (dayMap != null) {
                            Count = dayMap.size();
                        }
                        //为每一柱图添加颜色和数值
                        f = heartValues.get(i) / Count;
                    } else {
                        //为每一柱图添加颜色和数值
                        f = heartValues.get(i);
                    }
                    //为每一柱图添加颜色和数值
//                    float f = heartValues.get(i)/ValueCount;
//                SubcolumnValue sb = new SubcolumnValue();
//                sb.setTarget(f);
//                values.add(sb);
                    values.add(new SubcolumnValue(f, Color.WHITE));
                    if (j == numSubcolumns - 1) {
                        if ((numColumns - 1) == 13) {
                            //Log.d(TAG, "=======所有有数据的天数======" + ValueCount);
                            String s = String.valueOf(WatchUtils.div(numberData, ValueCount, 2)).split("[.]")[0];
                            newH9DataHeartShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + s + " bpm");
                        } else {
                            String s = String.valueOf(WatchUtils.div(numberData, numberTime, 2)).split("[.]")[0];
                            newH9DataHeartShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + s + " bpm");
                        }

                    }
                }

            }

            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);
            //是否有数据标注
            column.setHasLabels(true);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
            //给x轴坐标设置描述
            if (i == (numColumns - 1)) {
                axisValues.add(new AxisValue(i).setLabel(" "));
            } else {
                axisValues.add(new AxisValue(i).setLabel(heartXList.get(i)));
            }

        }

        //创建一个带有之前圆柱对象column集合的ColumnChartData
        heartData = new ColumnChartData(columns);
        //定义x轴y轴相应参数
        Axis axisX = new Axis();
        axisX.setTextSize(12);
        axisX.setMaxLabelChars(6);
        axisX.hasLines();
        //x轴颜色
        axisX.setTextColor(Color.WHITE);
        axisX.setLineColor(Color.WHITE);
        axisX.setValues(axisValues);
        axisX.setTypeface(Typeface.MONOSPACE);
        //把X轴Y轴数据设置到ColumnChartData 对象中
        heartData.setAxisXBottom(axisX);


//        final List<AxisValue> values1 = new ArrayList<>();
//        //        for(int i = 0; i < 100; i+= 50){
//        for (int i = 0; i < 60; i++) {
//            int i1 = i * 30;
//            AxisValue value = new AxisValue(i1);
//            String label = "" + i1;
//            value.setLabel(label);
//            values1.add(value);
//        }

        Axis axisY = new Axis();  //Y轴
        axisY.setTextColor(Color.WHITE);  //设置字体颜色
        axisY.setHasLines(true);
        axisY.setTypeface(Typeface.MONOSPACE);
        axisY.setAutoGenerated(true);
        //axisY.setLineColor(getResources().getColor(R.color.white_trans20));
        axisY.setMaxLabelChars(7); //默认是3，只能看最后三个数字
        axisY.setTextSize(10);//设置字体大小
        axisY.setLineColor(Color.parseColor("#30FFFFFF"));
        heartData.setAxisYLeft(axisY);  //Y轴设置在左边


        heartData.setValueLabelBackgroundEnabled(true);
        heartData.setValueLabelBackgroundColor(getResources().getColor(R.color.new_colorAccent));
        heartData.setValueLabelsTextColor(getResources().getColor(R.color.white));// 设置数据文字颜色
        heartData.setValueLabelTypeface(Typeface.MONOSPACE);// 设置数据文字样式

        heartData.setFillRatio(0.2f);    //设置柱子的宽度

        //给表填充数据，显示出来
        newH9DataHeartChartView.setColumnChartData(heartData);
        newH9DataHeartChartView.startDataAnimation(2000);
        newH9DataHeartChartView.setZoomEnabled(false);  //支持缩放
        newH9DataHeartChartView.setInteractive(true);  //支持与用户交互
        newH9DataHeartChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        newH9DataHeartChartView.postInvalidate();
        //item的点击事件
        newH9DataHeartChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                newH9DataHeartShowTv.setText("" + subcolumnValue.getValue() + " bpm");
            }

            @Override
            public void onValueDeselected() {

            }
        });

    }

    //获取睡眠
    private void getSleepH9Data(final int week) {
        sleepVlaues = new ArrayList<>();
        newsSleepBeanList = new ArrayList<>();
        sleepXList = new ArrayList<>();

        String sleepUrl = URLs.HTTPs + "/sleep/getSleepByTime";
        JSONObject sleepJson = new JSONObject();
        try {
            sleepJson.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            sleepJson.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(),  Commont.BLEMAC));
            sleepJson.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), week)));
            sleepJson.put("endDate", WatchUtils.getCurrentDate());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                W30BasicUtils.e(TAG, "----睡眠返回----" + result);
                if (result != null) {
                    try {
                        JSONObject sleepJson = new JSONObject(result);
                        if (sleepJson.getString("resultCode").equals("001")) {
                            String sleepData = sleepJson.getString("sleepData");
                            if (sleepData != null && !sleepData.equals("[]")) {
                                newsSleepBeanList = new Gson().fromJson(sleepData, new TypeToken<List<NewsSleepBean>>() {
                                }.getType());
                                W30BasicUtils.e(TAG, "----睡眠返回长度----" + newsSleepBeanList.size());
                                if (week == 365) {    //年
                                    sumSleepMap = new HashMap<>();
                                    int sleepSum = 0;
                                    int tempShallowSleep = 0;
                                    int tempdeepSleep = 0;
                                    for (int i = 0; i < newsSleepBeanList.size(); i++) {
                                        String strDate = newsSleepBeanList.get(i).getRtc().substring(2, 7);
                                        if (sumSleepMap.get(strDate) != null) {
                                            int shallowSleep = newsSleepBeanList.get(i).getShallowSleep();
                                            int deepSleep = newsSleepBeanList.get(i).getDeepSleep();
//                                            if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
//                                                shallowSleep = 0;
//                                            }
//                                            if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
//                                                deepSleep = 0;
//                                            }
                                            sleepSum += (shallowSleep + deepSleep);
                                            W30BasicUtils.e(TAG, "----深睡浅睡值----" + sleepSum);
//                                            sleepSum += newsSleepBeanList.get(i).getSleepLen();
                                        } else {
                                            int shallowSleep = newsSleepBeanList.get(i).getShallowSleep();
                                            int deepSleep = newsSleepBeanList.get(i).getDeepSleep();
//                                            if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
//                                                shallowSleep = 0;
//                                            }
//                                            if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
//                                                deepSleep = 0;
//                                            }
                                            sleepSum = (shallowSleep + deepSleep);
//                                            sleepSum = newsSleepBeanList.get(i).getSleepLen();
                                        }

                                        sumSleepMap.put(strDate, sleepSum);
                                    }
                                    tempSleepList = new ArrayList<>();
                                    tempSleepList.clear();
                                    //遍历map
                                    for (Map.Entry<String, Integer> maps : sumSleepMap.entrySet()) {
                                        tempSleepList.add(maps.getKey().trim());
                                        W30BasicUtils.e(TAG, "---睡眠MAP--=" + maps.getKey().trim());
                                    }
                                    //升序排列
                                    Collections.sort(tempSleepList, new Comparator<String>() {
                                        @Override
                                        public int compare(String s, String t1) {
                                            return s.compareTo(t1);
                                        }
                                    });
                                    sleepXList.clear();
                                    sleepVlaues.clear();
                                    for (int k = 0; k < tempSleepList.size(); k++) {
                                        sleepVlaues.add(sumSleepMap.get(tempSleepList.get(k)));
                                        sleepXList.add(tempSleepList.get(k));
                                        Log.e(TAG, "---睡眠Value--=" + sumSleepMap.get(tempSleepList.get(k)) + "-==-睡眠Data-=" + tempSleepList.get(k));
                                    }
                                    sleepTextShow.setText(getResources().getString(R.string.string_sleep_year));
                                    showSleepChat(13);

                                } else {
                                    sleepTextShow.setText(getResources().getString(R.string.string_sleep_day));
                                    sleepXList.clear();
                                    sleepVlaues.clear();
                                    for (NewsSleepBean sleepBean : newsSleepBeanList) {
                                        int shallowSleep = sleepBean.getShallowSleep();
                                        int deepSleep = sleepBean.getDeepSleep();
                                        if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
                                            shallowSleep = 0;
                                        }
                                        if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
                                            deepSleep = 0;
                                        }
                                        int sleepSum = (shallowSleep + deepSleep);
                                        sleepVlaues.add(sleepSum); //2017-11-11
//                                        sleepVlaues.add(sleepBean.getSleepLen()); //2017-11-11
                                        sleepXList.add(sleepBean.getRtc().substring(8, sleepBean.getRtc().length()));
                                    }
                                    showSleepChat(newsSleepBeanList.size());
                                }

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        commonSubscriber = new CommonSubscriber(subscriberOnNextListener, getActivity());
        OkHttpObservable.getInstance().getData(commonSubscriber, sleepUrl, sleepJson.toString());


    }

    //步数返回
    private void getStepsData(final int weekTag) {
        //mValues = new ArrayList<>();    //实例化步数的数值
        stepXList = new ArrayList<>();
        String url = URLs.HTTPs + URLs.GET_WATCH_DATA_DATA;
        JSONObject jsonObect = new JSONObject();
        try {
            jsonObect.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            jsonObect.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(), Commont.BLEMAC));
            jsonObect.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate()), weekTag)));
            jsonObect.put("endDate", WatchUtils.getCurrentDate());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        stepList = new ArrayList<>();
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                //W30BasicUtils.e(TAG, "----步数-------one-----" + result);
                if (result != null) {
                    try {
                        jsonObject = new JSONObject(result);
                        if (jsonObject.getString("resultCode").equals("001")) {
                            String daydata = jsonObject.getString("day");
                            if (!WatchUtils.isEmpty(daydata) && !daydata.equals("[]")) {
                                stepList = new Gson().fromJson(daydata, new TypeToken<List<WatchDataDatyBean>>() {
                                }.getType());
                                if (stepList != null && stepList.size() > 0) {
                                    newH9DataStepChartViewText.setVisibility(View.GONE);
                                }
                                //W30BasicUtils.e(TAG, "----步数返回长度-------two-----" + stepList.size());
                                if (weekTag == 365) { //年
                                    //W30BasicUtils.e(TAG, "----步数返回长度2-------two-----" + stepList.size());
                                    mValues = new ArrayList<>();
                                    stepSumMap = new HashMap<>();
                                    int sum = 0;
                                    for (int i = 0; i < stepList.size(); i++) {
                                        String strDate = stepList.get(i).getRtc().substring(2, 7);
                                        if (stepSumMap.get(strDate) != null) {
                                            sum += stepList.get(i).getStepNumber();
                                        } else {
                                            sum = stepList.get(i).getStepNumber();
                                        }
                                        //W30BasicUtils.e(TAG, "------步数返回时间=" + strDate + "-步数返回值-sum=" + sum);
                                        stepSumMap.put(strDate, sum);
                                    }
                                    tempList = new ArrayList<>();
                                    tempList.clear();
                                    //遍历map
                                    for (Map.Entry<String, Integer> entry : stepSumMap.entrySet()) {
                                        tempList.add(entry.getKey().trim());
                                        //W30BasicUtils.e(TAG, "------步数时间遍历----" + entry.getKey().trim());
                                    }
                                    //升序排列
                                    Collections.sort(tempList, new Comparator<String>() {
                                        @Override
                                        public int compare(String s, String t1) {
                                            return s.compareTo(t1);
                                        }
                                    });
                                    for (int k = 0; k < tempList.size(); k++) {
                                        mValues.add(stepSumMap.get(tempList.get(k)));
                                        stepXList.add(tempList.get(k));
                                        //W30BasicUtils.e(TAG, "------添加值=" + stepSumMap.get(tempList.get(k)) + "---x轴=" + tempList.get(k));
                                    }
                                    stepTextShow.setText(getResources().getString(R.string.string_step_year));
                                    showStepsChat(13);

                                } else {
                                    stepTextShow.setText(getResources().getString(R.string.string_step_day));
                                    mValues = new ArrayList<>();
                                    //获取值
                                    for (WatchDataDatyBean stepNumber : stepList) {
                                        mValues.add(stepNumber.getStepNumber());    //步数的数值显示
                                        String rct = stepNumber.getRtc().substring(8, stepNumber.getRtc().length());
                                        stepXList.add(rct);
                                        //Log.e(TAG, "---添加值周显示=" + stepNumber.getStepNumber() + "--rct=" + rct);
                                    }
                                    Log.e(TAG, "----listsize--" + stepList.size());
                                    showStepsChat(stepList.size());
                                }


                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        commonSubscriber = new CommonSubscriber(subscriberOnNextListener, getActivity());
        OkHttpObservable.getInstance().getData(commonSubscriber, url, jsonObect.toString());

    }

    private void initViews() {
        if (h8DataTitleTv != null) h8DataTitleTv.setText(getResources().getString(R.string.data));
        if (h8DataTitleLinImg != null) h8DataTitleLinImg.setVisibility(View.INVISIBLE);
        if (h8DataLinChartImg != null) h8DataLinChartImg.setVisibility(View.INVISIBLE);
        if (newH9DataSwipe != null) newH9DataSwipe.setOnRefreshListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //步数图表显示
    private void showStepsChat(final int count) {
        int numberTime = 0;
        int numberData = 0;
        if (stepList != null && stepList.size() > 0) {
            if (mValues != null) {
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i) != 0) {
                        numberTime++;
                        numberData += mValues.get(i);
                    }
                }
                int max = Collections.max(mValues);
                if (max <= 0) {
                    newH9DataStepChartViewText.setVisibility(View.VISIBLE);
                } else {
                    newH9DataStepChartViewText.setVisibility(View.GONE);
                }
            }
        }

        // Log.e(TAG, "---STEP-count--" + count);
        // 使用的 8列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = count + 1;
        //定义一个圆柱对象集合
        final List<Column> columns = new ArrayList<>();
        //子列数据集合
        List<SubcolumnValue> values;

        List<AxisValue> axisValues = new ArrayList<>();
        //遍历列数numColumns
        for (int i = 0; i < numColumns; i++) {
            values = new ArrayList<>();
            //遍历每一列的每一个子列
            for (int j = 0; j < numSubcolumns; j++) {
                //为每一柱图添加颜色和数值

//                SubcolumnValue sb = new SubcolumnValue();
//                sb.setTarget(f);
//                sb.setColor(Color.WHITE);
//                values.add(sb);

                if (i == (numColumns - 1)) {
                    //为每一柱图添加颜色和数值
                    values.add(new SubcolumnValue(0, Color.WHITE));
                } else {
                    if (count == 14) {    //年
                        float f = mValues.get(i);
                        //String s = tempList.get(i);
                        values.add(new SubcolumnValue(f, Color.WHITE));
                    } else {
                        float f = mValues.get(i);
                        //int stepNumber = stepList.get(i).getStepNumber();
                        values.add(new SubcolumnValue(f, Color.WHITE));
                    }
                    if (j == numSubcolumns - 1) {
                        String s = String.valueOf(WatchUtils.div(numberData, numberTime, 2)).split("[.]")[0];
                        newH9DataStepShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + s + " step");
                    }
                }

            }
            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);
            //是否有数据标注
            column.setHasLabels(true);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
            //给x轴坐标设置描述
            if (i == (numColumns - 1)) {
                axisValues.add(new AxisValue(i).setLabel(" "));
            } else {
                axisValues.add(new AxisValue(i).setLabel(stepXList.get(i)));
            }
        }


        //创建一个带有之前圆柱对象column集合的ColumnChartData
        data = new ColumnChartData(columns);
        //定义x轴y轴相应参数
        Axis axisX = new Axis();
        axisX.setTextSize(12);
        axisX.setMaxLabelChars(6);
        axisX.hasLines();
        //x轴颜色
        axisX.setTextColor(Color.WHITE);
        axisX.setLineColor(Color.WHITE);
        axisX.setValues(axisValues);
        axisX.setTypeface(Typeface.MONOSPACE);
        //把X轴Y轴数据设置到ColumnChartData 对象中
        data.setAxisXBottom(axisX);


//        final List<AxisValue> values1 = new ArrayList<>();
//        //        for(int i = 0; i < 100; i+= 50){
//        for (int i = 0; i < 60; i++) {
//            int i1 = i * 30;
//            AxisValue value = new AxisValue(i1);
//            String label = "" + i1;
//            value.setLabel(label);
//            values1.add(value);
//        }

        Axis axisY = new Axis();  //Y轴
        axisY.setTextColor(Color.WHITE);  //设置字体颜色
        axisY.setHasLines(true);
        axisY.setTypeface(Typeface.MONOSPACE);
        axisY.setAutoGenerated(true);
        //axisY.setLineColor(getResources().getColor(R.color.white_trans20));
        axisY.setMaxLabelChars(7); //默认是3，只能看最后三个数字
        axisY.setTextSize(10);//设置字体大小
        axisY.setLineColor(Color.parseColor("#30FFFFFF"));
        data.setAxisYLeft(axisY);  //Y轴设置在左边


//        //定义x轴y轴相应参数
//        Axis axisX = new Axis();
//        axisX.setTextSize(10);
//        axisX.setMaxLabelChars(6);
//        axisX.setTextColor(Color.WHITE);
//        axisX.setLineColor(Color.WHITE);
//        axisX.setTypeface(Typeface.MONOSPACE);
//        //x轴颜色
//        axisX.setTextColor(getResources().getColor(R.color.album_item_bg));
//        axisX.setValues(axisValues);
//

//        Axis axisY = new Axis().setHasLines(true);
//        axisY.setMaxLabelChars(7);
//        axisY.setHasLines(true);
//        //Y轴颜色
//        axisY.setLineColor(Color.parseColor("#8066ff"));
//        axisY.setTextSize(10);
//        axisY.setTextColor(Color.WHITE);
//        axisY.setLineColor(Color.parseColor("#30FFFFFF"));
//        axisY.setTypeface(Typeface.MONOSPACE);
//        axisY.setAutoGenerated(true);
//        data.setAxisYLeft(axisY);


        //把X轴Y轴数据设置到ColumnChartData 对象中

        data.setValueLabelBackgroundColor(getResources().getColor(R.color.new_colorAccent));
        data.setValueLabelsTextColor(getResources().getColor(R.color.white));// 设置数据文字颜色
        data.setValueLabelTypeface(Typeface.MONOSPACE);// 设置数据文字样式

        data.setValueLabelTextSize(8);
        data.setFillRatio(0.2f);    //设置柱子的宽度

        //给表填充数据，显示出来
        newH9DataStepChartView.setColumnChartData(data);
        newH9DataStepChartView.startDataAnimation(2000);
        newH9DataStepChartView.setZoomEnabled(false);  //支持缩放
        newH9DataStepChartView.setInteractive(true);  //支持与用户交互

        //item的点击事件
        newH9DataStepChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                Log.e(TAG, "----i--" + i + "--i1--" + i1);
                newH9DataStepShowTv.setText("" + subcolumnValue.getValue() + " step");
                if (count == 13) {    //年
                    //newH9DataStepShowTv.setText("" + stepSumMap.get(tempList.get(i)) + "");

                } else {
                    //newH9DataStepShowTv.setText("" + stepList.get(i).getStepNumber() + "");
                }

            }

            @Override
            public void onValueDeselected() {

            }
        });
    }

    //睡眠图标显示
    private void showSleepChat(final int count) {
        int numberTime = 0;
        int numberData = 0;

        if (newsSleepBeanList != null && newsSleepBeanList.size() > 0) {
            if (sleepVlaues != null) {
                for (int i = 0; i < sleepVlaues.size(); i++) {
                    if (sleepVlaues.get(i) != 0) {
                        numberTime++;
                        numberData += sleepVlaues.get(i);
                    }
                }
                int max = Collections.max(sleepVlaues);
                if (max <= 0) {
                    newH9DataSleepChartViewText.setVisibility(View.VISIBLE);
                } else {
                    newH9DataSleepChartViewText.setVisibility(View.GONE);
                }
            }
        }

        Log.e(TAG, "----count--" + count);
        // 使用的 8列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = count + 1;
        //定义一个圆柱对象集合
        final List<Column> columns = new ArrayList<>();
        //子列数据集合
        List<SubcolumnValue> values;

        List<AxisValue> axisValues = new ArrayList<>();
        //遍历列数numColumns
        for (int i = 0; i < numColumns; i++) {

            values = new ArrayList<>();
            //遍历每一列的每一个子列
            for (int j = 0; j < numSubcolumns; j++) {
                if (i == (numColumns - 1)) {
                    //为每一柱图添加颜色和数值
                    float f = 0;
                    values.add(new SubcolumnValue(f, Color.WHITE));
                } else {
                    //为每一柱图添加颜色和数值
                    float f = sleepVlaues.get(i);
//                SubcolumnValue sb = new SubcolumnValue();
//                sb.setTarget(f);
//                values.add(sb);
                    values.add(new SubcolumnValue(f, Color.WHITE));
                    if (j == numSubcolumns - 1) {
                        String s = String.valueOf(WatchUtils.div(numberData, numberTime, 0)).split("[.]")[0];
                        newH9DataSleepShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + s + " min");
                    }
                }
            }

            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);

            //是否有数据标注
            column.setHasLabels(true);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
            //给x轴坐标设置描述
            if (i == (numColumns - 1)) {
                axisValues.add(new AxisValue(i).setLabel(" "));
            } else {
                axisValues.add(new AxisValue(i).setLabel(sleepXList.get(i)));
            }
        }

        //创建一个带有之前圆柱对象column集合的ColumnChartData
        sleepColumnChartData = new ColumnChartData(columns);
//        //定义x轴y轴相应参数
//        Axis axisY = new Axis().setHasLines(true);
//        //axisY.setName("出场率(%)");//轴名称
//        axisY.hasLines();//是否显示网格线
//        //Y轴颜色
//        axisY.setTextColor(getResources().getColor(R.color.antiquewhite));//颜色
//
//        data.setAxisYLeft(axisY);
        //定义x轴y轴相应参数
        Axis axisX = new Axis();
        axisX.setTextSize(12);
        axisX.setMaxLabelChars(6);
        axisX.setTypeface(Typeface.MONOSPACE);
        axisX.hasLines();
        //x轴颜色
        axisX.setTextColor(Color.WHITE);
        axisX.setLineColor(Color.WHITE);
//        axisX.setTextColor(getResources().getColor(R.color.album_item_bg));
        axisX.setValues(axisValues);
        //把X轴Y轴数据设置到ColumnChartData 对象中
        sleepColumnChartData.setAxisXBottom(axisX);

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setTextColor(Color.WHITE);  //设置字体颜色
        axisY.setHasLines(true);
        axisY.setTypeface(Typeface.MONOSPACE);
        //axisY.setLineColor(getResources().getColor(R.color.white_trans20));
        axisY.setMaxLabelChars(7); //默认是3，只能看最后三个数字
        axisY.setTextSize(10);//设置字体大小
        axisY.setLineColor(Color.parseColor("#30FFFFFF"));
        axisY.setAutoGenerated(true);
        sleepColumnChartData.setAxisYLeft(axisY);  //Y轴设置在左边

        sleepColumnChartData.setValueLabelBackgroundColor(getResources().getColor(R.color.new_colorAccent));
        sleepColumnChartData.setValueLabelsTextColor(getResources().getColor(R.color.white));// 设置数据文字颜色
        sleepColumnChartData.setValueLabelTypeface(Typeface.MONOSPACE);// 设置数据文字样式

        //sleepColumnChartData.setValueLabelTextSize(15);
        sleepColumnChartData.setFillRatio(0.2f);    //设置柱子的宽度
        //给表填充数据，显示出来
        newH9DataSleepChartView.setColumnChartData(sleepColumnChartData);
        newH9DataSleepChartView.startDataAnimation(2000);
        newH9DataSleepChartView.setZoomEnabled(false);  //支持缩放
        newH9DataSleepChartView.setInteractive(true);  //支持与用户交互
        newH9DataSleepChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        newH9DataSleepChartView.postInvalidate();

        //item的点击事件
        newH9DataSleepChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                Log.e(TAG, "----i--" + i + "--i1--" + i1);
                newH9DataSleepShowTv.setText("" + subcolumnValue.getValue() + " min");


            }

            @Override
            public void onValueDeselected() {

            }
        });
    }


    @OnClick({R.id.newH9DataWeekTv, R.id.newH9DataMonthTv, R.id.newH9DataYearTv,
            R.id.h8_dataShareImg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.newH9DataWeekTv:  //周的点击
                if (heartList != null) heartList.clear();
                if (stepList != null) stepList.clear();
                if (newsSleepBeanList != null) newsSleepBeanList.clear();
                clearClickTvStyle();
                newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
                newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
                getAllChartData(7);
                break;
            case R.id.newH9DataMonthTv: //月的点击
                if (heartList != null) heartList.clear();
                if (stepList != null) stepList.clear();
                if (newsSleepBeanList != null) newsSleepBeanList.clear();
                clearClickTvStyle();
                newH9DataMonthTv.setTextColor(getResources().getColor(R.color.white));
                newH9DataMonthTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
                getAllChartData(30);
                break;
            case R.id.newH9DataYearTv:  //年的点击
                if (heartList != null) heartList.clear();
                if (stepList != null) stepList.clear();
                if (newsSleepBeanList != null) newsSleepBeanList.clear();
                clearClickTvStyle();
                newH9DataYearTv.setTextColor(getResources().getColor(R.color.white));
                newH9DataYearTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_two);
                getAllChartData(365);
                break;
            case R.id.h8_dataShareImg:  //分享
                startActivity(new Intent(getActivity(), H8ShareActivity.class));
                break;

        }
    }

    private void clearClickTvStyle() {//.new_colorAccent
        if (newH9DataWeekTv != null) {
            newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
            newH9DataWeekTv.setTextColor(Color.parseColor("#333333"));
        }
        if (newH9DataMonthTv != null) {
            newH9DataMonthTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
            newH9DataMonthTv.setTextColor(Color.parseColor("#333333"));
        }
        if (newH9DataYearTv != null) {
            newH9DataYearTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
            newH9DataYearTv.setTextColor(Color.parseColor("#333333"));
        }
    }


    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1001;
                handler.sendMessage(message);
            }
        }, 3 * 1000);

    }


    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog("Loaging...");
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        if (what == 1) {  //不是
            analysisStepData(object.toString(), daystag);
        } else if (what == 2) {    //心率
            analysisHeartData(object.toString(), daystag);
        } else if (what == 3) {    //睡眠
            analysisSleepData(object.toString(), daystag);
        }
    }


    @Override
    public void failedData(int what, Throwable e) {
        e.getMessage();
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        if (what == 3) {
            closeLoadingDialog();
        }
    }

    //解析不是
    private void analysisStepData(String stepdata, int weekTag) {
        stepXList = new ArrayList<>();
        if (stepdata != null) {
            try {
                jsonObject = new JSONObject(stepdata);
                if (jsonObject.getString("resultCode").equals("001")) {
                    String daydata = jsonObject.getString("day");
                    if (!WatchUtils.isEmpty(daydata) && !daydata.equals("[]")) {
                        stepList = new Gson().fromJson(daydata, new TypeToken<List<WatchDataDatyBean>>() {
                        }.getType());
                        if (stepList != null && stepList.size() > 0) {
                            newH9DataStepChartViewText.setVisibility(View.GONE);
                        }
                        //W30BasicUtils.e(TAG, "----步数返回长度-------two-----" + stepList.size());
                        if (weekTag == 365) { //年
                            //W30BasicUtils.e(TAG, "----步数返回长度2-------two-----" + stepList.size());
                            mValues = new ArrayList<>();
                            stepSumMap = new HashMap<>();
                            int sum = 0;
                            for (int i = 0; i < stepList.size(); i++) {
                                String strDate = stepList.get(i).getRtc().substring(2, 7);
                                if (stepSumMap.get(strDate) != null) {
                                    sum += stepList.get(i).getStepNumber();
                                } else {
                                    sum = stepList.get(i).getStepNumber();
                                }
                                //W30BasicUtils.e(TAG, "------步数返回时间=" + strDate + "-步数返回值-sum=" + sum);
                                stepSumMap.put(strDate, sum);
                            }
                            tempList = new ArrayList<>();
                            tempList.clear();
                            //遍历map
                            for (Map.Entry<String, Integer> entry : stepSumMap.entrySet()) {
                                tempList.add(entry.getKey().trim());
                                //W30BasicUtils.e(TAG, "------步数时间遍历----" + entry.getKey().trim());
                            }
                            //升序排列
                            Collections.sort(tempList, new Comparator<String>() {
                                @Override
                                public int compare(String s, String t1) {
                                    return s.compareTo(t1);
                                }
                            });
                            for (int k = 0; k < tempList.size(); k++) {
                                mValues.add(stepSumMap.get(tempList.get(k)));
                                stepXList.add(tempList.get(k));
                                //W30BasicUtils.e(TAG, "------添加值=" + stepSumMap.get(tempList.get(k)) + "---x轴=" + tempList.get(k));
                            }
                            stepTextShow.setText(getResources().getString(R.string.string_step_year));
                            showStepsChat(13);

                        } else {
                            stepTextShow.setText(getResources().getString(R.string.string_step_day));
                            mValues = new ArrayList<>();
                            //获取值
                            for (WatchDataDatyBean stepNumber : stepList) {
                                mValues.add(stepNumber.getStepNumber());    //步数的数值显示
                                String rct = stepNumber.getRtc().substring(8, stepNumber.getRtc().length());
                                stepXList.add(rct);
                                //Log.e(TAG, "---添加值周显示=" + stepNumber.getStepNumber() + "--rct=" + rct);
                            }
                            Log.e(TAG, "----listsize--" + stepList.size());
                            showStepsChat(stepList.size());
                        }


                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //解析xinlv
    private void analysisHeartData(String heartData, int week) {
        heartList = new ArrayList<>();
        heartXList = new ArrayList<>();
        dayMap = new HashMap<>();
        ValueCount = 0;
        if (heartData != null) {
            try {
                JSONObject heartJson = new JSONObject(heartData);
                if (heartJson.getString("resultCode").equals("001")) {
                    String heartRate = heartJson.getString("heartRate");
                    //W30BasicUtils.e(TAG, "----heartRate---" + heartRate);
                    if (!heartRate.equals("[]")) {
                        heartList = new Gson().fromJson(heartRate, new TypeToken<List<AvgHeartRate>>() {
                        }.getType());

                        if (week == 365) {    //年
                            heartMap = new HashMap<>();
                            heartMap.clear();
                            heartValues = new ArrayList<>();
                            heartValues.clear();
                            int heartSum = 0;
                            int Day = 0;
                            dayMap.clear();
                            for (int i = 0; i < heartList.size(); i++) {
                                String strDate = heartList.get(i).getRtc().substring(2, 7);

                                if (heartMap.get(strDate) != null) {
                                    heartSum += heartList.get(i).getAvgHeartRate();
                                    if (heartList.get(i).getAvgHeartRate() > 0) {
                                        dayMap.put(strDate, Day++);
                                    }
                                } else {
                                    heartSum = heartList.get(i).getAvgHeartRate();
                                    Day = 0;
                                    if (heartList.get(i).getAvgHeartRate() > 0) {
                                        dayMap.put(strDate, Day++);
                                    }
                                }
                                //W30BasicUtils.e(TAG, "----heartSum---" + heartSum);
                                if (heartSum > 0) {
                                    ValueCount++;
                                }
                                heartMap.put(strDate, heartSum);
                            }
                            tempHeartList = new ArrayList<>();
                            tempHeartList.clear();
                            for (Map.Entry<String, Integer> maps : heartMap.entrySet()) {
                                tempHeartList.add(maps.getKey().trim());
                            }
                            //排序时间
                            Collections.sort(tempHeartList, new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    return o1.compareTo(o2);
                                }
                            });
                            heartXList.clear();
                            //W30BasicUtils.e(TAG, "----heartSum---" + tempHeartList.size());
                            for (int i = 0; i < tempHeartList.size(); i++) {
                                heartValues.add(heartMap.get(tempHeartList.get(i)));
                                heartXList.add(tempHeartList.get(i));
//                                        W30BasicUtils.e(TAG, "------心率值----" + heartMap.get(tempHeartList.get(i))
//                                                + "====心率对应X====" + tempHeartList.get(i));
                            }
                            heartTextShow.setText(getResources().getString(R.string.string_heart_year));
                            showHeartChartData(13);
                        } else {  //周或者月
                            heartTextShow.setText(getResources().getString(R.string.string_heart_day));
                            heartValues = new ArrayList<>();
                            heartValues.clear();
                            for (AvgHeartRate avgHeart : heartList) {
                                heartValues.add(avgHeart.getAvgHeartRate()); //2017-10-10
                                //Log.e(TAG, "----xxx----" + avgHeart.getRtc().substring(5, avgHeart.getRtc().length()));
                                heartXList.add(avgHeart.getRtc().substring(8, avgHeart.getRtc().length()));
                            }
                            //newH9DataHeartShowTv.setText(heartList.get(heartList.size()-1).getAvgHeartRate());
                            showHeartChartData(heartList.size());
                        }

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //解析shuimian
    private void analysisSleepData(String sleepdata, int daystag) {
        W30BasicUtils.e(TAG, "----睡眠返回----" + sleepdata);
        sleepVlaues = new ArrayList<>();
        newsSleepBeanList = new ArrayList<>();
        sleepXList = new ArrayList<>();
        if (sleepdata != null) {
            try {
                JSONObject sleepJson = new JSONObject(sleepdata);
                if (sleepJson.getString("resultCode").equals("001")) {
                    String sleepData = sleepJson.getString("sleepData");
                    if (sleepData != null && !sleepData.equals("[]")) {
                        newsSleepBeanList = new Gson().fromJson(sleepData, new TypeToken<List<NewsSleepBean>>() {
                        }.getType());
                        W30BasicUtils.e(TAG, "----睡眠返回长度----" + newsSleepBeanList.size());
                        if (daystag == 365) {    //年
                            sumSleepMap = new HashMap<>();
                            int sleepSum = 0;
                            int tempShallowSleep = 0;
                            int tempdeepSleep = 0;
                            for (int i = 0; i < newsSleepBeanList.size(); i++) {
                                String strDate = newsSleepBeanList.get(i).getRtc().substring(2, 7);
                                if (sumSleepMap.get(strDate) != null) {
                                    int shallowSleep = newsSleepBeanList.get(i).getShallowSleep();
                                    int deepSleep = newsSleepBeanList.get(i).getDeepSleep();
//                                            if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
//                                                shallowSleep = 0;
//                                            }
//                                            if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
//                                                deepSleep = 0;
//                                            }
                                    sleepSum += (shallowSleep + deepSleep);
                                    W30BasicUtils.e(TAG, "----深睡浅睡值----" + sleepSum);
//                                            sleepSum += newsSleepBeanList.get(i).getSleepLen();
                                } else {
                                    int shallowSleep = newsSleepBeanList.get(i).getShallowSleep();
                                    int deepSleep = newsSleepBeanList.get(i).getDeepSleep();
//                                            if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
//                                                shallowSleep = 0;
//                                            }
//                                            if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
//                                                deepSleep = 0;
//                                            }
                                    sleepSum = (shallowSleep + deepSleep);
//                                            sleepSum = newsSleepBeanList.get(i).getSleepLen();
                                }

                                sumSleepMap.put(strDate, sleepSum);
                            }
                            tempSleepList = new ArrayList<>();
                            tempSleepList.clear();
                            //遍历map
                            for (Map.Entry<String, Integer> maps : sumSleepMap.entrySet()) {
                                tempSleepList.add(maps.getKey().trim());
                                W30BasicUtils.e(TAG, "---睡眠MAP--=" + maps.getKey().trim());
                            }
                            //升序排列
                            Collections.sort(tempSleepList, new Comparator<String>() {
                                @Override
                                public int compare(String s, String t1) {
                                    return s.compareTo(t1);
                                }
                            });
                            sleepXList.clear();
                            sleepVlaues.clear();
                            for (int k = 0; k < tempSleepList.size(); k++) {
                                sleepVlaues.add(sumSleepMap.get(tempSleepList.get(k)));
                                sleepXList.add(tempSleepList.get(k));
                                Log.e(TAG, "---睡眠Value--=" + sumSleepMap.get(tempSleepList.get(k)) + "-==-睡眠Data-=" + tempSleepList.get(k));
                            }
                            sleepTextShow.setText(getResources().getString(R.string.string_sleep_year));
                            showSleepChat(13);

                        } else {
                            sleepTextShow.setText(getResources().getString(R.string.string_sleep_day));
                            sleepXList.clear();
                            sleepVlaues.clear();
                            for (NewsSleepBean sleepBean : newsSleepBeanList) {
                                int shallowSleep = sleepBean.getShallowSleep();
                                int deepSleep = sleepBean.getDeepSleep();
//                                if (WatchUtils.isEmpty(String.valueOf(shallowSleep))) {
//                                    shallowSleep = 0;
//                                }
//                                if (WatchUtils.isEmpty(String.valueOf(deepSleep))) {
//                                    deepSleep = 0;
//                                }
                                int sleepSum = (shallowSleep + deepSleep);
                                sleepVlaues.add(sleepSum); //2017-11-11
//                                        sleepVlaues.add(sleepBean.getSleepLen()); //2017-11-11
                                sleepXList.add(sleepBean.getRtc().substring(8, sleepBean.getRtc().length()));
                            }
                            showSleepChat(newsSleepBeanList.size());
                        }

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
