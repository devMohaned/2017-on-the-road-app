package com.ride.travel.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ride.travel.R;

import java.util.List;

import com.ride.travel.Utils.Constants;
import com.ride.travel.messaging.MessagingActivity;
import com.ride.travel.models.ChatMessage;
import com.ride.travel.models.MessageItem;
import com.ride.travel.models.User;

import static com.ride.travel.HomePage.mAuth;

/**
 * Created by bestway on 14/04/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private Context mContext;
    private List<MessageItem> mMessageItemList;


    public MessageAdapter(Context context, List<MessageItem> list) {
        mContext = context;
        mMessageItemList = list;
    }


    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MyViewHolder holder, int position) {
        final MessageItem messageItem = mMessageItemList.get(position);

        String myUserId = mAuth.getUid();
        String messengerId = messageItem.getMessenger_id();
        String sendeRId = messageItem.getSender_id();


        if (!messengerId.equals(myUserId))
        {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DATABASE_USERS).child(messengerId);
            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User wantedUser = dataSnapshot.getValue(User.class);
                    holder.mMessengerNameTextView.setText(wantedUser.getName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MessagingActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(Constants.INTENT_USER_ID, messageItem.getMessenger_id());
                    mContext.startActivity(intent);
                }
            });
            String senderWithReceiver = mAuth.getUid() + Constants.CHAT_WITH + messengerId;

            DatabaseReference mSenderDatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(Constants.CHAT)
                    .child(mAuth.getUid())
                    .child(senderWithReceiver);

            mSenderDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    holder.mLastMessage.setText(chatMessage.getMessageText());
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


        }else if (!sendeRId.equals(myUserId))
        {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.DATABASE_USERS).child(sendeRId);
            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User wantedUser = dataSnapshot.getValue(User.class);
                    holder.mMessengerNameTextView.setText(wantedUser.getName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MessagingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(Constants.INTENT_USER_ID, messageItem.getSender_id());
                    mContext.startActivity(intent);
                }
            });

            String senderWithReceiver = mAuth.getUid() + Constants.CHAT_WITH + sendeRId;

            DatabaseReference mSenderDatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(Constants.CHAT)
                    .child(mAuth.getUid())
                    .child(senderWithReceiver);

            mSenderDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    holder.mLastMessage.setText(chatMessage.getMessageText());
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


        }

       /* String senderWithReceiver = HomePage.mCurrentUserID + Constants.CHAT_WITH + messageItem.getReciever_id();

        DatabaseReference mSenderDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.CHAT)
                .child(HomePage.mCurrentUserID)
                .child(senderWithReceiver);


        mSenderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               messageItem.setMessages_count(dataSnapshot.getChildrenCount());
                holder.mMessagesCountTextView.setText(String.valueOf(messageItem.getMessages_count()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.mMessengerNameTextView.setText(recieverName);
*/




    }


    @Override
    public int getItemCount() {
        return mMessageItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mMessengerNameTextView,mLastMessage;

        public MyViewHolder(View itemView) {
            super(itemView);
            mMessengerNameTextView = itemView.findViewById(R.id.ID_name_of_messenger);
            mLastMessage = itemView.findViewById(R.id.ID_last_message);

        }
    }
}
