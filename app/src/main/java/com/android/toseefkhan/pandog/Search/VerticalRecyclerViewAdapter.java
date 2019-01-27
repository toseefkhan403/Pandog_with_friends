package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.SquareImageView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.dingmouren.layoutmanagergroup.skidright.SkidRightLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerticalRecyclerViewAdapter extends RecyclerView.Adapter<VerticalRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "VerticalRecyclerViewAda";
    private Context mContext;
    private ArrayList<Post> mPostList = new ArrayList<>();
    private ArrayList<User> mUserList;
    private RecyclerView mHorizontalRecyclerView;
    private SkidRightLayoutManager mSkidLM;


    public VerticalRecyclerViewAdapter(Context mContext, ArrayList<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    public VerticalRecyclerViewAdapter(ArrayList<User> mUserList, Context mContext) {
        this.mContext = mContext;
        this.mUserList = mUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trending_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPostList.get(position);
        //for testing
        holder.for_style.setImageDrawable(mContext.getResources().getDrawable(R.drawable.heart_red));
        holder.hashtag_title.setText("Featured posts");

        holder.horizontalRecyclerViewAdapter = new HorizontalRecyclerViewAdapter(mContext, mPostList);
        mHorizontalRecyclerView.setAdapter(holder.horizontalRecyclerViewAdapter);
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView for_style;
        TextView hashtag_title;
        HorizontalRecyclerViewAdapter horizontalRecyclerViewAdapter;


        public ViewHolder(View itemView) {
            super(itemView);

            Context context = itemView.getContext();
            for_style = itemView.findViewById(R.id.just_for_style);
            hashtag_title= itemView.findViewById(R.id.hashtag_title);
            mHorizontalRecyclerView = itemView.findViewById(R.id.horizontal_list);
            mHorizontalRecyclerView.setHasFixedSize(true);
            mSkidLM = new SkidRightLayoutManager(1.5f,0.85f);
            mHorizontalRecyclerView.setLayoutManager(mSkidLM);

        }
    }
}
