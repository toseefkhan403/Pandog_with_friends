package com.android.toseefkhan.pandog.Share;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Profile.EditProfileActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;
import com.android.toseefkhan.pandog.Utils.GridImageAdapter;
import com.android.toseefkhan.pandog.Utils.Permissions;
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
    private TextView mOpenGallery;
    private String mCurrentPhotoPath;
    private Uri capturedImageUri;
    // private Spinner directorySpinner;
    private ImageView mOpenGalleryImage;
    private ImageView mOpenCameraImage;

    //vars
    private ArrayList<String> directories;
    private String mAppend = "file:/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mOpenGalleryImage= findViewById(R.id.gallery_button);
        mOpenCameraImage= findViewById(R.id.camera_button);
        mOpenGallery = findViewById(R.id.open_gallery);
        galleryImage = findViewById(R.id.galleryImageView);
        gridView = findViewById(R.id.gridView);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        tvNext = findViewById(R.id.tvNext);

        mOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to gallery");
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
            }
        });

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
                    Toast.makeText(mContext, "Hey! Don't hesitate to share your photo!", Toast.LENGTH_SHORT).show();
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

        //handle the image that is received from a camera
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


}
