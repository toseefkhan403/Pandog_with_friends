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
import es.dmoral.toasty.Toasty;

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
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.InitialSetup;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.Utils.PullToRefreshView;
import com.android.toseefkhan.pandog.Utils.StringManipulation;
import com.android.toseefkhan.pandog.models.Challenge;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.MyMention;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserDistance;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Random;


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
                getActivity().finish();
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

    private void doYourStuff() {

        Toasty.warning(getActivity(),"Beware some cool stuff is going on...", Toasty.LENGTH_LONG,true).show();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

//        //first make those users
//        String[] arr = {"vandana","vanshikaa","dimple","priyanka","akansha","Pratham","Anushree","Pari Baisla"};
//        String[] arr2 = {"https://scontent-lga3-1.cdninstagram.com/vp/841bc541d58cd84b9bc72c92920e55ee/5D0CCEF7/t51.2885-15/e35/51683097_1659167614389949_275401605049686217_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com"
//                ,"https://scontent-lga3-1.cdninstagram.com/vp/1a2b14829622a9579e09fd37499e9e95/5D23EAA9/t51.2885-15/e35/s1080x1080/53020543_369931253859293_9142302599771098161_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com",
//                "https://scontent-lga3-1.cdninstagram.com/vp/3c424cca9339d8562941a7073c4a1f59/5D079DB4/t51.2885-15/e35/52803983_360796681175705_4926478388603649303_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com",
//                "https://scontent-lga3-1.cdninstagram.com/vp/66a4b3b89c663870a893fda255fb61eb/5D2271C5/t51.2885-15/e35/47584546_757678054605983_6621485973782425517_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com",
//                "https://scontent-lga3-1.cdninstagram.com/vp/b8810186e3a51e96ce6f5ee42c919bbb/5D161A35/t51.2885-15/e35/50843378_426586281419341_8782679174938773412_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com"
//                ,"https://scontent-lga3-1.cdninstagram.com/vp/9d1f951f128f7eb38b057ce3673be616/5D1378AF/t51.2885-15/e35/50167465_198814314427275_5100747287541511205_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com"
//                ,"https://scontent-lga3-1.cdninstagram.com/vp/5913ea71abd391d765dbf4567c05e4b5/5D26C9D1/t51.2885-15/e35/52801086_1547259772043634_5954984716737087169_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com"
//                ,"https://scontent-lga3-1.cdninstagram.com/vp/6d732c4ad6a729374b63e1caced005cd/5D26427F/t51.2885-15/e35/53866545_364834834367670_360804512498698425_n.jpg?_nc_ht=scontent-lga3-1.cdninstagram.com"};
//
//        for (int i = 0; i < 8; i++) {
//
//            String userID = "beta_user_" + arr[i];
//            User user = new User( arr2[i],userID,  "",  StringManipulation.condenseUsername(arr[i]),"GREY",0 );
//
//            myRef.child(getString(R.string.dbname_users))
//                    .child(userID)
//                    .setValue(user);
//
//            UserAccountSettings settings = new UserAccountSettings(
//                    "",
//                    arr[i],
//                    arr2[i],
//                    StringManipulation.condenseUsername(arr[i])
//            );
//
//            myRef.child(getString(R.string.dbname_user_account_settings))
//                    .child(userID)
//                    .setValue(settings);
//        }
//
//        //follow all the people you haven't.
//        myRef.child(getString(R.string.dbname_users))
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        for (DataSnapshot ds: dataSnapshot.getChildren()){
//
//                            User user = ds.getValue(User.class);
//
//                            if (!user.getUser_id().contains("beta")) {
//
//                                for (int i = 0; i < 8; i++) {
//
//                                    FirebaseDatabase.getInstance().getReference()
//                                            .child(getString(R.string.dbname_followers))
//                                            .child(user.getUser_id())
//                                            .child("beta_user_" + arr[i])
//                                            .child(getString(R.string.field_user_id))
//                                            .setValue("beta_user_" + arr[i]);
//
//                                    FirebaseDatabase.getInstance().getReference()
//                                            .child(getString(R.string.dbname_following))
//                                            .child("beta_user_" + arr[i])
//                                            .child(user.getUser_id())
//                                            .child(getString(R.string.field_user_id))
//                                            .setValue(user.getUser_id());
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

        //challenge users!
        ArrayList<String> userIdnames = new ArrayList<>();
        HashMap<String,ArrayList<String>> photoIds = new HashMap<>();

        myRef.child("beta_photos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()){

                            HashMap<String,Object> objectHashMap =(HashMap<String,Object>) ds.getValue();

                            userIdnames.add(objectHashMap.get("name").toString());

                            ArrayList<String> stringArrayList = new ArrayList<>();
                            stringArrayList.add(objectHashMap.get("photo_url").toString());
                            stringArrayList.add(objectHashMap.get("photo_url1").toString());
                            stringArrayList.add(objectHashMap.get("photo_url2").toString());
                            stringArrayList.add(objectHashMap.get("photo_url3").toString());

                            photoIds.put(objectHashMap.get("name").toString(),stringArrayList);
                        }



                        myRef.child(getString(R.string.dbname_users))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot ds : dataSnapshot.getChildren()){

                                            User user = ds.getValue(User.class);

                                            for (int i = 0; i < 2; i++) {

                                                int rand0to7 = new Random().nextInt(8);
                                                int rand0to3 = new Random().nextInt(4);

                                                Log.d(TAG, "onDataChange: the random numbers are " + rand0to7 + " " + rand0to3);

                                                final String currentUserUid = userIdnames.get(rand0to7);
                                                final String selecteduserUid = user.getUser_id();
                                                final Challenge mChallenge = new Challenge(currentUserUid, selecteduserUid, photoIds.get(userIdnames.get(rand0to7)).get(rand0to3),"","");
                                                mChallenge.setStatus("NOT_DECIDED");
                                                final String challengedUserName = user.getUsername();
                                                myRef.child(getString(R.string.dbname_users))
                                                        .child(currentUserUid)
                                                        .child("username")
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    String challengerUserName = dataSnapshot.getValue(String.class);
                                                                    mChallenge.setChallengedName(challengedUserName);
                                                                    mChallenge.setChallengerName(challengerUserName);
                                                                    String challengeKey = myRef.child("Challenges").push().getKey();
                                                                    mChallenge.setChallengeKey(challengeKey);
                                                                    myRef.child("Challenges").child(challengeKey).setValue(mChallenge);

                                                                    DatabaseReference challengeReference = myRef.child("User_Challenges");
                                                                    challengeReference.child(currentUserUid).child(challengeKey).setValue(challengeKey);
                                                                    challengeReference.child(selecteduserUid).child(challengeKey).setValue(challengeKey);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }
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

    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");

        if (isAdded()) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
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
                                HashMap<String, Object> objectHashMap = (HashMap<String, Object>) dSnapshot.getValue();

                                comment.setUser_id(objectHashMap.get("user_id").toString());
                                comment.setComment(objectHashMap.get("comment").toString());
                                comment.setCommentID(objectHashMap.get("commentID").toString());

                                ArrayList<Like> co = new ArrayList<>();
                                for (DataSnapshot dSnapshot2 : dSnapshot
                                        .child("likes").getChildren()) {
                                    Like like = new Like();
                                    like.setUser_id(dSnapshot2.getValue(Like.class).getUser_id());
                                    co.add(like);
                                }
                                comment.setLikes(co);

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
                getActivity().finish();

                break;

            case 1:
                if (mAdapter != null) {
                    mAdapter.sharePost();
                }

                break;

            case 2:
             //   doYourStuff();
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.pull,R.anim.push);
                break;

            case 3:
                mRVPosts.smoothScrollToPosition(0);

                break;

            case 4:
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
//                        Intent i = new Intent(getActivity(),HomeActivity.class);
//                        startActivity(i);
//                        getActivity().overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
//                        getActivity().finish();
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
                    .setLabel("Scroll to top")
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_up_arrow))
                    .setIconNormalColor(getActivity().getResources().getColor(R.color.pink_400))
                    .setIconPressedColor(0xff1a237e)
                    .setLabelColor(0xff283593)
                    .setWrapper(3)
            );
            items.add(new RFACLabelItem<Integer>()
                    .setLabel("Disable this button")
                    .setDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close))
                    .setIconNormalColor(getActivity().getResources().getColor(R.color.light_blue_400))
                    .setIconPressedColor(0xff1a237e)
                    .setLabelColor(0xff283593)
                    .setWrapper(4)
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
