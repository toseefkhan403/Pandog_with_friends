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
    private ArrayList<User> mUserList= new ArrayList<>();

    public RecyclerViewAdapter(Context mContext, ArrayList<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: received user: " + mUserList.get(position));

        //for pp
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(mUserList.get(position).getProfile_photo(),holder.pp);

        holder.userName.setText(mUserList.get(position).getUsername());
        holder.email.setText(mUserList.get(position).getEmail());

        Log.d(TAG, "onBindViewHolder: the pp is " + mUserList.get(position).getProfile_photo());

        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(mContext, ViewProfileActivity.class);
                i.putExtra(mContext.getString(R.string.intent_user), mUserList.get(position));
                mContext.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView pp;
        TextView userName, email;

        public ViewHolder(View itemView) {
            super(itemView);

            pp= itemView.findViewById(R.id.UserProfilePictureView);
            userName= itemView.findViewById(R.id.UserNameView);
            email= itemView.findViewById(R.id.UserEmailView);
    }

    }
}