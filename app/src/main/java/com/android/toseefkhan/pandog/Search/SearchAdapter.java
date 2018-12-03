package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.StringManipulation;
import com.android.toseefkhan.pandog.Utils.UniversalImageLoader;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends BaseAdapter {
    private static final String TAG = "SearchAdapter";
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
    public int getCount() {
        if (ProfileList == null) {
            return 0;
        } else {
            return ProfileList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return ProfileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.profile_item, parent, false);
        }

        User user = (User) getItem(position);

        TextView userNameView = convertView.findViewById(R.id.UserNameView);
        userNameView.setText(StringManipulation.expandUsername(user.getUsername()));

        TextView userEmailView = convertView.findViewById(R.id.UserEmailView);
        userEmailView.setText(user.getEmail());


        String PhotoUrl = user.getProfile_photo();

        CircleImageView photoView = convertView.findViewById(R.id.UserProfilePictureView);
        UniversalImageLoader.setImage(PhotoUrl, photoView, null, "");
        return convertView;
    }


    public void filter(CharSequence constraint) {
        if (filter == null) {
            filter = new ProfileFilter();
        }
        filter.filter(constraint);
    }

    private class ProfileFilter {

        public ProfileFilter() {
        }


        public void filter(final CharSequence constraint) {

            if (constraint != null && constraint.length() > 0) {
                final ArrayList<User> users = new ArrayList<>();
                databaseReference.child("users").addValueEventListener(new ValueEventListener() {
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
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }
    }
}