package com.bozlun.health.android.b30.women;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.bozlun.health.android.R;
import com.bozlun.health.android.siswatch.WatchBaseActivity;

/**
 * Created by Admin
 * Date 2018/11/28
 */
public class WomenPersonalActivity extends WatchBaseActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_women_personal);

        initData();


    }

    private void initData() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.womenCont,new WomenPersonFragment());
        fragmentTransaction.commit();

    }
}
