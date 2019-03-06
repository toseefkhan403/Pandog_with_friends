package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.EditProfileActivity;
import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.PullToRefreshView;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserDistance;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.CustomTarget;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    private static final String TAG = "HomeFragment";

    private RecyclerView mRVPosts;
    private ProgressBar pb;
    private TextView noPosts;
    private PostsProfileRVAdapter mAdapter;
    private ArrayList<Post> mPostList = new ArrayList<>();
    private ArrayList<String> mFollowing = new ArrayList<>();
    private ArrayList<String> mPostKeysList = new ArrayList<>();
    private ArrayList<Post> mPaginatedPosts;
    private int mResults;

    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaBtn;
    private RapidFloatingActionHelper rfabHelper;

    SharedPreferences mPrefs;
    final String horizontalScreenEnabled = "horizontalScreenEnabled";
    final String showFloatingButton = "showFloatingButton";
    boolean horizontalScrollingEnabled;
    boolean isshowFloatingButton;

    /*
       The main feed list only displays posts from your following and your posts.
     */

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRVPosts = view.findViewById(R.id.posts_recycler_view_list);

        PullToRefreshView mPullToRefreshView = view.findViewById(R.id.pull_to_refresh);

        mPullToRefreshView.setOnRefreshListener(() -> mPullToRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshView.setRefreshing(false);
                Intent i = new Intent(getActivity(),HomeActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }, 1000));


        rfaLayout = view.findViewById(R.id.activity_main_rfal);
        rfaBtn = view.findViewById(R.id.activity_main_rfab);

        pb = view.findViewById(R.id.pb);
        noPosts = view.findViewById(R.id.no_posts);
        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(getActivity(),OrientationHelper.VERTICAL));
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        horizontalScrollingEnabled = mPrefs.getBoolean(horizontalScreenEnabled, true);
        isshowFloatingButton = mPrefs.getBoolean(showFloatingButton,true);

        mRVPosts.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getFollowing();
        }

        if (isshowFloatingButton)
            setupFloatingButton();
        else
            rfaBtn.setVisibility(View.GONE);


        return view;
    }

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get the photos
                getPhotosKeys();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPhotosKeys() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for (int i = 0 ; i < mFollowing.size() ; i++){
            final int count = i;
            Query query = reference
                    .child("user_posts")
                    .child(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " +
                                singleSnapshot.getValue(String.class));

                        if (!mPostKeysList.contains(singleSnapshot.getValue(String.class)))
                            mPostKeysList.add(singleSnapshot.getValue(String.class));
                    }
                    if(count >= mFollowing.size() -1){
                        //get the photos
                        getPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        if (mPostKeysList.size() == 0){

            pb.setVisibility(View.GONE);
            noPosts.setVisibility(View.VISIBLE);
            noPosts.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Comic Neue.ttf"));
            rfaBtn.setVisibility(View.GONE);
        }else {
            for (int i = 0; i < mPostKeysList.size(); i++) {
                final int count = i;
                Query query = reference
                        .child("Posts")
                        .child(mPostKeysList.get(i));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Post post = new Post();
                        if (dataSnapshot.exists()) {
                            HashMap<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            post.setStatus(objectMap.get("status").toString());

                            if (post.getStatus().equals("INACTIVE"))
                                post.setWinner(objectMap.get("winner").toString());

                            post.setImage_url(objectMap.get("image_url").toString());
                            post.setImage_url2(objectMap.get("image_url2").toString());

                            post.setCaption(objectMap.get("caption").toString());
                            post.setCaption2(objectMap.get("caption2").toString());

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
                            for (DataSnapshot dSnapshot : dataSnapshot
                                    .child("likes").getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList.add(like);
                            }
                            post.setLikes(likesList);

                            List<Like> likesList2 = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : dataSnapshot
                                    .child("likes2").getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList2.add(like);
                            }
                            post.setLikes2(likesList2);

                            List<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : dataSnapshot
                                    .child("comments").getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comments.add(comment);
                            }
                            post.setComments(comments);

                            mPostList.add(post);
                            Log.d(TAG, "onDataChange: contents of the postlist " + mPostList.size());
                        }

                        if (count >= mPostKeysList.size() - 1) {
                            //display our photos
                            displayPhotos();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    private void displayPhotos(){
        Log.d(TAG, "displayPhotos: i am being called.");
        mPaginatedPosts = new ArrayList<>();

        mRVPosts.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);

        if(!mPostList.isEmpty()){
            Log.d(TAG, "displayPhotos: List is not empty");
            try{
                Collections.sort(mPostList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post, Post t1) {
                        return Long.compare(t1.getTimeStamp(),post.getTimeStamp());
                    }
                });

                Log.d(TAG, "displayPhotos: postslist now" + mPostList);

                int iterations = mPostList.size();

                if(iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for(int i = 0; i < iterations; i++){
                    Log.d(TAG, "displayPhotos: adding posts to paginated posts");
                    mPaginatedPosts.add(mPostList.get(i));
                }

                if (getActivity() != null) {

                    if (horizontalScrollingEnabled) {
                        mAdapter = new PostsProfileRVAdapter(getActivity(), mPaginatedPosts);
                        mRVPosts.setAdapter(mAdapter);
                    }else{
                        mAdapter = new PostsProfileRVAdapter(getActivity(), mPaginatedPosts, true);
                        mRVPosts.setAdapter(mAdapter);
                    }
                    Log.d(TAG, "displayPhotos: i am making it this far and let's see what paginated posts has " + mPaginatedPosts.size());
                }
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mPostList.size() > mResults && mPostList.size() > 0){

                int iterations;
                if(mPostList.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPostList.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPosts.add(mPostList.get(i));
                }
                mResults = mResults + iterations;

                mRVPosts.post(new Runnable()
                {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {

    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {

        switch (position){

            case 0:
                Log.d(TAG, "onRFACItemIconClick: toggling horizontal off.");

                if (horizontalScrollingEnabled) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(horizontalScreenEnabled, false);
                    editor.apply();
                }else{
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean(horizontalScreenEnabled, true);
                    editor.apply();
                }

                Intent i = new Intent(getActivity(),HomeActivity.class);
                getActivity().startActivity(i);
                getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                break;

            case 1:
                mAdapter.sharePost();
                break;

            case 2:
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.pull,R.anim.push);
                break;

            case 3:
                spotlight();
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(showFloatingButton, false);
                editor.apply();
                rfaBtn.setVisibility(View.GONE);

                break;
        }

        rfabHelper.toggleContent();
    }

    private void spotlight() {

        View first = LayoutInflater.from(getActivity()).inflate(R.layout.overlay_shake_device, new FrameLayout(getActivity()));

        CustomTarget homeView = new CustomTarget.Builder(getActivity())
                .setPoint(0f,0f)
                .setShape(new Circle(0f))
                .setOverlay(first)
                .setOnSpotlightStartedListener(new OnTargetStateChangedListener<CustomTarget>() {
                    @Override
                    public void onStarted(CustomTarget target) {
                        // do something
                    }
                    @Override
                    public void onEnded(CustomTarget target) {
                        // do something
                    }
                })
                .build();

        Spotlight spotlight = Spotlight.with(getActivity())
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setTargets(homeView)
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                    @Override
                    public void onStarted() {

                    }

                    @Override
                    public void onEnded() {
                        Intent i = new Intent(getActivity(),HomeActivity.class);
                        startActivity(i);
                        getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    }
                });
        spotlight.start();

    }

    private void setupFloatingButton() {

        if (getActivity() != null) {

            RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(getActivity());
            rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
            List<RFACLabelItem> items = new ArrayList<>();
            items.add(new RFACLabelItem<Integer>()
                    .setLabel("Toggle Horizontal Scrolling")
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_flip))
                    .setIconNormalColor(0xffd84315)
                    .setIconPressedColor(0xffbf360c)
                    .setWrapper(0)
            );
            items.add(new RFACLabelItem<Integer>()
                    .setLabel("Share this post")
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_share))
                    .setIconNormalColor(0xff4e342e)
                    .setIconPressedColor(0xff3e2723)
                    .setWrapper(1)
            );
            items.add(new RFACLabelItem<Integer>()
                    .setLabel("Edit Your Profile "+ getEmojiByUnicode(0x1F60E))
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_face))
                    .setIconNormalColor(getActivity().getResources().getColor(R.color.white))
                    .setIconPressedColor(0xff0d5302)
                    .setLabelColor(0xff056f00)
                    .setWrapper(2)
            );
            items.add(new RFACLabelItem<Integer>()
                    .setLabel("Disable this button")
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close))
                    .setIconNormalColor(getActivity().getResources().getColor(R.color.light_blue_400))
                    .setIconPressedColor(0xff1a237e)
                    .setLabelColor(0xff283593)
                    .setWrapper(3)
            );

            rfaContent
                    .setItems(items)
                    .setIconShadowColor(0xff888888);

            rfabHelper = new RapidFloatingActionHelper(getActivity(),rfaLayout,rfaBtn,rfaContent).build();
        }

    }

    private String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}
