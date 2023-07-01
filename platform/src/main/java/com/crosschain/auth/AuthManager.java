package com.crosschain.auth;

import com.crosschain.datasource.UserAuthSource;

import java.util.List;

public class AuthManager {

    public void setDb(UserAuthSource db) {
        this.db = db;
    }

    private UserAuthSource db;

    public boolean authForUser(String user, String passwd) {
        List<AuthEntity> allUser = db.getAllUser();
        for (AuthEntity u : allUser) {
            if (u.getUsername().equals(user) && u.getPasswd().equals(passwd)) {
                return true;
            }
        }
        return false;
    }

    public String getUserId(String user) {
        return db.getUser(user).getUserId();
    }

}