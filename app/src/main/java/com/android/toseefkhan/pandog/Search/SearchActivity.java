package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Map.MapActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Profile.ViewPostsListActivity;
import com.android.toseefkhan.pandog.Utils.InterceptRelativeLayout;
import com.dingmouren.layoutmanagergroup.banner.BannerLayoutManager;
import com.dingmouren.layoutmanagergroup.echelon.EchelonLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext = SearchActivity.this;
    private RecyclerView profilesListView;
    private ArrayList<Post> mPostList = new ArrayList<>();
    private List<ImageView> mImgList = new ArrayList<>();
    private int mLastSelectPosition = 0;


    //for the welcome screen
    SharedPreferences mPrefs;
    final String tutorialScreenShownPrefSearch = "tutorialScreenShownSearch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //setting the banner recycler view on the top
        InterceptRelativeLayout mRelaIntercept1 = findViewById(R.id.rela_intercept_1);
        mRelaIntercept1.setIntercept(false);
        ImageView mImg1 =  findViewById(R.id.img_1);
        ImageView mImg2 =  findViewById(R.id.img_2);
        ImageView mImg3 =  findViewById(R.id.img_3);
        ImageView mImg4 =  findViewById(R.id.img_4);
        mImgList.add(mImg1);
        mImgList.add(mImg2);
        mImgList.add(mImg3);
        mImgList.add(mImg4);


        RecyclerView mRecycler_1 = findViewById(R.id.recycler1);
        MyAdapter myAdapter = new MyAdapter();
        BannerLayoutManager bannerLayoutManager = new BannerLayoutManager(this,mRecycler_1,4,OrientationHelper.HORIZONTAL);
        mRecycler_1.setLayoutManager(bannerLayoutManager);
        mRecycler_1.setAdapter(myAdapter);
        bannerLayoutManager.setOnSelectedViewListener(new BannerLayoutManager.OnSelectedViewListener() {
            @Override
            public void onSelectedView(View view, int position) {
                changeUI(position);
            }
        });
        changeUI(0);

        final RecyclerView vertical = findViewById(R.id.holder_vertical);
        vertical.setLayoutManager(new LinearLayoutManager(mContext));

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

                            if (post.getStatus().equals("INACTIVE"))
                                post.setWinner(objectMap.get("winner").toString());

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
                        ArrayList<ArrayList<String>> arrayLists = new ArrayList<ArrayList<String>>();
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
        TextView t = findViewById(R.id.t);
        ImageView i= findViewById(R.id.trending);

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
                profilesListView.setVisibility(View.VISIBLE);
                t.setVisibility(View.GONE);
                i.setVisibility(View.GONE);
                mRelaIntercept1.setVisibility(View.GONE);

                return true;
            }
        });

        setupBottomNavigationView();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(), "You are not online!", Snackbar.LENGTH_LONG).show();
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // second argument is the default to use if the preference can't be found
        Boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPrefSearch, false);

        if (!welcomeScreenShown) {

            startTutorial();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(tutorialScreenShownPrefSearch, true);
            editor.apply(); // Very important to save the preference
        }

    }

    private void changeUI(int position){
        if (position != mLastSelectPosition) {
            mImgList.get(position).setImageDrawable(getResources().getDrawable(R.drawable.circle_red));
            mImgList.get(mLastSelectPosition).setImageDrawable(getResources().getDrawable(R.drawable.circle_grey));
            mLastSelectPosition = position;
        }
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

            searchItemViewHolder.setIsRecyclable(false);
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


    private void startTutorial() {

        Log.d(TAG, "startTutorial: starting the tutorial");

        new MaterialTapTargetPrompt.Builder(SearchActivity.this)
                .setTarget(findViewById(R.id.trending))
                .setBackgroundColour(getResources().getColor(R.color.background))
                .setAutoDismiss(false)
                .setBackButtonDismissEnabled(false)
                .setPrimaryText("Feature yourself here by following the trending hashtags of the week!")
                .setSecondaryText("And let the world know your awesomeness!")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            new MaterialTapTargetPrompt.Builder(SearchActivity.this)
                                    .setTarget(findViewById(R.id.ic_cloud))
                                    .setBackgroundColour(getResources().getColor(R.color.background))
                                    .setAutoDismiss(false)
                                    .setBackButtonDismissEnabled(false)
                                    .setPrimaryText("Introducing")
                                    .setSecondaryText("Maps to find awesome people living near you!")
                                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                        @Override
                                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                            }
                                        }
                                    }).show();

                        }
                    }
                }).show();

    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


        private Context mContext;
        private ArrayList<String> mUrls;
        private ArrayList<String> mTitles;

        public MyAdapter() {
        }

        public MyAdapter(Context mContext, ArrayList<String> mUrls, ArrayList<String> mTitles) {
            this.mContext = mContext;
            this.mUrls = mUrls;
            this.mTitles = mTitles;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

            holder.setIsRecyclable(false);
            UniversalImageLoader.setImage(mUrls.get(position),holder.img,null,"",null);
            holder.text.setText(mTitles.get(position));
//            holder.img.setImageResource(imgs[position % 4]);
//            holder.text.

            //take to refer screen
            if (position%4 == 0) {
                holder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //take to referral activity
                        Intent i = new Intent(mContext, ReferActivity.class);
                        i.putExtra("position", position);
                        startActivity(i);
                    }
                });
            }else {
                //take the user to viewPostsListActivity where he can see the trending stuff
                Intent i = new Intent(mContext, ViewPostsListActivity.class);
                i.putExtra("trending", holder.text.getText().toString());
                startActivity(i);
            }

        }

        @Override
        public int getItemCount() {
            return mUrls.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView text;
            public ViewHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.img);
                text = itemView.findViewById(R.id.text);
            }
        }
    }

}
