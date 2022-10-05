package com.ride.travel.maps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.robertsimoes.shareable.Shareable;
import com.ride.travel.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ride.travel.Utils.Constants;
import com.ride.travel.Utils.DataParser;
import com.ride.travel.adapters.MessageAdapter;
import com.ride.travel.messaging.MessageListActivity;
import com.ride.travel.models.MessageItem;
import com.ride.travel.models.User;

import static com.ride.travel.HomePage.mAuth;

public class FindDriverMaps extends AppCompatActivity implements OnMapReadyCallback {

    private Marker mPositionMarker;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDatabase;
    private User tempuser;
    private String myCurrentUserID;
    //    private TextView mMessagesBadge;
    private MessageAdapter mMessageAdapter;
    private ArrayList<MessageItem> messageItemList = new ArrayList<>();
    private SupportMapFragment mapFragment;
    private Polyline polyLine;
    RelativeLayout mSplashScreen;
    private ProgressBar mMapProgressBar;
    private double latitudeTarget, longitudeTarget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        myCurrentUserID = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        removeUnwantedUsers();
        setupViews();
        setupPreviousChat();

    }


    private void setupViews() {
        ImageView mMessagesImageView = findViewById(R.id.ID_messages_image_view);
//        mMessagesBadge = findViewById(R.id.ID_messages_badge);


        DatabaseReference mTargetsDatabase = FirebaseDatabase
                .getInstance().getReference()
                .child(Constants.DATABASE_TARGETS).child(myCurrentUserID);

        mTargetsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    if (ds.getKey().equals("latitude")) {
                        try {
                            latitudeTarget = ds.getValue(Double.class);
                        } catch (NullPointerException e) {
                            Log.e("Find DriverMaps", "Couldn't find latitude" + e.toString());
                        }
                    } else if (ds.getKey().equals("longitude")) {
                        try{
                        longitudeTarget = ds.getValue(Double.class);
                    }catch (NullPointerException e )
                        {
                            Log.e("FindDriverMaps", "Couldn't find longitude" + e.toString());
                        }
                    }
                }
              /*  LatLng latLng = dataSnapshot.getValue(LatLng);
                latitudeTarget = latLng.getLatitude();
                longitudeTarget = latLng.getLongitude();*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ImageView mShare = findViewById(R.id.ID_share_map);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                Uri uri = Uri.parse("https://maps.google.com/maps?saddr="
                        + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude()
                        + "&daddr=" + latitudeTarget + "," + longitudeTarget);

                Shareable shareAction = new Shareable.Builder(FindDriverMaps.this)
                        .message("This is my message" + "\n" + "Route Direction")
                        .socialChannel(Shareable.Builder.ANY)
                        .url(String.valueOf(uri))
                        .build();
                shareAction.share();
            }catch (NullPointerException e)
                {
                    Toast.makeText(FindDriverMaps.this, "Location isn't found, please wait", Toast.LENGTH_LONG).show();
                }
            }
        });


        mSplashScreen = findViewById(R.id.ID_splash);


        mMapProgressBar = findViewById(R.id.ID_map_progress_bar);

        TextView mName = findViewById(R.id.ID_name_of_user_on_map);
        mName.setText(mAuth.getCurrentUser().getDisplayName());

//        String senderWithReceiver = mAuth.getUid() + Constants.CHAT_WITH + tempuser.getId();

        DatabaseReference mMessagesDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.CHAT)
                .child(myCurrentUserID);
               /* .child(senderWithReceiver);*/


      /*  mMessagesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessagesBadge.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/


        if (!messageItemList.isEmpty()) {
            mMapProgressBar.setVisibility(View.GONE);
        } else if (messageItemList.isEmpty()) {
            mMapProgressBar.setVisibility(View.GONE);
        }


        mMessagesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindDriverMaps.this, MessageListActivity.class);
                startActivity(intent);
            }
        });


    }


    boolean doubleBackToExitPressedOnce = false;

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


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        connectDriver();
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                final String userID = markersMap.get(marker);


                Toast.makeText(FindDriverMaps.this, getString(R.string.double_click_to_chat), Toast.LENGTH_LONG).show();
                if (doubleBackToExitPressedOnce) {

                    if (tempuser != null) {
                        if (userID.equals(myCurrentUserID)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FindDriverMaps.this)
                                    .setTitle(getString(R.string.error))
                                    .setMessage("You can't chat with yourself")
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FindDriverMaps.this)
                                    .setTitle(getString(R.string.show))
                                    .setMessage(getString(R.string.chat_with) + " " + tempuser.getName())
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            connectToChat(userID);

                                        }
                                    })
                                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder.show();
                        }
                    } else {
                        if (userID.equals(myCurrentUserID)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FindDriverMaps.this)
                                    .setTitle(getString(R.string.error))
                                    .setMessage("You can't chat with yourself")
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder.show();

                        } else {
                            mDatabase.child(Constants.DATABASE_USERS).child(userID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            tempuser = dataSnapshot.getValue(User.class);
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FindDriverMaps.this)
                                                    .setTitle(getString(R.string.show))
                                                    .setMessage(getString(R.string.chat_with) + " " + tempuser.getName())
                                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            connectToChat(userID);

                                                        }
                                                    })
                                                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });

                                            builder.show();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }

                                    });
                        }
                    }

                    /* Intent intent = new Intent(FindDriverMaps.this, *//*ProfileActivity*//*FindDriverMaps.class);
                    intent.putExtra(Constants.INTENT_USER_ID, userID);
                    startActivity(intent);*/
                } else {


                    DatabaseReference mRidesDatabaseReference = FirebaseDatabase
                            .getInstance().getReference()
                            .child(Constants.DATABASE_TARGETS);


                    mRidesDatabaseReference.child(userID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    com.ride.travel.models.LatLng latLng1 = dataSnapshot.getValue(com.ride.travel.models.LatLng.class);

                                    try {
                                        // Getting URL to the Google Directions API
                                        String url = getUrl((marker.getPosition()), new LatLng(latLng1.getLatitude(), latLng1.getLongitude()));
                                        Log.d("onMapClick", url);
                                        FetchUrl FetchUrl = new FetchUrl();

                                        // Start downloading json data from Google Directions API
                                        FetchUrl.execute(url);
                                        //move map camera
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                                    } catch (NullPointerException e) {
                                        AlertDialog dialog = new AlertDialog.Builder(FindDriverMaps.this)
                                                .setTitle(getString(R.string.error))
                                                .setMessage(getString(R.string.use_has_no_specific_target))
                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                    doubleBackToExitPressedOnce = true;
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }


                return false;
            }
        });
/*
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String userID = markersMap.get(marker);
                Toast.makeText(MapActivity.this, "We got userID: " + userID, Toast.LENGTH_SHORT).show();
            }
        });*/

//        addRandomMarkers();

    }


    private void setupPreviousChat() {
        RecyclerView mMessagingRecyclerView = findViewById(R.id.ID_messaging_messages_list_recyclerview);

        mMessagingRecyclerView.setHasFixedSize(true);
        mMessagingRecyclerView.setLayoutManager(new LinearLayoutManager(FindDriverMaps.this));


        mMessageAdapter = new MessageAdapter(this, messageItemList);
        mMessagingRecyclerView.setAdapter(mMessageAdapter);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mAuth != null) {
            DatabaseReference mMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(myCurrentUserID);

            mMessageContainterDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                    messageItemList.add(messageItem);
                    mMessageAdapter.notifyDataSetChanged();
                    mMapProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (!messageItemList.isEmpty()) {
                        mMapProgressBar.setVisibility(View.GONE);
                    } else if (messageItemList.isEmpty()) {
                        mMapProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mMapProgressBar.setVisibility(View.GONE);

                }
            });

//            mMessageContainterDatabaseReference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    messageItemList.clear();
                   /* for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        MessageItem messageItem = ds.getValue(MessageItem.class);
                        messageItemList.add(messageItem);
                        mMessageAdapter.notifyDataSetChanged();

                        if (!messageItemList.isEmpty()) {
                            mEmptyRecyclerViewText.setVisibility(View.GONE);
                        } else if (messageItemList.isEmpty()) {
                            mEmptyRecyclerViewText.setVisibility(View.VISIBLE);
                        }

                    }*/
//                    if (!messageItemList.isEmpty()) {
//                        mEmptyRecyclerViewText.setVisibility(View.GONE);
//                    } else if (messageItemList.isEmpty()) {
//                        mEmptyRecyclerViewText.setVisibility(View.VISIBLE);
//                    }
//
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//          }
        } else {
            Toast.makeText(FindDriverMaps.this, "There's something wrong with logging in, please reload the page", Toast.LENGTH_LONG).show();
        }
    }

    private void connectToChat(String idOfUser) {


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mCurrentUserMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(myCurrentUserID);
        DatabaseReference mWantedUserMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(idOfUser);

        DatabaseReference firstReference = mCurrentUserMessageContainterDatabaseReference.child(myCurrentUserID + Constants.CHAT_WITH + idOfUser);
        DatabaseReference secondReference = mWantedUserMessageContainterDatabaseReference.child(idOfUser + Constants.CHAT_WITH + myCurrentUserID);


        MessageItem messageItem1 = new MessageItem(myCurrentUserID, idOfUser);
        firstReference.setValue(messageItem1);

        MessageItem messageItem2 = new MessageItem(idOfUser, myCurrentUserID);
        secondReference.setValue(messageItem2);


//        mMessagingLayout.setVisibility(View.GONE);


    }


    private void connectDriver() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private Map<Marker, String> markersMap = new HashMap<Marker, String>();

    private boolean mapShownForFirstTime = true;
    private Circle myCircle;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    if (location != null) {

                        mLastLocation = location;
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                        if (mapShownForFirstTime) {
                            try {
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                                mapShownForFirstTime = false;


                                mSplashScreen.setVisibility(View.GONE);

                                LatLng point = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                CircleOptions circleOptions = new CircleOptions()
                                        .center(point)   //set center
                                        .radius(100000)   //set radius in meters
                                        .fillColor(Color.TRANSPARENT)  //default
                                        .strokeColor(Color.BLUE)
                                        .strokeWidth(5);

                                myCircle = mMap.addCircle(circleOptions);
                            } catch (NullPointerException e) {
                                Toast.makeText(FindDriverMaps.this, "No GPS found, you are not visible on map", Toast.LENGTH_LONG).show();
                            }
                        }


                        if (mPositionMarker == null) {
                            mPositionMarker =
                                    mMap.addMarker(new MarkerOptions()
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.
                                                    fromResource(R.drawable./*ic_airplanemode_active*/baseline_person_pin_circle_black_24))
                                            .anchor(0.5f, 0.5f)
                                            .position(latLng));


                            markersMap.put(mPositionMarker, myCurrentUserID);
                        }
                        mPositionMarker.setPosition(latLng);
                        DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance()
                                .getReference(Constants.DATABASE_GEO_FIRE_PASSENGERS_AVAILABLE);
                        GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
                        GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                        geoFire.setLocation(myCurrentUserID, geoLocation, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                HashMap<String, Object> timestampCreated = new HashMap<>();
                                timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                                FirebaseDatabase.getInstance()
                                        .getReference(Constants.DATABASE_GEO_FIRE_PASSENGERS_AVAILABLE)
                                        .child(key).updateChildren(timestampCreated);
                            }
                        });

                        if (!getDriversAroundStarted) {
                            getDriversAround();
                        }

                    }
                }

            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance()
                    .getReference(Constants.DATABASE_GEO_FIRE_PASSENGERS_AVAILABLE);
            GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
            geoFire.removeLocation(myCurrentUserID, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });

          /*  FirebaseDatabase
                    .getInstance().getReference()
                    .child(Constants.DATABASE_TARGETS)
                    .child(mAuth.getUid())
                    .removeValue();*/

            FirebaseDatabase.getInstance().getReference()
                    .child("current_time")
                    .child(myCurrentUserID)
                    .child(Constants.CURRENT_TIMESTAMP)
                    .removeValue();


        } catch (NullPointerException e) {
            Log.e("Map Activity : ", e.toString());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (NullPointerException e) {
            Log.e("MapActivity: ", e.toString());
        }
       /* mMessagesLinearLayoutContainingRecyclerView.setVisibility(View.GONE);
        mMessagingLayout.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();*/

    }

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();

    private void getDriversAround() {
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance()
                .getReference().child(Constants.DATABASE_GEO_FIRE_DRIVERS_AVAILABLE);

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(mLastLocation.getLongitude(),
                        mLastLocation.getLatitude()), 100000);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {

                for (Marker markerIt : markers) {
                    try {
                        if (markerIt.getTag().equals(key))
                            return;
                    } catch (NullPointerException e) {
                        Log.e("ENTERED NullpointerException error", e.toString());
                    }
                }


                mDatabase.child(Constants.DATABASE_USERS).child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                tempuser = dataSnapshot.getValue(User.class);

                                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                                Marker mDriverMarker = mMap
                                        .addMarker
                                                (new MarkerOptions()
                                                        .position(driverLocation)
                                                        .title(tempuser.getName())
                                                        .icon(BitmapDescriptorFactory.
                                                                fromResource(R.drawable./*ic_person*/baseline_directions_car_black_24)));
                                mDriverMarker.setTag(key);
                                markersMap.put(mDriverMarker, key);
                                markers.add(mDriverMarker);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        });


            }

            @Override
            public void onKeyExited(String key) {
                for (int i = 0; i < markers.size(); i++) {
                    Marker markerIt = markers.get(i);
                    try {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.remove();
                            markers.remove(i);
                        }
                    } catch (NullPointerException e) {
                        Log.e("EXIT NullPointerError", e.toString());
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (int i = 0; i < markers.size(); i++) {
                    Marker markerIt = markers.get(i);
                    try {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    } catch (NullPointerException e) {
                        Log.e("MOVED NullPointedException error", e.toString());
                    }
                }

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void addRandomMarkers() {
        List<LatLng> locationlocations = new ArrayList<>();
     /*   locationlocations.add(new LatLng(30.859151, 31.068616));
        locationlocations.add(new LatLng(30.859197, 31.067650));
        locationlocations.add(new LatLng(30.859197, 31.067650));
        locationlocations.add(new LatLng(30.858967, 31.066684));
        locationlocations.add(new LatLng(30.858258, 31.064903));
        locationlocations.add(new LatLng(30.857816, 31.066673));
        locationlocations.add(new LatLng(30.857659, 31.067703));
        locationlocations.add(new LatLng(30.859897, 31.066083));
        locationlocations.add(new LatLng(30.859685, 31.066941));
        locationlocations.add(new LatLng(30.860072, 31.068186));
        locationlocations.add(new LatLng(30.859750, 31.068615));
        locationlocations.add(new LatLng(30.857632, 31.069162));
        locationlocations.add(new LatLng(30.857337, 31.068153));
        locationlocations.add(new LatLng(30.863305, 31.058873));
        locationlocations.add(new LatLng(30.861205, 31.061426));
        locationlocations.add(new LatLng(30.864696, 31.056062));
        locationlocations.add(new LatLng(30.860902, 31.057349));
        locationlocations.add(new LatLng(30.860497, 31.060042));
        locationlocations.add(new LatLng(30.859502, 31.061394));
        locationlocations.add(new LatLng(30.858461, 31.061909));
        locationlocations.add(new LatLng(30.856297, 31.064034));
        locationlocations.add(new LatLng(30.858397, 31.063937));
        locationlocations.add(new LatLng(30.856555, 31.066823));
        locationlocations.add(new LatLng(30.855883, 31.066716));*/


//        locationlocations.add(new LatLng(value));


        locationlocations.add(new LatLng(31.224599, 29.935210));
        locationlocations.add(new LatLng(31.224870, 29.935537));

        for (LatLng location : locationlocations) {
            LatLng driverLocation = new LatLng(location.latitude, location.longitude);
            Marker mDriverMarker = mMap
                    .addMarker
                            (new MarkerOptions()
                                    .position(driverLocation)
                                    .title("Steven Bradley")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable./*ic_person*//*ic_add_box_black_48dp*/baseline_directions_car_black_24)));

        }
    }


    private String getUrl(com.google.android.gms.maps.model.LatLng origin, com.google.android.gms.maps.model.LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
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


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<com.google.android.gms.maps.model.LatLng> points;

            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    com.google.android.gms.maps.model.LatLng position = new com.google.android.gms.maps.model.LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                if (polyLine != null) {
                    polyLine.remove();
                }


                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                polyLine = mMap.addPolyline(lineOptions);

            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    long time1;
    long time;
    int i = 0;

    private void removeUnwantedUsers() {

        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.CURRENT_TIMESTAMP, ServerValue.TIMESTAMP);

        final Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DATABASE_GEO_FIRE_DRIVERS_AVAILABLE);


        FirebaseDatabase.getInstance().getReference()
                .child("current_time").child(myCurrentUserID).setValue(timestampCreated);

        FirebaseDatabase.getInstance().getReference()
                .child("current_time")
                .child(myCurrentUserID)
                .child(Constants.CURRENT_TIMESTAMP)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                        time1 = dataSnapshot3.getValue(Long.class);
                        time = time1;


                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                                        if (ds.getKey() != null) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(Constants.DATABASE_GEO_FIRE_DRIVERS_AVAILABLE).child(ds.getKey())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                                if (dataSnapshot1.getKey() != null) {
                                                                    if (dataSnapshot1.getKey().equals("timestamp")) {
                                                                        double timestamp = dataSnapshot1.getValue(Double.class);
                                                                        double difference = time - timestamp;
                                                                        if (difference > 30 * 60 * 1000) {
                                                                            FirebaseDatabase.getInstance().getReference()
                                                                                    .child(Constants.DATABASE_GEO_FIRE_DRIVERS_AVAILABLE).child(ds.getKey())
                                                                                    .removeValue();

                                                                            FirebaseDatabase.getInstance().getReference()
                                                                                    .child(Constants.DATABASE_TARGETS)
                                                                                    .child(ds.getKey())
                                                                                    .removeValue();
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                            if (i < dataSnapshot.getChildrenCount()) {
                                                i++;
                                            }

                                            if (i == dataSnapshot.getChildrenCount()) {
                                                mapFragment.getMapAsync(FindDriverMaps.this);
                                                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                                                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(FindDriverMaps.this);
                                                setupViews();
                                            }
                                        }
                                    }
                                } else if (dataSnapshot.getValue() == null) {
                                    mapFragment.getMapAsync(FindDriverMaps.this);
                                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(FindDriverMaps.this);
                                    setupViews();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}