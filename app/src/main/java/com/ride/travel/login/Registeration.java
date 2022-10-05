package com.ride.travel.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ride.travel.R;

import com.ride.travel.Utils.AppUtils;
import com.ride.travel.models.User;

import com.ride.travel.Utils.FirebaseMethods;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

/**
 * Created by midomohaned on 29/12/2016.
 */

public class Registeration extends AppCompatActivity {

    // the username of the user is placed in a EditText
    private EditText userName;
    // the e-mail of the user is placed in a EditText
    private EditText email;
    // the password & confirm password of the user is placed in EditText
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    // Spinner in which we choose what talent we are good at
    private Spinner talentSpinner;
    private FirebaseMethods firebaseMethods;

    Context mContext = Registeration.this;
    public static ProgressBar progressBar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the activity to registeration activity
        setContentView(R.layout.activity_registeration);

        setUpWidgits();
        firebaseMethods = new FirebaseMethods(mContext);
        setupFirebaseAuth();

    }


    private void setUpWidgits() {


        // Email is connected to EditText view.
        email = (EditText) findViewById(R.id.emailID);
        // username is connected to EditText view
        userName = (EditText) findViewById(R.id.usernameID);
        // password & passwordConfirmation are connected to EditText
        passwordEditText = (EditText) findViewById(R.id.passwordID);
        passwordConfirmEditText = (EditText) findViewById(R.id.confirmPasswordID);

        progressBar = (ProgressBar) findViewById(R.id.ID_registeration_progressBar);
        progressBar.setVisibility(View.GONE);
        // inizialize the account database connector to this context.

        // Shows password if checkBox is checked and doesn't show password if CheckBox is unchecked.
        CheckBox passwordCheckBox = (CheckBox) findViewById(R.id.CheckboxPaswordID);
        passwordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    passwordEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
                    passwordConfirmEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    passwordEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordConfirmEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }
        });

        //Register button doing a method "onRegistering" once it's clicked.
        Button registerButton = (Button) findViewById(R.id.registerButtonID);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegistering();
            }
        });




        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_registeration_toolbar);
                  setSupportActionBar(mToolbar);

                  // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Registeration.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void onRegistering() {

        // obtain & convert everyText inserted in the userName/email/password/password confirmation into a String type.
        String usernameText = userName.getText().toString();
        String emailText = email.getText().toString();
        String userPassword = passwordEditText.getText().toString();
        String confirmPassword = passwordConfirmEditText.getText().toString();

        // Several condition 1/ if username is empty
        if (usernameText.matches("")) {
            userName.setError(getString(R.string.empty_username));
            //  Toast.makeText(Registeration.this, "No username is found.", Toast.LENGTH_SHORT).show();
        } else {
            userName.setError(null);
        }
        // if email is empty
        if (emailText.matches("")) {
            email.setError(getString(R.string.empty_email));
        }
        // if userpassword is empty
        else if (userPassword.contains(" ")) {
            passwordEditText.setError(getString(R.string.you_cannot_have_space_in_password));
        }
        // if confirmpassword is empty
        else if (confirmPassword.contains(" ")) {
            passwordConfirmEditText.setError(getString(R.string.you_cannot_have_space_in_password));
        }
        // if userpassword is empty
        else if (userPassword.matches("")) {
            passwordEditText.setError(getString(R.string.empty_password));
            //   Toast.makeText(Registeration.this, "You cannot leave Passwords empty.", Toast.LENGTH_SHORT).show();
        }
        // if confirmpassword is empty
        else if (confirmPassword.matches("")) {
            passwordConfirmEditText.setError(getString(R.string.empty_password));
        } else if (userPassword.trim().length() < 8) {
            passwordEditText.setError(getString(R.string.password_should_have_eight_plus));
        } else if (confirmPassword.trim().length() < 8) {
            passwordConfirmEditText.setError(getString(R.string.password_should_have_eight_plus));

        }
        // if all the above is wrong(Passwords are not matched), thus:
        else if (!passwordEditText.getText().toString().trim().equals(passwordConfirmEditText.getText().toString().trim())) {
            Toast.makeText(Registeration.this, getString(R.string.password_are_not_matched), Toast.LENGTH_SHORT).show();
        }
        if (usernameText.trim().length() > 0 && emailText.trim().length() > 0 && confirmPassword.matches(userPassword) && userPassword.trim().length() > 7) {

            AppUtils.hideSoftKeyboard(Registeration.this);

            User user = new User(userName.getText().toString(),
                    email.getText().toString());

            firebaseMethods.registerNewEmail(emailText, userPassword, user);
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    // Once the activity of Registeration is shown, it'll read the CURRENT database.
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        //  readDatabase();
    }


    /*
       ------------------------------------ Firebase ---------------------------------------------
        */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    /**
     * Setup the firebase auth object
     */
    private String TAG = "Registeration: ";

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}