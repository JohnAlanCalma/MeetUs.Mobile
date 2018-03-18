package com.example.yun.meetup.requests;

import android.graphics.Bitmap;

/**
 * Created by alessio on 17-Mar-18.
 */

public class UpdateProfileRequest {
    private String user_id;
    private String description;
    private String interests;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }
}
