package com.android.toseefkhan.pandog.Search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.toseefkhan.pandog.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReferActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referral_activity);

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView share = findViewById(R.id.ref_code);
        TextView share2 = findViewById(R.id.ref_code_long_click);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String username = dataSnapshot.getValue(String.class);

                        share.setText(username);

                        share.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Share your selfies with the world using the Celfie app! Compete " +
                                        "with your friends with your selfies and let them know that you are the best! \nRegister " +
                                        "now using my referral code and get 50 bonus points upon joining! \napp link goes here" + " The referral code is " + share.getText().toString());
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent, "Share Celfie with..."));

                                share2.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {

                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Share your selfies with the world using the Celfie app! Compete " +
                                                "with your friends with your selfies and let them know that you are the best! \nRegister " +
                                                "now using my referral code and get 50 bonus points upon joining! \napp link goes here" + " The referral code is " + share.getText().toString());
                                        shareIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(shareIntent, "Share Celfie with..."));

                                        return true;
                                    }
                                });


                                return true;
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
