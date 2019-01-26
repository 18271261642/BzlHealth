package com.bozlun.health.android.siswatch.run;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afa.tourism.greendao.gen.LatLonBeanDao;
import com.afa.tourism.greendao.gen.SportMapsDao;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.activity.wylactivity.MapRecordActivity;
import com.bozlun.health.android.activity.wylactivity.wyl_util.service.ConnectManages;
import com.bozlun.health.android.b30.GPSSportHisyory;
import com.bozlun.health.android.bzlmaps.gaodemaps.BzlGaoDeActivity;
import com.bozlun.health.android.bzlmaps.googlemaps.BzlGoogleMapsActivity;
import com.bozlun.health.android.bzlmaps.mapdb.LatLonBean;
import com.bozlun.health.android.bzlmaps.mapdb.SportMaps;
import com.bozlun.health.android.siswatch.adapter.OutDoorSportAdapterNew;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.LocalizeTool;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.w30s.BaseFragment;
import com.bozlun.health.android.R;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018/4/4.
 */

public class W30sNewRunFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {//} ,RequestView{

    private static final String TAG = "W30sNewRunFragment";

    View w30sRunView;
    //总公里显示
    @BindView(R.id.w30sTotalKmTv)
    TextView w30sTotalKmTv;
    //月的总公里显示
    @BindView(R.id.w30sMonthTotalKmTv)
    TextView w30sMonthTotalKmTv;
    //recyclerView
    @BindView(R.id.commentRunRecyclerView)
    RecyclerView commentRunRecyclerView;
    //
    @BindView(R.id.today_data_type)
    ImageView todayDataType;
    @BindView(R.id.watch_runSwipe)
    SwipeRefreshLayout watchRunSwipe;
    Unbinder unbinder;
    //跑步
    @BindView(R.id.w30sRunTv)
    TextView w30sRunTv;
    //骑行
    @BindView(R.id.w30sCycleTv)
    TextView w30sCycleTv;
    @BindView(R.id.w30sMonthTv)
    TextView w30sMonthTv;
    @BindView(R.id.text_data_unit)
    TextView textDataUnit;
    @BindView(R.id.image_history)
    ImageView imageHistory;


    //    private RequestPressent requestPressent;
    //跑步或者骑行
    private int runTags = 0;
//    private List<OutDoorSportBean> outDoorSportBeanList;
    //临时数据集合
//    private List<OutDoorSportBean> runSportList;

//    private OutDoorSportAdapter outDoorSportAdapter;
    /**
     * 本地化帮助工具
     */
    private LocalizeTool mLocalTool;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Double distance = 0.0;
            //总公里数
            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
            List<SportMaps> runSportListDB = (List<SportMaps>) msg.obj;
            if (runSportListDB.size() > 0) {
                todayDataType.setVisibility(View.GONE);
                commentRunRecyclerView.setVisibility(View.VISIBLE);
            } else {
                todayDataType.setVisibility(View.VISIBLE);
                commentRunRecyclerView.setVisibility(View.GONE);
            }
            switch (msg.what) {
                case 0x08:
                    distance = 0.0;
                    Log.d("-------", runSportListDB.size() + "");
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


//            if (msg.what == 0x01) {
//                List<OutDoorSportBean> tmpOutList = (List<OutDoorSportBean>) msg.obj;
//                distance = 0.0;
//                for (int i = 0; i < tmpOutList.size(); i++) {
//                    OutDoorSportBean outDoorSportBean = tmpOutList.get(i);
//                    if (outDoorSportBean == null) return;
//                    String distances = outDoorSportBean.getDistance();
//                    if (TextUtils.isEmpty(distances)) {
//                        distances = "0";
//                    }
//                    distance += Double.valueOf((TextUtils.isEmpty(distances.trim()) ? "0" : distances.trim()));
//                }
//                w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);
//                if (w30sunit) {
//                    w30sTotalKmTv.setText(String.format("%.3f", distance));// 三位小数
////                    textDataUnit.setText(obtainKmMile());
//                    textDataUnit.setText(" KM");
//                } else {
//                    w30sTotalKmTv.setText("" + Math.round(distance * 1000 * 3.28));
//                    textDataUnit.setText(" FT");
//                }
//            } else if (msg.what == 0x02) {
//                Double distance = 0.0;
//                for (int i = 0; i < runSportList.size(); i++) {
//                    String distance1 = runSportList.get(i).getDistance();
//                    if (TextUtils.isEmpty(distance1)) {
//                        distance1 = "0";
//                    }
//                    distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1));
//                }
//                //总公里数
//                boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//                if (w30sunit) {
////                    w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + obtainKmMile());// 三位小数
//                    w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
//                } else {
//                    w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");
//                }
//                if (runSportList.size() > 0) {
//                    todayDataType.setVisibility(View.GONE);
//                    commentRunRecyclerView.setVisibility(View.VISIBLE);
//                } else {
//                    todayDataType.setVisibility(View.VISIBLE);
//                    commentRunRecyclerView.setVisibility(View.GONE);
//                }
//
//            } else if (msg.what == 0x03) {
//
//                Double distance = 0.0;
//                for (int i = 0; i < runSportList.size(); i++) {
//                    String distance1 = runSportList.get(i).getDistance();
//                    if (TextUtils.isEmpty(distance1)) {
//                        distance1 = "0";
//                    }
//                    distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1.trim()));
//                }
//
//                //总公里数
//                boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//                if (w30sunit) {
////                    w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + obtainKmMile());// 三位小数
//                    w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + " KM");// 三位小数
//                } else {
//                    w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");//
//                }
//                if (runSportList.size() > 0) {
//                    todayDataType.setVisibility(View.GONE);
//                    commentRunRecyclerView.setVisibility(View.VISIBLE);
//                } else {
//                    todayDataType.setVisibility(View.VISIBLE);
//                    commentRunRecyclerView.setVisibility(View.GONE);
//                }
//            }
        }
    };


    private static W30sNewRunFragment fragment = null;

    public static synchronized W30sNewRunFragment newInstance() {
        if (fragment == null) fragment = new W30sNewRunFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        w30sRunView = inflater.inflate(R.layout.fragment_new_run_layout, container, false);
        unbinder = ButterKnife.bind(this, w30sRunView);
        mLocalTool = new LocalizeTool(getActivity());

        try {
            initViews();
            initData();


        } catch (Exception e) {
            e.getMessage();
        }


        return w30sRunView;
    }

    private void initViews() {
        LinearLayoutManager linm = new LinearLayoutManager(getActivity());
        linm.setOrientation(LinearLayoutManager.VERTICAL);
        commentRunRecyclerView.setLayoutManager(linm);
        commentRunRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        runSportListDB = new ArrayList<>();
//        outDoorSportBeanList = new ArrayList<>();
//        outDoorSportAdapter = new OutDoorSportAdapter(outDoorSportBeanList, getActivity());
//        commentRunRecyclerView.setAdapter(outDoorSportAdapter);
        watchRunSwipe.setOnRefreshListener(this);
        clearClickTvStyle();
        w30sRunTv.setTextColor(getResources().getColor(R.color.white));
        w30sRunTv.setBackgroundResource(R.drawable.newh9data_unselecte_text_shap);

        imageHistory.setVisibility(View.VISIBLE);
    }

    private void clearClickTvStyle() {
        try {
            w30sRunTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
            w30sRunTv.setTextColor(Color.parseColor("#333333"));

            w30sCycleTv.setBackgroundResource(R.drawable.newh9data_selecte_text_shap);
            w30sCycleTv.setTextColor(Color.parseColor("#333333"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
//        requestPressent = new RequestPressent();
//        requestPressent.attach(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Log.e(TAG,"--------isV="+isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
            //总公里数
//            boolean w30sunit = (boolean) SharedPreferenceUtil.get(getContext(), "w30sunit", true);
            if (w30sunit) {
                //总运动距离
                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("0.00");
//                if (textDataUnit != null) textDataUnit.setText(obtainKmMile());
                if (textDataUnit != null) textDataUnit.setText(" KM");
                if (w30sMonthTotalKmTv != null) w30sMonthTotalKmTv.setText("0.0" + " KM");
            } else {
                //总运动距离
                if (w30sTotalKmTv != null) w30sTotalKmTv.setText("" + (0 * 3.28));
                if (textDataUnit != null) textDataUnit.setText(" FT");
                if (w30sMonthTotalKmTv != null) w30sMonthTotalKmTv.setText("0.0 FT");
            }

        } catch (Exception e) {
            e.getMessage();
        }
//        getRunMapListData(runTags);    //获取地图的历史记录


        getRunLiatDataDB(runTags, df.format(new Date()));
    }


    /**
     * 获取公制|英制单位
     */
    private String obtainKmMile() {
        boolean isMetric = mLocalTool.getMetricSystem();
        return isMetric ? getString(R.string.km) : getString(R.string.mileage_setmi);
    }

//    private void getRunMapListData(int runTags) {
//        try {
//            String url = "http://47.90.83.197:8080/watch/sport/getOutdoorSport";
//            JSONObject jsono = null;
//            jsono = new JSONObject();
//            jsono.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
//            jsono.put("date", WatchUtils.getCurrentDate());
//            if (requestPressent != null) {
//                requestPressent.getRequestJSONObject(1, url, getActivity(), jsono.toString(), runTags);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (requestPressent != null) {
//            requestPressent.detach();
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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

    @OnClick({R.id.w30sRunImg, R.id.w30sCycleImg, R.id.w30sRunTv, R.id.w30sCycleTv, R.id.image_history})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.w30sRunImg:   //跑步图片
                isConn(getContext());
                boolean b = initGPS();
                if (b) {
                    SharedPreferencesUtils.saveObject(getActivity(), "type", "0");
//                    startActivity(new Intent(getActivity(), OutdoorCyclingActivityStar.class));
                    Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
                    if (zhon) {
                        startActivity(new Intent(getContext(), BzlGaoDeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        startActivity(new Intent(getContext(), BzlGoogleMapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }

                } else {
                    //Toast.makeText(getContext(), "请打开GPS", Toast.LENGTH_SHORT).show();
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
//                startActivity(new Intent(getActivity(), SportsHistoryActivity.class));
                startActivity(new Intent(getActivity(), GPSSportHisyory.class));
                break;
        }
    }

    private List<SportMaps> runSportListDB;
    private OutDoorSportAdapterNew outDoorSportAdapterDB;

    private void getRunLiatDataDB(int runTags, String dataTime) {
        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac mylanmac
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");//
        if (WatchUtils.isEmpty(bm)) return;
        if (WatchUtils.isEmpty(userId)) return;
        final List<SportMaps> sportMapsList = MyApp.getInstance().getDBManager().getDaoSession().getSportMapsDao()
                .queryBuilder().where(SportMapsDao.Properties.Mac.eq(bm),
                        SportMapsDao.Properties.UserId.eq(userId), SportMapsDao.Properties.Rtc.eq(dataTime)).list();
        Log.d("=========", sportMapsList.size() + "===" + sportMapsList.toString());
        if (sportMapsList == null) {
            todayDataType.setVisibility(View.VISIBLE);
            commentRunRecyclerView.setVisibility(View.GONE);
            return;
        }
        List<LatLonBean> latLonBeanList = MyApp.getInstance().getDBManager().getDaoSession().getLatLonBeanDao()
                .queryBuilder().where(LatLonBeanDao.Properties.Mac.eq(bm),
                        LatLonBeanDao.Properties.UserId.eq(userId), LatLonBeanDao.Properties.Rtc.eq(dataTime)).list();
        Log.d("=========latLonBeanList", latLonBeanList.size() + "===" + latLonBeanList.toString());
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
                handler.sendMessage(message1);
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
                handler.sendMessage(message3);
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

        /**
         * 计算当月总运动距离
         */
        Message message = new Message();
        message.obj = sportMapsList;
        message.what = 0x08;
        handler.sendMessage(message);


    }

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


//
//    @Override
//    public void showLoadDialog(int what) {
//
//    }


//    @Override
//    public void successData(int what, Object object, int runtag) {
//
//        if (object.toString() != null) {
//            try {
//                JSONObject jsonObject = new JSONObject(object.toString());
//                if (jsonObject.getString("resultCode").equals("001")) {
//                    String outdoorSport = jsonObject.getString("outdoorSport");
//                    if (outdoorSport != null && !outdoorSport.equals("[]")) {
//                        todayDataType.setVisibility(View.GONE);
//                        commentRunRecyclerView.setVisibility(View.VISIBLE);
//                        outDoorSportBeanList = new Gson().fromJson(outdoorSport, new TypeToken<List<OutDoorSportBean>>() {
//                        }.getType());
//                        showToMonthSportData(outDoorSportBeanList); //计算当月总运动距离
//                        //DataRunState();
//                        runSportList = new ArrayList<>();
//                        if (runtag == 0) {  //跑步
//                            runSportList.clear();
//                            for (OutDoorSportBean ob : outDoorSportBeanList) {
//                                if (ob.getType() == 0) {
//                                    runSportList.add(ob);
//                                }
//                            }
//                            outDoorSportAdapter = new OutDoorSportAdapter(runSportList, getActivity());
//                            commentRunRecyclerView.setAdapter(outDoorSportAdapter);
//                            //showSportComputData(runSportList);
//                            //计算跑步总里程
//                            //BigDecimal bigDecimal = new BigDecimal("0.00");
//                            Message message = new Message();
//                            message.what = 0x02;
//                            message.obj = runSportList;
//                            handler.sendMessage(message);
////                            Double distance = 0.0;
////                            for (int i = 0; i < runSportList.size(); i++) {
////                                String distance1 = runSportList.get(i).getDistance();
////                                if (TextUtils.isEmpty(distance1)) {
////                                    distance1 = "0";
////                                }
////                                distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1));
//////                                BigDecimal bigD = new BigDecimal(runSportList.get(i).getDistance());
//////                                bigDecimal = bigD.add(bigDecimal);
////                            }
////                            //总公里数
////                            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, false);//是否为公制
//////                            boolean w30sunit = (boolean) SharedPreferenceUtil.get(getContext(), "w30sunit", true);
////                            if (w30sunit) {
//////                                double metric = Math.round(bigDecimal.doubleValue()) * Constant.METRIC_MILE;
////                                w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + obtainKmMile());// 三位小数
//////                                w30sMonthTotalKmTv.setText("" + Math.round(bigDecimal.doubleValue()) + obtainKmMile());
////                            } else {
////                                w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");
//////                                w30sMonthTotalKmTv.setText("" + Math.round((bigDecimal.doubleValue() * 1000 * 3.28)) + " ft");
////                            }
////                            //w30sMonthTotalKmTv.setText("" + bigDecimal.doubleValue() + "km");
////                            if (runSportList.size() > 0) {
////                                todayDataType.setVisibility(View.GONE);
////                                commentRunRecyclerView.setVisibility(View.VISIBLE);
////                            } else {
////                                todayDataType.setVisibility(View.VISIBLE);
////                                commentRunRecyclerView.setVisibility(View.GONE);
////                            }
//                            outDoorSportAdapter.notifyDataSetChanged();
//                            showItemClick(runSportList);
//                        } else if (runtag == 1) {  //骑行
//                            runSportList.clear();
//                            for (OutDoorSportBean ob : outDoorSportBeanList) {
//                                if (ob.getType() == 1) {
//                                    runSportList.add(ob);
//                                }
//                            }
//                            outDoorSportAdapter = new OutDoorSportAdapter(runSportList, getActivity());
//                            commentRunRecyclerView.setAdapter(outDoorSportAdapter);
//                            //showSportComputData(runSportList);
//                            //计算骑行总里程
//                            //BigDecimal bigDecimal = new BigDecimal("0.00");
//                            Message message = new Message();
//                            message.what = 0x03;
//                            message.obj = runSportList;
//                            handler.sendMessage(message);
////                            Double distance = 0.0;
////                            for (int i = 0; i < runSportList.size(); i++) {
////                                String distance1 = runSportList.get(i).getDistance();
////                                if (TextUtils.isEmpty(distance1)) {
////                                    distance1 = "0";
////                                }
////                                distance += Double.valueOf((TextUtils.isEmpty(distance1.trim()) ? "0" : distance1.trim()));
//////                                BigDecimal bigD = new BigDecimal(runSportList.get(i).getDistance());
//////                                bigDecimal = bigD.add(bigDecimal);
////                            }
////
////                            //总公里数
////                            boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, false);//是否为公制
//////                            boolean w30sunit = (boolean) SharedPreferenceUtil.get(getContext(), "w30sunit", true);
////                            if (w30sunit) {
//////                                double metric = Math.round(bigDecimal.doubleValue()) * Constant.METRIC_MILE;
////                                w30sMonthTotalKmTv.setText(String.format("%.3f", distance) + obtainKmMile());// 三位小数
//////                                w30sMonthTotalKmTv.setText("" + Math.round(bigDecimal.doubleValue()) + obtainKmMile());
////                            } else {
////                                w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + " FT");//
//////                                w30sMonthTotalKmTv.setText(String.format("%.3f", (distance * 1000 * 3.28)) + obtainKmMile());// 三位小数
//////                                w30sMonthTotalKmTv.setText("" + Math.round((bigDecimal.doubleValue() * 1000 * 3.28)) + " ft");
////                            }
////
////
////                            if (runSportList.size() > 0) {
////                                todayDataType.setVisibility(View.GONE);
////                                commentRunRecyclerView.setVisibility(View.VISIBLE);
////                            } else {
////                                todayDataType.setVisibility(View.VISIBLE);
////                                commentRunRecyclerView.setVisibility(View.GONE);
////                            }
//                            outDoorSportAdapter.notifyDataSetChanged();
//                            showItemClick(runSportList);
//                        } else {
//                            runSportList.clear();
//                            outDoorSportAdapter = new OutDoorSportAdapter(outDoorSportBeanList, getActivity());
//                            commentRunRecyclerView.setAdapter(outDoorSportAdapter);
//                            outDoorSportAdapter.notifyDataSetChanged();
//                            //showSportComputData(outDoorSportBeanList);
//                            showItemClick(outDoorSportBeanList);
//                        }
//                    } else {
//                        todayDataType.setVisibility(View.VISIBLE);
//                        commentRunRecyclerView.setVisibility(View.GONE);
//                        //总公里数
//                        boolean w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, true);//是否为公制
//                        //boolean w30sunit = (boolean) SharedPreferenceUtil.get(getContext(), "w30sunit", true);
//                        if (w30sunit) {
//                            //总运动距离
//                            w30sTotalKmTv.setText("" + 0.00);
////                            textDataUnit.setText(obtainKmMile());
//                            textDataUnit.setText(" KM");
//                        } else {
//                            //总运动距离
//                            w30sTotalKmTv.setText("" + (0 * 3.28));
//                            textDataUnit.setText(" FT");
//                        }
//
//                    }
//
//
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void showItemClick(final List<OutDoorSportBean> runSportList) {
//        outDoorSportAdapter.setListener(new OutDoorSportAdapter.OnOutDoorSportItemClickListener() {
//            @Override
//            public void doItemClick(int position) {
//                Map<String, Object> mapb = new HashMap<>();
//                mapb.put("year", runSportList.get(position).getRtc());//日期
//                mapb.put("day", runSportList.get(position).getStartTime());//开始日期
//                mapb.put("zonggongli", runSportList.get(position).getDistance() + "Km");//总公里
//                if (runSportList.get(position).getType() == 0) {
//                    mapb.put("qixing", getResources().getString(R.string.outdoor_running));//骑行或者跑步
//                    mapb.put("image", R.mipmap.huwaipaohuan);//跑步-骑行
//                } else {
//                    mapb.put("qixing", getResources().getString(R.string.outdoor_cycling));//骑行或者跑步
//                    mapb.put("image", R.mipmap.qixinghuan);//跑步-骑行
//                }
//                mapb.put("chixugongli", runSportList.get(position).getDistance() + "Km");//持续公里数
//                mapb.put("chixutime", runSportList.get(position).getTimeLen());//持续时间
//                mapb.put("kclal", runSportList.get(position).getCalories() + "Kcal");//卡路里
//                mapb.put("image", runSportList.get(position).getImage());
//                mapb.put("temp", runSportList.get(position).getTemp());
//                mapb.put("description", runSportList.get(position).getDescription());
//                mapb.put("speed", runSportList.get(position).getSpeed());
//                Intent intent = new Intent(getActivity(), MapRecordActivity.class);
////                Intent intent = new Intent(getActivity(), BzlMaps_HistoryActivity.class);
//                intent.putExtra("mapdata", runSportList.get(position).getLatLons().toString());
//                intent.putExtra("mapdata2", new Gson().toJson(mapb));
//                startActivity(intent);
//            }
//        });
//    }
//
//
//    //计算总运动距离
//    private void showToMonthSportData(final List<OutDoorSportBean> outDoorSportBeanList) {
//        Message message = new Message();
//        message.obj = outDoorSportBeanList;
//        message.what = 0x01;
//        handler.sendMessage(message);
////        bigDecimal = new BigDecimal("0.00");
////        for (int i = 0; i < outDoorSportBeanList.size(); i++) {
////            BigDecimal bigD = new BigDecimal(outDoorSportBeanList.get(i).getDistance());
////            bigDecimal = bigD.add(bigDecimal);
////        }
////        //总公里数
////        w30sunit = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISSystem, false);//是否为公制
////        //boolean w30sunit = (boolean) SharedPreferenceUtil.get(getContext(), "w30sunit", true);
////        MyLogUtil.d("-------", bigDecimal.doubleValue() + "");
////        getActivity().runOnUiThread(new Runnable() {
////            @Override
////            public void run() {
////                if (w30sunit) {
////                    //w30sTotalKmTv.setText("" + Math.round(bigDecimal.doubleValue()));
////                    //double metric = Math.round(bigDecimal.doubleValue()) * Constant.METRIC_MILE;
////                    w30sTotalKmTv.setText(String.format("%.3f", bigDecimal.doubleValue()));// 三位小数
////                    textDataUnit.setText(obtainKmMile());
////                } else {
////                    w30sTotalKmTv.setText("" + Math.round((bigDecimal.doubleValue() * 1000 * 3.28)));
////                    textDataUnit.setText(" FT");
////                }
////            }
////        });
////        //总运动距离
////        // w30sTotalKmTv.setText("" + bigDecimal.doubleValue() + "");
//    }
//
//    @Override
//    public void failedData(int what, Throwable e) {
//
//    }
//
//    @Override
//    public void closeLoadDialog(int what) {
//
//    }

    //刷新
    @Override
    public void onRefresh() {
        if (ConnectManages.isNetworkAvailable(getActivity()) && watchRunSwipe != null && !watchRunSwipe.isRefreshing()) {
            watchRunSwipe.setRefreshing(true);
//            getRunMapListData(runTags);
            getRunLiatDataDB(runTags, df.format(new Date()));
        } else {
            if (watchRunSwipe != null)
                watchRunSwipe.setRefreshing(false);
        }
    }
}
