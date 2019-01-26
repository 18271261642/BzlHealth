package com.bozlun.health.android.b31.bpoxy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.view.CusExpandableListView;
import com.bozlun.health.android.b31.GlossaryExpandableListAdapter;
import com.bozlun.health.android.b31.bpoxy.enums.EnumGlossary;
import com.bozlun.health.android.b31.bpoxy.util.VpSpo2hUtil;
import com.bozlun.health.android.b31.glossary.AGlossary;
import com.bozlun.health.android.b31.glossary.BeathBreathGlossary;
import com.bozlun.health.android.b31.glossary.BreathGlossary;
import com.bozlun.health.android.b31.glossary.HeartGlossary;
import com.bozlun.health.android.b31.glossary.LowOxgenGlossary;
import com.bozlun.health.android.b31.glossary.LowRemainGlossary;
import com.bozlun.health.android.b31.glossary.OsahsGlossary;
import com.bozlun.health.android.b31.glossary.OxgenGlossary;
import com.bozlun.health.android.b31.glossary.RateVariGlossary;
import com.bozlun.health.android.b31.glossary.SleepBreathBreakGlossary;
import com.bozlun.health.android.b31.glossary.SleepGlossary;
import com.bozlun.health.android.b31.model.B31Spo2hBean;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Constant;
import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.veepoo.protocol.model.datas.Spo2hOriginData;
import com.veepoo.protocol.model.enums.ESpo2hDataType;
import com.veepoo.protocol.util.Spo2hOriginUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bozlun.health.android.b31.bpoxy.enums.EnumGlossary.HEART;

/**
 * Created by Admin
 * Date 2019/1/19
 */
public class ShowB31SingleDescActivity extends WatchBaseActivity {

    private static final String TAG = "ShowB31SingleDescActivi";


    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.commArrowDate)
    TextView commArrowDate;

    //心脏负荷图表
    @BindView(R.id.single_block_chartview_heart)
    LineChart singleBlockChartviewHeart;
    //心脏负荷布局
    @BindView(R.id.b31SingleBlockHeart)
    LinearLayout b31SingleBlockHeart;

    //睡眠活动图表
    @BindView(R.id.single_block_chartview_sleep)
    LineChart singleBlockChartviewSleep;
    //睡眠活动布局
    @BindView(R.id.b31SingleBlockSleep)
    LinearLayout b31SingleBlockSleep;

    //呼吸率图表
    @BindView(R.id.single_block_chartview_breath)
    LineChart singleBlockChartviewBreath;
    //呼吸率布局
    @BindView(R.id.b31SingleBlockBreath)
    LinearLayout b31SingleBlockBreath;


    //低氧时间图表
    @BindView(R.id.single_block_chartview_lowspo2h)
    LineChart singleBlockChartviewLowspo2h;
    //低氧时间布局
    @BindView(R.id.b31SingleBlockLowspo2h)
    LinearLayout b31SingleBlockLowspo2h;


    //血氧呼吸暂停图表
    @BindView(R.id.singleBlockChartviewSpo2h)
    LineChart singleBlockChartviewSpo2h;
    //血氧呼吸暂停图表布局
    @BindView(R.id.single_block_spo2h)
    LinearLayout singleBlockSpo2h;
    //列表
    @BindView(R.id.singleSpo2DetailRecyclerView)
    RecyclerView singleSpo2DetailRecyclerView;
    //名词解释的view
    @BindView(R.id.singleDescExpandView)
    CusExpandableListView singleDescExpandView;
    //需要的数据源
    private List<Map<String, Float>> resultListMap;
    //adapter
    private ShowSpo2DetailAdapter showSpo2DetailAdapter;

    private String currDay;
    private List<Spo2hOriginData> list = new ArrayList<>();
    private Gson gson = new Gson();
    VpSpo2hUtil vpSpo2hUtil;
    private Spo2hOriginUtil spo2hOriginUtil = null;

    ESpo2hDataType eSpo2hDataType = null;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            closeLoadingDialog();

            if (msg.what == 1001) {
                initSpo2hUtil();
                vpSpo2hUtil.setData(list);
                vpSpo2hUtil.showAllChartView();
                //showSpo2Desc(list);
                showResultDesc(list, 1);
                Log.e(TAG, "-----");

            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_b31_single_desc);
        ButterKnife.bind(this);


        initViews();
        currDay = getIntent().getStringExtra(Constant.DETAIL_DATE);
        showVisableView();
        readSpo2Data(currDay);
        initSpo2hUtil();


    }

    private void showVisableView() {
        int type = getIntent().getIntExtra("type", 0);
        eSpo2hDataType = getSpo2Type(type);

        AGlossary glossary = getGlossaryOsahs(getApplicationContext(), HEART.getValue());
        GlossaryExpandableListAdapter adapter
                = new GlossaryExpandableListAdapter(getApplicationContext(), glossary);
        singleDescExpandView.setGroupIndicator(null);// 将控件默认的左边箭头去掉，
        singleDescExpandView.setAdapter(adapter);
        expandList();
    }

    private void expandList() {
        int groupCount = singleDescExpandView.getCount();
        for (int i = 0; i < groupCount; i++) {
            singleDescExpandView.expandGroup(i);
        }
        singleDescExpandView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText("分析数据");
        initTipTv();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        singleSpo2DetailRecyclerView.setNestedScrollingEnabled(false);
        singleSpo2DetailRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        singleSpo2DetailRecyclerView.setLayoutManager(linearLayoutManager);
        resultListMap = new ArrayList<>();
        showSpo2DetailAdapter = new ShowSpo2DetailAdapter(resultListMap, ShowB31SingleDescActivity.this);
        singleSpo2DetailRecyclerView.setAdapter(showSpo2DetailAdapter);


    }


    /**
     * 设置相关属性
     */
    private void initSpo2hUtil() {
        vpSpo2hUtil = new VpSpo2hUtil();
        vpSpo2hUtil.setLinearChart(singleBlockChartviewSpo2h, singleBlockChartviewHeart,
                singleBlockChartviewSleep, singleBlockChartviewBreath, singleBlockChartviewLowspo2h);
        vpSpo2hUtil.setMarkView(getApplicationContext(), R.layout.vpspo2h_markerview);
        vpSpo2hUtil.setModelIs24(false);
    }


    //读取本地保存的数据
    private void readSpo2Data(final String currDay) {
        commArrowDate.setText(currDay);
        list.clear();
        showLoadingDialog("Loading...");
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                //查询保存的数据
                String whereStr = "bleMac = ? and dateStr = ?";
                String bleMac = WatchUtils.getSherpBleMac(ShowB31SingleDescActivity.this);
                List<B31Spo2hBean> spo2hBeanList = DataSupport.where(whereStr, bleMac, currDay).find(B31Spo2hBean.class);
                if (spo2hBeanList == null || spo2hBeanList.isEmpty()) {
                    Message message = handler.obtainMessage();
                    message.what = 1001;
                    message.obj = list;
                    handler.sendMessage(message);
                    return;
                }
                for (B31Spo2hBean hBean : spo2hBeanList) {
                    list.add(gson.fromJson(hBean.getSpo2hOriginData(), Spo2hOriginData.class));
                }

                Message message = handler.obtainMessage();
                message.what = 1001;
                message.obj = list;
                handler.sendMessage(message);


            }
        };
        thread.start();

    }


    @OnClick({R.id.commentB30BackImg, R.id.commArrowLeft,
            R.id.commArrowRight})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:    //返回按钮
                finish();
                break;
            case R.id.commArrowLeft:    //前一天
                changeCurrDay(true);
                break;
            case R.id.commArrowRight:   //后一天
                changeCurrDay(false);
                break;
        }
    }

    //日期切换
    private void changeCurrDay(boolean isDay) {
        String date = WatchUtils.obtainAroundDate(currDay, isDay);
        if (date.equals(currDay) || date.isEmpty()) {
            return;// 空数据,或者大于今天的数据就别切了
        }
        currDay = date;
        readSpo2Data(currDay);

    }


    //展示结果
    private void showResultDesc(List<Spo2hOriginData> resultList, int tag) {
        resultListMap.clear();
        spo2hOriginUtil = new Spo2hOriginUtil(resultList);
        //获取每10分钟的数据
        List<Map<String, Float>> tenMinuteData = spo2hOriginUtil.getTenMinuteData(getSpo2Type(tag));
        resultListMap.addAll(tenMinuteData);
        showSpo2DetailAdapter.setSpowTag(tag);
        showSpo2DetailAdapter.notifyDataSetChanged();

    }

    private void initTipTv() {
        TextView tipSpo2h = (TextView) findViewById(R.id.block_bottom_tip_spo2h);
        TextView tipHeart = (TextView) findViewById(R.id.block_bottom_tip_heart);
        TextView tipSleep = (TextView) findViewById(R.id.block_bottom_tip_sleep);
        TextView tipBeath = (TextView) findViewById(R.id.block_bottom_tip_beath);
        TextView tipLowsp = (TextView) findViewById(R.id.block_bottom_tip_lowspo2h);

        String stateNormal = getResources().getString(R.string.vpspo2h_state_normal);
        String stateLittle = getResources().getString(R.string.vpspo2h_state_little);
        String stateCalm = getResources().getString(R.string.vpspo2h_state_calm);
        String stateError = getResources().getString(R.string.vpspo2h_state_error);
        String stateMulSport = getResources().getString(R.string.vpspo2h_state_mulsport);
        String stateMulMulSport = getResources().getString(R.string.vpspo2h_state_mulmulsport);

        tipSpo2h.setText("[95-99]" + stateNormal);
        tipHeart.setText("[0-20]" + stateLittle + "\t\t[21-40]" + stateNormal + "\t\t[≥41]" + stateError);
        tipSleep.setText("[0-20]" + stateCalm + "\t\t[21-50]" + stateMulSport + "\t\t[51-80]" + stateMulMulSport);
        tipBeath.setText("[0-26]" + stateNormal + "\t\t[27-50]" + stateError);
        tipLowsp.setText("[0-20]" + stateNormal + "\t\t[21-300]" + stateError);
    }


    private ESpo2hDataType getSpo2Type(int tag) {
        Log.e(TAG, "-------tag=" + tag);
        ESpo2hDataType eSpo2hDataType = null;
        switch (tag) {
            case 0:     //血氧，呼吸暂停
                eSpo2hDataType = ESpo2hDataType.TYPE_SPO2H;
                singleBlockSpo2h.setVisibility(View.VISIBLE);
                break;
            case 1:     //心脏负荷
                eSpo2hDataType = ESpo2hDataType.TYPE_HEART;
                b31SingleBlockHeart.setVisibility(View.VISIBLE);
                break;
            case 2:     //睡眠活动
                eSpo2hDataType = ESpo2hDataType.TYPE_SLEEP;
                b31SingleBlockSleep.setVisibility(View.VISIBLE);
                break;
            case 3:     //呼吸率
                eSpo2hDataType = ESpo2hDataType.TYPE_BREATH;
                b31SingleBlockBreath.setVisibility(View.VISIBLE);
                break;
            case 4:     //低氧时间
                eSpo2hDataType = ESpo2hDataType.TYPE_LOWSPO2H;
                b31SingleBlockLowspo2h.setVisibility(View.VISIBLE);
                break;
        }
        return eSpo2hDataType;
    }


    //名词解释的
    private AGlossary getGlossaryOsahs(Context context, int value) {
        EnumGlossary enumGlossary = EnumGlossary.getEnum(value);
        switch (enumGlossary) {
            case OSHAHS:
                return new OsahsGlossary(context);
            case BREATHBREAK:
                return new BeathBreathGlossary(context);
            case LOWOXGEN:  //低氧唤醒
                return new LowOxgenGlossary(context);
            case HEART:
                return new HeartGlossary(context);
            case RATEVARABLE:
                return new RateVariGlossary(context);
            case SLEEP:
                return new SleepGlossary(context);
            case LOWREAMIN:
                return new LowRemainGlossary(context);
            case SLEEPBREATHBREAKTIP:
                return new SleepBreathBreakGlossary(context);
            case BREATH:
                return new BreathGlossary(context);
            case OXGEN:
                return new OxgenGlossary(context);
        }
        return new OsahsGlossary(context);
    }
}
