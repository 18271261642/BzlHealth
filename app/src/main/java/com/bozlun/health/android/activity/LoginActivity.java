package com.bozlun.health.android.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.wylactivity.wyl_util.service.ConnectManages;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.bean.BlueUser;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.siswatch.view.LoginWaveView;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.util.MyLogUtil;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.util.VerifyUtil;
import com.bozlun.health.android.view.PromptDialog;
import com.bozlun.health.android.xinlangweibo.SinaUserInfo;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.permission.AndPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.google.GooglePlus;
import m.framework.utils.UIHandler;

/**
 * Created by thinkpad on 2017/3/3.
 */

public class LoginActivity extends BaseActivity implements Callback, PlatformActionListener {


    @BindView(R.id.login_visitorTv)
    TextView loginVisitorTv;
    //波浪形曲线
    @BindView(R.id.login_waveView)
    LoginWaveView loginWaveView;

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void onComplete(Platform platform, int action,
                           HashMap<String, Object> res) {
        Log.i(TAG, "onComplete执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            login(platform.getName(), platform.getDb().getUserId(), res);
        }
        Log.i(TAG, res.toString());
    }

    public void onError(Platform platform, int action, Throwable t) {
        Log.i(TAG, "onError执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }

    public void onCancel(Platform platform, int action) {
        Log.i(TAG, "onCancel执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }


    @Override
    public void hideTitleStute() {
        //隐藏状态栏，同时Activity会伸展全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void login(String plat, String userId,
                       HashMap<String, Object> userInfo) {
        Log.i(TAG, "login执行了");
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    @BindView(R.id.ll_bottom_tabaa)
    LinearLayout guolei;//在国内
    @BindView(R.id.ll_bottom_tabguowai)
    LinearLayout guiwai;//在国外

    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.username_input_logon)
    TextInputLayout usernameInput;
    @BindView(R.id.password_logon)
    EditText password;
    @BindView(R.id.textinput_password)
    TextInputLayout textinputPassword;
    @BindView(R.id.xinlang_iv)
    RelativeLayout weiboIv;
    @BindView(R.id.qq_iv)
    RelativeLayout qqIv;
    @BindView(R.id.weixin_iv)
    RelativeLayout weixinIv;
    private static final String TAG = "LoginActivity";
    private DialogSubscriber dialogSubscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener;
    //qq
    private Tencent mTencent;
    String openID;//唯一标识符 1105653402
    //     private final String APP_ID = "101357650";// 测试时使用，真正发布的时候要换成自己的APP_ID
    private final String APP_ID = "101357650";// 测试时使用，真正发布的时候要换成自己的APP_ID
    public static String mAppid;

    private IUiListener loginListener;
    // private CallbackManager callbackManager;
    JSONObject jsonObject;
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;


    private Platform platform11;

    private TextView userinfo_tv;
    private SinaUserInfo userInfo;


    private static final int REQUEST_CODE_WRITESDCARD = 10001;

    private BluetoothAdapter bluetoothAdapter;

    private IWXAPI iwxapi;

    @SuppressLint("ServiceCast")
    @Override
    protected void initViews() {
        loginWaveView.startMove();  //波浪线贝塞尔曲线
        //微信
//        api = WXAPIFactory.createWXAPI(this, "wx70148753927bd916", false);
//        api.registerApp("wx70148753927bd916");
        boolean lauage = VerifyUtil.isZh(LoginActivity.this);
        if (lauage) {
            guolei.setVisibility(View.VISIBLE);
            guiwai.setVisibility(View.GONE);
        } else {
            guolei.setVisibility(View.GONE);
            guiwai.setVisibility(View.VISIBLE);
        }

        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                //Loaddialog.getInstance().dissLoading();
                Log.e("LoainActivity", "-----loginresult---" + result);
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String loginResult = jsonObject.getString("resultCode");
                    if ("001".equals(loginResult)) {
                        BlueUser userInfo = gson.fromJson(jsonObject.getString("userInfo").toString(), BlueUser.class);
                        MyLogUtil.i("msg", "-userInfo-" + userInfo.toString());
                        Common.userInfo = userInfo;
                        Common.customer_id = userInfo.getUserId();
                        //保存userid
                        SharedPreferencesUtils.saveObject(LoginActivity.this, "userId", userInfo.getUserId());
                        SharedPreferencesUtils.saveObject(LoginActivity.this, "userInfo", jsonObject.getString("userInfo").toString());
                        MobclickAgent.onProfileSignIn(Common.customer_id);

                        String pass = password.getText().toString();
                        String usernametxt = username.getText().toString();
                        userInfo.setPassword(Md5Util.Md532(pass));

                        SharedPreferencesUtils.setParam(LoginActivity.this, SharedPreferencesUtils.CUSTOMER_ID, Common.customer_id);
                        SharedPreferencesUtils.setParam(LoginActivity.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, pass);
                        startActivity(new Intent(LoginActivity.this, NewSearchActivity.class));
                        finish();
                    } else if (loginResult.equals("003")) {
                        ToastUtil.showShort(LoginActivity.this, getString(R.string.yonghuzhej));
                    } else if (loginResult.equals("006")) {
                        ToastUtil.showShort(LoginActivity.this, getString(R.string.miamacuo));
                    } else {
                        ToastUtil.showShort(LoginActivity.this, getString(R.string.miamacuo));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

//        guiwai.setVisibility(View.VISIBLE);
//        guolei.setVisibility(View.GONE);
//        usernameInput.setHint(getResources().getString(R.string.input_email));
//        if (lauage) {
//            guolei.setVisibility(View.VISIBLE);
//            guiwai.setVisibility(View.GONE);
//            usernameInput.setHint(getResources().getString(R.string.input_name));
//        } else {
//            guiwai.setVisibility(View.VISIBLE);
//            guolei.setVisibility(View.GONE);
//            usernameInput.setHint(getResources().getString(R.string.input_email));
//        }
//        guiwai.setVisibility(View.VISIBLE);
//        guolei.setVisibility(View.GONE);
        usernameInput.setHint(getResources().getString(R.string.input_email));
        //请求读写SD卡的权限
        AndPermission.with(LoginActivity.this)
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
    protected int getContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    protected int getStatusBarColor() {
        return R.color.transparent;
    }

    @OnClick({R.id.register_btn, R.id.forget_tv, R.id.login_btn, R.id.xinlang_iv, R.id.qq_iv, R.id.weixin_iv, R.id.fecebook_longin, R.id.google_longin, R.id.twitter_longin, R.id.login_visitorTv})
    public void onClick(View view) {
        Context context = view.getContext();
        switch (view.getId()) {
            case R.id.fecebook_longin://登录fecebook
                /***** FaceBook 自定义按钮登录**/
                authorize(new Facebook(), 0);
                break;
            case R.id.google_longin://登录google
                authorize(new GooglePlus(), 1);
                break;
            case R.id.twitter_longin://登录twitter
                try {
                    QLogin("1");
                } catch (Exception E) {
                    E.printStackTrace();
                }
                break;
            case R.id.register_btn://注册
                startActivity(new Intent(LoginActivity.this, RegisterActivity2.class));
                break;
            case R.id.forget_tv://忘记密码
                startActivity(new Intent(LoginActivity.this, ForgetPasswardActivity.class));
                break;
            case R.id.login_btn:
                boolean lauage = VerifyUtil.isZh(LoginActivity.this);

                //登录时判断
                String pass = password.getText().toString();
                String usernametxt = username.getText().toString();
                if (!WatchUtils.isEmpty(usernametxt) && !WatchUtils.isEmpty(pass)) {
                    loginRemote(usernametxt, pass);
                } else {
                    ToastUtil.showToast(this, getResources().getString(R.string.input_name) + "/" + getResources().getString(R.string.input_password));
                }
                break;
            case R.id.xinlang_iv://f新浪登录
                break;
            case R.id.qq_iv://QQ登录
                //ToastUtil.showToast(LoginActivity.this,"QQ登录");
                if (mTencent == null)
                    ToastUtil.showToast(LoginActivity.this, "Tencent为null");
                if (!mTencent.isSessionValid()) {
                    mTencent.login(LoginActivity.this, "all", loginListener);
                }

                loginListener = new IUiListener() {
                    @Override
                    public void onComplete(Object o) {
                        JSONObject jsonObject = (JSONObject) o;
                        Log.e(TAG, "-----QQ登录返回----" + o.toString());
                        try {
                            String accessToken = jsonObject.getString("access_token");
                            String expires = jsonObject.getString("expires_in");
                            openID = jsonObject.getString("openid");
                            mTencent.setAccessToken(accessToken, expires);
                            mTencent.setOpenId(openID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        Log.i("=====", "===失败");
                    }

                    @Override
                    public void onCancel() {
                        Log.i("=====", "===取消");
                    }
                };
                break;
            case R.id.weixin_iv://微信登录
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";  //不能乱写
                req.state = "auth_state";
                iwxapi.sendReq(req);

                break;
            case R.id.login_visitorTv:  //游客登录
                final PromptDialog pd = new PromptDialog(LoginActivity.this);
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
                        Log.e("msg", "-mapjson-" + mapjson);
                        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, LoginActivity.this);
                        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.logon, mapjson);

                        SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
                        SharedPreferences.Editor editor = userSettings.edit();
                        editor.putInt("id", 0);
                        editor.commit();

                    }
                });
                break;
        }
    }

    private void authorize(Platform plat, final int id) {
        if (plat == null) {
            return;
        }
        //判断指定平台是否已经完成授权
        if (plat.isAuthValid()) {
            String userId = plat.getDb().getUserId();
            if (userId != null) {
                return;
            }
        }
        plat.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {//  Auto-generated method stub
                Log.i("platform", "LLLerror");
            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
                //  Auto-generated method stub
                Log.i("platform", "LLLcomplete");
                //成功得到用户信息
                String userId = arg0.getDb().getUserId();
                String userName = arg0.getDb().getUserName();
                String token = arg0.getDb().getToken();
                String userIcon = arg0.getDb().getUserIcon();
                //有效时期
                long expiresTime = arg0.getDb().getExpiresTime();
                try {
                    JSONObject shuju = new JSONObject();
                    shuju.put("thirdId", userId);
                    shuju.put("thirdType", "5");
                    shuju.put("image", userIcon);
                    if (arg0.getDb().getUserGender().equals("f")) {
                        shuju.put("sex", "F");
                    } else {
                        shuju.put("sex", "M");
                    }
                    shuju.put("nickName", userName);
                    if (id == 0) {
                        shagchuanfuwuqi(shuju);//facebook
                    } else {
                        shangchuangogle(shuju, "2");
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
            }
        });
        plat.SSOSetting(true); // true不使用SSO授权，false使用SSO授权
        plat.showUser(null);  //获取用户资料
    }


    /**
     * 登录到facekoob
     */
    public void shagchuanfuwuqi(JSONObject ASD) {
        //判断网络是否连接
        Boolean is = ConnectManages.isNetworkAvailable(LoginActivity.this);
        if (is == true) {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, URLs.HTTPs + URLs.disanfang, ASD,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
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
                                    SharedPreferencesUtils.saveObject(LoginActivity.this, "userId", userInfo.getUserId());
                                    SharedPreferencesUtils.saveObject(LoginActivity.this, "userInfo", shuzhu);
                                    MobclickAgent.onProfileSignIn(Common.customer_id);

//                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    startActivity(new Intent(LoginActivity.this, NewSearchActivity.class));
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (response.optString("resultCode").equals("003")) {
                                Toast.makeText(LoginActivity.this, R.string.yonghuzhej, Toast.LENGTH_SHORT).show();
                                return;
                            } else if (response.optString("resultCode").equals("006")) {
                                Toast.makeText(LoginActivity.this, R.string.miamacuo, Toast.LENGTH_SHORT).show();

                            }/*else{Toast.makeText(LoginActivity.this,R.string.miamacuo,Toast.LENGTH_SHORT).show();}*/
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, R.string.wangluo, Toast.LENGTH_SHORT).show();
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
            requestQueue.add(jsonRequest);
        } else {
            Toast.makeText(LoginActivity.this, R.string.wangluo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 到google
     */
    public void shangchuangogle(JSONObject ASD, final String aaa) {
        //判断网络是否连接
        Boolean is = ConnectManages.isNetworkAvailable(LoginActivity.this);
        if (is == true) {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, URLs.HTTPs + URLs.disanfang, ASD,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
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
                                    SharedPreferencesUtils.saveObject(LoginActivity.this, "userId", userInfo.getUserId());
                                    SharedPreferencesUtils.saveObject(LoginActivity.this, "userInfo", shuzhu);
                                    MobclickAgent.onProfileSignIn(Common.customer_id);

//                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    startActivity(new Intent(LoginActivity.this, NewSearchActivity.class));
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (response.optString("resultCode").equals("003")) {
                                Toast.makeText(LoginActivity.this, R.string.yonghuzhej, Toast.LENGTH_SHORT).show();
                                return;
                            } else if (response.optString("resultCode").equals("006")) {
                                Toast.makeText(LoginActivity.this, R.string.tianxie, Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.miamacuo, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, R.string.wangluo, Toast.LENGTH_SHORT).show();
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
            requestQueue.add(jsonRequest);
        } else {
            Toast.makeText(LoginActivity.this, R.string.wangluo, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加授权
     * Twitter
     */

    public void QLogin(final String AAA) {
//        platform11 = ShareSDK.getPlatform(Twitter.NAME);
        platform11.authorize();
        platform11.showUser(null);//必须要加的要不然不行！这个才是授权的！
        TelephonyManager tm = (TelephonyManager) LoginActivity.this.getSystemService(TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imi = tm.getDeviceId();
        platform11.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onError(Platform platform11, int arg1, Throwable arg2) {
                //弹出失败窗口
            }

            @Override
            public void onComplete(Platform platform11, int arg1, HashMap<String, Object> arg2) {
                //成功得到用户信息
                String userId = platform11.getDb().getUserId();
                String userName = platform11.getDb().getUserName();
                String token = platform11.getDb().getToken();
                String userIcon = platform11.getDb().getUserIcon();
                //有效时期
                long expiresTime = platform11.getDb().getExpiresTime();
                //  SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
              /*  System.out.println("用户ID为："+userId);
                System.out.println("用户名称为："+userName);
                System.out.println("token     "+token);
                System.out.println("userIcon     "+userIcon);
                System.out.println("getUserGender "+platform11.getDb().getUserGender());*/

                if (AAA.equals("1")) {
                    //twitter+上传到服务器
                    try {
                        JSONObject shuju = new JSONObject();
                        shuju.put("thirdId", userId);
                        shuju.put("thirdType", "7");
                        shuju.put("image", userIcon);
                        shuju.put("sex", "F");
                        shuju.put("nickName", userName);
                        shangchuangogle(shuju, "1");
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
            }
        });
    }


    //用户手机登录
    private void loginRemote(String uName, String uPwd) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", uName);
        map.put("pwd", Md5Util.Md532(uPwd));
        String mapjson = gson.toJson(map);
        Log.e("msg", "-mapjson-" + mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, LoginActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.logon, mapjson);

        SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putInt("id", 0);
        editor.commit();


    }


    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "----onActivityResult------" + requestCode + "---resultCode--" + resultCode);
        Log.e(TAG, "----requestCode:" + requestCode);
        Log.e(TAG, "----resultCode:" + resultCode);
        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == -1) {
                mTencent.onActivityResultData(requestCode, resultCode, data, loginListener);
                mTencent.handleResultData(data, loginListener);
                UserInfo info = new UserInfo(this, mTencent.getQQToken());
                info.getUserInfo(new IUiListener() {
                                     @Override
                                     public void onComplete(Object o) {
                                         JSONObject info = (JSONObject) o;
                                         Log.e(TAG, "----info--" + info.toString());
                                         try {
                                             String nickName = info.getString("nickname");//获取用户昵称
                                             String iconUrl = info.getString("figureurl_qq_2");//获取用户头像的url
                                             String qqxingbie = info.getString("gender");//性别

                                             try {
                                                 jsonObject = new JSONObject();
                                                 jsonObject.put("thirdId", openID);
                                                 jsonObject.put("nickName", nickName);
                                                 jsonObject.put("thirdType", 4);//QQ登录
                                                 jsonObject.put("image", iconUrl);//头像地址
                                                 //性别
                                                 if (qqxingbie.trim().equals("男")) {
                                                     jsonObject.put("sex", "M");
                                                 } else {
                                                     jsonObject.put("sex", "F");
                                                 }
                                                 //姓名
                                             } catch (JSONException e) {
                                                 e.printStackTrace();
                                             }
                                             JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, URLs.HTTPs + URLs.disanfang, jsonObject,
                                                     new Response.Listener<JSONObject>() {
                                                         @Override
                                                         public void onResponse(JSONObject response) {
                                                             Log.e(TAG, "----QQ--resonpse=" + response.toString());
                                                             String shuzhu = response.optString("userInfo").toString();
                                                             if (response.optString("resultCode").toString().equals("001")) {
                                                                 try {
                                                                     JSONObject jsonObject = new JSONObject(shuzhu);
                                                                     String userId = jsonObject.getString("userId");
                                                                     Gson gson = new Gson();
                                                                     BlueUser userInfo = gson.fromJson(shuzhu, BlueUser.class);
                                                                     Common.userInfo = userInfo;
                                                                     Common.customer_id = userId;
                                                                     //保存userid
                                                                     SharedPreferencesUtils.saveObject(LoginActivity.this, "userId", userInfo.getUserId());
                                                                     SharedPreferencesUtils.saveObject(LoginActivity.this, "userInfo", shuzhu);
                                                                     MobclickAgent.onProfileSignIn(Common.customer_id);
                                                                     //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                                     SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
                                                                     SharedPreferences.Editor editor = userSettings.edit();
                                                                     editor.putInt("id", 1);
                                                                     editor.commit();
                                                                     startActivity(new Intent(LoginActivity.this, NewSearchActivity.class));
                                                                     finish();
                                                                 } catch (Exception E) {
                                                                     E.printStackTrace();
                                                                 }
                                                             }
                                                         }
                                                     }, new Response.ErrorListener() {
                                                 @Override
                                                 public void onErrorResponse(VolleyError error) {
                                                     Log.e(TAG, "----qq-error=" + error.getMessage().toString());
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

                                         } catch (Exception e) {
                                             e.printStackTrace();
                                         }
                                     }

                                     @Override
                                     public void onError(UiError uiError) {

                                     }

                                     @Override
                                     public void onCancel() {
                                     }
                                 }
                );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // : add setContentView(...) invocation
        ButterKnife.bind(this);
        mTencent = Tencent.createInstance(APP_ID, MyApp.getContext());
        iwxapi = WXAPIFactory.createWXAPI(this, "wxd9dd17f96d73d54a", true);
        iwxapi.registerApp("wxd9dd17f96d73d54a");

    }


    /***
     * *****
     * **微信登录
     * ******
     * ***
     * ***/
    /**********************/

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //上传到服务器
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    try {
                        jsonObject = new JSONObject();
                        jsonObject.put("thirdId", userInfo.getUid());
                        jsonObject.put("thirdType", 4);//微博
                        jsonObject.put("image", userInfo.getAvatarHd());//头像地址
                        if (userInfo.getSEX().equals("m")) {
                            jsonObject.put("sex", "M");
                        } else {
                            jsonObject.put("sex", "F");
                        }//性别
                        jsonObject.put("nickName", userInfo.getName()); //姓名
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, URLs.HTTPs + URLs.disanfang, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e(TAG, "--------微博登录返回-=" + response.toString());
                                    Log.d("eeeee", response.toString());
                                    String shuzhu = response.optString("userInfo").toString();
                                    if (response.optString("resultCode").toString().equals("001")) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(shuzhu);
                                            String userId = jsonObject.getString("userId");
                                            Gson gson = new Gson();
                                            BlueUser userInfo = gson.fromJson(shuzhu, BlueUser.class);
                                            Common.userInfo = userInfo;
                                            Common.customer_id = userId;
                                            //保存userid
                                            SharedPreferencesUtils.saveObject(LoginActivity.this, "userId", userInfo.getUserId());
                                            SharedPreferencesUtils.saveObject(LoginActivity.this, "userInfo", userInfo);
                                            MobclickAgent.onProfileSignIn(Common.customer_id);
                                            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            startActivity(new Intent(LoginActivity.this, NewSearchActivity.class));
                                            SharedPreferences userSettings = getSharedPreferences("Login_id", 0);
                                            SharedPreferences.Editor editor = userSettings.edit();
                                            editor.putInt("id", 1);
                                            editor.commit();

                                            finish();
                                        } catch (Exception E) {
                                            E.printStackTrace();
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("LoginActivity", "---sina--error--" + error.getMessage());
                            Toast.makeText(LoginActivity.this, R.string.wangluo, Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Accept", "application/json");
                            headers.put("Content-Type", "application/json; charset=UTF-8");
                            return headers;
                        }
                    };
                    requestQueue.add(jsonRequest);
                    //}
//                    else{
//                        Toast.makeText(LoginActivity.this,R.string.wangluo,Toast.LENGTH_SHORT).show();}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            handler.removeCallbacksAndMessages(null);
        } catch (Exception E) {
            E.printStackTrace();
        }
        loginWaveView.stopMove();
    }

    public long exitTime; // 储存点击退出时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    ToastUtil.showToast(LoginActivity.this, "再按一次退出程序");
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
//        return super.dispatchKeyEvent(event);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    ToastUtil.showToast(LoginActivity.this, "再按一次退出程序");
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
