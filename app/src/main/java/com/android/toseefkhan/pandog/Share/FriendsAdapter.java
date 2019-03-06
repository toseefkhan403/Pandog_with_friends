package com.android.toseefkhan.pandog.Share;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private static final String TAG = "FriendsAdapter";
    FriendFilter filter;
    private String mUserUid;
    private ArrayList<User> friendUserList, usingList;
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private int mSelectedUserPosition = -1;


    public FriendsAdapter(String mUserUid, Context context) {
        this.mUserUid = mUserUid;
        friendUserList = new ArrayList<>();
        usingList = new ArrayList<>();
        this.mContext = context;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        getFriendsFromUid();
        initImageLoader();
    }

    public FriendsAdapter(User user, Context context) {
        Log.d(TAG, "FriendsAdapter: the selected user " + user);
        this.mContext = context;
        usingList = new ArrayList<>();
        friendUserList = new ArrayList<>();
        usingList.add(user);
        initImageLoader();
    }

    public void filter(CharSequence constraint) {
        if (filter == null) {
            filter = new FriendFilter();
        }
        filter.filter(constraint);
    }

    public int getSelectedUserPosition() {
        return mSelectedUserPosition;
    }

    private void getFriendsFromUid() {
        mDatabaseReference.child("followers").child(mUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
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
        mDatabaseReference.child("following").child(mUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                                usingList.add(user);
                                notifyItemInserted(usingList.indexOf(user));
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
    public int getItemCount() {
        if (usingList == null) {
            return 0;
        } else {
            return usingList.size();
        }
    }

    public User getItem(int position) {
        return usingList.get(position);
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.profile_item, parent, false);
        return new FriendViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {

        User user = getItem(position);
        if (getItemCount() == 1) {
            mSelectedUserPosition = position;
            holder.itemView.setBackgroundColor(Color.RED);
        }
        if (position == mSelectedUserPosition) {
            holder.itemView.setBackgroundColor(Color.RED);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.userNameView.setText(StringManipulation.expandUsername(user.getUsername()));
        holder.userEmailView.setText(user.getEmail());
        String Photourl = user.getProfile_photo();
        UniversalImageLoader.setImage(Photourl, holder.photoView, null, "", holder.child);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int p = mSelectedUserPosition;
                mSelectedUserPosition = position;
                FriendsAdapter.this.notifyItemChanged(p);
                FriendsAdapter.this.notifyItemChanged(mSelectedUserPosition);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private class FriendFilter {
        public FriendFilter() {

        }

        public void filter(CharSequence constraint) {
            if (constraint != null && constraint.length() > 0) {
                constraint = constraint.toString().toUpperCase();
                ArrayList<User> filteredList = new ArrayList<>();
                for (User user : friendUserList) {
                    if (user.getUsername().toUpperCase().contains(constraint)) {
                        filteredList.add(user);
                    }
                }
                usingList = filteredList;
                notifyDataSetChanged();
            } else {
                usingList = friendUserList;
                notifyDataSetChanged();
            }
            for (User user : usingList) {
                Log.v("Filtered Users",user.getUsername());
            }
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView userNameView, userEmailView;
        CircleImageView photoView;
        ProgressBar pb;
        View child, itemView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            userNameView = itemView.findViewById(R.id.UserNameView);
            userEmailView = itemView.findViewById(R.id.UserEmailView);
            photoView = itemView.findViewById(R.id.UserProfilePictureView);
            pb = itemView.findViewById(R.id.pb);
            child = itemView.findViewById(R.id.progress_child);
        }
    }
}
