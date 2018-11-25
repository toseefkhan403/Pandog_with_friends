package com.android.toseefkhan.pandog.Search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
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
    private ArrayList<String> ProfileList;
    private Context mContext;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public SearchAdapter(Context context) {
        this.mContext = context;
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("users");
        ProfileList = new ArrayList<>();
        ;
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

        TextView profileTextView = convertView.findViewById(R.id.ProfileTextView);
        profileTextView.setText(getItem(position).toString());

        return convertView;
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
                final List<String> userNames = new ArrayList<>();
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot childData : dataSnapshot.getChildren()) {
                                User user = childData.getValue(User.class);
                                String Username = user.getUsername().toUpperCase();
                                String entered = constraint.toString().toUpperCase();
                                if (Username.contains(entered)) {
                                    userNames.add(StringManipulation.expandUsername(user.getUsername()));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                filterResults.count = userNames.size();
                filterResults.values = userNames;
            } else {
                filterResults.count = 0;
                filterResults.values = null;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ProfileList = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }
}
