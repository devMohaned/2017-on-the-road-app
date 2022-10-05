package com.ride.travel.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ride.travel.R;
import com.ride.travel.models.User;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ride.travel.Utils.Constants;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private Marker mPositionMarker;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDatabase;
    private User tempuser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {


                Toast.makeText(MapActivity.this, getString(R.string.double_click_to_open_profile), Toast.LENGTH_LONG).show();
                if (doubleBackToExitPressedOnce) {

                    String userID = markersMap.get(marker);
                    Intent intent = new Intent(MapActivity.this, /*ProfileActivity*/MapActivity.class);
                    intent.putExtra(Constants.INTENT_USER_ID, userID);
                    startActivity(intent);
                } else {
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

    }


    private void connectDriver() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mLastLocation = location;
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


                    if (mPositionMarker == null) {
                        mPositionMarker =
                                mMap.addMarker(new MarkerOptions()
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable./*ic_airplanemode_active*/ic_account_circle_black_24dp))
                                        .anchor(0.5f, 0.5f)
                                        .position(latLng));





                        markersMap.put(mPositionMarker, /*myCurrentUserID*/"myid");
                    }
                    mPositionMarker.setPosition(latLng);
                    DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance().getReference("speakersAvailable");
                    GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
                    GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                    geoFire.setLocation(/*myCurrentUserID*/"myid", geoLocation, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                    if (!getDriversAroundStarted)
                        getDriversAround();
                }


            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance().getReference("speakersAvailable");
            GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
            geoFire.removeLocation(/*myCurrentUserID*/"myid", new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        } catch (NullPointerException e) {
            Log.e("Map Activity : ", e.toString());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();

    private void getDriversAround() {
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("speakersAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {

                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key))
                        return;
                }


                mDatabase.child(Constants.DATABASE_USERS).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        tempuser = dataSnapshot.getValue(User.class);

                        LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                        Marker mDriverMarker = mMap
                                .addMarker
                                        (new MarkerOptions()
                                                .position(driverLocation)
                                                .title(tempuser.getName())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable./*ic_person*/ic_add_box_black_48dp)));
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
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
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
        locationlocations.add(new LatLng(30.859151, 31.068616));
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
        locationlocations.add(new LatLng(30.855883, 31.066716));
//        locationlocations.add(new LatLng(value));


        for (LatLng location : locationlocations) {
            LatLng driverLocation = new LatLng(location.latitude, location.longitude);
            Marker mDriverMarker = mMap
                    .addMarker
                            (new MarkerOptions()
                                    .position(driverLocation)
                                    .title("Steven Bradley")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable./*ic_person*/ic_add_box_black_48dp)));

        }
    }
}