package com.android.toseefkhan.pandog.Utils;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.Home.HomeFragment;
import com.android.toseefkhan.pandog.Home.NotificationFragment;
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

public class ViewLikesActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_likes);

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

        ViewLikesFragment viewLikesFragment = new ViewLikesFragment();
        viewLikesFragment.setArguments(getIntent().getExtras());

        ViewLikesFragment2 viewLikesFragment2 = new ViewLikesFragment2();
        viewLikesFragment2.setArguments(getIntent().getExtras());

        adapter.addFragment(viewLikesFragment); //index is 0
        adapter.addFragment(viewLikesFragment2);  //index is 1

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (getIntent().getExtras()!=null) {
            Post post = getIntent().getExtras().getParcelable(getString(R.string.intent_post));

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child(getString(R.string.dbname_users))
                    .child(post.getUser_id())
                    .child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            tabLayout.getTabAt(0).setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            ref.child(getString(R.string.dbname_users))
                    .child(post.getUser_id2())
                    .child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            tabLayout.getTabAt(1).setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

}
