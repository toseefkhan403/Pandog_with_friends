package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.dingmouren.layoutmanagergroup.echelon.EchelonLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.StringManipulation;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext = SearchActivity.this;
    private RecyclerView profilesListView;
    private ArrayList<Post> mPostList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final RecyclerView vertical = findViewById(R.id.holder_vertical);
        vertical.setLayoutManager(new ViewPagerLayoutManager(mContext, OrientationHelper.VERTICAL));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            Post post = new Post();
                            HashMap<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

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
                            post.setStatus(objectMap.get("status").toString());
                            post.setTimeStamp(Long.parseLong(objectMap.get("timeStamp").toString()));

                            post.setPostKey(objectMap.get("postKey").toString());

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

                            mPostList.add(post);
                            Log.d(TAG, "onDataChange: singlesnapshot.getValue " + post);
                        }
                        VerticalRecyclerViewAdapter adapter = new VerticalRecyclerViewAdapter(mContext, mPostList);
                        vertical.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        SearchView profileSearchView = findViewById(R.id.searchProfiles);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profilesListView = findViewById(R.id.ProfileList);
        final SearchAdapter adapter = new SearchAdapter(mContext, user.getUid());
        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(adapter);
        a.setDuration(800);
        profilesListView.setAdapter(a);
        profilesListView.setItemAnimator(new SlideInUpAnimator());

        profilesListView.setLayoutManager(new EchelonLayoutManager(mContext));

        profileSearchView.setActivated(true);
        profileSearchView.onActionViewExpanded();
        profileSearchView.setQueryHint("Search here...");

        profileSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                vertical.setVisibility(View.GONE);
                return true;
            }
        });


        setupBottomNavigationView();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(), "You are not online!", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, SearchActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchItemViewHolder> {

        String userUID;
        private ProfileFilter filter;
        private Context mContext;
        private FirebaseDatabase firebaseDatabase;
        private DatabaseReference databaseReference;
        private ArrayList<User> ProfileList;

        public SearchAdapter(Context context, String uid) {
            this.mContext = context;
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference();
            ProfileList = new ArrayList<>();
            this.userUID = uid;
        }

        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.search_profile_item, viewGroup, false);
            return new SearchItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder searchItemViewHolder, int i) {

            User user = getItem(i);
            searchItemViewHolder.userNameView.setText(StringManipulation.expandUsername(user.getUsername()));
            searchItemViewHolder.userEmailView.setText(user.getEmail());
            String PhotoUrl = user.getProfile_photo();
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.photoView, null, "",
                    searchItemViewHolder.child);
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.userppSearch, null, "",
                    null);

            searchItemViewHolder.mView.setOnClickListener(view -> {
                //navigate to view profile activity
                Intent intent = new Intent(SearchActivity.this, ViewProfileActivity.class);
                intent.putExtra(getString(R.string.intent_user), ProfileList.get(i));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent);
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
            TextView userEmailView;
            ImageView photoView;
            CircleImageView userppSearch;
            View child;

            public SearchItemViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mView = itemView;
                userNameView = itemView.findViewById(R.id.UserNameView);
                userEmailView = itemView.findViewById(R.id.UserEmailView);
                photoView = itemView.findViewById(R.id.UserProfilePictureView);
                child = itemView.findViewById(R.id.progress_child);
                userppSearch = itemView.findViewById(R.id.userppSearch);
            }
        }

        private class ProfileFilter {

            public ProfileFilter() {
            }


            public void filter(final CharSequence constraint) {

                if (constraint != null && constraint.length() > 0) {
                    final ArrayList<User> users = new ArrayList<>();
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot childData : dataSnapshot.getChildren()) {
                                    User user = childData.getValue(User.class);
                                    String Username = user.getUsername().toUpperCase();
                                    String entered = constraint.toString().toUpperCase();
                                    if (Username.contains(entered) && !user.getUser_id().equals(userUID)) {
                                        users.add(user);
                                    }
                                }
                                ProfileList = users;
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

        }
    }
}
