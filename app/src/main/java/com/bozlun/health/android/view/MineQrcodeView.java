package com.bozlun.health.android.view;


import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bean.UserInfoBean;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.suchengkeji.android.w30sblelibrary.utils.W30SBleUtils;
import com.yzq.zxinglibrary.encode.CodeCreator;

/**
 * 我的二维码
 * Created by Admin
 * Date 2019/3/22
 */
public class MineQrcodeView extends Dialog {

    ImageView qrImg;
    private Context mContext;

    private String userAccount = null;

    public MineQrcodeView(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine_qrcode_layout);


        initViews();

//        String userId = (String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA);
//        if(WatchUtils.isEmpty(userId))
//            return;

        String userStr = (String) SharedPreferencesUtils.readObject(mContext,Commont.USER_INFO_DATA);
        Log.e("-","------iserStr="+userStr);
        if(userStr == null)
            return;
        UserInfoBean userInfoBean = new Gson().fromJson(userStr,UserInfoBean.class);
        if(userInfoBean == null)
            return;
        userAccount = userInfoBean.getPhone();


        Bitmap bitmap = null;
        try {
            /*
             * contentEtString：字符串内容
             * w：图片的宽
             * h：图片的高
             * logo：不需要logo的话直接传 0
             * */
            Bitmap logo = BitmapFactory.decodeResource((mContext==null?MyApp.getContext():mContext).getResources(), 0);
            bitmap = CodeCreator.createQRCode(userAccount, 400, 400, logo);
            qrImg.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


    }

    private void initViews() {
        qrImg = findViewById(R.id.qrCodeImg);

    }


}
