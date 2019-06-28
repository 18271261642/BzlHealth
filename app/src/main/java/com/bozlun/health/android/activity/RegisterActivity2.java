package com.bozlun.health.android.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.PhoneAdapter;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.bean.AreCodeBean;
import com.bozlun.health.android.bean.BlueUser;
import com.bozlun.health.android.bean.CodeBean;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.util.MyLogUtil;
import com.bozlun.health.android.util.NetUtils;
import com.bozlun.health.android.view.PhoneAreaCodeView;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.view.PrivacyActivity;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.smssdk.SMSSDK;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by thinkpad on 2017/3/4.
 * 注册页面
 */

public class RegisterActivity2 extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_phone_head)
    TextView tv_phone_head;
    @BindView(R.id.lv_register)
    ListView lv_register;
    @BindView(R.id.register_agreement_my)
    TextView registerAgreement;
    @BindView(R.id.username_input)
    TextInputLayout usernameInput;
    @BindView(R.id.textinput_password_regster)
    TextInputLayout textinputPassword;
    @BindView(R.id.code_et_regieg)
    EditText codeEt;
    @BindView(R.id.username_regsiter)
    EditText username;
    @BindView(R.id.password_logonregigter)
    EditText password;
    @BindView(R.id.send_btn)
    Button sendBtn;
    @BindView(R.id.textinput_code)
    TextInputLayout textinput_code;
    private DialogSubscriber dialogSubscriber, dialogSubscriber2;
    private Subscriber subscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener, subscriberOnNextListener2;
    private String phoneTxt, pwdText;


    //倒计时
    MyCountDownTimerUtils countTimeUtils;
    Gson gson = new Gson();

    private PhoneAreaCodeView phoneAreaCodeView;


    @Override
    protected void initViews() {
        tvTitle.setText(R.string.user_regsiter);
        usernameInput.setHint(getResources().getString(R.string.input_name));

        tv_phone_head.setText("+86");
        codeEt.setHintTextColor(getResources().getColor(R.color.white));
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                Log.e("RegisterActivity", "------11---注册返回----" + result);
                //Loaddialog.getInstance().dissLoading();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String loginResult = jsonObject.getString("resultCode");
                    if ("001".equals(loginResult)) {
                        BlueUser userInfo = gson.fromJson(jsonObject.getString("userInfo").toString(), BlueUser.class);
                        MyLogUtil.i("msg", "-userInfo-" + userInfo.toString());
                        Common.userInfo = userInfo;
                        Common.customer_id = userInfo.getUserId();
                        MobclickAgent.onProfileSignIn(Common.customer_id);
                        String pass = password.getText().toString();
                        SharedPreferencesUtils.saveObject(RegisterActivity2.this, "userId", jsonObject.getJSONObject("userInfo").getString("userId"));
                        SharedPreferencesUtils.setParam(RegisterActivity2.this, SharedPreferencesUtils.CUSTOMER_ID, Common.customer_id);
                        SharedPreferencesUtils.setParam(RegisterActivity2.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, pass);
                        startActivity(new Intent(RegisterActivity2.this, PersonDataActivity.class));
                        finish();
                    } else {
                        WatchUtils.verServerCode(RegisterActivity2.this, loginResult);
                    }
//                    else if ("003".equals(loginResult)) {
//                        ToastUtil.showShort(RegisterActivity2.this, getString(R.string.yonghuzhej));
//                    } else {
//                        ToastUtil.showShort(RegisterActivity2.this, getString(R.string.regsiter_fail));
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


        countTimeUtils = new MyCountDownTimerUtils(60 * 1000, 1000);


        subscriberOnNextListener2 = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String s) {
                Log.e("---222", "----12-" + s);
                if (WatchUtils.isEmpty(s))
                    return;
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String resCoce = jsonObject.getString("resultCode");
                    if (!WatchUtils.isEmpty(resCoce))
                        WatchUtils.verServerCode(RegisterActivity2.this, resCoce);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };


        //初始化底部声明
        String INSURANCE_STATEMENT = getResources().getString(R.string.register_agreement);
        SpannableString spanStatement = new SpannableString(INSURANCE_STATEMENT);
        ClickableSpan clickStatement = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //跳转到协议页面
                startActivity(new Intent(RegisterActivity2.this, PrivacyActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        spanStatement.setSpan(clickStatement, 0, INSURANCE_STATEMENT.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanStatement.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0,
                INSURANCE_STATEMENT.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        registerAgreement.setText(R.string.agree_agreement);
        registerAgreement.append(spanStatement);
        registerAgreement.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_regsiter2;
    }


    @OnClick({R.id.login_btn_reger, R.id.send_btn, R.id.login_btn_emil_reger, R.id.tv_phone_head})
    public void onClick(View view) {
        phoneTxt = username.getText().toString().trim();
        switch (view.getId()) {
            case R.id.login_btn_emil_reger://跳转到邮箱注册
                startActivity(new Intent(RegisterActivity2.this, RegisterActivity.class));
                break;

            case R.id.tv_phone_head:    //选择区号
                choosePhoneArea();
                break;
            case R.id.send_btn:
                //手机号
                final String phoneNum = username.getText().toString().trim();
                //国标码
                final String pCode = tv_phone_head.getText().toString().trim();
                if (WatchUtils.isEmpty(phoneNum)) {
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.format_is_wrong));
                    return;
                }
                if (!NetUtils.isNetworkAvailable(this)) {
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.wangluo));
                    return;
                }
                //snedPhoneNumToServer(phoneNum.trim(),pCode);
                if (countTimeUtils == null)
                    countTimeUtils = new MyCountDownTimerUtils(60 * 1000, 1000);

                countTimeUtils.start();
                snedPhoneNumToServer(phoneNum, pCode);

                break;
            case R.id.login_btn_reger:
                String verCode = codeEt.getText().toString().trim();
                String pwdTxt = password.getText().toString().trim();
                if (WatchUtils.isEmpty(verCode)) {
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.string_code_null));
                    return;
                }
                if (WatchUtils.isEmpty(pwdTxt)) {
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.input_password));
                    return;
                }

                registerRemote(verCode, pwdTxt);
        }
    }

    //选择区号
    private void choosePhoneArea() {
        phoneAreaCodeView = new PhoneAreaCodeView(RegisterActivity2.this);
        phoneAreaCodeView.show();
        phoneAreaCodeView.setPhoneAreaClickListener(new PhoneAreaCodeView.PhoneAreaClickListener() {
            @Override
            public void chooseAreaCode(AreCodeBean areCodeBean) {
                phoneAreaCodeView.dismiss();
                tv_phone_head.setText("+"+areCodeBean.getPhoneCode());
            }
        });
    }

    /**
     * @param number 手机号
     * @param pCode  国标码
     */
    private void snedPhoneNumToServer(String number, String pCode) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", number);
        map.put("code", StringUtils.substringAfter(pCode, "+"));
        String mapjson = gson.toJson(map);
        Log.e("msg", "-mapjson-" + mapjson);
        dialogSubscriber2 = new DialogSubscriber(subscriberOnNextListener2, RegisterActivity2.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber2, URLs.HTTPs + URLs.GET_PHONE_VERCODE_URL, mapjson);
    }


    private void registerRemote(String verCode, String pwd) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", phoneTxt);
        map.put("pwd", Md5Util.Md532(pwd));
        map.put("code", verCode);
        map.put("status", "0");
        map.put("type", "0");
        String mapjson = gson.toJson(map);
        Log.e("msg", "-mapjson-" + mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, RegisterActivity2.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.myHTTPs, mapjson);
    }


    private class MyCountDownTimerUtils extends CountDownTimer {


        public MyCountDownTimerUtils(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendBtn.setClickable(false);
            sendBtn.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            sendBtn.setClickable(true);
            sendBtn.setText(getResources().getString(R.string.send_code));
        }
    }


}
