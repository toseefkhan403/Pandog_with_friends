package com.android.toseefkhan.pandog.Profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.ExpandableListViewAdapter;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class FAQs extends AppCompatActivity{

    private static final String TAG = "FAQs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        ExpandableListView expandableListView = findViewById(R.id.expandable_lv);


        HashMap<String,List<String>> hashMap = new HashMap<>();

        String heading_questions[] = getResources().getStringArray(R.array.header_questions);
        String answers_array[] = getResources().getStringArray(R.array.answers);

        List<String> questions = new ArrayList<>(Arrays.asList(heading_questions));

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

        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(questions,hashMap,this);
        expandableListView.setAdapter(adapter);

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

}
