package com.ride.travel.messaging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;

import com.ride.travel.models.ChatMessage;
import com.ride.travel.Utils.Constants;
import com.ride.travel.Utils.FirebaseMethods;
import com.ride.travel.models.User;

import static com.ride.travel.HomePage.mAuth;

/**
 * Created by bestway on 28/03/2018.
 */

public class MessagingActivity extends AppCompatActivity {


    private EditText mMessageEditText;
    private String userID;
    private LinearLayout layout;
    DatabaseReference mReceiverDatabaseReference;
    DatabaseReference mSenderDatabaseReference;
    ImageView mDoneButton;
    private User receivedUser;
    private Toolbar mToolbar;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            String id;
            if (extras == null) {
                id = null;
            } else {
                id = extras.getString(Constants.INTENT_USER_ID);
                this.userID = id;
            }

        }

        setupWidgits();
        getUserDetail();
        setupClicks();
        connectToWantedUser();
    }

    private void setupWidgits() {
        mMessageEditText = (EditText) findViewById(R.id.ID_message_edit_text);
        layout = (LinearLayout) findViewById(R.id.ID_messaging_linear_layout);

        mDoneButton = (ImageView) findViewById(R.id.ID_done_my_message);

         mToolbar = (Toolbar) findViewById(R.id.ID_messaging_toolbar);
                  setSupportActionBar(mToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

        mScrollView = (ScrollView) findViewById(R.id.ID_messaging_scroll_view);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    private void connectToWantedUser()
    {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(Constants.DATABASE_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (ds.getKey().equals(userID))
                    {
                        try{
                        User targetedUser = ds.getValue(User.class);
                        receivedUser  = targetedUser;
                        mToolbar.setTitle(receivedUser.getName());
                    }catch (NullPointerException e)
                        {}
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setupClicks() {
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do what you want when you send a message
                String pushedKey = mSenderDatabaseReference.push().getKey();
                ChatMessage chatMessage = new ChatMessage(pushedKey,mCurrentUser.getId()
                        , mMessageEditText.getText().toString().trim()
                        , mCurrentUser.getName());
                if (!mMessageEditText.getText().toString().trim().isEmpty()) {
                    mReceiverDatabaseReference.child(pushedKey).setValue(chatMessage);
                    mSenderDatabaseReference.child(pushedKey).setValue(chatMessage);
                    mMessageEditText.setText("");
                } else {
                    mMessageEditText.setError(getString(R.string.all_fields_must_be_filled));
                }

            }
        });
    }

    String senderWithReceiver;
    String recieverWithSender;
    private void setupDatabase() {
        if (mSenderDatabaseReference != null) {
            mSenderDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        addMessageBox(chatMessage);
                    mScrollView.post(new Runnable() {

                        @Override
                        public void run() {
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

          /*  if (mReceiverDatabaseReference != null)
            {
                mReceiverDatabaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                        addMessageBox(chatMessage);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }*/


        }
    }

    private User mCurrentUser;

    private void getUserDetail() {
        final FirebaseMethods firebaseMethods = new FirebaseMethods(MessagingActivity.this);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCurrentUser = firebaseMethods.getUserData(dataSnapshot);
                senderWithReceiver = mCurrentUser.getId() + Constants.CHAT_WITH + userID;
                recieverWithSender = userID + Constants.CHAT_WITH + mCurrentUser.getId();


                mSenderDatabaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(Constants.CHAT)
                        .child(mCurrentUser.getId())
                        .child(senderWithReceiver);

                mReceiverDatabaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(Constants.CHAT)
                        .child(userID)
                        .child(recieverWithSender);



              /*  mSenderDatabaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(Constants.DATABASE_MESSAGES_CONTAINER)
                        .child(mCurrentUser.getId())
                        .child(senderWithReceiver)
                .child(Constants.CHAT);

                mReceiverDatabaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(Constants.DATABASE_MESSAGES_CONTAINER)
                        .child(userID)
                        .child(recieverWithSender)
                        .child(Constants.CHAT);*/

                setupDatabase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void addMessageBox(ChatMessage chatMessage) {
        TextView textView = new TextView(MessagingActivity.this);
        textView.setText(chatMessage.getMessageText());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (chatMessage.getUserID().equals(mAuth.getUid())) {
            lp.setMargins(15, 5, 15, 10);
            lp.gravity = Gravity.LEFT;
            textView.setLayoutParams(lp);


            textView.setBackgroundResource(R.drawable.left_messaging_shape);
        } else {
            lp.setMargins(15, 5, 15, 10);
            lp.gravity = Gravity.RIGHT;
            textView.setLayoutParams(lp);
            textView.setBackgroundResource(R.drawable.right_messaging_shape);
        }

        layout.addView(textView);
//        mMessagingScrollView.fullScroll(View.FOCUS_DOWN);
    }

}
