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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.h9.utils.H9TimeUtil;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bean.AvgHeartRate;
import com.bozlun.health.android.bean.NewsSleepBean;
import com.bozlun.health.android.siswatch.H8ShareActivity;
import com.bozlun.health.android.siswatch.bean.WatchDataDatyBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.BaseFragment;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lecho.lib.hellocharts.formatter.ColumnChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by sunjianhua on 2017/11/1.
 */

public class W30sNewsH9DataFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, RequestView {

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
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.CHINESE);
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
    @BindView(R.id.text_tell)
    TextView textTell;
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
    private int ValueCount = 0;//计算年平均心率
    private Map<String, Integer> dayMap;

    //睡眠相关
    private List<NewsSleepBean> newsSleepBeanList;  //数据源
    private ColumnChartData sleepColumnChartData;
    private List<Integer> sleepVlaues;  //睡眠的数值
    private List<String> sleepXList;    //睡眠X轴
    private Map<String, Integer> sumSleepMap;    //保存计算睡眠年的数据
    private List<String> tempSleepList; //

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
                    break;
            }
        }
    };


    boolean isOneCreate = true;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOneCreate = true;
        try {
            requestPressent = new RequestPressent();
            requestPressent.attach(this);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        newH9DataView = inflater.inflate(R.layout.fragment_new_h9_data, container, false);
        unbinder = ButterKnife.bind(this, newH9DataView);
        Log.e(TAG,"-----onCreateView---");


        initViews();
        isOneCreate = true;
        clearClickTvStyle();

        newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
        newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
        textTell.setVisibility(View.VISIBLE);
        scrollviewOntch();

        return newH9DataView;
    }

    private void scrollviewOntch() {
        scrollView.setOnTouchListener(new TouchListenerImpl());
    }


    private class TouchListenerImpl implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (isOneCreate) {
                isOneCreate = false;
                textTell.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private int isOne = 0;//界面第一次创建

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        Log.e(TAG,"------onFragmentVisibleChange---"+isVisible);
        if(isVisible){
            if(getActivity() != null && !getActivity().isFinishing()){
                getDatas();
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        isOne = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        isOne = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOne = 0;
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
        if (heartList != null)
            heartList.clear();
        if (stepList != null)
            stepList.clear();

//        if (newH9DataWeekTv != null) {
//            newH9DataWeekTv.setTextColor(getResources().getColor(R.color.white));
//            newH9DataWeekTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_one);
//        }
        getAllChartData(7);
    }

    @Override
    protected void onFragmentFirstVisible() {
        isOne = 1;
    }

    //获取所有的数据
    private void getAllChartData(int week) {
        try {
            String baseurl = URLs.HTTPs;
            JSONObject jsonObect = new JSONObject();
            jsonObect.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            String blemac = (String) SharedPreferencesUtils.readObject(getActivity(),Commont.BLEMAC);
            if(!WatchUtils.isEmpty(blemac)){
                jsonObect.put("deviceCode",blemac);
            }
            Log.e(TAG,"-------blemc="+blemac);
//            if (MyCommandManager.DEVICENAME != null && MyCommandManager.DEVICENAME.equals("W30")) {
//                jsonObect.put("deviceCode", SharedPreferenceUtil.get(getContext(), "mylanmac", ""));
//            } else {
//                jsonObect.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(), "mylanmac"));
//            }
            jsonObect.put("startDate", sdf.format(WatchUtils.getDateBefore(sdf.parse(WatchUtils.getCurrentDate3()), week)));
//            jsonObect.put("endDate", WatchUtils.getCurrentDate());
            Date dateBeforess = H9TimeUtil.getDateBefore(new Date(), 1);
            String nextDay = H9TimeUtil.getValidDateStr2(dateBeforess);
            jsonObect.put("endDate", nextDay.substring(0, 10));


            if (requestPressent != null) {
                //获取步数
                requestPressent.getRequestJSONObject(1, baseurl + URLs.GET_WATCH_DATA_DATA, getActivity(), jsonObect.toString(), week);
                //获取心率
                requestPressent.getRequestJSONObject(2, baseurl + "/data/getHeartRateByTime", getActivity(), jsonObect.toString(), week);
                //获取睡眠
                requestPressent.getRequestJSONObject(3, baseurl + "/sleep/getSleepByTime", getActivity(), jsonObect.toString(), week);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    //展示心率的图表
    private void showHeartChartData(int count) {
        int heartnumberTime = 0;    //有数据的天数
        int heartnumberData = 0;    //有数据天的数据
        if (heartList != null && heartList.size() > 0) {
            if (heartValues != null) {
                for (int i = 0; i < heartValues.size(); i++) {
                    if (heartValues.get(i) != 0) {
                        heartnumberTime++;
                        heartnumberData += heartValues.get(i);
                    }
                }
                if (heartnumberTime != 0) {
                    //计算平均步数
                    double aveStep = WatchUtils.div(heartnumberData, heartnumberTime, 2);
                    String showAveStep;
                    if (String.valueOf(aveStep).trim().contains(".")) {   //不是整数
                        showAveStep = StringUtils.substringBefore(String.valueOf(aveStep).trim(), ".");
                    } else {
                        showAveStep = String.valueOf(aveStep);
                    }
                    newH9DataHeartShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + showAveStep + " bpm");
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
        int numColumns = count;
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
                float f = heartValues.get(i);
                values.add(new SubcolumnValue(f, Color.WHITE));
            }
            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);
            //是否有数据标注
            column.setHasLabels(false);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(false);
            columns.add(column);
            //给x轴坐标设置描述
            axisValues.add(new AxisValue(i).setLabel(heartXList.get(i)));
        }
        //创建一个带有之前圆柱对象column集合的ColumnChartData
        heartData = new ColumnChartData(columns);
        //定义x轴y轴相应参数
        Axis axisX = new Axis();
        axisX.setTextSize(12);
        if (count == 8) {
            axisX.setMaxLabelChars(0);
        } else {
            axisX.setMaxLabelChars(6);
        }

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
//        newH9DataHeartChartView.startDataAnimation(2000);
//        newH9DataHeartChartView.setInteractive(true);  //支持与用户交互
//        newH9DataHeartChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
//        newH9DataHeartChartView.postInvalidate();

        if (count != 8) {
            newH9DataHeartChartView.setZoomEnabled(true);  //支持缩放
        } else {
            newH9DataHeartChartView.setZoomEnabled(false);  //支持缩放
        }
        newH9DataHeartChartView.setInteractive(true);  //支持与用户交互
        newH9DataHeartChartView.setScrollEnabled(true);
        newH9DataHeartChartView.setMaxZoom(2f);
        newH9DataHeartChartView.setZoomType(ZoomType.HORIZONTAL);
        newH9DataHeartChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        newH9DataHeartChartView.startDataAnimation(2000);
        newH9DataHeartChartView.postInvalidate();

        Viewport viewport = new Viewport(newH9DataHeartChartView.getMaximumViewport());
        viewport.left = 0;
        viewport.right = 5;
        newH9DataHeartChartView.setCurrentViewport(viewport);

        //item的点击事件
        newH9DataHeartChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                String tmpHeartV = StringUtils.substringBefore(String.valueOf(subcolumnValue.getValue()),".");
                if(!WatchUtils.isEmpty(tmpHeartV)){
                    newH9DataHeartShowTv.setText("" + tmpHeartV + " bpm");
                }

            }

            @Override
            public void onValueDeselected() {

            }
        });

    }


    private void initViews() {
        if (h8DataTitleTv != null) h8DataTitleTv.setText(getResources().getString(R.string.data));
        if (h8DataTitleLinImg != null)
            h8DataTitleLinImg.setVisibility(View.INVISIBLE);
        if (h8DataLinChartImg != null)
            h8DataLinChartImg.setVisibility(View.INVISIBLE);
        if (newH9DataSwipe != null)
            newH9DataSwipe.setOnRefreshListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //步数图表显示
    private void showStepsChat(final int count) {
//        Log.e(TAG, "---STEP-count--" + count + "--=mValues=" + mValues.toString());
        int numberTime = 0;     //有数据的天数
        int numberData = 0;     //有数据天数的步数集合
        if (stepList != null && stepList.size() > 0) {
            if (mValues != null) {
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i) != 0) {
                        numberTime++;
                        numberData += mValues.get(i);
                    }
                }
//                Log.e(TAG, "-------天数=" + numberTime + "-=累计步数=" + numberData);
                if (numberTime != 0) {
                    //计算平均步数
                    double aveStep = WatchUtils.div(numberData, numberTime, 2);
                    String showAveStep;
                    if (String.valueOf(aveStep).trim().contains(".")) {   //不是整数
                        showAveStep = StringUtils.substringBefore(String.valueOf(aveStep).trim(), ".");
                    } else {
                        showAveStep = String.valueOf(aveStep);
                    }
                    newH9DataStepShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + showAveStep + " step");
                }
                int max = Collections.max(mValues);
//                Log.e(TAG, "---max=" + max);
                if (max <= 0) {
                    newH9DataStepChartViewText.setVisibility(View.VISIBLE);
                } else {
                    newH9DataStepChartViewText.setVisibility(View.GONE);
                }
            }
        }
//        Log.e(TAG, "-------1---11-1-");
        // 使用的 8列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = count;
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
                float f = mValues.get(i);
//                SubcolumnValue sb = new SubcolumnValue();
//                sb.setTarget(f);
//                sb.setColor(Color.WHITE);
//                values.add(sb);
                values.add(new SubcolumnValue(f, Color.WHITE));
            }
            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);
            //是否有数据标注
            column.setHasLabels(false);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(false);
            columns.add(column);
            //给x轴坐标设置描述
            axisValues.add(new AxisValue(i).setLabel(stepXList.get(i)));
        }

//        Log.e(TAG, "-------3333-");
        //创建一个带有之前圆柱对象column集合的ColumnChartData
        data = new ColumnChartData(columns);
        //定义x轴y轴相应参数
        Axis axisX = new Axis(axisValues);
        axisX.setTextSize(12);
        if (count == 8) {
            axisX.setMaxLabelChars(0);
        } else {
            axisX.setMaxLabelChars(6);
        }
        axisX.hasLines();
        //x轴颜色
        axisX.setTextColor(Color.WHITE);
        axisX.setLineColor(Color.WHITE);
        axisX.setValues(axisValues);
        axisX.setTypeface(Typeface.MONOSPACE);
        //把X轴Y轴数据设置到ColumnChartData 对象中
        data.setAxisXBottom(axisX);
//        Log.e(TAG, "-------444-");
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
        //把X轴Y轴数据设置到ColumnChartData 对象中
        data.setValueLabelBackgroundColor(getResources().getColor(R.color.new_colorAccent));
        data.setValueLabelsTextColor(getResources().getColor(R.color.white));// 设置数据文字颜色
        data.setValueLabelTypeface(Typeface.MONOSPACE);// 设置数据文字样式
        data.setValueLabelTextSize(8);
        data.setFillRatio(0.2f);    //设置柱子的宽度
        //给表填充数据，显示出来
        newH9DataStepChartView.setColumnChartData(data);

//        Log.e(TAG, "------height==" + newH9DataStepChartView.getMaximumViewport().height() * 1.25f + "-count=" + count);
        if (count != 8) {
            newH9DataStepChartView.setZoomEnabled(true);  //支持缩放
        } else {
            newH9DataStepChartView.setZoomEnabled(false);  //支持缩放
        }
        newH9DataStepChartView.setInteractive(true);  //支持与用户交互
        newH9DataStepChartView.setScrollEnabled(true);
        newH9DataStepChartView.setMaxZoom(2f);
        newH9DataStepChartView.setZoomType(ZoomType.HORIZONTAL);
        newH9DataStepChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        newH9DataStepChartView.startDataAnimation(2000);
//        Viewport viewport = new Viewport(newH9DataStepChartView.getMaximumViewport());
//        viewport.bottom = 0f;
//        viewport.top = newH9DataStepChartView.getMaximumViewport().height()*1.25f;
//        newH9DataStepChartView.setMaximumViewport(viewport);
        Viewport viewport = new Viewport(newH9DataStepChartView.getMaximumViewport());
        viewport.left = 0;
        viewport.right = 5;
        newH9DataStepChartView.setCurrentViewport(viewport);
        // newH9DataStepChartView.moveTo(0,0);


        //item的点击事件
        newH9DataStepChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
//                Log.e(TAG, "----i--" + i + "--i1--" + i1);
                String tmpStepV = StringUtils.substringBefore(String.valueOf(subcolumnValue.getValue()),".");
                if(!WatchUtils.isEmpty(tmpStepV)){
                    newH9DataStepShowTv.setText("" + tmpStepV + " step");
                }

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

//        Log.e(TAG, "----count--" + count);
        // 使用的 8列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = count;
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
                float f = sleepVlaues.get(i);
//                SubcolumnValue sb = new SubcolumnValue();
//                sb.setTarget(f);
//                values.add(sb);
                values.add(new SubcolumnValue(f, Color.WHITE));
                String s = String.valueOf(WatchUtils.div(numberData, numberTime, 0)).split("[.]")[0];
                double setScale = (double) WatchUtils.div((double) Integer.valueOf(s), (double) 60, 1);
                newH9DataSleepShowTv.setText(getResources().getString(R.string.string_data_chart_pingjun) + setScale + " h");
            }

            //创建Column对象
            Column column = new Column(values);
            //这一步是能让圆柱标注数据显示带小数的重要一步 让我找了好久问题
            //作者回答https://github.com/lecho/hellocharts-android/issues/185
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter();
            column.setFormatter(chartValueFormatter);

            //是否有数据标注
            column.setHasLabels(false);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(false);
            columns.add(column);
            //给x轴坐标设置描述
            axisValues.add(new AxisValue(i).setLabel(sleepXList.get(i)));
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
        if (count == 8) {
            axisX.setMaxLabelChars(0);
        } else {
            axisX.setMaxLabelChars(6);
        }
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
//        newH9DataSleepChartView.startDataAnimation(2000);
//        newH9DataSleepChartView.setZoomEnabled(false);  //支持缩放
//        newH9DataSleepChartView.setInteractive(true);  //支持与用户交互
//        newH9DataSleepChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        if (count != 8) {
            newH9DataSleepChartView.setZoomEnabled(true);  //支持缩放
        } else {
            newH9DataSleepChartView.setZoomEnabled(false);  //支持缩放
        }
        newH9DataSleepChartView.setInteractive(true);  //支持与用户交互
        newH9DataSleepChartView.setScrollEnabled(true);
        newH9DataSleepChartView.setMaxZoom(2f);
        newH9DataSleepChartView.setZoomType(ZoomType.HORIZONTAL);
        newH9DataSleepChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        newH9DataSleepChartView.startDataAnimation(2000);
        newH9DataSleepChartView.postInvalidate();

        Viewport viewport = new Viewport(newH9DataSleepChartView.getMaximumViewport());
        viewport.left = 0;
        viewport.right = 5;
        newH9DataSleepChartView.setCurrentViewport(viewport);

        //item的点击事件
        newH9DataSleepChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
//                Log.e(TAG, "----i--" + i + "--i1--" + i1);
                float value = subcolumnValue.getValue();
                if (value <= 0) return;
                double setScale = (double) WatchUtils.div((double) value, (double) 60, 1);
                String tmpSetScale = StringUtils.substringBefore(String.valueOf(setScale),".");
                if(!WatchUtils.isEmpty(tmpSetScale)){
                    newH9DataSleepShowTv.setText("" + tmpSetScale + " h");
                }

            }

            @Override
            public void onValueDeselected() {

            }
        });
    }


    int isWhat = 7;

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
                isWhat = 7;
                getAllChartData(7);
                break;
            case R.id.newH9DataMonthTv: //月的点击
                if (heartList != null) heartList.clear();
                if (stepList != null) stepList.clear();
                if (newsSleepBeanList != null) newsSleepBeanList.clear();
                clearClickTvStyle();
                newH9DataMonthTv.setTextColor(getResources().getColor(R.color.white));
                newH9DataMonthTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
                isWhat = 30;
                getAllChartData(30);
                break;
            case R.id.newH9DataYearTv:  //年的点击
                if (heartList != null) heartList.clear();
                if (stepList != null) stepList.clear();
                if (newsSleepBeanList != null) newsSleepBeanList.clear();
                clearClickTvStyle();
                newH9DataYearTv.setTextColor(getResources().getColor(R.color.white));
                newH9DataYearTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap_two);
                isWhat = 365;
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
                try {
                    if (isWhat > 0) {
                        getAllChartData(isWhat);
                    }
                    Message message = new Message();
                    message.what = 1001;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        }, 3 * 1000);

    }


    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog("loading...");
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        Log.d(TAG,"---------请求返回="+object);
        try {
            if (what == 1) {  //不是
                analysisStepData(object.toString(), daystag);
            } else if (what == 2) {    //心率
                analysisHeartData(object.toString(), daystag);
            } else if (what == 3) {    //睡眠
                analysisSleepData(object.toString(), daystag);
            }
        } catch (Exception e) {
            e.getMessage();
        }

    }


    @Override
    public void failedData(int what, Throwable e) {
        e.getMessage();
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }

    //解析步数
    private void analysisStepData(String stepdata, int weekTag) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        stepXList = new ArrayList<>();
        if (!WatchUtils.isEmpty(stepdata)) {
            try {
                jsonObject = new JSONObject(stepdata);
                if (jsonObject.getString("resultCode").equals("001")) {
                    String daydata = jsonObject.getString("day");
                    if (!WatchUtils.isEmpty(daydata) && !daydata.equals("[]")) {
                        stepList = new Gson().fromJson(daydata, new TypeToken<List<WatchDataDatyBean>>() {
                        }.getType());
//                        Log.e(TAG, "-----stepList.si==" + stepList.size());
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
                                String dateStr = stepList.get(i).getRtc();
                                String strDate = dateStr.substring(2, 7);
                                if (stepSumMap.get(strDate) != null) {
                                    sum += stepList.get(i).getStepNumber();
                                } else {
                                    sum = stepList.get(i).getStepNumber();
                                }
                                stepSumMap.put(strDate, sum);
                            }
                            tempList = new ArrayList<>();
                            tempList.clear();
                            //遍历map
                            for (Map.Entry<String, Integer> entry : stepSumMap.entrySet()) {
                                tempList.add(entry.getKey().trim());
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
                            }
                            stepTextShow.setText(getResources().getString(R.string.string_step_year));
                            showStepsChat(13);

                        } else {
                            stepTextShow.setText(getResources().getString(R.string.string_step_day));
                            mValues = new ArrayList<>();
                            //获取值
                            for (WatchDataDatyBean stepNumber : stepList) {
                                mValues.add(stepNumber.getStepNumber());    //步数的数值显示
                                String dateStr = stepNumber.getRtc();
                                String rct = dateStr.substring(5, dateStr.length());
                                stepXList.add(rct);
                            }
//                            Log.e(TAG, "----listsize--" + stepList.size());
                            showStepsChat(stepList.size());
                        }


                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //解析心率
    private void analysisHeartData(String heartData, int week) {
        heartList = new ArrayList<>();
        heartXList = new ArrayList<>();
        dayMap = new HashMap<>();
        ValueCount = 0;
        if (!WatchUtils.isEmpty(heartData)) {
            try {
                JSONObject heartJson = new JSONObject(heartData);
                if (heartJson.getString("resultCode").equals("001")) {
                    String heartRate = heartJson.getString("heartRate");
//                    W30BasicUtils.e(TAG, "----heartRate---" + heartRate);
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
                                heartXList.add(avgHeart.getRtc().substring(5, avgHeart.getRtc().length()));
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
        sleepVlaues = new ArrayList<>();
        newsSleepBeanList = new ArrayList<>();
        sleepXList = new ArrayList<>();
        if (!WatchUtils.isEmpty(sleepdata)) {
            try {
                JSONObject sleepJson = new JSONObject(sleepdata);
                if (sleepJson.getString("resultCode").equals("001")) {
                    String sleepData = sleepJson.getString("sleepData");
                    if (sleepData != null && !sleepData.equals("[]")) {
                        newsSleepBeanList = new Gson().fromJson(sleepData, new TypeToken<List<NewsSleepBean>>() {
                        }.getType());
                        if (daystag == 365) {    //年
                            sumSleepMap = new HashMap<>();
                            int sleepSum = 0;
                            for (int i = 0; i < newsSleepBeanList.size(); i++) {
                                String strDate = newsSleepBeanList.get(i).getRtc().substring(2, 7);
                                if (sumSleepMap.get(strDate) != null) {
                                    int shallowSleep = newsSleepBeanList.get(i).getShallowSleep();
                                    int deepSleep = newsSleepBeanList.get(i).getDeepSleep();
                                    sleepSum += (shallowSleep + deepSleep);
                                    //W30BasicUtils.e(TAG, "----深睡浅睡值----" + sleepSum);
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
                                //W30BasicUtils.e(TAG, "---睡眠MAP--=" + maps.getKey().trim());
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
//                                Log.e(TAG, "---睡眠Value--=" + sumSleepMap.get(tempSleepList.get(k)) + "-==-睡眠Data-=" + tempSleepList.get(k));
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
                                sleepXList.add(sleepBean.getRtc().substring(5, sleepBean.getRtc().length()));
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
