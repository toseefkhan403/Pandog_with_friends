package com.android.toseefkhan.pandog.Share;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.toseefkhan.pandog.Map.MapActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.Search.SearchActivity;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.EditProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.GridImageAdapter;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.Permissions;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";
    //constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 5;
    private static final int ACTIVITY_SELECT_IMAGE = 1234;
    private Context mContext = ShareActivity.this;
    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private TextView tvNext;
    private ImageView camera;
    private String mSelectedImage;
    private String mCurrentPhotoPath;
    private Uri capturedImageUri;
    // private Spinner directorySpinner;
    private View mOpenGalleryImage;
    private View mOpenCameraImage;

    //vars
    private ArrayList<String> directories;
    private String mAppend = "file:/";
    private User mChosenUser;
    private Intent getIntent;
    private String mChallengeKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        setupBottomNavigationView();

        gridView = findViewById(R.id.gridView);
        mOpenGalleryImage= findViewById(R.id.gallery_button);
        mOpenCameraImage= findViewById(R.id.camera_button);
        galleryImage = findViewById(R.id.galleryImageView);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        tvNext = findViewById(R.id.tvNext);

        Intent i = getIntent();

        getIntent = getIntent();
        if (i.hasExtra("chosen_user")){
            Log.d(TAG, "onCreate: the user " + i.getExtras());

            mChosenUser = getUserFromBundle();
            Log.d(TAG, "onCreate: the chosen user " + mChosenUser);

            //todo this user should be automatically selected in the NextActivity
            Toast.makeText(mContext, "Please upload a photo to compete with: " + mChosenUser.getUsername() , Toast.LENGTH_SHORT).show();
        }


        mOpenGalleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to gallery");
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: taking user to the next share screen where he can choose amongst his friends");
                Uri mSelectedImageUri = Uri.fromFile(new File(mSelectedImage));
                if (isRootTask()) {
                    Intent intent = new Intent(mContext, NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImageUri.toString());
                    intent.putExtra("challenger_user",mChosenUser);
                    if (getIntent.hasExtra("post_task")){
                        Log.d(TAG, "onClick: do you have the challengekey " + getIntent.getStringArrayListExtra("post_task"));
                        intent.putStringArrayListExtra("post_task",getIntent.getStringArrayListExtra("post_task"));
                    }
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, EditProfileActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImageUri.toString());
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    finish();
                }
            }
        });


        camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File imageFile = null;
                    try {
                        imageFile = createUniqueImageFile();
                    } catch (IOException exception) {
                        Log.w("File error", exception.toString());
                    }
                    if (imageFile != null) {
                        capturedImageUri = FileProvider.getUriForFile(mContext,
                                "com.example.android.fileprovider"
                                , imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }
                }
            }
        });

        mOpenCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File imageFile = null;
                    try {
                        imageFile = createUniqueImageFile();
                    } catch (IOException exception) {
                        Log.w("File error", exception.toString());
                    }
                    if (imageFile != null) {
                        capturedImageUri = FileProvider.getUriForFile(mContext,
                                "com.example.android.fileprovider"
                                , imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }
                }
            }
        });


        try {
            if (checkPermissionsArray(Permissions.PERMISSIONS)) {
                Log.d(TAG, "onCreate: All the permissions are granted.");
                getFilePaths();

            } else {
                askPermissions(Permissions.PERMISSIONS);
            }

        } catch (Exception e) {
            Log.d(TAG, "onCreate: caught a exception" + e.getMessage());
        }

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

    private User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: " + getIntent().getExtras());

            Bundle bundle = getIntent().getExtras();

            if(bundle != null){
                return bundle.getParcelable("chosen_user");
            }else{
                return null;
            }
    }

    private int getTask() {
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    private boolean isRootTask() {
        if (((ShareActivity) mContext).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void setupImageGrid(ArrayList<String> filepaths) {
        final ArrayList<String> filep = filepaths;

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / 4;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_image_view, mAppend, filep);
        gridView.setAdapter(adapter);

        //set the first image to be displayed when the activity fragment view is inflated
        try {
            setImage(filep.get(0), galleryImage, mAppend);
            mSelectedImage = filep.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "setupImageGrid: exception" + e.getMessage());
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an image: " + filep.get(position));
                setImage(filep.get(position), galleryImage, mAppend);
                mSelectedImage = filep.get(position);
            }
        });
    }


    private void setImage(String filepaths, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + filepaths, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isPermissionsGranted = true;
        if (requestCode == VERIFY_PERMISSIONS_REQUEST) {
            for (int granted : grantResults) {
                if (granted == PackageManager.PERMISSION_DENIED) {
                    isPermissionsGranted = false;
                }
            }
        }
        if (isPermissionsGranted) {
            getFilePaths();
        } else {
            Toast.makeText(this, "Need to grant Permission to work properly", Toast.LENGTH_SHORT).show();
        }
    }


    public ArrayList<String> getFilePaths() {
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<>();
        ArrayList<String> resultIAV = new ArrayList<>();

        String[] directories = null;
        if (u != null) {
            c = managedQuery(u, projection, null, null, null);
        }

        if ((c != null) && (c.moveToFirst())) {
            do {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();

                    }
                    if (imagePath.getName().contains(".jpg") || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg") || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                            ) {

                        String path = imagePath.getAbsolutePath();
                        resultIAV.add(path);

                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        setupImageGrid(resultIAV);


        return resultIAV;

    }

    private File createUniqueImageFile() throws IOException {
        // Create an image file name
        long timeStamp = System.currentTimeMillis();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getPath();
        return image;
    }

    /**
     * for retrieving images taken by the camera
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTIVITY_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    if (isRootTask()) {
                        try {
                            Log.d(TAG, "onActivityResult: received new bitmap from gallery: " + selectedImageUri);
                            Intent intent = new Intent(mContext, NextActivity.class);
                            intent.putExtra(getString(R.string.selected_image), selectedImageUri.toString());
                            intent.putExtra("challenger_user",mChosenUser);
                            if (getIntent.hasExtra("post_task")){
                                intent.putStringArrayListExtra("post_task",getIntent.getStringArrayListExtra("post_task"));
                            }
                            startActivity(intent);
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                        }
                    } else {
                        try {
                            Log.d(TAG, "onActivityResult: received new bitmap from gallery: " + selectedImageUri);
                            Intent intent = new Intent(mContext, EditProfileActivity.class);
                            intent.putExtra(getString(R.string.selected_image), selectedImageUri.toString());
                            intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                            startActivity(intent);
                            finish();
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                        }
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult: request cancelled");
                }
                break;

            case CAMERA_REQUEST_CODE:
                Log.d(TAG, "onActivityResult: done taking a photo.");
                Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");

                if (resultCode == RESULT_OK) {
                if (isRootTask()) {
                    try {
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + capturedImageUri);
                        Intent intent = new Intent(mContext, NextActivity.class);
                        intent.putExtra(getString(R.string.selected_image), capturedImageUri.toString());
                        intent.putExtra("challenger_user",mChosenUser);
                        if (getIntent.hasExtra("post_task")){
                            intent.putStringArrayListExtra("post_task",getIntent.getStringArrayListExtra("post_task"));
                        }
                        startActivity(intent);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                } else {
                    try {
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + capturedImageUri);
                        Intent intent = new Intent(mContext, EditProfileActivity.class);
                        intent.putExtra(getString(R.string.selected_image), capturedImageUri.toString());
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        finish();
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                    }
                }
                }else if (resultCode== RESULT_CANCELED){

                }
                break;

        }

    }


    /* ------------------------------------------PERMISSIONS------------------------------------*/

    public void askPermissions(String[] permissions) {
        Log.d(TAG, "askingPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (String check : permissions) {
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, ShareActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
