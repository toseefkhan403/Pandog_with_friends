package com.android.toseefkhan.pandog.Map;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapRecyclerViewAdapter extends RecyclerView.Adapter<MapRecyclerViewAdapter.ViewHolder>{

    public interface OnTopReachedListener {

        void onTopReached(int position);
    }

    public interface OnScrollDownListener {

        void onScrolledDown();
    }

    private static final String TAG = "MapRecyclerViewAdapter";
    private Context mContext;
    private ArrayList<User> mUserList;
    private String uid;
    OnTopReachedListener onTopReachedListener;
    OnScrollDownListener onScrollDownListener;

    public MapRecyclerViewAdapter(Context mContext, ArrayList<User> mUserList) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    public void setOnTopReachedListener(OnTopReachedListener onTopReachedListener){
        this.onTopReachedListener = onTopReachedListener;
    }

    public void setOnScrollDownListener(OnScrollDownListener onScrollDownListener){
        this.onScrollDownListener = onScrollDownListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: received user: " + mUserList.get(position).getUser_id());

        holder.setIsRecyclable(false);

        UniversalImageLoader.setImage(mUserList.get(position).getProfile_photo(),holder.pp,null,"",holder.child);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        holder.bestTag.setVisibility(View.GONE);

        if (mUserList.get(position).getLevel().equals("BLACK"))
            holder.bestTag.setVisibility(View.VISIBLE);

        if (mUserList.get(position).getUser_id().equals(uid) ) {
            holder.userName.setText(mUserList.get(position).getUsername() + " (You)" + "  #"+ String.valueOf(position+1));
        }
        else
            holder.userName.setText(mUserList.get(position).getUsername() + "  #"+ String.valueOf(position+1));

        holder.email.setText(mUserList.get(position).getEmail());

        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mUserList.get(position).getUser_id().equals(uid)){
                    Intent i= new Intent(mContext, ViewProfileActivity.class);
                    i.putExtra(mContext.getString(R.string.intent_user), mUserList.get(position));
                    mContext.startActivity(i);
                }
                else {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    mContext.startActivity(i);
                }
            }
        });

        if (position < 10)
            onTopReachedListener.onTopReached(0);
        else
            onScrollDownListener.onScrolledDown();
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout main;
        CircleImageView pp,bestTag;
        TextView userName, email;
        ProgressBar pb;
        View child;

        public ViewHolder(View itemView) {
            super(itemView);

            pp= itemView.findViewById(R.id.UserProfilePictureView);
            userName= itemView.findViewById(R.id.UserNameView);
            email= itemView.findViewById(R.id.UserEmailView);
            pb = itemView.findViewById(R.id.pb);
            pb.setVisibility(View.GONE);
            bestTag = itemView.findViewById(R.id.bestTag);
            main = itemView.findViewById(R.id.main);
            child = itemView.findViewById(R.id.progress_child);
        }
    }
}
