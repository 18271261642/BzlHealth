package com.bozlun.health.android.friend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.R;
import com.bozlun.health.android.friend.bean.MyFrendListBean;
import com.bozlun.health.android.friend.bean.NewFrendApplyBean;
import com.bozlun.health.android.friend.bean.TodayRankBean;
import com.bozlun.health.android.friend.mutilbind.FrendAdapter;
import com.bozlun.health.android.friend.mutilbind.TodayRankAdapter;
import com.bozlun.health.android.siswatch.WatchBaseActivity;
import com.bozlun.health.android.util.RecycleViewDivider;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import com.bozlun.health.android.util.URLs;
import com.bozlun.health.android.w30s.utils.httputils.RequestPressent;
import com.bozlun.health.android.w30s.utils.httputils.RequestView;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @aboutContent:
 * @author： An
 * @crateTime: 2018/3/9 18:07
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class FriendActivity
        extends WatchBaseActivity
        implements RequestView, TabLayout.OnTabSelectedListener, FrendAdapter.OnItemListenter {
    @BindView(R.id.toolbar_normal)
    Toolbar mNormalToolbar;
    @BindView(R.id.recycler_frend)
    RecyclerView recyclerViewFrend;
    @BindView(R.id.recycler_unfrend)
    RecyclerView recyclerViewUnFrend;
    @BindView(R.id.m_tablayout)
    TabLayout mTabLayout;
    //    @BindView(R.id.un_frend_smartrefresh)
//    SmartRefreshLayout un_frend_smartrefresh;
    private int pageNumber = 0;//记录当前页码

    private RequestPressent requestPressent;
    String userId = "";

    //好友
    List<MyFrendListBean.MyfriendsBean> myfriendsList;
    FrendAdapter frendAdapter;

    //排行榜
    TodayRankAdapter todayRankAdapter;
    List<TodayRankBean.RankListBean> leaderList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fredens);
        ButterKnife.bind(this);
        inEdit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userId = (String) SharedPreferencesUtils.readObject(this, "userId");
        if (pageNumber == 0) {
            recyclerViewFrend.setVisibility(View.VISIBLE);
//            un_frend_smartrefresh.setVisibility(View.GONE);
            recyclerViewUnFrend.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(userId)) {
                showLoadingDialog(getResources().getString(R.string.dlog));
                findFrendList(userId);
                findNewApplyFrend(userId);
            }
        } else {
            recyclerViewFrend.setVisibility(View.GONE);
//            un_frend_smartrefresh.setVisibility(View.VISIBLE);
            recyclerViewUnFrend.setVisibility(View.VISIBLE);
            showLoadingDialog(getResources().getString(R.string.dlog));
            findUnFrendList();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageNumber = 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inEdit() {
        requestPressent = new RequestPressent();
        requestPressent.attach(this);

        mTabLayout.setVisibility(View.VISIBLE);
        mTabLayout.addTab(mTabLayout.newTab().setText(getResources().getString(R.string.string_frend)));//好友
        mTabLayout.addTab(mTabLayout.newTab().setText(getResources().getString(R.string.string_world)));//世界
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.addOnTabSelectedListener(this);

        //替换三个点
        mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.image_add));
        setSupportActionBar(mNormalToolbar);

        //LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(FriendActivity.this,R.anim.layout_animation_fall_down);

        recyclerViewFrend.setLayoutManager(new GridLayoutManager(this, 1));
        //分割线
        recyclerViewFrend.addItemDecoration(new RecycleViewDivider(
                this, LinearLayoutManager.VERTICAL, 8, Color.parseColor("#D9D9D9")));
        //recyclerViewFrend.setLayoutAnimation(layoutAnimationController);
        myfriendsList = new ArrayList<>();
        frendAdapter = new FrendAdapter(FriendActivity.this, myfriendsList);
        recyclerViewFrend.setAdapter(frendAdapter);


        //非好友列表
        recyclerViewUnFrend.setLayoutManager(new GridLayoutManager(this, 1));
        //分割线
        recyclerViewUnFrend.addItemDecoration(new RecycleViewDivider(
                this, LinearLayoutManager.VERTICAL, 1, Color.parseColor("#D9D9D9")));
       // recyclerViewUnFrend.setLayoutAnimation(layoutAnimationController);
        leaderList = new ArrayList<>();
        todayRankAdapter = new TodayRankAdapter(FriendActivity.this, leaderList);
        recyclerViewUnFrend.setAdapter(todayRankAdapter);

    }


    /**
     * 让菜单同时显示图标和文字
     *
     * @param featureId
     * @param menu
     * @return
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.frend_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_new_frend) {//添加好友
            startActivity(new Intent(this, AddNewFriendActivity.class));
            return true;
        }
        if (id == R.id.action_new_frend_apply) {//好友申请
            startActivity(new Intent(this, FriendApplyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /************************************************/
/**
 *
 * 可以重复添加好友,添加之后删除一条后,再删除相同的返回013
 * 添加好友返回成功，但是好友列表没有数据
 * 不过自己搜索添加自己能成功，而且删除提示013
 */

    /**
     * 查找好友列表
     *
     * @param userId
     */
    public void findFrendList(String userId) {
        String sleepUrl = URLs.HTTPs + Commont.Findlist;
        JSONObject sleepJson = new JSONObject();
        try {
            sleepJson.put("userId", userId);

            Log.d("-----------朋友--", "获取好友列表参数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x01, sleepUrl, FriendActivity.this, sleepJson.toString(), 0);
        }
    }

    /**
     * 删除好友
     *
     * @param userId
     */
    public void deleteFrenditem(String userId, String applicant) {
        String sleepUrl = URLs.HTTPs + Commont.DeleteFrendItem;
        JSONObject sleepJson = new JSONObject();
        try {
            sleepJson.put("userId", userId);
            sleepJson.put("applicant", applicant);

            Log.d("-----------朋友--", " 删除好友获取参数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x03, sleepUrl, FriendActivity.this, sleepJson.toString(), 0);
        }
    }

    /**
     * 给好友点赞 （好友之间一天只能点赞一次，不能取消赞）
     *
     * @param userId
     * @param applicant 被赞人
     */
    public void awesomeFrenditem(String userId, String applicant) {
        String sleepUrl = URLs.HTTPs + Commont.FrendAwesome;
        JSONObject sleepJson = new JSONObject();
        try {
            sleepJson.put("userId", userId);
            sleepJson.put("applicant", applicant);
            Log.d("-----------朋友--", " 给好友点赞获取参数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x04, sleepUrl, FriendActivity.this, sleepJson.toString(), 0);
        }
    }

    /**
     * 查找非好友列表
     */
    public void findUnFrendList() {
        String sleepUrl = URLs.HTTPs + Commont.TodayRank;
        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x02, sleepUrl, FriendActivity.this, "", 0);
        }
    }


    /**
     * 查找朋友的新申请
     *
     * @param userId
     */
    public void findNewApplyFrend(String userId) {
        String sleepUrl = URLs.HTTPs + Commont.FindNewFrend;
        JSONObject sleepJson = new JSONObject();
        try {
            sleepJson.put("userId", userId);
            Log.d("-----------朋友--", " 查找新申请的朋友参数--" + sleepJson.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (requestPressent != null) {
            requestPressent.getRequestJSONObject(0x05, sleepUrl, this, sleepJson.toString(), 0);
        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case 0x01:
                        Log.d("----------获取朋友列表返回--", message.obj.toString());
                        MyFrendListBean myFrendListBean = new Gson().fromJson(message.obj.toString(), MyFrendListBean.class);
                        if (myFrendListBean != null) {
                            if (myFrendListBean.getResultCode().equals("001")) {
                                List<MyFrendListBean.MyfriendsBean> myfriends = myFrendListBean.getMyfriends();


                                MyFrendListBean.MyInfoBean myInfo = myFrendListBean.getMyInfo();

                                if (myfriends != null && !myfriends.isEmpty() && myInfo != null) {


                                    //我的数据添加到头
                                    MyFrendListBean.MyfriendsBean myfriendsBean = new MyFrendListBean.MyfriendsBean();
                                    myfriendsBean.setBirthday(myInfo.getBirthday());
                                    myfriendsBean.setImage(myInfo.getImage());
                                    myfriendsBean.setTodayThumbs(myInfo.getTodayThumbs());//被赞次数
                                    myfriendsBean.setNickName(myInfo.getNickName());
                                    myfriendsBean.setSex(myInfo.getSex());
                                    myfriendsBean.setWeight(myInfo.getWeight());
                                    myfriendsBean.setEquipment(myInfo.getEquipment());
                                    myfriendsBean.setUserId(myInfo.getUserId());
                                    myfriendsBean.setPhone(myInfo.getPhone());
                                    myfriendsBean.setStepNumber(myInfo.getStepNumber());
                                    myfriendsBean.setHeight(myInfo.getHeight());

                                    myfriends.add(0, myfriendsBean);


//                                Collections.sort(myfriends, new Comparator<MyFrendListBean.MyfriendsBean>() {
//                                    @Override
//                                    public int compare(MyFrendListBean.MyfriendsBean o1, MyFrendListBean.MyfriendsBean o2) {
//                                        // 返回值为int类型，大于0表示正序，小于0表示逆序
////                                        return o2-o1;
//                                        return o2.getStepNumber() - o1.getStepNumber();
//                                    }
//                                });


                                    myfriendsList.clear();
                                    myfriendsList.addAll(myfriends);
                                    frendAdapter.notifyDataSetChanged();
//                                    FrendAdapter frendAdapter = new FrendAdapter(FriendActivity.this, myfriends);
//                                    recyclerViewFrend.setAdapter(frendAdapter);
//                                    frendAdapter.setmOnItemListenter(FriendActivity.this);
                                   // frendAdapter.notifyDataSetChanged();
                                   // runLayoutAnimation(recyclerViewFrend);
                                }
                            }
                        }

                        break;
                    case 0x02:
                        TodayRankBean todayRankBean = new Gson().fromJson(message.obj.toString(), TodayRankBean.class);
                        if (todayRankBean != null) {
                            if (todayRankBean.getResultCode().equals("001")) {
                                List<TodayRankBean.RankListBean> rankList = todayRankBean.getRankList();
                               // TodayRankAdapter todayRankAdapter = new TodayRankAdapter(FriendActivity.this, rankList);
                                //recyclerViewUnFrend.setAdapter(todayRankAdapter);
                                //todayRankAdapter.notifyDataSetChanged();
                                leaderList.clear();
                                leaderList.addAll(rankList);
                                //todayRankAdapter.notifyDataSetChanged();
                                runLayoutAnimation(recyclerViewUnFrend);
                            }
                        }
                        break;
                    case 0x03:
                        Log.d("----------删除朋友返回--", message.obj.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(message.obj.toString());
                            if (jsonObject.has("resultCode")) {
                                String resultCode1 = jsonObject.getString("resultCode");
                                boolean b = Commont.ReturnCode(resultCode1);
                                if (b) {
                                    Log.d("-----------朋友--", "删除成功--重新获取列表");
                                    userId = (String) SharedPreferencesUtils.readObject(FriendActivity.this, "userId");
                                    if (!TextUtils.isEmpty(userId)) {
                                        findFrendList(userId);
                                        findNewApplyFrend(userId);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x04:
                        Log.d("----------给朋友点赞返回--", message.obj.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(message.obj.toString());
                            if (jsonObject.has("resultCode")) {
                                String resultCode1 = jsonObject.getString("resultCode");
                                boolean b = Commont.ReturnCode(resultCode1);
                                if (b) {
                                    Log.d("-----------朋友--", "点赞成功--重新获取列表");
                                    userId = (String) SharedPreferencesUtils.readObject(FriendActivity.this, "userId");
                                    if (!TextUtils.isEmpty(userId)) {
                                        findFrendList(userId);
                                        findNewApplyFrend(userId);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x05:
                        Log.d("----------新申请朋友列表返回--", message.obj.toString());
                        NewFrendApplyBean newFrendApplyBean = new Gson().fromJson(message.obj.toString(), NewFrendApplyBean.class);
                        if (newFrendApplyBean != null && newFrendApplyBean.getResultCode().equals("001")) {
                            List<NewFrendApplyBean.ApplyListBean> applyList = newFrendApplyBean.getApplyList();
                            if (applyList != null && !applyList.isEmpty()) {
                                //替换三个点
                                mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_new_add_frend));
                                setSupportActionBar(mNormalToolbar);
                            } else {
                                //替换三个点
                                mNormalToolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.image_add));
                                setSupportActionBar(mNormalToolbar);
                            }

                        }
                        break;
                }
            } catch (Error error) {
            }

            return false;
        }
    });

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    @Override
    public void showLoadDialog(int what) {
        showLoadingDialog(getResources().getString(R.string.dlog));
    }

    @Override
    public void successData(int what, Object object, int daystag) {
        closeLoadingDialog();
        if (object == null || TextUtils.isEmpty(object.toString().trim()) || object.toString().contains("<html>"))
            return;
        Log.d("-----------朋友--", object.toString());
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
     * TabLayout 改变监听
     *
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pageNumber = tab.getPosition();
        userId = (String) SharedPreferencesUtils.readObject(this, "userId");
        if (pageNumber == 0) {
            recyclerViewFrend.setVisibility(View.VISIBLE);
//            un_frend_smartrefresh.setVisibility(View.GONE);
            recyclerViewUnFrend.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(userId)) {
                findFrendList(userId);
            }
        } else {
            recyclerViewFrend.setVisibility(View.GONE);
//            un_frend_smartrefresh.setVisibility(View.VISIBLE);
            recyclerViewUnFrend.setVisibility(View.VISIBLE);
            findUnFrendList();
        }
        if (!TextUtils.isEmpty(userId)) findNewApplyFrend(userId);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    /**
     * 好友列表点击事件
     *
     * @param view
     * @param applicant   朋友UserID
     * @param stepNumber  步数
     * @param frendHeight 身高
     */
    @Override
    public void ItemOnClick(View view, String applicant, int stepNumber, String frendHeight, int postion) {
        //去朋友数据界面
        startActivity(FrendDataActivity.class, new String[]{"applicant", "stepNumber", "frendHeight"},
                new String[]{applicant, stepNumber + "", frendHeight});
    }

    /**
     * 点击我的条目--- 去咱我的人的界面
     *
     * @param postion
     */
    @Override
    public void ItemOnClickMine(int postion) {
        if (postion == 0){
            startActivity(FrendLoveMineActivity.class);
        }
    }


    /**
     * 好友点赞
     *
     * @param view
     */
    @Override
    public void ItemLoveOnClick(View view, String applicant) {
        userId = (String) SharedPreferencesUtils.readObject(FriendActivity.this, "userId");
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(applicant)) {
            awesomeFrenditem(userId, applicant);
        }
    }


    /**
     * 好友列表长按事件-------删除好友
     *
     * @param view
     */
    @Override
    public void ItemOnLongClick(View view, final String applicant) {

        userId = (String) SharedPreferencesUtils.readObject(FriendActivity.this, "userId");
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(applicant)) {
            deleteFrenditem(userId, applicant);
        }
    }

}
