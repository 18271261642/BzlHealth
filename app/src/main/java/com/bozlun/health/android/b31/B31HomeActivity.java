package com.bozlun.health.android.b31;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.FragmentAdapter;
import com.bozlun.health.android.b30.b30run.B36RunFragment;
import com.bozlun.health.android.b30.service.VerB30PwdListener;
import com.bozlun.health.android.b31.record.B31RecordFragment;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.bzlmaps.sos.GPSGaoDeUtils;
import com.bozlun.health.android.bzlmaps.sos.GPSGoogleUtils;
import com.bozlun.health.android.bzlmaps.sos.SendSMSBroadCast;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.mine.WatchMineFragment;
import com.bozlun.health.android.siswatch.utils.PhoneUtils;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.VerifyUtil;
import com.bozlun.health.android.view.CusInputDialogView;
import com.bozlun.health.android.widget.NoScrollViewPager;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IDeviceControlPhone;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.Setting;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * B31的主activity
 * Created by Admin
 * Date 2018/12/17
 */
public class B31HomeActivity extends WatchBaseActivity implements IDeviceControlPhone, Rationale<List<String>> {


    @BindView(R.id.b31View_pager)
    NoScrollViewPager b31ViewPager;
    @BindView(R.id.b31BottomBar)
    BottomBar b31BottomBar;


    private List<Fragment> fragmentList = new ArrayList<>();

    //列设备验证密码提示框
    CusInputDialogView cusInputDialogView;

    private SendSMSBroadCast sendSMSBroadCast;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    if (MyApp.getInstance().getB30ConnStateService() != null) {
                        String bm = (String) SharedPreferencesUtils.readObject(B31HomeActivity.this, Commont.BLEMAC);//设备mac
                        if (!WatchUtils.isEmpty(bm))
                            MyApp.getInstance().getB30ConnStateService().connectAutoConn(true);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b31_home);
        ButterKnife.bind(this);


        initViews();
        registerReceiver(broadcastReceiver, new IntentFilter("com.example.bozhilun.android.siswatch.CHANGEPASS"));
        sendSMSBroadCast = new SendSMSBroadCast();
        IntentFilter intentFilter = new IntentFilter("com.example.bozhilun.android.sos.SENDSMS");
        registerReceiver(sendSMSBroadCast,intentFilter);


//        MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(this);
        MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(MyApp.getPhoneSosOrDisPhone());

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        fragmentList.add(new B31RecordFragment());
        //fragmentList.add(new B30DataFragment());
        fragmentList.add(new B36RunFragment());
        fragmentList.add(new WatchMineFragment());
        FragmentStatePagerAdapter fragmentPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList);
        if (b31ViewPager != null) {
            b31ViewPager.setAdapter(fragmentPagerAdapter);
            b31ViewPager.setOffscreenPageLimit(0);

        }
        b31BottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                switch (tabId) {
                    case R.id.b30_tab_home: //首页
                        b31ViewPager.setCurrentItem(0, false);
                        break;
//                    case R.id.b30_tab_data: //数据
//                        b31ViewPager.setCurrentItem(1, false);
//                        break;
                    case R.id.b30_tab_set:  //开跑
                        b31ViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.b30_tab_my:   //我的
                        b31ViewPager.setCurrentItem(2, false);
                        break;
                }
            }
        });
    }


    /**
     * 重新连接设备
     */
    public void reconnectDevice() {
        if (MyCommandManager.ADDRESS == null) {    //未连接
            if (MyApp.getInstance().getB30ConnStateService() != null) {
                String bm = (String) SharedPreferencesUtils.readObject(B31HomeActivity.this, Commont.BLEMAC);//设备mac
                if (!WatchUtils.isEmpty(bm)) {
                    MyApp.getInstance().getB30ConnStateService().connectAutoConn(true);
                }
            } else {
                handler.sendEmptyMessageDelayed(1001, 3 * 1000);
            }
        }
    }


    /**
     * 启动上传数据的服务
     */
    public void startUploadDate() {
        boolean uploading = MyApp.getInstance().isUploadDate();
        if (!uploading) {// 判断一下是否正在上传数据
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        if(sendSMSBroadCast != null)
            unregisterReceiver(sendSMSBroadCast);
        if (cusInputDialogView != null)
            cusInputDialogView.cancel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);

        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            moveTaskToBack(true);
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals("com.example.bozhilun.android.siswatch.CHANGEPASS")) {
                showB30InputPwd();  //弹出输入密码的提示框
            }
        }
    };


    //提示输入密码
    private void showB30InputPwd() {
        if (cusInputDialogView == null) {
            cusInputDialogView = new CusInputDialogView(B31HomeActivity.this);
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
                //handler.sendEmptyMessage(777);
            }

            @Override
            public void cusDialogSureData(String data) {
                MyApp.getInstance().getB30ConnStateService().continuteConn(data, new VerB30PwdListener() {
                    @Override
                    public void verPwdFailed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showCusToast(B31HomeActivity.this, getResources().getString(R.string.miamacuo));
                            }
                        });

                        //ToastUtil.showLong(B31HomeActivity.this, getResources().getString(R.string.miamacuo));
                    }

                    @Override
                    public void verPwdSucc() {
                        cusInputDialogView.dismiss();
                    }
                });
            }
        });

    }

    //挂断电话
    @Override
    public void rejectPhone() {
        try {
            TelephonyManager tm = (TelephonyManager) MyApp.getContext()
                    .getSystemService(Service.TELEPHONY_SERVICE);
            PhoneUtils.endPhone(MyApp.getContext(), tm);
            PhoneUtils.dPhone();
            PhoneUtils.endCall(MyApp.getContext());
           // PhoneUtils.endcall();
            Log.d("call---", "rejectPhone: " + "电话被挂断了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //手环静音提示
    @Override
    public void cliencePhone() {
        getDoNotDisturb();
        try {
            AudioManager audioManager = (AudioManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                audioManager.getStreamVolume(AudioManager.STREAM_RING);
                Log.d("call---", "RINGING 已被静音");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void knocknotify(int i) {

    }

    @Override
    public void sos() {
        if (!Commont.isSosOpen) {
            Commont.isSosOpen = true;
            //Toast.makeText(this,"SOS 执行了 进入",Toast.LENGTH_SHORT).show();
            boolean isSos = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), Commont.ISHelpe, false);//sos
            String stringpersonOne = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personOne", "");
            String stringpersonTwo = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personTwo", "");
            String stringpersonThree = (String) SharedPreferencesUtils.getParam(MyApp.getInstance(), "personThree", "");
            if ((!TextUtils.isEmpty(stringpersonOne)
                    || !TextUtils.isEmpty(stringpersonTwo)
                    || !TextUtils.isEmpty(stringpersonThree))
                    && isSos) {
                //Toast.makeText(this,"SOS 执行了 电话和条件允许",Toast.LENGTH_SHORT).show();
                Commont.COUNTNUMBER = 0;
                Commont.GPSCOUNT = 0;
//                Commont.isGPSed = true;
                Log.e("===", "======开始定位");
                getGps();

                Log.e("===", "======5 秒后打电话");
                handler.sendEmptyMessageAtTime(0x01, 5000);
                //handler.sendEmptyMessageAtTime(0x02, 1000 * 60 * 3);
            } else {
                Commont.isSosOpen = false;
                ToastUtil.showShort(B31HomeActivity.this, "SOS未打开或者没有添加紧急联系人");
            }

        }
    }

    GPSGoogleUtils instance;

    /**
     * 获取定位以及发送短信
     */
    void getGps() {
        AndPermission.with(this)
                .runtime()
//                .permission(Permission.Group.SMS, Permission.Group.LOCATION)
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION,
                        //--------------
                        Permission.SEND_SMS
                )
                .rationale(this)//添加拒绝权限回调
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
//                        GPSGaoDeUtils.getInstance(B30HomeActivity.this);

//                        Commont.isSosOpen = true;
//                        boolean zh = VerifyUtil.isZh(MyApp.getInstance());
//                        if (zh) {
//                            GPSGaoDeUtils.getInstance(MyApp.getInstance());
//                        } else {
//                            instance = GPSGoogleUtils.getInstance(MyApp.getInstance());
//                            getGpsGoogle();
//                        }  13145994816

                        boolean zh = VerifyUtil.isZh(MyApp.getInstance());
                        if (zh) {
                            Boolean zhonTW = getResources().getConfiguration().locale.getCountry().equals("TW");
                            Log.e("======", zh + "====" + zhonTW);
                            if (zhonTW) {
                                instance = GPSGoogleUtils.getInstance(MyApp.getInstance());
                                getGpsGoogle();
                            } else {
                                GPSGaoDeUtils.getInstance(MyApp.getInstance());
                            }
                        } else {
                            instance = GPSGoogleUtils.getInstance(MyApp.getInstance());
                            getGpsGoogle();
                        }

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        /**
                         * 当用户没有允许该权限时，回调该方法
                         */
                        Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                        /**
                         * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                         */
                        if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
                            //true，弹窗再次向用户索取权限
                            showSettingDialog(B31HomeActivity.this, data);
                        }
                    }
                }).start();
    }


    void getGpsGoogle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean b = instance.startLocationUpdates(MyApp.getInstance());
                if (!b) {
                    getGpsGoogle();
                }
            }
        }, 3000);
    }


    /**
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = getResources().getString(R.string.string_get_permission) + "\n" + permissionNames;
//                context.getString("Please give us permission in the settings:\\n\\n%1$s", TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.prompt))
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        //Toast.makeText(MyApp.getContext(),"用户从设置页面返回。", Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }


    @Override
    public void showRationale(Context context, List<String> data, final RequestExecutor executor) {
        List<String> permissionNames = Permission.transformText(context, data);
        String message = getResources().getString(R.string.string_get_permission) + "\n" + permissionNames;

        new android.app.AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.prompt))
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.execute();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.cancel();
                    }
                })
                .show();
    }

    /**
     * 打电话
     *
     * @param tel
     */
    //点击事件调用的类
    protected void call(final String tel) {

        AndPermission.with(B31HomeActivity.this)
                .runtime()
                .permission(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
//                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.USE_SIP
//                        Manifest.permission.PROCESS_OUTGOING_CALLS
                )
                .rationale(this)
                .rationale(this)//添加拒绝权限回调
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //Toast.makeText(B30HomeActivity.this,"SOS 执行了 拨打电话 "+tel,Toast.LENGTH_SHORT).show();
                        //直接拨打
//                        Log.d("GPS", "call:" + tel);
                        Uri uri = Uri.parse("tel:" + tel);
                        Intent intent = new Intent(Intent.ACTION_CALL, uri);
                        if (ActivityCompat.checkSelfPermission(B31HomeActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(intent);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        /**
                         * 当用户没有允许该权限时，回调该方法
                         */
                        Toast.makeText(MyApp.getContext(), getString(R.string.string_no_permission), Toast.LENGTH_SHORT).show();
                        /**
                         * 判断用户是否点击了禁止后不再询问，AndPermission.hasAlwaysDeniedPermission(MainActivity.this, data)
                         */
                        if (AndPermission.hasAlwaysDeniedPermission(MyApp.getContext(), data)) {
                            //true，弹窗再次向用户索取权限
                            showSettingDialog(B31HomeActivity.this, data);
                        }
                    }
                }).start();
    }


    //获取Do not disturb权限,才可进行音量操作
    private void getDoNotDisturb(){
        NotificationManager notificationManager =
                (NotificationManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            MyApp.getInstance().getApplicationContext().startActivity(intent);
        }

    }

}
