package com.bozlun.health.android.bzlmaps.sos;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Administrator on 2018/4/17.
 * 获取用户的地理位置
 */
public class GPSGaoDeUtils {

    private static GPSGaoDeUtils instance;
    private Context mContext;
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;


    private GPSGaoDeUtils(Context context) {
        this.mContext = context;

        mlocationClient = new AMapLocationClient(mContext);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(aMapLocationListener);

        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(3 * 1000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
    }

//
//    //简体转成繁体
//    public String change(String changeText) {
//        try {
//            JChineseConvertor jChineseConvertor = JChineseConvertor
//                    .getInstance();
//            changeText = jChineseConvertor.s2t(changeText);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return changeText;
//    }
//
//
//    //繁体转成简体
//    public String change1(String changeText) {
//        try {
//            JChineseConvertor jChineseConvertor = JChineseConvertor
//                    .getInstance();
//            changeText = jChineseConvertor.t2s(changeText);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return changeText;
//    }

    AMapLocationListener aMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    Commont.GPSCOUNT++;
                    if (Commont.GPSCOUNT <= 2) {
                        //定位成功回调信息，设置相关消息
                        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.getLatitude();//获取纬度
                        amapLocation.getLongitude();//获取经度
                        amapLocation.getAccuracy();//获取精度信息
                        amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                        amapLocation.getCountry();//国家信息
                        amapLocation.getProvince();//省信息
                        amapLocation.getCity();//城市信息
                        amapLocation.getDistrict();//城区信息
                        amapLocation.getStreet();//街道信息
                        amapLocation.getStreetNum();//街道门牌号信息
                        amapLocation.getCityCode();//城市编码
                        amapLocation.getAdCode();//地区编码
                        amapLocation.getAoiName();//获取当前定位点的AOI信息
                        amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
                        amapLocation.getFloor();//获取当前室内定位的楼层
                        amapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
                        //获取定位时间
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(amapLocation.getTime());
                        df.format(date);

                        double latitude = amapLocation.getLatitude();
                        double longitude = amapLocation.getLongitude();
                        String stringpersonOne = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personOne", "");
                        String stringpersonTwo = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personTwo", "");
                        String stringpersonThree = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personThree", "");
                        String stringpersonContent = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personContent", "");
                        if (WatchUtils.isEmpty(stringpersonContent))
                            stringpersonContent = "大侠，救命啊！ ";

                        String dizhi = amapLocation.getCountry()//国
                                + " " + amapLocation.getProvince()//省
                                + " " + amapLocation.getCity()//城
                                + " " + amapLocation.getDistrict()//区
                                + " " + amapLocation.getStreet()//街
                                + " " + amapLocation.getStreetNum();//街道门牌号信息
//                        Boolean zhon = mContext.getResources().getConfiguration().locale.getCountry().equals("CN");
////                        Log.d("----------AA", zhon + "");
////                        if (zhon) {
////                            dizhi = mContext.getResources().getString(R.string.string_emergency_gps) + ":" + amapLocation.getCountry()
////                                    + "-" + amapLocation.getProvince()
////                                    + "-" + amapLocation.getCity()
////                                    + "-" + amapLocation.getDistrict()
////                                    + "-" + amapLocation.getStreet() + "--Latitude:" + String.valueOf(latitude) + ",Longitude:" + String.valueOf(longitude);
////
////                        } else {
////                            dizhi = mContext.getResources().getString(R.string.string_emergency_gps) + ":" + change(amapLocation.getCountry())
////                                    + "-" + change(amapLocation.getProvince())
////                                    + "-" + change(amapLocation.getCity())
////                                    + "-" + change(amapLocation.getDistrict())
////                                    + "-" + change(amapLocation.getStreet()) + "--lat:" + String.valueOf(latitude) + ",lon:" + String.valueOf(longitude);
////
////                        }
//                        if (zhon) {
//                            dizhi = amapLocation.getCountry()
//                                    + " " + amapLocation.getProvince()
//                                    + " " + amapLocation.getCity()
//                                    + " " + amapLocation.getDistrict()
//                                    + " " + amapLocation.getStreet();
//
//                        } else {
//                            dizhi = change(amapLocation.getCountry())
//                                    + " " + change(amapLocation.getProvince())
//                                    + " " + change(amapLocation.getCity())
//                                    + " " + change(amapLocation.getDistrict())
//                                    + " " + change(amapLocation.getStreet());
//                        }

                        Log.e("----------AA", "定位成功--地址=" + dizhi);
//                        if (Commont.isGPSed){
                        if (WatchUtils.isEmpty(dizhi)) dizhi = amapLocation.getAddress();
                        Intent intent = new Intent();
                        //com.example.bozhilun.android.bzlmaps.sos.SENDSMS
                        intent.setAction("com.example.bozhilun.android.sos.SENDSMS");
                        intent.putExtra("msm", stringpersonContent.trim() + "  " + latitude + "," + longitude);
                        intent.putExtra("gps", dizhi.trim());
                        Log.e("----------AA", "拿到位置-- - 位置广播已经发送  去发短信  ");
                        mContext.sendBroadcast(intent);
//                        }

//                        if (!WatchUtils.isEmpty(stringpersonOne)) {
//                            Log.d("----------AA", "发送给手机一");
//                            SmsManager.getDefault().sendTextMessage(stringpersonOne, null, stringpersonContent.trim() + "\n" + dizhi.trim(), null, null);
//                            //PhoneUtile.sendSMS(stringpersonOne, messge);
//                        }
//
//                        if (!WatchUtils.isEmpty(stringpersonTwo)) {
//                            Log.d("----------AA", "发送给手机二");
////                            PhoneUtile.sendSMS(stringpersonTwo, messge);
//                            SmsManager.getDefault().sendTextMessage(stringpersonTwo, null, stringpersonContent.trim() + "\n" + dizhi.trim(), null, null);
//                        }
//
//                        if (!WatchUtils.isEmpty(stringpersonThree)) {
//                            Log.d("----------AA", "发送给手机三");
////                            PhoneUtile.sendSMS(stringpersonThree, messge);
//                            SmsManager.getDefault().sendTextMessage(stringpersonThree, null, stringpersonContent.trim() + "\n" + dizhi.trim(), null, null);
//                        }
                    } else {
                        //Commont.isGPSed = false;
                        stopGPS();
                        destroyGPS();
                    }
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
//                    Log.e("AmapError", "location Error, ErrCode:"
//                            + amapLocation.getErrorCode() + ", errInfo:"
//                            + amapLocation.getErrorInfo());
                }
            }
        }
    };


    /**
     * 停止定位
     */
    void stopGPS() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
            mlocationClient = null;
        }
        if (instance != null) instance = null;

    }

    /**
     * 销毁定位
     */
    void destroyGPS() {
        if (mlocationClient != null) {
            mlocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
            mlocationClient = null;
        }
        if (aMapLocationListener != null) aMapLocationListener = null;
    }

    public static GPSGaoDeUtils getInstance(Context context) {
//        if (instance == null) {
//            instance = new GPSGaoDeUtils(context);
//        }
        return new GPSGaoDeUtils(context);
    }

}