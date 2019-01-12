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
import com.android.toseefkhan.pandog.Utils.SquareImageView;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerticalRecyclerViewAdapter extends RecyclerView.Adapter<VerticalRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "VerticalRecyclerViewAda";
    private Context mContext;
    private ArrayList<Post> mPostList;
    private ArrayList<User> mUserList;
    private RecyclerView mHorizontalRecyclerView;


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

        Log.d(TAG, "onBindViewHolder: received user: " + mUserList.get(position));

        //todo a query to get different hash tags
        //for testing
        holder.hashtag_title.setText("Featured posts");
  //      holder.hashtag_title.setText("Trending hashtags");



    }

    @Override
    public int getItemCount() {
        return mUserList.size();
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
            mHorizontalRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            horizontalRecyclerViewAdapter = new HorizontalRecyclerViewAdapter(mUserList,mContext);
            mHorizontalRecyclerView.setAdapter(horizontalRecyclerViewAdapter);
        }
    }
}
