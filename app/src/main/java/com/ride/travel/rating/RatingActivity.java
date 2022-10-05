package com.ride.travel.rating;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ride.travel.R;
import com.ride.travel.models.RatingItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.ride.travel.Utils.Constants;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private DatabaseReference mRatingDatabase;
    private ArrayList<Float> list;
    private float ratingx;
    private TextView ratingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        setupDatabase();
        setupViews();
    }

    private void setupViews()
    {
        final TextView mNameOfUser = findViewById(R.id.ID_rating_name_of_user);


        // Initialize RatingBar
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ID_rating_text);

        final EditText ratingText =  findViewById(R.id.ID_rating_edittext);

        Button submitButton = findViewById(R.id.ID_submit_btn);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        String.valueOf(ratingBar.getRating()), Toast.LENGTH_LONG).show();
                String pushedKey = mRatingDatabase.push().getKey();

                RatingItem ratingItem = new RatingItem(pushedKey
                        ,ratingText.getText().toString().trim()
                        ,ratingBar.getRating());
                mRatingDatabase.child("SomeTravelShit").child(pushedKey).setValue(ratingItem);
            }
        });





    }

    private void setupDatabase()
    {
        ratingx =0 ;
        list = new ArrayList<Float>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mRatingDatabase = mDatabase.child(Constants.DATABASE_RATING);

        mRatingDatabase.child("SomeTravelShit").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    RatingItem ratingItem = ds.getValue(RatingItem.class);
                    float rating = ratingItem.getRating();
                    ratingx = ratingx + rating;
                    list.add(rating);
                    if (list.size() == dataSnapshot.getChildrenCount())
                    {
                        float finalRating = ratingx / dataSnapshot.getChildrenCount();
//                        float fin = (float) (Math.round(finalRating * 100.0) / 100.0);
                        String ratingFinalText = String.format("%.1f", finalRating);
                        ratingText.setText(ratingFinalText);
                        ratingBar.setRating(finalRating);
                        hideLoadingScreen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void hideLoadingScreen()
    {}

}
