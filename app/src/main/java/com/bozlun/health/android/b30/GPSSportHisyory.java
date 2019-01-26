package com.bozlun.health.android.b30;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.afa.tourism.greendao.gen.LatLonBeanDao;
import com.afa.tourism.greendao.gen.SportMapsDao;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.wylactivity.MapRecordActivity;
import com.bozlun.health.android.bzlmaps.mapdb.LatLonBean;
import com.bozlun.health.android.bzlmaps.mapdb.SportMaps;
import com.bozlun.health.android.siswatch.adapter.OutDoorSportAdapterNew;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPSSportHisyory extends AppCompatActivity implements View.OnClickListener {
    private FrameLayout frmBack;
    private ImageView imageDate;
    private RecyclerView commentRunRecyclerView;
    private ImageView imageNoData;
    @SuppressLint("SimpleDateFormat")
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String selectTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpssport_history);
        initViews();
        selectTime = df1.format(new Date());
    }

    protected void initViews() {
        frmBack = findViewById(R.id.frm_back);
        imageDate = findViewById(R.id.image_data);
        commentRunRecyclerView = findViewById(R.id.rec_list);
        LinearLayoutManager linm = new LinearLayoutManager(this);
        linm.setOrientation(LinearLayoutManager.VERTICAL);
        commentRunRecyclerView.setLayoutManager(linm);
        commentRunRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        imageNoData = findViewById(R.id.image_no_data);
        frmBack.setOnClickListener(this);
        imageDate.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finHisData(df.format(new Date()));
    }

    private List<SportMaps> sportMapsList = null;
    private List<LatLonBean> latLonBeanList = null;
    private OutDoorSportAdapterNew outDoorSportAdapterDB;


    /**
     * 从数据看查询数据
     */
    private void finHisData(String dateTime) {
        String bm = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);//设备mac mylanmac
        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), "userId");//
        if (WatchUtils.isEmpty(userId)) return;
        if (WatchUtils.isEmpty(bm)) return;
        sportMapsList = MyApp.getInstance().getDBManager().getDaoSession().getSportMapsDao()
                .queryBuilder().where(SportMapsDao.Properties.Mac.eq( bm),
                        SportMapsDao.Properties.UserId.eq(userId), SportMapsDao.Properties.Rtc.eq(dateTime)).list();
        if (popupWindow != null && popupWindow.isShowing()) popupWindow.dismiss();
        if (sportMapsList == null || sportMapsList.size() <= 0) {
            imageNoData.setVisibility(View.VISIBLE);
            commentRunRecyclerView.setVisibility(View.GONE);
            return;
        }
        imageNoData.setVisibility(View.GONE);
        commentRunRecyclerView.setVisibility(View.VISIBLE);
        latLonBeanList = MyApp.getInstance().getDBManager().getDaoSession().getLatLonBeanDao()
                .queryBuilder().where(LatLonBeanDao.Properties.Mac.eq(bm),
                        LatLonBeanDao.Properties.UserId.eq(userId), LatLonBeanDao.Properties.Rtc.eq(dateTime)).list();
        outDoorSportAdapterDB = new OutDoorSportAdapterNew(sportMapsList, this);
        commentRunRecyclerView.setAdapter(outDoorSportAdapterDB);
        outDoorSportAdapterDB.notifyDataSetChanged();
        showItemClickDB(sportMapsList, latLonBeanList);
    }

    /**
     * list item 点击事件
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
                Intent intent = new Intent(GPSSportHisyory.this, MapRecordActivity.class);
//                Intent intent = new Intent(getActivity(), BzlMaps_HistoryActivity.class);
                Log.d("---------", latLonBeanList.toString() + "\n" + new Gson().toJson(latLonBeanList));
                intent.putExtra("mapdata", latLonBeanList.get(position).getLatLons().trim());
                intent.putExtra("mapdata2", new Gson().toJson(mapb));
                startActivity(intent);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frm_back:
                finish();
                break;
            case R.id.image_data:
                CalendarView calendarView = showPop(view);
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                        if (sportMapsList != null) sportMapsList.clear();
                        if (latLonBeanList != null) latLonBeanList.clear();
                        String m = "";
                        String d = "";
                        if ((i1 + 1) < 10) {
                            m = "0" + (i1 + 1);
                        } else {
                            m = "" + (i1 + 1);
                        }
                        if (i2 < 10) {
                            d = "0" + i2;
                        } else {
                            d = "" + i2;
                        }
                        selectTime = i + "-" + m + "-" + d + " 00:00:00";
                        finHisData(i + "-" + m + "-" + d);
                    }
                });
                break;
        }
    }


    private PopupWindow popupWindow = null;

    /**
     * 设置pop
     *
     * @param view
     * @return
     */
    CalendarView showPop(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentview = inflater.inflate(R.layout.date_time_popupwidow, null);
        contentview.getBackground().setAlpha(150);
        popupWindow = new PopupWindow(contentview,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        popupWindow.setBackgroundDrawable(dw);
//        contentview.setOnKeyListener(this);
        popupWindow.showAtLocation(view, Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
        CalendarView c = contentview.findViewById(R.id.calend_views);
        long l = dataOne(selectTime);
        if (l != 0) c.setDate(l);
        return c;
    }

    /**
     * 调此方法输入所要转换的时间输入例如（"2014-06-14-16-09-00"）返回时间戳 1539964800
     *
     * @param time
     * @return
     */
    public static long dataOne(String time) {
        long ts = 0;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(time);
            ts = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;
    }

    /**
     * 监听Back键按下事件
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


}
