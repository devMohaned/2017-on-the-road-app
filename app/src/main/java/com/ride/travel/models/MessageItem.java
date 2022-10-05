package com.ride.travel.models;

/**
 * Created by bestway on 14/04/2018.
 */

public class MessageItem {

    public String  messenger_id,sender_id;

    public MessageItem(String messenger_id, String sender_id) {
        this.messenger_id = messenger_id;
        this.sender_id = sender_id;
    }

    public MessageItem()
    {

    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMessenger_id() {
        return messenger_id;
    }

    public void setMessenger_id(String messenger_id) {
        this.messenger_id = messenger_id;
    }
}
