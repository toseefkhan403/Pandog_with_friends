package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class FriendsAdapter extends BaseAdapter {
    private String mUserUid;
    private ArrayList<User> friendUserList;
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private int mSelectedUserPosition = -1;

    public FriendsAdapter(String mUserUid, Context context) {
        this.mUserUid = mUserUid;
        friendUserList = new ArrayList<>();
        this.mContext = context;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        getFriendsFromUid();
    }

    public int getSelectedUserPosition() {
        return mSelectedUserPosition;
    }

    private void getFriendsFromUid() {
        mDatabaseReference.child("followers").child(mUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Friends:followers", databaseError.toString());
                Toast.makeText(mContext, "Error Occured", Toast.LENGTH_LONG).show();
            }
        });
        mDatabaseReference.child("following").child(mUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Friends:following", databaseError.toString());
                Toast.makeText(mContext, "Error Occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUsers(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                String UserUid = userSnapshot.getKey();
                adduserFromUid(UserUid);
            }
        }
    }

    private void adduserFromUid(String userUid) {
        Log.i("addingUser", userUid);
        mDatabaseReference.child(mContext.getString(R.string.dbname_users)).child(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            boolean repeated = false;
                            for (User friend : friendUserList) {
                                if (friend.getUser_id().equals(user.getUser_id())) {
                                    repeated = true;
                                    break;
                                }
                            }
                            if (!repeated) {
                                friendUserList.add(user);
                                notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("Friends:AddingUser", databaseError.toString());
                    }
                });
    }

    @Override
    public int getCount() {
        if (friendUserList == null) {
            return 0;
        } else {
            return friendUserList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return friendUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.profile_item, parent, false);
        }
        User user = (User) getItem(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedUserPosition = position;
                notifyDataSetChanged();
            }
        });

        Log.i("User", user.getUser_id());
        if (position == mSelectedUserPosition) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.skyBlue));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        TextView userNameView = convertView.findViewById(R.id.UserNameView);
        userNameView.setText(StringManipulation.expandUsername(user.getUsername()));

        TextView userEmailView = convertView.findViewById(R.id.UserEmailView);
        userEmailView.setText(user.getEmail());

        String PhotoUrl = user.getProfile_photo();

        CircleImageView photoView = convertView.findViewById(R.id.UserProfilePictureView);
        ProgressBar pb = convertView.findViewById(R.id.pb);
        UniversalImageLoader.setImage(PhotoUrl, photoView, pb, "");

        return convertView;
    }
}
