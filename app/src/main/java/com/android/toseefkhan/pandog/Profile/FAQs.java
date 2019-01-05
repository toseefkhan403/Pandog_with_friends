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
import java.util.Collections;
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
        setupBottomNavigationView();

        expandableListView = findViewById(R.id.expandable_lv);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        List<String> questions = new ArrayList<>();
   //     List<String> answers = new ArrayList<>();
        HashMap<String,List<String>> hashMap = new HashMap<>();

        String heading_questions[] = getResources().getStringArray(R.array.header_questions);
        String answers_array[] = getResources().getStringArray(R.array.answers);

        for (String title : heading_questions){
            questions.add(title);
        }

        List<String> an = new ArrayList<>();
        List<String> an2 = new ArrayList<>();
        List<String> an3 = new ArrayList<>();
        List<String> an4 = new ArrayList<>();
        List<String> an5 = new ArrayList<>();
        List<String> an6 = new ArrayList<>();
        List<String> an7 = new ArrayList<>();
        List<String> an8 = new ArrayList<>();
        List<String> an9 = new ArrayList<>();
        List<String> an10 = new ArrayList<>();
        List<String> an11 = new ArrayList<>();
        List<String> an12 = new ArrayList<>();

        an.add(answers_array[0]);
        an2.add(answers_array[1]);
        an3.add(answers_array[2]);
        an4.add(answers_array[3]);
        an5.add(answers_array[4]);
        an6.add(answers_array[5]);
        an7.add(answers_array[6]);
        an8.add(answers_array[7]);
        an9.add(answers_array[8]);
        an10.add(answers_array[9]);
        an11.add(answers_array[10]);
        an12.add(answers_array[11]);

        hashMap.put(questions.get(0),an);
        hashMap.put(questions.get(1),an2);
        hashMap.put(questions.get(2),an3);
        hashMap.put(questions.get(3),an4);
        hashMap.put(questions.get(4),an5);
        hashMap.put(questions.get(5),an6);
        hashMap.put(questions.get(6),an7);
        hashMap.put(questions.get(7),an8);
        hashMap.put(questions.get(8),an9);
        hashMap.put(questions.get(9),an10);
        hashMap.put(questions.get(10),an11);
        hashMap.put(questions.get(11),an12);

        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(questions,hashMap,this);
        expandableListView.setAdapter(adapter);
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
