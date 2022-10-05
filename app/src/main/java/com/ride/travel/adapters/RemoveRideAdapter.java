package com.ride.travel.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.ride.travel.models.LatLng;
import com.ride.travel.models.RideItem;
import com.ride.travel.rides.MyRidesActivity;

/**
 * Created by bestway on 02/07/2018.
 */

public class RemoveRideAdapter extends RecyclerView.Adapter<RemoveRideAdapter.RideItemViewHolder> {

    private List<RideItem> mList;
    private Context mContext;

    public RemoveRideAdapter(Context context, ArrayList<RideItem> list)
    {
        mList = list;
        mContext = context;
    }


    @NonNull
    @Override
    public RemoveRideAdapter.RideItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_remove_ride, parent, false);

        return new RideItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RemoveRideAdapter.RideItemViewHolder holder, final int position) {
        final RideItem currentRideItem = mList.get(position);
        String startingPoint = currentRideItem.getStartingPoint();
        String endingPoint = currentRideItem.getEndingPoint();
        final String idOfUser = currentRideItem.getIdOfUser();
        String description = currentRideItem.getDescription();
        String time = currentRideItem.getTimeToLeave();
        final LatLng startingLatLng = currentRideItem.getStartingLatLng();
        final LatLng endingLatLng = currentRideItem.getEndingLatLng();
        holder.fromAndToOfRide.setText(startingPoint + " " + mContext.getString(R.string.to) + " " + endingPoint);
        holder.description.setText(description);
        holder.dateOfRide.setText(time);





        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                MyRidesActivity.mMyRidesMap.clear();

                    MyRidesActivity.mMyRidesMap.addMarker(
                        new MarkerOptions().position(new com.google.android.gms.maps.model.LatLng(
                                startingLatLng.getLatitude()
                                ,startingLatLng.getLongitude()))
                                .title(mContext.getString(R.string.starting_point)));

                    MyRidesActivity.mMyRidesMap.addMarker(
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
                    MyRidesActivity.mMyRidesMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    MyRidesActivity.mMyRidesMap.animateCamera(CameraUpdateFactory.zoomTo(13));
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

        holder.removeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            holder.itemView.setVisibility(View.GONE);
            mList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_RIDES)
                        .child(currentRideItem.getIdOfRide()).removeValue();
            }
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
                MyRidesActivity.mMyRidesMap.addPolyline(lineOptions);
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
        public TextView fromAndToOfRide, dateOfRide,description;
        public ImageButton removeRide;

        public RideItemViewHolder(View view) {
            super(view);
            fromAndToOfRide = view.findViewById(R.id.ID_item_from_and_to);
            dateOfRide = view.findViewById(R.id.ID_item_date_of_ride);
            removeRide = view.findViewById(R.id.ID_remove_ride);
            description = view.findViewById(R.id.ID_item_description_of_ride);
        }
    }



}
