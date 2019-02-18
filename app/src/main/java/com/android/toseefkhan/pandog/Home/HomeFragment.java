package com.android.toseefkhan.pandog.Home;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.Like;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

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

    /*
       The main feed list only displays posts from your following and your posts.
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRVPosts = view.findViewById(R.id.posts_recycler_view_list);
        mRVPosts.setItemViewCacheSize(20);
        mRVPosts.setDrawingCacheEnabled(true);
        mRVPosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        pb = view.findViewById(R.id.pb);
        noPosts = view.findViewById(R.id.no_posts);
        mRVPosts.setLayoutManager(new ViewPagerLayoutManager(getActivity(),OrientationHelper.VERTICAL));

        mRVPosts.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getFollowing();
        }

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
            public void onDataChange(DataSnapshot dataSnapshot) {
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
        for(int i = 0; i < mPostKeysList.size(); i++){
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

                    if(count >= mPostKeysList.size() -1){
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


    private void displayPhotos(){
        mPaginatedPosts = new ArrayList<>();

        mRVPosts.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);

        if(!mPostList.isEmpty()){
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
                    mAdapter = new PostsProfileRVAdapter(getActivity(), mPaginatedPosts);
                    mRVPosts.setAdapter(mAdapter);
                    Log.d(TAG, "displayPhotos: i am making it this far and let's see what paginated posts has " + mPaginatedPosts.size());
                }
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }else{
            noPosts.setVisibility(View.VISIBLE);
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
                mAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }
}
