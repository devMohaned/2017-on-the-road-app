package com.ride.travel.models;

import java.util.Date;

/**
 * Created by bestway on 28/03/2018.
 */

public class ChatMessage {
    private String messageID,messageText,messageUser,userID;
    private long messageTime;

   public ChatMessage()
   {}

    public ChatMessage(String messageID,String userID,String messageText, String messageUser) {
        this.messageID = messageID;
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.userID = userID;
        this.messageTime = new Date().getTime();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
