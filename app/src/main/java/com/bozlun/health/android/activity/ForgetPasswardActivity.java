package com.bozlun.health.android.activity;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.bozlun.health.android.R;
import com.bozlun.health.android.adpter.PhoneAdapter;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.util.NetUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * Created by admin on 2017/4/21.
 * 忘记密码
 */

public class ForgetPasswardActivity extends BaseActivity implements RequestView {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_phone_head)
    TextView tv_phone_head;
    @BindView(R.id.lv_forget)
    ListView lv_forget;
    @BindView(R.id.username_forget)
    EditText username;//用户名
    @BindView(R.id.password_forget)
    EditText password;//密码
    @BindView(R.id.send_btn_forget)
    Button sendBtn;//发送按钮
    @BindView(R.id.code_et_forget)
    EditText yuanzhengma;//验证码
    @BindView(R.id.username_input_forget)
    TextInputLayout textInputLayoutname;
    @BindView(R.id.forget_pwd_user_text)
    TextView forget_pwd_user_text;
    @BindView(R.id.forget_pwd_email_text)
    TextView forget_pwd_email_text;
    @BindView(R.id.forget_pwd_user_line)
    View forget_pwd_user_line;
    @BindView(R.id.forget_pwd_email_line)
    View forget_pwd_email_line;


    private DialogSubscriber dialogSubscriber;
    private Subscriber subscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener;



    private RequestPressent requestPressent;
    MyCountDownTimerUtils countDownTimerUtils;

    //判断是手机用户或者邮箱用户
    private boolean isPhone = true;


    /**
     * true_邮箱找回 false_手机找回
     */
    private boolean isEmail;
    /**
     * 蓝和黑
     */
    private int colorBlue, colorBlack;
    /**
     * 用户名左边图片(邮箱用)
     */
    private Drawable leftDrawable;

    private List<Integer> phoneHeadList;
    private PhoneAdapter phoneAdapter ;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //moveTaskToBack(true);
            finish();

        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            moveTaskToBack(true);
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            moveTaskToBack(true);
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void initViews() {
        tvTitle.setText(R.string.forget_password);
        initData();

        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                //Loaddialog.getInstance().dissLoading();
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String loginResult = jsonObject.getString("resultCode");
                    if ("001".equals(loginResult)) {
                        ToastUtil.showToast(ForgetPasswardActivity.this,getResources().getString(R.string.change_password));
                  /*      BlueUser userInfo = gson.fromJson(jsonObject.getString("userInfo").toString(), BlueUser.class);
                        MyLogUtil.i("msg", "-userInfo-" + userInfo.toString());
                        B18iCommon.userInfo = userInfo;
                        B18iCommon.customer_id = userInfo.getUserId();
                        MobclickAgent.onProfileSignIn(B18iCommon.customer_id);
                        String pass = password.getText().toString();
                        String usernametxt = username.getText().toString();
                        userInfo.setPassword(Md5Util.Md532(pass));


                        MyApp.getApplication().getDaoSession().getBlueUserDao().insertOrReplace(userInfo);
                        SharedPreferencesUtils.setParam(ForgetPasswardActivity.this, SharedPreferencesUtils.CUSTOMER_ID, B18iCommon.customer_id);
                        SharedPreferencesUtils.setParam(ForgetPasswardActivity.this, SharedPreferencesUtils.CUSTOMER_PASSWORD, pass);*/
                        finish();
                    }else{
                        WatchUtils.verServerCode(ForgetPasswardActivity.this,loginResult);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void initData() {
        colorBlue = ContextCompat.getColor(this, R.color.new_colorAccent);
        colorBlack = ContextCompat.getColor(this, R.color.black_9);
        leftDrawable = getResources().getDrawable(R.mipmap.yonghuming_dianji);
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

        phoneAdapter = new PhoneAdapter(phoneHeadList,this);

        lv_forget.setAdapter(phoneAdapter);

        lv_forget.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_phone_head.setText("+"+phoneHeadList.get(position));
                lv_forget.setVisibility(View.INVISIBLE);
            }
        });
    }

    @OnClick({R.id.login_btn__forget, R.id.send_btn_forget, R.id.forget_pwd_user, R.id.forget_pwd_email
    ,R.id.tv_phone_head})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_btn_forget:      //发送验证码
                String inputData = username.getText().toString().trim();
                if(WatchUtils.isEmpty(inputData)){
                    ToastUtil.showShort(ForgetPasswardActivity.this, getResources().getString(R.string.input_email));
                    return;
                }
                if(!NetUtils.isNetworkAvailable(this)){
                    ToastUtil.showShort(ForgetPasswardActivity.this,getResources().getString(R.string.wangluo));
                    return;
                }
                if(countDownTimerUtils == null)
                    countDownTimerUtils = new MyCountDownTimerUtils(60 * 1000,1000);
                if(isPhone){    //是手机用户
                    countDownTimerUtils.start();
                    sendPhoneCode(inputData,tv_phone_head.getText().toString().trim());
                }else{      //是邮箱用户
                    String emailUrl = URLs.HTTPs + URLs.sendEmail;
                    if(!isEmail(inputData)){
                        ToastUtil.showShort(ForgetPasswardActivity.this, getResources().getString(R.string.tianxie));
                        return;
                    }

                    countDownTimerUtils.onFinish();
                    countDownTimerUtils.start();
                    sendEmailVerCode(inputData,emailUrl);

                }

                break;
            case R.id.login_btn__forget:        //提交
                String uName = username.getText().toString().trim();    //手机号或者邮箱
                String uPwd = password.getText().toString().trim();     //密码
                String uVCode = yuanzhengma.getText().toString().trim();    //验证码
                if(isPhone){    //手机号
                    if (WatchUtils.isEmpty(uName)) {    //账号为空
                        ToastUtil.showToast(ForgetPasswardActivity.this, getResources().getString(R.string.input_email));
                        return;
                    }
                    if(WatchUtils.isEmpty(uPwd)){   //密码为空
                        ToastUtil.showToast(ForgetPasswardActivity.this,
                                getResources().getString(R.string.input_password));
                        return;
                    }
                    if(uPwd.length() < 6){  //密码长度小于6位
                        ToastUtil.showShort(ForgetPasswardActivity.this,
                                getResources().getString(R.string.not_b_less));
                        return;
                    }
                    if(WatchUtils.isEmpty(uVCode)){ //验证码为空
                        ToastUtil.showShort(ForgetPasswardActivity.this,
                                getResources().getString(R.string.yonghuzdffhej));
                        return;
                    }
                    //提交
                    if(requestPressent != null){
                        String subUrl = URLs.HTTPs + URLs.SUB_GET_BACK_PWD_URL;
                        Map<String,String> maps = new HashMap<>();
                        maps.put("phone",uName);
                        maps.put("code",uVCode);
                        maps.put("pwd",Md5Util.Md532(uPwd));
                        requestPressent.getRequestJSONObject(2,subUrl,
                                ForgetPasswardActivity.this,new Gson().toJson(maps),2);
                    }

                }else{  //邮箱
                    if(WatchUtils.isEmpty(uName)){ //邮箱为空
                        ToastUtil.showToast(ForgetPasswardActivity.this,getResources().getString(R.string.input_email));
                        return;
                    }
//                    if (!VerifyUtil.checkEmail(uName)) {   //邮箱格式错误
//                        ToastUtil.showShort(this, getResources().getString(R.string.mailbox_format_error));
//                        return;
//                    }
                    if (WatchUtils.isEmpty(uPwd)){      //密码为空
                        ToastUtil.showShort(this, getResources().getString(R.string.input_password));
                        return;
                    }
                    if (uPwd.length() < 6){     //密码长度小于6位
                        ToastUtil.showShort(this, getResources().getString(R.string.not_b_less));
                        return;
                    }
                    if (WatchUtils.isEmpty(uVCode)) {   //验证码位空
                        ToastUtil.showShort(this, getResources().getString(R.string.input_code));
                        return;
                    }
                    //提交信息
                    registerRemote(uName,uPwd,uVCode);
                }
                break;
            case R.id.tv_phone_head:
                lv_forget.setVisibility(View.VISIBLE);
                break;
            case R.id.forget_pwd_user:  //手机
                isPhone = true;
                changeModel(false);
                break;
            case R.id.forget_pwd_email: //邮箱
                isPhone = false;
                changeModel(true);
                break;
        }
    }


    //发送邮箱验证码
    private void sendEmailVerCode(String inputData, String emailUrl) {
        if(requestPressent != null){
            Map<String,String> mps = new HashMap<>();
            mps.put("phone",inputData);
            requestPressent.getRequestJSONObject(3,emailUrl,
                    ForgetPasswardActivity.this,new Gson().toJson(mps),3);
        }
    }

    //发送手机号码
    private void sendPhoneCode(String phoneNum, String pCode) {
        if(requestPressent != null){
            String url = URLs.HTTPs + URLs.GET_BACK_PWD_PHOE_CODE_URL;
            Map<String,String> mps = new HashMap<>();
            mps.put("phone",phoneNum);
            mps.put("code",StringUtils.substringAfter(pCode,"+"));
            requestPressent.getRequestJSONObject(1,url,ForgetPasswardActivity.this,new Gson().toJson(mps),1);
        }

    }

    /**
     * 切换找回方式
     * @param email true_邮箱找回 false_手机找回
     */
    private void changeModel(boolean email) {
        if (email == isEmail) return;
        forget_pwd_user_text.setTextColor(email ? colorBlack : colorBlue);
        forget_pwd_email_text.setTextColor(email ? colorBlue : colorBlack);
        forget_pwd_user_line.setVisibility(email ? View.GONE : View.VISIBLE);
        forget_pwd_email_line.setVisibility(email ? View.VISIBLE : View.GONE);
        tv_phone_head.setVisibility(email ? View.GONE : View.VISIBLE);
        username.setCompoundDrawablesWithIntrinsicBounds(email ? leftDrawable : null, null, null, null);
        if (email) {
            textInputLayoutname.setHint(getResources().getString(R.string.input_email));
            lv_forget.setVisibility(View.INVISIBLE);
        } else {
            textInputLayoutname.setHint(getResources().getString(R.string.input_name));
        }
        isEmail = email;
    }


    /*
 * 是否email
 */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    //提交邮箱修改密码信息
    private void registerRemote(String uName,String uPwd,String uCode) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("phone", uName);
        map.put("pwd", Md5Util.Md532(uPwd));
        map.put("code", uCode);
        String mapjson = gson.toJson(map);
        Log.i("msg","-mapjson-"+mapjson);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, ForgetPasswardActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.xiugaimima, mapjson);
        /*Intent intent = new Intent(ForgetPasswardActivity.this, MainActivity.class);
        startActivity(intent);*/
    }


    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mymobiles) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        String telRegex = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mymobiles)) return false;
        else return mymobiles.matches(telRegex);
    }


    @Override
    protected int getContentViewId() {
        return R.layout.activity_forgetpassward;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  handler.removeCallbacksAndMessages(null);
        if(requestPressent != null)
            requestPressent.detach();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
    }

    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog("loading...");
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        closeLoadingDialog();
        if(what == 1){  //发送手机验证码
            try {
                JSONObject jsonObject = new JSONObject((String)object);
                String resultCOde = jsonObject.getString("resultCode");
                WatchUtils.verServerCode(ForgetPasswardActivity.this,resultCOde.trim());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(what == 2){     //找回密码提交成功返回
            try {
                JSONObject jsoO = new JSONObject((String)object);
                String resultCode = jsoO.getString("resultCode");
                if(resultCode.equals("001")){   //提交成功
                    ToastUtil.showToast(ForgetPasswardActivity.this, getResources().getString(R.string.change_password));
                    finish();
                }else{
                    WatchUtils.verServerCode(ForgetPasswardActivity.this,resultCode.trim());
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        else if(what == 3){     //发送邮箱验证码
            try {
                JSONObject js = new JSONObject((String)object);
                String resultCode = js.getString("resultCode");
                WatchUtils.verServerCode(ForgetPasswardActivity.this,resultCode);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }


    private class MyCountDownTimerUtils extends CountDownTimer {


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

    private void clearCountTime(){
        if(countDownTimerUtils != null){
            countDownTimerUtils.cancel();
        }
    }



}
