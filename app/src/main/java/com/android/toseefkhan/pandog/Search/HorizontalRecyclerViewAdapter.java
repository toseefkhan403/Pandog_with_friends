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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "HorizontalRecyclerViewA";
    private Context mContext;
    private ArrayList<String> mPostKeysList;
    private DatabaseReference myRef;

    public HorizontalRecyclerViewAdapter(Context mContext, ArrayList<String> mPostKeysList) {
        this.mContext = mContext;
        this.mPostKeysList = mPostKeysList;
        myRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String postKey = mPostKeysList.get(position);
        holder.setIsRecyclable(false);

        myRef.child("Posts")
                .child(postKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        HashMap<String,Object> objectMap = (HashMap<String,Object>) dataSnapshot.getValue();

                        UniversalImageLoader.setImage(objectMap.get("image_url").toString(),holder.first_photo,null,"",holder.child);
                        UniversalImageLoader.setImage(objectMap.get("image_url2").toString(),holder.second_photo,null,"",holder.child);

                        holder.first_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext, ViewPostActivity.class);
                                i.putExtra("intent_post_key",postKey);
                                mContext.startActivity(i);
                            }
                        });

                        holder.second_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mContext, ViewPostActivity.class);
                                i.putExtra("intent_post_key",postKey);
                                mContext.startActivity(i);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return mPostKeysList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView first_photo,second_photo;
        View child;

        public ViewHolder(View itemView) {
            super(itemView);

            first_photo = itemView.findViewById(R.id.first_image);
            second_photo = itemView.findViewById(R.id.second_image);
            child = itemView.findViewById(R.id.progress_child);
        }
    }
}
