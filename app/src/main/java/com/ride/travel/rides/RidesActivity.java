package com.ride.travel.rides;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;
import java.util.ArrayList;

import com.ride.travel.Utils.Constants;
import com.ride.travel.adapters.RideAdapter;
import com.ride.travel.models.RideItem;

public class RidesActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private DatabaseReference mDatabase;
    private RideAdapter mRideAdapter;
    public static SupportMapFragment mapFragment;
    private String city1;
    private boolean startingPointisPlaced = false;
    private String city2;
    private boolean endingPointisPlaced = false;
    private RelativeLayout mSearchLayout;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides);
        mProgressBar = findViewById(R.id.ID_rideS_progress_bar);

        setupSearch();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ID_rides_map);

        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();


        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        setupRecyclerView();
        getRidesFromDatabase();



        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_toolbar);
        setSupportActionBar(mToolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }


    ArrayList<RideItem> rideItemArrayList;

    private void setupRecyclerView() {
        rideItemArrayList = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.ID_rides_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRideAdapter = new RideAdapter(this, rideItemArrayList);
        mRecyclerView.setAdapter(mRideAdapter);
    }



   private void doneSearching(RadioGroup radioGroup) {
       DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

       switch (radioGroup.getCheckedRadioButtonId()) {
           case R.id.ID_radioStartingPoint: {
               Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("startingPoint").equalTo(city1);
               query.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     for (DataSnapshot ds : dataSnapshot.getChildren()) {
                           RideItem ride = ds.getValue(RideItem.class);
                           rideItemArrayList.add(ride);
                           mRideAdapter.notifyDataSetChanged();


                     /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                       }


                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                   }
               });
               break;
           }
           case R.id.ID_radioEndingPoint: {
               Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("endingPoint").equalTo(city2);
               query.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot ds : dataSnapshot.getChildren()) {
                           RideItem ride = ds.getValue(RideItem.class);
                           rideItemArrayList.add(ride);
                           mRideAdapter.notifyDataSetChanged();
                                    /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                       }


                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                   }
               });
               break;

           }
           case R.id.ID_radioStartingAndEndingPoints: {
               Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("startToEnd").equalTo(city1 + " " + city2);
               query.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot ds : dataSnapshot.getChildren()) {
                           RideItem ride = ds.getValue(RideItem.class);
                           rideItemArrayList.add(ride);
                           mRideAdapter.notifyDataSetChanged();
                                    /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                       }


                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                   }
               });
               break;
           }

       }
   }


    private void setupSearch()
    {
        mSearchLayout = findViewById(R.id.ID_search_layout);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.ID_radioGroup);

        RadioButton radioStartingPoint = (RadioButton) findViewById(R.id.ID_radioStartingPoint);
        RadioButton radioEndingPoint = (RadioButton) findViewById(R.id.ID_radioEndingPoint);
        final RadioButton radioStartingAndEndingPoint = (RadioButton)
                findViewById(R.id.ID_radioStartingAndEndingPoints);


        PlaceAutocompleteFragment placeAutoCompleteCity1 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_search_ride_city_1);
        placeAutoCompleteCity1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startingPointisPlaced = true;
                city1 = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });


        PlaceAutocompleteFragment placeAutoCompleteCity2 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_search_ride_city_2);
        placeAutoCompleteCity2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endingPointisPlaced = true;
                city2 = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });


//                        final EditText mSearchingForEmail = findViewById(R.id.ID_search_ride);

        Button mSearchButton = findViewById(R.id.ID_search_button_for_ride);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rideItemArrayList.clear();
                doneSearching(radioGroup);
                mSearchLayout.setVisibility(View.GONE);
            }
        });

        ImageView mHideSearch = findViewById(R.id.ID_hide_search);
        mHideSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchLayout.setVisibility(View.GONE);
            }
        });


//                        EditText editText = (EditText) dialogView.findViewById(R.id.ID_search_ride);


    }

    private void getRidesFromDatabase() {
        DatabaseReference mRidesDatabase = mDatabase.child(Constants.DATABASE_RIDES);

//        mRidesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds : dataSnapshot.getChildren())
//                {
//                    RideItem rideItem = ds.getValue(RideItem.class);
//                    rideItemArrayList.add(rideItem);
//                    mRideAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        mRidesDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RideItem rideItem = dataSnapshot.getValue(RideItem.class);
                rideItemArrayList.add(rideItem);
                mRideAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRideAdapter != null) {
            mRideAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rides_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ID_menu_add_ride) {
            Intent intent = new Intent(RidesActivity.this, AddRideActivity.class);
                startActivity(intent);

            return true;
        } else if (id == R.id.ID_menu_search) {
//            getRideEqualToDestination();
            mSearchLayout.setVisibility(View.VISIBLE);
        } else if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }


        return super.onOptionsItemSelected(item);
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
        mMap = googleMap;





/*
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String userID = markersMap.get(marker);
                Toast.makeText(MapActivity.this, "We got userID: " + userID, Toast.LENGTH_SHORT).show();
            }
        });*/

    }


    private void getRideEqualToDestination() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.search_for_ride, null);

        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.ID_radioGroup);

        RadioButton radioStartingPoint = (RadioButton) dialogView.findViewById(R.id.ID_radioStartingPoint);
        RadioButton radioEndingPoint = (RadioButton) dialogView.findViewById(R.id.ID_radioEndingPoint);
        final RadioButton radioStartingAndEndingPoint = (RadioButton)
                dialogView.findViewById(R.id.ID_radioStartingAndEndingPoints);


        PlaceAutocompleteFragment placeAutoCompleteCity1 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_search_ride_city_1);
        placeAutoCompleteCity1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startingPointisPlaced = true;
                city1 = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });


        PlaceAutocompleteFragment placeAutoCompleteCity2 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_search_ride_city_2);
        placeAutoCompleteCity1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endingPointisPlaced = true;
                city2 = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });




        AlertDialog.Builder searchDialogForRide = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.search_using_method))
//                .setMessage(getString(R.string.write_exactly_your_destination))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        final EditText mSearchingForEmail = findViewById(R.id.ID_search_ride);

                        rideItemArrayList.clear();



//                        EditText editText = (EditText) dialogView.findViewById(R.id.ID_search_ride);
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        switch (radioGroup.getCheckedRadioButtonId())
                        {
                            case R.id.ID_radioStartingPoint:
                            {
                                Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("startingPoint").equalTo(city1);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            RideItem ride = ds.getValue(RideItem.class);
                                            rideItemArrayList.add(ride);
                                            mRideAdapter.notifyDataSetChanged();
                                    /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                                    }
                                });
                                break;
                            }
                            case R.id.ID_radioEndingPoint:
                            {
                                Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("endingPoint").equalTo(city2);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            RideItem ride = ds.getValue(RideItem.class);
                                            rideItemArrayList.add(ride);
                                            mRideAdapter.notifyDataSetChanged();
                                    /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                                    }
                                });
                                break;

                            }
                            case R.id.ID_radioStartingAndEndingPoints:
                            {
                                Query query = mDatabase.child(Constants.DATABASE_RIDES).orderByChild("startToEnd").equalTo(city1 + " " + city2);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            RideItem ride = ds.getValue(RideItem.class);
                                            rideItemArrayList.add(ride);
                                            mRideAdapter.notifyDataSetChanged();
                                    /* userID = user.getId();
                                    mNameOfUser.setText(user.getName());
                                    mSearchingForEmail.setVisibility(View.GONE);*/
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
/*
                                mSearchingForEmail.setText(getString(R.string.cannot_find_user));
*/
                                    }
                                });
                                break;
                            }
                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        searchDialogForRide.show();


    }


}