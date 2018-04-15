package com.example.yun.meetup.requests;

/**
 * Created by alessio on 15-Apr-18.
 */

public class AddCommentRequest {

    private String user_id;
    private String event_id;
    private String text;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
