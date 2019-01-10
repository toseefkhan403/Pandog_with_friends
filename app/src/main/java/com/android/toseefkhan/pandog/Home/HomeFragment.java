package com.android.toseefkhan.pandog.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.R;

import com.android.toseefkhan.pandog.models.Photo;


import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ArrayList<String> mProfilePhoto= new ArrayList<>();
    private ArrayList<Photo> mPhotoList= new ArrayList<>();
    private View view;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.layout_mainfeed_listitem,container,false);


    //    mProfilePhoto.add("https://firebasestorage.googleapis.com/v0/b/pandog-with-friends.appspot.com/o/photos%2Fusers%2FWnkWgAskxMfSXxeWgxdcQEkXOWG3%2Fphoto2?alt=media&token=503ddc1f-a0f3-440a-84b1-baa0e9e3d34b");
     //   mProfilePhoto.add("https://i.imgur.com/ZcLLrkY.jpg");


       // initRecyclerView();
//        ImageView redHeart2= view.findViewById(R.id.image_heart_red2);
//        redHeart2.setVisibility(View.VISIBLE);


        return view;
    }



//    public void initRecyclerView(){
//
//
//            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
//            RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
//            recyclerView.setLayoutManager(layoutManager);
//            RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), mProfilePhoto, mPhotoList);
//            Log.d(TAG, "onCreateView: the pp which is added "+ mProfilePhoto);
//            recyclerView.setAdapter(adapter);
//
//    }




}
