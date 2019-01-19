package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.R;

import com.android.toseefkhan.pandog.Utils.Heart;
import com.android.toseefkhan.pandog.models.Photo;


import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ArrayList<String> mProfilePhoto= new ArrayList<>();
    private ArrayList<Photo> mPhotoList= new ArrayList<>();
    private Heart mHeart;
    private ImageView mHeartWhite, mHeartRed, mHeartWhite2, mHeartRed2;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_view_post_fragment, container, false);
        int time =(int) System.currentTimeMillis()%2;

        mHeartWhite = view.findViewById(R.id.image_heart_white);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mHeartWhite2 = view.findViewById(R.id.image_heart_white2);
        mHeartRed2 = view.findViewById(R.id.image_heart_red2);

        if (time == 1){
            mHeartWhite.setImageDrawable(getResources().getDrawable(R.drawable.flames_emoji));
            mHeartRed.setImageDrawable(getResources().getDrawable(R.drawable.fire_emoji_with_color));
            mHeartRed.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mHeartWhite2.setImageDrawable(getResources().getDrawable(R.drawable.flames_emoji));
            mHeartRed2.setImageDrawable(getResources().getDrawable(R.drawable.fire_emoji_with_color));
            mHeartRed2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        mHeartRed.setVisibility(View.GONE);
        mHeartWhite.setVisibility(View.VISIBLE);
        mHeartRed2.setVisibility(View.GONE);
        mHeartWhite2.setVisibility(View.VISIBLE);

        mHeart = new Heart(mHeartWhite,mHeartRed,mHeartWhite2,mHeartRed2,view,getContext());

    //    mProfilePhoto.add("https://firebasestorage.googleapis.com/v0/b/pandog-with-friends.appspot.com/o/photos%2Fusers%2FWnkWgAskxMfSXxeWgxdcQEkXOWG3%2Fphoto2?alt=media&token=503ddc1f-a0f3-440a-84b1-baa0e9e3d34b");
     //   mProfilePhoto.add("https://i.imgur.com/ZcLLrkY.jpg");

        // initRecyclerView();
//        ImageView redHeart2= view.findViewById(R.id.image_heart_red2);
//        redHeart2.setVisibility(View.VISIBLE);
        testToggle();

        return view;
    }

    private void testToggle() {

        mHeartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  mHeart.toggleLike();
            }
        });
        mHeartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   mHeart.toggleLike();
            }
        });
        mHeartWhite2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //      mHeart.toggleLike2();
            }
        });
        mHeartRed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  mHeart.toggleLike2();
            }
        });

    }


//




}
