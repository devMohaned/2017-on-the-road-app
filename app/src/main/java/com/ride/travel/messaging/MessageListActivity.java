package com.ride.travel.messaging;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ride.travel.R;

import java.util.ArrayList;
import java.util.List;

import com.ride.travel.Utils.Constants;
import com.ride.travel.adapters.MessageAdapter;
import com.ride.travel.models.MessageItem;

import static com.ride.travel.HomePage.mAuth;

/**
 * Created by bestway on 14/04/2018.
 */

public class MessageListActivity extends AppCompatActivity {

    private RecyclerView mMessageListRecyclerView;
    private Context mContext;
    private MessageAdapter mMessageAdapter;
    private List<MessageItem> messageItemList;
    private DatabaseReference mDatabase;
    private DatabaseReference mMessageContainterDatabaseReference;
    private TextView emptyTextView;
    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_list);

      /*  String userID;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                userID = null;
            } else {
                userID = extras.getString(Constants.USERID);
                this.userID = userID;
            }
        }
*/
        setupViews();
        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.ID_message_list_toolbar);
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




    private void setupViews() {




        mContext = MessageListActivity.this;

        emptyTextView = (TextView) findViewById(R.id.ID_empty_text_view_for_recycler_view);


        mMessageListRecyclerView = (RecyclerView) findViewById(R.id.ID_messages_list_recyclerview);
        mMessageListRecyclerView.setHasFixedSize(true);
        mMessageListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));





        messageItemList = new ArrayList<>();

        mMessageAdapter = new MessageAdapter(mContext, messageItemList);
        mMessageListRecyclerView.setAdapter(mMessageAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mAuth != null) {
            mMessageContainterDatabaseReference = mDatabase.child(Constants.DATABASE_MESSAGES_CONTAINER).child(mAuth.getUid());
//           mMessageContainterDatabaseReference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                  /*  for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        MessageItem messageItem = ds.getValue(MessageItem.class);
//                        messageItemList.add(messageItem);
//                        mMessageAdapter.notifyDataSetChanged();
//                    }*/
//                    if (!messageItemList.isEmpty()) {
//                        emptyTextView.setVisibility(View.GONE);
//                    }
//
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
          mMessageContainterDatabaseReference.addChildEventListener(new ChildEventListener() {
              @Override
              public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                  MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                  messageItemList.add(messageItem);
                  mMessageAdapter.notifyDataSetChanged();
                  emptyTextView.setVisibility(View.GONE);
              }

              @Override
              public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

              }

              @Override
              public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

              }

              @Override
              public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });

        }else {
            Toast.makeText(mContext, "There's something wrong with logging in, please reload the page", Toast.LENGTH_LONG).show();
        }
    }
}
