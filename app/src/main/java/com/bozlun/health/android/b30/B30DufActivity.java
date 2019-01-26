package com.bozlun.health.android.b30;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bozlun.health.android.R;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.util.ToastUtil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class B30DufActivity extends WatchBaseActivity {


    @BindView(R.id.commentB30BackImg)
    ImageView commentB30BackImg;
    @BindView(R.id.commentB30TitleTv)
    TextView commentB30TitleTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30_dfu);
        ButterKnife.bind(this);

        initViews();

    }

    private void initViews() {
        commentB30BackImg.setVisibility(View.VISIBLE);
        commentB30TitleTv.setText(getResources().getString(R.string.firmware_upgrade));
    }

    @OnClick({R.id.commentB30BackImg, R.id.b30DufBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commentB30BackImg:
                finish();
                break;
            case R.id.b30DufBtn:
                ToastUtil.showShort(B30DufActivity.this,getResources().getString(R.string.latest_version));
                break;
        }
    }
}
