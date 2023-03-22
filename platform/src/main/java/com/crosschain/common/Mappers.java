package com.crosschain.common;

import com.crosschain.auth.AuthEntity;
import org.springframework.jdbc.core.RowMapper;

public class Mappers {
    public static RowMapper<Channel> channelRowMapper = (resultSet, row) -> {
        Channel channel = new Channel();
        channel.setChannelId(resultSet.getString(1));
        channel.setChannelName(resultSet.getString(2));
        channel.setStatus(resultSet.getInt(3));
        return channel;
    };

    public static RowMapper<Chain> chainRowMapper = (res, row) -> {
        Chain chain = new Chain();
        chain.setChainId(res.getString(1));
        chain.setChainName(res.getString(2));
        chain.setStatus(res.getInt(3));
        return chain;
    };

    public static RowMapper<AuthEntity> authEntityRowMapper = (res,row)->{
        AuthEntity authEntity = new AuthEntity();
        authEntity.setUsername(res.getString(2));
        authEntity.setPasswd(res.getString(3));
        return authEntity;
    };

}