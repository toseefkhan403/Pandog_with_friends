package com.android.toseefkhan.pandog.Utils;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Post;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class ViewFollowersActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_followers);

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setupViewPager();
    }

    private void setupViewPager() {

        FragmentPagerAdapter adapter=new FragmentPagerAdapter(getSupportFragmentManager());

        ViewFollowersFragment viewFollowersFragment = new ViewFollowersFragment();
        viewFollowersFragment.setArguments(getIntent().getExtras());

        ViewFollowingFragment viewFollowingFragment = new ViewFollowingFragment();
        viewFollowingFragment.setArguments(getIntent().getExtras());

        adapter.addFragment(viewFollowersFragment); //index is 0
        adapter.addFragment(viewFollowingFragment);  //index is 1

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText("Followers");
        tabLayout.getTabAt(1).setText("Following");

        if (getIntent().hasExtra("set_to_two"))
            mViewPager.setCurrentItem(1);

    }

}
