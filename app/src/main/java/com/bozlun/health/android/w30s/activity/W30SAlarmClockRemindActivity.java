package com.bozlun.health.android.w30s.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.w30s.bean.W30SAlarmClockBean;
import com.bozlun.health.android.w30s.utils.W30BasicUtils;
import com.bozlun.health.android.R;
import com.suchengkeji.android.w30sblelibrary.bean.W30S_AlarmInfo;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @aboutContent: 闹钟设置界面
 * @author： An
 * @crateTime: 2018/3/17 16:14
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class W30SAlarmClockRemindActivity extends WatchBaseActivity
        implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "W30SAlarmClockRemindAct";
    private final int HANDLER_MES = 201314;
    private final int HANDLER_TIME = 1000;
    private final int REQUEST_ALARM_CLOCK_NEW = 1;// 新建闹钟的requestCode
    private final int REQUEST_ALARM_CLOCK_EDIT = 2;// 修改闹钟的requestCode
    @BindView(R.id.bar_titles)
    TextView barTitles;
    @BindView(R.id.rela_view)
    RelativeLayout relaView;
    @BindView(R.id.alarm_list)
    ListView alarmList;
    @BindView(R.id.image_add_remind)
    ImageView imageAddRemind;
    @BindView(R.id.text_w30s_alarm_number)
    TextView textW30sAlarmNumber;
    private MyAdapter myAdapter;
    private int[] weekArray = new int[]{1, 2, 4, 8, 16, 32, 64};


    private List<W30S_AlarmInfo> mAliarmList;  //闹钟集合
    private List<W30S_AlarmInfo> tempmAliarmList = new ArrayList<>();
    private List<W30SAlarmClockBean> mAlarmClock;       //列表展现所有闹钟集合
    private W30SAlarmClockBean w30SAlarmClockBean;//数据库实体类


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.w30s_alarm_clock_layout);
        ButterKnife.bind(this);
        barTitles.setText(getResources().getString(R.string.alarmclock));
        initViews();
        textW30sAlarmNumber.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }


    private void initViews() {
        mAlarmClock = DataSupport.findAll(W30SAlarmClockBean.class);
        Log.e(TAG, "----initViewsmAlarmClock-=" + mAlarmClock.size() + mAlarmClock.toString());
        if (mAlarmClock == null || mAlarmClock.size() <= 0) {
            relaView.setVisibility(View.VISIBLE);
            alarmList.setVisibility(View.GONE);
        }
        if (mAlarmClock.size() >= 5) {
            imageAddRemind.setVisibility(View.GONE);
        } else {
            imageAddRemind.setVisibility(View.VISIBLE);
        }
        // mAliarmList = getAliarmList();
        myAdapter = new MyAdapter(mAlarmClock);
        alarmList.setAdapter(myAdapter);
        alarmList.setOnItemLongClickListener(this);
        alarmList.setOnItemClickListener(this);
        Log.d("====闹钟数======", mAlarmClock.size() + "===========" + mAlarmClock.size());
    }


    public List<W30S_AlarmInfo> getAliarmList() {
        if (mAlarmClock == null) {
            mAlarmClock = DataSupport.findAll(W30SAlarmClockBean.class);
        }
        if (mAlarmClock.size() >= 5) {
            imageAddRemind.setVisibility(View.GONE);
        } else {
            imageAddRemind.setVisibility(View.VISIBLE);
        }
        Log.e(TAG, "-=----mALack=" + mAlarmClock.size() + "-=" + mAlarmClock.toString());
        for (int w = 0; w < mAlarmClock.size(); w++) {
            Log.e(TAG, "---mAlarmClock----11---" + mAlarmClock.get(w).getAlarmInfoList());
            if (mAlarmClock.get(w).getAlarmInfoList() != null) {
                tempmAliarmList.addAll(mAlarmClock.get(w).getAlarmInfoList());
            }
        }
        if (mAliarmList == null) {
            mAliarmList = new ArrayList<>();
        }
        mAliarmList.addAll(tempmAliarmList);
        return mAliarmList;
    }


    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == HANDLER_MES) {
                mAlarmClock = DataSupport.findAll(W30SAlarmClockBean.class);
                if (mAlarmClock == null || mAlarmClock.size() <= 0) {
                    relaView.setVisibility(View.VISIBLE);
                    alarmList.setVisibility(View.GONE);
                } else {
                    relaView.setVisibility(View.GONE);
                    alarmList.setVisibility(View.VISIBLE);
                    Log.d("====闹钟数==333====", mAlarmClock.size() + "");
                }
                if (mAlarmClock.size() >= 5) {
                    imageAddRemind.setVisibility(View.GONE);
                } else {
                    imageAddRemind.setVisibility(View.VISIBLE);
                }
                myAdapter.notifyDataSetChanged();
                closeLoadingDialog();
                mHandler.removeMessages(HANDLER_MES);
            }
            return false;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {


            Log.d("==返回=", requestCode + "===" + resultCode);
            if (data == null) {
                return;
            }
            int h = data.getIntExtra("h", 12);
            int m = data.getIntExtra("m", 12);
            int week = data.getIntExtra("c", 0);
            // int week = 127;
            int alarmClockMsg_h = (int) SharedPreferencesUtils.getParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_H", 12);
            int alarmClockMsg_m = (int) SharedPreferencesUtils.getParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_M", 12);
            int alarmClockMsg_week = (int) SharedPreferencesUtils.getParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_WEEK", 127);

            if (alarmClockMsg_h == h && alarmClockMsg_m == m && alarmClockMsg_week == week) {
                Toast.makeText(this, getResources().getString(R.string.string_clock_setting) + "error", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferencesUtils.setParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_H", h);
            SharedPreferencesUtils.setParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_M", m);
            SharedPreferencesUtils.setParam(W30SAlarmClockRemindActivity.this, "alarmClockMsg_WEEK", week);


            String[] sateString = {"1", "0", "0", "0", "0", "0", "0", "0"};
            //周期
            if ((week & weekArray[1]) == 2) { //周一
                sateString[1] = "1";
            }
            if ((week & weekArray[2]) == 4) { //周二
                sateString[2] = "1";
            }
            if ((week & weekArray[3]) == 8) {  //周三
                sateString[3] = "1";
            }
            if ((week & weekArray[4]) == 16) {  //周四
                sateString[4] = "1";
            }
            if ((week & weekArray[5]) == 32) {  //周五
                sateString[5] = "1";
            }
            if ((week & weekArray[6]) == 64) {  //周六
                sateString[6] = "1";
            }
            if ((week & weekArray[0]) == 1) {   //周日
                sateString[7] = "1";
            }

            switch (requestCode) {
                case REQUEST_ALARM_CLOCK_NEW://添加新的闹钟
                    int ID = 1;
                    if (mAlarmClock.size() > 0) {
                        ID = (mAlarmClock.get(mAlarmClock.size() - 1).getId()) + 1;
                    }

                    String stringData = "".trim();
                    String stringDatas = Arrays.toString(sateString);
                    Log.d("===AAA==stringDatas=", stringDatas);
                    String stringDatas2 = stringDatas.substring(1, stringDatas.length() - 1);
                    Log.d("===AAA=stringDatas2=", stringDatas);
                    String[] split = stringDatas2.trim().split("[,]");
                    for (int i = 0; i < split.length; i++) {
                        stringData += split[i].trim();
                    }
                    //二进制转十进制
                    int integer = Integer.parseInt(W30BasicUtils.toD(stringData, 2));
                    Log.d("===AAAA====integer=", stringData + "====" + integer);

                    W30S_AlarmInfo alarmInfo = new W30S_AlarmInfo();
                    alarmInfo.setAlarmId(ID);
                    alarmInfo.setAlarmtHour(h);
                    alarmInfo.setAlarmtMin(m);
                    alarmInfo.setAlarmtData(integer);
                    if (mAliarmList != null) {
                        mAliarmList.add(alarmInfo);
                    } else {
//                    mAliarmList = new ArrayList<>();
                        mAliarmList = getAliarmList();
                        mAliarmList.add(alarmInfo);
                    }
                    w30SAlarmClockBean = new W30SAlarmClockBean();
                    w30SAlarmClockBean.setId(ID);
                    w30SAlarmClockBean.setHour(h);
                    w30SAlarmClockBean.setMin(m);
                    w30SAlarmClockBean.setStatus(1);
                    w30SAlarmClockBean.setDatas(integer);
                    w30SAlarmClockBean.setAlarmWeek(week);
                    Log.e(TAG, "----mAliarmList-=" + ID + h + m + 1 + integer + week + mAliarmList.size() + mAliarmList.toString());
                    w30SAlarmClockBean.setAlarmInfoList(mAliarmList);
                    Log.d(TAG, "===========" + w30SAlarmClockBean.toString() + "==========" + mAliarmList.toString() + mAliarmList.size());
                    w30SAlarmClockBean.save();

                    MyApp.getInstance().getmW30SBLEManage().setAlarm(mAliarmList);
                    break;

                case REQUEST_ALARM_CLOCK_EDIT://修改旧的闹钟
                    //二进制转十进制
                    String stringDataChange = "".trim();
                    String stringDataChanges = Arrays.toString(sateString);
                    String stringDataChanges2 = stringDataChanges.substring(1, stringDataChanges.length() - 1);
                    String[] splitchange = stringDataChanges2.trim().split("[,]");
                    for (int i = 0; i < splitchange.length; i++) {
                        stringDataChange += splitchange[i].trim();
                    }
                    Log.d("===AAAA=2=", stringDataChange);
                    int integerChange = Integer.parseInt(W30BasicUtils.toD(stringDataChange, 2));
                    int ids = Integer.parseInt(data.getStringExtra("ids"));
                    Log.d("===修改的==", ids + "===" + h + "===" + m + "===" + week + "===" + stringDataChange + "===" + integerChange + "===" + Integer.valueOf(stringDataChange, 2));
                    W30S_AlarmInfo alarmInfoChange = new W30S_AlarmInfo();
                    alarmInfoChange.setAlarmId(ids);
                    alarmInfoChange.setAlarmtHour(h);
                    alarmInfoChange.setAlarmtMin(m);
                    alarmInfoChange.setAlarmtData(integerChange);
                    if (mAliarmList != null) {
                        mAliarmList.add(alarmInfoChange);
                    } else {
                        mAliarmList = getAliarmList();
                        mAliarmList.add(alarmInfoChange);
                    }


                    w30SAlarmClockBean = DataSupport.find(W30SAlarmClockBean.class, ids);
                    w30SAlarmClockBean.setId(ids);
                    w30SAlarmClockBean.setHour(h);
                    w30SAlarmClockBean.setMin(m);
                    w30SAlarmClockBean.setStatus(1);
                    w30SAlarmClockBean.setDatas(integerChange);
                    w30SAlarmClockBean.setAlarmWeek(week);
                    w30SAlarmClockBean.setAlarmInfoList(mAliarmList);
                    w30SAlarmClockBean.save();
                    Log.e(TAG, "------111=" + w30SAlarmClockBean.toString());
                    Log.e("=======", "--改---" + h + ":" + m + "===" + week + "==" + ids);//B18iUtils.getCycle()
                    //  w30SAlarmClockBean1.updateAll("id=? and hour=? and min=? and datas=?", ids + "", h + "", m + "", integer1 + "");
                    Log.e(TAG, "------修改查询=" + DataSupport.find(W30SAlarmClockBean.class, ids).toString());

                    MyApp.getInstance().getmW30SBLEManage().setAlarm(mAliarmList);
                    break;
            }

            mAlarmClock = null;
            showLoadingDialog(getResources().getString(R.string.dlog));
            mHandler.sendEmptyMessageDelayed(HANDLER_MES, HANDLER_TIME);

        } catch (Exception e) {
            e.getMessage();
        }

    }

    @OnClick({R.id.image_back, R.id.image_add_remind})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.image_add_remind:
                Intent intent = new Intent(W30SAlarmClockRemindActivity.this, W30STimePicker.class);
                intent.putExtra("type", "new");
                startActivityForResult(intent, 1);
                break;
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        try {
            new MaterialDialog.Builder(this)
                    .title(getResources().getString(R.string.prompt))
                    .content(getResources().getString(R.string.deleda))
                    .positiveText(getResources().getString(R.string.confirm))
                    .negativeText(getResources().getString(R.string.cancle))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Log.d("=======", "删除成功");
                            mAlarmClock.get(position).delete();
                            mAlarmClock.remove(position);
                            List<W30S_AlarmInfo> w30S_alarmInfos = new ArrayList<W30S_AlarmInfo>();
                            for (int i = 0; i < mAlarmClock.size(); i++) {
                                W30S_AlarmInfo w30S_alarmInfo = mAlarmClock.get(i).w30AlarmInfoChange();
                                w30S_alarmInfos.add(w30S_alarmInfo);
                            }
//
//                        int alarmtData = mAlarmClock.get(position).getDatas();
//                        String alarmtDataString = Integer.toBinaryString(alarmtData);
//                        String s = "0" + alarmtDataString.substring(1, alarmtDataString.length());
//                        //二进制转十进制
//                        Integer integer = Integer.valueOf(s, 2);
//                        W30S_AlarmInfo alarmInfo = new W30S_AlarmInfo();
//                        alarmInfo.setAlarmId(mAlarmClock.get(position).getId());
//                        alarmInfo.setAlarmtHour(mAlarmClock.get(position).getHour());
//                        alarmInfo.setAlarmtMin(mAlarmClock.get(position).getMin());
//                        alarmInfo.setAlarmtData(integer);
//                        if (mAliarmList != null) {
//                            mAliarmList.add(alarmInfo);
//                        } else {
//                            mAliarmList = getAliarmList();
//                            mAliarmList.add(alarmInfo);
//                        }
                            MyApp.getInstance().getmW30SBLEManage().setAlarm(w30S_alarmInfos);
//                        DataSupport.deleteAll(W30SAlarmClockBean.class, "id=?", mAlarmClock.get(position).getId() + "");
                            myAdapter.deleteData(position);
                            mAlarmClock = null;
                            showLoadingDialog(getResources().getString(R.string.dlog));
                            mHandler.sendEmptyMessageDelayed(HANDLER_MES, HANDLER_TIME);
                        }
                    }).show();
        }catch (Exception e){
            e.getMessage();
        }


        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(W30SAlarmClockRemindActivity.this, W30STimePicker.class);
        intent.putExtra("type", "change");
        intent.putExtra("ids", String.valueOf(mAlarmClock.get(position).getId()));
        intent.putExtra("hour", String.valueOf(mAlarmClock.get(position).getHour()));
        intent.putExtra("min", String.valueOf(mAlarmClock.get(position).getMin()));
        intent.putExtra("datas", String.valueOf(mAlarmClock.get(position).getDatas()));
        intent.putExtra("alarmWeek", mAlarmClock.get(position).getAlarmWeek());
        // 开启编辑闹钟界面
        startActivityForResult(intent, REQUEST_ALARM_CLOCK_EDIT);
        // 启动移动进入效果动画
        overridePendingTransition(R.anim.move_in_bottom, 0);
    }


    class MyAdapter extends BaseAdapter {
        List<W30SAlarmClockBean> w30SAlarmClockBeanList;

        public MyAdapter(List<W30SAlarmClockBean> alarmInfoList) {
            this.w30SAlarmClockBeanList = alarmInfoList;
        }

        public void deleteData(int postion) {
            w30SAlarmClockBeanList.remove(postion);
        }

        @Override
        public int getCount() {
            return w30SAlarmClockBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return w30SAlarmClockBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.b18i_list_alarm_clock_item, null);
                initView(viewHolder, convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            int id = w30SAlarmClockBeanList.get(position).getId();
            int hour = w30SAlarmClockBeanList.get(position).getHour();
            int min = w30SAlarmClockBeanList.get(position).getMin();
            int alarmtData = w30SAlarmClockBeanList.get(position).getDatas();
            int status = w30SAlarmClockBeanList.get(position).getStatus();
            String h = "00";
            String m = "00";
            if (hour > 9) {
                h = String.valueOf(hour);
            } else {
                h = "0" + hour;
            }
            if (min > 9) {
                m = String.valueOf(min);
            } else {
                m = "0" + min;
            }
            viewHolder.hour.setText(h + ":" + m);
            //十进制转成二进制
            String alarmtDataString = Integer.toBinaryString(alarmtData);


            if (status == 1) {
                viewHolder.switchA.setChecked(true);
            } else {
                viewHolder.switchA.setChecked(false);
            }
            String substring = alarmtDataString.substring(1, alarmtDataString.length());
            //

            setCycle(viewHolder, w30SAlarmClockBeanList.get(position).getAlarmWeek());
            Log.d("-----闹钟信息---111--", id + "===" + substring + "===" + alarmtDataString + "===" + alarmtData + alarmtDataString.substring(0, 1).equals("1"));
//            initListenter(viewHolder, id, hour, min, alarmtData, position, w30SAlarmClockBeanList.get(position).getAlarmWeek());
            initListenter(viewHolder, w30SAlarmClockBeanList.get(position).getAlarmWeek(), id, hour, min, alarmtData, position);
            Log.d("-----闹钟信息-----", id + "==" + hour + "==" + min + "==" + alarmtData + "==" + position);
            return convertView;
        }

        private void initListenter(final ViewHolder viewHolder, final int week, final int id, final int hour, final int min, final int alarmtData, final int position) {
            Log.d(TAG, "=====1111===状态======" + week + "==" + id + "==" + hour + "==" + hour + "===" + min + "===" + alarmtData + "===" + position);


            //状态改变
            viewHolder.switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "========状态==333====" + week + "==" + id + "==" + hour + "==" + hour + "===" + min + "===" + alarmtData + "===" + position);
                    Log.d("-----状态改变-----", isChecked + "");
                    try {


                        if (isChecked) {
                            W30S_AlarmInfo alarmInfo = new W30S_AlarmInfo();
                            alarmInfo.setAlarmId(id);
                            alarmInfo.setAlarmtHour(hour);
                            alarmInfo.setAlarmtMin(min);
                            //十进制转换二进制
                            String alarmtDataString = Integer.toBinaryString(alarmtData);
                            // 1 1111111
                            String s = "1" + alarmtDataString.substring(1, alarmtDataString.length());
                            //二进制转十进制
                            int integer = Integer.valueOf(W30BasicUtils.toD(s, 2));
                            Log.d("-----打开-----", s + "===" + integer);
                            alarmInfo.setAlarmtData(integer);
                            if (mAliarmList != null) {
                                mAliarmList.add(alarmInfo);
                            } else {
                                mAliarmList = getAliarmList();
                                mAliarmList.add(alarmInfo);
                            }


                            w30SAlarmClockBean = DataSupport.find(W30SAlarmClockBean.class, id);
                            w30SAlarmClockBean.setId(id);
                            w30SAlarmClockBean.setHour(hour);
                            w30SAlarmClockBean.setMin(min);
                            w30SAlarmClockBean.setStatus(1);
                            w30SAlarmClockBean.setDatas(integer);
                            w30SAlarmClockBean.setAlarmWeek(week);
                            w30SAlarmClockBean.setAlarmInfoList(mAliarmList);
                            w30SAlarmClockBean.save();

                            MyApp.getInstance().getmW30SBLEManage().setAlarm(mAliarmList);
                            Log.e(TAG, "-----查询=" + DataSupport.find(W30SAlarmClockBean.class, id).toString());
                        } else {
                            //List<W30S_AlarmInfo> w30S_alarmInfos = new ArrayList<>();
                            W30S_AlarmInfo alarmInfo = new W30S_AlarmInfo();
                            alarmInfo.setAlarmId(id);
                            alarmInfo.setAlarmtHour(hour);
                            alarmInfo.setAlarmtMin(min);
                            //十进制转换二进制
                            String alarmtDataString = Integer.toBinaryString(alarmtData);
                            String s = "0" + alarmtDataString.substring(1, alarmtDataString.length());
                            //二进制转十进制
                            int integer = Integer.valueOf(W30BasicUtils.toD(s, 2));
                            Log.d("-----关闭-----", s + "===" + integer);
                            alarmInfo.setAlarmtData(integer);
                            if (mAliarmList != null) {
                                mAliarmList.add(alarmInfo);
                            } else {
                                mAliarmList = getAliarmList();
                                mAliarmList.add(alarmInfo);
                            }
                            w30SAlarmClockBean = DataSupport.find(W30SAlarmClockBean.class, id);
                            // Log.d("-----w30SAlarmClockBean-----", w30SAlarmClockBean.getStatus() + "");
                            if (w30SAlarmClockBean != null) {
                                w30SAlarmClockBean.setId(id);
                                w30SAlarmClockBean.setHour(hour);
                                w30SAlarmClockBean.setMin(min);
                                w30SAlarmClockBean.setStatus(0);
                                w30SAlarmClockBean.setDatas(integer);
                                w30SAlarmClockBean.setAlarmWeek(week);
                                w30SAlarmClockBean.setAlarmInfoList(mAliarmList);
                                w30SAlarmClockBean.save();
                                MyApp.getInstance().getmW30SBLEManage().setAlarm(mAliarmList);
                            }


                        }


                    } catch (Exception e) {
                        e.getMessage();
                    }

                }
            });


        }
    }


    private void initView(ViewHolder viewHolder, View convertView) {
        viewHolder.hour = (TextView) convertView.findViewById(R.id.text_hour);
        //viewHolder.min = (TextView) convertView.findViewById(R.id.text_min);
        viewHolder.type = (TextView) convertView.findViewById(R.id.text_type);
        viewHolder.type1 = (TextView) convertView.findViewById(R.id.text_type_one);
        viewHolder.type2 = (TextView) convertView.findViewById(R.id.text_type_two);
        viewHolder.type3 = (TextView) convertView.findViewById(R.id.text_type_three);
        viewHolder.type4 = (TextView) convertView.findViewById(R.id.text_type_four);
        viewHolder.type5 = (TextView) convertView.findViewById(R.id.text_type_five);
        viewHolder.type6 = (TextView) convertView.findViewById(R.id.text_type_six);
        viewHolder.type7 = (TextView) convertView.findViewById(R.id.text_type_senven);
        //viewHolder.imageType = (ImageView) convertView.findViewById(R.id.image_type);
        viewHolder.switchA = (Switch) convertView.findViewById(R.id.switch_alarm);
        viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.layout_item);

        viewHolder.type.setVisibility(View.GONE);
        viewHolder.type1.setVisibility(View.GONE);
        viewHolder.type2.setVisibility(View.GONE);
        viewHolder.type3.setVisibility(View.GONE);
        viewHolder.type4.setVisibility(View.GONE);
        viewHolder.type5.setVisibility(View.GONE);
        viewHolder.type6.setVisibility(View.GONE);
        viewHolder.type7.setVisibility(View.GONE);
    }

    /**
     * 循环模式
     *
     * @param viewHolder
     */
    private void setCycle(ViewHolder viewHolder, int week) {

        Log.d("=======viewHolder======", week + "");
        if (week == 127) {
            viewHolder.type.setVisibility(View.VISIBLE);
            //viewHolder.type7.setText(getResources().getString(R.string.sunday));
            viewHolder.type.setText(getResources().getString(R.string.every_time));
            return;
        }
        if ((week & weekArray[0]) == 1) {   //周日
            viewHolder.type7.setVisibility(View.VISIBLE);
            //viewHolder.type7.setText(getResources().getString(R.string.sunday));
            viewHolder.type7.setText(getResources().getString(R.string.sunday));
        }
        if ((week & weekArray[1]) == 2) { //周一
            viewHolder.type1.setVisibility(View.VISIBLE);
            //viewHolder.type1.setText(getResources().getString(R.string.monday));
            viewHolder.type1.setText(getResources().getString(R.string.monday));
        }
        if ((week & weekArray[2]) == 4) { //周二

            viewHolder.type2.setVisibility(View.VISIBLE);
            //viewHolder.type2.setText(getResources().getString(R.string.tuesday));
            viewHolder.type2.setText(getResources().getString(R.string.tuesday));

        }
        if ((week & weekArray[3]) == 8) {  //周三
            viewHolder.type3.setVisibility(View.VISIBLE);
            //viewHolder.type3.setText(getResources().getString(R.string.wednesday));
            viewHolder.type3.setText(getResources().getString(R.string.wednesday));

        }
        if ((week & weekArray[4]) == 16) {  //周四

            viewHolder.type4.setVisibility(View.VISIBLE);
            //viewHolder.type4.setText(getResources().getString(R.string.thursday));
            viewHolder.type4.setText(getResources().getString(R.string.thursday));


        }
        if ((week & weekArray[5]) == 32) {  //周五
            viewHolder.type5.setVisibility(View.VISIBLE);
            //viewHolder.type5.setText(getResources().getString(R.string.friday));
            viewHolder.type5.setText(getResources().getString(R.string.friday));
        }
        if ((week & weekArray[6]) == 64) {  //周六
            viewHolder.type6.setVisibility(View.VISIBLE);
            //viewHolder.type6.setText(getResources().getString(R.string.saturday));
            viewHolder.type6.setText(getResources().getString(R.string.saturday));
        }


//        char[] strChar = cycle.toCharArray();
////        String result = "";
//        for (int i = 0; i < strChar.length; i++) {
////            result += Integer.toBinaryString(strChar[i]) + " ";
//            Log.d("=====integers=======", strChar[i] + "");
//            switch (strChar[i]) {
//                case '1':
//                    isFewWeeks(i, viewHolder);
//                    break;
//            }
//        }
    }

    class ViewHolder {
        TextView hour;
        TextView min;
        TextView type, type1, type2, type3, type4, type5, type6, type7;
        ImageView imageType;
        Switch switchA;
        LinearLayout linearLayout;
    }
}
