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
    private DialogSubscriber dialogSubscriber,dialogSubscriber2;
    private Subscriber subscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener,subscriberOnNextListener2;
    private String phoneTxt, pwdText;

    private List<Integer>phoneHeadList;
    private PhoneAdapter phoneAdapter ;

    //倒计时
    MyCountDownTimerUtils countTimeUtils;
    Gson gson = new Gson();


    @Override
    protected void initViews() {
        tvTitle.setText(R.string.user_regsiter);
        phoneHeadList = new ArrayList<>();
        phoneHeadList.add(86);
        phoneHeadList.add(852);
        phoneHeadList.add(853);
        phoneHeadList.add(886);
        phoneHeadList.add(81);
        phoneHeadList.add(82);
        phoneHeadList.add(84);
        phoneHeadList.add(49);
        phoneHeadList.add(39);
        phoneHeadList.add(7);
        phoneHeadList.add(34);
        phoneHeadList.add(351);

        phoneAdapter = new PhoneAdapter(phoneHeadList,RegisterActivity2.this);

        lv_register.setAdapter(phoneAdapter);

        lv_register.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_phone_head.setText("+"+phoneHeadList.get(position));
                lv_register.setVisibility(View.GONE);
            }
        });

//        boolean lauage= VerifyUtil.isZh(RegisterActivity2.this);
//        if(lauage){
            usernameInput.setHint(getResources().getString(R.string.input_name));
//        }else{
//            usernameInput.setHint(getResources().getString(R.string.input_email));
//            sendBtn.setVisibility(View.GONE);
//            textinput_code.setVisibility(View.GONE);
//        }

        codeEt.setHintTextColor(getResources().getColor(R.color.white));
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                Log.e("RegisterActivity","------11---注册返回----"+result);
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
                        String usernametxt = username.getText().toString();
                        userInfo.setPassword(Md5Util.Md532(pass));

                        SharedPreferencesUtils.saveObject(RegisterActivity2.this,"userId",jsonObject.getJSONObject("userInfo").getString("userId"));
                        MyApp.getInstance().getDaoSession().getBlueUserDao().insertOrReplace(userInfo);
                        SharedPreferencesUtils.setParam(RegisterActivity2.this, SharedPreferencesUtils.CUSTOMER_ID, Common.customer_id);
                        SharedPreferencesUtils.setParam(RegisterActivity2.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, pass);
                        startActivity(new Intent(RegisterActivity2.this, PersonDataActivity.class));
                        finish();
                    }else{
                        WatchUtils.verServerCode(RegisterActivity2.this,loginResult);
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


        countTimeUtils = new MyCountDownTimerUtils(60 * 1000,1000);


        subscriberOnNextListener2 = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String s) {
                Log.e("---222","----12-"+s);
                if(WatchUtils.isEmpty(s))
                    return;
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String resCoce = jsonObject.getString("resultCode");
                    if(!WatchUtils.isEmpty(resCoce))
                        WatchUtils.verServerCode(RegisterActivity2.this,resCoce);
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

    private void initTime() {
        final int countTime = 60;
        sendBtn.setText(getResources().getString(R.string.resend) + "(" + countTime + "s)");
        sendBtn.setClickable(false);
        subscriber = new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                if (integer == 0) {
                    //isTime = false;
                    sendBtn.setText(getResources().getString(R.string.resend));
                    sendBtn.setClickable(true);
                } else {
                    sendBtn.setText(getResources().getString(R.string.resend)+"(" + integer + "s)");
                }
            }
        };
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime + 1)
                .subscribe(subscriber);
    }

    @OnClick({R.id.login_btn_reger, R.id.send_btn,R.id.login_btn_emil_reger,R.id.tv_phone_head})
    public void onClick(View view) {
        phoneTxt = username.getText().toString().trim();
        switch (view.getId()) {
            case R.id.login_btn_emil_reger://跳转到邮箱注册
            startActivity(new Intent(RegisterActivity2.this,RegisterActivity.class));
            break;

            case R.id.tv_phone_head:
                lv_register.setVisibility(View.VISIBLE);
                break;

            case R.id.send_btn:
                //手机号
                final String phoneNum = username.getText().toString().trim();
                //国标码
                final String pCode = tv_phone_head.getText().toString().trim();
                if(WatchUtils.isEmpty(phoneNum)){
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.format_is_wrong));
                    return;
                }
                if(!NetUtils.isNetworkAvailable(this)){
                    ToastUtil.showShort(RegisterActivity2.this,getResources().getString(R.string.wangluo));
                    return;
                }
                //snedPhoneNumToServer(phoneNum.trim(),pCode);
                if(countTimeUtils == null)
                    countTimeUtils = new MyCountDownTimerUtils(60 * 1000,1000);

                countTimeUtils.start();
                snedPhoneNumToServer(phoneNum,pCode);

//                if(Common.isFastClick()){
//
//                    if (WatchUtils.isEmpty(phoneTxt)) {
//                        ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.format_is_wrong));
//                    } else {
//                        initTime();
//                        SMSSDK.getVerificationCode(tv_phone_head.getText()+"", phoneTxt);
//                    }
//
//                    EventHandler eventHandler = new EventHandler() {
//                        @Override
//                        public void afterEvent(int event, int result, Object data) {
//                            Message msg = new Message();
//                            msg.arg1 = event;
//                            msg.arg2 = result;
//                            msg.obj = data;
//                            handler.sendMessage(msg);
//                        }
//                    };
//                    SMSSDK.registerEventHandler(eventHandler);
//
//                }
                break;
            case R.id.login_btn_reger:
                String verCode = codeEt.getText().toString().trim();
                String pwdTxt = password.getText().toString().trim();
                if(WatchUtils.isEmpty(verCode)){
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.string_code_null));
                    return;
                }
                if(WatchUtils.isEmpty(pwdTxt)){
                    ToastUtil.showShort(RegisterActivity2.this, getResources().getString(R.string.input_password));
                    return;
                }

                registerRemote(verCode,pwdTxt);
        }
    }

    /**
     *
     * @param number 手机号
     * @param pCode 国标码
     */
    private void snedPhoneNumToServer(String number,String pCode) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", number);
        map.put("code", StringUtils.substringAfter(pCode,"+"));
        String mapjson = gson.toJson(map);
        Log.e("msg","-mapjson-"+mapjson);
        dialogSubscriber2 = new DialogSubscriber(subscriberOnNextListener2, RegisterActivity2.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber2, URLs.HTTPs + URLs.GET_PHONE_VERCODE_URL, mapjson);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            if (result == SMSSDK.RESULT_COMPLETE) {
                // 短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                    //registerRemote();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    Toast.makeText(getApplicationContext(), R.string.yanzhengma,
                            Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {// 返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), R.string.guojia,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                String s = data.toString().trim().substring(20, data.toString().trim().length());
                if (TextUtils.isEmpty(s)) return;
                CodeBean codeBean = new Gson().fromJson(s, CodeBean.class);
                if (codeBean != null) {
                    int status = codeBean.getStatus();
                    if (status == 603) {//手机号错
                        ToastUtil.showLong(RegisterActivity2.this, getResources().getString(R.string.string_phone_er));
                    } else if (status == 468) {//验证码错
                        ToastUtil.showLong(RegisterActivity2.this, getResources().getString(R.string.yonghuzdffhej));
                    } else {
                        ToastUtil.showLong(RegisterActivity2.this, data.toString());
                    }
//
                }
//                ToastUtil.showLong(RegisterActivity2.this, getResources().getString(R.string.yonghuzdffhej));
//                ToastUtil.showLong(RegisterActivity2.this, data.toString());
            }
        }
    };

    private void registerRemote(String verCode,String pwd) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", phoneTxt);
        map.put("pwd", Md5Util.Md532(pwd));
        map.put("code",verCode);
        map.put("status", "0");
        map.put("type", "0");
        String mapjson = gson.toJson(map);
        Log.e("msg","-mapjson-"+mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, RegisterActivity2.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.myHTTPs, mapjson);
    }


    private class MyCountDownTimerUtils extends CountDownTimer{


        public MyCountDownTimerUtils(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendBtn.setClickable(false);
            sendBtn.setText(millisUntilFinished/1000+"s");
        }

        @Override
        public void onFinish() {
            sendBtn.setClickable(true);
            sendBtn.setText(getResources().getString(R.string.send_code));
        }
    }



}
