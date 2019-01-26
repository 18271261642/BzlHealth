package com.bozlun.health.android.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.HomeAdapter;
import com.bozlun.health.android.b30.B30HomeActivity;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.bean.BlueUser;
import com.bozlun.health.android.h9.H9HomeActivity;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.WatchHomeActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.w30s.W30SHomeActivity;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.W30SBLEServices;
import com.yanzhenjie.permission.AndPermission;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * 滑动页
 */
public class GuideActivity extends BaseActivity {

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;//启动时就开启来电监听

    private static final int READ_CONTACTS_CODE = 1001;
    private static final int READ_MSG_CODE = 1002;



    @Override
    protected void initViews() {

        try {
            boolean serviceRunning = W30SBLEServices.isServiceRunning(this, "com.suchengkeji.android.w30sblelibrary.W30SBLEServices");
            Log.e("--------调试-", "-GuideActivity-onStart--" + serviceRunning);
            if (!serviceRunning){MyApp.getInstance().getmW30SBLEManage().openW30SBLEServices();}
        } catch (Exception e) {
            e.getMessage();
        }

        //B30目标步数 默认8000
        int goalStep = (int) SharedPreferencesUtils.getParam(MyApp.getContext(),"b30Goal",0);
        if(goalStep==0){
            SharedPreferencesUtils.setParam(MyApp.getContext(),"b30Goal",8000);
        }
        String b30SleepGoal = (String) SharedPreferencesUtils.getParam(MyApp.getContext(),"b30SleepGoal","");
        if(WatchUtils.isEmpty(b30SleepGoal)){
            SharedPreferencesUtils.setParam(MyApp.getContext(),"b30SleepGoal","8.0");
        }
        //B30的默认密码
        String b30Pwd = (String) SharedPreferencesUtils.getParam(MyApp.getContext(),Commont.DEVICESCODE,"");
        if(WatchUtils.isEmpty(b30Pwd)){
            SharedPreferencesUtils.setParam(MyApp.getContext(),Commont.DEVICESCODE,"0000");
        }
        //SharedPreferencesUtils.saveObject(MyApp.getContext(),"mylanya","H9");
        String isFirst = (String) SharedPreferencesUtils.getParam(GuideActivity.this, "msgfirst", "");
        if (WatchUtils.isEmpty(isFirst)) {
            SharedPreferencesUtils.saveObject(GuideActivity.this, "weixinmsg", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "msg", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "qqmsg", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "Viber", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "Twitteraa", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "facebook", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "Whatsapp", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "Instagrambutton", "1");
            SharedPreferencesUtils.saveObject(GuideActivity.this, "laidian", "0");
            SharedPreferencesUtils.setParam(MyApp.getContext(), "laidianphone", "off");
        }
        //初始化H8消息提醒功能
        SharedPreferencesUtils.setParam(GuideActivity.this, "msgfirst", "isfirst");
        //申请读取通讯录的权限
        AndPermission.with(GuideActivity.this)
                .runtime()
                .permission(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_SMS, Manifest.permission.WRITE_SETTINGS)
                .start();
        switchLoginUser();

        List<View> pageList = createPageList();
        HomeAdapter adapter = new HomeAdapter();
        adapter.setData(pageList);
        viewPager.setAdapter(adapter);


    }

    private void switchLoginUser(){
        String isFirstData = (String) SharedPreferencesUtils.getParam(GuideActivity.this, "isFirst", "");
        if (!WatchUtils.isEmpty(isFirstData) && "on".equals(isFirstData)) {   //不是第一次进入
            try {
                if (null != SharedPreferencesUtils.readObject(GuideActivity.this, "userInfo")) {
                    String userInfo = SharedPreferencesUtils.readObject(GuideActivity.this, "userInfo").toString();
                    System.out.print("userInfo" + userInfo);
                    Gson gson = new Gson();
                    BlueUser userInfos = gson.fromJson(userInfo, BlueUser.class);
                    Common.userInfo = userInfos;
                    Common.customer_id = userInfos.getUserId();
                    // MyApp.getApplication().getDaoSession().getBlueUserDao().insertOrReplace(userInfos);

                }
                //判断有没有登录
                if (null != SharedPreferencesUtils.readObject(GuideActivity.this, "userId")) {
                    Log.e("GuideActivity", "--------蓝牙---" + SharedPreferencesUtils.readObject(GuideActivity.this,  Commont.BLENAME));
                    String btooth = (String) SharedPreferencesUtils.readObject(GuideActivity.this,  Commont.BLENAME);
                    if (!WatchUtils.isEmpty(btooth)) {
                        if ("bozlun".equals(btooth)) {    //H8 手表
                            startActivity(new Intent(GuideActivity.this, WatchHomeActivity.class));
                        }
                        else if ("H9".equals(btooth)) {   // H9 手表
                            startActivity(new Intent(GuideActivity.this, H9HomeActivity.class));
                        }
                        else if ("W30".equals(btooth) || "w30".equals(btooth)) {
                            startActivity(new Intent(GuideActivity.this, W30SHomeActivity.class));
                        }else if("B30".equals(btooth)){
                            startActivity(new Intent(GuideActivity.this, B30HomeActivity.class));
                        }
                        else {
                            startActivity(new Intent(GuideActivity.this, NewSearchActivity.class));
                        }
                    } else {
                        startActivity(new Intent(GuideActivity.this, NewSearchActivity.class));
                    }
                    finish();
                } else {
//                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                    startActivity(new Intent(GuideActivity.this, NewLoginActivity.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //单位默认为公制
        //SPUtils.putValue(GuideActivity.this,"unit","metric");


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获得activity的Window对象，设置其属性
//        Window mWindow = getWindow();
//        mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
//        WindowManager.LayoutParams attrParams = mWindow.getAttributes();
//        attrParams.x = 0;
//        attrParams.y = 0;
//        attrParams.height = 1;
//        attrParams.width = 1;
//        mWindow.setAttributes(attrParams);
//        // 绑定SinglePixelActivity到ScreenManager
//        ScreenManager.getScreenManagerInstance(this).setSingleActivity(this);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(!WatchUtils.isAPPALive(GuideActivity.this, "com.example.bozhilun.android")){
//            switchLoginUser();
//
//        }

    }

    private final int[] imageIds = {R.mipmap.beraceiocn, R.mipmap.beraceiocn
            ,R.mipmap.beraceiocn};

    @NonNull
    private List<View> createPageList() {
        List<View> pageList = new ArrayList<>();
        //去掉直接用mipmap.image,用添加ImageView方法，防止图片太大要求的内存太多，超出限制出现OOM
        //获取屏幕的默认分辨率
        Display display = getWindowManager().getDefaultDisplay();
        for (int i = 0; i < imageIds.length; i++) {
            ImageView image = new ImageView(this);
            image.setMinimumHeight(display.getHeight());
            image.setMinimumWidth(display.getWidth());
            image.setMaxHeight(display.getHeight());
            image.setMaxHeight(display.getWidth());
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            InputStream is = getResources().openRawResource(
                    imageIds[i] );
            Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
            BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
            image.setBackgroundDrawable(bd);
            pageList.add(createPageViews(image, i));
        }

//        pageList.add(createPageView(R.mipmap.image_guide_one, 0));
//        pageList.add(createPageView(R.mipmap.image_guide_two, 1));
//        pageList.add(createPageView(R.mipmap.image_guide_three, 2));
        return pageList;
    }


    private View createPageViews(ImageView drawable, final int index) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.banner_guide, null,false);
        RelativeLayout activity_guide = (RelativeLayout) contentView.findViewById(R.id.activity_guide);
        if (index == 2) {
            activity_guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.setParam(GuideActivity.this, "w30sunit", true);
                    SharedPreferencesUtils.setParam(GuideActivity.this, "isFirst", "on");
//                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                    startActivity(new Intent(GuideActivity.this, NewLoginActivity.class));
                    finish();
                }
            });
        }
        activity_guide.addView(drawable);
        return contentView;
    }

    @NonNull
    private View createPageView(int drawable, int index) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.banner_guide, null);
        RelativeLayout activity_guide = (RelativeLayout) contentView.findViewById(R.id.activity_guide);
        TextView txt_one = (TextView) contentView.findViewById(R.id.guide_txt_1);
        TextView txt_two = (TextView) contentView.findViewById(R.id.guide_txt_2);
        String[] txt_banner_1 = this.getResources().getStringArray(R.array.banner_txt_one);
        String[] txt_banner_2 = this.getResources().getStringArray(R.array.banner_txt_two);
        txt_one.setText(txt_banner_1[index]);
        //txt_one.setLetterSpacing(5);
        txt_two.setText(txt_banner_2[index]);
        //txt_two.setLetterSpacing(5);
        if (index == 2) {
            //Button go_home = (Button) contentView.findViewById(R.id.go_home);
            //go_home.setVisibility(View.VISIBLE);
            activity_guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtils.setParam(GuideActivity.this, "isFirst", "on");
//                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                    startActivity(new Intent(GuideActivity.this, NewLoginActivity.class));
                    finish();
                }
            });
//            go_home.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    SharedPreferencesUtils.setParam(GuideActivity.this, "isFirst", "on");
//                    startActivity(new Intent(GuideActivity.this, LoginActivity.class));
//                    finish();
//                }
//            });
        }
        activity_guide.setBackgroundResource(drawable);
        return contentView;
    }


    @Override
    protected int getStatusBarColor() {
        return R.color.transparent;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_guide;
    }

    @OnClick(R.id.viewPager)
    public void onClick() {
        SharedPreferencesUtils.setParam(GuideActivity.this, "isFirst", "on");
//        startActivity(new Intent(GuideActivity.this, LoginActivity.class));
        startActivity(new Intent(GuideActivity.this, NewLoginActivity.class));
        finish();
    }

}
