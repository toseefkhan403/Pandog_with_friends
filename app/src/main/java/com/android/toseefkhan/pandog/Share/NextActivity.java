package com.android.toseefkhan.pandog.Share;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.FirebaseMethods;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.SpacesItemDecoration;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Mention;
import com.android.toseefkhan.pandog.models.MyMention;
import com.android.toseefkhan.pandog.models.TrendingItem;
import com.android.toseefkhan.pandog.models.User;
import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.percolate.mentions.Mentionable;
import com.percolate.mentions.Mentions;
import com.percolate.mentions.QueryListener;
import com.percolate.mentions.SuggestionsListener;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NextActivity extends AppCompatActivity implements ThumbnailAdapter.ThumbnailsAdapterListener {

    private static final String TAG = "NextActivity";

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private CropperView imagePreview;
    private Bitmap originalImage;
    private Bitmap filteredImage;
    private Bitmap finalBitmap;

    private SearchView friendSearchView;
    private Context mContext;

    //firebase
    private FirebaseMethods mFirebaseMethods;
    //widgets
    private SocialAutoCompleteTextView mCaption;
    private RecyclerView friendsListView;
    private Intent intent;
    private ImageView image;
    private FriendsAdapter mFriendsAdapter;
    private RecyclerView mRVMentions;
    private Mentions mentions;
    private HashMap<String, String> mentionHash = new HashMap<>();
    private ArrayList<User> mUserList = new ArrayList<>();

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

        friendSearchView = findViewById(R.id.FriendsSearch);

        friendSearchView.setQueryHint("Search for your friends here ...");

        Log.d(TAG, "onCreate: got the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        mRVMentions = findViewById(R.id.recycler_mentions);
        mRVMentions.setLayoutManager(new LinearLayoutManager(mContext));

        friendsListView = findViewById(R.id.FriendsListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        friendsListView.setLayoutManager(layoutManager);

        image = findViewById(R.id.imageShare);

        Intent i = getIntent();
        if (i.hasExtra("challenger_user")) {
            Bundle b = i.getExtras();
            if (b != null) {
                User user = b.getParcelable("challenger_user");
                Log.d(TAG, "onCreate: user " + user);
                if (user == null) {
                    setupFriendsList();
                } else {
                    mFriendsAdapter = new FriendsAdapter(user, mContext);
                    friendsListView.setAdapter(mFriendsAdapter);
                    friendSearchView.setVisibility(View.GONE);
                }
            }
        } else {
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

            Snackbar.make(getWindow().getDecorView().getRootView(), "You are not online!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void loadEditScreen() {

        findViewById(R.id.next_activity).setVisibility(View.GONE);
        findViewById(R.id.r).setVisibility(View.VISIBLE);

        imagePreview = findViewById(R.id.image_preview);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

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
                                cleanMemory();
                                setupHashtagAndMentioning();
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

    private void cleanMemory() {

        imagePreview = null;
        originalImage = null;
        filteredImage = null;
    }

    private void setupHashtagAndMentioning() {

        ArrayAdapter<Hashtag> hashtagAdapter = new HashtagAdapter(mContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("trending")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()) {

                            if (ss.exists()) {
                                try{
                                    Log.d(TAG, "onDataChange: trends " + ss.getValue(TrendingItem.class));
                                    String title = ss.getValue(TrendingItem.class).getTitle();
                                    hashtagAdapter.add(new Hashtag(title.replace("#", "")
                                            , ss.getValue(TrendingItem.class).getPost_keys_list().size()));
                                }catch (NullPointerException e){
                                    Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                                }
                            }
                        }
                        Log.d(TAG, "onDataChange: hashtagadapternow " + hashtagAdapter.getItem(0));
                        mCaption.setHashtagAdapter(hashtagAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        mCaption.setMentionEnabled(false);

        final SearchAdapter adapter = new SearchAdapter(mContext);
        mRVMentions.setAdapter(adapter);

        getUsers();
        mentions = new Mentions.Builder(mContext, mCaption)
                .highlightColor(R.color.deep_orange_400)
                .maxCharacters(20)
                .queryListener(new QueryListener() {
                    @Override
                    public void onQueryReceived(String s) {
                        adapter.filter(s);
                        Log.d(TAG, "setupMentioning: getInsertedMentions " + mentions.getInsertedMentions());
                    }
                })
                .suggestionsListener(new SuggestionsListener() {
                    @Override
                    public void displaySuggestions(boolean b) {

                        if (!b)
                            mRVMentions.setVisibility(View.GONE);
                        Log.d(TAG, "displaySuggestions: boolean boy " + b);
                    }
                })
                .build();

    }

    private void getUsers() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss :
                                dataSnapshot.getChildren()) {

                            if (ss.exists())
                                mUserList.add(ss.getValue(User.class));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
            } else if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
                Log.d(TAG, "loadImage: image coming from gallery");
                originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            }

            int count = 0, num = originalImage.getByteCount();
            while(num != 0)
            {
                num /= 10;
                ++count;
            }

            if (count > 7){
                //make it smaller man!
                int maxHeight = 1280;
                int maxWidth = 940;
                float scale = Math.min(((float)maxHeight / originalImage.getWidth()), ((float)maxWidth / originalImage.getHeight()));

                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);

                originalImage = Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), matrix, true);
            }

            Log.d(TAG, "loadImage: bitmap scales " + originalImage.getWidth() + " " + originalImage.getHeight());

            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(originalImage);

        } catch (Exception e) {
            Log.d(TAG, "loadImage: Exception " + e.getMessage());
        }

    }


    private void sharePost() {
        Log.d(TAG, "onClick: navigating to the final share screen.");
        //upload the image to firebase
        int selectedUserPosition = mFriendsAdapter.getSelectedUserPosition();
        if (selectedUserPosition == -1) {
            Toasty.info(mContext, "Select a user from list first", Toast.LENGTH_SHORT, true).show();
            return;
        }

        Log.d(TAG, "Attempting to upload new photo");
        String caption = mCaption.getText().toString();

        User selectedUser = (User) mFriendsAdapter.getItem(selectedUserPosition);
        intent = getIntent();

        //todo saves the bitmap in memory. Not good
        Uri myUri = bitmapToUriConverter(finalBitmap);
        Log.d(TAG, "onClick: this is the uri from the intent " + myUri);

        long timeStamp = System.currentTimeMillis();
        String imageName = "photo" + timeStamp;

        ArrayList<MyMention> mentions = checkLists();

        if (getIntent().hasExtra("post_task")) {
            String challengeKey = getIntent().getStringArrayListExtra("post_task").get(0);
            mFirebaseMethods.uploadNewPhoto(getString(R.string.post_photo), caption, imageName, null, myUri, selectedUser, challengeKey, mentions);

        } else {
            mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageName, null, myUri, selectedUser, mentions);
        }

    }

    private ArrayList<MyMention> checkLists() {

        ArrayList<MyMention> arr = new ArrayList<>();

        for (Mentionable mentionable : mentions.getInsertedMentions()) {

            MyMention m = new MyMention();
            m.setMentionName(mentionable.getMentionName());
            m.setMentionUid(mentionHash.get(mentionable.getMentionName()));
            if (!arr.contains(m))
                arr.add(m);
        }

        return arr;
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
        mFriendsAdapter = new FriendsAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid(), mContext, (TextView) findViewById(R.id.no_friends_found));
        friendsListView.setAdapter(mFriendsAdapter);

        friendSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFriendsAdapter.filter(newText);
                return true;
            }
        });

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

    private class HashtagAdapter extends ArrayAdapter<Hashtag> {

        public HashtagAdapter(@NonNull Context context) {
            super(context, R.layout.socialview_layout_hashtag, R.id.socialview_hashtag);

        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.socialview_layout_hashtag, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Hashtag item = getItem(position);
            if (item != null) {
                holder.hashtagView.setText(item.getHashtag());

                Log.d(TAG, "getView: this part is working " + item.getCount());
                final int count = item.getCount();
                holder.countView.setText(count + " Posts");

            }
            return convertView;
        }

        private class ViewHolder {
            private final TextView hashtagView;
            private final TextView countView;

            ViewHolder(View itemView) {
                hashtagView = itemView.findViewById(R.id.socialview_hashtag);
                countView = itemView.findViewById(R.id.socialview_hashtag_count);
                countView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchItemViewHolder> {

        private ProfileFilter filter;
        private Context mContext;
        private ArrayList<User> ProfileList;

        public SearchAdapter(Context context) {
            this.mContext = context;
            ProfileList = new ArrayList<>();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.profile_item_mention, viewGroup, false);
            return new SearchAdapter.SearchItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder searchItemViewHolder, int i) {

            searchItemViewHolder.setIsRecyclable(false);
            User user = getItem(i);

            searchItemViewHolder.userNameView.setText(user.getUsername());
            String PhotoUrl = user.getProfile_photo();
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.userppSearch, null, "",
                    null);

            searchItemViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final com.android.toseefkhan.pandog.models.Mention mention = new Mention();
                    mention.setMentionName("@" + user.getUsername());
                    mention.setMentionUid(user.getUser_id());
                    mentions.insertMention(mention);
                    mentionHash.put(mention.getMentionName(), mention.getMentionUid());
                }
            });
        }

        private User getItem(int i) {
            return ProfileList.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return ProfileList != null ? ProfileList.size() : 0;
        }

        public void filter(CharSequence constraint) {
            if (filter == null) {
                filter = new ProfileFilter();
            }
            filter.filter(constraint);
        }

        public class SearchItemViewHolder extends RecyclerView.ViewHolder {

            View mView;
            TextView userNameView;
            CircleImageView userppSearch;

            public SearchItemViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mView = itemView;

                userNameView = itemView.findViewById(R.id.UserNameView);
                userppSearch = itemView.findViewById(R.id.UserProfilePictureView);
            }
        }

        private class ProfileFilter {

            public ProfileFilter() {
            }

            public void filter(final CharSequence constraint) {

                ProfileList.clear();

                if (constraint != null && constraint.length() > 0) {
                    for (int i = 0; i < mUserList.size(); i++) {

                        Log.d(TAG, "filter: userslist " + mUserList);

                        if (mUserList.get(i) != null) {
                            String Username = mUserList.get(i).getUsername().toUpperCase();
                            String entered = constraint.toString().toUpperCase();
                            if (Username.contains(entered)) {
                                ProfileList.add(mUserList.get(i));
                            }
                        }
                    }

                    if (ProfileList.size() != 0) {
                        mRVMentions.setVisibility(View.VISIBLE);
                    } else {
                        mRVMentions.setVisibility(View.GONE);
                    }

                    notifyDataSetChanged();
                }

            }

        }
    }

}