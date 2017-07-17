package com.knowlounge.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mansu on 2017-03-23.
 */

public class RoomUsers {


    ConcurrentHashMap<String, RoomUser> userMap;

    public RoomUsers() {
        userMap = new ConcurrentHashMap<String, RoomUser>();
    }

    public void addUser(RoomUser user) {
        userMap.put(user.getUserNo(), user);
    }

//    public void addUser(int index, RoomUser user) {
//        userList.put(index, user);
//    }

    public void removeUser(RoomUser user) {
        userMap.remove(user);
    }

    public int getUserCount() {
        return userMap.size();
    }

    public ConcurrentHashMap<String, RoomUser> getUserMap() {
        return userMap;
    }


    public static class Builder {
        public RoomUsers build() {
            return new RoomUsers();
        }
    }
}
