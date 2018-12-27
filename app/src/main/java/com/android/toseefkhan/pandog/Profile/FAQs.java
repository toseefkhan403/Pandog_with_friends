package com.android.toseefkhan.pandog.Profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.ExpandableListViewAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FAQs extends AppCompatActivity{

    private static final String TAG = "FAQs";
    private static final int ACTIVITY_NUM=4;

    private ExpandableListView expandableListView;
    private ImageView backArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        expandableListView = findViewById(R.id.expandable_lv);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        HashMap<String,String> hashMap = new HashMap<>();

        String heading_questions[] = getResources().getStringArray(R.array.header_questions);
        String answers_array[] = getResources().getStringArray(R.array.answers);


        for (String title : heading_questions){
            questions.add(title);
        }

        for (String ans : answers_array){
            answers.add(ans);
        }

        for (int i=0; i<questions.size(); i++){
            hashMap.put(questions.get(i),answers.get(i));
        }

        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(questions,hashMap,this);
        expandableListView.setAdapter(adapter);
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
