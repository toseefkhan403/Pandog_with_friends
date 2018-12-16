package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Search.SearchActivity;
import com.android.toseefkhan.pandog.models.Photo;
import com.android.toseefkhan.pandog.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    //ignore this class, this was just for testing i m not deleting it i suppose we can use this in future if we want to use recycler view

    private static final String TAG = "RecyclerViewAdapter";

    private Context mContext;
    private ArrayList<String> mProfilePhoto= new ArrayList<>();               //needs to be a user object
    private ArrayList<Photo> mPhotoList= new ArrayList<>();

    public RecyclerViewAdapter(Context mContext, ArrayList<String> mProfilePhoto, ArrayList<Photo> mPhotoList) {
        this.mContext = mContext;
        this.mProfilePhoto = mProfilePhoto;
        this.mPhotoList = mPhotoList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_photo_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: received user 1: " + mProfilePhoto.get(0));
        Log.d(TAG, "onBindViewHolder: received user 2: " + mProfilePhoto.get(1));


        //for testing
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(mProfilePhoto.get(position),holder.pp);
        imageLoader.displayImage("https://i.imgur.com/ZcLLrkY.jpg",holder.photoInPost);


        Log.d(TAG, "onBindViewHolder: the pp is " + mProfilePhoto.get(position));
    //    holder.toolbarName.setText(mUserList.get(position).getUsername());

        //set the photo properties
//        UniversalImageLoader.setImage(mPhotoList.get(position).getImage_path(),holder.photoInPost, null,null);
//        holder.caption.setText(mPhotoList.get(position).getCaption());
//        holder.timePeriod.setText(mPhotoList.get(position).getDate_created());

        //todo get the comments and like stuff with click listeners on the comments link and the likes toggle
       // holder.comments.setText(mPhotoList.get(position).get);

//        holder.pp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent =  new Intent(mContext, ViewProfileActivity.class);
//                intent.putExtra(mContext.getString(R.string.intent_user), mProfilePhoto.get(0));
//                mContext.startActivity(intent);
//            }
//        });
//        holder.toolbarName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent =  new Intent(mContext, ViewProfileActivity.class);
//                intent.putExtra(mContext.getString(R.string.intent_user), mUserList.get(position));
//                mContext.startActivity(intent);
//            }
//        });


    }

    @Override
    public int getItemCount() {
        return mProfilePhoto.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView pp;
        TextView toolbarName;
        SquareImageView photoInPost;
        TextView caption, timePeriod;
        ImageView likeToggleWhite,likeToggleRed;
        TextView comments;

        public ViewHolder(View itemView) {
            super(itemView);

            pp= itemView.findViewById(R.id.profile_photo);
            toolbarName = itemView.findViewById(R.id.username);
            photoInPost= itemView.findViewById(R.id.post_image);
            likeToggleWhite= itemView.findViewById(R.id.image_heart_white);
            likeToggleRed= itemView.findViewById(R.id.image_heart_red);
            likeToggleRed.setVisibility(View.GONE);
            comments= itemView.findViewById(R.id.image_comments_link);
            caption= itemView.findViewById(R.id.image_caption);
            timePeriod= itemView.findViewById(R.id.image_time_posted);

    }



    }
}