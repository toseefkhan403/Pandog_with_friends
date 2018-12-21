package com.android.toseefkhan.pandog.Map;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.RecyclerViewAdapter;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.Utils.ViewWeightAnimationWrapper;
import com.android.toseefkhan.pandog.models.LatLong;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserSettings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
    ,View.OnClickListener{

    private static final String TAG = "MapActivity";

    //constants
    private Context mContext = MapActivity.this;
    private static final int ACTIVITY_NUM = 1;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;

    private boolean mLocationPermissionGranted = false;
    private Dialog gpsDialog;
    private MapView mMapView;
    private RecyclerView mUserListRecyclerView;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference myRef;

    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<User> mUserListMaps = new ArrayList<>();
    private User mBestUser;
    private RecyclerViewAdapter mUserRecyclerAdapter;
    private RelativeLayout relativeLayout;
    private LinearLayout linearLayout;
    private TextView gps;
    private Bundle mSavedInstanceState;
    private LatLng fr,fl,nr,nl;
    private LatLng latLng;
    private GoogleMap mMap;
    private RelativeLayout mMapContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: map activity called.");
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_map);


        setupBottomNavigationView();
        relativeLayout = findViewById(R.id.permission_null);
        linearLayout = findViewById(R.id.permission_not_null);
        mMapContainer = findViewById(R.id.map_container);
        gps = findViewById(R.id.gps);
        findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
        gpsDialog = new Dialog(mContext);

        mUserListRecyclerView = findViewById(R.id.user_list_recycler_view);
        mMapView = findViewById(R.id.user_list_map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initGoogleMap(savedInstanceState);

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                gpsDialog.dismiss();
                //todo handle events after the request has been granted
                showLayout();
                getLastKnownLocation();
            } else {
                getLocationPermission();
            }
        }
        getUsersFromArea();
    }

    /*
    todo step 1: Add the markers of all the users on the map with rounded profile photos
    todo step 2: in the current viewable area, calculate who has the highest panda points and sort them in ascending order and display them in the RecyclerView as well in the same order
    todo step 3: the person who is at the top of the recyclerViewList should have the biggest marker
     */

    private void getUsersFromArea(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user list: " + singleSnapshot.getValue());  // gives the whole user objects


                    try{
                        User user= singleSnapshot.getValue(User.class);
                        Log.d(TAG, "onDataChange: found the lng of the user: "+ user.getLat_lng().getLatitude() + " " + user.getLat_lng().getLongitude());
//
//                        if (user.getLat_lng().getLatitude() > nl.latitude && user.getLat_lng().getLongitude() > nl.longitude
//                                    && user.getLat_lng().getLatitude() > nr.latitude && user.getLat_lng().getLongitude() < nr.longitude) {
//                            if (user.getLat_lng().getLatitude() < fl.latitude && user.getLat_lng().getLongitude() > fl.longitude
//                                    && user.getLat_lng().getLatitude() < fr.latitude && user.getLat_lng().getLongitude() < fr.longitude) {
                        Log.d(TAG, "onDataChange: the user satisfying the coordinates " + user.getUsername().toString());
                        mUserList.add(user);
                        }catch (NullPointerException e){
                            Log.d(TAG, "onDataChange: NullPointerException " + e.getMessage());
                        }
                }
                initUserListRecyclerView(mUserList);
                setMarker(mUserList);

             //   mBestUser= sortList(mUserList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }//gives the list of ALL the users of the application

    private void setMarker(ArrayList<User> users){

        for (int i=0; i<users.size(); i++){
            Log.d(TAG, "onMapReady: the marker is adding of this user " + users.get(i));
            LatLng latLng= new LatLng(users.get(i).getLat_lng().getLatitude(), users.get(i).getLat_lng().getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).
                    icon(BitmapDescriptorFactory.fromBitmap(createMarker(mContext,users.get(i))))
                    .title(users.get(i).getUsername())
                    .snippet("this is a Genius"));
        }
    }//resposible for setting the marker on the map


    @SuppressLint("NewApi")
    private Bitmap createMarker(Context context, User user) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView markerImage = marker.findViewById(R.id.user_dp);

        //All of this isn't working
//        UniversalImageLoader.setImage(user.getProfile_photo(),markerImage, null, "");
//        Bitmap bm= ((BitmapDrawable)markerImage.getDrawable()).getBitmap();
//        Log.d(TAG, "createMarker: the bitmap " + bm);
//        markerImage.setImageBitmap(bm);


        //But this is
        markerImage.setImageBitmap(((BitmapDrawable)getDrawable(R.drawable.fire_emoji_with_color)).getBitmap());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }//this is the culprit. It gives the bitmap for the marker which does not work


    private User sortList(ArrayList<User> userList) {

        return null;
    }//this should sort the list and provide the highest panda points holder in the currently bounded map area whose marker will be the biggest on the map

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: map activity called.");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    try{
                        //the map does not show when the gps on the device is on(api key issues). Therefore this latlng is always null since i can't
                        // get the current location of the user from the gps, I am using hard coded coordinates
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "onComplete: latlng " + latLng.latitude + "  " + latLng.longitude);

                    }catch (NullPointerException e){
                        Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());
                        latLng = new LatLng(28.572945, 77.255721);
                    }

                    setLatlongs(latLng);

//                    mUserLocation.setGeo_point(geoPoint);
//                    mUserLocation.setTimestamp(null);
//                    saveUserLocation();
                }
            }
        });
    }



    @Override
    public void onMapReady(final GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        map.setMyLocationEnabled(true);


        //centers the map on the current user's location
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (task.getResult()!=null){
                        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(11);
                        map.moveCamera(center);
                        map.animateCamera(zoom);
                    }
                    else{
                        //for testing
                        LatLng latLng= new LatLng(28.572945, 77.255721);
                        CameraUpdate center=CameraUpdateFactory.newLatLng(latLng);
                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(11);
                        map.moveCamera(center);
                        map.animateCamera(zoom);
                    }

                }
            }
        });//moves the camera to the current location of the user


        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();

                fr = visibleRegion.farRight;
                fl = visibleRegion.farLeft;
                nr = visibleRegion.nearRight;
                nl = visibleRegion.nearLeft;


                Log.d(TAG, "onCameraChange: farRight " + fr);
                Log.d(TAG, "onCameraChange: farLeft " + fl);
                Log.d(TAG, "onCameraChange: nearRight " + nr);
                Log.d(TAG, "onCameraChange: nearLeft " + nl);

            }
        });//please ignore the crossed line, this provides us the coordinates of the corners of the area that is currently being viewed

        long time= System.currentTimeMillis()%10;
        int timeConst= (int) time;
        Log.d(TAG, "onMapReady: the constant received " + timeConst);
        boolean success;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            switch (timeConst){

                case 1:
                     success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.silver_map));
                    break;

                case 2:
                     success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.silver_map));
                    break;

                case 3:
                     success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.retro_map));
                    break;

                case 4:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.retro_map));
                    break;

                case 5:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.night_map));
                    break;

                case 6:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.night_map));
                    break;

                case 7:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.dark_map));
                    break;

                case 8:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.dark_map));
                    break;

                case 9:
                    success = map.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.aubergine_map));
                    break;


                    default:
                        success = map.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        this, R.raw.aubergine_map));
                        break;
            }//just some styling stuff
            mMap=map;

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }




































    /**
     --------------------------------------------------Permissions and stuff that no one should care about--------------------------------------------------------------
     */

    private void showLayout() {
//        relativeLayout.setVisibility(View.GONE);
//        linearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                gpsDialog.dismiss();
                //todo handle events after the request has been granted
                showLayout();
                getLastKnownLocation();
                initGoogleMap(mSavedInstanceState);
            }
            else{
                getLocationPermission();
            }
        }

    }

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                60,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                40,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                60);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                40);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void setLatlongs(LatLng latlng){

        myRef= FirebaseDatabase.getInstance().getReference();

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.db_timestamp))
                .setValue(System.currentTimeMillis());

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.db_latlng))
                .setValue(latlng);
    }//sets the current location of the user in the db


    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    private void initUserListRecyclerView(ArrayList<User> users) {
        mUserRecyclerAdapter = new RecyclerViewAdapter(mContext , users);
        mUserListRecyclerView.setAdapter(mUserRecyclerAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: map activity called.");
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void buildAlertMessageNoGps() {
        gpsDialog.setContentView(R.layout.layout_gps_dialog_box);
        ImageView cancelDialog = gpsDialog.findViewById(R.id.cancel_dialog);
        TextView yesDialog= gpsDialog.findViewById(R.id.enable_text);

        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsDialog.dismiss();
                Intent intent= new Intent(mContext, HomeActivity.class);
                startActivity(intent);
            }
        });

        yesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
            }
        });

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
            }
        });

        gpsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        gpsDialog.show();

    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: map activity called ");
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //todo handle events after the request has been granted
           

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: map activity checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;

            //todo if the user denies the google's dialog request, the system goes in an infinite loop. Fix that.
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: map activity called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    gpsDialog.dismiss();
                   //todo handle events after the request has been granted
                    
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: map activity called");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_full_screen_map: {

                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
                break;
            }

        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
