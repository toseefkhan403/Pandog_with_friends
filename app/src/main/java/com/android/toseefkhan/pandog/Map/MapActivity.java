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
import android.graphics.Paint;
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
import android.support.design.widget.Snackbar;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.BottomNavViewHelper;
import com.android.toseefkhan.pandog.Utils.RecyclerViewAdapter;
import com.android.toseefkhan.pandog.Utils.ViewWeightAnimationWrapper;
import com.android.toseefkhan.pandog.models.User;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.koushikdutta.ion.Ion;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import de.hdodenhof.circleimageview.CircleImageView;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
    ,View.OnClickListener{


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
    private ArrayList<User> mUserListViewable = new ArrayList<>();
    private RecyclerViewAdapter mUserRecyclerAdapter;
    private RelativeLayout relativeLayout;
    private LinearLayout linearLayout;
    private TextView gps;
    private Bundle mSavedInstanceState;
    private LatLng latLng;
    private GoogleMap mMap;
    private RelativeLayout mMapContainer;
    private CircleImageView pp;
    private ProgressBar mProgressbar, dialogProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        mProgressbar = findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.VISIBLE);
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
                //getUsersFromArea();
            } else {
                getLocationPermission();
            }
        }

        getUsersFromArea();
    }

    private void getUsersFromArea(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    try{
                        User user= singleSnapshot.getValue(User.class);
                        mUserList.add(user);
                        }catch (Exception e){
                            Log.d("Error", "onDataChange: NullPointerException " + e.getMessage());
                        }
                }
                setMarkerswithLevels(mUserList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }//gives the list of ALL the users of the application

    private ArrayList<Marker> markerList = new ArrayList<>();


    private void setMarkerswithLevels(ArrayList<User> mUserList) {


        for (int i=0; i<mUserList.size(); i++){
            LatLng latLng= new LatLng(mUserList.get(i).getLat_lng().getLatitude(), mUserList.get(i).getLat_lng().getLongitude());
            Marker marker=mMap.addMarker(new MarkerOptions().position(latLng).
                    icon(BitmapDescriptorFactory.fromBitmap(createMarker(mContext,mUserList.get(i))))
                    .title(mUserList.get(i).getUsername())
                    .snippet("Points: " + String.valueOf(mUserList.get(i).getPanda_points())));
            marker.setTag(mUserList.get(i));
            markerList.add(marker);                 //needs this for the recycler view
        }

        checkVisibility(markerList);

    }

    private View marker;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    private Bitmap createMarker(Context context, final User user) {

        switch (user.getLevel()){

            case "BLACK":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
                break;

            case "PURPLE":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout2, null);
                break;

            case "BLUE":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout3, null);
                break;

            case "GREEN":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout4, null);
                break;

            case "GREY":
                marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout5, null);
                break;

        }

        final CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        try {
            Bitmap bmImg =  Ion.with(mContext)
                    .load(user.getBitmap())
                    .asBitmap().get();
            markerImage.setImageBitmap(bmImg);
        } catch (Exception e) {
            Log.d("Error", "createMarker: error");
        }

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
    }

    private void getLastKnownLocation() {
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

                    }catch (NullPointerException e){
                        Log.d("Error", "onComplete: NullPointerException " + e.getMessage());
                        latLng = new LatLng(28.582945, 77.255721);
                    }

                    setLatlongs(latLng);
//                    saveUserLocation();
                }
            }
        });
    }

    private void checkVisibility(ArrayList<Marker> markerList){
        boolean show=false;
        if(mMap != null){

            LatLngBounds latLongBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

            for (int i=0; i<markerList.size() ; i++){

                if(latLongBounds.contains(markerList.get(i).getPosition())){
                    //If the marker is within the the bounds of the screen add it to the ArrayList
                    User user = (User) markerList.get(i).getTag();
                    if (!mUserListViewable.contains(user)){
                        mUserListViewable.add(user);
                    }
                    show= true;
                    mUserListRecyclerView.setVisibility(View.VISIBLE);
                }
            }
            if (!show){
                mUserListRecyclerView.setVisibility(View.INVISIBLE);
                Snackbar snackbar= Snackbar.make(mMapView,"Uh-oh! Looks like no one interesting lives here",Snackbar.LENGTH_SHORT);
                snackbar.show();
            }

            if (!mUserListViewable.isEmpty()){
                initUserListRecyclerView(sortList(mUserListViewable));
            }
        }
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

        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

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
            public void onCameraChange(CameraPosition cameraPosition) {
                mUserListViewable.clear();
                checkVisibility(markerList);
            }
        });

        long time= System.currentTimeMillis()%10;
        int timeConst= (int) time;
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

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.layout_info_window, null);

                    pp = v.findViewById(R.id.profile_photo_infow);
                    TextView username = v.findViewById(R.id.username_infow);
                    TextView description = v.findViewById(R.id.description_infow);
                    User user= (User) marker.getTag();

                    try {
                        Bitmap bmImg = Ion.with(mContext)
                                .load(user.getBitmap()).asBitmap().get();
                        pp.setImageBitmap(bmImg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    username.setText(marker.getTitle());
                    description.setText(marker.getSnippet());

                    return v;
                }
            });

            if (!success) {
                Log.e("error", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("error", "Can't find style. Error: ", e);
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
        mProgressbar.setVisibility(View.GONE);
        dialogProgressbar.setVisibility(View.GONE);
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
        dialogProgressbar = gpsDialog.findViewById(R.id.dialogProgressBar);
        dialogProgressbar.setVisibility(View.VISIBLE);

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
            showLayout();
            getLastKnownLocation();
           

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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            return true;

            //todo if the user denies the google's dialog request, the system goes in an infinite loop. Fix that.
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
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
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    gpsDialog.dismiss();
                   //todo handle events after the request has been granted
                    showLayout();
                    getLastKnownLocation();
                    
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

    private ArrayList<User> sortList(ArrayList<User> userList) {

        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return Integer.valueOf(o2.getPanda_points()).compareTo(Integer.valueOf(o1.getPanda_points()));
            }
        });

        return userList;
    }//this should sort the list and provide the highest panda points holder in the currently bounded map area whose marker will be the biggest on the map


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
