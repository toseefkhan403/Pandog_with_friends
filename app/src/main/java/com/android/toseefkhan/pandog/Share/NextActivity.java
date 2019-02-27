package com.android.toseefkhan.pandog.Share;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Utils.BitmapUtils;
import com.android.toseefkhan.pandog.Utils.FragmentPagerAdapter;
import com.android.toseefkhan.pandog.Utils.SpacesItemDecoration;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.fenchtose.nocropper.BitmapResult;
import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NextActivity extends AppCompatActivity implements ThumbnailAdapter.ThumbnailsAdapterListener {

    private static final String TAG = "NextActivity";
    private Context mContext;

    //firebase
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mCaption;
    private ListView friendsListView;

    private Intent intent;
    private ImageView image;
    private Bitmap finalBitmap;

    private FriendsAdapter mFriendsAdapter;

    CropperView imagePreview;
    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    public void onPause() {
        super.onPause();

        mContext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext = null;
    }


    @Override
    protected void onResume() {
        super.onResume();

        mContext = NextActivity.this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mContext = NextActivity.this;

        initImageLoader();
        loadEditScreen();

        Log.d(TAG, "onCreate: got the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        friendsListView = findViewById(R.id.FriendsListView);
        image = findViewById(R.id.imageShare);

        Intent i = getIntent();
        if (i.hasExtra("challenger_user")){
            Bundle b = i.getExtras();
            if(b!=null){
                User user = b.getParcelable("challenger_user");
                Log.d(TAG, "onCreate: user " + user);
                if (user == null){
                    setupFriendsList();
                }else{
                    mFriendsAdapter = new FriendsAdapter(user,mContext);
                    friendsListView.setAdapter(mFriendsAdapter);
                }
            }
        }else{
            setupFriendsList();
        }

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePost();
            }
        });

        setImage();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void loadEditScreen() {

        findViewById(R.id.next_activity).setVisibility(View.GONE);
        findViewById(R.id.r).setVisibility(View.VISIBLE);

        imagePreview = findViewById(R.id.image_preview);
        RecyclerView recyclerView  = findViewById(R.id.recycler_view);

        loadImage();

        List<ThumbnailItem> thumbnailItemList = new ArrayList<>();
        ThumbnailAdapter mAdapter = new ThumbnailAdapter(mContext, thumbnailItemList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        ThumbnailsManager.clearThumbs();
        thumbnailItemList.clear();

        // add normal bitmap first
        Log.d(TAG, "loadEditScreen: original image " + originalImage);
        com.zomato.photofilters.utils.ThumbnailItem thumbnailItem = new com.zomato.photofilters.utils.ThumbnailItem();
        thumbnailItem.image = originalImage;
        thumbnailItem.filterName = getString(R.string.filter_normal);
        ThumbnailsManager.addThumb(thumbnailItem);

        List<Filter> filters = FilterPack.getFilterPack(mContext);

        for (Filter filter : filters) {
            com.zomato.photofilters.utils.ThumbnailItem tI = new com.zomato.photofilters.utils.ThumbnailItem();
            tI.image = originalImage;
            tI.filter = filter;
            tI.filterName = filter.getName();
            ThumbnailsManager.addThumb(tI);
        }

        thumbnailItemList.addAll(ThumbnailsManager.processThumbs(mContext));
        mAdapter.notifyDataSetChanged();

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                findViewById(R.id.next_activity).setVisibility(View.VISIBLE);
                findViewById(R.id.r).animate()
                        .translationY(findViewById(R.id.r).getHeight())
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                findViewById(R.id.r).setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });

                imagePreview.getCroppedBitmapAsync(new CropperCallback() {
                    @Override
                    public void onCropped(Bitmap bitmap) {

                        image.setImageBitmap(bitmap);
                        finalBitmap = bitmap;
                    }
                });
            }
        });

        findViewById(R.id.rotate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: changing the rotation");
                imagePreview.fitToCenter();
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap bitmap1 = Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), matrix, true);
                imagePreview.setImageBitmap(bitmap1);
                refreshBitmap();
            }
        });

    }

    private void refreshBitmap() {

        imagePreview.getCroppedBitmapAsync(new CropperCallback() {
            @Override
            public void onCropped(Bitmap bitmap) {
                Log.d(TAG, "onCropped: cropped successfully");
                originalImage = bitmap;
            }
        });
    }

    @Override
    public void onFilterSelected(Filter filter) {

        //todo filter applying is slow for some images
        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));

    }

    private void loadImage() {
        try {
            if (getIntent().hasExtra(getString(R.string.selected_image))) {
                originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(getIntent().getStringExtra(getString(R.string.selected_image))));
            }else if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
                Log.d(TAG, "loadImage: image coming from gallery");
                originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            }
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(originalImage);

        }catch (Exception e){}

    }


    private void sharePost() {
        Log.d(TAG, "onClick: navigating to the final share screen.");
        //upload the image to firebase
        int selectedUserPosition = mFriendsAdapter.getSelectedUserPosition();
        if (selectedUserPosition == -1) {
            Toasty.info(mContext, "Select a user from list first", Toast.LENGTH_SHORT,true).show();
            return;
        }

        Log.d(TAG, "Attempting to upload new photo");
        String caption = mCaption.getText().toString();

        User selectedUser = (User) mFriendsAdapter.getItem(selectedUserPosition);
        intent = getIntent();

        //saves the bitmap in memory. Not good
        Uri myUri = bitmapToUriConverter(finalBitmap);
        Log.d(TAG, "onClick: this is the uri from the intent " + myUri);

        long timeStamp = System.currentTimeMillis();
        String imageName = "photo" + timeStamp;

        if (getIntent().hasExtra("post_task")){
            String challengeKey = getIntent().getStringArrayListExtra("post_task").get(0);
            mFirebaseMethods.uploadNewPhoto(getString(R.string.post_photo),caption,imageName,null,myUri, selectedUser, challengeKey);
        }else{
            mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageName, null, myUri, selectedUser);
        }

    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        intent = getIntent();
        //Image view that is present in the NextActivity

        if (intent.hasExtra(getString(R.string.selected_image))) {
            //vars
            String imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image uri: " + imgUrl);

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(imgUrl, image);
        }
    }

    private void setupFriendsList() {
        mFriendsAdapter = new FriendsAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid(), mContext);
        friendsListView.setAdapter(mFriendsAdapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private Uri bitmapToUriConverter(Bitmap mBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), mBitmap, "CelfieImage", null);
        return Uri.parse(path);
    }
}