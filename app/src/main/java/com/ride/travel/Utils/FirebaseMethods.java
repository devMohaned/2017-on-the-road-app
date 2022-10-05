package com.ride.travel.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;

import com.ride.travel.models.User;

import com.ride.travel.login.LoginActivity;
import com.ride.travel.login.Registeration;

/**
 * Created by midomohaned on 16/08/2017.
 */

public class FirebaseMethods {
    private Context mContext;
    private String LOG_TAG = "FirebaseMethods";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;


    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    //    public void registerNewAccount(String name,String email,String password,String talent, String gender,String country)
    public void registerNewEmail(final String email, String password, final User registeredUser) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign up fails, display a message to the user. If sign up succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_register_failed,
                                    Toast.LENGTH_SHORT).show();
                            Registeration.progressBar.setVisibility(View.GONE);
                        } else if (task.isSuccessful()) {
                            userID = mAuth.getCurrentUser().getUid();


                            setupNewUser(registeredUser);

                        }
                        // ...
                    }
                });
    }

    public void sendEmailVarification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(mContext, R.string.auth_register_success, Toast.LENGTH_SHORT).show();
            user.sendEmailVerification();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.failed_in_sending_email_verification), Toast.LENGTH_SHORT).show();
        }
    }

    public User getUserData(@NonNull DataSnapshot dataSnapshot) {
        String userID = mAuth.getCurrentUser().getUid();
        User user = new User();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.getKey().equals(Constants.DATABASE_USERS)) {
                try {
                    user.setId(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getId());

                    user.setName(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getName()
                    );

                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );

                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "You've got null data Snap Shot " + e.getMessage());
                }
            }
        }
        return user;
    }


    public String getCurrentUserID() {
        return userID;
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

                    userID = mAuth.getCurrentUser().getUid();
                    myRef.child(userID).setValue(newUser);


                    FirebaseUser user = mAuth.getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName).build();

                    user.updateProfile(profileUpdates);



                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mContext.startActivity(intent);
                        }
                    }, 3000);
                }catch (NullPointerException e)
                {
                    Toast.makeText(mContext, mContext.getString(R.string.failed_in_making_new_account), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
