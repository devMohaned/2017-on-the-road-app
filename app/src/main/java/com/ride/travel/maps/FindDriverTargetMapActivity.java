package com.ride.travel.maps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import com.ride.travel.Utils.Constants;
import com.ride.travel.Utils.DataParser;

import static com.ride.travel.HomePage.mAuth;

public class FindDriverTargetMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private PlaceAutocompleteFragment placeAutoComplete;
    private boolean everythingIsDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_target);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_find_target_toolbar);
        setSupportActionBar(mToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_done, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        } else if (item.getItemId() == R.id.ID_menu_done) {
            if (everythingIsDone) {
                Intent intent = new Intent(FindDriverTargetMapActivity.this, FindDriverMaps.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please add your destination", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
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

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(FindDriverTargetMapActivity.this)
                        .setTitle(getString(R.string.destination))
                        .setMessage(place.getName() + " " + getString(R.string.is_your_destination))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (mLastLocation != null) {

                                    if (place.getLatLng() != null) {

                                        mMap.clear();


                                        mMap.addMarker(
                                                new MarkerOptions().position(place.getLatLng())
                                                        .title(getString(R.string.ending_point)));


                                        // Getting URL to the Google Directions API
                                        String url = getUrl((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), place.getLatLng());
                                        Log.d("onMapClick", url);
                                        FetchUrl FetchUrl = new FetchUrl();

                                        // Start downloading json data from Google Directions API
                                        FetchUrl.execute(url);
                                        //move map camera
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


                                        DatabaseReference mRidesDatabaseReference = FirebaseDatabase
                                                .getInstance().getReference()
                                                .child(Constants.DATABASE_TARGETS);
                                        String pushedKey = mAuth.getUid();
                                        if (pushedKey != null) {
                                            mRidesDatabaseReference.child(pushedKey).setValue(place.getLatLng());
                                        }
                                        everythingIsDone = true;
                                    } else {
                                        Toast.makeText(FindDriverTargetMapActivity.this, getString(R.string.no_place_found), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
//                                    Toast.makeText(FindDriverTargetMapActivity.this, getString(R.string.error_no_location_found), Toast.LENGTH_SHORT).show();

                                    mMap.clear();


                                    mMap.addMarker(
                                            new MarkerOptions().position(place.getLatLng())
                                                    .title(getString(R.string.ending_point)));


                                  /*  // Getting URL to the Google Directions API
                                    String url = getUrl((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), place.getLatLng());
                                    Log.d("onMapClick", url);
                                    FetchUrl FetchUrl = new FetchUrl();

                                    // Start downloading json data from Google Directions API
                                    FetchUrl.execute(url);*/

                                    //move map camera
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


                                    DatabaseReference mRidesDatabaseReference = FirebaseDatabase
                                            .getInstance().getReference()
                                            .child(Constants.DATABASE_TARGETS);
                                    String pushedKey = mAuth.getUid();
                                    if (pushedKey != null) {
                                        mRidesDatabaseReference.child(pushedKey).setValue(place.getLatLng());
                                    }
                                    everythingIsDone = true;
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();
                Log.d("Maps", "Place selected: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        placeAutoComplete.setHint(getString(R.string.destination));


        connectDriver();
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();


//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(final LatLng latLng) {
//
//                AlertDialog.Builder dialog = new AlertDialog.Builder(FindDriverTargetMapActivity.this)
//                        .setTitle(getString(R.string.destination))
//                        .setMessage(getString(R.string.add_ending_point_to_the_map))
//                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                if (mLastLocation != null) {
//
//                                    if (latLng != null)
//                                    {
//
//                                    mMap.clear();
//
//
//                                    mMap.addMarker(
//                                            new MarkerOptions().position(latLng)
//                                                    .title(getString(R.string.ending_point)));
//
//
//                                    // Getting URL to the Google Directions API
//                                    String url = getUrl((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), latLng);
//                                    Log.d("onMapClick", url);
//                                    FetchUrl FetchUrl = new FetchUrl();
//
//                                    // Start downloading json data from Google Directions API
//                                    FetchUrl.execute(url);
//                                    //move map camera
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
//                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
//
//
//                                    DatabaseReference mRidesDatabaseReference = FirebaseDatabase
//                                            .getInstance().getReference()
//                                            .child(Constants.DATABASE_TARGETS);
//                                    String pushedKey = mAuth.getUid();
//                                    if (pushedKey != null) {
//                                        mRidesDatabaseReference.child(pushedKey).setValue(latLng);
//                                    }
//                                    Intent intent = new Intent(FindDriverTargetMapActivity.this, FindDriverMaps.class);
//                                    startActivity(intent);
//                                    finish();
//                                }else{
//                                    Toast.makeText(FindDriverTargetMapActivity.this, getString(R.string.error_no_location_found), Toast.LENGTH_SHORT).show();
//                                }}else {
//                                    Toast.makeText(FindDriverTargetMapActivity.this, getString(R.string.error_no_location_found), Toast.LENGTH_SHORT).show();
//                                }
//                                }
//                        })
//                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                dialog.show();
//
//
//            }
//        });


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


    private boolean mapShownForFirstTime = true;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

//                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null) {
                        if (imm.isAcceptingText()) {
                            //                        writeToLog("Software Keyboard was shown");
                        } else {
                            mLastLocation = location;
                        }
                    } else {
                        mLastLocation = location;
                    }


                    if (mapShownForFirstTime) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                        mapShownForFirstTime = false;
                    }




                   /* DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance().getReference("speakersAvailable");
                    GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
                    GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                    geoFire.setLocation(*//*myCurrentUserID*//*"myid", geoLocation, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
*/
                }


            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
          /*  DatabaseReference mDriversAvailableDatabaseReference = FirebaseDatabase.getInstance().getReference("speakersAvailable");
            GeoFire geoFire = new GeoFire(mDriversAvailableDatabaseReference);
            geoFire.removeLocation(*//*myCurrentUserID*//*"myid", new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });*/
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
//            getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
        } catch (NullPointerException e) {
            Log.e("MapActivity: ", e.toString());
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

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}