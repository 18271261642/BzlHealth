package com.bozlun.health.android;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Process;

import com.afa.tourism.greendao.gen.DaoMaster;
import com.afa.tourism.greendao.gen.DaoSession;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.bzlmaps.PhoneSosOrDisPhone;
import com.bozlun.health.android.db.DBManager;
import com.bozlun.health.android.bi8i.b18iutils.B18iUtils;
import com.bozlun.health.android.activity.wylactivity.wyl_util.service.AlertService;
import com.bozlun.health.android.b30.service.B30ConnStateService;
import com.bozlun.health.android.b30.service.B30DataServer;
import com.bozlun.health.android.exection.CrashHandler;
import com.bozlun.health.android.siswatch.bleus.H8BleManagerInstance;
import com.bozlun.health.android.siswatch.bleus.WatchBluetoothService;
import com.mob.MobSDK;
import com.suchengkeji.android.w30sblelibrary.W30SBLEManage;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.umeng.commonsdk.UMConfigure;
import com.veepoo.protocol.VPOperateManager;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by thinkpad on 2016/7/20.
 */

public class MyApp extends LitePalApplication {
    //用于退出activity
    private List<AppCompatActivity> activities;

    /**
     * 手环MAC地址
     */
    private String macAddress;
    /**
     * 是否正在上传数据
     */
    private boolean uploadDate;
    private static MyApp instance = null;
    public static final String RefreshBroad = "com.example.bozhilun.android.RefreshBroad";

    private static PhoneSosOrDisPhone phoneSosOrDisPhone;


    public static PhoneSosOrDisPhone getPhoneSosOrDisPhone() {
        return phoneSosOrDisPhone == null ? new PhoneSosOrDisPhone() : phoneSosOrDisPhone;
    }

    public static MyApp getInstance() {
        return instance;
    }

    //异常收集
    CrashHandler crashHandler;


    //判断是否是B31带血压功能
    private boolean isB31HasBP = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppisOne = true;
        AppisOneStar = true;
        activities = new ArrayList<>();


        //App初始启动是断开状态
        MyCommandManager.DEVICENAME = null;
        LitePal.initialize(instance);
        /**
         * 第三方登陆分享+注册短信
         */
        MobSDK.init(this);
        initSDK();
        phoneSosOrDisPhone = new PhoneSosOrDisPhone();
    }


    /**
     * 各类型设备服务启动以及 SDK等的初始化
     */
    private void initSDK() {
        /**
         * 减少启动时间---优化启动项
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    Thread.sleep(700);

                    // 必须在调用任何统计SDK接口之前调用初始化函数
                    UMConfigure.init(instance, UMConfigure.DEVICE_TYPE_PHONE, null);

                    /**
                     * 全局异常处理
                     */
//                    初始化异常收集
                    crashHandler = CrashHandler.getInstance();
                    crashHandler.init(getContext());

                    /**
                     * 启动w30服务
                     */
                    //startW30SSerever();
                    /**
                     * 启动B30的服务
                     */
                    startB30Server();
                    /**
                     * 前台服务
                     */
                    bindAlertServices();    //绑定通知的服务
                    /**
                     * 数据库
                     * 单利获取 DBManager
                     */
                    setDatabase();
                    dbManager = DBManager.getInstance(getContext());
                    /**
                     * H8配对工具
                     */
                    // HidUtil.getInstance(getContext());

                    /**
                     * H9手表初始化
                     */
                    // BluetoothApplicationContext.getInstance().init(getContext());

                    //startH9Server();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public boolean isB31HasBP() {
        return isB31HasBP;
    }

    public void setB31HasBP(boolean b31HasBP) {
        isB31HasBP = b31HasBP;
    }

    public boolean isOne = true;
    public boolean AppisOne = false;
    public boolean AppisOneStar = false;


    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        super.onTrimMemory(level);
    }


    /***********************************  前台服务   **********************************************/
    /**
     * 前台服务
     */
    private void bindAlertServices() {
        Intent ints = new Intent(instance, AlertService.class);
        bindService(ints, alertConn, BIND_AUTO_CREATE);
    }

    //前台服务绑定
    private ServiceConnection alertConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    /***********************************  数据库   **********************************************/
    /**
     * ------------- 1
     * 数据库
     */

    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private void setDatabase() {
        // 通过DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为greenDAO 已经帮你做了。
        // 注意：默认的DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();

    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * -----  2
     * greenDao
     */
    private DBManager dbManager;

    public DBManager getDBManager() {
        dbManager = DBManager.getInstance(instance);
        return dbManager;
    }


    /***********************************  W30   **********************************************/
    /**
     * W30
     */
    public void startW30SSerever() {
        try {
            //启动W30S手表服务
            mW30SBLEManage = W30SBLEManage.getInstance(getContext());
            //上传数据时间间隔
            SharedPreferencesUtils.setParam(getContext(), "upSleepTime", "2018-12-25 12:20");
            SharedPreferencesUtils.setParam(getContext(), "upStepTime", "2018-12-25 12:20");//上传步数时间间隔
            SharedPreferencesUtils.setParam(getContext(), "upSportTime", "2017-11-02 15:00:00");
            SharedPreferencesUtils.setParam(getContext(), "upSleepTime", "2013-11-02 15:00:00");
            SharedPreferencesUtils.setParam(getContext(), "upHeartTimetwo", "2017-11-02 15:00:00");
            SharedPreferencesUtils.setParam(getContext(), "upHeartTimeone", B18iUtils.getSystemDataStart());
            SharedPreferencesUtils.saveObject(getContext(), "type", "0");
        } catch (Exception e) {
            e.getMessage();
        }
    }


    W30SBLEManage mW30SBLEManage;

    public W30SBLEManage getmW30SBLEManage() {
        if (mW30SBLEManage == null) {
            return W30SBLEManage.getInstance(getContext());
        } else {
            return mW30SBLEManage;
        }
    }

    public void setmW30SBLEManage(W30SBLEManage mW30SBLEManage) {
        this.mW30SBLEManage = mW30SBLEManage;
    }


    //解除W30
    public void unBindW30() {
        try {
            if (mW30SBLEManage != null && mW30SBLEManage.getmW30SBLEServices() != null) {
                mW30SBLEManage.unbindW30Services(getContext());
            }
        } catch (Error e) {
        }
    }


    /***********************************  B30   **********************************************/
    /**
     * 启动B30的服务
     */
    //B30手环
    private VPOperateManager vpOperateManager;

    //B30手环的链接服务
    private B30ConnStateService b30ConnStateService;


    public B30DataServer getB30DataServer() {
        return B30DataServer.getB30DataServer();
    }

    //B30的服务
    public B30ConnStateService getB30ConnStateService() {
        if (b30ConnStateService == null) {
            startB30Server();
        }
        return b30ConnStateService;
    }


    public VPOperateManager getVpOperateManager() {

        if (vpOperateManager == null) {
            vpOperateManager = VPOperateManager.getMangerInstance(instance);

        }
        return vpOperateManager;
    }

    //启动B30的服务
    public void startB30Server() {
        Intent ints = new Intent(instance, B30ConnStateService.class);
        instance.bindService(ints, b30ServerConn, BIND_AUTO_CREATE);
    }

    //解绑b30服务
    public void unBindB30() {
        try {
            if (b30ServerConn != null) {
                getContext().unbindService(b30ServerConn);
                b30ServerConn = null;
            }
        } catch (Error error) {
        }

    }

    //B30的服务绑定
    private ServiceConnection b30ServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                b30ConnStateService = ((B30ConnStateService.B30LoadBuilder) service).getB30Service();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            b30ConnStateService = null;
        }
    };


    /***********************************  H8   **********************************************/
    //H8
    private H8BleManagerInstance h8BleManagerInstance;


    public H8BleManagerInstance h8BleManagerInstance() {
        if (h8BleManagerInstance == null) {
            h8BleManagerInstance = h8BleManagerInstance.getH8BleManagerInstance();
        }
        return h8BleManagerInstance;
    }

    /**
     * H8
     */
    //sis watch service
    private WatchBluetoothService watchBluetoothService;

    public WatchBluetoothService getWatchBluetoothService() {
        if (watchBluetoothService == null) {
            initWatchBlueTooth();
        }
        return watchBluetoothService;
    }

    //开启手表的bluetoothservie
    private void initWatchBlueTooth() {
        Intent gattServiceIntent = new Intent(instance, WatchBluetoothService.class);
        instance.bindService(gattServiceIntent, mWatchServiceConnection, BIND_AUTO_CREATE);
    }

    //解除H8的服务
    public void unBindWatchBlueTooth() {
        if (watchBluetoothService != null) {
            watchBluetoothService.unbindService(mWatchServiceConnection);
        }
    }


    // 管理服务的生命周期
    private ServiceConnection mWatchServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            watchBluetoothService = ((WatchBluetoothService.LocalBinder) service).getService();
            if (!watchBluetoothService.initialize()) {
                // Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            // watchBluetoothService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            watchBluetoothService = null;
        }
    };


    RequestQueue requestQueue;

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getContext());
        }
        return requestQueue;
    }

    /**
     * 添加Activity
     */
    public void addActivity(AppCompatActivity activity) {
        // 判断当前集合中不存在该Activity
        if (!activities.contains(activity)) {
            activities.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeALLActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (AppCompatActivity activity : activities) {
            //unregisterReceiver(refreshBroadcastReceiver);
            activity.finish();
        }
    }


    /**
     * 全局手环MAC地址
     */
    public String getMacAddress() {
        if (macAddress == null)
            macAddress = (String) SharedPreferencesUtils.readObject(instance, Commont.BLEMAC);
        return macAddress;
    }

    /**
     * 全局手环MAC地址
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * 是否正在上传数据
     */
    public boolean isUploadDate() {
        return uploadDate;
    }

    /**
     * 是否正在上传数据
     */
    public void setUploadDate(boolean uploadDate) {
        this.uploadDate = uploadDate;
    }


    /*********H9***********/
    //启动H9的服务
    public void startH9Server() {

        SharedPreferencesUtils.setParam(MyApp.getInstance(), "GET_DEVICES_SYS", "2018-12-25 12:20");//获取设备数据  每3分钟后可以获取
        SharedPreferencesUtils.setParam(MyApp.getInstance(), "GET_TIME", "2018-12-25 12:20");//第一次同步电池电量时间
        SharedPreferencesUtils.saveObject(MyApp.getInstance(), "H9_BATTERY", 100);//电池电量值
//        Intent ints = new Intent(instance, BluetoothService.class);
//        instance.bindService(ints, h9ServerConn, BIND_AUTO_CREATE);
    }


    //B30的服务绑定
    private ServiceConnection h9ServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
