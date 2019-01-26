package com.bozlun.health.android.b31;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.FragmentAdapter;
import com.bozlun.health.android.b30.b30datafragment.B30DataFragment;
import com.bozlun.health.android.b30.b30run.B36RunFragment;
import com.bozlun.health.android.b30.service.CommVpDateUploadService;
import com.bozlun.health.android.b30.service.DateUploadService;
import com.bozlun.health.android.b30.service.VerB30PwdListener;
import com.bozlun.health.android.b31.record.B31RecordFragment;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.mine.WatchMineFragment;
import com.bozlun.health.android.siswatch.utils.PhoneUtils;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.view.CusInputDialogView;
import com.bozlun.health.android.widget.NoScrollViewPager;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IDeviceControlPhone;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * B31的主activity
 * Created by Admin
 * Date 2018/12/17
 */
public class B31HomeActivity extends WatchBaseActivity implements IDeviceControlPhone {


    @BindView(R.id.b31View_pager)
    NoScrollViewPager b31ViewPager;
    @BindView(R.id.b31BottomBar)
    BottomBar b31BottomBar;


    private List<Fragment> fragmentList = new ArrayList<>();

    //列设备验证密码提示框
    CusInputDialogView cusInputDialogView;


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
        MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        fragmentList.add(new B31RecordFragment());
        fragmentList.add(new B30DataFragment());
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
                    case R.id.b30_tab_data: //数据
                        b31ViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.b30_tab_set:  //开跑
                        b31ViewPager.setCurrentItem(2, false);
                        break;
                    case R.id.b30_tab_my:   //我的
                        b31ViewPager.setCurrentItem(3, false);
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
            startService(new Intent(this, CommVpDateUploadService.class));
            startService(new Intent(this, DateUploadService.class));

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
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
        AudioManager audioManager = (AudioManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
            Log.d("call---", "RINGING 已被静音");
        }
    }

    @Override
    public void knocknotify(int i) {

    }

    @Override
    public void sos() {

    }
}
