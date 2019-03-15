package com.android.toseefkhan.pandog.Utils;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Comment;
import com.android.toseefkhan.pandog.models.Post;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class ViewLikesFragment extends Fragment {

    private static final String TAG = "ViewLikesFragment";
    private RecyclerView recyclerView;
    private List<String> userIds = new ArrayList<>();
    private ProgressBar pb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_likes, container, false);

        recyclerView = view.findViewById(R.id.likes1);
        pb = view.findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        try {

            Bundle b = getArguments();
            Post post = b.getParcelable("intent_post");

            if (post != null) {
                for (Like like : post.getLikes()) {
                    userIds.add(like.getUser_id());
                }
            }else {
                Log.d(TAG, "onCreateView: displaying likes from comments");

                Comment comment =  b.getParcelable("comment_likes");

                if (comment.getLikes() == null){

                    pb.setVisibility(View.GONE);
                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "NO LIKES FOUND!", Snackbar.LENGTH_LONG).show();
                }else {
                    for (Like like : comment.getLikes()) {
                        userIds.add(like.getUser_id());
                    }
                }
            }

            initRecyclerView();

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointer " + e.getMessage());
        }


        return view;
    }

    private void initRecyclerView() {

        Log.d(TAG, "initRecyclerView: useridlist " + userIds);
        if (!userIds.isEmpty()) {
            Collections.reverse(userIds);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            UserListRVAdapter adapter = new UserListRVAdapter(getActivity(), userIds,pb);
            AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(adapter);
            a.setDuration(1750);
            a.setInterpolator(new OvershootInterpolator());
            recyclerView.setAdapter(a);

        } else if (userIds.isEmpty()) {
            try {
                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "NO LIKES FOUND!", Snackbar.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                Toasty.warning(getActivity(), "NO LIKES FOUND!", Toast.LENGTH_SHORT,true).show();
            }
            pb.setVisibility(View.GONE);
        }
    }

}
