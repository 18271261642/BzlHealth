package com.bozlun.health.android.friend;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bozlun.health.android.Commont;
import com.bozlun.health.android.R;
import com.bozlun.health.android.friend.bean.LoveMeBean;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.adapters.CommonRecyclerAdapter;
import com.bozlun.health.android.w30s.adapters.MyViewHolder;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FrendLoveMineActivity
        extends WatchBaseActivity implements RequestView {

    @BindView(R.id.bar_titles)
    TextView barTitles;
    @BindView(R.id.toolbar_normal)
    Toolbar toolbarNormal;
    @BindView(R.id.rec_list_loveme)
    RecyclerView recListLoveme;
    private RequestPressent requestPressent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frend_loveme_activity);
        ButterKnife.bind(this);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoveMe();
    }

    private void init() {
        requestPressent = new RequestPressent();
        requestPressent.attach(this);
        barTitles.setText(getResources().getString(R.string.string_love_mine_frend));
        toolbarNormal.setNavigationIcon(getResources().getDrawable(R.mipmap.backs));//设置返回按钮
        toolbarNormal.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });//右边返回按钮点击事件


        recListLoveme.setLayoutManager(new GridLayoutManager(this, 3));
        //分割线
//        recyclerViewFrend.addItemDecoration(new RecycleViewDivider(
//                this, LinearLayoutManager.VERTICAL, 8, Color.parseColor("#D9D9D9")));
    }

    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog(getResources().getString(R.string.dlog));
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        closeLoadingDialog();
        if (object == null || TextUtils.isEmpty(object.toString().trim())) return;
        Log.d("-----------赞我的--", object.toString());
        Message message = new Message();
        message.what = what;
        message.obj = object;
        if (handler != null) handler.sendMessage(message);
    }

    @Override
    public void failedData(int what, Throwable e) {
        closeLoadingDialog();
    }

    @Override
    public void closeLoadDialog(int what) {
        closeLoadingDialog();
    }


    /**
     * 返回今日已赞我的好友
     */
    void getLoveMe() {
        String sleepUrl = URLs.HTTPs + Commont.TodayLoveMe;
        JSONObject sleepJson = new JSONObject();
        try {
            String userId = (String) SharedPreferencesUtils.readObject(this, "userId");
            if (!TextUtils.isEmpty(userId)) sleepJson.put("userId", userId);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x01, sleepUrl, FrendLoveMineActivity.this, sleepJson.toString(), 0);
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0x01:
                    LoveMeBean loveMeBean = new Gson().fromJson(message.obj.toString(), LoveMeBean.class);
                    if (loveMeBean.getResultCode().equals("001")) {
                        List<LoveMeBean.FriendListBean> friendList = loveMeBean.getFriendList();
                        if (friendList != null)
                            recListLoveme.setAdapter(new MyAdapter(FrendLoveMineActivity.this, friendList,
                                    R.layout.loveme_item));
                    }

//                    FrendAdapter frendAdapter = new FrendAdapter(FriendActivity.this, myfriends);
//                    recListLoveme.setAdapter(frendAdapter);

                    break;
            }
            return false;
        }
    });


    /**
     * rec---适配器
     */
    class MyAdapter extends CommonRecyclerAdapter<LoveMeBean.FriendListBean> {

        public MyAdapter(Context context, List<LoveMeBean.FriendListBean> data, int layoutId) {
            super(context, data, layoutId);
        }

        @Override
        public void convert(MyViewHolder holder, final LoveMeBean.FriendListBean item) {
            holder.setImageGlidNo(R.id.image_user, FrendLoveMineActivity.this);
            if (!WatchUtils.isEmpty(item.getImage())) {
                holder.setImageGlid(R.id.image_user, item.getImage(), FrendLoveMineActivity.this);
            }
            if (!WatchUtils.isEmpty(item.getNickName()))
                holder.setText(R.id.text_names, item.getNickName());

//            holder.setText(R.id.itemHeartDetailDateTv, item.getRtc().substring(11, 16));
//            holder.setText(R.id.itemHeartDetailValueTv, item.getHeartRate() + "");
        }
    }


}
