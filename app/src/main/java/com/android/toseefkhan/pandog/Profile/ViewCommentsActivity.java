package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Mention;
import com.android.toseefkhan.pandog.models.MyMention;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.CommentsRVAdapter;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.percolate.mentions.Mentionable;
import com.percolate.mentions.Mentions;
import com.percolate.mentions.QueryListener;
import com.percolate.mentions.SuggestionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class ViewCommentsActivity extends AppCompatActivity{

    private static final String TAG = "ViewCommentsActivity";

    private RecyclerView recyclerView;
    private EditText editComment;
    private Context mContext = ViewCommentsActivity.this;
    private ArrayList<Comment> mComments;
    private CommentsRVAdapter mAdapter;
    private DatabaseReference ref;

    private ArrayList<String> mFollowingList = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();

    private RecyclerView mRVMentions;
    private Mentions mentions;
    private HashMap<String,String> mentionHash = new HashMap<>();
    private String mPostKey;

    /*
        Just pass the postKey to this activity and it will show the comments.
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mPostKey = getIntent().getExtras().getString("post_comments");

        ref = FirebaseDatabase.getInstance().getReference();
        mRVMentions = findViewById(R.id.recycler_mentions);
        mRVMentions.setLayoutManager(new LinearLayoutManager(mContext));
        setupFollowingList();
        initImageLoader();

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.comments_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false));

        editComment = findViewById(R.id.comment);
        editComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if(view != null){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(view, 0);
                    }
                }
            }
        });

        ImageView submitComment = findViewById(R.id.ivPostComment);
        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(editComment.getText().toString());

                    editComment.setText("");
                }else{
                    Snackbar.make(Objects.requireNonNull(getCurrentFocus()),"You can't post a blank comment",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        mComments = new ArrayList<>();

        getCommentsFromPostKey();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

    private void setupFollowingList() {

        Query query = ref
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mFollowingList.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }

                Query query1 = ref
                        .child(getString(R.string.dbname_followers))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: found user: " +
                                    singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                            if (!mFollowingList.contains(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString()))
                                mFollowingList.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                        }

                        //m done!
                        getUsersListFromFollowing();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsersListFromFollowing() {

        for (int i = 0 ; i < mFollowingList.size() ; i++){
            final int count = i;
            Query query = ref
                    .child(getString(R.string.dbname_users))
                    .child(mFollowingList.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mUserList.add(dataSnapshot.getValue(User.class));

                    if(count >= mFollowingList.size() -1){
                        setupMentioning();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void setupMentioning() {

        final SearchAdapter adapter = new SearchAdapter(mContext);
        mRVMentions.setAdapter(adapter);

        mentions = new Mentions.Builder(mContext,editComment)
                .highlightColor(R.color.bg_screen2)
                .maxCharacters(20)
                .queryListener(new QueryListener() {
                    @Override
                    public void onQueryReceived(String s) {
                        adapter.filter(s);
                        Log.d(TAG, "setupMentioning: getInsertedMentions " + mentions.getInsertedMentions());
                    }
                })
                .suggestionsListener(new SuggestionsListener() {
                    @Override
                    public void displaySuggestions(boolean b) {

                        if (!b)
                            mRVMentions.setVisibility(View.GONE);
                        Log.d(TAG, "displaySuggestions: boolean boy " + b);
                    }
                })
                .build();
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getCommentsFromPostKey() {

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("Posts")
                .child(mPostKey)
                .child("comments")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            Comment comment = singleSnapshot.getValue(Comment.class);
                            mComments.add(comment);
                        }
                        Collections.reverse(mComments);
                        mAdapter = new CommentsRVAdapter(mContext, mComments);
                        recyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        comment.setMentionArrayList(checkLists());

        myRef.child("Posts")
                .child(mPostKey)
                .child("comments")
                .child(commentID)
                .setValue(comment);

        mComments.add(0,comment);
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<MyMention> checkLists() {

        ArrayList<MyMention> arr = new ArrayList<>();

        for(Mentionable mentionable : mentions.getInsertedMentions()){

            MyMention m = new MyMention();
            m.setMentionName(mentionable.getMentionName());
            m.setMentionUid(mentionHash.get(mentionable.getMentionName()));
            if (!arr.contains(m))
                arr.add(m);
        }

        return arr;
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchItemViewHolder> {

        private ProfileFilter filter;
        private Context mContext;
        private ArrayList<User> ProfileList;

        public SearchAdapter(Context context) {
            this.mContext = context;
            ProfileList = new ArrayList<>();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.profile_item_mention, viewGroup, false);
            return new SearchItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder searchItemViewHolder, int i) {

            searchItemViewHolder.setIsRecyclable(false);
            User user = getItem(i);

            searchItemViewHolder.userNameView.setText(user.getUsername());
            String PhotoUrl = user.getProfile_photo();
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.userppSearch, null, "",
                    null);

            searchItemViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Mention mention = new Mention();
                    mention.setMentionName("@" + user.getUsername());
                    mention.setMentionUid(user.getUser_id());
                    mentions.insertMention(mention);
                    mentionHash.put(mention.getMentionName(),mention.getMentionUid());
                }
            });
        }

        private User getItem(int i) {
            return ProfileList.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return ProfileList != null ? ProfileList.size() : 0;
        }

        public void filter(CharSequence constraint) {
            if (filter == null) {
                filter = new ProfileFilter();
            }
            filter.filter(constraint);
        }

        public class SearchItemViewHolder extends RecyclerView.ViewHolder {

            View mView;
            TextView userNameView;
            CircleImageView userppSearch;

            public SearchItemViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mView = itemView;

                userNameView = itemView.findViewById(R.id.UserNameView);
                userppSearch = itemView.findViewById(R.id.UserProfilePictureView);
            }
        }

        private class ProfileFilter {

            public ProfileFilter() {
            }

            public void filter(final CharSequence constraint) {

                ProfileList.clear();

                if (constraint != null && constraint.length() > 0) {
                    for (int i = 0; i <mUserList.size() ; i++) {

                        Log.d(TAG, "filter: userslist " + mUserList);

                        if (mUserList.get(i) != null) {
                            String Username = mUserList.get(i).getUsername().toUpperCase();
                            String entered = constraint.toString().toUpperCase();
                            if (Username.contains(entered)) {
                                ProfileList.add(mUserList.get(i));
                            }
                        }
                    }

                    if (ProfileList.size() != 0) {
                        mRVMentions.setVisibility(View.VISIBLE);
                    }else{
                        mRVMentions.setVisibility(View.GONE);
                    }

                    notifyDataSetChanged();
                }

            }

        }
    }

}



