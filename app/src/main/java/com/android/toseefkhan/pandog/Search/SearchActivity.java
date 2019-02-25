package com.android.toseefkhan.pandog.Search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.android.toseefkhan.pandog.Profile.ViewPostsListActivity;
import com.android.toseefkhan.pandog.Utils.InterceptRelativeLayout;
import com.android.toseefkhan.pandog.models.TrendingItem;
import com.dingmouren.layoutmanagergroup.banner.BannerLayoutManager;
import com.dingmouren.layoutmanagergroup.echelon.EchelonLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.toseefkhan.pandog.Profile.ViewProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.InternetStatus;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext = SearchActivity.this;
    private RecyclerView profilesListView;
    private RecyclerView vertical;
    private List<ImageView> mImgList = new ArrayList<>();
    private int mLastSelectPosition = 0;
    private TextView textView;
    private ImageView imageView;
    private InterceptRelativeLayout mRelaIntercept1;
//    private RelativeLayout searchRelativeLayout;
    private TextView searchEmptyTextView;
    private SearchView profileSearchView;

    //for the welcome screen
    SharedPreferences mPrefs;
    final String tutorialScreenShownPrefSearch = "tutorialScreenShownSearch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initImageLoader();
        initBannerRV();
        initVerticalRV();

  //      searchRelativeLayout = findViewById(R.id.SearchRelativeLayout);
        searchEmptyTextView = findViewById(R.id.EmptySearchTextView);
        searchEmptyTextView.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Comic Neue.ttf"));
        profileSearchView = findViewById(R.id.searchProfiles);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        profilesListView = findViewById(R.id.ProfileList);
        final SearchAdapter adapter = new SearchAdapter(mContext, user.getUid());
        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(adapter);
        a.setDuration(800);
        profilesListView.setAdapter(a);
        profilesListView.setItemAnimator(new SlideInUpAnimator());

        profilesListView.setLayoutManager(new EchelonLayoutManager(mContext));
        textView = findViewById(R.id.t);
        imageView = findViewById(R.id.trending);

        profileSearchView.setActivated(true);
        profileSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileSearchView.onActionViewExpanded();
            }
        });
   //     profileSearchView.onActionViewExpanded();
        profileSearchView.setQueryHint("Search here...");

        profileSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);

                vertical.setVisibility(View.GONE);
                profilesListView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                mRelaIntercept1.setVisibility(View.GONE);

                return true;
            }
        });

        setupBottomNavigationView();

        if (!InternetStatus.getInstance(this).isOnline()) {

            Snackbar.make(getWindow().getDecorView().getRootView(), "You are not online!", Snackbar.LENGTH_LONG).show();
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // second argument is the default to use if the preference can'textView be found
        boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPrefSearch, false);

        if (!welcomeScreenShown) {

            startTutorial();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(tutorialScreenShownPrefSearch, true);
            editor.apply(); // Very important to save the preference
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void initBannerRV() {

        //setting the banner recycler view on the top
        mRelaIntercept1 = findViewById(R.id.rela_intercept_1);
        mRelaIntercept1.setIntercept(false);
        ImageView mImg1 =  findViewById(R.id.img_1);
        ImageView mImg2 =  findViewById(R.id.img_2);
        ImageView mImg3 =  findViewById(R.id.img_3);
        ImageView mImg4 =  findViewById(R.id.img_4);
        mImgList.add(mImg1);
        mImgList.add(mImg2);
        mImgList.add(mImg3);
        mImgList.add(mImg4);

        RecyclerView mRecycler_1 = findViewById(R.id.recycler1);
        BannerLayoutManager bannerLayoutManager = new BannerLayoutManager(this,mRecycler_1,4,OrientationHelper.HORIZONTAL);
        mRecycler_1.setLayoutManager(bannerLayoutManager);
        bannerLayoutManager.setOnSelectedViewListener(new BannerLayoutManager.OnSelectedViewListener() {
            @Override
            public void onSelectedView(View view, int position) {
                changeUI(position);
            }
        });
        changeUI(0);

        ArrayList<String> imgUrls = new ArrayList<>();
        ArrayList<TrendingItem> trendingItems = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("search_banner")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            HashMap<String,Object> objectMap = (HashMap<String,Object>) ss.getValue();

                            imgUrls.add(objectMap.get("imageUrl").toString());

                            String trendingTitle = objectMap.get("trending_title").toString();

                            ref.child("trending")
                                    .child(trendingTitle)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            TrendingItem item = dataSnapshot.getValue(TrendingItem.class);
                                            trendingItems.add(item);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                        MyAdapter myAdapter = new MyAdapter(imgUrls,trendingItems);
                        mRecycler_1.setAdapter(myAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initVerticalRV() {

        vertical = findViewById(R.id.holder_vertical);
        vertical.setLayoutManager(new LinearLayoutManager(mContext));

        ArrayList<TrendingItem> trendingItemsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("trending")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ss : dataSnapshot.getChildren()){

                            TrendingItem item = ss.getValue(TrendingItem.class);

                            trendingItemsList.add(item);
                        }

                        VerticalRecyclerViewAdapter adapter = new VerticalRecyclerViewAdapter(mContext,trendingItemsList);
                        vertical.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void changeUI(int position){
        if (position != mLastSelectPosition) {
            mImgList.get(position).setImageDrawable(getResources().getDrawable(R.drawable.circle_red));
            mImgList.get(mLastSelectPosition).setImageDrawable(getResources().getDrawable(R.drawable.circle_grey));
            mLastSelectPosition = position;
        }
    }


    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchItemViewHolder> {

        String userUID;
        private ProfileFilter filter;
        private Context mContext;
        private FirebaseDatabase firebaseDatabase;
        private DatabaseReference databaseReference;
        private ArrayList<User> ProfileList;

        public SearchAdapter(Context context, String uid) {
            this.mContext = context;
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference();
            ProfileList = new ArrayList<>();
            this.userUID = uid;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.search_profile_item, viewGroup, false);
            return new SearchItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder searchItemViewHolder, int i) {

            searchItemViewHolder.setIsRecyclable(false);
            User user = getItem(i);

            searchItemViewHolder.userNameView.setText(user.getUsername());
            searchItemViewHolder.userEmailView.setText(user.getEmail());
            String PhotoUrl = user.getProfile_photo();
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.photoView, null, "",
                    searchItemViewHolder.child);
            UniversalImageLoader.setImage(PhotoUrl, searchItemViewHolder.userppSearch, null, "",
                    null);

            searchItemViewHolder.mView.setOnClickListener(view -> {
                //navigate to view profile activity
                Intent intent = new Intent(SearchActivity.this, ViewProfileActivity.class);
                intent.putExtra(getString(R.string.intent_user), ProfileList.get(i));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                ProfileList.clear();
                startActivity(intent);
                profileSearchView.setQuery("",false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vertical.setVisibility(View.VISIBLE);
                        profilesListView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        mRelaIntercept1.setVisibility(View.VISIBLE);
                    }
                },1000);

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
            TextView userEmailView;
            ImageView photoView;
            CircleImageView userppSearch;
            View child;

            public SearchItemViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mView = itemView;

                userNameView = itemView.findViewById(R.id.UserNameView);
                userEmailView = itemView.findViewById(R.id.UserEmailView);
                photoView = itemView.findViewById(R.id.UserProfilePictureView);
                child = itemView.findViewById(R.id.progress_child);
                userppSearch = itemView.findViewById(R.id.userppSearch);
            }
        }

        private class ProfileFilter {

            public ProfileFilter() {
            }

            public void filter(final CharSequence constraint) {

                removeEmptyView();
                if (constraint != null && constraint.length() > 0) {
                    final ArrayList<User> users = new ArrayList<>();
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot childData : dataSnapshot.getChildren()) {
                                    User user = childData.getValue(User.class);
                                    String Username = user.getUsername().toUpperCase();
                                    String entered = constraint.toString().toUpperCase();
                                    if (Username.contains(entered) && !user.getUser_id().equals(userUID)) {
                                        users.add(user);
                                    }
                                }
                                ProfileList = users;
                                if(ProfileList.size() == 0){
                                    setEmptyView();
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else{
                    Log.d(TAG, "filter: list got cleared.");
//                    ProfileList.clear();
//                    notifyDataSetChanged();
//                    Intent i = new Intent(mContext,SearchActivity.class);
//                    startActivity(i);
//                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
//                    Log.d(TAG, "filter: clearing the views");
//                    profilesListView.removeAllViews();
                }
            }
        }

        private void setEmptyView() {
            profilesListView.removeAllViews();
            //searchRelativeLayout.setVisibility(View.GONE);
            searchEmptyTextView.setVisibility(View.VISIBLE);
        }

    }

    private void removeEmptyView() {
       // mViewHolder.userNameView.setVisibility(View.GONE);
        //  searchRelativeLayout.setVisibility(View.VISIBLE);
        try {
            searchEmptyTextView.setVisibility(View.GONE);
        }catch (NullPointerException e){
            Log.d(TAG, "removeEmptyView: NullPointerException " + e.getMessage());
        }
    }




    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx, SearchActivity.this);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        menuItem.setEnabled(false);
    }


    private void startTutorial() {

        Log.d(TAG, "startTutorial: starting the tutorial");

        new MaterialTapTargetPrompt.Builder(SearchActivity.this)
                .setTarget(findViewById(R.id.trending))
                .setBackgroundColour(getResources().getColor(R.color.background))
                .setAutoDismiss(false)
                .setBackButtonDismissEnabled(false)
                .setPrimaryText("Feature yourself here by following the trending hashtags of the week!")
                .setSecondaryText("And let the world know your awesomeness!")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            new MaterialTapTargetPrompt.Builder(SearchActivity.this)
                                    .setTarget(findViewById(R.id.ic_cloud))
                                    .setBackgroundColour(getResources().getColor(R.color.background))
                                    .setAutoDismiss(false)
                                    .setBackButtonDismissEnabled(false)
                                    .setPrimaryText("Introducing")
                                    .setSecondaryText("Discover section to find awesome people living near you!")
                                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                                        @Override
                                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {

                                            }
                                        }
                                    }).show();

                        }
                    }
                }).show();

    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private ArrayList<String> mUrls;
        private ArrayList<TrendingItem> mList;

        public MyAdapter(ArrayList<String> mUrls, ArrayList<TrendingItem> mList) {
            this.mUrls = mUrls;
            this.mList = mList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

            holder.setIsRecyclable(false);

            switch (position%4){

                case 0:
                    holder.img.setImageResource(R.drawable.referandearn);
                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //take to referral activity
                            Intent i = new Intent(mContext, ReferActivity.class);
                            startActivity(i);
                        }
                    });
                    break;

                case 1:
                    UniversalImageLoader.setImage(mUrls.get(0),holder.img,null,"",null);
                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideKeyboard();
                            Intent i = new Intent(mContext, ViewPostsListActivity.class);
                            i.putExtra("post_keys_list",mList.get(0).getPost_keys_list());
                            i.putExtra("title",mList.get(0).getTitle());
                            startActivity(i);
                            overridePendingTransition(R.anim.pull,R.anim.push);
                        }
                    });

                    break;

                case 2:
                    UniversalImageLoader.setImage(mUrls.get(1),holder.img,null,"",null);
                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideKeyboard();
                            Intent i = new Intent(mContext, ViewPostsListActivity.class);
                            i.putExtra("post_keys_list",mList.get(1).getPost_keys_list());
                            i.putExtra("title",mList.get(1).getTitle());
                            startActivity(i);
                            overridePendingTransition(R.anim.pull,R.anim.push);
                        }
                    });

                    break;

                case 3:
                    UniversalImageLoader.setImage(mUrls.get(2),holder.img,null,"",null);
                    holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideKeyboard();
                            Intent i = new Intent(mContext, ViewPostsListActivity.class);
                            i.putExtra("post_keys_list",mList.get(2).getPost_keys_list());
                            i.putExtra("title",mList.get(2).getTitle());
                            startActivity(i);
                            overridePendingTransition(R.anim.pull,R.anim.push);
                        }
                    });

                    break;
            }

        }

        @Override
        public int getItemCount() {
            return Integer.MAX_VALUE;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            public ViewHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.img);
            }
        }
    }


    private void hideKeyboard(){

        InputMethodManager imm =(InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null && imm != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }



}
