package com.example.yun.meetup.interfaces;

import com.example.yun.meetup.models.UserInfo;

/**
 * Created by alessio on 10-Mar-18.
 */

public interface RemoveMemberCallback {

    void onRemoveMemberClicked(UserInfo userInfo);
}
