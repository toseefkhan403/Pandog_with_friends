package com.android.toseefkhan.pandog.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.MyMention;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


public class CommentsRVAdapter extends RecyclerView.Adapter<CommentsRVAdapter.ViewHolder>{

    public interface replyButtonListener {

        void replyButtonPressed(User user);
    }

    public interface editButtonListener {

        void editButtonPressed(Comment comment);
    }

    private static final String TAG = "CommentsRVAdapter";
    private Context mContext;
    private ArrayList<Comment> mCommentsList;
    private DatabaseReference ref;
    private String postKey;
    replyButtonListener replyButtonListener;
    editButtonListener editButtonListener;
    private boolean mLikedbyCurrentUser;
    private int likesCount;

    public CommentsRVAdapter(Context mContext, ArrayList<Comment> mCommentsList,String postKey) {
        this.mContext = mContext;
        this.mCommentsList = mCommentsList;
        this.postKey = postKey;
        ref = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Comment comment = mCommentsList.get(position);
        Log.d(TAG, "onBindViewHolder: the comment is this " + comment);

        setUserDetails(holder,comment);
        setLikesIcons(holder,comment);
        initLikesString(holder, comment);

        holder.viewLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to see the likes");

                Intent i = new Intent(mContext,ViewLikesActivity.class);
                i.putExtra("comment_likes",comment);
                mContext.startActivity(i);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: editing the comment");

                try{
                    editButtonListener = (editButtonListener) mContext;
                }catch (ClassCastException e){
                    Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
                }

                try{
                    editButtonListener.editButtonPressed(comment);
                }catch (Exception e){
                    Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
                }

            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: editing the comment");

                ref.child("Posts")
                        .child(postKey)
                        .child("comments")
                        .child(comment.getCommentID())
                        .removeValue();

                mCommentsList.remove(comment);
                notifyItemRemoved(position);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                },500);
            }
        });


        if (comment.getComment().contains("@")){
            ArrayList<MyMention> arr = comment.getMentionArrayList();
            Log.d(TAG, "onBindViewHolder: m coming to this if " + arr);
            List<String> links = new ArrayList<>();
            List<ClickableSpan> clicks = new ArrayList<>();

            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {

                    links.add(arr.get(i).getMentionName());
                    String s = arr.get(i).getMentionUid();
                    clicks.add(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            initiateIntent(s);
                        }
                    });
                }
                makeLinks(holder.comment, links, clicks, comment.getComment());
            }else{
                holder.comment.setText(comment.getComment());
            }

        }else {
            holder.comment.setText(comment.getComment());
        }

    }

    private void replyToTheComment(User user) {

        try{
            replyButtonListener = (replyButtonListener) mContext;
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            replyButtonListener.replyButtonPressed(user);
        }catch (Exception e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }

    private void initLikesString(ViewHolder holder, Comment comment) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query =  ref.child("Posts")
                .child(postKey)
                .child("comments")
                .child(comment.getCommentID())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()){

                    Like like = singleSnap.getValue(Like.class);
                    if (like.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        mLikedbyCurrentUser = true;
                    }

                    likesCount++;
                    Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                }
                if (likesCount==1)
                    holder.viewLikes.setText(String.valueOf(likesCount)+ " Like");
                else
                    holder.viewLikes.setText(String.valueOf(likesCount)+ " Likes");

                likesCount = 0;
                if (mLikedbyCurrentUser){
                    holder.heartRed.setVisibility(View.VISIBLE);
                    holder.heartWhite.setVisibility(View.GONE);
                    mLikedbyCurrentUser = false;
                }else {
                    holder.heartRed.setVisibility(View.GONE);
                    holder.heartWhite.setVisibility(View.VISIBLE);
                    mLikedbyCurrentUser=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder, Comment comment) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query =  ref.child("Posts")
                .child(postKey)
                .child("comments")
                .child(comment.getCommentID())
                .child("likes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()){
                    Like like = singleSnap.getValue(Like.class);

                    likesCount++;
                    Log.d(TAG, "onDataChange: the first post has been liked by " +  like.getUser_id());
                }
                if (likesCount==1)
                    holder.viewLikes.setText(String.valueOf(likesCount)+ " Like");
                else
                    holder.viewLikes.setText(String.valueOf(likesCount)+ " Likes");

                likesCount = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setLikesIcons(final ViewHolder holder, Comment comment) {

        final HeartComment mHeart = new HeartComment(holder.heartWhite,holder.heartRed,mContext,postKey);
        holder.heartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(holder,comment);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getLikesString(holder,comment);
                    }
                },600);
            }
        });
        holder.heartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeart.toggleLike(holder,comment);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getLikesString(holder,comment);
                    }
                },600);            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

    private void initiateIntent(String s) {

        ref.child(mContext.getString(R.string.dbname_users))
                .child(s)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);

                            if (user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                                Intent i = new Intent(mContext,ProfileActivity.class);
                                mContext.startActivity(i);
                            }else {
                                Intent intent = new Intent(mContext, ViewProfileActivity.class);
                                intent.putExtra(mContext.getString(R.string.intent_user), user);
                                mContext.startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void makeLinks(TextView textView, List<String> links, List<ClickableSpan> clickableSpans,String comment) {
        SpannableString spannableString = new SpannableString(comment);
        for (int i = 0; i < links.size(); i++) {
            ClickableSpan clickableSpan = clickableSpans.get(i);
            String link = links.get(i);

            Log.d(TAG, "makeLinks: link " + link);
            int startIndexOfLink = comment.indexOf(link);
            spannableString.setSpan(clickableSpan, startIndexOfLink,
                    startIndexOfLink + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setHighlightColor(
                Color.TRANSPARENT); // prevent TextView change background when highlight
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private void setUserDetails(final ViewHolder viewHolder, Comment comment) {

        final String uid = comment.getUser_id();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(mContext.getString(R.string.dbname_users))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: datasnapshot.getValue" + dataSnapshot.getValue());

                        final User user = dataSnapshot.getValue(User.class);

                        viewHolder.comment_username.setText(user.getUsername());
                        UniversalImageLoader.setImage(user.getProfile_photo(), viewHolder.profile_image,null,"",null);

                        if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            viewHolder.comment_username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user),user);
                                    mContext.startActivity(i);
                                }
                            });

                            viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(mContext, ViewProfileActivity.class);
                                    i.putExtra(mContext.getString(R.string.intent_user),user);
                                    mContext.startActivity(i);
                                }
                            });

                            viewHolder.edit.setVisibility(View.GONE);
                            viewHolder.delete.setVisibility(View.GONE);
                            viewHolder.reply.setVisibility(View.VISIBLE);

                            viewHolder.reply.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "onClick: replying to the comment");

                                    replyToTheComment(user);
                                }
                            });
                        }
                        else{
                            viewHolder.comment_username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });

                            viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(mContext, ProfileActivity.class);
                                    mContext.startActivity(i);
                                }
                            });

                            viewHolder.edit.setVisibility(View.VISIBLE);
                            viewHolder.delete.setVisibility(View.VISIBLE);
                            viewHolder.reply.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_image;
        TextView comment_username;
        TextView comment,edit,delete,reply,viewLikes;
        ImageView heartWhite,heartRed;

        public ViewHolder(View itemView) {
            super(itemView);

            profile_image = itemView.findViewById(R.id.comment_profile_image);
            comment_username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment);
            edit = itemView.findViewById(R.id.edit_comment);
            reply = itemView.findViewById(R.id.reply_comment);
            delete = itemView.findViewById(R.id.delete_comment);
            viewLikes = itemView.findViewById(R.id.view_likes_comment);
            heartWhite = itemView.findViewById(R.id.image_heart_white);
            heartRed = itemView.findViewById(R.id.image_heart_red);
        }
    }
}
