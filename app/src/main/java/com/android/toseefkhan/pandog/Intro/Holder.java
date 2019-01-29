package com.android.toseefkhan.pandog.Intro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;

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
