package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.Profile.ViewPostActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;

import java.util.ArrayList;

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

        Post post = mPostList.get(position);
        //todo a query to get the post from hash tags
        //for testing
        UniversalImageLoader.setImage(post.getImage_url(),holder.first_photo,null,"",holder.child);

        holder.first_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo navigate to that particular post
               Intent i = new Intent(mContext, ViewPostActivity.class);
               i.putExtra(mContext.getString(R.string.intent_post),post);
               mContext.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView first_photo;
        View child;

        public ViewHolder(View itemView) {
            super(itemView);

            first_photo = itemView.findViewById(R.id.first_image);
            child = itemView.findViewById(R.id.progress_child);
        }
    }
}
