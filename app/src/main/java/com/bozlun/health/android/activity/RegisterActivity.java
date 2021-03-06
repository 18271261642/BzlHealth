package com.bozlun.health.android.activity;


import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bean.UserInfoBean;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.view.PrivacyActivity;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 *
 * 邮箱注册页面
 */

public class RegisterActivity extends WatchBaseActivity implements RequestView {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.register_agreement_my)
    TextView registerAgreement;
    @BindView(R.id.username_input)
    TextInputLayout usernameInput;
    @BindView(R.id.textinput_password_regster)
    TextInputLayout textinputPassword;
    @BindView(R.id.code_et_regieg)
    EditText codeEt;
    @BindView(R.id.username_regsiter)
    EditText usernameEdit;
    @BindView(R.id.password_logonregigter)
    EditText passwordEdit;
    @BindView(R.id.send_btn)
    Button sendBtn;
    @BindView(R.id.textinput_code)
    TextInputLayout textinput_code;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.login_btn_reger)
    Button loginBtnReger;


    ///user/register

    private RequestPressent requestPressent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter);
        ButterKnife.bind(this);


        initViews();
        requestPressent = new RequestPressent();
        requestPressent.attach(this);

    }

    private void initViews() {
        tvTitle.setText(R.string.user_regsiter);
        usernameInput.setHint(getResources().getString(R.string.input_email));
        sendBtn.setVisibility(View.GONE);
        textinput_code.setVisibility(View.GONE);
        toolbar.setNavigationIcon(R.mipmap.backs);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //初始化底部声明
        String INSURANCE_STATEMENT = getResources().getString(R.string.register_agreement);
        SpannableString spanStatement = new SpannableString(INSURANCE_STATEMENT);
        ClickableSpan clickStatement = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //跳转到协议页面
                startActivity(new Intent(RegisterActivity.this, PrivacyActivity.class));
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


    @OnClick({R.id.login_btn_reger, R.id.send_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn_reger:
                String eMailTxt = usernameEdit.getText().toString();
                String pwdTxt = passwordEdit.getText().toString();
                if (WatchUtils.isEmpty(eMailTxt) || WatchUtils.isEmpty(pwdTxt)) {
                    ToastUtil.showToast(RegisterActivity.this, getResources().getString(R.string.input_user));
                    return;
                }
                if(pwdTxt.length()<6){
                    ToastUtil.showToast(this,getResources().getString(R.string.not_b_less));
                    return;
                }
                registerForEmail(eMailTxt, pwdTxt); //邮箱注册
                break;
        }
    }


    //邮箱注册
    private void registerForEmail(String uName, String uPwd) {
        if (requestPressent != null) {
            Map<String, String> params = new HashMap<>();
            params.put("phone", uName);
            params.put("pwd", Md5Util.Md532(uPwd));
            params.put("status", "0");
            params.put("type", "1");
            requestPressent.getRequestJSONObject(0x01, Commont.FRIEND_BASE_URL + URLs.myHTTPs, RegisterActivity.this, new Gson().toJson(params), 1);

        }

    }


    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog("Loading...");
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        Log.e("TAG","----------obj="+object.toString());
        closeLoadingDialog();
        if (object == null)
            return;
        try {
            JSONObject jsonObject = new JSONObject(object.toString());
            if(!jsonObject.has("code"))
                return;
            if (jsonObject.getInt("code") == 200) {
                String data = jsonObject.getString("data");
                UserInfoBean userInfoBean = new Gson().fromJson(data,UserInfoBean.class);
                if(userInfoBean != null){
                    Common.customer_id = userInfoBean.getUserid();
                    MobclickAgent.onProfileSignIn(Common.customer_id);
                    SharedPreferencesUtils.saveObject(RegisterActivity.this, Commont.USER_ID_DATA, userInfoBean.getUserid());
                    //SharedPreferencesUtils.saveObject(RegisterActivity2.this, "userId", jsonObject.getJSONObject("userInfo").getString("userId"));
                    startActivity(new Intent(RegisterActivity.this, PersonDataActivity.class));
                    finish();
                }
            } else {
                ToastUtil.showToast(RegisterActivity.this, jsonObject.getString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
        ToastUtil.showToast(RegisterActivity.this, e.getMessage() + "");
    }

    @Override
    public void closeLoadDialog(int what) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestPressent != null)
            requestPressent.detach();
    }

}
