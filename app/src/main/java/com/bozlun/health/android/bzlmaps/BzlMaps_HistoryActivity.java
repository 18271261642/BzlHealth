//package com.example.bozhilun.android.bzlmaps;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.annotation.Nullable;
//import android.support.v4.app.FragmentActivity;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Chronometer;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.amap.api.maps.AMap;
//import com.amap.api.maps.CameraUpdate;
//import com.amap.api.maps.CameraUpdateFactory;
//import com.amap.api.maps.MapView;
//import com.amap.api.maps.model.CameraPosition;
//import com.amap.api.maps.model.PolylineOptions;
//import com.example.bozhilun.android.R;
//import com.example.bozhilun.android.activity.wylactivity.MapRecordActivity;
//import com.example.bozhilun.android.activity.wylactivity.wyl_util.ScreenShot;
//import com.example.bozhilun.android.bzlmaps.gaodemaps.BzlGaoDeActivity;
//import com.example.bozhilun.android.bzlmaps.googlemaps.BzlGoogleMapsActivity;
//import com.example.bozhilun.android.util.Common;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.lidroid.xutils.BitmapUtils;
//import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
//import com.lidroid.xutils.bitmap.core.BitmapSize;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class BzlMaps_HistoryActivity extends FragmentActivity implements OnMapReadyCallback {
//    String value, value2;
//
//    @BindView(R.id.huwaiqixing_years)
//    TextView Starttime;
//    /**
//     * 开始时间
//     */
//    @BindView(R.id.qichekong_qizhiliangyy)
//    TextView Kongqi;
//    /**
//     * 空气质量
//     */
//    @BindView(R.id.qiche_kongqiyu)
//    ImageView TianqiImage;
//    /**
//     * 天气图片
//     */
//    @BindView(R.id.qiche_wendu)
//    TextView Wendu;
//    /**
//     * 温度
//     */
//    @BindView(R.id.test_chronometer_times)
//    Chronometer Duration;
//    /**
//     * 总计时间
//     */
//    @BindView(R.id.test_full_kilometer)
//    TextView Fullkilometer;
//    /**
//     * 总公里
//     */
//    @BindView(R.id.test_peisu)
//    TextView Pace;
//    /**
//     * 配速
//     */
//    @BindView(R.id.test_xiaohao_kclal)
//    TextView Consume;
//    /**
//     * 消耗
//     */
//    @BindView(R.id.qiche_mypmyy)
//    LinearLayout linearLayoutONE;
//    /**
//     * p25视图
//     */
//    @BindView(R.id.qichemypm_www)
//    LinearLayout linearLayoutTwo;
//    /**
//     * 温度视图
//     */
//    @BindView(R.id.test_huwaiqixing_ditut)
//    RelativeLayout linearLayoutThere;
//    /**
//     * 温度视图
//     */
//    @BindView(R.id.qixingshu_waipao_bustar)
//    LinearLayout Back;
//    /**
//     * 返回
//     */
//
//    @BindView(R.id.qixingfugai_hostory)
//    ImageView fugaiwu;
//    @BindView(R.id.shuwaipao_buhuwai)
//    TextView BIAOTI;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maprecord);
//        ButterKnife.bind(this);
//        Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
//        if (zhon) {
//            startGaoDeMap(savedInstanceState);
//        } else {
//            starGooGleMap();
//        }
//        linearLayoutONE.getBackground().setAlpha(120);
//        linearLayoutTwo.getBackground().setAlpha(120);
//        linearLayoutThere.getBackground().setAlpha(120);
//        Intent _intent = getIntent();
//        value = _intent.getStringExtra("mapdata");//这里是经纬度
//        value2 = _intent.getStringExtra("mapdata2");//这里是其他数据
//    }
//
//
//    private MapView mapView;
//    private AMap aMap;
//    com.amap.api.maps.model.LatLng ll, oldll;
//    Bitmap bm;
//
//    void startGaoDeMap(Bundle savedInstanceState) {
//        mapView = (MapView) findViewById(R.id.test_qixing_map);
//        mapView.onCreate(savedInstanceState);
//        mapView.setVisibility(View.VISIBLE);
//        if (aMap == null) aMap = mapView.getMap();
//
//        try {
//            JSONObject JSONO = new JSONObject(value2);
//            Starttime.setText(JSONO.optString("day").toString());
//            if ("良".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.good));
//            } else if ("轻度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.mild_pollution));
//            } else if ("中度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.moderate_pollution));
//            } else if ("重度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.heavy_pollution));
//            } else if ("严重污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.serious_pollution));
//            }
//            Wendu.setText(JSONO.optString("temp").toString());
//            BIAOTI.setText(JSONO.optString("qixing").toString());
//            Duration.setText(JSONO.optString("chixutime").toString());
//            Fullkilometer.setText(JSONO.optString("zonggongli").toString());
//            Pace.setText(getResources().getString(R.string.paces) + JSONO.optString("speed").toString());
//            Consume.setText(getResources().getString(R.string.XIAOHAO) + JSONO.optString("kclal").toString());
//            System.out.print("inmage" + JSONO.optString("image").toString());
//            BitmapUtils bitmapUtils = new BitmapUtils(BzlMaps_HistoryActivity.this);
//            BitmapDisplayConfig config = new BitmapDisplayConfig();
//            // 设置图片的分辨率
//            BitmapSize size = new BitmapSize(500, 500);
//            config.setBitmapMaxSize(size);
//            bitmapUtils.display(TianqiImage, JSONO.optString("image").toString());
//            //解析地图轨迹
//            JSONArray Mapdata = new JSONArray(value);
//            for (int i = 0; i < Mapdata.length(); i++) {
//                JSONObject jo = (JSONObject) Mapdata.get(i);
//                String rtc = jo.optString("lon").toString();//纬度
//                String jindu = jo.optString("lat").toString();//经度
//                //MyLogUtil.i("response"+rtc+"rrrrrrrr"+jindu);
//                if (i == 0) {
//                    if (aMap == null) {
//                        aMap = mapView.getMap();
//                    }
//                    //修改地图的中心点位置
//                    CameraPosition cp = aMap.getCameraPosition();
//                    CameraPosition cpNew = CameraPosition.fromLatLngZoom(new com.amap.api.maps.model.LatLng(Double.valueOf(jindu), Double.valueOf(rtc)), cp.zoom);
//                    CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cpNew);
//                    aMap.moveCamera(cu);
//                    aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
//                    oldll = new com.amap.api.maps.model.LatLng(Double.valueOf(rtc), Double.valueOf(jindu));
//                    ll = oldll;
//                    hua(1, Double.valueOf(jindu), Double.valueOf(rtc));
//                }
//                if (i == Mapdata.length() - 1) {
//                    hua(0, Double.valueOf(jindu), Double.valueOf(rtc));
//                }
//                ll = new com.amap.api.maps.model.LatLng(Double.valueOf(jindu), Double.valueOf(rtc));
//                if (oldll.latitude != 90.0 && null != ll) {
//                    aMap.addPolyline((new PolylineOptions()).add(oldll, ll).color(Color.parseColor("#00aeef")).width(20).geodesic(true));
//                }
//                oldll = ll;
//            }
//
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//
//    }
//
//    //画高德地图的起始点
//    public void hua(int id, double jin, double wei) {
//        com.amap.api.maps.model.MarkerOptions mo = new com.amap.api.maps.model.MarkerOptions();
//        mo.position(new com.amap.api.maps.model.LatLng(jin, wei));
//        mo.draggable(true);
//        // mo.icon(BitmapDescriptorFactory.defaultMarker());
//        if (id == 1) {
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_start_pistion);
//            mo.icon(com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bm));
//            aMap.addMarker(mo);
//        } else {
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_pistion);
//            mo.icon(com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(bm));
//            aMap.addMarker(mo);
//        }
//    }
//
//
//    private com.google.android.gms.maps.MapView mapViews;
//    private GoogleMap mMap;
//    LatLng ll1, oldll1;
//
//    void starGooGleMap() {
//
//        mapViews = findViewById(R.id.test_qixinggoogle_map);
//        mapViews.setVisibility(View.VISIBLE);
//        if (mMap == null) mapViews.getMapAsync(this);
//    }
//
//
//    //画高德地图的起始点
//    public void googleMaeker(int id, double jin, double wei) {
//        MarkerOptions mo = new MarkerOptions();
//        mo.position(new LatLng(jin, wei));
//        mo.draggable(true);
//        // mo.icon(BitmapDescriptorFactory.defaultMarker());
//        if (id == 1) {
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_start_pistion);
//            mo.icon(BitmapDescriptorFactory.fromBitmap(bm));
//            mMap.addMarker(mo);
//        } else {
//            bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_pistion);
//            mo.icon(BitmapDescriptorFactory.fromBitmap(bm));
//            mMap.addMarker(mo);
//        }
//    }
//
//
//    @OnClick({R.id.qixingshu_waipao_bustar, R.id.huwaiq_ixingbubao_fengxiang})
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.qixingshu_waipao_bustar:
//                finish();
//                break;
//            case R.id.huwaiq_ixingbubao_fengxiang:
//                Boolean zhon = getResources().getConfiguration().locale.getCountry().equals("CN");
//                if (zhon == true) {
//                    aMap.getMapScreenShot(new Gaode()); /**高德地图截屏*/
//                } else {
//                    GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
//                        Bitmap bitmap;
//
//                        public void onSnapshotReady(Bitmap snapshot) {
//                            bitmap = snapshot;
//                            try {
//                                FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/DCIM/" + "SDSDSdd.png");
//                                boolean ifSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                                try {
//                                    out.flush();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                try {
//                                    out.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                if (ifSuccess) {
//                                    fugaiwu.setVisibility(View.VISIBLE);
//                                    fugaiwu.setImageBitmap(bitmap);
//                                    Date timedf = new Date();
//                                    SimpleDateFormat formatdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                    String xXXXdf = formatdf.format(timedf);
//                                    String filePath = Environment.getExternalStorageDirectory() + "/DCIM/" + xXXXdf + ".png";
//                                    ScreenShot.shoot(BzlMaps_HistoryActivity.this, new File(filePath));
//                                    Common.showShare(BzlMaps_HistoryActivity.this, null, false, filePath);
//                                } else {
//
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    };
//                    mMap.snapshot(callback);
//                }
//                break;
//
//        }
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        try {
//            JSONObject JSONO = new JSONObject(value2);
//            Starttime.setText(JSONO.optString("day").toString());
//            if ("良".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.good));
//            } else if ("轻度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.mild_pollution));
//            } else if ("中度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.moderate_pollution));
//            } else if ("重度污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.heavy_pollution));
//            } else if ("严重污染".equals(JSONO.optString("description").toString().trim())) {
//                Kongqi.setText(getResources().getString(R.string.serious_pollution));
//            }
//            Wendu.setText(JSONO.optString("temp").toString());
//            BIAOTI.setText(JSONO.optString("qixing").toString());
//            Duration.setText(JSONO.optString("chixutime").toString());
//            Fullkilometer.setText(JSONO.optString("zonggongli").toString());
//            Pace.setText(getResources().getString(R.string.paces) + JSONO.optString("speed").toString());
//            Consume.setText(getResources().getString(R.string.XIAOHAO) + JSONO.optString("kclal").toString());
//            System.out.print("inmage" + JSONO.optString("image").toString());
//            BitmapUtils bitmapUtils = new BitmapUtils(BzlMaps_HistoryActivity.this);
//            BitmapDisplayConfig config = new BitmapDisplayConfig();
//            // 设置图片的分辨率
//            BitmapSize size = new BitmapSize(500, 500);
//            config.setBitmapMaxSize(size);
//            bitmapUtils.display(TianqiImage, JSONO.optString("image").toString());
//            //解析地图轨迹
//            JSONArray Mapdata = new JSONArray(value);
//            for (int i = 0; i < Mapdata.length(); i++) {
//                JSONObject jo = (JSONObject) Mapdata.get(i);
//                String rtc = jo.optString("lon").toString();//纬度
//                String jindu = jo.optString("lat").toString();//经度
//                //MyLogUtil.i("response"+rtc+"rrrrrrrr"+jindu);
//                if (i == 0) {
//                    com.google.android.gms.maps.model.CameraPosition cameraPosition
//                            = mMap.getCameraPosition();
//
//                    com.google.android.gms.maps.model.CameraPosition newCameraPosition
//                            = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
//                            new LatLng(Double.valueOf(jindu), Double.valueOf(rtc)), cameraPosition.zoom);
//                    com.google.android.gms.maps.CameraUpdate cameraUpdate = com.google.android.gms.maps.CameraUpdateFactory
//                            .newCameraPosition(newCameraPosition);
//                    mMap.moveCamera(cameraUpdate);
//                    mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.zoomTo(12));
//                    oldll1 = new LatLng(Double.valueOf(rtc), Double.valueOf(jindu));
//                    ll1 = oldll1;
//                    googleMaeker(1, Double.valueOf(jindu), Double.valueOf(rtc));
//                }
//                if (i == Mapdata.length() - 1) {
//                    googleMaeker(0, Double.valueOf(jindu), Double.valueOf(rtc));
//                }
//                ll1 = new LatLng(Double.valueOf(jindu), Double.valueOf(rtc));
//                if (oldll.latitude != 90.0 && null != ll) {
//                    mMap.addPolyline((new com.google.android.gms.maps.model.PolylineOptions()).add(oldll1, ll1).color(Color.parseColor("#00aeef")).width(10f).geodesic(true));
//                }
//                oldll = ll;
//            }
//
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//    }
//
//
//    /**
//     * 高德地图截屏
//     */
//    public class Gaode implements AMap.OnMapScreenShotListener {
//        @Override
//        public void onMapScreenShot(Bitmap bitmap) {
//            try {
//                // 保存在SD卡根目录下，图片为png格式。
//                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/DCIM/" + "eeed.png");
//                boolean ifSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                try {
//                    fos.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (ifSuccess) {
//                    fugaiwu.setVisibility(View.VISIBLE);
//                    fugaiwu.setImageBitmap(bitmap);
//                    Date timedf = new Date();
//                    SimpleDateFormat formatdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    String xXXXdf = formatdf.format(timedf);
//                    String filePath = Environment.getExternalStorageDirectory() + "/DCIM/" + xXXXdf + ".png";
//                    ScreenShot.shoot(BzlMaps_HistoryActivity.this, new File(filePath));
//                    Common.showShare(BzlMaps_HistoryActivity.this, null, false, filePath);
//                } else {
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onMapScreenShot(Bitmap bitmap, int i) {
//        }
//    }
//}
