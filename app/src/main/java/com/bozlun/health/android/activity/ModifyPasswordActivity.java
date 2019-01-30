package com.bozlun.health.android.activity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.R;
import com.bozlun.health.android.base.BaseActivity;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.DialogSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.bozlun.health.android.util.Common;
import com.bozlun.health.android.util.Md5Util;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.google.gson.Gson;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import butterknife.BindView;
import static com.bozlun.health.android.util.Common.userInfo;

/**
 * Created by thinkpad on 2017/3/9.
 * 修改密码
 */

public class ModifyPasswordActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.old_password)
    EditText oldPassword;
    @BindView(R.id.new_password)
    EditText newPassword;
    @BindView(R.id.confrim_password)
    EditText confrimPassword;

    private DialogSubscriber dialogSubscriber;
    private SubscriberOnNextListener<String> subscriberOnNextListener;

    @Override
    protected void initViews() {
        tvTitle.setText(R.string.modify_password);
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String resultCode = jsonObject.getString("resultCode");
                    if ("001".equals(resultCode)) {
                        String confrimPass = confrimPassword.getText().toString();
                        userInfo.setPassword(Md5Util.Md532(confrimPass));
//                        MyApp.getApplication().getDaoSession().getBlueUserDao().insertOrReplace(userInfo);
                        ToastUtil.showShort(ModifyPasswordActivity.this, getString(R.string.modify_success));
                        finish();
                    } else {
                        ToastUtil.showShort(ModifyPasswordActivity.this, getString(R.string.submit_fail));
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
        String oldPass = oldPassword.getText().toString();
        String newPass = newPassword.getText().toString();
        String confrimPass = confrimPassword.getText().toString();
        if(WatchUtils.isEmpty(oldPass)){
            ToastUtil.showShort(this, getString(R.string.input_old_password));
        }else if(WatchUtils.isEmpty(newPass) || WatchUtils.isEmpty(confrimPass)){
            ToastUtil.showShort(this, getString(R.string.input_old_password));
        }else if(newPass.length()<6 || confrimPass.length() < 6){
            ToastUtil.showToast(this,"新密码长度小于6位!");
        }else if(!newPass.equals(confrimPass)){
            ToastUtil.showToast(this,getResources().getString(R.string.string_two_passwords_are_different));
        }else{
            modifyPersonData(oldPass, confrimPass);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_modify_password;
    }

    private void modifyPersonData(String oldePwd, String newPwd) {
        Gson gson = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", SharedPreferencesUtils.readObject(this,Commont.USER_ID_DATA));
        map.put("oldePwd", Md5Util.Md532(oldePwd));
        map.put("newPwd", Md5Util.Md532(newPwd));
        String mapjson = gson.toJson(map);
        dialogSubscriber = new DialogSubscriber(subscriberOnNextListener, ModifyPasswordActivity.this);
        OkHttpObservable.getInstance().getData(dialogSubscriber, URLs.HTTPs + URLs.xiugaipassword, mapjson);
    }

}
