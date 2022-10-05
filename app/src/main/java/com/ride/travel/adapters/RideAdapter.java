package com.ride.travel.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import com.ride.travel.Utils.Constants;
import com.ride.travel.Utils.DataParser;
import com.ride.travel.messaging.MessageListActivity;
import com.ride.travel.models.LatLng;
import com.ride.travel.models.MessageItem;
import com.ride.travel.models.RatingItem;
import com.ride.travel.models.RideItem;
import com.ride.travel.rating.ViewRatingOfUser;
import com.ride.travel.rides.RidesActivity;

import static com.ride.travel.HomePage.mAuth;

/**
 * Created by bestway on 02/07/2018.
 */

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideItemViewHolder> {

    private List<RideItem> mList;
    private Context mContext;

    public RideAdapter(Context context, ArrayList<RideItem> list)
    {
        mList = list;
        mContext = context;
    }


    @NonNull
    @Override
    public RideAdapter.RideItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);

        return new RideItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RideAdapter.RideItemViewHolder holder, int position) {
        final RideItem currentRideItem = mList.get(position);
        final String startingPoint = currentRideItem.getStartingPoint();
        String endingPoint = currentRideItem.getEndingPoint();
        final String idOfUser = currentRideItem.getIdOfUser();
        String description = currentRideItem.getDescription();
        String time = currentRideItem.getTimeToLeave();
        final LatLng startingLatLng = currentRideItem.getStartingLatLng();
        final LatLng endingLatLng = currentRideItem.getEndingLatLng();
        String fareValue = currentRideItem.getFare() + " EGP";

        holder.fromAndToOfRide.setText(startingPoint + " " + mContext.getString(R.string.to) + " " + endingPoint);
        holder.description.setText(description);
        holder.dateOfRide.setText(time);
        holder.fare.setText(fareValue);

        final ArrayList<Float> list = new ArrayList<>();
        final float[] ratingx = {0};
        FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_RATING)
                .child(idOfUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    RatingItem ratingItem = ds.getValue(RatingItem.class);
                    float rating = ratingItem.getRating();
                    ratingx[0] = ratingx[0] + rating;
                    list.add(rating);
                    if (list.size() == dataSnapshot.getChildrenCount()) {
                        float finalRating = ratingx[0] / dataSnapshot.getChildrenCount();
//                        float fin = (float) (Math.round(finalRating * 100.0) / 100.0);
                        String ratingFinalText = String.format("%.1f", finalRating);
                        holder.ratingScore.setText(ratingFinalText + " (View All)");
                        holder.ratingBar.setRating(finalRating);
//                        hideLoadingScreen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ViewRatingOfUser.class);
                intent.putExtra(Constants.INTENT_USER_ID,idOfUser);
                mContext.startActivity(intent);
            }
        });


        holder.ratingScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ViewRatingOfUser.class);
                intent.putExtra(Constants.INTENT_USER_ID,idOfUser);
                mContext.startActivity(intent);
            }
        });




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                RidesActivity.mMap.clear();

                RidesActivity.mMap.addMarker(
                        new MarkerOptions().position(new com.google.android.gms.maps.model.LatLng(
                                startingLatLng.getLatitude()
                                ,startingLatLng.getLongitude()))
                                .title(mContext.getString(R.string.starting_point)));

                RidesActivity.mMap.addMarker(
                        new MarkerOptions().position(new com.google.android.gms.maps.model.LatLng(
                                endingLatLng.getLatitude()
                                ,endingLatLng.getLongitude()))
                                .title(mContext.getString(R.string.ending_point)));


                com.google.android.gms.maps.model.LatLng origin = new com.google.android.gms.maps.model.LatLng(startingLatLng.getLatitude(),startingLatLng.getLongitude());
                com.google.android.gms.maps.model.LatLng dest = new com.google.android.gms.maps.model.LatLng(endingLatLng.getLatitude(),endingLatLng.getLongitude());

                // Getting URL to the Google Directions API
                String url = getUrl(origin, dest);
                Log.d("onMapClick", url);
                FetchUrl FetchUrl = new FetchUrl();

                // Start downloading json data from Google Directions API
                FetchUrl.execute(url);
                //move map camera
                RidesActivity.mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                RidesActivity.mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            }catch (NullPointerException error)
                {
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle(mContext.getString(R.string.error))
                            .setMessage(mContext.getString(R.string.use_has_no_specific_target))
                            .setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }

            }
        });

        holder.messageOfRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!idOfUser.equals(mAuth.getUid()))
                {



                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                DatabaseReference mCurrentUserMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(mAuth.getUid());
                DatabaseReference mWantedUserMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(idOfUser);

                DatabaseReference firstReference = mCurrentUserMessageContainterDatabaseReference.child(mAuth.getUid() + Constants.CHAT_WITH + idOfUser);
                DatabaseReference secondReference =  mWantedUserMessageContainterDatabaseReference.child(idOfUser + Constants.CHAT_WITH + mAuth.getUid());



                MessageItem messageItem1 = new MessageItem(mAuth.getUid(),idOfUser);
                firstReference.setValue(messageItem1);

                MessageItem messageItem2 = new MessageItem(idOfUser,mAuth.getUid());
                secondReference.setValue(messageItem2);

                Intent intent = new Intent(mContext, MessageListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constants.INTENT_USER_ID,idOfUser);
                mContext.startActivity(intent);
                }else {
                    Toast.makeText(mContext, "Can't message yourself", Toast.LENGTH_SHORT).show();
                }
            }
        });


        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Uri uri = Uri.parse("https://maps.google.com/maps?saddr="
                            + startingLatLng.getLatitude() + "," + startingLatLng.getLongitude()
                            + "&daddr=" + endingLatLng.getLatitude() + "," + endingLatLng.getLongitude());

                    Shareable shareAction = new Shareable.Builder(mContext)
                            .message("This is my message" + "\n" + "Route Direction")
                            .socialChannel(Shareable.Builder.ANY)
                            .url(String.valueOf(uri))
                            .build();
                    shareAction.share();
                }catch (NullPointerException e)
                {
                    Toast.makeText(mContext, "Location isn't found, please wait", Toast.LENGTH_LONG).show();

                }}
        });



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
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
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

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                RidesActivity.mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class RideItemViewHolder extends RecyclerView.ViewHolder {
        public TextView fromAndToOfRide, dateOfRide,description,fare,ratingScore;
        public ImageButton messageOfRide,share;
        public RatingBar ratingBar;

        public RideItemViewHolder(View view) {
            super(view);
            fromAndToOfRide = view.findViewById(R.id.ID_item_from_and_to);
            dateOfRide = view.findViewById(R.id.ID_item_date_of_ride);
            messageOfRide = view.findViewById(R.id.ID_message_ride);
            description = view.findViewById(R.id.ID_item_description_of_ride);
            share = view.findViewById(R.id.ID_share_ride);
            fare = view.findViewById(R.id.ID_ride_fare);
            ratingScore = view.findViewById(R.id.ID_rating_score_ride);
            ratingBar = view.findViewById(R.id.ID_rating_of_user_ride);

        }
    }



}
