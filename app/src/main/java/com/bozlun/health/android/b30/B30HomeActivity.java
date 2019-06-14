package com.bozlun.health.android.b30;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.android.internal.telephony.ITelephony;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.FragmentAdapter;
import com.bozlun.health.android.b30.b30homefragment.B30HomeFragment;
import com.bozlun.health.android.b30.b30run.B36RunFragment;
import com.bozlun.health.android.b30.service.FriendsUploadServices;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.commdbserver.CommDBManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.mine.WatchMineFragment;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.widget.NoScrollViewPager;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.veepoo.protocol.listener.data.IDeviceControlPhone;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/7/20.
 */

public class B30HomeActivity extends WatchBaseActivity implements IDeviceControlPhone {


    @BindView(R.id.b30View_pager)
    NoScrollViewPager b30ViewPager;
    @BindView(R.id.b30BottomBar)
    BottomBar b30BottomBar;

    private List<Fragment> b30FragmentList = new ArrayList<>();


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    if (MyApp.getInstance().getB30ConnStateService() != null) {
                        String bm = (String) SharedPreferencesUtils.readObject(B30HomeActivity.this, Commont.BLEMAC);//设备mac
                        if (!TextUtils.isEmpty(bm))
                            MyApp.getInstance().getB30ConnStateService().connectAutoConn(true);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_home);
        ButterKnife.bind(this);
        initViews();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //初始设置挂断电话胡回调监听
        MyApp.getInstance().getVpOperateManager().settingDeviceControlPhone(this);
    }

    private Vibrator vibrator;

    public void vibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(3000); //参数标识  震动持续多少毫秒
        }
    }


    private void initViews() {
        b30FragmentList.add(new B30HomeFragment());
//        b30FragmentList.add(new B30DataFragment());
//        b30FragmentList.add(new W30sNewRunFragment());
//        b30FragmentList.add(new B30RunFragment());   //跑步
        b30FragmentList.add(new B36RunFragment());   //跑步
        // b30FragmentList.add(new B30MineFragment());
        b30FragmentList.add(new WatchMineFragment());
        FragmentStatePagerAdapter fragmentPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), b30FragmentList);
        if (b30ViewPager != null) {
            b30ViewPager.setAdapter(fragmentPagerAdapter);
            b30ViewPager.setOffscreenPageLimit(0);
        }
        if (b30BottomBar != null) b30BottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                if (b30ViewPager == null) return;
                switch (tabId) {
                    case R.id.b30_tab_home: //首页
                        b30ViewPager.setCurrentItem(0, false);
                        break;
//                    case R.id.b30_tab_data: //数据
//                        b30ViewPager.setCurrentItem(1, false);
//                        break;
                    case R.id.b30_tab_set:  //开跑
                        b30ViewPager.setCurrentItem(1, false);
                        break;
                    case R.id.b30_tab_my:   //我的
                        b30ViewPager.setCurrentItem(2, false);
                        break;
                }
            }
        });
    }

    /**
     * 重新连接设备B30
     */
    public void reconnectDevice() {
        if (MyCommandManager.ADDRESS == null) {    //未连接
            if (MyApp.getInstance().getB30ConnStateService() != null) {
                String bm = (String) SharedPreferencesUtils.readObject(B30HomeActivity.this, Commont.BLEMAC);//设备mac
//                Log.d("----去自动链接-", "go go go3" + bm);
                if (!WatchUtils.isEmpty(bm)) {
//                    Log.d("----去自动链接-", "go go go4" + bm);

                    //MyApp.getB30ConnStateService().connB30ConnBle(bm.trim());
                    MyApp.getInstance().getB30ConnStateService().connectAutoConn(true);
                }
            } else {
                handler.sendEmptyMessageDelayed(1001, 3 * 1000);
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
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

    /**
     * 启动上传数据的服务
     */
    public void startUploadDate() {
        try {
            CommDBManager.getCommDBManager().startUploadDbService(this);
            //上传缓存的详细数据
            Intent intent1 = new Intent(this,FriendsUploadServices.class);
            this.startService(intent1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void rejectPhone() {
        try {
            /**
             * 通过反射的方式挂断电话
             */
            //获取到ServiceManager
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);//getSystemService内部就是调用了ServiceManager的getService方法。
            IBinder binder = (IBinder) method.invoke(null,
                    new Object[]{TELEPHONY_SERVICE});
            ITelephony iTelephony = ITelephony.Stub.asInterface(binder);
            iTelephony.endCall();
//            Log.d("call---", "rejectPhone: " + "电话被挂断了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void cliencePhone() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
//            Log.d("call---", "RINGING 已被静音");
        }
    }

    @Override
    public void knocknotify(int i) {

    }

    @Override
    public void sos() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
