package com.crosschain.filter;

import com.crosschain.auth.AuthManager;
import com.crosschain.common.SystemInfo;
import com.crosschain.service.RequestEntity;

public class RequestFilter {

    private AuthManager authManager;

    public void doFilter(RequestEntity requestEntity) throws Exception{
        if (!authManager.authForUser(requestEntity.getUserName(), requestEntity.getUserToken())) {
            throw new Exception("authentication failed!");
        }
        if (requestEntity.getDesChain().equals(SystemInfo.getSelfChainName())) {
            throw new Exception("目标链指向自己");
        }
    }
}