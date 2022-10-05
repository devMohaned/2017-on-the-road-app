package com.ride.travel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robertsimoes.shareable.Shareable;
import com.ride.travel.rating.GiveRatingActivity;
import com.ride.travel.R;

import java.util.ArrayList;
import java.util.List;

import com.ride.travel.Utils.AppUtils;
import com.ride.travel.Utils.Constants;
import com.ride.travel.login.LoginActivity;
import com.ride.travel.maps.FindDriverTargetMapActivity;
import com.ride.travel.maps.FindPassengerTargetMapActivity;
import com.ride.travel.messaging.MessageListActivity;
import com.ride.travel.miscellaneous.NoInternetConnection;
import com.ride.travel.models.DummyLocation;
import com.ride.travel.models.User;
import com.ride.travel.rides.MyRidesActivity;
import com.ride.travel.rides.RidesActivity;

public class HomePage extends AppCompatActivity {

    private TextView mNameOfUser;
    private Context mContext;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


        if (isNetworkAvailable()) {
            setupAds();
            setupViews();
            setupFirebaseAuth();
            setupDatabase();
//            addDummyLocations();

        } else {
            Intent i = new Intent(this, NoInternetConnection.class);
            startActivity(i);
            finish();
        }

    }

    private void setupAds() {
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7003413723788424/4679484375");
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setupViews() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );



        AppUtils.hideSoftKeyboard(HomePage.this);


        mContext = this;

        //        onTheRoad = findViewById(R.id.ID_on_the_road);
        mNameOfUser = findViewById(R.id.ID_current_user_name);




        Button findDrivers = findViewById(R.id.ID_find_drivers);
        findDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String provider = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if(!provider.equals("")){
                    //GPS Enabled
                    Intent intent = new Intent(HomePage.this, FindDriverTargetMapActivity.class);
                    startActivity(intent);
                }else{
                    buildAlertMessageNoGps();
                }


            }
        });

        Button findPassengers = findViewById(R.id.ID_find_passengers);
        findPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String provider = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if(!provider.equals("")){
                    //GPS Enabled
                    Intent intent = new Intent(HomePage.this, FindPassengerTargetMapActivity.class);
                    startActivity(intent);
                }else{
                    buildAlertMessageNoGps();
                }
            }
        });


        Button ridesAvailable = findViewById(R.id.ID_rides_available);
        ridesAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, RidesActivity.class);
                displayInterstitial(intent);

//                startActivity(intent);
            }
        });

        Button messagesButton = findViewById(R.id.ID_messages);
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MessageListActivity.class);
                displayInterstitial(intent);
            }
        });

        Button myRide = findViewById(R.id.ID_my_rides);
        myRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,MyRidesActivity.class);
                displayInterstitial(intent);
            }
        });

        Button giveRating = findViewById(R.id.ID_give_rating);
        giveRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,GiveRatingActivity.class);
                displayInterstitial(intent);
            }
        });


        Button logout = findViewById(R.id.ID_log_out);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuth.signOut();

                } catch (NullPointerException e) {
                    Log.e("HomePage: ", e.toString());
                }
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });


        ImageView mFacebookShare, mTwitterShare, mLinkedInShare, mTumblrShare, mGooglePlusShare, mAnyShare;

        mFacebookShare = findViewById(R.id.ID_fb_share);
        mFacebookShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.FACEBOOK)
                        .build();
                shareAction.share();
            }
        });

        mTwitterShare = findViewById(R.id.ID_twitter_share);
        mTwitterShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.TWITTER)
                        .build();
                shareAction.share();
            }
        });

        mLinkedInShare = findViewById(R.id.ID_linkedIn_share);
        mLinkedInShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.LINKED_IN)
                        .build();
                shareAction.share();
            }
        });

        mTumblrShare = findViewById(R.id.ID_tumblr_share);
        mTumblrShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.TUMBLR)
                        .build();
                shareAction.share();
            }
        });


        mGooglePlusShare = findViewById(R.id.ID_google_plus_share);
        mGooglePlusShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.GOOGLE_PLUS)
                        .build();
                shareAction.share();
            }
        });

        mAnyShare = findViewById(R.id.ID_any_share);
        mAnyShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shareable shareAction = new Shareable.Builder(mContext)
                        .message("This is my message")
                        .socialChannel(Shareable.Builder.ANY)
                        .build();
                shareAction.share();
            }
        });

    }


    private void setupDatabase() {
        try {
            mNameOfUser.setText(mAuth.getCurrentUser().getDisplayName());



        } catch (NullPointerException e) {
            Toast.makeText(mContext, "Couldn't get your name", Toast.LENGTH_LONG).show();

            try {
                DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_USERS);
                mUserDatabase.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        mNameOfUser.setText(currentUser.getName());

                        FirebaseUser user = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(currentUser.getName()).build();

                        user.updateProfile(profileUpdates);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } catch (NullPointerException e2) {
                Toast.makeText(mContext, "Couldn't get your name", Toast.LENGTH_LONG).show();
            }

        }


    }


    public static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    //     if (user.isEmailVerified()) {
                    // User is signed in

                } else {
                    // User is signed out
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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


    private void addDummyLocations() {
        ArrayList list = new ArrayList();


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


        DatabaseReference mPushedRef;
        for (int i = 0; i < locationlocations.size(); i++) {
            list.add(i, new DummyLocation("a", locationlocations.get(i), 121));


            mPushedRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DATABASE_GEO_FIRE_PASSENGERS_AVAILABLE)
                    .child("Count" + i);

            DatabaseReference mPushedRef2 = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DATABASE_GEO_FIRE_DRIVERS_AVAILABLE)
                    .child("Count" + i);
            mPushedRef2.setValue(list.get(i));


            mPushedRef.setValue(list.get(i));
        }
    }


    public void displayInterstitial(final Intent intent) {

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }else if (mInterstitialAd.isLoading())
        {
            startActivity(intent);
            AdRequest newRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(newRequest);
        }

        // If Ads are loaded, show Interstitial else show nothing.
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                startActivity(intent);
                AdRequest newRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(newRequest);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                startActivity(intent);
                AdRequest newRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(newRequest);
            }

        });

    }

}
