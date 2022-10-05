package com.ride.travel.rating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;
import com.ride.travel.Utils.Constants;
import com.ride.travel.adapters.RatingAdapter;
import com.ride.travel.models.RatingItem;

import java.util.ArrayList;

import static com.ride.travel.Utils.Constants.DATABASE_RATING;
import static com.ride.travel.Utils.Constants.DATABASE_USERS;

public class ViewRatingOfUser extends AppCompatActivity {


    private String userID;
    private ArrayList<RatingItem> ratingItems;
    private RatingAdapter ratingAdapter;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_of_one_user);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userID = extras.getString(Constants.INTENT_USER_ID);
            //The key argument here must match that used in the other activity
            setupViews();
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mRatingDatabase = mDatabase.child(DATABASE_RATING);
    }


    private void setupViews() {
        final TextView mNameOfUser = findViewById(R.id.ID_rating_name_of_user);

          mProgressBar = findViewById(R.id.ID_rating_progressBar);

        final TextView mMyNameTextView = findViewById(R.id.ID_my_rating_name);
          FirebaseDatabase.getInstance().getReference().child(DATABASE_USERS).child(userID).child("name")
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          mMyNameTextView.setText(dataSnapshot.getValue(String.class));
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });




        final RecyclerView mRecyclerView = findViewById(R.id.ID_rating_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setVisibility(View.VISIBLE);


        ratingItems = new ArrayList<>();


        ratingAdapter = new RatingAdapter(this, ratingItems);
        mRecyclerView.setAdapter(ratingAdapter);
        setupRecyclerViewWithNewData(userID);





        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_rating_toolbar);
        setSupportActionBar(mToolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    private void setupRecyclerViewWithNewData(String idOfUser) {
        FirebaseDatabase.getInstance().getReference().child(DATABASE_RATING)
                .child(idOfUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    RatingItem ratingItem = ds.getValue(RatingItem.class);
                    ratingItems.add(ratingItem);
                    ratingAdapter.notifyDataSetChanged();
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }


        return super.onOptionsItemSelected(item);
    }


}
