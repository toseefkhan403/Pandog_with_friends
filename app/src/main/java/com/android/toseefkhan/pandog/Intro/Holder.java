package com.android.toseefkhan.pandog.Intro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.toseefkhan.pandog.Home.HomeFragment;
import com.android.toseefkhan.pandog.Home.NotificationFragment;
import com.android.toseefkhan.pandog.Login.LoginActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Holder extends AppCompatActivity {


    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


        setupViewPager();
    }

    private void setupViewPager() {

        FragmentPagerAdapter adapter=new FragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Screen1()); //index is 0
        adapter.addFragment(new Screen2());  //index is 1
        adapter.addFragment(new Screen3());     //2
        adapter.addFragment(new Screen4());     //3

        viewPager=findViewById(R.id.container);
        viewPager.setAdapter(adapter);
    }

    public void gotoFragment(int fragmentNumber){

        viewPager.setCurrentItem(fragmentNumber);

    }

}
