package com.android.toseefkhan.pandog.Search;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ViewPostsListActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.TrendingItem;
import com.android.toseefkhan.pandog.models.User;
import com.dingmouren.layoutmanagergroup.skidright.SkidRightLayoutManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerticalRecyclerViewAdapter extends RecyclerView.Adapter<VerticalRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "VerticalRecyclerViewAda";
    private Context mContext;
    private ArrayList<TrendingItem> mTrendingList;

    public VerticalRecyclerViewAdapter(Context mContext, ArrayList<TrendingItem> mTrendingList) {
        this.mContext = mContext;
        this.mTrendingList = mTrendingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trending_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TrendingItem item = mTrendingList.get(position);
        holder.setIsRecyclable(false);

        holder.hashtag_title.setText(item.getTitle());
        holder.hashtag_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                Intent i = new Intent(mContext, ViewPostsListActivity.class);
                i.putExtra("post_keys_list",item.getPost_keys_list());
                i.putExtra("title",item.getTitle());
                mContext.startActivity(i);
                ((Activity)mContext).overridePendingTransition(R.anim.pull,R.anim.push);
            }
        });

        holder.mHorizontalRecyclerView.setAdapter(new HorizontalRecyclerViewAdapter(mContext,item.getPost_keys_list()));

    }

    @Override
    public int getItemCount() {
        return mTrendingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView hashtag_title;
        RecyclerView mHorizontalRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);

            hashtag_title= itemView.findViewById(R.id.hashtag_title);
            mHorizontalRecyclerView = itemView.findViewById(R.id.horizontal_list);
            mHorizontalRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false));
        }
    }

    private void hideKeyboard(){

        Activity activity = (Activity) mContext;
        InputMethodManager imm =(InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (activity.getCurrentFocus() != null && imm != null)
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),0);
    }


}
