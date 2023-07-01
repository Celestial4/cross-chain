package com.crosschain.filter;

import com.crosschain.auth.AuthManager;
import com.crosschain.common.SystemInfo;
import com.crosschain.service.request.CrossChainVo;

public class RequestFilter {

    public void setAuthManager(AuthManager authManager) {
        this.authManager = authManager;
    }

    private AuthManager authManager;

    public void doFilter(CrossChainVo crossChainVo) throws Exception{
        if (!authManager.authForUser(crossChainVo.getUser_name(), crossChainVo.getUser_token())) {
            throw new Exception("authentication failed!");
        }
        if (crossChainVo.getDes_chain().equals(SystemInfo.getSelfChainName())) {
            throw new Exception("目标链指向自己");
        }
    }
}