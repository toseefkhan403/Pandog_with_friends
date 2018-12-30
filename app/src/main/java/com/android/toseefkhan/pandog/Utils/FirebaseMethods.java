package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.Home.HomeActivity;
import com.android.toseefkhan.pandog.Profile.ProfileActivity;
import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.models.Challenge;
import com.android.toseefkhan.pandog.models.Photo;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


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
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        myRef = mFirebaseDatabase.getReference();
        mContext = context;

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

                            Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onSuccess: this is the filepath " + filePaths.FIREBASE_IMAGE_STORAGE);

                            ((Activity)mContext).finish();

                            //navigate to the main feed so the user can see their photo
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Photo upload failed.");
                            Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: progress " + String.format("%.0f", progress));

                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                                builder.setMessage("Photo upload " + (int)progress + "% done ")
                                        .setCancelable(false)
                                        .setView(R.layout.layout_progress_dialog);
                                final AlertDialog alert = builder.create();
                                alert.show();
                                if (progress >= 100) {
                                    alert.dismiss();
                                }

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
                            Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();

                            ((Activity)mContext).finish();

                            //navigate to the profileActivity so the user can see their photo
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            mContext.startActivity(intent);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Photo upload failed.");
                            Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: progress " + String.format("%.0f", progress));

                                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage("Photo upload " + (int)progress + "% done")
                                        .setCancelable(false)
                                        .setView(R.layout.layout_progress_dialog);
                                final AlertDialog alert = builder.create();
                                alert.show();
                                if (progress >= 100) {
                                    alert.dismiss();
                                }
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
        bmp = Bitmap.createScaledBitmap(bmp, 120 , 120, true);

        byte[] bytes = ImageManager.getBytesFromBitmap(bmp, 100);

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

    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child(newPhotoKey).setValue(photo);

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String selecteduserUid = mSelectedUser.getUser_id();
        Challenge mChallenge = new Challenge(currentUserUid, selecteduserUid, newPhotoKey, url);
        mChallenge.setStatus("NOT_DECIDED");

        String challengeKey = myRef.child("Challenges").push().getKey();
        myRef.child("Challenges").child(challengeKey).setValue(mChallenge);

        DatabaseReference challengeReference = myRef.child("User_Challenges");
        challengeReference.child(currentUserUid).push().setValue(challengeKey);
        challengeReference.child(selecteduserUid).push().setValue(challengeKey);

        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

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
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();

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

                            }else{
                                Toast.makeText(mContext, "couldn't send verification email.", Toast.LENGTH_SHORT).show();
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
    public void addNewUser(String email, String username, String description, String profile_photo){

        User user = new User( userID,  email,  StringManipulation.condenseUsername(username) );

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);


        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
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


    public void uploadNewPhoto(String photoType, String caption, String imageName, String imgUrl, Uri imageUri, User selectedUser) {
        uploadNewPhoto(photoType, caption, imageName, imgUrl, imageUri);
        mSelectedUser = selectedUser;

    }
}
