package com.bozlun.health.android.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.bean.BlueUser;
import com.bozlun.health.android.bean.UserInfoBean;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.utils.UpdateManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.LoginListenter;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.util.ShareSDKUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.view.PromptDialog;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.permission.AndPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.google.GooglePlus;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by thinkpad on 2017/3/3.
 */

public class NewLoginActivity extends BaseActivity implements LoginListenter {

    private static final String TAG = "NewLoginActivity";


    @BindView(R.id.logo_img)
    ImageView logoImg;
    //    @BindView(R.id.login_waveView)
//    LoginWaveView loginWaveView;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.username_input_logon)
    TextInputLayout usernameInputLogon;
    @BindView(R.id.password_logon)
    EditText passwordLogon;
    @BindView(R.id.textinput_password)
    TextInputLayout textinputPassword;
    @BindView(R.id.login_visitorTv)
    TextView loginVisitorTv;
    @BindView(R.id.forget_tv)
    TextView forgetTv;
    @BindView(R.id.login_btn)
    Button loginBtn;
    @BindView(R.id.register_btn)
    Button registerBtn;
    @BindView(R.id.login_linearlayout)
    LinearLayout loginLinearlayout;
    @BindView(R.id.xinlang_iv)
    RelativeLayout xinlangIv;
    @BindView(R.id.qq_iv)
    RelativeLayout qqIv;
    @BindView(R.id.weixin_iv)
    RelativeLayout weixinIv;
    @BindView(R.id.ll_bottom_tabaa)
    LinearLayout llBottomTabaa;
    @BindView(R.id.fecebook_longin)
    RelativeLayout fecebookLongin;
    @BindView(R.id.google_longin)
    RelativeLayout googleLongin;
    @BindView(R.id.twitter_longin)
    RelativeLayout twitterLongin;
    @BindView(R.id.ll_bottom_tabguowai)
    LinearLayout llBottomTabguowai;
    private ShareSDKUtils instance;
    private BluetoothAdapter bluetoothAdapter;  //蓝牙适配器


    //更新
    UpdateManager updateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = ShareSDKUtils.getInstance(this);
        //请求读写SD卡的权限
        AndPermission.with(NewLoginActivity.this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
                .start();

        //判断蓝牙是否开启
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            turnOnBlue();
        }
    }

    private void turnOnBlue() {
        // 请求打开 Bluetooth
        Intent requestBluetoothOn = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // 设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
        requestBluetoothOn
                .setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // 设置 Bluetooth 设备可见时间
        requestBluetoothOn.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                1111);
        // 请求开启 Bluetooth
        this.startActivityForResult(requestBluetoothOn,
                1112);
    }

    @Override
    protected void initViews() {
//        boolean lauage = VerifyUtil.isZh(NewLoginActivity.this);
        Boolean lauage = getResources().getConfiguration().locale.getCountry().equals("CN");
        if (lauage) {
            llBottomTabaa.setVisibility(View.VISIBLE);
            llBottomTabguowai.setVisibility(View.GONE);
        } else {
            llBottomTabaa.setVisibility(View.GONE);
            llBottomTabguowai.setVisibility(View.VISIBLE);
        }
        //if (loginWaveView != null) loginWaveView.startMove();  //波浪线贝塞尔曲线


        String upUrl = URLs.HTTPs + URLs.bozlun_health_url;
        updateManager = new UpdateManager(NewLoginActivity.this, upUrl);
        updateManager.checkForUpdate(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (loginWaveView != null) loginWaveView.stopMove();  //波浪线贝塞尔曲线
        if (updateManager != null)
            updateManager.destoryUpdateBroad();
        if (handler != null) handler = null;
        isShouQuanOnClick = false;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login;
    }


    @OnClick({R.id.login_visitorTv, R.id.forget_tv, R.id.login_btn, R.id.register_btn, R.id.xinlang_iv, R.id.qq_iv, R.id.weixin_iv, R.id.fecebook_longin, R.id.google_longin, R.id.twitter_longin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.login_visitorTv://游客
                final PromptDialog pd = new PromptDialog(NewLoginActivity.this);
                pd.show();
                pd.setTitle(getResources().getString(R.string.prompt));
                pd.setContent(getResources().getString(R.string.login_alert));
                pd.setleftText(getResources().getString(R.string.cancle));
                pd.setrightText(getResources().getString(R.string.confirm));
                pd.setListener(new PromptDialog.OnPromptDialogListener() {
                    @Override
                    public void leftClick(int code) {
                        pd.dismiss();
                    }

                    @Override
                    public void rightClick(int code) {
                        pd.dismiss();
                        Gson gson = new Gson();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("phone", "bozlun888@gmail.com");
                        map.put("pwd", Md5Util.Md532("e10adc3949ba59abbe56e057f20f883e"));
                        String mapjson = gson.toJson(map);
                        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, NewLoginActivity.this);
                        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.logon, mapjson);

                        SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putInt("id", 0);
                        editor.commit();

                    }
                });
                break;
            case R.id.forget_tv://忘记密码
                startActivity(new Intent(NewLoginActivity.this, ForgetPasswardActivity.class));
                break;
            case R.id.login_btn://登陆
                //登录时判断
                String pass = passwordLogon.getText().toString();
                String usernametxt = username.getText().toString();
                if (!WatchUtils.isEmpty(usernametxt) && !WatchUtils.isEmpty(pass)) {
                    loginRemote(usernametxt, pass);
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.input_name) + "/" + getResources().getString(R.string.input_password));
                }
                break;
            case R.id.register_btn://注册
                startActivity(new Intent(NewLoginActivity.this, RegisterActivity2.class));
                break;
            case R.id.xinlang_iv://新浪
                if (instance != null) {
                    isShouQuanOnClick = true;
                    showLoadingDialog(getResources().getString(R.string.dlog));
                    instance.loginShow(SinaWeibo.NAME);
                }
                break;
            case R.id.qq_iv://qq
                if (instance != null) {
                    isShouQuanOnClick = true;
                    showLoadingDialog(getResources().getString(R.string.dlog));
                    instance.loginShow(QQ.NAME);
                }
                break;
            case R.id.weixin_iv://微信
                if (instance != null) {
                    isShouQuanOnClick = true;
                    showLoadingDialog(getResources().getString(R.string.dlog));
                    instance.loginShow(Wechat.NAME);
                }
                break;
            case R.id.fecebook_longin://facebook
                if (instance != null) {
                    isShouQuanOnClick = true;
                    showLoadingDialog(getResources().getString(R.string.dlog));
                    instance.loginShow(Facebook.NAME);
                }
                break;
            case R.id.google_longin://google+
                if (instance != null) {
                    isShouQuanOnClick = true;
                    showLoadingDialog(getResources().getString(R.string.dlog));
                    instance.loginShow(GooglePlus.NAME);
                }
                break;
            case R.id.twitter_longin://twitter
//                if (instance != null) instance.loginShow(Twitter.NAME);
                break;
        }
    }


    boolean isShouQuanOnClick = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (isShouQuanOnClick)
            showLoadingDialog(getResources().getString(R.string.dlog));
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            isShouQuanOnClick = false;
            switch (message.what) {
                case 0x01:
                    closeLoadingDialog();
                    showLoadingDialog(getResources().getString(R.string.user_login) + "...");
                    //ToastUtil.showShort(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.shouquancg));
                    break;
                case 0x02:
                    //ToastUtil.showShort(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.shouquanshib));
                    closeLoadingDialog();
                    break;
                case 0x03:
                    ToastUtil.showShort(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.cancle));
                    closeLoadingDialog();
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onStop() {
        super.onStop();
        closeLoadingDialog();
    }

    private DialogSubscriber dialogSubscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener = new SubscriberOnNextListener<String>() {
        @Override
        public void onNext(String result) {
            //Loaddialog.getInstance().dissLoading();
            Log.e("LoainActivity", "-----loginresult---" + result);
            Gson gson = new Gson();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String loginResult = jsonObject.getString("resultCode");
                if ("001".equals(loginResult)) {
                    String userInfoStr = jsonObject.getString("userInfo");
                    Log.e(TAG,"------userInfoStr="+userInfoStr);
                    UserInfoBean userInfoBean = gson.fromJson(userInfoStr,UserInfoBean.class);
                    Log.e(TAG,"------userInfoBean="+userInfoBean.toString());
                    Common.customer_id = userInfoBean.getUserId();
                    //保存userid
                    SharedPreferencesUtils.saveObject(NewLoginActivity.this, Commont.USER_ID_DATA, userInfoBean.getUserId());
                    SharedPreferencesUtils.saveObject(NewLoginActivity.this, "userInfo", userInfoStr);
                    SharedPreferencesUtils.saveObject(NewLoginActivity.this,Commont.USER_INFO_DATA,userInfoStr);

                    MobclickAgent.onProfileSignIn(Common.customer_id);
                    startActivity(new Intent(NewLoginActivity.this, NewSearchActivity.class));
                    finish();
                } else if (loginResult.equals("003")) {
                    ToastUtil.showShort(NewLoginActivity.this, getString(R.string.yonghuzhej));
                } else if (loginResult.equals("006")) {
                    ToastUtil.showShort(NewLoginActivity.this, getString(R.string.miamacuo));
                } else {
                    ToastUtil.showShort(NewLoginActivity.this, getString(R.string.miamacuo));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //用户手机登录
    private void loginRemote(String uName, String uPwd) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", uName);
        map.put("pwd", Md5Util.Md532(uPwd));
        String mapjson = gson.toJson(map);
        Log.e("msg", "-mapjson-" + mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, NewLoginActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.logon, mapjson);

        SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putInt("id", 0);
        editor.commit();


    }

    JSONObject jsonObject = null;


    /**
     * 过滤特殊字符
     *
     * @param str
     * @return
     * @throws PatternSyntaxException
     */
    public static String StringFilter(String str) throws PatternSyntaxException {
        // 清除掉所有特殊字符
        String regEx = "[-`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？_♚\uD83D\uDC8E]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    @Override
    public void OnLoginListenter(Platform platform, HashMap<String, Object> hashMap) {
        //Log.e(TAG,"---------OnLoginListenter="+platform.toString()+"------="+hashMap.toString());

        if (handler != null) handler.sendEmptyMessage(0x01);
        String userId = platform.getDb().getUserId();//获取用户账号
        String userName = platform.getDb().getUserName();//获取用户名字
        String userIcon = platform.getDb().getUserIcon();//获取用户头像
        String userGender = platform.getDb().getUserGender(); //获取用户性别，m = 男, f = 女，如果微信没有设置性别,默认返回null
        upUserData(userId, userName, userIcon, userGender);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        //Log.e(TAG,"----------error="+platform.toString()+"---throw="+throwable.toString());
        if (handler != null) handler.sendEmptyMessage(0x02);
//        try {
//
//            isShouQuanOnClick = false;
//            Toast.makeText(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.shouquanshib), Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            //解决在子线程中调用Toast的异常情况处理
//            Looper.prepare();
//            Toast.makeText(MyApp.getInstance(), MyApp.getInstance().getResources().getString(R.string.shouquanshib), Toast.LENGTH_SHORT).show();
//            Looper.loop();
//        }
    }

    @Override
    public void onCancel(Platform platform, int i) {
        if (handler != null) handler.sendEmptyMessage(0x03);
    }


    /**
     * 根据返回注册账号
     *
     * @param userId
     * @param userName
     * @param userIcon
     * @param userGender
     */
    void upUserData(String userId, String userName, String userIcon, String userGender) {

        try {
            userName = StringFilter(userName);//过滤昵称字符
            if (TextUtils.isEmpty(userName)) userName = "racefitpro";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("thirdId", userId);
            jsonObject.put("nickName", userName);
            jsonObject.put("thirdType", 4);//QQ登录
            jsonObject.put("image", userIcon);//头像地址

            jsonObject.put("sex", "M");//默认为男，根据下面在判断
            //性别
            if (userGender.trim().equals("男") || userGender.trim().equalsIgnoreCase("M")) {
                jsonObject.put("sex", "M");
            } else {
                jsonObject.put("sex", "F");
            }
            //Log.d("-----register--", "用户名账号：" + userId + "  头像：" + userIcon + "用户名：" + userName + "  性别：" + userGender);
            //姓名
            JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, URLs.HTTPs + URLs.disanfang, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            closeLoadingDialog();
                            Log.e(TAG, "----QQ--resonpse=" + response.toString());
                            String shuzhu = response.optString("userInfo");
                            if (response.optString("resultCode").equals("001")) {
                                try {
                                    JSONObject jsonObject = new JSONObject(shuzhu);
                                    String userId = jsonObject.getString("userId");
                                    Gson gson = new Gson();
                                    BlueUser userInfo = gson.fromJson(shuzhu, BlueUser.class);
                                    Common.userInfo = userInfo;
                                    Common.customer_id = userId;
                                    //保存userid
                                    SharedPreferencesUtils.saveObject(NewLoginActivity.this, "userId", userInfo.getUserId());
                                    SharedPreferencesUtils.saveObject(NewLoginActivity.this, "userInfo", shuzhu);
                                    SharedPreferencesUtils.saveObject(NewLoginActivity.this,Commont.USER_INFO_DATA,jsonObject.getString("userInfo"));
                                    MobclickAgent.onProfileSignIn(Common.customer_id);
                                    //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
                                    SharedPreferences.Editor editor = userSettings.edit();
                                    editor.putInt("id", 1);
                                    editor.commit();
                                    startActivity(new Intent(NewLoginActivity.this, NewSearchActivity.class));
                                    finish();
                                } catch (Exception E) {
                                    E.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error == null)
                        return;
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    return headers;
                }
            };
            MyApp.getInstance().getRequestQueue().add(jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public long exitTime; // 储存点击退出时间

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    ToastUtil.showToast(NewLoginActivity.this, getResources().getString(R.string.string_double_out));
                    exitTime = System.currentTimeMillis();
                    return false;
                } else {
                    // 全局推出
                    //removeAllActivity();
                    MyApp.getInstance().removeALLActivity();
                    return true;
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


}
