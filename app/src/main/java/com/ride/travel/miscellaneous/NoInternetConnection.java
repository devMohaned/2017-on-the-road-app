package com.ride.travel.miscellaneous;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ride.travel.R;

import com.ride.travel.login.LoginActivity;

/**
 * Created by midomohaned on 30/09/2017.
 */

public class NoInternetConnection extends AppCompatActivity {

    private Button mRetryButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nointernetconnection);
        mRetryButton = (Button) findViewById(R.id.ID_retry_button_no_connection);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(NoInternetConnection.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);    }
}
