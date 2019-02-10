package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Search.ReferActivity;
import com.android.toseefkhan.pandog.Search.SearchActivity;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Notif;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.gms.common.data.ObjectExclusionFilterable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private Context mContext = NotificationsActivity.this;

    private DatabaseReference myRef;
    private ArrayList<Notif> mNotifsList = new ArrayList<>();
    private NotifsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: started");
        setContentView(R.layout.layout_notif_activity);

        myRef = FirebaseDatabase.getInstance().getReference();

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerView r = findViewById(R.id.recycler_view_list_notifs);
        r.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new NotifsAdapter(mNotifsList);
        r.setAdapter(adapter);

        getNotifs();
    }

    private void getNotifs() {

        myRef.child("user_notif")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            HashMap<String, Object> data =(HashMap<String, Object>) ((HashMap<String, Object>) ss.getValue()).get("data");
                            Log.d(TAG, "onDataChange: data" + data);

                            switch((String)data.get("type")){

                                case "Following":
                                    getDataForFollowing((String) data.get("followerUserUid"));
                                    break;

                                case "Challenge":
                                    getDataForChallenge((String) data.get("challengerUserUid"));
                                    break;

                                case "RESULTS":
                                    getDataForResult((String) data.get("postKey"),(String) data.get("status"), data);
                                    break;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getDataForResult(String postKey, String status,HashMap<String,Object> data) {

        Notif notif = new Notif();

        switch (status) {

            case "draw":
                notif.setmTitle("It's a Tie!");

                String uidOpponent;
                if (data.get("user1").equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    uidOpponent = (String) data.get("user2");
                else
                    uidOpponent = (String) data.get("user1");

                myRef.child(getString(R.string.dbname_users))
                        .child(uidOpponent)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);

                                notif.setmDescription("Your challenge with " + user.getUsername() + " ended in a Draw");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;

            case "win":

                notif.setmTitle("Woohooo! You Won!");

                myRef.child(getString(R.string.dbname_users))
                        .child((String) data.get("loser"))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);

                                notif.setmDescription("You won your challenge against " + user.getUsername());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;

            case "lose":

                notif.setmTitle("You lose! Better Luck Next Time!");

                myRef.child(getString(R.string.dbname_users))
                        .child((String) data.get("winner"))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);

                                notif.setmDescription("You lost against " + user.getUsername() + ". \nAll you need is a better photographer, trust me.");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;
        }

        myRef.child("Posts")
                .child(postKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot singleSnapshot) {

                        Post post = new Post();
                        HashMap<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        post.setStatus(objectMap.get("status").toString());

                        if (post.getStatus().equals("INACTIVE"))
                            post.setWinner(objectMap.get("winner").toString());

                        post.setImage_url(objectMap.get("image_url").toString());
                        post.setImage_url2(objectMap.get("image_url2").toString());

                        post.setCaption(objectMap.get("caption").toString());
                        post.setCaption2(objectMap.get("caption2").toString());
                        post.setPhoto_id(objectMap.get("photo_id").toString());
                        post.setPhoto_id2(objectMap.get("photo_id2").toString());

                        post.setTags(objectMap.get("tags").toString());
                        post.setTags2(objectMap.get("tags2").toString());

                        post.setUser_id(objectMap.get("user_id").toString());
                        post.setUser_id2(objectMap.get("user_id2").toString());

                        post.setChallenge_id(objectMap.get("challenge_id").toString());
                        post.setTimeStamp(Long.parseLong(objectMap.get("timeStamp").toString()));

                        post.setPostKey(objectMap.get("postKey").toString());
                        /*String image_url, String caption, String photo_id, String user_id, String tags,
                String image_url2, String caption2, String photo_id2, String user_id2, String tags2*/

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child("likes").getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        post.setLikes(likesList);

                        List<Like> likesList2 = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child("likes2").getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList2.add(like);
                        }
                        post.setLikes2(likesList2);

                        List<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child("comments").getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comments.add(comment);
                        }
                        post.setComments(comments);

                        notif.setmImgUrl(post.getImage_url());
                        HashMap<String, Object> obj = new HashMap<>();
                        obj.put(getString(R.string.intent_post),post);
                        notif.setmIntentExtra(obj);

                        mNotifsList.add(notif);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void getDataForChallenge(String s) {

        myRef.child(getString(R.string.dbname_users))
                .child(s)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        HashMap<String,Object> obj = new HashMap<>();
                        obj.put("ChallengerUser",user);

                        mNotifsList.add(new Notif(user.getUsername() + " Challenged you!",
                                "Put up your selfie and show'em who's the best!",
                                user.getProfile_photo(),obj));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getDataForFollowing(String followerUserUid) {

        //get the followers info
        myRef.child(getString(R.string.dbname_users))
                .child(followerUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        Notif notif = new Notif();
                        notif.setmImgUrl(user.getProfile_photo());
                        notif.setmTitle(user.getUsername() + " started following you!");
                        notif.setmDescription("Click here now to challenge him/her with your best Selfie");

                        HashMap<String,Object> obj = new HashMap<>();
                        obj.put(getString(R.string.intent_user),user);
                        notif.setmIntentExtra(obj);

                        mNotifsList.add(notif);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    class NotifsAdapter extends RecyclerView.Adapter<NotifsAdapter.ViewHolder> {

        private ArrayList<Notif> notifsList;

        public NotifsAdapter(ArrayList<Notif> notifsList) {
            this.notifsList = notifsList;
        }

        public NotifsAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notif_item_activity, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.setIsRecyclable(false);
            String photoUrl = notifsList.get(position).getmImgUrl();
            String title = notifsList.get(position).getmTitle();
            String description = notifsList.get(position).getmDescription();
            HashMap<String, Object> intentExtra = notifsList.get(position).getmIntentExtra();

            UniversalImageLoader.setImage(photoUrl,holder.img,null,"",null);

            holder.title.setText(title);

            holder.desc.setText(description);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (intentExtra.containsKey(getString(R.string.intent_user))){

                        Intent i = new Intent(mContext, ViewProfileActivity.class);
                        i.putExtra(getString(R.string.intent_user),(User)intentExtra.get(getString(R.string.intent_user)));
                        startActivity(i);

                    }else if (intentExtra.containsKey(getString(R.string.intent_post))){

                        Intent i = new Intent(mContext, ViewPostActivity.class);
                        i.putExtra(getString(R.string.intent_post),(Post)intentExtra.get(getString(R.string.intent_post)));
                        startActivity(i);

                    }else if(intentExtra.containsKey("ChallengerUser")){

                        Intent i = new Intent(mContext, HomeActivity.class);
                        i.putExtra("ChallengerUser",2);
                        startActivity(i);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mNotifsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout view;
            CircleImageView img;
            TextView title,desc;
            public ViewHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.PictureView);
                title = itemView.findViewById(R.id.TitleView);
                desc = itemView.findViewById(R.id.DescriptionView);
                view = itemView.findViewById(R.id.main);
            }
        }
    }

}
