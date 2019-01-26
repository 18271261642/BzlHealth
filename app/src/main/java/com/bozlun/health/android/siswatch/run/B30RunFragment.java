package com.bozlun.health.android.siswatch.run;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afa.tourism.greendao.gen.B30DevicesSportDao;
import com.afa.tourism.greendao.gen.LatLonBeanDao;
import com.afa.tourism.greendao.gen.SportMapsDao;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.wylactivity.MapRecordActivity;
import com.bozlun.health.android.activity.wylactivity.SportsHistoryActivity;
import com.bozlun.health.android.activity.wylactivity.wyl_util.service.ConnectManages;
import com.bozlun.health.android.adpter.MyPagerAdapter;
import com.bozlun.health.android.b30.B30BaseFragment;
import com.bozlun.health.android.b30.DevicesSportHisyory;
import com.bozlun.health.android.b30.GPSSportHisyory;
import com.bozlun.health.android.b30.bean.B30DevicesSport;
import com.bozlun.health.android.bzlmaps.gaodemaps.BzlGaoDeActivity;
import com.bozlun.health.android.bzlmaps.googlemaps.BzlGoogleMapsActivity;
import com.bozlun.health.android.bzlmaps.mapdb.LatLonBean;
import com.bozlun.health.android.bzlmaps.mapdb.SportMaps;
import com.bozlun.health.android.siswatch.adapter.OutDoorSportAdapterNew;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.MyLogUtil;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.google.gson.Gson;
import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.ISportModelOriginListener;
import com.veepoo.protocol.listener.data.ISportModelStateListener;
import com.veepoo.protocol.model.datas.SportModelOriginHeadData;
import com.veepoo.protocol.model.datas.SportModelOriginItemData;
import com.veepoo.protocol.model.datas.SportModelStateData;
import com.veepoo.protocol.model.enums.ESportModelStateStauts;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class B30RunFragment extends B30BaseFragment implements
        TabLayout.OnTabSelectedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private TabLayout mTabLayout = null;
    private int pages = 0;
    private ViewPagerSlide viewPagerSlide = null;
    private ImageView imageHis;
    private Runnable runnable = null;
    private Runnable runnableO = null;


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Double distance = 0.0;
            //总公里数
            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
            List<SportMaps> runSportListDB = (List<SportMaps>) msg.obj;
            if (runSportListDB != null && runSportListDB.size() > 0) {
                todayDataType.setVisibility(View.GONE);
                commentRunRecyclerView.setVisibility(View.VISIBLE);
            } else {
                todayDataType.setVisibility(View.VISIBLE);
                commentRunRecyclerView.setVisibility(View.GONE);
            }
            switch (msg.what) {
                case 0x08:
                    distance = 0.0;
                    for (int i = 0; i < runSportListDB.size(); i++) {
                        SportMaps sportMaps = runSportListDB.get(i);
                        if (sportMaps == null) return;
                        String distances = sportMaps.getDistance();
                        if (TextUtils.isEmpty(distances)) {
                            distances = "0";
                        }
                        distance += Double.valueOf((TextUtils.isEmpty(distances.trim()) ? "0" : distances.trim()));
                    }
                    if (w30sunit) {
                        w30sTotalKmTv.setText(String.format("%.3f", distance));// 三位小数
                        textDataUnit.setText(" KM");
                    } else {
                        w30sTotalKmTv.setText("" + Math.round(distance * 1000 * 3.28));
                        textDataUnit.setText(" FT");
                    }
                    break;
                case 0x09:
                    for (int i = 0; i < runSportListDB.size(); i++) {
                        String distance1 = runSportListDB.get(i).getDistance();
                        if (TextUtils.isEmpty(distance1)) {
                            distance1 = "0";
                        }
                        distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1));
                    }
                    //总公里数
                    if (w30sunit) {
                        w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
                    } else {
                        w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");
                    }
                    break;
                case 0x10:
                    for (int i = 0; i < runSportListDB.size(); i++) {
                        String distance1 = runSportListDB.get(i).getDistance();
                        if (TextUtils.isEmpty(distance1)) {
                            distance1 = "0";
                        }
                        distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1.trim()));
                    }
                    if (w30sunit) {
                        w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
                    } else {
                        w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");//
                    }

                    break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.bozhilun.android.siswatch.run.UNIT");
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("-------------", TextUtils.isEmpty(intent.getAction()) ? "null" : intent.getAction());
            if (pages == 0) {
                //if (imageHis != null) imageHis.setVisibility(View.VISIBLE);
                if (runnableO !=null)mHandler.removeCallbacks(runnableO);
                runnableO = new Runnable() {
                    @Override
                    public void run() {
                        shouGpsRun();
                    }
                };
                mHandler.post(runnableO);
            } else {
                //if (imageHis != null) imageHis.setVisibility(View.GONE);
                //检查设备运动是否开启

                if (runnableO !=null)mHandler.removeCallbacks(runnableO);
                runnableO = new Runnable() {
                    @Override
                    public void run() {
                        devicesShow();
                    }
                };
                mHandler.post(runnableO);
            }
        }
    };

    /**
     * tab 选中
     *
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pages = tab.getPosition();
        if (pages == 0) {
            //imageHis.setVisibility(View.VISIBLE);
            if (runnableO !=null)mHandler.removeCallbacks(runnableO);
            runnableO = new Runnable() {
                @Override
                public void run() {
                    shouGpsRun();
                }
            };
            mHandler.post(runnableO);
        } else {
            //imageHis.setVisibility(View.GONE);
            //检查设备运动是否开启
            if (runnableO !=null)mHandler.removeCallbacks(runnableO);
            runnableO = new Runnable() {
                @Override
                public void run() {
                    devicesShow();
                }
            };
            mHandler.post(runnableO);
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        super.onFragmentVisibleChange(isVisible);

    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_b30_run_layout, container, false);
    }

    @Override
    protected void initView(View root) {
        View GpsRun = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_new_run_layout, null, false);
        initGpsRunView(GpsRun);

        View DeviceRun = LayoutInflater.from(getActivity()).inflate(R.layout.b30_devices_fragment, null, false);
        initDeviceRunView(DeviceRun);

        if (imageHis == null) imageHis = root.findViewById(R.id.watch_run_sportHistoryTitleTv);
        imageHis.setOnClickListener(this);
        if (viewPagerSlide == null) viewPagerSlide = root.findViewById(R.id.run_view_pager);
        if (mTabLayout == null) mTabLayout = (TabLayout) root.findViewById(R.id.mTabLayout);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.addOnTabSelectedListener(this);
        mTabLayout.setupWithViewPager(viewPagerSlide);
        String[] stringsTitle = {getResources().getString(R.string.string_gps_run),
                getResources().getString(R.string.string_devices_run)};
        List<View> viewList = new ArrayList<>();
        viewList.add(GpsRun);
        viewList.add(DeviceRun);
        viewPagerSlide.setAdapter(new MyPagerAdapter(viewList, stringsTitle));
    }


    @Override
    protected void initListener() {
    }

    @Override
    protected void lazyLoad() {
    }

    /**
     * 历史纪录图片点击
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.w30sRunImg:   //跑步图片
                isConn(getContext());
                boolean b = initGPS();
                if (b) {
                    SharedPreferencesUtils.saveObject(getActivity(), "type", "0");
                    Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
                    if (zhon) {
                        startActivity(new Intent(getContext(), BzlGaoDeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        startActivity(new Intent(getContext(), BzlGoogleMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }

                } else {
                    openGPS();
                }

                break;
            case R.id.w30sCycleImg:     //骑行图片
                isConn(getContext());
                boolean b1 = initGPS();
                if (b1) {
                    SharedPreferencesUtils.saveObject(getActivity(), "type", "1");
//                    startActivity(new Intent(getActivity(), OutdoorCyclingActivityStar.class));
                    Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
                    if (zhon) {
                        startActivity(new Intent(getContext(), BzlGaoDeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        startActivity(new Intent(getContext(), BzlGoogleMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                } else {
                    openGPS();
                }
                break;
            case R.id.w30sRunTv:    //跑步切换
                w30sMonthTv.setText(getResources().getString(R.string.string_sport_all_data));
                clearClickTvStyle();
                w30sRunTv.setTextColor(getResources().getColor(R.color.white));
                w30sRunTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
                runTags = 0;
//                getRunMapListData(runTags);    //获取地图的历史记录
                getRunLiatDataDB(runTags, df.format(new Date()));
                break;
            case R.id.w30sCycleTv:  //骑行切换
                w30sMonthTv.setText(getResources().getString(R.string.string_run_all_data));
                clearClickTvStyle();
                w30sCycleTv.setTextColor(getResources().getColor(R.color.white));
                w30sCycleTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
                runTags = 1;
//                getRunMapListData(runTags);    //获取地图的历史记录
                getRunLiatDataDB(runTags, df.format(new Date()));
                break;
            case R.id.image_history:
                startActivity(new Intent(getActivity(), SportsHistoryActivity.class));
                break;
            case R.id.watch_run_sportHistoryTitleTv:
                if (pages == 0) {
                    startActivity(new Intent(getActivity(), GPSSportHisyory.class));
                } else {
                    startActivity(new Intent(getActivity(), DevicesSportHisyory.class));
                }
                break;
        }

    }


    /****************手环运动*********************/
    private ImageView imageView = null;
    private TextView kaluli_text = null;
    private ListView list_kcluli = null;
    private TextView shuo_text = null;
    private boolean isPlay = false;
    private VPOperateManager vpOperateManager;

    /**
     * 手环运动
     *
     * @param deviceRun
     */
    private void initDeviceRunView(View deviceRun) {
        vpOperateManager = MyApp.getInstance().getVpOperateManager();
        imageView = deviceRun.findViewById(R.id.devices_run);
        kaluli_text = deviceRun.findViewById(R.id.kaluli_text);
        list_kcluli = deviceRun.findViewById(R.id.list_kcluli);
        shuo_text = deviceRun.findViewById(R.id.shuo_text);
        shuo_text.setText("" + getResources().getString(R.string.string_all_rkcal) + "(" + getResources().getString(R.string.km_cal) + ")");
    }


    /**
     * 查找本地数据库的手还运动数据
     */
    void findDatas() {
        String currentDate4 = WatchUtils.getCurrentDate4();
        String bleMac = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
        if (WatchUtils.isEmpty(bleMac)) return;
        if (WatchUtils.isEmpty(userId)) return;
        final List<B30DevicesSport> b30DevicesSports = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao()
                .queryBuilder().where(B30DevicesSportDao.Properties.Address.eq( bleMac)
                        , B30DevicesSportDao.Properties.Date.eq(currentDate4)).list();
        if (!b30DevicesSports.isEmpty()) {
            double kcals = 0.0;
            for (int i = 0; i < b30DevicesSports.size(); i++) {
                kcals += b30DevicesSports.get(i).getKcals();
            }
            if (kaluli_text != null) kaluli_text.setText(new DecimalFormat("0.0").format(kcals));
            if (list_kcluli != null) list_kcluli.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return b30DevicesSports.size();
                }

                @Override
                public Object getItem(int i) {
                    return b30DevicesSports.get(i);
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    View view1 = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.devices_sport_item, null);

                    TextView textViewKcal = view1.findViewById(R.id.text_kcal);
                    TextView textViewHeart = view1.findViewById(R.id.text_heart);
                    TextView textViewStep = view1.findViewById(R.id.text_step);
                    TextView textViewDataTime = view1.findViewById(R.id.text_data_time);
                    TextView textViewTimes = view1.findViewById(R.id.text_times);


                    String s = formatTime((b30DevicesSports.get(i).getSportTime() * 1000));
                    textViewTimes.setText(s);
                    textViewKcal.setText(b30DevicesSports.get(i).getKcals() + getResources().getString(R.string.km_cal));

                    textViewHeart.setText(b30DevicesSports.get(i).getAverRate() + "bpm");
                    textViewStep.setText(b30DevicesSports.get(i).getStepCount() + getResources().getString(R.string.daily_numberofsteps_default));
                    textViewDataTime.setText(b30DevicesSports.get(i).getStartTime());

                    return view1;
                }
            });
        }
    }

    /*
     * 毫秒转化
     */
    public static String formatTime(long ms) {

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day; //天
        String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
        String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
        String strSecond = second < 10 ? "0" + second : "" + second;//秒
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strMinute + ":" + strSecond + ":" + strMilliSecond;
    }

    /**
     * 获取设备运动保存数据库
     */
    void loadData() {
        vpOperateManager.readSportModelOrigin(iBleWriteResponse, new ISportModelOriginListener() {
            @Override
            public void onReadOriginProgress(float v) {

            }

            @Override
            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {
            }

            @Override
            public void onHeadChangeListListener(SportModelOriginHeadData sportModelOriginHeadData) {
                MyLogUtil.d("-------数据------",
                        "\n日期:" + sportModelOriginHeadData.getDate()
                                + "\n开始时间:" + sportModelOriginHeadData.getStartTime().getDate()
                                + "\n停止时间:" + sportModelOriginHeadData.getStopTime().getDate()
                                + "\n运动时间:" + sportModelOriginHeadData.getSportTime()
                                + "\n步数:" + sportModelOriginHeadData.getStepCount()
                                + "\n运动计数:" + sportModelOriginHeadData.getSportCount()
                                + "\n千卡:" + sportModelOriginHeadData.getKcals()
                                + "\n距离:" + sportModelOriginHeadData.getDistance()
                                + "\n记录计数:" + sportModelOriginHeadData.getRecordCount()
                                + "\n暂停计数:" + sportModelOriginHeadData.getPauseCount()
                                + "\n暂停时长:" + sportModelOriginHeadData.getPauseTime()
                                + "\nCRC:" + sportModelOriginHeadData.getCrc()
                                + "\n配速:" + sportModelOriginHeadData.getPeisu()
                                + "\n牛运动时:" + sportModelOriginHeadData.getOxsporttimes()
                                + "\n心率:" + sportModelOriginHeadData.getAverRate());
                B30DevicesSport db = new B30DevicesSport();
                db.setDate(sportModelOriginHeadData.getDate());
                db.setStartTime(sportModelOriginHeadData.getStartTime().getYear()
                        + "-" + sportModelOriginHeadData.getStartTime().getMonth()
                        + "-" + sportModelOriginHeadData.getStartTime().getDay()
                        + " " + sportModelOriginHeadData.getStartTime().getHour()
                        + ":" + sportModelOriginHeadData.getStartTime().getMinute()
                        + ":" + sportModelOriginHeadData.getStartTime().getSecond());
                db.setStopTime(sportModelOriginHeadData.getStopTime().getDate());
                db.setSportTime(sportModelOriginHeadData.getSportTime());
                db.setStepCount(sportModelOriginHeadData.getStepCount());
                db.setSportCount(sportModelOriginHeadData.getSportCount());
                db.setKcals(sportModelOriginHeadData.getKcals());
                db.setDistance(sportModelOriginHeadData.getDistance());
                db.setRecordCount(sportModelOriginHeadData.getRecordCount());
                db.setPauseCount(sportModelOriginHeadData.getPauseCount());
                db.setPauseTime(sportModelOriginHeadData.getPauseTime());
                db.setCrc(sportModelOriginHeadData.getCrc());
                db.setPeisu(sportModelOriginHeadData.getPeisu());
                db.setOxsporttimes(sportModelOriginHeadData.getOxsporttimes());
                db.setAverRate(sportModelOriginHeadData.getAverRate());
                String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac
                db.setAddress(bm);
                String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
                db.setUsername(userId);
                List<B30DevicesSport> b30DevicesSports = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().loadAll();
                if (b30DevicesSports == null || b30DevicesSports.size() <= 0) {
                    MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().insert(db);
                } else {
                    for (int i = 0; i < b30DevicesSports.size(); i++) {

                        B30DevicesSport unique = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().queryBuilder().where(B30DevicesSportDao.Properties.StartTime.eq(sportModelOriginHeadData.getStartTime().getYear()
                                + "-" + sportModelOriginHeadData.getStartTime().getMonth()
                                + "-" + sportModelOriginHeadData.getStartTime().getDay()
                                + " " + sportModelOriginHeadData.getStartTime().getHour()
                                + ":" + sportModelOriginHeadData.getStartTime().getMinute()
                                + ":" + sportModelOriginHeadData.getStartTime().getSecond())).unique();
                        if (unique == null) {
                            MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().insert(db);
                        }

                    }
                }


                findDatas();
            }

            @Override
            public void onItemChangeListListener(List<SportModelOriginItemData> list) {
            }

            @Override
            public void onReadOriginComplete() {
            }
        });
    }

    IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };


    /**
     * 设备运动界面显示---检查设备运动是否开启
     */
    void devicesShow() {
        if (getActivity()!=null&&!getActivity().isFinishing()){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MyApp.getInstance().getVpOperateManager().readSportModelState(iBleWriteResponse, new ISportModelStateListener() {
                        @Override
                        public void onSportModelStateChange(SportModelStateData sportModelStateData) {
                            if (sportModelStateData.getDeviceStauts().equals(ESportModelStateStauts.DEVICE_HAD_START_BEFORE)) {
                                if (imageView != null) imageView.setImageResource(R.mipmap.ic_stop_devices);
                                isPlay = true;
                            } else {
                                isPlay = false;
                                if (imageView != null) imageView.setImageResource(R.mipmap.ic_play_devices);
                            }
                        }
                    });
                    if (imageView != null) imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPlay) {
                                isPlay = true;
                                if (imageView != null) imageView.setImageResource(R.mipmap.ic_stop_devices);
                                MyApp.getInstance().getVpOperateManager().startSportModel(iBleWriteResponse, new ISportModelStateListener() {
                                    @Override
                                    public void onSportModelStateChange(SportModelStateData sportModelStateData) {
                                    }
                                });
                            } else {
                                isPlay = false;
                                if (imageView != null) imageView.setImageResource(R.mipmap.ic_play_devices);

                                MyApp.getInstance().getVpOperateManager().stopSportModel(iBleWriteResponse, new ISportModelStateListener() {
                                    @Override
                                    public void onSportModelStateChange(SportModelStateData sportModelStateData) {
                                        if (runnable != null) mHandler.removeCallbacks(runnable);
                                        runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                loadData();
                                            }
                                        };
                                        mHandler.postDelayed(runnable, 500);
                                    }
                                });
                            }

                        }
                    });
                }
            });
        }


        findDatas();
    }


    /****************GPS运动*********************/


    //总公里显示
    TextView w30sTotalKmTv;
    //月的总公里显示
    TextView w30sMonthTotalKmTv;
    //recyclerView
    RecyclerView commentRunRecyclerView;
    //
    ImageView todayDataType;
    SwipeRefreshLayout watchRunSwipe;
    //跑步
    TextView w30sRunTv;
    //骑行
    TextView w30sCycleTv;
    TextView w30sMonthTv;
    TextView textDataUnit;
    //跑步或者骑行
    private int runTags = 0;
    /**
     * 本地化帮助工具
     */
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private List<SportMaps> runSportListDB;
    private OutDoorSportAdapterNew outDoorSportAdapterDB;

    /**
     * GPS运动
     *
     * @param gpsRun
     */
    private void initGpsRunView(View gpsRun) {
        gpsRun.findViewById(R.id.w30sRunImg).setOnClickListener(this);
        gpsRun.findViewById(R.id.w30sCycleImg).setOnClickListener(this);
        gpsRun.findViewById(R.id.w30sRunTv).setOnClickListener(this);
        gpsRun.findViewById(R.id.w30sCycleTv).setOnClickListener(this);
        gpsRun.findViewById(R.id.image_history).setOnClickListener(this);
        //总公里显示
        w30sTotalKmTv = gpsRun.findViewById(R.id.w30sTotalKmTv);
        //月的总公里显示
        w30sMonthTotalKmTv = gpsRun.findViewById(R.id.w30sMonthTotalKmTv);
        //recyclerView
        commentRunRecyclerView = gpsRun.findViewById(R.id.commentRunRecyclerView);
        //
        todayDataType = gpsRun.findViewById(R.id.today_data_type);
        watchRunSwipe = gpsRun.findViewById(R.id.watch_runSwipe);
        //跑步
        w30sRunTv = gpsRun.findViewById(R.id.w30sRunTv);
        //骑行
        w30sCycleTv = gpsRun.findViewById(R.id.w30sCycleTv);
        w30sMonthTv = gpsRun.findViewById(R.id.w30sMonthTv);
        textDataUnit = gpsRun.findViewById(R.id.text_data_unit);


        boolean param = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
        if (!param) {
            w30sMonthTotalKmTv.setText("0.0 FT");
        } else {
            w30sMonthTotalKmTv.setText("0.0" + " KM");
        }
        LinearLayoutManager linm = new LinearLayoutManager(getActivity());
        linm.setOrientation(LinearLayoutManager.VERTICAL);
        commentRunRecyclerView.setLayoutManager(linm);
        commentRunRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        runSportListDB = new ArrayList<>();
        watchRunSwipe.setOnRefreshListener(this);
        clearClickTvStyle();
        w30sRunTv.setTextColor(getResources().getColor(R.color.white));
        w30sRunTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
    }


    private boolean initGPS() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }


    private void openGPS() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(getResources().getString(R.string.string_open_gps));
        dialog.setMessage(getResources().getString(R.string.string_hello_gps));
        dialog.setPositiveButton(getResources().getString(R.string.menu_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // 转到手机设置界面，用户设置GPS
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Toast.makeText(getContext(), getResources().getString(R.string.string_gps_stute), Toast.LENGTH_SHORT).show();
                startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
            }
        });
        dialog.setNeutralButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 判断网络连接是否已开
     * true 已打开  false 未打开
     */
    public static boolean isConn(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
            searchNetwork(context);//弹出提示对话框
        }
        return false;
    }

    /**
     * 判断网络是否连接成功，连接成功不做任何操作
     * 未连接则弹出对话框提示用户设置网络连接
     */
    public static void searchNetwork(final Context context) {
        //提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.string_net_work)).setMessage(context.getResources().getString(R.string.string_no_net_work))
                .setPositiveButton(context.getResources().getString(R.string.menu_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        //判断手机系统的版本  即API大于10 就是3.0或以上版本
                        if (Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(component);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                }).setNegativeButton(context.getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }


    private void clearClickTvStyle() {
        try {
            if (w30sRunTv != null) {
                w30sRunTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
                w30sRunTv.setTextColor(Color.parseColor("#333333"));
            }

            if (w30sCycleTv != null) {
                w30sCycleTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
                w30sCycleTv.setTextColor(Color.parseColor("#333333"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRefresh() {
        if (ConnectManages.isNetworkAvailable(getActivity()) && watchRunSwipe != null && !watchRunSwipe.isRefreshing()) {
            watchRunSwipe.setRefreshing(true);
            getRunLiatDataDB(runTags, df.format(new Date()));
        } else {
            if (watchRunSwipe != null)
                watchRunSwipe.setRefreshing(false);
        }
    }


    private void getRunLiatDataDB(int runTags, String dataTime) {
//        String bm = (String) SharedPreferenceUtil.get(MyApp.getContext(), Commont.BLEMAC, "");
//        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac mylanmac
        String bleMac = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
        if (WatchUtils.isEmpty(bleMac) ) return;
        if (WatchUtils.isEmpty(userId)) return;
        final List<SportMaps> sportMapsList = MyApp.getInstance().getDBManager().getDaoSession().getSportMapsDao()
                .queryBuilder().where(SportMapsDao.Properties.Mac.eq(bleMac),
                        SportMapsDao.Properties.UserId.eq(userId), SportMapsDao.Properties.Rtc.eq(dataTime)).list();
        if (sportMapsList == null) {

            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
            //总公里数
            if (w30sunit) {
                //总运动距离
                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("0.0");
                if (textDataUnit != null) textDataUnit.setText(" KM");
            } else {
                //总运动距离
                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("0.0");
                if (textDataUnit != null) textDataUnit.setText(" FT");
            }


            todayDataType.setVisibility(View.VISIBLE);
            commentRunRecyclerView.setVisibility(View.GONE);
            return;
        }
        List<LatLonBean> latLonBeanList = MyApp.getInstance().getDBManager().getDaoSession().getLatLonBeanDao()
                .queryBuilder().where(LatLonBeanDao.Properties.Mac.eq(bleMac),
                        LatLonBeanDao.Properties.UserId.eq(userId), LatLonBeanDao.Properties.Rtc.eq(dataTime)).list();
        /**
         * 计算当月总运动距离
         */
        Message message = new Message();
        message.obj = sportMapsList;
        message.what = 0x08;
        mHandler.sendMessage(message);

        switch (runTags) {
            case 0:
                runSportListDB.clear();
                for (SportMaps ob : sportMapsList) {
                    if (ob.getType() == 0) {
                        runSportListDB.add(ob);
                    }
                }
                outDoorSportAdapterDB = new OutDoorSportAdapterNew(runSportListDB, getActivity());
                commentRunRecyclerView.setAdapter(outDoorSportAdapterDB);
                /**
                 * 计算跑步总里程
                 */
                Message message1 = new Message();
                message1.what = 0x09;
                message1.obj = runSportListDB;
                mHandler.sendMessage(message1);

                outDoorSportAdapterDB.notifyDataSetChanged();
                showItemClickDB(runSportListDB, latLonBeanList);
                if (runSportListDB.size() > 0) {
                    todayDataType.setVisibility(View.GONE);
                    commentRunRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    todayDataType.setVisibility(View.VISIBLE);
                    commentRunRecyclerView.setVisibility(View.GONE);
                }
                break;
            case 1:
                runSportListDB.clear();
                for (SportMaps ob : sportMapsList) {
                    if (ob.getType() == 1) {
                        runSportListDB.add(ob);
                    }
                }
                outDoorSportAdapterDB = new OutDoorSportAdapterNew(runSportListDB, getActivity());
                commentRunRecyclerView.setAdapter(outDoorSportAdapterDB);
                Message message3 = new Message();
                message3.what = 0x10;
                message3.obj = runSportListDB;
                mHandler.sendMessage(message3);
                outDoorSportAdapterDB.notifyDataSetChanged();
                showItemClickDB(runSportListDB, latLonBeanList);
                if (runSportListDB.size() > 0) {
                    todayDataType.setVisibility(View.GONE);
                    commentRunRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    todayDataType.setVisibility(View.VISIBLE);
                    commentRunRecyclerView.setVisibility(View.GONE);
                }
                break;
        }
    }


    void shouGpsRun() {
        try {
            if (getActivity()!=null&&!getActivity().isFinishing()){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
                        //总公里数
                        if (w30sunit) {
                            //总运动距离
                            if (w30sTotalKmTv != null) w30sTotalKmTv.setText("0.00");
                            if (textDataUnit != null) textDataUnit.setText(" KM");
                        } else {
                            //总运动距离
                            if (w30sTotalKmTv != null) w30sTotalKmTv.setText("" + (0 * 3.28));
                            if (textDataUnit != null) textDataUnit.setText(" FT");
                        }
                    }
                });
            }

            getRunLiatDataDB(runTags, df.format(new Date()));
        } catch (Exception e) {
            e.getMessage();
        }

    }

    /**
     * 列表点击事件
     *
     * @param runSportListDB
     * @param latLonBeanList
     */
    private void showItemClickDB(final List<SportMaps> runSportListDB, final List<LatLonBean> latLonBeanList) {
        outDoorSportAdapterDB.setListener(new OutDoorSportAdapterNew.OnOutDoorSportItemClickListener() {
            @Override
            public void doItemClick(int position) {


                Map<String, Object> mapb = new HashMap<>();
                mapb.put("year", runSportListDB.get(position).getRtc());//日期
                mapb.put("day", runSportListDB.get(position).getStartTime());//开始日期
                mapb.put("zonggongli", runSportListDB.get(position).getDistance() + "Km");//总公里
                if (runSportListDB.get(position).getType() == 0) {
                    mapb.put("qixing", getResources().getString(R.string.outdoor_running));//骑行或者跑步
                    mapb.put("image", R.mipmap.huwaipaohuan);//跑步-骑行
                } else {
                    mapb.put("qixing", getResources().getString(R.string.outdoor_cycling));//骑行或者跑步
                    mapb.put("image", R.mipmap.qixinghuan);//跑步-骑行
                }
                mapb.put("chixugongli", runSportListDB.get(position).getDistance() + "Km");//持续公里数
                mapb.put("chixutime", runSportListDB.get(position).getTimeLen());//持续时间
                mapb.put("kclal", runSportListDB.get(position).getCalories() + "Kcal");//卡路里
                mapb.put("image", runSportListDB.get(position).getImage());
                mapb.put("temp", runSportListDB.get(position).getTemp());
                mapb.put("description", runSportListDB.get(position).getDescription());
                mapb.put("speed", runSportListDB.get(position).getSpeed());
                Intent intent = new Intent(getActivity(), MapRecordActivity.class);
//                Intent intent = new Intent(getActivity(), BzlMaps_HistoryActivity.class);
                Log.d("---------", latLonBeanList.toString() + "\n" + new Gson().toJson(latLonBeanList));
                intent.putExtra("mapdata", latLonBeanList.get(position).getLatLons().trim());
                intent.putExtra("mapdata2", new Gson().toJson(mapb));
                startActivity(intent);
            }
        });
    }
}

//package com.example.bozhilun.android.siswatch.run;
//
//import android.annotation.SuppressLint;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.location.LocationManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.Settings;
//import android.support.annotation.Nullable;
//import android.support.design.widget.TabLayout;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.afa.tourism.greendao.gen.B30DevicesSportDao;
//import com.afa.tourism.greendao.gen.DaoSession;
//import com.afa.tourism.greendao.gen.LatLonBeanDao;
//import com.afa.tourism.greendao.gen.SportMapsDao;
//import com.example.bozhilun.android.db.DBManager;
//import com.example.bozhilun.android.Commont;
//import com.example.bozhilun.android.MyApp;
//import com.example.bozhilun.android.R;
//import com.example.bozhilun.android.activity.wylactivity.MapRecordActivity;
//import com.example.bozhilun.android.activity.wylactivity.SportsHistoryActivity;
//import com.example.bozhilun.android.activity.wylactivity.wyl_util.service.ConnectManages;
//import com.example.bozhilun.android.adpter.MyPagerAdapter;
//import com.example.bozhilun.android.b30.B30BaseFragment;
//import com.example.bozhilun.android.b30.GPSSportHisyory;
//import com.example.bozhilun.android.b30.bean.B30DevicesSport;
//import com.example.bozhilun.android.bzlmaps.gaodemaps.BzlGaoDeActivity;
//import com.example.bozhilun.android.bzlmaps.googlemaps.BzlGoogleMapsActivity;
//import com.example.bozhilun.android.bzlmaps.mapdb.LatLonBean;
//import com.example.bozhilun.android.bzlmaps.mapdb.SportMaps;
//import com.example.bozhilun.android.siswatch.adapter.OutDoorSportAdapterNew;
//import com.example.bozhilun.android.util.SharedPreferencesUtils;
//import com.google.gson.Gson;
//import com.veepoo.protocol.VPOperateManager;
//import com.veepoo.protocol.listener.base.IBleWriteResponse;
//import com.veepoo.protocol.listener.data.ISportModelOriginListener;
//import com.veepoo.protocol.listener.data.ISportModelStateListener;
//import com.veepoo.protocol.model.datas.SportModelOriginHeadData;
//import com.veepoo.protocol.model.datas.SportModelOriginItemData;
//import com.veepoo.protocol.model.datas.SportModelStateData;
//import com.veepoo.protocol.model.enums.ESportModelStateStauts;
//
//import java.text.DateFormat;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class B36RunFragment extends B30BaseFragment implements
//        TabLayout.OnTabSelectedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
//
//    private TabLayout mTabLayout = null;
//    private int pages = 0;
//    private ViewPagerSlide viewPagerSlide = null;
//    private ImageView imageHis;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.bozhilun.android.siswatch.run.UNIT");
//        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        getActivity().unregisterReceiver(mBroadcastReceiver);
//    }
//
//    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (pages == 0) {
//                if (imageHis != null) imageHis.setVisibility(View.VISIBLE);
//                shouGpsRun();
//            } else {
//                if (imageHis != null) imageHis.setVisibility(View.GONE);
//                //检查设备运动是否开启
//                devicesShow();
//            }
//        }
//    };
//
//
//    /**
//     * tab 选中
//     *
//     * @param tab
//     */
//    @Override
//    public void onTabSelected(TabLayout.Tab tab) {
//        pages = tab.getPosition();
//        if (pages == 0) {
//            imageHis.setVisibility(View.VISIBLE);
//            shouGpsRun();
//        } else {
//            imageHis.setVisibility(View.GONE);
//            //检查设备运动是否开启
//            devicesShow();
//        }
//    }
//
//    @Override
//    public void onTabUnselected(TabLayout.Tab tab) {
//
//    }
//
//    @Override
//    public void onTabReselected(TabLayout.Tab tab) {
//
//    }
//
//
//    @Override
//    protected void onFragmentVisibleChange(boolean isVisible) {
//        super.onFragmentVisibleChange(isVisible);
//
//    }
//
//
//    @Override
//    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_b30_run_layout, container, false);
//    }
//
//    @Override
//    protected void initView(View root) {
//        View GpsRun = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_new_run_layout, null, false);
//        initGpsRunView(GpsRun);
//
//        View DeviceRun = LayoutInflater.from(getActivity()).inflate(R.layout.b30_devices_fragment, null, false);
//        initDeviceRunView(DeviceRun);
//
//        if (imageHis == null) imageHis = root.findViewById(R.id.watch_run_sportHistoryTitleTv);
//        imageHis.setOnClickListener(this);
//        if (viewPagerSlide == null) viewPagerSlide = root.findViewById(R.id.run_view_pager);
//        if (mTabLayout == null) mTabLayout = (TabLayout) root.findViewById(R.id.mTabLayout);
//        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
//        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        mTabLayout.addOnTabSelectedListener(this);
//        mTabLayout.setupWithViewPager(viewPagerSlide);
//        String[] stringsTitle = {getResources().getString(R.string.string_gps_run),
//                getResources().getString(R.string.string_devices_run)};
//        List<View> viewList = new ArrayList<>();
//        viewList.add(GpsRun);
//        viewList.add(DeviceRun);
//        viewPagerSlide.setAdapter(new MyPagerAdapter(viewList, stringsTitle));
//    }
//
//
//    @Override
//    protected void initListener() {
//    }
//
//    @Override
//    protected void lazyLoad() {
//    }
//
//    /**
//     * 历史纪录图片点击
//     *
//     * @param view
//     */
//    @Override
//    public void onClick(View view) {
//
//        switch (view.getId()) {
//            case R.id.w30sRunImg:   //跑步图片
//                isConn(getContext());
//                boolean b = initGPS();
//                if (b) {
//                    SharedPreferencesUtils.saveObject(getActivity(), "type", "0");
//                    Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
//                    if (zhon) {
//                        startActivity(new Intent(getContext(), BzlGaoDeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    } else {
//                        startActivity(new Intent(getContext(), BzlGoogleMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    }
//
//                } else {
//                    openGPS();
//                }
//
//                break;
//            case R.id.w30sCycleImg:     //骑行图片
//                isConn(getContext());
//                boolean b1 = initGPS();
//                if (b1) {
//                    SharedPreferencesUtils.saveObject(getActivity(), "type", "1");
////                    startActivity(new Intent(getActivity(), OutdoorCyclingActivityStar.class));
//                    Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
//                    if (zhon) {
//                        startActivity(new Intent(getContext(), BzlGaoDeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    } else {
//                        startActivity(new Intent(getContext(), BzlGoogleMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    }
//                } else {
//                    openGPS();
//                }
//                break;
//            case R.id.w30sRunTv:    //跑步切换
//                w30sMonthTv.setText(getResources().getString(R.string.string_sport_all_data));
//                clearClickTvStyle();
//                w30sRunTv.setTextColor(getResources().getColor(R.color.white));
//                w30sRunTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
//                runTags = 0;
////                getRunMapListData(runTags);    //获取地图的历史记录
//                getRunLiatDataDB(runTags, df.format(new Date()));
//                break;
//            case R.id.w30sCycleTv:  //骑行切换
//                w30sMonthTv.setText(getResources().getString(R.string.string_run_all_data));
//                clearClickTvStyle();
//                w30sCycleTv.setTextColor(getResources().getColor(R.color.white));
//                w30sCycleTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
//                runTags = 1;
////                getRunMapListData(runTags);    //获取地图的历史记录
//                getRunLiatDataDB(runTags, df.format(new Date()));
//                break;
//            case R.id.image_history:
//                startActivity(new Intent(getActivity(), SportsHistoryActivity.class));
//                break;
//            case R.id.watch_run_sportHistoryTitleTv:
//                startActivity(new Intent(getActivity(), GPSSportHisyory.class));
//                break;
//        }
//
//    }
//
//
//    /****************手环运动*********************/
//    private ImageView imageView = null;
//    private TextView kaluli_text = null;
//    private ListView list_kcluli = null;
//    private TextView shuo_text = null;
//    private boolean isPlay = false;
//    private VPOperateManager vpOperateManager;
//
//    /**
//     * 手环运动
//     *
//     * @param deviceRun
//     */
//    private void initDeviceRunView(View deviceRun) {
//        vpOperateManager = MyApp.getInstance().getVpOperateManager();
//        imageView = deviceRun.findViewById(R.id.devices_run);
//        kaluli_text = deviceRun.findViewById(R.id.kaluli_text);
//        list_kcluli = deviceRun.findViewById(R.id.list_kcluli);
//        shuo_text = deviceRun.findViewById(R.id.shuo_text);
//        shuo_text.setText( getResources().getString(R.string.string_all_rkcal) + "(" + getResources().getString(R.string.km_cal) + ")");
//    }
//
//
//    /**
//     * 查找本地数据库的手还运动数据
//     */
//    void findDatas() {
//        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac
//        final List<B30DevicesSport> b30DevicesSports = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().queryBuilder().where(B30DevicesSportDao.Properties.Address.eq(bm)).list();
//        if (!b30DevicesSports.isEmpty()) {
//            double kcals = 0.0;
//            for (int i = 0; i < b30DevicesSports.size(); i++) {
//                kcals += b30DevicesSports.get(i).getKcals();
//            }
//            if (kaluli_text != null) kaluli_text.setText(new DecimalFormat("0.0").format(kcals));
//            if (list_kcluli != null) list_kcluli.setAdapter(new BaseAdapter() {
//                @Override
//                public int getCount() {
//                    return b30DevicesSports.size();
//                }
//
//                @Override
//                public Object getItem(int i) {
//                    return b30DevicesSports.get(i);
//                }
//
//                @Override
//                public long getItemId(int i) {
//                    return i;
//                }
//
//                @Override
//                public View getView(int i, View view, ViewGroup viewGroup) {
//                    View view1 = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.devices_sport_item, null);
//
//                    TextView textViewKcal = view1.findViewById(R.id.text_kcal);
//                    TextView textViewHeart = view1.findViewById(R.id.text_heart);
//                    TextView textViewStep = view1.findViewById(R.id.text_step);
//                    TextView textViewDataTime = view1.findViewById(R.id.text_data_time);
//                    TextView textViewTimes = view1.findViewById(R.id.text_times);
//
//
//                    String s = formatTime((b30DevicesSports.get(i).getSportTime() * 1000));
//                    textViewTimes.setText(s);
//                    textViewKcal.setText(b30DevicesSports.get(i).getKcals() + getResources().getString(R.string.km_cal));
//
//                    textViewHeart.setText(b30DevicesSports.get(i).getAverRate() + "bpm");
//                    textViewStep.setText(b30DevicesSports.get(i).getStepCount() + getResources().getString(R.string.daily_numberofsteps_default));
//                    textViewDataTime.setText(b30DevicesSports.get(i).getStartTime());
//
//                    return view1;
//                }
//            });
//        }
//    }
//
//    /*
//     * 毫秒转化
//     */
//    public static String formatTime(long ms) {
//
//        int ss = 1000;
//        int mi = ss * 60;
//        int hh = mi * 60;
//        int dd = hh * 24;
//
//        long day = ms / dd;
//        long hour = (ms - day * dd) / hh;
//        long minute = (ms - day * dd - hour * hh) / mi;
//        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
//        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
//
//        String strDay = day < 10 ? "0" + day : "" + day; //天
//        String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
//        String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
//        String strSecond = second < 10 ? "0" + second : "" + second;//秒
//        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
//        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;
//
//        return strMinute + ":" + strSecond + ":" + strMilliSecond;
//    }
//
//    /**
//     * 获取设备运动保存数据库
//     */
//    void loadData() {
//        vpOperateManager.readSportModelOrigin(iBleWriteResponse, new ISportModelOriginListener() {
//            @Override
//            public void onReadOriginProgress(float v) {
//
//            }
//
//            @Override
//            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {
//            }
//
//            @Override
//            public void onHeadChangeListListener(SportModelOriginHeadData sportModelOriginHeadData) {
//
//                B30DevicesSport db = new B30DevicesSport();
//                db.setDate(sportModelOriginHeadData.getDate());
//                db.setStartTime(sportModelOriginHeadData.getStartTime().getYear()
//                        + "-" + sportModelOriginHeadData.getStartTime().getMonth()
//                        + "-" + sportModelOriginHeadData.getStartTime().getDay()
//                        + " " + sportModelOriginHeadData.getStartTime().getHour()
//                        + ":" + sportModelOriginHeadData.getStartTime().getMinute()
//                        + ":" + sportModelOriginHeadData.getStartTime().getSecond());
//                db.setStopTime(sportModelOriginHeadData.getStopTime().getDate());
//                db.setSportTime(sportModelOriginHeadData.getSportTime());
//                db.setStepCount(sportModelOriginHeadData.getStepCount());
//                db.setSportCount(sportModelOriginHeadData.getSportCount());
//                db.setKcals(sportModelOriginHeadData.getKcals());
//                db.setDistance(sportModelOriginHeadData.getDistance());
//                db.setRecordCount(sportModelOriginHeadData.getRecordCount());
//                db.setPauseCount(sportModelOriginHeadData.getPauseCount());
//                db.setPauseTime(sportModelOriginHeadData.getPauseTime());
//                db.setCrc(sportModelOriginHeadData.getCrc());
//                db.setPeisu(sportModelOriginHeadData.getPeisu());
//                db.setOxsporttimes(sportModelOriginHeadData.getOxsporttimes());
//                db.setAverRate(sportModelOriginHeadData.getAverRate());
//                String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac
//                db.setAddress(bm);
//                String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");
//                db.setUsername(userId);
//                List<B30DevicesSport> b30DevicesSports = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().loadAll();
//                if (b30DevicesSports == null || b30DevicesSports.size() <= 0) {
//                    MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().insert(db);
//                } else {
//                    for (int i = 0; i < b30DevicesSports.size(); i++) {
//
//                        B30DevicesSport unique = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().queryBuilder().where(B30DevicesSportDao.Properties.StartTime.eq(sportModelOriginHeadData.getStartTime().getYear()
//                                + "-" + sportModelOriginHeadData.getStartTime().getMonth()
//                                + "-" + sportModelOriginHeadData.getStartTime().getDay()
//                                + " " + sportModelOriginHeadData.getStartTime().getHour()
//                                + ":" + sportModelOriginHeadData.getStartTime().getMinute()
//                                + ":" + sportModelOriginHeadData.getStartTime().getSecond())).unique();
//                        if (unique == null) {
//                            MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().insert(db);
//                        }
//
//                    }
//                }
//
//
//                findDatas();
//            }
//
//            @Override
//            public void onItemChangeListListener(List<SportModelOriginItemData> list) {
//            }
//
//            @Override
//            public void onReadOriginComplete() {
//            }
//        });
//    }
//
//    IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
//        @Override
//        public void onResponse(int i) {
//
//        }
//    };
//
//
//    /**
//     * 设备运动界面显示---检查设备运动是否开启
//     */
//    void devicesShow() {
//        MyApp.getInstance().getVpOperateManager().readSportModelState(iBleWriteResponse, new ISportModelStateListener() {
//            @Override
//            public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                if (sportModelStateData.getDeviceStauts().equals(ESportModelStateStauts.DEVICE_HAD_START_BEFORE)) {
//                    if (imageView != null) imageView.setImageResource(R.mipmap.ic_stop_devices);
//                    isPlay = true;
//                } else {
//                    isPlay = false;
//                    if (imageView != null) imageView.setImageResource(R.mipmap.ic_play_devices);
//                }
//            }
//        });
//        if (imageView != null) imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isPlay) {
//                    isPlay = true;
//                    if (imageView != null) imageView.setImageResource(R.mipmap.ic_stop_devices);
//                    MyApp.getInstance().getVpOperateManager().startSportModel(iBleWriteResponse, new ISportModelStateListener() {
//                        @Override
//                        public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                        }
//                    });
//                } else {
//                    isPlay = false;
//                    if (imageView != null) imageView.setImageResource(R.mipmap.ic_play_devices);
//
//                    MyApp.getInstance().getVpOperateManager().stopSportModel(iBleWriteResponse, new ISportModelStateListener() {
//                        @Override
//                        public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    loadData();
//                                }
//                            }, 500);
//                        }
//                    });
//
//
//                }
//
//            }
//        });
//
//        findDatas();
//    }
//
//
//    /****************GPS运动*********************/
//
//
//    //总公里显示
//    TextView w30sTotalKmTv;
//    //月的总公里显示
//    TextView w30sMonthTotalKmTv;
//    //recyclerView
//    RecyclerView commentRunRecyclerView;
//    //
//    ImageView todayDataType;
//    SwipeRefreshLayout watchRunSwipe;
//    //跑步
//    TextView w30sRunTv;
//    //骑行
//    TextView w30sCycleTv;
//    TextView w30sMonthTv;
//    TextView textDataUnit;
//    //跑步或者骑行
//    private int runTags = 0;
//    /**
//     * 本地化帮助工具
//     */
//    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//    private List<SportMaps> runSportListDB;
//    private OutDoorSportAdapterNew outDoorSportAdapterDB;
//
//    /**
//     * GPS运动
//     *
//     * @param gpsRun
//     */
//    private void initGpsRunView(View gpsRun) {
//        gpsRun.findViewById(R.id.w30sRunImg).setOnClickListener(this);
//        gpsRun.findViewById(R.id.w30sCycleImg).setOnClickListener(this);
//        gpsRun.findViewById(R.id.w30sRunTv).setOnClickListener(this);
//        gpsRun.findViewById(R.id.w30sCycleTv).setOnClickListener(this);
//        gpsRun.findViewById(R.id.image_history).setOnClickListener(this);
//        //总公里显示
//        w30sTotalKmTv = gpsRun.findViewById(R.id.w30sTotalKmTv);
//        //月的总公里显示
//        w30sMonthTotalKmTv = gpsRun.findViewById(R.id.w30sMonthTotalKmTv);
//        //recyclerView
//        commentRunRecyclerView = gpsRun.findViewById(R.id.commentRunRecyclerView);
//        //
//        todayDataType = gpsRun.findViewById(R.id.today_data_type);
//        watchRunSwipe = gpsRun.findViewById(R.id.watch_runSwipe);
//        //跑步
//        w30sRunTv = gpsRun.findViewById(R.id.w30sRunTv);
//        //骑行
//        w30sCycleTv = gpsRun.findViewById(R.id.w30sCycleTv);
//        w30sMonthTv = gpsRun.findViewById(R.id.w30sMonthTv);
//        textDataUnit = gpsRun.findViewById(R.id.text_data_unit);
//
//
//        boolean param = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//        if (!param) {
//            w30sMonthTotalKmTv.setText("0.0 FT");
//        } else {
//            w30sMonthTotalKmTv.setText("0.0" + " KM");
//        }
//        LinearLayoutManager linm = new LinearLayoutManager(getActivity());
//        linm.setOrientation(LinearLayoutManager.VERTICAL);
//        commentRunRecyclerView.setLayoutManager(linm);
//        commentRunRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
//        runSportListDB = new ArrayList<>();
//        watchRunSwipe.setOnRefreshListener(this);
//        clearClickTvStyle();
//        w30sRunTv.setTextColor(getResources().getColor(R.color.white));
//        w30sRunTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);
//    }
//
//
//    private boolean initGPS() {
//        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        // 判断GPS模块是否开启，如果没有则开启
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//
//    private void openGPS() {
//        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//        dialog.setTitle(getResources().getString(R.string.string_open_gps));
//        dialog.setMessage(getResources().getString(R.string.string_hello_gps));
//        dialog.setPositiveButton(getResources().getString(R.string.menu_settings), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                // 转到手机设置界面，用户设置GPS
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                Toast.makeText(getContext(), getResources().getString(R.string.string_gps_stute), Toast.LENGTH_SHORT).show();
//                startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
//            }
//        });
//        dialog.setNeutralButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                arg0.dismiss();
//            }
//        });
//        dialog.show();
//    }
//
//
//    /**
//     * 判断网络连接是否已开
//     * true 已打开  false 未打开
//     */
//    public static boolean isConn(Context context) {
//        if (context != null) {
//            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//            if (mNetworkInfo != null) {
//                return mNetworkInfo.isAvailable();
//            }
//            searchNetwork(context);//弹出提示对话框
//        }
//        return false;
//    }
//
//    /**
//     * 判断网络是否连接成功，连接成功不做任何操作
//     * 未连接则弹出对话框提示用户设置网络连接
//     */
//    public static void searchNetwork(final Context context) {
//        //提示对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(context.getResources().getString(R.string.string_net_work)).setMessage(context.getResources().getString(R.string.string_no_net_work))
//                .setPositiveButton(context.getResources().getString(R.string.menu_settings), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = null;
//                        //判断手机系统的版本  即API大于10 就是3.0或以上版本
//                        if (Build.VERSION.SDK_INT > 10) {
//                            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                        } else {
//                            intent = new Intent();
//                            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
//                            intent.setComponent(component);
//                            intent.setAction("android.intent.action.VIEW");
//                        }
//                        context.startActivity(intent);
//                    }
//                }).setNegativeButton(context.getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        }).show();
//    }
//
//
//    private void clearClickTvStyle() {
//        try {
//            if (w30sRunTv != null) {
//                w30sRunTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
//                w30sRunTv.setTextColor(Color.parseColor("#333333"));
//            }
//
//            if (w30sCycleTv != null) {
//                w30sCycleTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
//                w30sCycleTv.setTextColor(Color.parseColor("#333333"));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void onRefresh() {
//        if (ConnectManages.isNetworkAvailable(getActivity()) && watchRunSwipe != null && !watchRunSwipe.isRefreshing()) {
//            watchRunSwipe.setRefreshing(true);
//            getRunLiatDataDB(runTags, df.format(new Date()));
//        } else {
//            if (watchRunSwipe != null)
//                watchRunSwipe.setRefreshing(false);
//        }
//    }
//
//
//    private void getRunLiatDataDB(int runTags, String dataTime) {
//        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac mylanmac
//        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");//
//        DBManager dbManager = MyApp.getInstance().getDBManager();
//        if (dbManager == null)return;
//        DaoSession daoSession = dbManager.getDaoSession();
//        if (daoSession == null)return;
//        List<SportMaps> sportMapsList = daoSession.getSportMapsDao()
//                .queryBuilder().where(SportMapsDao.Properties.Mac.eq(bm),
//                        SportMapsDao.Properties.UserId.eq(userId), SportMapsDao.Properties.Rtc.eq(dataTime)).list();
//        if (sportMapsList == null) {
//            todayDataType.setVisibility(View.VISIBLE);
//            commentRunRecyclerView.setVisibility(View.GONE);
//            return;
//        }
//        List<LatLonBean> latLonBeanList = daoSession.getLatLonBeanDao()
//                .queryBuilder().where(LatLonBeanDao.Properties.Mac.eq(bm),
//                        LatLonBeanDao.Properties.UserId.eq(userId), LatLonBeanDao.Properties.Rtc.eq(dataTime)).list();
//        /**
//         * 计算当月总运动距离
//         */
//        Message message = new Message();
//        message.obj = sportMapsList;
//        message.what = 0x08;
//        handler.sendMessage(message);
//
//        switch (runTags) {
//            case 0:
//                runSportListDB.clear();
//                for (SportMaps ob : sportMapsList) {
//                    if (ob.getType() == 0) {
//                        runSportListDB.add(ob);
//                    }
//                }
//                outDoorSportAdapterDB = new OutDoorSportAdapterNew(runSportListDB, getActivity());
//                commentRunRecyclerView.setAdapter(outDoorSportAdapterDB);
//                /**
//                 * 计算跑步总里程
//                 */
//                Message message1 = new Message();
//                message1.what = 0x09;
//                message1.obj = runSportListDB;
//                handler.sendMessage(message1);
//                outDoorSportAdapterDB.notifyDataSetChanged();
//                showItemClickDB(runSportListDB, latLonBeanList);
//                if (runSportListDB.size() > 0) {
//                    todayDataType.setVisibility(View.GONE);
//                    commentRunRecyclerView.setVisibility(View.VISIBLE);
//                } else {
//                    todayDataType.setVisibility(View.VISIBLE);
//                    commentRunRecyclerView.setVisibility(View.GONE);
//                }
//                break;
//            case 1:
//                runSportListDB.clear();
//                for (SportMaps ob : sportMapsList) {
//                    if (ob.getType() == 1) {
//                        runSportListDB.add(ob);
//                    }
//                }
//                outDoorSportAdapterDB = new OutDoorSportAdapterNew(runSportListDB, getActivity());
//                commentRunRecyclerView.setAdapter(outDoorSportAdapterDB);
//                Message message3 = new Message();
//                message3.what = 0x10;
//                message3.obj = runSportListDB;
//                handler.sendMessage(message3);
//                outDoorSportAdapterDB.notifyDataSetChanged();
//                showItemClickDB(runSportListDB, latLonBeanList);
//                if (runSportListDB.size() > 0) {
//                    todayDataType.setVisibility(View.GONE);
//                    commentRunRecyclerView.setVisibility(View.VISIBLE);
//                } else {
//                    todayDataType.setVisibility(View.VISIBLE);
//                    commentRunRecyclerView.setVisibility(View.GONE);
//                }
//                break;
//        }
//    }
//
//
//    @SuppressLint("HandlerLeak")
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Double distance = 0.0;
//            //总公里数
//            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//            List<SportMaps> runSportListDB = (List<SportMaps>) msg.obj;
//            if (runSportListDB.size() > 0) {
//                todayDataType.setVisibility(View.GONE);
//                commentRunRecyclerView.setVisibility(View.VISIBLE);
//            } else {
//                todayDataType.setVisibility(View.VISIBLE);
//                commentRunRecyclerView.setVisibility(View.GONE);
//            }
//            switch (msg.what) {
//                case 0x08:
//                    distance = 0.0;
//                    for (int i = 0; i < runSportListDB.size(); i++) {
//                        SportMaps sportMaps = runSportListDB.get(i);
//                        if (sportMaps == null) return;
//                        String distances = sportMaps.getDistance();
//                        if (TextUtils.isEmpty(distances)) {
//                            distances = "0";
//                        }
//                        distance += Double.valueOf((TextUtils.isEmpty(distances.trim()) ? "0" : distances.trim()));
//                    }
//                    if (w30sunit) {
//                        w30sTotalKmTv.setText(String.format("%.3f", distance));// 三位小数
//                        textDataUnit.setText(" KM");
//                    } else {
//                        w30sTotalKmTv.setText("" + Math.round(distance * 1000 * 3.28));
//                        textDataUnit.setText(" FT");
//                    }
//                    break;
//                case 0x09:
//                    for (int i = 0; i < runSportListDB.size(); i++) {
//                        String distance1 = runSportListDB.get(i).getDistance();
//                        if (TextUtils.isEmpty(distance1)) {
//                            distance1 = "0";
//                        }
//                        distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1));
//                    }
//                    //总公里数
//                    if (w30sunit) {
//                        w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
//                    } else {
//                        w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");
//                    }
//                    break;
//                case 0x10:
//                    for (int i = 0; i < runSportListDB.size(); i++) {
//                        String distance1 = runSportListDB.get(i).getDistance();
//                        if (TextUtils.isEmpty(distance1)) {
//                            distance1 = "0";
//                        }
//                        distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1.trim()));
//                    }
//                    if (w30sunit) {
//                        w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
//                    } else {
//                        w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");//
//                    }
//
//                    break;
//            }
//        }
//    };
//
//
//    void shouGpsRun() {
//        try {
//            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//            //总公里数
//            if (w30sunit) {
//                //总运动距离
//                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("0.00");
//                if (textDataUnit != null) textDataUnit.setText(" KM");
//            } else {
//                //总运动距离
//                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("" + (0 * 3.28));
//                if (textDataUnit != null) textDataUnit.setText(" FT");
//            }
//        } catch (Exception e) {
//            e.getMessage();
//        }
//
//        getRunLiatDataDB(runTags, df.format(new Date()));
//    }
//
//    /**
//     * 列表点击事件
//     *
//     * @param runSportListDB
//     * @param latLonBeanList
//     */
//    private void showItemClickDB(final List<SportMaps> runSportListDB, final List<LatLonBean> latLonBeanList) {
//        outDoorSportAdapterDB.setListener(new OutDoorSportAdapterNew.OnOutDoorSportItemClickListener() {
//            @Override
//            public void doItemClick(int position) {
//
//
//                Map<String, Object> mapb = new HashMap<>();
//                mapb.put("year", runSportListDB.get(position).getRtc());//日期
//                mapb.put("day", runSportListDB.get(position).getStartTime());//开始日期
//                mapb.put("zonggongli", runSportListDB.get(position).getDistance() + "Km");//总公里
//                if (runSportListDB.get(position).getType() == 0) {
//                    mapb.put("qixing", getResources().getString(R.string.outdoor_running));//骑行或者跑步
//                    mapb.put("image", R.mipmap.huwaipaohuan);//跑步-骑行
//                } else {
//                    mapb.put("qixing", getResources().getString(R.string.outdoor_cycling));//骑行或者跑步
//                    mapb.put("image", R.mipmap.qixinghuan);//跑步-骑行
//                }
//                mapb.put("chixugongli", runSportListDB.get(position).getDistance() + "Km");//持续公里数
//                mapb.put("chixutime", runSportListDB.get(position).getTimeLen());//持续时间
//                mapb.put("kclal", runSportListDB.get(position).getCalories() + "Kcal");//卡路里
//                mapb.put("image", runSportListDB.get(position).getImage());
//                mapb.put("temp", runSportListDB.get(position).getTemp());
//                mapb.put("description", runSportListDB.get(position).getDescription());
//                mapb.put("speed", runSportListDB.get(position).getSpeed());
//                Intent intent = new Intent(getActivity(), MapRecordActivity.class);
////                Intent intent = new Intent(getActivity(), BzlMaps_HistoryActivity.class);
//                Log.d("---------", latLonBeanList.toString() + "\n" + new Gson().toJson(latLonBeanList));
//                intent.putExtra("mapdata", latLonBeanList.get(position).getLatLons().trim());
//                intent.putExtra("mapdata2", new Gson().toJson(mapb));
//                startActivity(intent);
//            }
//        });
//    }
//}
