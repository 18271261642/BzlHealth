package com.bozlun.health.android.activity;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

/**
 * Created by thinkpad on 2017/3/9.
 * 意见反馈
 */

public class FeedbackActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.question_detail_tv)
    TextView questionDetailTv;
    @BindView(R.id.password_feed)
    EditText password;
    @BindView(R.id.contact_info_tv)
    TextView contactInfoTv;
    @BindView(R.id.leave_phone_et)
    EditText leavePhoneEt;
    private DialogSubscriber dialogSubscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener;

    @Override
    protected void initViews() {
        tvTitle.setText(R.string.feedback);
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String resultCode = jsonObject.getString("resultCode");
                    if ("001".equals(resultCode)) {
                        ToastUtil.showShort(FeedbackActivity.this, getString(R.string.submit_success));
                        finish();
                        closeKeyboard();
                    } else {
//                        ToastUtil.showShort(FeedbackActivity.this, getString(R.string.submit_fail));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String describe_problem = password.getText().toString();
        String confrimPass = leavePhoneEt.getText().toString();
        if(!WatchUtils.isEmpty(describe_problem) && !WatchUtils.isEmpty(confrimPass)){
            submitFeekback(describe_problem, confrimPass);
        }else{
            ToastUtil.showShort(this, getString(R.string.leave_contact));
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isMobile(String number) {
        String num = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return number.matches(num);
    }

    public boolean isEmail(String strEmail) {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher matcher = pattern.matcher(strEmail);
        return matcher.matches();
    }

    private void submitFeekback(String content, String contact) {
        try {
            Gson gson = new Gson();
            HashMap<String, Object> map = new HashMap<>();
            String userId = (String) SharedPreferencesUtils.getParam(MyApp.getContext(), "userId","");
            map.put("userId", userId);
            map.put("content", content);
            map.put("contact", contact);
            String mapjson = gson.toJson(map);
            dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, FeedbackActivity.this);
            OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.yijian, mapjson);
        }catch (Error error){
        }
    }


    @Override
    protected int getContentViewId() {
        return R.layout.activity_feedback;
    }


    @Override
    protected void onPause() {
        super.onPause();
        closeKeyboard();
    }

    /**
     * 关闭软键盘
     */
    public void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        hideKeyboard();

        if (getCurrentFocus() != null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && this.getCurrentFocus() != null) {
            if (this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


}
