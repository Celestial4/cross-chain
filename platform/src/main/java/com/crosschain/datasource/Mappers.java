package com.crosschain.datasource;

import com.crosschain.auth.AuthEntity;
import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import org.springframework.jdbc.core.RowMapper;

public class Mappers {
    public static RowMapper<Group> channelRowMapper = (resultSet, row) -> {
        Group group = new Group();
        group.setGroupId(resultSet.getString(1));
        group.setGroupName(resultSet.getString(2));
        group.setStatus(resultSet.getInt(3));
        return group;
    };

    public static RowMapper<Chain> chainRowMapper = (res, row) -> {
        Chain chain = new Chain();
        chain.setChainId(res.getString(1));
        chain.setChainName(res.getString(2));
        chain.setStatus(res.getInt(3));
        chain.setChainType(res.getString(4));
        return chain;
    };

    public static RowMapper<AuthEntity> authEntityRowMapper = (res,row)->{
        AuthEntity authEntity = new AuthEntity();
        authEntity.setUserId(res.getString(1));
        authEntity.setUsername(res.getString(2));
        authEntity.setPasswd(res.getString(3));
        return authEntity;
    };

}