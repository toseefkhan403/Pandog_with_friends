package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.CommentsRVAdapter;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ViewCommentsActivity extends AppCompatActivity{

    private static final String TAG = "ViewCommentsActivity";

    private RecyclerView recyclerView;
    private EditText editComment;
    private Context mContext = ViewCommentsActivity.this;
    private ArrayList<Comment> mComments;

    private Post mPost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.comments_rv);
        editComment = findViewById(R.id.comment);
        editComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if(view != null){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, 0);
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

        mPost = getPostFromBundle();
        getCommentsFromPost();

    }

    private void getCommentsFromPost() {

        mComments.clear();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("Posts")
                .child(mPost.getPostKey())
                .child("comments")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            Comment comment = singleSnapshot.getValue(Comment.class);
                            mComments.add(comment);
                        }
                        initRecyclerView();
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

        myRef.child("Posts")
                .child(mPost.getPostKey())
                .child("comments")
                .child(commentID)
                .setValue(comment);

        getCommentsFromPost();
    }

    private void initRecyclerView() {

        Collections.reverse(mComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        CommentsRVAdapter adapter = new CommentsRVAdapter(mContext, mComments);
        recyclerView.setAdapter(adapter);
    }

    private Post getPostFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getIntent().getExtras());

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            Log.d(TAG, "getPostFromBundle: bundle.getParcelable " + bundle.getParcelable("post_comments"));
            return bundle.getParcelable("post_comments");
        }else{
            return null;
        }
    }



}
