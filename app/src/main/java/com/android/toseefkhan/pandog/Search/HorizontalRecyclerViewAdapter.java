package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.SquareImageView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "HorizontalRecyclerViewA";
    private Context mContext;
    private ArrayList<Post> mPostList;
    private ArrayList<User> mUserList;


    public HorizontalRecyclerViewAdapter(Context mContext, ArrayList<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    public HorizontalRecyclerViewAdapter(ArrayList<User> mUserList,Context mContext) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: received user: " + mUserList.get(position));

        //todo a query to get the post from hash tags
        //for testing

        UniversalImageLoader.setImage(mUserList.get(position).getProfile_photo(),  holder.first_photo, null, "");
        UniversalImageLoader.setImage(mUserList.get(position).getProfile_photo(),  holder.second_photo, null, "");

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        SquareImageView first_photo,second_photo;

        public ViewHolder(View itemView) {
            super(itemView);

            first_photo = itemView.findViewById(R.id.first_image);
            second_photo= itemView.findViewById(R.id.second_image);
        }
    }
}
