package com.ride.travel.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.Utils.Constants;
import com.ride.travel.models.RatingItem;
import com.ride.travel.models.User;
import com.ride.travel.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.ride.travel.HomePage.mAuth;
import static com.ride.travel.Utils.Constants.DATABASE_RATING;

/**
 * Created by bestway on 02/07/2018.
 */

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RideItemViewHolder> {

    private List<RatingItem> mList;
    private Context mContext;

    public RatingAdapter(Context context, ArrayList<RatingItem> list) {
        mList = list;
        mContext = context;
    }


    @NonNull
    @Override
    public RatingAdapter.RideItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);

        return new RideItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RatingAdapter.RideItemViewHolder holder, int position) {
        final RatingItem currentRating = mList.get(position);
        String myCurrentUserID = mAuth.getUid();
        String userID = currentRating.getId();
        String feedback = currentRating.getRatingText();
        long timestamp = currentRating.getTimestamp();
        float x = currentRating.getRating();

        FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_USERS)
                .child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                try {
                    holder.nameOfUserOfRating.setText(u.getName());
                }catch (NullPointerException e)
                {
                    holder.nameOfUserOfRating.setText("Null Name");
                }}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.feedbackOfRating.setText(feedback);

        holder.timingOfRating.setText(getDateCurrentTimeZone(timestamp));

        FirebaseDatabase.getInstance().getReference().child(DATABASE_RATING).child(myCurrentUserID).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                com.ride.travel.models.RatingItem ratingItem = dataSnapshot.getValue(com.ride.travel.models.RatingItem.class);
                try {
                    float rating = ratingItem.getRating();
                    holder.ratingBarOfRating.setRating(rating);
                } catch (NullPointerException e) {
                    holder.ratingBarOfRating.setRating(0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class RideItemViewHolder extends RecyclerView.ViewHolder {
        public TextView nameOfUserOfRating, timingOfRating,feedbackOfRating;
        public RatingBar ratingBarOfRating;

        public RideItemViewHolder(View view) {
            super(view);
            nameOfUserOfRating = view.findViewById(R.id.ID_rating_item_name_of_user);
            timingOfRating = view.findViewById(R.id.ID_timing_of_rating);
            feedbackOfRating = view.findViewById(R.id.ID_rating_item_feedback);
            ratingBarOfRating = view.findViewById(R.id.ID_rating_bar_item);



        }
    }



}
