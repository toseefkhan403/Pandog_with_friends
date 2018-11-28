package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;
import com.android.toseefkhan.pandog.Utils.StringManipulation;
import com.android.toseefkhan.pandog.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter implements Filterable {

    private ProfileFilter filter;
    String userUID;
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

        // TODO this is photoUrl
        String PhotoUrl = getPhotoUrlFromUser(user);
        // TODO this is photoView
        ImageView photoView = convertView.findViewById(R.id.UserProfilePictureView);

        //TODO Simply load the photo into PhotoView here


        //TODO also improve the ui of individual profilePhoto by editing profile_item
        return convertView;
    }

    private String getPhotoUrlFromUser(User user) {

        final String photoUrl[] = new String[1];
        databaseReference.child("user_account_setting").child(user.getUser_id()).child("profile_photo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            photoUrl[0] = dataSnapshot.getValue(String.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("ErrorGettingPr.Photo", databaseError.toString());
                    }
                });
        return photoUrl[0];
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ProfileFilter();
        }
        return filter;
    }

    private class ProfileFilter extends Filter {

        public ProfileFilter() {
        }

        @Override

        protected FilterResults performFiltering(final CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                final List<User> users = new ArrayList<>();
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                filterResults.count = users.size();
                filterResults.values = users;
            } else {
                filterResults.count = 0;
                filterResults.values = null;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ProfileList = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    }
}