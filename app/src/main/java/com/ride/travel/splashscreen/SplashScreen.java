package com.ride.travel.splashscreen;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ride.travel.R;

/**
 * Created by bestway on 25/04/2018.
 */

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
    }
}
