package com.bozlun.health.android.siswatch.run;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bozlun.health.android.R;
import com.bozlun.health.android.b30.B30BaseFragment;
import com.bozlun.health.android.b30.GPSSportHisyory;

import java.util.ArrayList;
import java.util.List;

public class B30RunFragmentNew extends B30BaseFragment implements
        TabLayout.OnTabSelectedListener, View.OnClickListener {

    private TabLayout mTabLayout = null;
    private int pages = 0;
    private ViewPagerSlide viewPagerSlide = null;
    private ImageView imageHis;

    /**
     * tab 选中
     *
     * @param tab
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pages = tab.getPosition();
        if (pages == 0) {
            imageHis.setVisibility(View.VISIBLE);
        } else {
            imageHis.setVisibility(View.GONE);
        }
//        setFragment(pages);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        super.onFragmentVisibleChange(isVisible);

    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_b30_run_layout, container, false);
    }

    @Override
    protected void initView(View root) {
        View GpsRun = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_new_run_layout, null, false);
        initGpsRunView(GpsRun);
        View DeviceRun = LayoutInflater.from(getActivity()).inflate(R.layout.b30_devices_fragment, null, false);
        initDeviceRunView(DeviceRun);
        if (imageHis == null) imageHis = root.findViewById(R.id.watch_run_sportHistoryTitleTv);
        imageHis.setOnClickListener(this);
        if (viewPagerSlide == null) viewPagerSlide = root.findViewById(R.id.run_view_pager);
        if (mTabLayout == null) mTabLayout = (TabLayout) root.findViewById(R.id.mTabLayout);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        mTabLayout.addTab(mTabLayout.newTab().setText(getResources().getString(R.string.string_gps_run)));//"GPS运动"
//        mTabLayout.addTab(mTabLayout.newTab().setText(getResources().getString(R.string.string_devices_run)));//"手环运动"
        mTabLayout.addOnTabSelectedListener(this);


        mTabLayout.setupWithViewPager(viewPagerSlide);


        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(W30sNewRunFragment.newInstance());
        fragmentList.add(B30DevicesFragment.newInstance());
        viewPagerSlide.setAdapter(new RunVPAdapter(getActivity().getSupportFragmentManager(), fragmentList));
    }

    /**
     * 手环运动
     *
     * @param deviceRun
     */
    private void initDeviceRunView(View deviceRun) {

    }

    /**
     * GPS运动
     *
     * @param gpsRun
     */
    private void initGpsRunView(View gpsRun) {

    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void lazyLoad() {
    }

    /**
     * 历史纪录图片点击
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        startActivity(new Intent(getActivity(), GPSSportHisyory.class));
    }


//    使用这种方式 getActivity() 偶尔为空会崩溃
//    /**
//     * Fragment切换
//     */
//    /**********     xml布局是，加载fragment的必须是 FrameLayout布局，并且上面不能有所遮挡，负责会不显示    ***********/
//    private W30sNewRunFragment w30sNewRunFragment = null;//GPS运动界面
//    private B30DevicesFragment b30DevicesFragment = null;//手环运动界面
//
//    //设置Fragment
//    private void setFragment(int dex) {
//        try {
//            //开启一个事务
//            FragmentTransaction beginTransaction = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                beginTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
//            }
//            if (beginTransaction == null) return;
//            if (w30sNewRunFragment != null) {
//                beginTransaction.hide(w30sNewRunFragment);
//            }
//            if (b30DevicesFragment != null) {
//                beginTransaction.hide(b30DevicesFragment);
//                b30DevicesFragment = null;
//            }
//
//            switch (dex) {
//                case 0:
//                    if (w30sNewRunFragment == null) {
////                    w30sNewRunFragment = W30sNewRunFragment.newInstance();
//                        w30sNewRunFragment = new W30sNewRunFragment();
//                        //加入事物
//                        beginTransaction.add(R.id.w30_run_content, w30sNewRunFragment);
//                    } else {
//                        //否则就显示
//                        beginTransaction.show(w30sNewRunFragment);
//                    }
//                    break;
//
//                case 1:
//                    if (b30DevicesFragment == null) {
////                    b30DevicesFragment = B30DevicesFragment.newInstance();
//                        b30DevicesFragment = new B30DevicesFragment();
//                        //加入事物
//                        beginTransaction.add(R.id.w30_run_content, b30DevicesFragment);
//                    } else {
//                        //否则就显示
//                        beginTransaction.show(b30DevicesFragment);
//                    }
//                    break;
//                default:
//                    break;
//            }
//            //执行
//            beginTransaction.commit();
//        } catch (Error error) {
//
//        }
//
//    }


    public class RunVPAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList = null;
        String[] stringsTitle = {getResources().getString(R.string.string_gps_run),
                getResources().getString(R.string.string_devices_run)};

        public RunVPAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }


        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return stringsTitle[position];
        }
    }
}
