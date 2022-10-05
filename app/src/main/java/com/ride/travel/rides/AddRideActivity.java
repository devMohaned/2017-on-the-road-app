package com.ride.travel.rides;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.ride.travel.HomePage;
import com.ride.travel.Utils.AppUtils;
import com.ride.travel.Utils.Constants;
import com.ride.travel.Utils.DataParser;
import com.ride.travel.models.RideItem;

import org.json.JSONObject;

public class AddRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private boolean startingPointisPlaced = false;
    private boolean endingPointIsPlaced = true;
    private SupportMapFragment mapFragment;
    private String dateOfRide;
    private com.ride.travel.models.LatLng latLng1;
    private com.ride.travel.models.LatLng latLng2;
    private MenuItem item;
    PlaceAutocompleteFragment placeAutoCompleteCity1,placeAutoCompleteCity2;
    private MarkerOptions marker1,marker2;
    private String city1,city2;
    private String fare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        placeAutoCompleteCity1 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_city_1);
        placeAutoCompleteCity1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                    startingPointisPlaced = true;
                    latLng1 = new com.ride.travel.models.LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                     marker1 = new MarkerOptions().position(
                            new LatLng(place.getLatLng().latitude,place.getLatLng().longitude)).title(getString(R.string.starting_point));
                    mMap.addMarker(marker1);
                    txt.setText(getString(R.string.select_your_destination));

                    System.out.println(place.getLatLng().latitude+"---"+ place.getLatLng().longitude);
                    endingPointIsPlaced = false;
                    city1 = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });


        placeAutoCompleteCity2 = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.ID_city_2);
        placeAutoCompleteCity2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latLng2 = new com.ride.travel.models.LatLng(place.getLatLng().latitude, place.getLatLng().longitude);


                 marker2 = new MarkerOptions().position(
                        new LatLng(place.getLatLng().latitude,place.getLatLng().longitude)).title(getString(R.string.ending_point));

                mMap.addMarker(marker2);

                txt.setText(getString(R.string.all_done));
                System.out.println(place.getLatLng().latitude+"---"+ place.getLatLng().longitude);
                endingPointIsPlaced = true;
                city2 = place.getName().toString();
                Log.d("Maps", "Place selected: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ID_add_ride_map);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();

        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        setupViews();
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


    }

    private void showStartingPointDialog()
    {
        AlertDialog.Builder startingDialog = new AlertDialog.Builder(AddRideActivity.this)
                .setTitle(getString(R.string.starting_point))
                .setMessage(getString(R.string.add_starting_point_to_the_map))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                    }
                });
    }

    private void showEndingPointDialog()
    {
        AlertDialog.Builder startingDialog = new AlertDialog.Builder(AddRideActivity.this)
                .setTitle(getString(R.string.destination))
                .setMessage(getString(R.string.add_ending_point_to_the_map))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                    }
                });
    }

    private EditText /*mCity1,mCity2,*/mDescription;
    private Button mAddMap;
    private TextView txt;

    private void setupViews()
    {

        final android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_add_ride_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
         txt = findViewById(R.id.ID_formate);

       /* mCity1 = findViewById(R.id.ID_city_1);
        mCity2 = findViewById(R.id.ID_city_2);*/
        mDescription = findViewById(R.id.ID_description);

        mAddMap = findViewById(R.id.ID_add_map_starting_ending_points);

        final LinearLayout mDateLinearLayout = findViewById(R.id.ID_timing_of_ride);
        final TextView mPlaceTxtView = findViewById(R.id.ID_txt_place_holder);

        final TextView mStartingPt = findViewById(R.id.ID_from);
        final TextView mDestinationPt = findViewById(R.id.ID_to);


        final Spinner mDaySpinner = findViewById(R.id.ID_day_of_birthday);
        final Spinner mMonthSpinner = findViewById(R.id.ID_month_of_birthday);
        final Spinner mYearSpinner = findViewById(R.id.ID_year_of_birthday);

        ArrayList<String> days = new ArrayList<String>();
        for (int i = 1; i <= 31; i++) {
            days.add(Integer.toString(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, days);
        mDaySpinner.setAdapter(dayAdapter);


        ArrayList<String> months = new ArrayList<String>();
        for (int i = 1; i <= 12; i++) {
            months.add(Integer.toString(i));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, months);
        mMonthSpinner.setAdapter(monthAdapter);


        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2018; i <= 2020; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);
        mYearSpinner.setAdapter(yearAdapter);

        final EditText mFareEditText = findViewById(R.id.ID_fare);
        final TextView mFarePlaceHolderTextView = findViewById(R.id.ID_txt_fare_place_holder);

        mAddMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  String city1Text = mCity1.getText().toString().trim();
                String city2Text = mCity2.getText().toString().trim();*/
                String city1Text = city1;
                String city2Text = city2;
                String descriptionText = mDescription.getText().toString().trim();
                fare = mFareEditText.getText().toString().trim();

                if (city1Text.isEmpty() || city2Text.isEmpty() || descriptionText.isEmpty() || fare.isEmpty())
                {
                    Toast.makeText(AddRideActivity.this
                            , getString(R.string.all_fields_must_be_filled)
                            , Toast.LENGTH_LONG)
                            .show();
                }else if (!city1Text.isEmpty() && !city2Text.isEmpty() && !descriptionText.isEmpty() && !fare.isEmpty()){
                    getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                    txt.setVisibility(View.VISIBLE);
                   /* mCity1.setVisibility(View.GONE);
                    mCity2.setVisibility(View.GONE);*/
                    mDescription.setVisibility(View.GONE);
                    mAddMap.setVisibility(View.GONE);
                    mDateLinearLayout.setVisibility(View.GONE);
                    mPlaceTxtView.setVisibility(View.GONE);
                    mDestinationPt.setVisibility(View.GONE);
                    mStartingPt.setVisibility(View.GONE);
                    mToolbar.setVisibility(View.VISIBLE);
                    dateOfRide = mDaySpinner.getSelectedItem().toString() + "/" + mMonthSpinner.getSelectedItem().toString()
                            +"/" + mYearSpinner.getSelectedItem().toString();
//                    showStartingPointDialog();
                    item.setVisible(true);
                    mFareEditText.setVisibility(View.GONE);
                    mFarePlaceHolderTextView.setVisibility(View.GONE);
                    AppUtils.hideSoftKeyboard(AddRideActivity.this);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(mAddMap.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    }


                    LatLng latLng = new LatLng(latLng1.getLatitude(),latLng1.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(9));


                    String url = getUrl(new LatLng(latLng1.getLatitude(),latLng1.getLongitude()),new LatLng(latLng2.getLatitude(),latLng2.getLongitude()));
                    Log.d("onMapClick", url);
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                }

            }
        });

    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_ride, menu);
         item = menu.findItem(R.id.ID_menu_post);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ID_menu_post) {
            if (startingPointisPlaced && endingPointIsPlaced)
            {
                DatabaseReference mRidesDatabaseReference = mDatabase.child(Constants.DATABASE_RIDES);
                String pushedKey = mRidesDatabaseReference.push().getKey();
                String city1Text = city1;
                String city2Text = city2;

            RideItem newRideItem = new RideItem(
                    pushedKey,
                    HomePage.mAuth.getUid()
                    ,mDescription.getText().toString()
                   /* ,mCity1.getText().toString()
                    ,mCity2.getText().toString()*/
                   ,city1Text
                    ,city2Text
                    ,dateOfRide,latLng1,latLng2,
                    city1Text +" " + city2Text/*mCity1.getText().toString() + " " + mCity2.getText().toString()*/
            ,fare);


            if (pushedKey != null) {
                mRidesDatabaseReference.child(pushedKey).setValue(newRideItem);
            }
            finish();
            return true;
        }else {
                Toast.makeText(this, getString(R.string.add_starting_and_ending_point), Toast.LENGTH_SHORT).show();
            }
        }else   if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }





    private String getUrl(LatLng origin, LatLng dest) {

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
            ArrayList<LatLng> points;
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
                    LatLng position = new LatLng(lat, lng);

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