package com.android.toseefkhan.pandog.Map;

import androidx.appcompat.app.AppCompatActivity;

//import android.Manifest;
//import android.animation.ObjectAnimator;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.res.Resources;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.location.Location;
//import android.location.LocationManager;
//import android.os.Build;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//
//import com.android.toseefkhan.pandog.Share.ShareActivity;
//import com.google.android.material.snackbar.Snackbar;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.preference.PreferenceManager;
//import android.transition.Fade;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewTreeObserver;
//import android.view.animation.DecelerateInterpolator;
//import android.view.animation.OvershootInterpolator;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.android.toseefkhan.pandog.Home.HomeActivity;
//import com.android.toseefkhan.pandog.R;
//import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
//import com.android.toseefkhan.pandog.Utils.InitialSetup;
//import com.android.toseefkhan.pandog.Utils.InternetStatus;
//import com.android.toseefkhan.pandog.Utils.ViewWeightAnimationWrapper;
//import com.android.toseefkhan.pandog.models.User;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.MapStyleOptions;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
//import com.koushikdutta.ion.Ion;
//import com.takusemba.spotlight.OnSpotlightStateChangedListener;
//import com.takusemba.spotlight.OnTargetStateChangedListener;
//import com.takusemba.spotlight.Spotlight;
//import com.takusemba.spotlight.shape.Circle;
//import com.takusemba.spotlight.target.CustomTarget;
//
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.concurrent.ExecutionException;
//import de.hdodenhof.circleimageview.CircleImageView;
//import es.dmoral.toasty.Toasty;
//import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
//import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
//import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
//import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
//
//
//public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
//    , View.OnClickListener{
//
public class MapActivity extends AppCompatActivity {}
//    private static final String TAG = "MapActivity";
//
//    //constants
//    private Context mContext = MapActivity.this;
//    private static final int ACTIVITY_NUM = 1;
//    private static final int ERROR_DIALOG_REQUEST = 9001;
//    private static final int PERMISSIONS_REQUEST_E
// NABLE_GPS = 9002;
//    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
//    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
//    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
//    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
//    private int mMapLayoutState = 0;
//
//    private boolean mLocationPermissionGranted = false;
//    private Dialog gpsDialog;
//    private MapView mMapView;
//    private RecyclerView mUserListRecyclerView;
//    private FusedLocationProviderClient mFusedLocationClient;
//    private DatabaseReference myRef;
//
//    private ArrayList<User> mUserListViewable = new ArrayList<>();
//    private MapRecyclerViewAdapter mUserRecyclerAdapter;
//    private RelativeLayout relativeLayout;
//    private LinearLayout linearLayout;
//    private TextView gps;
//    private Bundle mSavedInstanceState;
//    private LatLng latLng;
//    private GoogleMap mMap;
//    private RelativeLayout mMapContainer;
//    private CircleImageView pp;
//    private ProgressBar mProgressbar, dialogProgressbar;
//    private ArrayList<Marker> markerList = new ArrayList<>();
//
//    //for the welcome screen
//    SharedPreferences mPrefs;
//    final String tutorialScreenShownPrefMap = "tutorialScreenShownMap";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mSavedInstanceState = savedInstanceState;
//        setContentView(R.layout.activity_map);
//
//        setupBottomNavigationView();
//
//        myRef= FirebaseDatabase.getInstance().getReference();
//        relativeLayout = findViewById(R.id.permission_null);
////        linearLayout = findViewById(R.id.permission_not_null);
////        mMapContainer = findViewById(R.id.map_container);
////        gps = findViewById(R.id.gps);
////        findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
////        findViewById(R.id.btn_hide_my_marker).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////
////               AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
////                       .setCancelable(true)
////                       .setMessage("Your position will not be displayed on the map.\nBut if you do so, you won't be able to use the map feature.")
////                       .setPositiveButton("HIDE ME", new DialogInterface.OnClickListener() {
////                           @Override
////                           public void onClick(DialogInterface dialogInterface, int i) {
////
////                               myRef.child(getString(R.string.dbname_users))
////                                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
////                                       .child("hide_position")
////                                       .setValue("true");
////
////                               Intent intent = new Intent(mContext,MapActivity.class);
////                               startActivity(intent);
////                               overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
////                           }
////                       });
////
////               AlertDialog alertDialog = builder.create();
////               alertDialog.setTitle("Hide my Position");
////               alertDialog.show();
////            }
////        });
//
//        gpsDialog = new Dialog(mContext);
////        mProgressbar = findViewById(R.id.progressBar);
////        mProgressbar.setVisibility(View.VISIBLE);
//        mUserListRecyclerView = findViewById(R.id.user_list_recycler_view);
//   //     mMapView = findViewById(R.id.user_list_map);
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        initGoogleMap(savedInstanceState);
//
//        if (checkMapServices()) {
//            if (mLocationPermissionGranted) {
//                gpsDialog.dismiss();
//                //todo handle events after the request has been granted
//           //     showLayout();
//                getLastKnownLocation();
//                //getUsersFromArea();
//            } else {
//                getLocationPermission();
//            }
//        }
//        if (!InternetStatus.getInstance(this).isOnline()) {
//
//            Snackbar.make(getWindow().getDecorView().getRootView(),"You are not online!",Snackbar.LENGTH_LONG).show();
//        }
//    }
//
//    private void setMarkersOnMap(ArrayList<User> mUserList,ArrayList<MarkerOptions> markerOptions){
//
//        Marker marker;
//
//        for (int i= 0 ; i<mUserList.size() ; i++){
//            try {
//                marker = mMap.addMarker(markerOptions.get(i));
//                marker.setTag(mUserList.get(i));
//                markerList.add(marker);    //needs this for the recycler view
//            }catch (Exception e){}
//        }
//        checkVisibility(markerList);
//    }
//
//    private void getLastKnownLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    Location location = task.getResult();
//                    try{
//                        //the map does not show when the gps on the device is on(api key issues). Therefore this latlng is always null since i can't
//                        // get the current location of the user from the gps, I am using hard coded coordinates
//                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                    }catch (NullPointerException e){
//                        Log.d("Error", "onComplete: NullPointerException " + e.getMessage());
//                    //    latLng = new LatLng(28.582985, 77.255820);
//                    }
//
//                    setLatlongs(latLng);
//                }
//            }
//        });
//    }
//
//    private void checkVisibility(ArrayList<Marker> markerList){
//        boolean show=false;
//        if(mMap != null){
//
//            LatLngBounds latLongBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//
//            for (int i=0; i<markerList.size() ; i++){
//
//                if(latLongBounds.contains(markerList.get(i).getPosition())){
//                    //If the marker is within the the bounds of the screen add it to the ArrayList
//                    User user = (User) markerList.get(i).getTag();
//                    if (!mUserListViewable.contains(user)){
//                        mUserListViewable.add(user);
//                    }
//                    show= true;
//                    mUserListRecyclerView.setVisibility(View.VISIBLE);
//                }
//            }
//            if (!show){
//                mUserListRecyclerView.setVisibility(View.INVISIBLE);
//          //      Snackbar snackbar= Snackbar.make(mMapView,"Uh-oh! Looks like no one interesting lives here",Snackbar.LENGTH_SHORT);
//           //     snackbar.show();
//            }
//
//            if (!mUserListViewable.isEmpty()){
//                ///initUserListRecyclerView(sortList(mUserListViewable));
//            }
//        }
//    }
//
//
//    @Override
//    public void onMapReady(final GoogleMap map) {
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
////        map.setMyLocationEnabled(true);
//
//        map.getUiSettings().setMapToolbarEnabled(false);
//        map.getUiSettings().setCompassEnabled(false);
//
//        //centers the map on the current user's location
//        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    Location location = task.getResult();
//                    if (task.getResult()!=null){
//                        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
//                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(11);
//                        map.moveCamera(center);
//                        map.animateCamera(zoom);
//                    }
//                    else{
//                        //for testing
//                        LatLng latLng= new LatLng(28.572945, 77.255721);
//                        CameraUpdate center=CameraUpdateFactory.newLatLng(latLng);
//                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(11);
//                        map.moveCamera(center);
//                        map.animateCamera(zoom);
//                    }
//
//                }
//            }
//        });//moves the camera to the current location of the user
//
//        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(CameraPosition cameraPosition) {
//                mUserListViewable.clear();
//                checkVisibility(markerList);
//            }
//        });
//
//        long time= System.currentTimeMillis()%10;
//        int timeConst= (int) time;
//        boolean success;
//        try {
//            // Customise the styling of the base map using a JSON object defined
//            // in a raw resource file.
//            switch (timeConst){
//
//                case 1:
//                     success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.silver_map));
//                    break;
//
//                case 2:
//                     success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.silver_map));
//                    break;
//
//                case 3:
//                     success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.retro_map));
//                    break;
//
//                case 4:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.retro_map));
//                    break;
//
//                case 5:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.night_map));
//                    break;
//
//                case 6:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.night_map));
//                    break;
//
//                case 7:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.dark_map));
//                    break;
//
//                case 8:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.dark_map));
//                    break;
//
//                case 9:
//                    success = map.setMapStyle(
//                            MapStyleOptions.loadRawResourceStyle(
//                                    this, R.raw.aubergine_map));
//                    break;
//
//
//                    default:
//                        success = map.setMapStyle(
//                                MapStyleOptions.loadRawResourceStyle(
//                                        this, R.raw.aubergine_map));
//                        break;
//            }//just some styling stuff
//            mMap=map;
//
//       //     setMarkersOnMap(((InitialSetup)getApplicationContext()).mUserList,((InitialSetup)getApplicationContext()).markerOptionsList);
//
//            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
//
//                @Override
//                public View getInfoWindow(Marker marker) {
//                    return null;
//                }
//
//                @Override
//                public View getInfoContents(Marker marker) {
//                    View v = getLayoutInflater().inflate(R.layout.layout_info_window, null);
//
//                    pp = v.findViewById(R.id.profile_photo_infow);
//                    TextView username = v.findViewById(R.id.username_infow);
//                    TextView description = v.findViewById(R.id.description_infow);
//                    User user= (User) marker.getTag();
//
//                    try {
//                        Bitmap bmImg = Ion.with(mContext)
//                                .load(user.getBitmap()).asBitmap().get();
//                        pp.setImageBitmap(bmImg);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    } catch (NullPointerException e){
//                        e.printStackTrace();
//                    }
//
//                    username.setText(marker.getTitle());
//                    description.setText(marker.getSnippet());
//
//                    return v;
//                }
//            });
//
//            if (!success) {
//                Log.e("error", "Style parsing failed.");
//            }
//        } catch (Resources.NotFoundException e) {
//            Log.e("error", "Can't find style. Error: ", e);
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//    /**
//     --------------------------------------------------Permissions and stuff that no one should care about--------------------------------------------------------------
//     */
//
////    private void showLayout() {
////        relativeLayout.setVisibility(View.GONE);
////        linearLayout.setVisibility(View.VISIBLE);
////        mProgressbar.setVisibility(View.GONE);
////
////        myRef.child(getString(R.string.dbname_users))
////                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
////                .child("hide_position")
////                .addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        String isPositionHidden = dataSnapshot.getValue(String.class);
////
////                        try{
////
////                            if (isPositionHidden.equals("true")){
////
////                                FrameLayout root = new FrameLayout(mContext);
////                                LayoutInflater inflater = LayoutInflater.from(mContext);
////
////                                View first = inflater.inflate(R.layout.overlay_hidden_position, root);
////
////                                CustomTarget homeView = new CustomTarget.Builder(MapActivity.this)
////                                        .setPoint(0f,0f)
////                                        .setShape(new Circle(0f))
////                                        .setOverlay(first)
////                                        .setOnSpotlightStartedListener(new OnTargetStateChangedListener<CustomTarget>() {
////                                            @Override
////                                            public void onStarted(CustomTarget target) {
////                                                // do something
////                                            }
////                                            @Override
////                                            public void onEnded(CustomTarget target) {
////                                                // do something
////                                            }
////                                        })
////                                        .build();
////
////
////                                TextView Yes = first.findViewById(R.id.yes);
////                                TextView No = first.findViewById(R.id.no);
////
////                                linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////                                    @Override
////                                    public void onGlobalLayout() {
////                                        linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
////                                        Spotlight spotlight = Spotlight.with(MapActivity.this)
////                                                .setOverlayColor(R.color.background)
////                                                .setDuration(1000L)
////                                                .setAnimation(new DecelerateInterpolator(2f))
////                                                .setTargets(homeView)
////                                                .setClosedOnTouchedOutside(false)
////                                                .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
////                                                    @Override
////                                                    public void onStarted() {
////
////                                                    }
////
////                                                    @Override
////                                                    public void onEnded() {
////                                                    }
////                                                });
////                                        spotlight.start();
////
////                                        Yes.setOnClickListener(new View.OnClickListener() {
////                                            @Override
////                                            public void onClick(View view) {
////
////                                                myRef.child(getString(R.string.dbname_users))
////                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
////                                                        .child("hide_position")
////                                                        .removeValue();
////
////                                                Intent intent = new Intent(mContext, MapActivity.class);
////                                                startActivity(intent);
////                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
////
////                                                Toasty.warning(mContext, "Kindly restart the app to see changes.", Toast.LENGTH_SHORT,true).show();
////                                            }
////                                        });
////
////                                        No.setOnClickListener(new View.OnClickListener() {
////                                            @Override
////                                            public void onClick(View view) {
////
////                                                Intent intent = new Intent(mContext, HomeActivity.class);
////                                                startActivity(intent);
////                                            }
////                                        });
////
////                                    }
////                                });
////                            }
////                        }catch (NullPointerException e){
////                            Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
////                        }
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
////
////
////
////
////        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
////
////        // second argument is the default to use if the preference can't be found
////        boolean welcomeScreenShown = mPrefs.getBoolean(tutorialScreenShownPrefMap, false);
////
////        if (!welcomeScreenShown) {
////
////            startTutorial();
////            SharedPreferences.Editor editor = mPrefs.edit();
////            editor.putBoolean(tutorialScreenShownPrefMap, true);
////            editor.apply(); // Very important to save the preference
////        }
////
////    }
//
////    private void startTutorial() {
////
////        Log.d(TAG, "startTutorial: starting the tutorial");
////
////        FrameLayout root = new FrameLayout(mContext);
////        LayoutInflater inflater = LayoutInflater.from(mContext);
////
////        View first = inflater.inflate(R.layout.overlay_map_tutorial, root);
////
////        CustomTarget homeView = new CustomTarget.Builder(this)
////                .setPoint(0f,0f)
////                .setShape(new Circle(0f))
////                .setOverlay(first)
////                .setOnSpotlightStartedListener(new OnTargetStateChangedListener<CustomTarget>() {
////                    @Override
////                    public void onStarted(CustomTarget target) {
////                        // do something
////                    }
////                    @Override
////                    public void onEnded(CustomTarget target) {
////                        // do something
////                    }
////                })
////                .build();
////
////        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////            @Override
////            public void onGlobalLayout() {
////                linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
////                Spotlight spotlight = Spotlight.with(MapActivity.this)
////                        .setOverlayColor(R.color.background)
////                        .setDuration(1000L)
////                        .setAnimation(new DecelerateInterpolator(2f))
////                        .setTargets(homeView)
////                        .setClosedOnTouchedOutside(true)
////                        .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
////                            @Override
////                            public void onStarted() {
////
////                            }
////
////                            @Override
////                            public void onEnded() {
////                                Log.d(TAG, "onEnded: spotlight ended navigating to usual stuff");
////                                Intent i = new Intent(mContext,MapActivity.class);
////                                startActivity(i);
////                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
////                            }
////                        });
////                spotlight.start();
////            }
////        });
////
////    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(checkMapServices()){
//            if(mLocationPermissionGranted){
//                gpsDialog.dismiss();
//                //todo handle events after the request has been granted
//            //    showLayout();
//                getLastKnownLocation();
//           //     initGoogleMap(mSavedInstanceState);
//            }
//            else{
//                getLocationPermission();
//            }
//        }
//
//    }
//
////    private void expandMapAnimation(){
////        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
////        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
////                "weight",
////                60,
////                100);
////        mapAnimation.setDuration(800);
////
////        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
////        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
////                "weight",
////                40,
////                0);
////        recyclerAnimation.setDuration(800);
////
////        recyclerAnimation.start();
////        mapAnimation.start();
////    }
//
////    private void contractMapAnimation(){
////        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
////        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
////                "weight",
////                100,
////                60);
////        mapAnimation.setDuration(800);
////
////        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
////        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
////                "weight",
////                0,
////                40);
////        recyclerAnimation.setDuration(800);
////
////        recyclerAnimation.start();
////        mapAnimation.start();
////    }
//
//    private void setLatlongs(LatLng latlng){
//
//        myRef.child(mContext.getString(R.string.dbname_users))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(mContext.getString(R.string.db_timestamp))
//                .setValue(System.currentTimeMillis());
//
//        myRef.child(mContext.getString(R.string.dbname_users))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(mContext.getString(R.string.db_latlng))
//                .setValue(latlng);
//    }//sets the current location of the user in the db
//
//
//    private void initGoogleMap(Bundle savedInstanceState){
//        // *** IMPORTANT ***
//        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
//        // objects or sub-Bundles.
//        Bundle mapViewBundle = null;
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
//        }
//
//        mMapView.onCreate(mapViewBundle);
//
//        mMapView.getMapAsync(this);
//    }
////
////    private void initUserListRecyclerView(ArrayList<User> users) {
////        mProgressbar.setVisibility(View.GONE);
//////        dialogProgressbar.setVisibility(View.GONE);
////        mUserRecyclerAdapter = new MapRecyclerViewAdapter(mContext , users);
////        AlphaInAnimationAdapter a = new AlphaInAnimationAdapter(mUserRecyclerAdapter);
////        a.setDuration(1750);
////        a.setInterpolator(new OvershootInterpolator());
////        mUserListRecyclerView.setAdapter(a);
////        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
////    }
////
////
////    @Override
////    public void onSaveInstanceState(Bundle outState) {
////        super.onSaveInstanceState(outState);
////
////        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
////        if (mapViewBundle == null) {
////            mapViewBundle = new Bundle();
////            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
////        }
////
////        mMapView.onSaveInstanceState(mapViewBundle);
////    }
////
////    @Override
////    public void onStart() {
////        super.onStart();
////        mMapView.onStart();
////    }
////
////    @Override
////    public void onStop() {
////        super.onStop();
////        mMapView.onStop();
////    }
////
////    @Override
////    public void onPause() {
////        mMapView.onPause();
////        super.onPause();
////    }
////
////    @Override
////    public void onDestroy() {
////        mMapView.onDestroy();
////        super.onDestroy();
////    }
////
////    @Override
////    public void onLowMemory() {
////        super.onLowMemory();
////        mMapView.onLowMemory();
////    }
////
////    private void buildAlertMessageNoGps() {
////        gpsDialog.setContentView(R.layout.layout_gps_dialog_box);
////        ImageView cancelDialog = gpsDialog.findViewById(R.id.cancel_dialog);
////        TextView yesDialog= gpsDialog.findViewById(R.id.enable_text);
////        dialogProgressbar = gpsDialog.findViewById(R.id.dialogProgressBar);
////        dialogProgressbar.setVisibility(View.VISIBLE);
////
////        cancelDialog.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                gpsDialog.dismiss();
////                Intent intent= new Intent(mContext, HomeActivity.class);
////                startActivity(intent);
////            }
////        });
////
////        yesDialog.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
////            }
////        });
////
////        gps.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
////            }
////        });
////
////        gpsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
////        gpsDialog.show();
////
////    }
//
//    public boolean isMapsEnabled(){
//        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
//
//        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
//       //     buildAlertMessageNoGps();
//            return false;
//        }
//        return true;
//    }
//
//    private void getLocationPermission() {
//        /*
//         * Request location permission, so that we can get the location of the
//         * device. The result of the permission request is handled by a callback,
//         * onRequestPermissionsResult.
//         */
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLocationPermissionGranted = true;
//            //todo handle events after the request has been granted
//       //     showLayout();
//            getLastKnownLocation();
//
//
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//
//        }
//    }
//
//    private boolean checkMapServices(){
//        if(isServicesOK()){
//            if(isMapsEnabled()){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean isServicesOK(){
//
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);
//
//        if(available == ConnectionResult.SUCCESS){
//            //everything is fine and the user can make map requests
//            return true;
//
//            //todo if the user denies the google's dialog request, the system goes in an infinite loop. Fix that.
//        }
//        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//            //an error occured but we can resolve it
//            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
//            dialog.show();
//        }else{
//            Toasty.error(this, "You can't make map requests", Toast.LENGTH_SHORT,true).show();
//        }
//        return false;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_ENABLE_GPS: {
//                if(mLocationPermissionGranted){
//                    gpsDialog.dismiss();
//                   //todo handle events after the request has been granted
//              //      showLayout();
//                    getLastKnownLocation();
//
//                }
//                else{
//                    getLocationPermission();
//                }
//            }
//        }
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        mLocationPermissionGranted = false;
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mLocationPermissionGranted = true;
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
////            case R.id.btn_full_screen_map: {
////
////                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
////                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
////                    expandMapAnimation();
////                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
////                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
////                    contractMapAnimation();
////                }
////                break;
////            }
//        }
//    }
//
//    private ArrayList<User> sortList(ArrayList<User> userList) {
//
//        Collections.sort(userList, new Comparator<User>() {
//            @Override
//            public int compare(User o1, User o2) {
//                return Integer.valueOf(o2.getPanda_points()).compareTo(Integer.valueOf(o1.getPanda_points()));
//            }
//        });
//
//        return userList;
//    }//this should sort the list and provide the highest panda points holder in the currently bounded map area whose marker will be the biggest on the map
//
//
//    /**
//     * BottomNavigationView setup
//     */
//    private void setupBottomNavigationView(){
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
//        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx,MapActivity.this);
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
//
//}
