package com.bozlun.health.android.b31;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aigestudio.wheelpicker.widgets.ProfessionPick;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.B30CounDownActivity;
import com.bozlun.health.android.b30.B30DufActivity;
import com.bozlun.health.android.b30.B30LongSitSetActivity;
import com.bozlun.health.android.b30.B30MessAlertActivity;
import com.bozlun.health.android.b30.B30ResetActivity;
import com.bozlun.health.android.b30.B30ScreenStyleActivity;
import com.bozlun.health.android.b30.B30TrunWristSetActivity;
import com.bozlun.health.android.b30.view.B30DeviceAlarmActivity;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;

import com.bozlun.health.android.w30s.carema.W30sCameraActivity;
import com.bozlun.health.android.w30s.wxsport.WXSportActivity;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
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
import butterknife.OnClick;

/**
 * B31的设备页面
 * Created by Admin
 * Date 2018/12/18
 */
public class B31DeviceActivity extends WatchBaseActivity implements Rationale<List<String>> {


    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.b31DeviceSportGoalTv)
    TextView b31DeviceSportGoalTv;
    @BindView(R.id.b31DeviceSleepGoalTv)
    TextView b31DeviceSleepGoalTv;
    @BindView(R.id.b31DeviceUnitTv)
    TextView b31DeviceUnitTv;
    @BindView(R.id.b31DeviceStyleRel)
    RelativeLayout b31DeviceStyleRel;

    private AlertDialog.Builder builder;

    //运动目标
    ArrayList<String> sportGoalList;
    //睡眠目标
    ArrayList<String> sleepGoalList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b31_device_layout);
        ButterKnife.bind(this);

        initViews();
        initData();


    }

    private void initData() {

        sportGoalList = new ArrayList<>();
        sleepGoalList = new ArrayList<>();
        for (int i = 1000; i <= 64000; i += 1000) {
            sportGoalList.add(i + "");
        }

        for (int i = 1; i < 48; i++) {
            sleepGoalList.add(WatchUtils.mul(Double.valueOf(i), 0.5) + "");
        }



        //显示运动目标和睡眠目标
        int b30SportGoal = (int) SharedPreferencesUtils.getParam(B31DeviceActivity.this, "b30Goal", 0);
        b31DeviceSportGoalTv.setText(b30SportGoal+"");
        //睡眠
        String b30SleepGoal = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), "b30SleepGoal", "");
        b31DeviceSleepGoalTv.setText(b30SleepGoal+"");
        //公英制
        boolean isMeter = (boolean) SharedPreferencesUtils.getParam(MyApp.getContext(), "isSystem", true);//是否为公制
        b31DeviceUnitTv.setText(isMeter?getResources().getString(R.string.setkm):getResources().getString(R.string.setmi));
    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.device));

    }

    @OnClick({R.id.commentB30BackImg, R.id.b31DeviceMsgRel,
            R.id.b31DeviceAlarmRel, R.id.b31DeviceLongSitRel,
            R.id.b31DeviceWristRel,
            R.id.img233, R.id.b31DeviceSportRel,
            R.id.b31DeviceSleepRel, R.id.b31DeviceUnitRel,
            R.id.b31DeviceSwitchRel, R.id.b31DevicePtoRel,
            R.id.b31DeviceResetRel, R.id.b31DeviceStyleRel,
            R.id.b31DeviceDfuRel, R.id.b31DeviceClearDataRel,
            R.id.wxSportRel, R.id.b31DisConnBtn,R.id.b31DeviceCounDownRel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:    //返回
                finish();
                break;
            case R.id.b31DeviceMsgRel:  //消息提醒
                startActivity(B30MessAlertActivity.class);
                break;
            case R.id.b31DeviceAlarmRel:    //闹钟设置
                startActivity(B30DeviceAlarmActivity.class);
                break;
            case R.id.b31DeviceLongSitRel:  //久坐提醒
                startActivity(B30LongSitSetActivity.class);
                break;
            case R.id.b31DeviceWristRel:    //转腕亮屏
                startActivity(B30TrunWristSetActivity.class);
                break;
            case R.id.b31DeviceSportRel:    //运动目标
                setSportGoal();
                break;
            case R.id.b31DeviceSleepRel:    //睡眠目标
                setSleepGoal();
                break;
            case R.id.b31DeviceUnitRel:     //单位设置
                showUnitDialog();
                break;
            case R.id.b31DeviceSwitchRel:   //开关设置
                startActivity(B31SwitchActivity.class);
                break;
            case R.id.b31DeviceCounDownRel:     //倒计时
                startActivity(B30CounDownActivity.class);
                break;
            case R.id.b31DevicePtoRel:      //拍照
                AndPermission.with(this)
                        .runtime()
                        .permission(Permission.Group.CAMERA, Permission.Group.STORAGE)
                        .rationale(this)//添加拒绝权限回调
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                startActivity(W30sCameraActivity.class);
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
                                    showSettingDialog(MyApp.getContext(), data);
                                }
                            }
                        }).start();

                break;
            case R.id.b31DeviceResetRel:    //重置设备密码
                startActivity(B30ResetActivity.class);
                break;
            case R.id.b31DeviceStyleRel:    //界面风格
                startActivity(B30ScreenStyleActivity.class);
                break;
            case R.id.b31DeviceDfuRel:      //固件更新
                startActivity(B30DufActivity.class);
                break;
            case R.id.b31DeviceClearDataRel:    //清除数据

                new MaterialDialog.Builder(this)
                        .title(getResources().getString(R.string.prompt))
                        .content(getResources().getString(R.string.string_is_clear_data))
                        .positiveText(getResources().getString(R.string.confirm))
                        .negativeText(getResources().getString(R.string.cancle))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MyApp.getInstance().getVpOperateManager().clearDeviceData(iBleWriteResponse);
                            }
                        }).show();
                break;
            case R.id.wxSportRel:       //微信运动
                startActivity(WXSportActivity.class, new String[]{"bleName"}, new String[]{"B31"});
                break;
            case R.id.b31DisConnBtn:    //断开连接
                disB30Conn();

                break;
        }
    }


    //展示公英制
    private void showUnitDialog() {

        String runTypeString[] = new String[]{getResources().getString(R.string.setkm),
                getResources().getString(R.string.setmi)};
        builder = new AlertDialog.Builder(B31DeviceActivity.this);
        builder.setTitle(getResources().getString(R.string.select_running_mode))
                .setItems(runTypeString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (i == 0) {
                            b31DeviceUnitTv.setText(getResources().getString(R.string.setkm));
                            SharedPreferencesUtils.setParam(MyApp.getContext(), "isSystem", true);//是否为公制
                           // changeCustomSetting(true);
                        } else {
                            //changeCustomSetting(false);
                            b31DeviceUnitTv.setText(getResources().getString(R.string.setmi));
                            SharedPreferencesUtils.setParam(MyApp.getContext(), "isSystem", false);//是否为公制
                        }
//                        changeCustomSetting(i == 0);
                    }
                }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }


    //设置睡眠目标
    private void setSleepGoal() {
        ProfessionPick dailyumberofstepsPopWin = new ProfessionPick.Builder(B31DeviceActivity.this,
                new ProfessionPick.OnProCityPickedListener() {
                    @Override
                    public void onProCityPickCompleted(String profession) {
                        b31DeviceSleepGoalTv.setText(profession);
                        SharedPreferencesUtils.setParam(B31DeviceActivity.this, "b30SleepGoal", profession.trim());
                    }
                }).textConfirm(getResources().getString(R.string.confirm)) //text of confirm button
                .textCancel(getResources().getString(R.string.cancle)) //text of cancel button
                .btnTextSize(18) // button text size
                .viewTextSize(25) // pick view text size
                .colorCancel(Color.parseColor("#999999")) //color of cancel button
                .colorConfirm(Color.parseColor("#009900"))//color of confirm button
                .setProvinceList(sleepGoalList) //min year in loop
                .dateChose("8.0") // date chose when init popwindow
                .build();
        dailyumberofstepsPopWin.showPopWin(B31DeviceActivity.this);
    }



    //设置运动目标
    private void setSportGoal() {
        ProfessionPick dailyumberofstepsPopWin = new ProfessionPick.Builder(B31DeviceActivity.this,
                new ProfessionPick.OnProCityPickedListener() {
                    @Override
                    public void onProCityPickCompleted(String profession) {
                        b31DeviceSportGoalTv.setText(profession);
                        SharedPreferencesUtils.setParam(B31DeviceActivity.this, "b30Goal", Integer.valueOf(profession.trim()));

                        setDeviceSportGoal();


                    }
                }).textConfirm(getResources().getString(R.string.confirm)) //text of confirm button
                .textCancel(getResources().getString(R.string.cancle)) //text of cancel button
                .btnTextSize(18) // button text size
                .viewTextSize(25) // pick view text size
                .colorCancel(Color.parseColor("#999999")) //color of cancel button
                .colorConfirm(Color.parseColor("#009900"))//color of confirm button
                .setProvinceList(sportGoalList) //min year in loop
                .dateChose("8000") // date chose when init popwindow
                .build();
        dailyumberofstepsPopWin.showPopWin(B31DeviceActivity.this);
    }

    //设置运动目标
    private void setDeviceSportGoal() {
        MyApp.getInstance().getB30ConnStateService().setUserInfoToDevice();
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
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = getResources().getString(R.string.string_get_permission) + "\n" + permissionNames;

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


    //断开连接
    private void disB30Conn() {
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.prompt))
                .content(getResources().getString(R.string.string_devices_is_disconnected))
                .positiveText(getResources().getString(R.string.confirm))
                .negativeText(getResources().getString(R.string.cancle))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (MyCommandManager.DEVICENAME != null) {
                            MyCommandManager.DEVICENAME = null;
                            MyCommandManager.ADDRESS = null;
                            MyApp.getInstance().getVpOperateManager().disconnectWatch(new IBleWriteResponse() {
                                @Override
                                public void onResponse(int state) {
                                    if (state == -1) {
                                        SharedPreferencesUtils.saveObject(B31DeviceActivity.this, Commont.BLENAME, "");
                                        SharedPreferencesUtils.saveObject(B31DeviceActivity.this, Commont.BLEMAC, "");
                                        SharedPreferencesUtils.setParam(MyApp.getContext(), Commont.DEVICESCODE, "0000");
                                        MyApp.getInstance().setMacAddress(null);// 清空全局
                                        startActivity(NewSearchActivity.class);
                                        finish();

                                    }
                                }
                            });
                        } else {
                            MyCommandManager.DEVICENAME = null;
                            MyCommandManager.ADDRESS = null;
                            SharedPreferencesUtils.saveObject(B31DeviceActivity.this, Commont.BLENAME, "");
                            SharedPreferencesUtils.saveObject(B31DeviceActivity.this, Commont.BLEMAC, "");
                            SharedPreferencesUtils.setParam(MyApp.getContext(), Commont.DEVICESCODE, "0000");
                            MyApp.getInstance().setMacAddress(null);// 清空全局
                            startActivity(NewSearchActivity.class);
                            finish();
                        }


                    }
                }).show();
    }





    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {

        }
    };

}
