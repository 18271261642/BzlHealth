package com.bozlun.health.android.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.bleus.H8ConnstateListener;
import com.bozlun.health.android.siswatch.bleus.WatchBluetoothService;
import com.bozlun.health.android.siswatch.utils.HidUtil;
import com.bozlun.health.android.siswatch.utils.UpdateManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.URLs;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.umeng.analytics.MobclickAgent;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by thinkpad on 2017/3/9.
 */

public class SetActivity extends BaseActivity {

    private static final int REQUEST_UNPAIR_CODE = 1001;
    private static final String TAG = "SetActivity";
    private BluetoothAdapter bluetoothAdapter;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    private String bleMac;

    private UpdateManager updateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // registerReceiver(broadcastReceiver,new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
        registerReceiver(broadcastReceiver,new IntentFilter(WatchUtils.WACTH_DISCONNECT_BLE_ACTION));
        HidUtil.instance = null;

        checkUpdate();
    }

    @Override
    protected void initViews() {
        tvTitle.setText(R.string.menu_settings);
        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bm.getAdapter();
        bleMac = (String) SharedPreferencesUtils.readObject(SetActivity.this,"mylanmac");
    }

    //检查更新
    private void checkUpdate(){
        String upUrl = URLs.HTTPs + URLs.bozlun_health_url;
        updateManager = new UpdateManager(SetActivity.this, upUrl);
        updateManager.checkForUpdate(true);
    }



    @Override
    protected void onResume() {
        super.onResume();
        HidUtil.getInstance(MyApp.getContext());
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_set;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
       // HidUtil.getInstance(MyApp.getContext()).close();
    }

    @OnClick({R.id.modify_password_relayout, R.id.help_relayout, R.id.abour_relayout, R.id.feedback_relayout, R.id.exit_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.modify_password_relayout: //修改密码
                SharedPreferences share = getSharedPreferences("Login_id", 0);
                String userId = (String) SharedPreferencesUtils.readObject(SetActivity.this, "userId");
                if(!WatchUtils.isEmpty(userId) && userId.equals("9278cc399ab147d0ad3ef164ca156bf0")){
                    ToastUtil.showToast(SetActivity.this,getResources().getString(R.string.noright));
                }else{
                    int isoff = share.getInt("id",0);
                    if(isoff==0){
                        startActivity(new Intent(SetActivity.this, ModifyPasswordActivity.class));
                    }else{
                        // Toast.makeText(this,"您使用第三方登录不可修改密码 !",Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.help_relayout:    //帮助
                startActivity(new Intent(SetActivity.this, HelpActivity.class));
                break;
            case R.id.abour_relayout:   //关于
                startActivity(new Intent(SetActivity.this, AboutActivity.class));
                break;
            case R.id.feedback_relayout:    //意见反馈
                startActivity(new Intent(SetActivity.this, FeedbackActivity.class));
                break;
            case R.id.exit_login:   //退出登录
                showExitdialog();
                break;
        }
    }

    private void showExitdialog() {
        final String bleName = (String) SharedPreferencesUtils.readObject(SetActivity.this,  Commont.BLENAME);
        if (!WatchUtils.isEmpty(bleName)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.exit_login)
                    .content(R.string.confrim_exit)
                    .positiveText(getResources().getString(R.string.confirm))
                    .negativeText(getResources().getString(R.string.cancle))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (bleName.equals("bozlun")) {//H8手表先取消配对
                                new MaterialDialog.Builder(SetActivity.this)
                                        .title(getResources().getString(R.string.prompt))
                                        .content(getResources().getString(R.string.confirm_unbind_strap))
                                        .positiveText(getResources().getString(R.string.confirm))
                                        .negativeText(getResources().getString(R.string.cancle))
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                                // showLoadingDialog("disconn...");
                                                if(MyCommandManager.DEVICENAME != null){
                                                    SharedPreferencesUtils.saveObject(SetActivity.this, Commont.BLEMAC, "");
                                                    MyApp.getInstance().h8BleManagerInstance().disConnH8(new H8ConnstateListener() {
                                                        @Override
                                                        public void h8ConnSucc() {

                                                        }

                                                        @Override
                                                        public void h8ConnFailed() {

                                                        }
                                                    });
                                                }
                                                logoOutApp();

                                            }
                                        }).show();
                            }
                            else {
                                logoOutApp();
                            }
                        }
                    }).show();
        }

    }

    private void logoOutApp(){
        MyCommandManager.deviceDisconnState = true;
        MyApp.getInstance().getDaoSession().getStepBeanDao().deleteAll();//清空数据库
        SharedPreferencesUtils.saveObject(SetActivity.this,  Commont.BLENAME, "");
        SharedPreferencesUtils.saveObject(SetActivity.this, Commont.BLEMAC, "");
        MyApp.getInstance().setMacAddress(null);// 清空全局
        SharedPreferencesUtils.saveObject(SetActivity.this, "userId", "");
        MyApp.getInstance().getWatchBluetoothService().disconnect();  //断开siswatch手表的连接
        SharedPreferencesUtils.setParam(SetActivity.this, "stepsnum", "0");
        MyCommandManager.ADDRESS = null;
        MyCommandManager.DEVICENAME = null;
        SharedPreferencesUtils.setParam(SetActivity.this, SharedPreferencesUtils.CUSTOMER_ID, "");
        SharedPreferencesUtils.setParam(SetActivity.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, "");
        Common.userInfo = null;
        Common.customer_id = null;
        MobclickAgent.onProfileSignOff();
//        startActivity(new Intent(SetActivity.this, LoginActivity.class));
        startActivity(new Intent(SetActivity.this, NewLoginActivity.class));

        //登出
        MobclickAgent.onProfileSignOff();

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("解绑","---------"+requestCode+"----resultCode---"+resultCode+"--resultok="+RESULT_OK);
    }

    /**
     * H8断开连接接收广播，只断开连接，不需要解除配对
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG,"-------action---"+action);
            if(!WatchUtils.isEmpty(action)){
                if(action.equals(WatchUtils.WACTH_DISCONNECT_BLE_ACTION)){
                    String bleState = intent.getStringExtra("bledisconn");
                    if(!WatchUtils.isEmpty(bleState) && bleState.equals("bledisconn")){ //断开连接
                        closeLoadingDialog();
                        MyCommandManager.deviceDisconnState = true;
                        SharedPreferencesUtils.saveObject(MyApp.getContext(), "mylanmac", "");
                        MyApp.getInstance().setMacAddress(null);// 清空全局
                        MyApp.getInstance().getDaoSession().getStepBeanDao().deleteAll();//清空数据库
                        SharedPreferencesUtils.saveObject(SetActivity.this,  Commont.BLENAME, null);
                        SharedPreferencesUtils.saveObject(SetActivity.this, "mylanmac", null);
                        MyApp.getInstance().setMacAddress(null);// 清空全局
                        SharedPreferencesUtils.saveObject(SetActivity.this, "userId", null);
                        MyApp.getInstance().getWatchBluetoothService().disconnect();  //断开siswatch手表的连接
                        SharedPreferencesUtils.setParam(SetActivity.this, "stepsnum", "0");
                        MyCommandManager.ADDRESS = null;
                        MyCommandManager.DEVICENAME = null;
                        Common.userInfo = null;
                        Common.customer_id = null;
                        WatchBluetoothService.isInitiative = true;
                        SharedPreferencesUtils.setParam(SetActivity.this, SharedPreferencesUtils.CUSTOMER_ID, "");
                        SharedPreferencesUtils.setParam(SetActivity.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, "");
                        MobclickAgent.onProfileSignOff();
//                        startActivity(new Intent(SetActivity.this, LoginActivity.class));
                        startActivity(new Intent(SetActivity.this, NewLoginActivity.class));
                        finish();

                    }
                }

            }
        }
    };

    /**
     * //                                                if(WatchConstants.customBlueDevice != null){
     //                                                    Log.e(TAG,"---bozlun断开连接---");
     //                                                    BluetoothDevice bluetoothDevice = WatchConstants.customBlueDevice.getBluetoothDevice();
     //                                                    HidUtil.getInstance(MyApp.getContext()).disConnect(bluetoothDevice);
     //                                                }else{
     //                                                    //根据地址获取连接的设备
     //                                                    BluetoothDevice bd = HidUtil.getInstance(MyApp.getContext()).getConnectedDevice(bleMac);
     //                                                    if(bd != null){
     //                                                        HidUtil.getInstance(MyApp.getContext()).disConnect(bd);
     //                                                    }else{
     //                                                        //g
     //                                                        BluetoothDevice pairD = HidUtil.getInstance(MyApp.getContext()).getPairDeviceByAddress(bluetoothAdapter,bleMac);
     //                                                        if(pairD != null){
     //                                                            HidUtil.getInstance(MyApp.getContext()).unPair(bd);
     //                                                            //已取消配对
     //                                                            MyApp.getWatchBluetoothService().disconnect();//断开蓝牙
     //                                                            MyCommandManager.deviceDisconnState = true;
     //                                                            MyCommandManager.ADDRESS = null;
     //                                                            MyCommandManager.DEVICENAME = null;
     //                                                            SharedPreferencesUtils.saveObject(MyApp.getContext(),"mylanya",null);
     //                                                            SharedPreferencesUtils.saveObject(MyApp.getContext(),"mylanmac",null);
     //                                                            SharedPreferencesUtils.setParam(MyApp.getContext(), "stepsnum", "0");
     //                                                            startActivity(new Intent(SetActivity.this,NewSearchActivity.class));
     //                                                            finish();
     //                                                        }else{
     //                                                            //已取消配对
     //                                                            MyApp.getWatchBluetoothService().disconnect();//断开蓝牙
     //                                                            MyCommandManager.deviceDisconnState = true;
     //                                                            MyCommandManager.ADDRESS = null;
     //                                                            MyCommandManager.DEVICENAME = null;
     //                                                            SharedPreferencesUtils.saveObject(MyApp.getContext(),"mylanya",null);
     //                                                            SharedPreferencesUtils.saveObject(MyApp.getContext(),"mylanmac",null);
     //                                                            SharedPreferencesUtils.setParam(MyApp.getContext(), "stepsnum", "0");
     //                                                            startActivity(new Intent(SetActivity.this,NewSearchActivity.class));
     //                                                            finish();
     //                                                        }
     //                                                    }
     //                                                }
     */
}
