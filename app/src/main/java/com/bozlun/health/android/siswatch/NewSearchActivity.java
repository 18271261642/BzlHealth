package com.bozlun.health.android.siswatch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.NewLoginActivity;
import com.bozlun.health.android.b30.B30HomeActivity;
import com.bozlun.health.android.b30.service.VerB30PwdListener;
import com.bozlun.health.android.b31.B31HomeActivity;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.h9.H9HomeActivity;
import com.bozlun.health.android.siswatch.adapter.CustomBlueAdapter;
import com.bozlun.health.android.siswatch.bean.CustomBlueDevice;
import com.bozlun.health.android.siswatch.bleus.H8BleManagerInstance;
import com.bozlun.health.android.siswatch.bleus.H8ConnstateListener;
import com.bozlun.health.android.siswatch.h8.H8HomeActivity;
import com.bozlun.health.android.siswatch.utils.BlueAdapterUtils;
import com.bozlun.health.android.siswatch.utils.HidUtil;
import com.bozlun.health.android.siswatch.utils.WatchConstants;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.sdk.bluetooth.bean.BluetoothScanDevice;
import com.sdk.bluetooth.interfaces.BluetoothManagerScanListener;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.view.CusInputDialogView;
import com.bozlun.health.android.w30s.W30SHomeActivity;
import com.sdk.bluetooth.config.BluetoothConfig;
import com.sdk.bluetooth.interfaces.BluetoothManagerDeviceConnectListener;
import com.sdk.bluetooth.manage.AppsBluetoothManager;
import com.suchengkeji.android.w30sblelibrary.W30SBLEManage;
import com.suchengkeji.android.w30sblelibrary.W30SBLEServices;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bozlun.health.android.siswatch.utils.WatchConstants.isScanConn;
import static com.bozlun.health.android.siswatch.utils.WatchUtils.B31_NAME;

/**
 * Created by Administrator on 2017/10/31.
 */

/**
 * 新的搜索页面
 */
public class NewSearchActivity extends GetUserInfoActivity implements CustomBlueAdapter.OnSearchOnBindClickListener, SwipeRefreshLayout.OnRefreshListener, BluetoothManagerScanListener, BluetoothManagerDeviceConnectListener {

    private static final String TAG = "NewSearchActivity";
    private static final int BLUE_VISIABLE_TIME_CODE = 10 * 1000;
    private static final int REQUEST_TURNON_BLUE_CODE = 1001;   //打开蓝牙请求码
    private static final int STOP_SCANNER_DEVICE_CODE = 1002;   //停止扫描
    private static final int GET_OPENBLUE_SUCCESS_CODE = 120;   //请求打开蓝牙 ，用户确认打开后返回
    public static final int H9_REQUEST_CONNECT_CODE = 1112;
    public static final int H8_CONNECT_SUCCESS_CODE = 1113; //H8手表连接成功
    public static final int H8_CONNECT_FAILED_CDOEE = 1114; //H8手表连接失败
    private static final int H8_BLE_BANDSTATE_CODE = 12;    //H8手表蓝牙配对状态
    private static final int H8_PAIR_REQUEST_CODE = 1115;   //H8手表配对返回
    private static final String B15P_BLENAME = "B15P";  //B15P
    private static final String B18I_BLENAME = "B18I";  //B18I
    private static final String H9_BLENAME = "H9"; //H9手表名字标识  保存时后面+X
    private String H9CONNECT_STATE_ACTION = "com.example.bozhilun.android.h9.connstate";

    //RecycleView
    @BindView(R.id.search_recycler)
    RecyclerView searchRecycler;
    //Swiper
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    //跑马灯textView
    @BindView(R.id.search_alertTv)
    TextView searchAlertTv;
    @BindView(R.id.newSearchTitleLeft)
    FrameLayout newSearchTitleLeft;
    @BindView(R.id.newSearchTitleTv)
    TextView newSearchTitleTv;
    @BindView(R.id.newSearchRightImg1)
    ImageView newSearchRightImg1;


    private List<CustomBlueDevice> customDeviceList;  //数据源集合
    private CustomBlueAdapter customBlueAdapter;    //适配器
    private BluetoothAdapter bluetoothAdapter;  //蓝牙适配器
    private List<String> repeatList;

    CustomBlueDevice customBlueDevice = null;

    //B30系列设备验证密码提示框
    CusInputDialogView cusInputDialogView;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP_SCANNER_DEVICE_CODE:  //停止扫描
                    swipeRefresh.setRefreshing(false);
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    break;
                case H9_REQUEST_CONNECT_CODE:  //H 9手表连接
                    closeLoadingDialog();
//                    BluetoothDevice h9Blued = (BluetoothDevice) msg.obj;
//                    //连接成功后移除连接监听
//                    //AppsBluetoothManager.getInstance(NewSearchActivity.this).clearBluetoothManagerDeviceConnectListeners();
//                    SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "H9");
//                    SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLEMAC, h9Blued.getAddress());
                    startActivity(new Intent(NewSearchActivity.this, H9HomeActivity.class));
                    finish();
                    break;
                case 777:   //连接失败后更新目录
                    refresh();
                    break;

            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "-------onCreate----");
        setContentView(R.layout.activity_search_device);
        ButterKnife.bind(this);

        //启动w30服务
       // MyApp.getInstance().startW30SSerever();
        //启动B30的服务
        MyApp.getInstance().startB30Server();
        //启动H9的服务
//        MyApp.getInstance().startH9Server();

     //   AppsBluetoothManager.getInstance(this).redoRegiresterService();
        initViews();
        h9DataOper();
        initData();



    }

    private void h9DataOper() {
        String mylanya = (String) SharedPreferencesUtils.readObject(NewSearchActivity.this, Commont.BLENAME);
        String mylanmac = (String) SharedPreferencesUtils.readObject(NewSearchActivity.this, Commont.BLEMAC);
        String defaultMac = BluetoothConfig.getDefaultMac(NewSearchActivity.this);
        if (!TextUtils.isEmpty(mylanya) || !TextUtils.isEmpty(mylanmac) || !TextUtils.isEmpty(defaultMac)) {
            AppsBluetoothManager.getInstance(MyApp.getContext()).doUnbindDevice(mylanmac);
            AppsBluetoothManager.getInstance(NewSearchActivity.this).clearBluetoothManagerDeviceConnectListeners();
            SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "");
            SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLEMAC, "");
            MyApp.getInstance().setMacAddress(null);// 清空全局
            BluetoothConfig.setDefaultMac(NewSearchActivity.this, "");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            //添加扫描监听
            AppsBluetoothManager.getInstance(this).addBluetoothManagerScanListeners(this);
            //添加链接监听
            AppsBluetoothManager.getInstance(this).addBluetoothManagerDeviceConnectListener(this);
            //AppsBluetoothManager.getInstance(this).startDiscovery();
            boolean serviceRunning = W30SBLEServices.isServiceRunning(this, "com.suchengkeji.android.w30sblelibrary.W30SBLEServices");
            Log.e("--------调试-", "-NewSearch-onStart--" + serviceRunning);
            if (!serviceRunning) {
                MyApp.getInstance().getmW30SBLEManage().openW30SBLEServices();
            }

            if (customDeviceList != null)
                customDeviceList.clear();
            if (customBlueAdapter != null)
                customBlueAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        //HidUtil.getInstance(MyApp.getContext());
        //设置H9手表的连接监听回调
        //AppsBluetoothManager.getInstance(MyApp.getContext()).addBluetoothManagerDeviceConnectListener(bluetoothManagerDeviceConnectListener);

    }

    private void initData() {
        //Log.d("-ccc-SDK中的--mac---", BluetoothConfig.getDefaultMac(MyApp.getContext()));
        String blmac = (String) SharedPreferencesUtils.readObject(NewSearchActivity.this, Commont.BLEMAC);
        if (!WatchUtils.isEmpty(blmac) && blmac.equals("H9")) {
            AppsBluetoothManager.getInstance(MyApp.getContext()).doUnbindDevice(blmac);
        }

        repeatList = new ArrayList<>();
        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bm.getAdapter();

        //判断是否有位置权限
        if (AndPermission.hasPermissions(NewSearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            operScan();
        } else {
            verticalPermission();
        }


    }

    private void verticalPermission() {
        AndPermission.with(NewSearchActivity.this)
                .runtime()
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .rationale(rational)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                ToastUtil.showToast(NewSearchActivity.this, "已拒绝权限,可能无法搜索到蓝牙设备！");
            }
        }).start();


    }


    //当权限被拒绝时提醒用户再次授权
    private Rationale rational = new Rationale() {
        @Override
        public void showRationale(Context context, Object data, RequestExecutor executor) {
            executor.execute();
            AndPermission.with(NewSearchActivity.this)
                    .runtime()
                    .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {

                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //AndPermission.with(NewSearchActivity.this).runtime().setting().
                        }
                    })
                    .start();
        }
    };


    private void operScan() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {  //未打开蓝牙
                BlueAdapterUtils.getBlueAdapterUtils(NewSearchActivity.this).turnOnBlue(NewSearchActivity.this,
                        BLUE_VISIABLE_TIME_CODE, REQUEST_TURNON_BLUE_CODE);
            } else {
                scanBlueDevice(true);   //扫描设备
            }
        } else {
            //不支持蓝牙
            ToastUtil.showToast(NewSearchActivity.this, getResources().getString(R.string.bluetooth_not_supported));
            return;
        }
    }

    //扫描和停止扫描设备
    private void scanBlueDevice(boolean b) {
        if (!AndPermission.hasPermissions(NewSearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
            verticalPermission();
        if (b) {

            //扫描设备，默认扫描时间为10秒
            swipeRefresh.setRefreshing(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 2000);
            getPhonePairDevice();   //获取手机配对的设备
            bluetoothAdapter.startLeScan(leScanCallback);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = STOP_SCANNER_DEVICE_CODE;
                    handler.sendMessage(message);
                }
            }, BLUE_VISIABLE_TIME_CODE);
        } else {
            if (swipeRefresh != null)
                swipeRefresh.setRefreshing(false);
            if (bluetoothAdapter != null)
                bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    //获取配对的蓝牙设备
    private void getPhonePairDevice() {
        //获取已配对设备
        Object[] lstDevice = bluetoothAdapter.getBondedDevices().toArray();
        for (int i = 0; i < lstDevice.length; i++) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) lstDevice[i];
            if (bluetoothDevice != null) {
                if (bluetoothDevice.getName() != null && bluetoothDevice.getName().length() > 6 && bluetoothDevice.getName().substring(0, 2).equals("H8")) {
                    repeatList.add(bluetoothDevice.getAddress());
                    String pairDevice = "已配对|" + bluetoothDevice.getName() + "-" + bluetoothDevice.getAddress();
//                    Log.d(TAG, "----已配对设备--" + pairDevice + "----" + bluetoothDevice.getUuids());
                    customDeviceList.add(new CustomBlueDevice(bluetoothDevice, "", 0));
                    customBlueAdapter.notifyDataSetChanged();
                }
            }

        }

    }

    /**
     * 蓝牙扫描回调
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            String bleName = bluetoothDevice.getName();
            String bleMac = bluetoothDevice.getAddress(); //bozlun
            if (!WatchUtils.isEmpty(bleName) && bleName.length() >= 3) {
                if (WatchUtils.verBleNameForSearch(bleName)) {
                    if (!repeatList.contains(bleMac)) {
                        if (customDeviceList.size() <= 30) {
                            repeatList.add(bleMac);
                            customDeviceList.add(new CustomBlueDevice(bluetoothDevice, Math.abs(rssi) + "", ((scanRecord[7] + scanRecord[8]))));
                            Comparator comparator = new Comparator<CustomBlueDevice>() {
                                @Override
                                public int compare(CustomBlueDevice o1, CustomBlueDevice o2) {
                                    return o1.getRssi().compareTo(o2.getRssi());
                                }
                            };
                            Collections.sort(customDeviceList, comparator);
                            customBlueAdapter.notifyDataSetChanged();
                        } else {
                            scanBlueDevice(false);
                        }
                    }

                }
            }
        }
    };

    private void initViews() {
        //注册扫描蓝牙设备的广播
        registerReceiver(broadcastReceiver, BlueAdapterUtils.getBlueAdapterUtils(NewSearchActivity.this).scanIntFilter()); //注册广播
        H8BleManagerInstance.getH8BleManagerInstance();
        //跑马灯效果
        searchAlertTv.setSelected(true);
        newSearchTitleTv.setText(getResources().getString(R.string.search_device));
        newSearchRightImg1.setVisibility(View.VISIBLE);
        //设置RecyclerView相关
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchRecycler.setLayoutManager(linearLayoutManager);
        searchRecycler.addItemDecoration(new DividerItemDecoration(NewSearchActivity.this, DividerItemDecoration.VERTICAL));
        customDeviceList = new ArrayList<>();
        customBlueAdapter = new CustomBlueAdapter(customDeviceList, NewSearchActivity.this);
        searchRecycler.setAdapter(customBlueAdapter);
        customBlueAdapter.setOnBindClickListener(this);
        swipeRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TURNON_BLUE_CODE) {    //打开蓝牙返回
            if (resultCode == GET_OPENBLUE_SUCCESS_CODE) {  //打开蓝牙返回
                scanBlueDevice(true);
            } else {  //点击取消 0    //取消后强制打开
                bluetoothAdapter.enable();
                scanBlueDevice(true);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (broadcastReceiver != null)
                unregisterReceiver(broadcastReceiver);
            if (cusInputDialogView != null) {
                cusInputDialogView.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //绑定按钮的点击事件
    @Override
    public void doBindOperator(int position) {
        try {
            if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
                scanBlueDevice(false);
            }
            if (customDeviceList != null) {
                customBlueDevice = customDeviceList.get(position);
            }
            String bleName = customBlueDevice.getBluetoothDevice().getName();
            if (customBlueDevice == null || WatchUtils.isEmpty(bleName))
                return;
            //H8手表
            if (customBlueDevice.getCompanyId() == 160 || bleName.substring(0, 2).equals("H8")) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.prompt)
                        .setMessage(getResources().getString(R.string.setting_pair))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //连接H8手表
                                connectH8Watch(customBlueDevice);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                return;
            }


            //W30手表
            if (bleName.substring(0, 3).equals("W30")) {
                showLoadingDialog("connection...");
                String W30address = customBlueDevice.getBluetoothDevice().getAddress();
                if (!WatchUtils.isEmpty(W30address)) {
                    W30SBLEManage.mW30SBLEServices.connectBle(W30address);
                }
                return;
            }
            //B30，B36，Ringmiihx手表
            if (bleName.equals(WatchUtils.B30_NAME) || bleName.equals(WatchUtils.B36_NAME)
                    || bleName.equals(WatchUtils.RINGMII_NAME) || bleName.equals(B31_NAME)
                    || bleName.equals(WatchUtils.B31S_NAME) || bleName.equals(WatchUtils.S500_NAME)) {
                connectB30(customBlueDevice.getBluetoothDevice().getAddress().trim(), bleName);
                return;
            }

            //H9
            if (bleName.substring(0, 2).equals(H9_BLENAME) || bleName.substring(0, 4).equals("W06X")) {
                showLoadingDialog("connection...");
                String h9Address = customBlueDevice.getBluetoothDevice().getAddress().trim();
                if (!WatchUtils.isEmpty(h9Address)) {
                    AppsBluetoothManager.getInstance(this).connectDevice(h9Address);
                    AppsBluetoothManager.getInstance(this).cancelDiscovery();
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //连接B30手环
    private void connectB30(final String b30Mac, String bleName) {
        showLoadingDialog("connect...");
        MyApp.getInstance().getB30ConnStateService().connB30ConnBle(b30Mac, bleName);
    }


    //连接H8手表
    private void connectH8Watch(final CustomBlueDevice customBlueDevice) {
        if (customBlueDevice != null && customBlueDevice.getBluetoothDevice() != null) {
            //先判断是否绑定,绑定即配对
            if (customBlueDevice.getBluetoothDevice().getBondState() == H8_BLE_BANDSTATE_CODE) {
                //判断是否配对连接
                if (HidUtil.getInstance(MyApp.getContext()).isConnected(customBlueDevice.getBluetoothDevice())) {
                    showLoadingDialog("conn...");
                    //连接
                    if (MyApp.getInstance().h8BleManagerInstance().getH8BleService() != null) {
                        MyApp.getInstance().h8BleManagerInstance().getH8BleService().connectBleByMac(customBlueDevice.bluetoothDevice.getAddress());
                        MyApp.getInstance().h8BleManagerInstance().getH8BleService().setH8ConnstateListener(new H8ConnstateListener() {
                            @Override
                            public void h8ConnSucc() {
                                MyCommandManager.DEVICENAME = "bozlun";
                                if (customBlueDevice != null) {
                                    closeLoadingDialog();
                                    WatchConstants.customBlueDevice = customBlueDevice;
                                    WatchConstants.H8ConnectState = true;
                                    SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "bozlun");
                                    SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLEMAC, customBlueDevice.getBluetoothDevice().getAddress());
                                    startActivity(H8HomeActivity.class);
                                    finish();

                                }
                            }

                            @Override
                            public void h8ConnFailed() {
                                closeLoadingDialog();
                                ToastUtil.showToast(NewSearchActivity.this, getResources().getString(R.string.string_conn_fail));
                                handler.sendEmptyMessage(777);
                            }
                        });

                    }

                } else {  //已配对，但未连接
                    //showH8PairAlert();
                    showLoadingDialog("conn...");
                    HidUtil.getInstance(MyApp.getContext()).connect(customBlueDevice.getBluetoothDevice());
                }
            } else {  //没有配对
                //showH8PairAlert();
                showLoadingDialog(verLanguage());
                HidUtil.getInstance(MyApp.getContext()).pair(customBlueDevice.bluetoothDevice);
            }
        }
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        customDeviceList.clear();
        repeatList.clear();
        customBlueAdapter.notifyDataSetChanged();
        scanBlueDevice(true);
    }

    //广播接收者接收H8手表配对连接的状态
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            closeLoadingDialog();
            Log.e(TAG, "--------action-------" + action);
            if (!WatchUtils.isEmpty(action)) {
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON://13

                            break;
                        case BluetoothAdapter.STATE_ON://13
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF://11
                            closeLoadingDialog();
                            if (customDeviceList != null)
                                customDeviceList.clear();
                            if (customBlueAdapter != null)
                                customBlueAdapter.notifyDataSetChanged();


                            break;
                        case BluetoothAdapter.STATE_OFF://10
                            if (customDeviceList != null) customDeviceList.clear();
                            if (customBlueAdapter != null)
                                customBlueAdapter.notifyDataSetChanged();

                            break;
                    }
                }
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {   //绑定状态的广播，配对
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                    BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (bd == null)
                        return;
                    if (bd.getName() == null)
                        return;
                    Log.e(TAG, "------配对状态返回----" + bondState + "--bd=" + bd.getName() + "-" + bd.getAddress());
                    switch (bondState) {
                        case BluetoothDevice.BOND_BONDED:   //已绑定 12
//                            Log.e(TAG, "-----111-----");
                            if (customBlueDevice != null) {
                                if (bd.getName().equals(customBlueDevice.getBluetoothDevice().getName())) {
//                                    Log.e(TAG, "-----22-----");
                                    showLoadingDialog("connect...");
                                    HidUtil.getInstance(MyApp.getContext()).connect(bd);
                                }
                            }
                            break;
                        case BluetoothDevice.BOND_BONDING:  //绑定中   11
                            if (customBlueDevice != null) {
                                if (bd.getName().equals(customBlueDevice.getBluetoothDevice().getName())) {
//                                    Log.e(TAG, "-----22-----");
                                    showLoadingDialog(verLanguage());
                                }
                            }
                            break;
                        case BluetoothDevice.BOND_NONE: //绑定失败  10
                            if (customBlueDevice != null && customBlueDevice.getBluetoothDevice().getName() != null) {
                                if (bd.getName().equals(customBlueDevice.getBluetoothDevice().getName())) {
                                    closeLoadingDialog();
                                    ToastUtil.showToast(NewSearchActivity.this, getResources().getString(R.string.string_conn_fail));
                                    refresh();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) { //连接状态的改变
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                    int connState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                    BluetoothDevice conBle = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (conBle == null)
                        return;
                    //0-1;0-2 3;0
                    Log.e(TAG, "-----连接状态----" + state + "--" + connState + "----conBle--" + conBle.getName());
                    if (connState == 2 && customBlueDevice != null && conBle.getName().equals(customBlueDevice.getBluetoothDevice().getName())) { //连接成功
                        showLoadingDialog("conn...");
                        if (MyApp.getInstance().h8BleManagerInstance().getH8BleService() != null) {
                            MyApp.getInstance().h8BleManagerInstance().getH8BleService().connectBleByMac(customBlueDevice.bluetoothDevice.getAddress());
                            MyApp.getInstance().h8BleManagerInstance().getH8BleService().setH8ConnstateListener(new H8ConnstateListener() {
                                @Override
                                public void h8ConnSucc() {
                                    MyCommandManager.DEVICENAME = "bozlun";
                                    if (customBlueDevice != null) {
                                        closeLoadingDialog();
                                        WatchConstants.customBlueDevice = customBlueDevice;
                                        WatchConstants.H8ConnectState = true;
                                        SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "bozlun");
                                        SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLEMAC, customBlueDevice.getBluetoothDevice().getAddress());
                                        startActivity(H8HomeActivity.class);
                                        finish();

                                    }
                                }

                                @Override
                                public void h8ConnFailed() {

                                }
                            });
                        }

                    } else if (connState == 0) {   //断开连接成功

                    }
                }

                /**
                 * W30S链接监听
                 */
//                if (W30SBLEServices.ACTION_GATT_CONNECTED.equals(action)) {
//                    closeLoadingDialog();
//                    MyCommandManager.DEVICENAME = "W30";
//                    MyApp.getInstance().getmW30SBLEManage().SendAnddroidLanguage(0x01);
//                    SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "W30");
//                    startActivity(new Intent(NewSearchActivity.this, W30SHomeActivity.class));
//                    finish();
//                }

                //B30手环连接成功
                if (action.equals(WatchUtils.B30_CONNECTED_ACTION)) { //B30连接成功
                    closeLoadingDialog();
                    isScanConn = true;
                    startActivity(B30HomeActivity.class);
                    NewSearchActivity.this.finish();

                }

                //B31连接成功
                if(action.equals(WatchUtils.B31_CONNECTED_ACTION)){
                    closeLoadingDialog();
                    isScanConn = true;
                    startActivity(B31HomeActivity.class);
                    NewSearchActivity.this.finish();
                }

                //B30连接失败
                if (action.equals(WatchUtils.B30_DISCONNECTED_ACTION)) {
                    closeLoadingDialog();
                    ToastUtil.showShort(NewSearchActivity.this, getResources().getString(R.string.string_conn_fail));
                    MyCommandManager.ADDRESS = null;// 断开连接了就设置为null
                }

                //B30提示输入密码
                if (action.equals("com.example.bozhilun.android.siswatch.CHANGEPASS")) {
                    String b30ID = intent.getStringExtra("b30ID");
                    Log.d("--------id-", b30ID);
                    Log.d("------zza---", "弹出输入提示");
                    //showLoadingDialog2(b30ID);
                    showB30InputPwd();
                }

                //H9链接成功失败监听
//                if (action.equals("com.example.bozhilun.android.h9.connstate")) {
//                    String h9Redata = intent.getStringExtra("h9constate");
//                    if (!WatchUtils.isEmpty(h9Redata)) {
//                        if (h9Redata.equals("conn")) {    //已链接
//                            AppsBluetoothManager.getInstance(MyApp.getInstance()).clearBluetoothManagerScanListeners();
//                            AppsBluetoothManager.getInstance(MyApp.getInstance()).clearBluetoothManagerDeviceConnectListeners();
//                            MyCommandManager.DEVICENAME = "H9";
//                            closeLoadingDialog();
//                            startActivity(new Intent(NewSearchActivity.this, H9HomeActivity.class));
//                            finish();
//                        } else {
//                            MyCommandManager.DEVICENAME = null;
//                            BluetoothConfig.setDefaultMac(NewSearchActivity.this, "");
//                            SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLENAME, "");
//                            SharedPreferencesUtils.saveObject(NewSearchActivity.this, Commont.BLEMAC, "");
//                            MyApp.getInstance().setMacAddress(null);// 清空全局
//                            closeLoadingDialog();
//                        }
//                    }
//                }
            }
        }
    };

    //提示输入密码
    private void showB30InputPwd() {
        if (cusInputDialogView == null) {
            cusInputDialogView = new CusInputDialogView(NewSearchActivity.this);
        }
        cusInputDialogView.show();
        cusInputDialogView.setCancelable(false);
        cusInputDialogView.setCusInputDialogListener(new CusInputDialogView.CusInputDialogListener() {
            @Override
            public void cusDialogCancle() {
                cusInputDialogView.dismiss();
                //断开连接
                MyApp.getInstance().getVpOperateManager().disconnectWatch(new IBleWriteResponse() {
                    @Override
                    public void onResponse(int i) {

                    }
                });
                //刷新搜索界面
                handler.sendEmptyMessage(777);
            }

            @Override
            public void cusDialogSureData(String data) {
                MyApp.getInstance().getB30ConnStateService().continuteConn(data, new VerB30PwdListener() {
                    @Override
                    public void verPwdFailed() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ToastUtil.showCusToast(NewSearchActivity.this,getResources().getString(R.string.miamacuo));
//                            }
//                        });
                        ToastUtil.showCusToast(NewSearchActivity.this,getResources().getString(R.string.miamacuo));
                       // ToastUtil.showLong(NewSearchActivity.this, getResources().getString(R.string.miamacuo));
                    }

                    @Override
                    public void verPwdSucc() {
                        cusInputDialogView.dismiss();
                    }
                });
            }
        });

    }


    //返回按键
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            startActivity(B31HomeActivity.class);
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @OnClick({R.id.newSearchTitleLeft, R.id.newSearchRightImg1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.newSearchTitleLeft:   //返回
                startActivity(B31HomeActivity.class);
                finish();
                break;
            case R.id.newSearchRightImg1: //帮助
                if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
                    scanBlueDevice(false);
                }
                startActivity(SearchExplainActivity.class);
                break;
        }
    }


    //判断系统语言
    private String verLanguage() {
        //语言
        String locals = Locale.getDefault().getLanguage();
        if (!WatchUtils.isEmpty(locals)) {
            if (locals.equals("zh")) {
                return "配对中...";
            } else {
                return "Pairing..";
            }
        } else {
            return "Pairing..";
        }

    }

    @Override
    public void onDeviceFound(BluetoothScanDevice bluetoothScanDevice) {

    }

    @Override
    public void onDeviceScanEnd() {

    }

    @Override
    public void onConnected(BluetoothDevice bluetoothDevice) {

    }


    @Override
    public void onConnectFailed() {
        Log.e(TAG, "-------onConnectFailed-----连接失败-");
        MyCommandManager.DEVICENAME = null;
        Intent intent = new Intent();
        intent.setAction(H9CONNECT_STATE_ACTION);
        intent.putExtra("h9constate", "disconn");
        sendBroadcast(intent);  //发送连接失败的广播
    }

    @Override
    public void onEnableToSendComand(BluetoothDevice bluetoothDevice) {
        Log.e(TAG, "-------onEnableToSendComand-----连接绑定成功-");
        MyCommandManager.DEVICENAME = "H9";
        AppsBluetoothManager.getInstance(this).cancelDiscovery();//取消扫描
        BluetoothConfig.setDefaultMac(this, bluetoothDevice.getAddress());
        SharedPreferencesUtils.saveObject(this, Commont.BLENAME, "H9");
        SharedPreferencesUtils.saveObject(this, Commont.BLEMAC, bluetoothDevice.getAddress());

        AppsBluetoothManager.getInstance(this).killCommandRunnable();
        Intent intent = new Intent();
        intent.setAction(H9CONNECT_STATE_ACTION);
        intent.putExtra("h9constate", "conn");
        sendBroadcast(intent);   //发送连接成功的广播
    }

    @Override
    public void onConnectDeviceTimeOut() {
        MyCommandManager.DEVICENAME = null;
        Intent intent = new Intent();
        intent.setAction(H9CONNECT_STATE_ACTION);
        intent.putExtra("h9constate", "disconn");
        sendBroadcast(intent);  //发送连接失败的广播

        AppsBluetoothManager.getInstance(this).disConnectDevice(true);
        AppsBluetoothManager.getInstance(this).clearCommandBlockingDeque();

        handler.sendEmptyMessage(777);
    }

}
