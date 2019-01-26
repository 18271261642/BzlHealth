package com.bozlun.health.android.siswatch.run;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afa.tourism.greendao.gen.B30DevicesSportDao;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.bean.B30DevicesSport;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.ISportModelOriginListener;
import com.veepoo.protocol.listener.data.ISportModelStateListener;
import com.veepoo.protocol.model.datas.SportModelOriginHeadData;
import com.veepoo.protocol.model.datas.SportModelOriginItemData;
import com.veepoo.protocol.model.datas.SportModelStateData;
import com.veepoo.protocol.model.enums.ESportModelStateStauts;

import java.text.DecimalFormat;
import java.util.List;


public class B30DevicesFragment extends Fragment {
    private ImageView imageView = null;
    private TextView kaluli_text = null;
    private ListView list_kcluli = null;
    private TextView shuo_text = null;

    private static B30DevicesFragment fragment = null;
    private boolean isPlay = false;
    private VPOperateManager vpOperateManager;

    public static synchronized B30DevicesFragment newInstance() {
        if (fragment == null) fragment = new B30DevicesFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.b30_devices_fragment, container, false);

        initView(view);
        return view;
    }


    protected void initView(View root) {
        vpOperateManager = MyApp.getInstance().getVpOperateManager();
        imageView = root.findViewById(R.id.devices_run);
        kaluli_text = root.findViewById(R.id.kaluli_text);
        list_kcluli = root.findViewById(R.id.list_kcluli);
        shuo_text = root.findViewById(R.id.shuo_text);
//        boolean param = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, false);//是否为公制
//        if (!param) {
//            shuo_text.setText("0"+getResources().getString(R.string.string_all_rkcal)+"(mile)");
//        } else {
//            shuo_text.setText("0"+getResources().getString(R.string.string_all_rkcal)+"("+getResources().getString(R.string.km_cal)+")");
//        }
        shuo_text.setText("0"+getResources().getString(R.string.string_all_rkcal)+"("+getResources().getString(R.string.km_cal)+")");
    }


    void findDatas() {
//        B30DevicesSportDao b30DevicesSportDao = MyApp.getDBManager().getDaoSession().getB30DevicesSportDao();
        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac
        final List<B30DevicesSport> b30DevicesSports = MyApp.getInstance().getDBManager().getDaoSession().getB30DevicesSportDao().queryBuilder().where(B30DevicesSportDao.Properties.Address.eq(bm)).list();

//        if (b30DevicesSportDao != null) {
//            final List<B30DevicesSport> b30DevicesSports = b30DevicesSportDao.loadAll();
        if (!b30DevicesSports.isEmpty()) {
            double kcals = 0.0;
            for (int i = 0; i < b30DevicesSports.size(); i++) {
                kcals += b30DevicesSports.get(i).getKcals();
            }
            kaluli_text.setText(new DecimalFormat("0.0").format(kcals));
            list_kcluli.setAdapter(new BaseAdapter() {
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
//                    boolean param = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, false);//是否为公制
//                    if (!param) {
//                        double metric = Math.round(b30DevicesSports.get(i).getKcals()) * Constant.METRIC_MILE;
//                        textViewKcal.setText(String.format("%.1f", metric) + getResources().getString(R.string.mileage_setmi));
//                    } else {
//                        textViewKcal.setText(b30DevicesSports.get(i).getKcals() + getResources().getString(R.string.km_cal));
//                    }
                    textViewKcal.setText(b30DevicesSports.get(i).getKcals() + getResources().getString(R.string.km_cal));

                    textViewHeart.setText(b30DevicesSports.get(i).getAverRate() + "bpm");
                    textViewStep.setText(b30DevicesSports.get(i).getStepCount() + getResources().getString(R.string.daily_numberofsteps_default));
                    textViewDataTime.setText(b30DevicesSports.get(i).getStartTime());

                    return view1;
                }
            });
//            }


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

    void loadData() {

        vpOperateManager.readSportModelOrigin(iBleWriteResponse, new ISportModelOriginListener() {
            @Override
            public void onReadOriginProgress(float v) {

            }

            @Override
            public void onReadOriginProgressDetail(int i, String s, int i1, int i2) {
//                MyLogUtil.d("-------数据--0----", i + "==" + s + "==" + i1 + "==" + i2);
            }

            @Override
            public void onHeadChangeListListener(SportModelOriginHeadData sportModelOriginHeadData) {
//                MyLogUtil.d("-------数据------",
//                        "\n日期:" + sportModelOriginHeadData.getDate()
//                                + "\n开始时间:" + sportModelOriginHeadData.getStartTime().getDate()
//                                + "\n停止时间:" + sportModelOriginHeadData.getStopTime().getDate()
//                                + "\n运动时间:" + sportModelOriginHeadData.getSportTime()
//                                + "\n步数:" + sportModelOriginHeadData.getStepCount()
//                                + "\n运动计数:" + sportModelOriginHeadData.getSportCount()
//                                + "\n千卡:" + sportModelOriginHeadData.getKcals()
//                                + "\n距离:" + sportModelOriginHeadData.getDistance()
//                                + "\n记录计数:" + sportModelOriginHeadData.getRecordCount()
//                                + "\n暂停计数:" + sportModelOriginHeadData.getPauseCount()
//                                + "\n暂停时长:" + sportModelOriginHeadData.getPauseTime()
//                                + "\nCRC:" + sportModelOriginHeadData.getCrc()
//                                + "\n配速:" + sportModelOriginHeadData.getPeisu()
//                                + "\n牛运动时:" + sportModelOriginHeadData.getOxsporttimes()
//                                + "\n心率:" + sportModelOriginHeadData.getAverRate());
//
//                /**
//                 10-09 13:18:40.212 17801-17801/com.guider.ringmiihx I/-------数据------: 日期2018-09-27
//                 开始时间TimeData [2018-09-27 11:21:26]
//                 停止时间TimeData [2018-09-27 11:32:26]
//                 运动时间660
//                 步数0
//                 运动计数77
//                 千卡0.0
//                 距离0.0
//                 记录计数11
//                 暂停计数0
//                 暂停时长0
//                 CRC28285
//                 配速0
//                 牛运动时660
//                 心率106
//                 */

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

//                        if (!b30DevicesSports.get(i).getStartTime().equals(
//                                sportModelOriginHeadData.getStartTime().getYear()
//                                        + "-" + sportModelOriginHeadData.getStartTime().getMonth()
//                                        + "-" + sportModelOriginHeadData.getStartTime().getDay()
//                                        + " " + sportModelOriginHeadData.getStartTime().getHour()
//                                        + ":" + sportModelOriginHeadData.getStartTime().getMinute()
//                                        + ":" + sportModelOriginHeadData.getStartTime().getSecond())) {
//                            MyApp.getDBManager().getDaoSession().getB30DevicesSportDao().insert(db);
//                        }
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


    @Override
    public void onResume() {
        super.onResume();

//        MyLogUtil.d("-------onResume------", "onResume");
        MyApp.getInstance().getVpOperateManager().readSportModelState(iBleWriteResponse, new ISportModelStateListener() {
            @Override
            public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                MyLogUtil.d("-------readSportModelState------", sportModelStateData.toString());
                if (sportModelStateData.getDeviceStauts().equals(ESportModelStateStauts.DEVICE_HAD_START_BEFORE)) {
                    imageView.setImageResource(R.mipmap.ic_stop_devices);
                    isPlay = true;
                } else {
                    isPlay = false;
                    imageView.setImageResource(R.mipmap.ic_play_devices);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay) {
                    isPlay = true;
                    imageView.setImageResource(R.mipmap.ic_stop_devices);
                    MyApp.getInstance().getVpOperateManager().startSportModel(iBleWriteResponse, new ISportModelStateListener() {
                        @Override
                        public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                            MyLogUtil.d("-------startSportModel------", sportModelStateData.toString());
                        }
                    });
                } else {
                    isPlay = false;
                    imageView.setImageResource(R.mipmap.ic_play_devices);

                    MyApp.getInstance().getVpOperateManager().stopSportModel(iBleWriteResponse, new ISportModelStateListener() {
                        @Override
                        public void onSportModelStateChange(SportModelStateData sportModelStateData) {
//                            MyLogUtil.d("-------stopSportModel------", sportModelStateData.toString());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    MyLogUtil.d("-------stopSportModel------", "获取运动模式数据");
                                    loadData();
                                }
                            }, 500);
                        }
                    });


                }

            }
        });


        findDatas();
    }


    IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };


    @Override
    public void onStop() {
        super.onStop();
//        MyLogUtil.d("-------onStop------", "onStop");
    }
}
