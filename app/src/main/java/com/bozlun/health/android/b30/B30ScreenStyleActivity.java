package com.bozlun.health.android.b30;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IScreenStyleListener;
import com.veepoo.protocol.model.datas.ScreenStyleData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/8/14.
 */

/**
 * 主题界面风格设置
 */
public class B30ScreenStyleActivity extends WatchBaseActivity {

    private static final String TAG = "B30ScreenStyleActivity";

    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;
    @BindView(R.id.style1Img)
    ImageView style1Img;
    @BindView(R.id.style2Img)
    ImageView style2Img;
    @BindView(R.id.style3Img)
    ImageView style3Img;
    @BindView(R.id.style33Img)
    ImageView style33Img;
    @BindView(R.id.Style3Rel)
    RelativeLayout Style3Rel;
    @BindView(R.id.style44Img)
    ImageView style44Img;
    @BindView(R.id.Style4Rel)
    RelativeLayout Style4Rel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_themestyle);
        ButterKnife.bind(this);

        initViews();

        clearImg();
        style1Img.setVisibility(View.VISIBLE);

        readStyleData();
    }

    private void readStyleData() {
        if (MyCommandManager.DEVICENAME != null) {
            MyApp.getInstance().getVpOperateManager().readScreenStyle(iBleWriteResponse, new IScreenStyleListener() {
                @Override
                public void onScreenStyleDataChange(ScreenStyleData screenStyleData) {
                    Log.e(TAG, "----screenStyleData=" + screenStyleData.toString());
                    switch (screenStyleData.getscreenStyle()) {
                        case 0:
                            clearImg();
                            style1Img.setVisibility(View.VISIBLE);
                            break;
                        case 1:
                            clearImg();
                            style2Img.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            clearImg();
                            style3Img.setVisibility(View.VISIBLE);
                            break;
                        case 3:
                            clearImg();
                            style33Img.setVisibility(View.VISIBLE);
                            break;
                        case 4:
                            clearImg();
                            style44Img.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
        }
    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.string_devices_ui));

        //B36,B31总共有5个主界面风格
        if(MyCommandManager.DEVICENAME != null){
            if(!MyCommandManager.DEVICENAME.equals("B30") || !MyCommandManager.DEVICENAME.equals("Ringmii")){
                Style3Rel.setVisibility(View.VISIBLE);
                Style4Rel.setVisibility(View.VISIBLE);
            }else{
                Style3Rel.setVisibility(View.INVISIBLE);
                Style4Rel.setVisibility(View.INVISIBLE);
            }
        }


        //B36总共有5个主界面风格
        if(WatchUtils.isB36Device(B30ScreenStyleActivity.this,"B31")){
            Style3Rel.setVisibility(View.VISIBLE);
            Style4Rel.setVisibility(View.VISIBLE);

        }else{
            Style3Rel.setVisibility(View.INVISIBLE);
            Style4Rel.setVisibility(View.INVISIBLE);
        }


    }

    @OnClick({R.id.commentB30BackImg, R.id.defaultStyleRel,
            R.id.Style1Rel, R.id.Style2Rel,R.id.Style3Rel,R.id.Style4Rel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:
                finish();
                break;
            case R.id.defaultStyleRel:
                clearImg();
                style1Img.setVisibility(View.VISIBLE);
                settingStyle(0);
                break;
            case R.id.Style1Rel:
                clearImg();
                style2Img.setVisibility(View.VISIBLE);
                settingStyle(1);
                break;
            case R.id.Style2Rel:
                clearImg();
                style3Img.setVisibility(View.VISIBLE);
                settingStyle(2);
                break;
            case R.id.Style3Rel:    //B36的风格
                clearImg();
                style33Img.setVisibility(View.VISIBLE);
                settingStyle(3);
                break;
            case R.id.Style4Rel:    //B36的风格
                clearImg();
                style44Img.setVisibility(View.VISIBLE);
                settingStyle(4);
                break;
        }
    }

    private void settingStyle(int styleId) {
        if (MyCommandManager.DEVICENAME != null) {
            MyApp.getInstance().getVpOperateManager().settingScreenStyle(iBleWriteResponse, new IScreenStyleListener() {
                @Override
                public void onScreenStyleDataChange(ScreenStyleData screenStyleData) {

                }
            }, styleId);
        }
    }

    private void clearImg() {
        style1Img.setVisibility(View.INVISIBLE);
        style2Img.setVisibility(View.INVISIBLE);
        style3Img.setVisibility(View.INVISIBLE);

        style33Img.setVisibility(View.INVISIBLE);
        style44Img.setVisibility(View.INVISIBLE);
    }

    private IBleWriteResponse iBleWriteResponse = new IBleWriteResponse() {
        @Override
        public void onResponse(int i) {
            Log.d("---------", i + "");
        }
    };
}
