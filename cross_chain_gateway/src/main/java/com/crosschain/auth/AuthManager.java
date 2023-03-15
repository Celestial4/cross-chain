package com.crosschain.auth;

import java.util.List;

public class AuthManager {

    private AuthDB db;

    public boolean authForUser(String user, String passwd) {
        List<AuthEntity> allUser = db.getAllUser();
        for (AuthEntity u : allUser) {
            if (u.getUsername().equals(user) && u.getPasswd().equals(passwd)) {
                return true;
            }
        }
        return false;
    }

}