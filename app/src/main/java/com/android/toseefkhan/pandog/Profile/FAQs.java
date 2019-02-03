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
import java.util.HashMap;
import java.util.List;


public class FAQs extends AppCompatActivity{

    private static final String TAG = "FAQs";
    private static final int ACTIVITY_NUM=4;


//    private LinearLayout linearLayout;
//    private Button saveBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        setupBottomNavigationView();

//        linearLayout = (LinearLayout) findViewById(R.id.logo_image);
//        saveBtn = (Button) findViewById(R.id.buttonSave);
//        saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                File file = saveBitMap(FAQs.this, linearLayout);    //which view you want to pass that view as parameter
//                if (file != null) {
//                    Toast.makeText(FAQs.this, "successful", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(FAQs.this, "unsuccessful", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        ExpandableListView expandableListView = findViewById(R.id.expandable_lv);

//        final VideoView vv = findViewById(R.id.vv);
//        Uri uri= Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoone);
//        vv.setVideoURI(uri);
//
//        vv.start();
//        vv.setZOrderOnTop(true);
//        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                vv.start();
//                vv.setZOrderOnTop(true);
//            }
//        });


        List<String> questions = new ArrayList<>();
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

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

//
//    private File saveBitMap(Context context, View drawView){
//        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Handcare");
//        if (!pictureFileDir.exists()) {
//            boolean isDirectoryCreated = pictureFileDir.mkdirs();
//            if(!isDirectoryCreated)
//                Log.i("ATG", "Can't create directory to save the image");
//            return null;
//        }
//        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
//        File pictureFile = new File(filename);
//        Bitmap bitmap =getBitmapFromView(drawView);
//        try {
//            pictureFile.createNewFile();
//            FileOutputStream oStream = new FileOutputStream(pictureFile);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
//            oStream.flush();
//            oStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.i("TAG", "There was an issue saving the image.");
//        }
//        scanGallery( context,pictureFile.getAbsolutePath());
//        return pictureFile;
//    }
//
//    //create bitmap from view and returns it
//    private Bitmap getBitmapFromView(View view) {
//        //Define a bitmap with the same size as the view
//        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
//        //Bind a canvas to it
//        Canvas canvas = new Canvas(returnedBitmap);
//        //Get the view's background
//        Drawable bgDrawable =view.getBackground();
//        if (bgDrawable!=null) {
//            //has background drawable, then draw it on the canvas
//            bgDrawable.draw(canvas);
//        }   else{
//            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
//        }
//        // draw the view on the canvas
//        view.draw(canvas);
//        //return the bitmap
//        return returnedBitmap;
//    }
//
//    // used for scanning gallery
//    private void scanGallery(Context cntx, String path) {
//        try {
//            MediaScannerConnection.scanFile(cntx, new String[] { path },null, new MediaScannerConnection.OnScanCompletedListener() {
//                public void onScanCompleted(String path, Uri uri) {
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(this, bottomNavigationViewEx,FAQs.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
