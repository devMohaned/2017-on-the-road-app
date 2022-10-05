package com.ride.travel.rating;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.adapters.RatingAdapter;
import com.ride.travel.models.RatingItem;
import com.ride.travel.R;
import com.ride.travel.Utils.Constants;
import com.ride.travel.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ride.travel.HomePage.mAuth;
import static com.ride.travel.Utils.Constants.DATABASE_RATING;

public class GiveRatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private DatabaseReference mRatingDatabase;
    private ArrayList<Float> list = new ArrayList<Float>();
    private EditText ratingText;
    private TextView ratingTextView;

    private float ratingx;
    private String userID;
    private ArrayList<RatingItem> ratingItems;
    private RatingAdapter ratingAdapter;
    private boolean userFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_rating);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mRatingDatabase = mDatabase.child(DATABASE_RATING);
        setupViews();
    }


    private void setupViews() {
        final TextView mNameOfUser = findViewById(R.id.ID_rating_name_of_user);

        final ProgressBar mProgressBar = findViewById(R.id.ID_rating_progressBar);

        final RelativeLayout mRatingLayout = findViewById(R.id.ID_rating_relative_layout);
        final LinearLayout mSearchingLayout = findViewById(R.id.ID_search_using_email_linear_layout);

        final RelativeLayout mMyRatingLayout = findViewById(R.id.ID_my_rating_relative_layout);

        // Initialize RatingBar
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

         ratingText = findViewById(R.id.ID_rating_feedback_edittext);

        Button submitButton = findViewById(R.id.ID_submit_rating_btn);

        ratingTextView =findViewById(R.id.ID_rating_text);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFound) {
                    addRating(userID);
                } else {
                    Toast.makeText(GiveRatingActivity.this, "User is not found", Toast.LENGTH_LONG).show();
                }
            }
        });


        final EditText emailEditText = findViewById(R.id.ID_rating_email_of_user);
        Button searchButton = findViewById(R.id.ID_search_using_email_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String email = emailEditText.getText().toString();

                if (email.contains("@") && email.length() > 3) {
                    Query query = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.DATABASE_USERS).orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mProgressBar.setVisibility(View.GONE);
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                User user = ds.getValue(User.class);
                                if (user != null) {
                                    mNameOfUser.setText(user.getName());
                                    mProgressBar.setVisibility(View.GONE);
                                    mSearchingLayout.setVisibility(View.GONE);
                                    mRatingLayout.setVisibility(View.VISIBLE);
                                    userID = user.getId();
                                    setupDatabase(ds.getKey());
                                    userFound = true;
                                } else {
                                    mNameOfUser.setText("Name isn't found");
                                }

                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            mProgressBar.setVisibility(View.GONE);
                            mNameOfUser.setText("Name isn't found");
                        }
                    });

                } else {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(GiveRatingActivity.this, "E-mail is not correct", Toast.LENGTH_LONG).show();
                }

            }
        });


        TextView mMyNameTextView = findViewById(R.id.ID_my_rating_name);
        mMyNameTextView.setText(mAuth.getCurrentUser().getDisplayName());


        final RatingBar mSmallRatingBar = findViewById(R.id.ID_my_rating_bar);
        final TextView mSmallRatingTextView = findViewById(R.id.ID_my_rating_score);

        mRatingDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    RatingItem ratingItem = ds.getValue(RatingItem.class);
                    float rating = ratingItem.getRating();
                    ratingx = ratingx + rating;
                    list.add(rating);
                    if (list.size() == dataSnapshot.getChildrenCount()) {
                        float finalRating = ratingx / dataSnapshot.getChildrenCount();
//                        float fin = (float) (Math.round(finalRating * 100.0) / 100.0);
                        String ratingFinalText = String.format("%.1f", finalRating);
                        mSmallRatingTextView.setText(ratingFinalText);
                        mSmallRatingBar.setRating(finalRating);
//                        hideLoadingScreen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final RecyclerView mRecyclerView = findViewById(R.id.ID_rating_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        ratingItems = new ArrayList<>();


        ratingAdapter = new RatingAdapter(this, ratingItems);
        mRecyclerView.setAdapter(ratingAdapter);
        setupRecyclerViewWithNewData();


        TextView mViewAllTextView = findViewById(R.id.ID_view_all);
        mViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        Button giveRating = findViewById(R.id.ID_give_rating_button);
        giveRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setVisibility(View.GONE);
                mMyRatingLayout.setVisibility(View.GONE);
                mSearchingLayout.setVisibility(View.VISIBLE);
                mRatingLayout.setVisibility(View.GONE);
            }
        });


        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_rating_toolbar);
        setSupportActionBar(mToolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    private void setupRecyclerViewWithNewData() {
        FirebaseDatabase.getInstance().getReference().child(DATABASE_RATING)
                .child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    com.ride.travel.models.RatingItem ratingItem = ds.getValue(com.ride.travel.models.RatingItem.class);
                    ratingItems.add(ratingItem);
                    ratingAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setupDatabase(String key) {
        ratingx = 0;


        mRatingDatabase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    com.ride.travel.models.RatingItem ratingItem = ds.getValue(com.ride.travel.models.RatingItem.class);
                    float rating = ratingItem.getRating();
                    ratingx = ratingx + rating;
                    list.add(rating);
                    if (list.size() == dataSnapshot.getChildrenCount()) {
                        float finalRating = ratingx / dataSnapshot.getChildrenCount();
//                        float fin = (float) (Math.round(finalRating * 100.0) / 100.0);
                        String ratingFinalText = String.format("%.1f", finalRating);
                        ratingTextView.setText(ratingFinalText);
                        ratingBar.setRating(finalRating);
//                        hideLoadingScreen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addRating(String userID) {
       /* com.ride.travel.models.RatingItem ratingItem = new com.ride.travel.models.RatingItem(
                mAuth.getUid()
                ,ratingText.getText().toString().trim()
                ,ratingBar.getRating());*/
        Map<String, Object> value = new HashMap<>();
        if (!ratingText.getText().toString().trim().isEmpty())
        {
            value.put("id", mAuth.getUid());
            value.put("ratingText", ratingText.getText().toString().trim());
            value.put("rating", ratingBar.getRating());
            value.put("timestamp", ServerValue.TIMESTAMP);
            mRatingDatabase.child(userID).child(mAuth.getUid()).setValue(value);
            finish();
        }else{
            Toast.makeText(this, getString(R.string.all_fields_must_be_filled), Toast.LENGTH_LONG).show();
        }

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
