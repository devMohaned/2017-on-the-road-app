package com.ride.travel.rides;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;

import java.util.ArrayList;

import com.ride.travel.Utils.Constants;
import com.ride.travel.adapters.RemoveRideAdapter;
import com.ride.travel.models.RideItem;

import static com.ride.travel.HomePage.mAuth;

public class MyRidesActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static GoogleMap mMyRidesMap;
    private RemoveRideAdapter mRideAdapter;
    public static SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ID_rides_map);

        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();


        mapFragment.getMapAsync(this);

        setupRecyclerView();
        getRidesFromDatabase();

        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.my_rides));
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }


    ArrayList<RideItem> rideItemArrayList;

    private void setupRecyclerView()
    {
        rideItemArrayList = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.ID_rides_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRideAdapter = new RemoveRideAdapter(this,rideItemArrayList);
        mRecyclerView.setAdapter(mRideAdapter);
    }

    private void getRidesFromDatabase()
    {
        rideItemArrayList.clear();

        final ProgressBar mProgressBar = findViewById(R.id.ID_rideS_progress_bar);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("idOfUser").equalTo(mAuth.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    RideItem ride = ds.getValue(RideItem.class);
                    rideItemArrayList.add(ride);
                    mRideAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRideAdapter != null)
        {
        mRideAdapter.notifyDataSetChanged();
    }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMyRidesMap = googleMap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

     if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }



}