package com.bozlun.health.android.siswatch.mine;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.R;
import com.bozlun.health.android.activity.MyPersonalActivity;
import com.bozlun.health.android.b30.B30DeviceActivity;
import com.bozlun.health.android.b30.B30SysSettingActivity;
import com.bozlun.health.android.b31.B31DeviceActivity;
import com.bozlun.health.android.bleutil.MyCommandManager;
import com.bozlun.health.android.friend.FriendActivity;
import com.bozlun.health.android.net.OkHttpObservable;
import com.bozlun.health.android.rxandroid.CommonSubscriber;
import com.bozlun.health.android.rxandroid.SubscriberOnNextListener;
import com.bozlun.health.android.siswatch.LazyFragment;
import com.bozlun.health.android.siswatch.NewSearchActivity;
import com.bozlun.health.android.siswatch.WatchDeviceActivity;
import com.bozlun.health.android.siswatch.utils.UpdateManager;
import com.bozlun.health.android.siswatch.utils.WatchUtils;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.ToastUtil;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.xinlangweibo.SinaUserInfo;
import org.json.JSONException;
import org.json.JSONObject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * Created by Administrator on 2017/7/17.
 */

/**
 * sis watch 我的fragmet
 */
public class WatchMineFragment extends LazyFragment {

    View watchMineView;
    Unbinder unbinder;
    //用户昵称
    @BindView(R.id.watch_mine_uname)
    TextView watchMineUname;
    //头像
    @BindView(R.id.watch_mine_userheadImg)
    ImageView watchMineUserheadImg;
    //总公里数
    @BindView(R.id.watch_distanceTv)
    TextView watchDistanceTv;
    //日平均步数
    @BindView(R.id.watch_mine_avageStepsTv)
    TextView watchMineAvageStepsTv;
    //达标天数
    @BindView(R.id.watch_mine_dabiaoTv)
    TextView watchMineDabiaoTv;

    //显示蓝牙名字和地址
    @BindView(R.id.showBleNameTv)
    TextView showBleNameTv;

    private SinaUserInfo userInfo;


    private CommonSubscriber commonSubscriber, commonSubscriber2;
    private SubscriberOnNextListener subscriberOnNextListener, subscriberOnNextListener2;

    private String bleName = null;

    //更新
    private UpdateManager updateManager;
    String userId = "9278cc399ab147d0ad3ef164ca156bf0";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bleName = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        watchMineView = inflater.inflate(R.layout.fragment_watch_mine, container, false);
        unbinder = ButterKnife.bind(this, watchMineView);

        initViews();

        initData();

        getMyInfoData();    //获取我的总数


        return watchMineView;
    }

    private void initViews() {
        if (bleName == null)
            return;
        if (MyCommandManager.DEVICENAME == null) {
            showBleNameTv.setText("未连接");
            return;
        }

        String bleMac = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLEMAC);
        if (!WatchUtils.isEmpty(bleMac) && bleName.equals("bozlun")) {
            showBleNameTv.setText("H8" + " " + bleMac);
        } else {
            showBleNameTv.setText(bleName + " " + bleMac);
        }


    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        super.onFragmentVisibleChange(isVisible);
        if (getActivity() == null || getActivity().isFinishing())
            return;
        if (isVisible) {
            updateManager = new UpdateManager(getActivity(), URLs.HTTPs + URLs.bozlun_health_url);
            updateManager.checkForUpdate(true);
        }

    }

    /**
     * 获取我的总数
     */
    private void getMyInfoData() {
        String myInfoUrl = URLs.HTTPs + URLs.myInfo;
        JSONObject js = new JSONObject();
        try {
            js.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
            js.put("deviceCode", SharedPreferencesUtils.readObject(getActivity(), Commont.BLEMAC));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        commonSubscriber = new CommonSubscriber(subscriberOnNextListener, getActivity());
        OkHttpObservable.getInstance().getData(commonSubscriber, myInfoUrl, js.toString());

    }

    @Override
    public void onResume() {
        super.onResume();


        getUserInfoData();  //获取用户信息

    }

    //获取用户信息
    private void getUserInfoData() {
        String url = URLs.HTTPs + URLs.getUserInfo; //查询用户信息
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("userId", SharedPreferencesUtils.readObject(getActivity(), "userId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        commonSubscriber2 = new CommonSubscriber(subscriberOnNextListener2, getActivity());
        OkHttpObservable.getInstance().getData(commonSubscriber2, url, jsonObj.toString());
    }

    private void initData() {
        SharedPreferences share = getActivity().getSharedPreferences("nickName", 0);
        String name = share.getString("name", "");
        if (!WatchUtils.isEmpty(name)) {
            watchMineUname.setText(name + "");
        }
        //数据返回
        subscriberOnNextListener = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) { //{"myInfo":{"distance":48.3,"count":2,"stepNumber":1582},"resultCode":"001"}
                // Log.e("mine", "------result----" + result);
                if (!WatchUtils.isEmpty(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getInt("resultCode") == 001) {
                            JSONObject myInfoJsonObject = jsonObject.getJSONObject("myInfo");
                            if (myInfoJsonObject != null) {
                                String distances = myInfoJsonObject.getString("distance");
                                if (WatchUtils.judgeUnit(MyApp.getContext())) {
                                    //总公里数
                                    watchDistanceTv.setText(WatchUtils.resrevedDeci(distances.trim()) + "km");
                                } else {
                                    watchDistanceTv.setText(WatchUtils.doubleunitToImperial(Double.valueOf(distances), 0) + "mi");
                                }

                                String counts = myInfoJsonObject.getString("count");
                                if (!WatchUtils.isEmpty(myInfoJsonObject.getString("count"))) {
                                    //达标天数
                                    watchMineDabiaoTv.setText("" + myInfoJsonObject.getString("count") + getResources().getString(R.string.data_report_day));
                                }
                                String stepNums = myInfoJsonObject.getString("stepNumber");
                                if (!WatchUtils.isEmpty(stepNums)) {
                                    //平均步数
                                    watchMineAvageStepsTv.setText("" + myInfoJsonObject.getString("stepNumber") + getResources().getString(R.string.daily_numberofsteps_default));
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        //我的数据返回
        subscriberOnNextListener2 = new SubscriberOnNextListener<String>() {
            @Override
            public void onNext(String result) {
                // Log.e("mine", "-----个人信息-----" + result);
                if (!WatchUtils.isEmpty(result)) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getString("resultCode").equals("001")) {
                            JSONObject myInfoJsonObject = jsonObject.getJSONObject("userInfo");
                            if (myInfoJsonObject != null) {
                                watchMineUname.setText("" + myInfoJsonObject.getString("nickName") + "");
                                String imgHead = myInfoJsonObject.getString("image");
                                if (!WatchUtils.isEmpty(imgHead)) {
                                    //头像

                                    RequestOptions mRequestOptions = RequestOptions.circleCropTransform().diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true);

//                                    Glide.with(getActivity()).load(myInfoJsonObject.getString("image"))
//                                            .bitmapTransform(new CropCircleTransformation(getActivity())).placeholder(R.mipmap.ic_default_himg).into(watchMineUserheadImg);    //头像


                                    Glide.with(getActivity()).load(myInfoJsonObject.getString("image"))
                                            .apply(mRequestOptions).into(watchMineUserheadImg);    //头像
                                }
                                userId = myInfoJsonObject.getString("userId");
                                String userHeight = myInfoJsonObject.getString("height");
                                if (userHeight != null) {
                                    if (userHeight.contains("cm")) {
                                        String newHeight = userHeight.substring(0, userHeight.length() - 2);
                                        SharedPreferencesUtils.setParam(getActivity(), "userheight", newHeight.trim());
                                    } else {
                                        SharedPreferencesUtils.setParam(getActivity(), "userheight", userHeight.trim());
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (updateManager != null)
            updateManager.destoryUpdateBroad();
    }


    @OnClick({R.id.watchMinepersonalData, R.id.watchMineDevice,
            R.id.watchmineSetting, R.id.watch_mine_userheadImg,
            R.id.card_frend,R.id.mineNotiMsgCardView})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.watch_mine_userheadImg://用户头像点击
            case R.id.watchMinepersonalData:    //个人资料
                startActivity(new Intent(getActivity(), MyPersonalActivity.class));
                break;
            case R.id.watchMineDevice:  //我的设备
                if (MyCommandManager.DEVICENAME != null) {
                    if (MyCommandManager.DEVICENAME.equals("bozlun")) {
                        startActivity(new Intent(getActivity(), WatchDeviceActivity.class));
                    } else if (MyCommandManager.DEVICENAME.equals("B30") || MyCommandManager.DEVICENAME.equals("B36")
                            || MyCommandManager.DEVICENAME.equals("Ringmii")) {    //B30
                        startActivity(new Intent(getActivity(), B30DeviceActivity.class));
                    } else if (MyCommandManager.DEVICENAME.equals("B31")
                            || MyCommandManager.DEVICENAME.equals("B31S")
                            || MyCommandManager.DEVICENAME.equals("500S")) {    //B31
                        startActivity(new Intent(getActivity(), B31DeviceActivity.class));
                    }


                } else {
                    String bleName = (String) SharedPreferencesUtils.readObject(MyApp.getContext(), Commont.BLENAME);
                    if (!WatchUtils.isEmpty(bleName) && bleName.equals("bozlun")) {
                        SharedPreferencesUtils.saveObject(MyApp.getContext(), Commont.BLEMAC, "");
                        if (MyApp.getInstance().h8BleManagerInstance().getH8BleService() != null) {
                            MyApp.getInstance().h8BleManagerInstance().getH8BleService().autoConnByMac(false);
                        }
                    }
                    startActivity(new Intent(getActivity(), NewSearchActivity.class));
                    if (getActivity() != null)
                        getActivity().finish();
                }

                break;
            case R.id.watchmineSetting:  //系统设置
                startActivity(new Intent(getActivity(), B30SysSettingActivity.class));
                break;
            case R.id.card_frend://亲情互动
                if (!WatchUtils.isEmpty(userId) && !userId.equals("9278cc399ab147d0ad3ef164ca156bf0")) {
                    startActivity(new Intent(getActivity(), FriendActivity.class));
                } else {
                    ToastUtil.showShort(MyApp.getInstance(), getString(R.string.noright));
                }

                break;
            case R.id.mineNotiMsgCardView:  //通知中心
                startActivity(new Intent(getActivity(),NotiMsgFragment.class));
                break;
        }
    }

}
