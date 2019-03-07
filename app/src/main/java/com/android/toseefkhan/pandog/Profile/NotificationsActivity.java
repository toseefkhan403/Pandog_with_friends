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
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Notif;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private Context mContext = NotificationsActivity.this;

    private DatabaseReference myRef;
    private RecyclerView r;
    private ArrayList<Notif> mNotifsList = new ArrayList<>();
    private int count = 0;

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

        r = findViewById(R.id.recycler_view_list_notifs);
        r.setLayoutManager(new LinearLayoutManager(mContext));

        getNotifs();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void hasListEnded(){
        Log.d(TAG, "hasListEnded: called." + count + "  " + mNotifsList.size());

        if (mNotifsList.size() == count) {
            Log.d(TAG, "hasListEnded: making sure you happen only once");
            Collections.reverse(mNotifsList);
            NotifsAdapter mAdapter = new NotifsAdapter(mNotifsList);
            r.setAdapter(mAdapter);
        }
    }

    private void getNotifs() {

        myRef.child("user_notif")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()) {
                            count++;
                        }
                        if (count == 0){
                            findViewById(R.id.no_notifs).setVisibility(View.VISIBLE);
                            return;
                        }

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            HashMap<String, Object> data =(HashMap<String, Object>) ((HashMap<String, Object>) ss.getValue()).get("data");
                            Log.d(TAG, "onDataChange: data" + data);

                            switch((String)data.get("type")){

                                case "Following":
                                    getDataForFollowing((String) data.get("followerUserUid"));
                                    break;

                                case "Challenge":
                                    if (data.get("status").equals("NOT_DECIDED"))
                                        getDataForChallenger((String) data.get("challengerUserUid"));
                                    else if (data.get("status").equals("ACCEPTED"))
                                        getDataForChallenged((String) data.get("challengedUserUid"),(String) data.get("postKey"));
                                    else if (data.get("status").equals("REJECTED"))
                                        getDataForRejected((String) data.get("challengedUserUid"));

                                    break;

                                case "RESULTS":
                                    getDataForResult((String) data.get("postKey"),(String) data.get("status"), data);
                                    break;

                                case "mention":
                                    getDataForMention((String) data.get("postKey"),(String) data.get("userUid"),(String) data.get("mentionedPlace"));
                                    break;

                                case "comment":
                                    getDataForComment((String) data.get("postKey"),(String) data.get("userUid"));
                                    break;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getDataForMention(String postKey, String userUid, String mentionedPlace) {

        if (mentionedPlace.equals("comment")) {
            myRef.child(getString(R.string.dbname_users))
                    .child(userUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                Notif notif = new Notif();
                                notif.setmTitle(dataSnapshot.getValue(User.class).getUsername() + " mentioned you in a comment.");
                                notif.setmDescription(getEmojiByUnicode(0x1F496));
                                notif.setmImgUrl(dataSnapshot.getValue(User.class).getProfile_photo());

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("intent_post_key", postKey);
                                obj.put("post_comments",postKey);
                                notif.setmIntentExtra(obj);

                                mNotifsList.add(notif);
                                hasListEnded();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }else if (mentionedPlace.equals("post")) {

            myRef.child(getString(R.string.dbname_users))
                    .child(userUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                Notif notif = new Notif();
                                notif.setmTitle(dataSnapshot.getValue(User.class).getUsername() + " mentioned you in a Post!");
                                notif.setmDescription(getEmojiByUnicode(0x1F929));
                                notif.setmImgUrl(dataSnapshot.getValue(User.class).getProfile_photo());

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("intent_post_key", postKey);
                                notif.setmIntentExtra(obj);

                                mNotifsList.add(notif);
                                hasListEnded();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }

    private void getDataForComment(String postKey, String userUid) {

        myRef.child(getString(R.string.dbname_users))
                .child(userUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            Notif notif = new Notif();
                            notif.setmTitle(dataSnapshot.getValue(User.class).getUsername() + " commented on your post.");
                            notif.setmDescription("Click here to know more.");
                            notif.setmImgUrl(dataSnapshot.getValue(User.class).getProfile_photo());

                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("intent_post_key", postKey);
                            obj.put("post_comments",postKey);
                            notif.setmIntentExtra(obj);

                            mNotifsList.add(notif);
                            hasListEnded();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getDataForRejected(String challengedUserUid) {

        Notif notif = new Notif();

        myRef.child(getString(R.string.dbname_users))
                .child(challengedUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        String username = user.getUsername();
                        notif.setmTitle(username +" rejected your challenge " + getEmojiByUnicode(0x1F614));
                        notif.setmDescription("Perhaps you should unfollow him/her " + getEmojiByUnicode(0x1F606));
                        notif.setmImgUrl(user.getProfile_photo());

                        HashMap<String, Object> obj = new HashMap<>();
                        obj.put(getString(R.string.intent_user),user);
                        notif.setmIntentExtra(obj);

                        mNotifsList.add(notif);
                        hasListEnded();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void getDataForChallenged(String challengedUserUid, String postKey) {

        Log.d(TAG, "getDataForChallenged: getting data for accepted post");
        Notif notif = new Notif();

        myRef.child(getString(R.string.dbname_users))
                .child(challengedUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        String username = user.getUsername();
                        notif.setmTitle("Your post with "+ username +" is up for voting!");
                        notif.setmDescription("Get maximum likes on your Celfie to win the war " + getEmojiByUnicode(0x1F60E));
                        notif.setmImgUrl(user.getProfile_photo());

                        HashMap<String, Object> obj = new HashMap<>();
                        obj.put("intent_post_key",postKey);
                        notif.setmIntentExtra(obj);

                        mNotifsList.add(notif);
                        hasListEnded();
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

                                notif.setmDescription("Your challenge with " + user.getUsername() + " ended in a Draw. Yay! You got +2 Flames");

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("intent_post_key",postKey);
                                notif.setmIntentExtra(obj);
                                notif.setmImgUrl(user.getProfile_photo());

                                mNotifsList.add(notif);
                                hasListEnded();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;

            case "win":

                notif.setmTitle("Woohooo! You Won +5 Flames! " + getEmojiByUnicode(0x1F603));

                myRef.child(getString(R.string.dbname_users))
                        .child((String) data.get("loser"))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);

                                notif.setmDescription("You won your challenge against " + user.getUsername() +" like a boss " + getEmojiByUnicode(0x1F60E));

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("intent_post_key",postKey);
                                notif.setmIntentExtra(obj);
                                notif.setmImgUrl(user.getProfile_photo());

                                mNotifsList.add(notif);
                                hasListEnded();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;

            case "lose":

                notif.setmTitle("You lose! Better Luck Next Time! " + getEmojiByUnicode(0x1F613));

                myRef.child(getString(R.string.dbname_users))
                        .child((String) data.get("winner"))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);

                                notif.setmDescription("You lost against " + user.getUsername() + ". \n You received -2 Flames.");

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("intent_post_key",postKey);
                                notif.setmIntentExtra(obj);
                                notif.setmImgUrl(user.getProfile_photo());

                                mNotifsList.add(notif);
                                hasListEnded();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                break;
        }
    }

    private void getDataForChallenger(String s) {

        if(s != null) {
            myRef.child(getString(R.string.dbname_users))
                    .child(s)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);

                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("ChallengerUser", user);

                            mNotifsList.add(new Notif(user.getUsername() + " challenged you!",
                                    "Put up your selfie and show'em who's the best! " + getEmojiByUnicode(0x1F60E),
                                    user.getProfile_photo(), obj));
                            hasListEnded();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
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
                        notif.setmDescription("Click here now to challenge him/her with your best Selfie " + getEmojiByUnicode(0x1F60E));

                        HashMap<String,Object> obj = new HashMap<>();
                        obj.put(getString(R.string.intent_user),user);
                        notif.setmIntentExtra(obj);

                        mNotifsList.add(notif);
                        hasListEnded();
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notif_item_activity, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Log.d(TAG, "onBindViewHolder: called.");
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

                    }else if (intentExtra.containsKey("intent_post_key")){

                        if (intentExtra.containsKey("post_comments")){
                            Intent i = new Intent(mContext, ViewPostActivity.class);
                            i.putExtra("intent_post_key", (String) intentExtra.get("intent_post_key"));
                            i.putExtra("post_comments", (String) intentExtra.get("post_comments"));
                            startActivity(i);
                        }else {
                            Intent i = new Intent(mContext, ViewPostActivity.class);
                            i.putExtra("intent_post_key", (String) intentExtra.get("intent_post_key"));
                            startActivity(i);
                        }

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

    private String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

}
