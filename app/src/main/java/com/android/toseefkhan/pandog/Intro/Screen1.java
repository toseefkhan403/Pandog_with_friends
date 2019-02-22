package com.android.toseefkhan.pandog.Intro;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;

public class Screen1 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_screen1,container,false);

        TextView tvNext = view.findViewById(R.id.tvNext);
        TextView tvCelfie = view.findViewById(R.id.celfie);

        if (getActivity() != null) {
            tvNext.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cursive.ttf"));
            tvCelfie.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cursive.ttf"));
        }

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((Holder)getActivity()).gotoFragment(1);
            }
        });


        return view;
    }
}
