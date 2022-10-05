package com.ride.travel.login;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;

import com.ride.travel.HomePage;
import com.ride.travel.Utils.AppUtils;
import com.ride.travel.Utils.Constants;
import com.ride.travel.miscellaneous.NoInternetConnection;
import com.ride.travel.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private RelativeLayout splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);
      /*  LoginManager.getInstance().logOut();

        if (mAuth != null)
        {
            mAuth.signOut();
        }
*/
        // Add code to print out the key hash
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ride.travel",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/


        if (isNetworkAvailable()) {
            setupFacebook();
            setupWidgits();
        }else{
            Intent i = new Intent(this, NoInternetConnection.class);
            startActivity(i);
            finish();
        }

    }

    private void setupWidgits() {
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        Button mLoginButton = (Button) findViewById(R.id.email_sign_in_button);
        TextView mDontHaveAccount = (TextView) findViewById(R.id.ID_donthaveAccount_register);
        mDontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, Registeration.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        // Set up the login form.
        mAuth = FirebaseAuth.getInstance();
        splashScreen = findViewById(R.id.ID_splash_screen);
        setupFirebaseAuth();
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.hideSoftKeyboard(LoginActivity.this);

                final String email = String.valueOf(mEmail.getText());
                final String password = String.valueOf(mPassword.getText());
                if (isStringNull(email) || isStringNull(password)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.all_fields_must_be_filled), Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());


                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                    } else {
                                        IsEmailVerified();
                                    }
                                }

                                // ...

                            });
                }


            }
        });
    }


    private boolean isStringNull(String string) {
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    //--------------------------------Firebase-----------------------
    // -------------------------------------Firebase---------------------------------

    private static final String TAG = "HomePage:";
    private FirebaseAuth mAuth;
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

                    Intent intent = new Intent(LoginActivity.this, HomePage.class);
                    startActivity(intent);
                    finish();
                    splashScreen.setVisibility(View.GONE);


                } else {
                    // User is signed out
                    splashScreen.setVisibility(View.GONE);

                    Log.d(TAG, "onAuthStateChanged:signed_out");
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

    private void IsEmailVerified() {

        Toast.makeText(LoginActivity.this, getString(R.string.auth_sucess), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Facebook

    private CallbackManager mCallbackManager;

    private void setupFacebook() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.ID_facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());




            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            if (LoginManager.getInstance() != null) {
                                LoginManager.getInstance().logOut();
                            }
                            if (mAuth != null)
                            {
                                mAuth.signOut();
                            }
                        }

                        // ...
                    }
                });
    }

    private void updateUI(final FirebaseUser firebaseUser) {
        User user;
        try {
            if (!firebaseUser.getUid().isEmpty()) {
                user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail());
            } else {
                user = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail());

            }

            final String userID = mAuth.getCurrentUser().getUid();

            setupNewUser(user);

        } catch (NullPointerException e) {
            Log.e("LOGIN ACTIVITY: ", e.toString());

        }


    }

    public void setupNewUser(final User user) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference(Constants.DATABASE_USERS);
        // Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String userName = user.getName();
                String email = user.getEmail();
                    try{
                        User newUser = new User(mAuth.getCurrentUser().getUid(), userName,
                                email);
                        String userID = mAuth.getCurrentUser().getUid();
                        myRef.child(userID).setValue(newUser);
                    }catch (NullPointerException e)
                    {
                        Toast.makeText(LoginActivity.this, getString(R.string.creating_new_account_using_facebook_failed), Toast.LENGTH_LONG).show();
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

