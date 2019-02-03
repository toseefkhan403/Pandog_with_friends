package com.android.toseefkhan.pandog.Home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView mRVPosts;
    private ArrayList<Post> mPostList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRVPosts = view.findViewById(R.id.posts_recycler_view_list);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getPostsOnProfile();
        }

        return view;
    }

    private void getPostsOnProfile() {
        Log.d(TAG, "getPostsOnProfile: getting posts.");

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        //todo currently its retrieving all the posts. get only those which the user is related to
        myRef.child("Posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: .getValue " + dataSnapshot.getValue());
                        mPostList.clear();

                        for (DataSnapshot singleSnapshot: dataSnapshot.getChildren() ) {

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

                            mPostList.add(post);
                            Log.d(TAG, "onDataChange: singlesnapshot.getValue " + post);
                        }

                        initRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void initRecyclerView() {

        if (!mPostList.isEmpty()) {
            Collections.reverse(mPostList);
            mRVPosts.setLayoutManager(new ViewPagerLayoutManager(getActivity(), OrientationHelper.VERTICAL));
            PostsProfileRVAdapter adapter = new PostsProfileRVAdapter(getActivity(), mPostList);
            mRVPosts.setAdapter(adapter);
        }else if (mPostList.isEmpty()){
            Log.d(TAG, "initRecyclerView: no the layout should be empty");
            try {
                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "NO POSTS FOUND!", Snackbar.LENGTH_LONG).show();
            }catch (NullPointerException e){
                Toast.makeText(getActivity(), "NO POSTS FOUND!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
