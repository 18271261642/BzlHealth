package com.bozlun.health.android.bleutil;


/**
 * Created by thinkpad on 2017/3/19.
 */

public class MyCommandManager {


    /** 设备连接状态 */
    public static boolean deviceConnctState = false;
    /** 是否手动断开连接若意外断开连接重连 true为正常断开，flase为非正常断开 */
    public static boolean deviceDisconnState = false;
    public static String deviceAddress = "";

    public static String DEVICENAME = null;
    public static String ADDRESS = null;
    //是否是ota升级
    public static boolean isOta = false;


}
