package com.android.toseefkhan.pandog.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Login.LoginActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Challenge;
import com.android.toseefkhan.pandog.models.MyMention;
import com.android.toseefkhan.pandog.models.Post;
import com.android.toseefkhan.pandog.models.User;
import com.android.toseefkhan.pandog.models.UserAccountSettings;
import com.android.toseefkhan.pandog.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;


public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private ProgressDialog pd;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    private User mSelectedUser;
    //vars
    private Context mContext;
    private Resources r;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        myRef = mFirebaseDatabase.getReference();
        mContext = context;
        r = mContext.getResources();
        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(final String photoType, final String caption, final String imageName, final String imgUrl,final Uri imageUri) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");

        final Dialog uploadDialog= new Dialog(mContext);
        uploadDialog.setContentView(R.layout.layout_confirmation_dialog);
        ImageView cancelDialog = uploadDialog.findViewById(R.id.cancel_dialog);
        TextView yesDialog= uploadDialog.findViewById(R.id.tvYes);
        TextView noDialog= uploadDialog.findViewById(R.id.tvNo);

        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialog.dismiss();
            }
        });
        noDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialog.dismiss();
            }
        });

        yesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FilePaths filePaths = new FilePaths();
                //case1) new photo
                if (photoType.equals(mContext.getString(R.string.new_photo))) {
                    Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

                    final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final StorageReference storageReference = mStorageReference
                            .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/" + imageName);

                    //convert image url to bitmap
                    UploadTask uploadTask = storageReference.putFile(imageUri);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                            mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/" + imageName)
                                    .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    addPhotoToDatabase(caption, task.getResult().toString());
                                }
                            });

                            Toasty.success(mContext, "Photo upload success", Toast.LENGTH_SHORT,true).show();
                            Log.d(TAG, "onSuccess: this is the filepath " + filePaths.FIREBASE_IMAGE_STORAGE);

                            ((Activity)mContext).finish();

                            //navigate to the main feed so the user can see their photo
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            intent.putExtra("ChallengerUser",2);
                            mContext.startActivity(intent);
                            System.gc();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Photo upload failed.");
                            Toasty.error(mContext, "Photo upload failed ", Toast.LENGTH_SHORT,true).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: progress " + String.format("%.0f", progress));

                            uploadDialog.dismiss();
                            Toasty.info(mContext, "Photo upload " + (int)progress + "% done", Toast.LENGTH_SHORT,true).show();

                            SquareDrawable indicator = new TriangleDrawable(new int[]{r.getColor(R.color.teal_400), r.getColor(R.color.brown_400)
                                    , r.getColor(R.color.light_blue_400)});
                            indicator.setPadding(20);
                            View child = ((Activity)mContext).findViewById(R.id.progress_root);;
                            child.setBackground(indicator);
                            final Animatable animatable = (Animatable) indicator;
                            animatable.start();
                            animatable.start();

                        }

                    });

                }
                //case new profile photo
                else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
                    Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");

                    final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final StorageReference storageReference = mStorageReference
                            .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

                    UploadTask uploadTask = storageReference.putFile(imageUri);
                    Log.d(TAG, "uploadNewPhoto: this is the uri " + imageUri);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                            mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo")
                                    .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    //insert into 'user_account_settings' node and 'users' node
                                    setProfilePhoto(task.getResult().toString());
                                }
                            });

                            uploadBitmap(imageUri);
                            Toasty.success(mContext, "Photo upload success", Toast.LENGTH_SHORT,true).show();

                            ((Activity)mContext).finish();

                            //navigate to the profileActivity so the user can see their photo
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            mContext.startActivity(intent);
                            System.gc();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Photo upload failed.");
                            Toasty.error(mContext, "Photo upload failed ", Toast.LENGTH_SHORT,true).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: progress " + String.format("%.0f", progress));

                            uploadDialog.dismiss();
                            Toasty.info(mContext, "Photo upload " + (int)progress + "% done", Toast.LENGTH_SHORT,true).show();


                            SquareDrawable indicator = new SquareSpinDrawable(new int[]{r.getColor(R.color.amber_400), r.getColor(R.color.blue_400)
                                    , r.getColor(R.color.deep_orange_400), r.getColor(R.color.lime_400)});
                            indicator.setPadding(20);
                            View child = ((Activity)mContext).findViewById(R.id.progress_root);
                            child.setBackground(indicator);
                            final Animatable animatable = (Animatable) indicator;
                            animatable.start();
                        }
                    });
                }
            }
        });

        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadDialog.show();
    }

    public void uploadNewPhoto(final String photoType, final String caption, final String imageName,
                               final String imgUrl, final Uri imageUri,User selectedUser, final String challengeKey,ArrayList<MyMention> mentions) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");
        mSelectedUser = selectedUser;
        this.mentions = mentions;

        final Dialog uploadDialog= new Dialog(mContext);
        uploadDialog.setContentView(R.layout.layout_confirmation_dialog);
        ImageView cancelDialog = uploadDialog.findViewById(R.id.cancel_dialog);
        TextView yesDialog= uploadDialog.findViewById(R.id.tvYes);
        TextView noDialog= uploadDialog.findViewById(R.id.tvNo);

        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialog.dismiss();
            }
        });
        noDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialog.dismiss();
            }
        });

        yesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FilePaths filePaths = new FilePaths();

                if (photoType.equals(mContext.getString(R.string.post_photo))){
                        Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

                        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final StorageReference storageReference = mStorageReference
                                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/" + imageName);

                        UploadTask uploadTask = storageReference.putFile(imageUri);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/" + imageName)
                                        .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        addPhotoToDatabase(caption, task.getResult().toString(), challengeKey);
                                    }
                                });

                                Log.d(TAG, "onSuccess: this is the filepath " + filePaths.FIREBASE_IMAGE_STORAGE);

                                ((Activity)mContext).finish();

                                //navigate to the main feed so the user can see their photo
                                Intent intent = new Intent(mContext, HomeActivity.class);
                                mContext.startActivity(intent);
                                System.gc();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Photo upload failed.");
                                Toasty.error(mContext, "Photo upload failed ", Toast.LENGTH_SHORT,true).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                Log.d(TAG, "onProgress: progress " + String.format("%.0f", progress));
                                uploadDialog.dismiss();
                                Toasty.info(mContext, "Photo upload " + (int)progress + "% done", Toast.LENGTH_SHORT,true).show();

                                SquareDrawable indicator = new SquareSpinDrawable(new int[]{r.getColor(R.color.deep_purple_400), r.getColor(R.color.brown_400)
                                        , r.getColor(R.color.deep_orange_400), r.getColor(R.color.lime_400)});
                                indicator.setPadding(20);
                                View child = ((Activity)mContext).findViewById(R.id.progress_root);;
                                child.setBackground(indicator);
                                final Animatable animatable = (Animatable) indicator;
                                animatable.start();

                            }
                        });
                }
            }
        });

        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadDialog.show();
    }

    private void uploadBitmap(Uri imageUri) {

        final FilePaths filePaths = new FilePaths();
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference storageReference2 = mStorageReference
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo_bitmap");

        InputStream imageStream = null;
        try {
            imageStream = mContext.getContentResolver().openInputStream(
                    imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);
//        int width = bmp.getWidth()/4;
//        int height = bmp.getHeight()/4;
//        bmp = Bitmap.createScaledBitmap(bmp, width , height, true);

        int maxHeight = 320;
        int maxWidth = 320;
        float scale = Math.min(((float)maxHeight / bmp.getWidth()), ((float)maxWidth / bmp.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        byte[] bytes = ImageManager.getBytesFromBitmap(bmp, 20);

        UploadTask uploadTask2 = storageReference2.putBytes(bytes);
        Log.d(TAG, "uploadNewPhoto: this is the uri " + imageUri);

        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //  Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo_bitmap")
                        .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        //insert into 'user_account_settings' node and 'users' node
                        setProfilePhotoBitmap(task.getResult().toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Bitmap upload failed.");
            }
        });
    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private void setProfilePhotoBitmap(String url) {
        Log.d(TAG, "setProfilePhoto: setting new profile image bitmap: " + url);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("bitmap")
                .setValue(url);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, final String url) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

        String tags = StringManipulation.getTags(caption);

        final String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String selecteduserUid = mSelectedUser.getUser_id();
        final Challenge mChallenge = new Challenge(currentUserUid, selecteduserUid, url,caption,tags);
        mChallenge.setStatus("NOT_DECIDED");
        final String challengedUserName = mSelectedUser.getUsername();
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(currentUserUid)
                .child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String challengerUserName = dataSnapshot.getValue(String.class);
                            mChallenge.setChallengedName(challengedUserName);
                            mChallenge.setChallengerName(challengerUserName);
                            String challengeKey = myRef.child("Challenges").push().getKey();
                            mChallenge.setChallengeKey(challengeKey);
                            mChallenge.setMentions(mentions);
                            myRef.child("Challenges").child(challengeKey).setValue(mChallenge);

                            DatabaseReference challengeReference = myRef.child("User_Challenges");
                            challengeReference.child(currentUserUid).child(challengeKey).setValue(challengeKey);
                            challengeReference.child(selecteduserUid).child(challengeKey).setValue(challengeKey);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addPhotoToDatabase(final String caption, final String url, final String challengeKey) {
        Log.d(TAG, "addPhotoToDatabase: challenge key " + challengeKey);

        final String tags = StringManipulation.getTags(caption);

        DatabaseReference dref = FirebaseDatabase.getInstance().getReference();
        dref.child("Challenges").child(challengeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Challenge c = dataSnapshot.getValue(Challenge.class);
                Post post = new Post(c.getPhotoUrl(),c.getCaption(),c.getChallengerUserUid(),c.getTags()
                ,url, caption, c.getChallengedUserUid(), tags );
                Log.d(TAG, "onDataChange: post " + post);
                String postKey = myRef.child("Posts").push().getKey();
                post.setPostKey(postKey);
                post.setStatus("ACTIVE");
                post.setTimeStamp(System.currentTimeMillis());
                post.setChallenge_id(challengeKey);

                long timeStamp = System.currentTimeMillis();
                post.setTimeStamp(timeStamp);
                Log.d(TAG, "onDataChange: getting the mentions " + c.getMentions());

                HashMap<String,ArrayList<MyMention>> hashMap = new HashMap<>();

                hashMap.put(c.getChallengerUserUid(),c.getMentions());
                hashMap.put(c.getChallengedUserUid(),mentions);
                post.setMention_hash_map(hashMap);

                addPostToDataBase(post, postKey);

                addPostToUserNode(c.getChallengerUserUid(),postKey);
                c.setPostKey(postKey);
                c.setStatus("ACCEPTED");
                //delete the challenge as it is accepted successfully
                myRef.child("Challenges").child(c.getChallengeKey()).setValue(c);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPostToUserNode(String challengerUid, String postKey) {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef.child("user_posts").child(userUid).child(postKey).setValue(postKey);
        myRef.child("user_posts").child(challengerUid).child(postKey).setValue(postKey);
    }

    private void addPostToDataBase(Post post, String postKey){

        myRef.child("Posts").child(postKey).setValue(post);
        Toasty.success(mContext, "Your post is up for voting!", Toast.LENGTH_SHORT,true).show();
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .getChildren()) {
            count++;
        }

        return count;
    }


    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param description
     */
    public void updateUserAccountSettings(String displayName, String description){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if(displayName != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }


        if(description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
    }

    /**
     * update username in the 'users' node and 'user_account_settings' node
     * @param username
     */
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: upadting username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toasty.error(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT,true).show();

                        } else if (task.isSuccessful()) {
                            //send verificaton email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed: " + userID);
                        }

                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent i = new Intent(mContext,LoginActivity.class);
                                mContext.startActivity(i);

                                Toasty.success(mContext, "Signup successful. Sending verification email.", Toast.LENGTH_LONG,true).show();
                            }else{
                                Toasty.error(mContext, "Couldn't send verification email", Toast.LENGTH_SHORT,true).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add information to the users nodes
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param profile_photo
     */
    public void addNewUser(String email, String username,String displayName, String description, String profile_photo){

        User user = new User( null,userID,  email,  StringManipulation.condenseUsername(username),"GREY",0 );

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                displayName,
                profile_photo,
                StringManipulation.condenseUsername(username)
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the account settings for the user currently logged in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.");

        UserAccountSettings settings  = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()){

            // user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }

                // users node
                if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                    Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername()
                    );
                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
                }

        }
        return new UserSettings(user, settings);

    }

    private ArrayList<MyMention> mentions;

    public void uploadNewPhoto(String photoType, String caption, String imageName, String imgUrl, Uri imageUri, User selectedUser, ArrayList<MyMention> mentions) {
        uploadNewPhoto(photoType, caption, imageName, imgUrl, imageUri);
        mSelectedUser = selectedUser;
        this.mentions = mentions;
    }
}
