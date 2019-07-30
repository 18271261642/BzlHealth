package com.bozlun.health.android.b30;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.FeedbackActivity;
import com.bozlun.health.android.activity.ModifyPasswordActivity;
import com.bozlun.health.android.activity.NewLoginActivity;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.bleus.H8ConnstateListener;
import com.bozlun.health.android.siswatch.utils.UpdateManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.umeng.analytics.MobclickAgent;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * B30手环系统设置页面
 */
public class B30SysSettingActivity extends WatchBaseActivity {


    @BindView(R.id.bar_titles)
    TextView barTitles;
    @BindView(R.id.version_tv)
    TextView versionTv;

    UpdateManager updateManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_syssetting);
        ButterKnife.bind(this);


        initViews();

        initData();
    }

    private void initData() {

        try {
            versionTv.setText(packageName(MyApp.getContext()) + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateApp();

    }

    public static String packageName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }


    private void initViews() {
        barTitles.setText(getResources().getString(R.string.system_settings));

    }

    @OnClick({R.id.image_back, R.id.updatePwdLin, R.id.feebackLin, R.id.aboutLin,R.id.commExit_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_back:   //返回
                finish();
                break;
            case R.id.updatePwdLin:  //修改密码
                SharedPreferences share = getSharedPreferences("Login_id", 0);
                String userId = (String) SharedPreferencesUtils.readObject(B30SysSettingActivity.this, "userId");
                if (!WatchUtils.isEmpty(userId) && userId.equals("9278cc399ab147d0ad3ef164ca156bf0")) {
                    ToastUtil.showToast(B30SysSettingActivity.this, getResources().getString(R.string.noright));
                } else {
                    int isoff = share.getInt("id", 0);
                    if (isoff == 0) {
                        startActivity(ModifyPasswordActivity.class);
                    } else {
                        ToastUtil.showToast(B30SysSettingActivity.this, getResources().getString(R.string.string_third_login_changepass));
                    }
                }
                break;
            case R.id.feebackLin:    //意见反馈
                startActivity(FeedbackActivity.class);
                break;
            case R.id.aboutLin:
                updateApp();
                break;
            case R.id.commExit_login:   //退出登录
                showExitdialog();
                break;
        }
    }

    //更新APP
    private void updateApp(){
        updateManager =
                new UpdateManager(this, Commont.FRIEND_BASE_URL + URLs.bozlun_health_url);
        updateManager.checkForUpdate(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updateManager != null)
            updateManager.destoryUpdateBroad();
    }

    public String getVersionName() throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        String version = packInfo.versionName;
        return version;
    }

    //退出提示
    private void showExitdialog() {
        final String bleName = (String) SharedPreferencesUtils.readObject(B30SysSettingActivity.this, Commont.BLENAME);
        if (!WatchUtils.isEmpty(bleName)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.exit_login)
                    .content(R.string.confrim_exit)
                    .positiveText(getResources().getString(R.string.confirm))
                    .negativeText(getResources().getString(R.string.cancle))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            new MaterialDialog.Builder(B30SysSettingActivity.this)
                                    .title(getResources().getString(R.string.prompt))
                                    .content(getResources().getString(R.string.confirm_unbind_strap))
                                    .positiveText(getResources().getString(R.string.confirm))
                                    .negativeText(getResources().getString(R.string.cancle))
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                            alertLogoutMsg(bleName);
                                        }
                                    }).show();

                        }
                    }).show();
        }else{
            clearCommLogoutApp();
        }

    }

    //退出提示
    private void alertLogoutMsg(String bleName) {
        if(bleName.equals("bozlun")){   //H8手表
            // showLoadingDialog("disconn...");
            if(MyCommandManager.DEVICENAME != null){
                SharedPreferencesUtils.saveObject(B30SysSettingActivity.this, Commont.BLEMAC, "");
                MyApp.getInstance().h8BleManagerInstance().disConnH8(new H8ConnstateListener() {
                    @Override
                    public void h8ConnSucc() {

                    }

                    @Override
                    public void h8ConnFailed() {

                    }
                });
            }

        }else if(bleName.equals("B30") || bleName.equals("B36") || bleName.equals("B31")){
            if(MyCommandManager.DEVICENAME != null){
                SharedPreferencesUtils.saveObject(B30SysSettingActivity.this, Commont.BLEMAC, "");
                MyApp.getInstance().getVpOperateManager().disconnectWatch(new IBleWriteResponse() {
                    @Override
                    public void onResponse(int stateCode) {
                        if(stateCode == -1){
                            SharedPreferencesUtils.setParam(MyApp.getContext(), Commont.DEVICESCODE, "0000");
                            //clearCommLogoutApp();
                        }
                    }
                });
            }
        }
        clearCommLogoutApp();
    }

    //清除数据
    private void clearCommLogoutApp(){
        MyCommandManager.ADDRESS = null;
        MyCommandManager.DEVICENAME = null;
        SharedPreferencesUtils.saveObject(B30SysSettingActivity.this, Commont.BLENAME, "");
        SharedPreferencesUtils.saveObject(B30SysSettingActivity.this, Commont.BLEMAC, "");
       // MyApp.getInstance().getDaoSession().getStepBeanDao().deleteAll();//清空数据库
        MyApp.getInstance().setMacAddress(null);// 清空全局
        SharedPreferencesUtils.saveObject(B30SysSettingActivity.this, "userId", null);
        SharedPreferencesUtils.saveObject(MyApp.getContext(), "userInfo", "");
        SharedPreferencesUtils.setParam(B30SysSettingActivity.this, "stepsnum", "0");
        SharedPreferencesUtils.setParam(B30SysSettingActivity.this, SharedPreferencesUtils.CUSTOMER_ID, "");
        SharedPreferencesUtils.setParam(B30SysSettingActivity.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, "");
        Common.customer_id = null;
        MobclickAgent.onProfileSignOff();
        startActivity(NewLoginActivity.class);
        finish();
    }


}
