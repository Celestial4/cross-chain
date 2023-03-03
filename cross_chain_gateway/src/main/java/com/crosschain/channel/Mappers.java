package com.crosschain.channel;

import com.crosschain.common.Chain;
import com.crosschain.common.Channel;
import org.springframework.jdbc.core.RowMapper;

public class Mappers {
    public static RowMapper<Channel> channelRowMapper = (resultSet, row)->{
        Channel channel = new Channel();
        channel.setChannelId(resultSet.getString(1));
        channel.setChannelName(resultSet.getString(2));
        channel.setStatus(resultSet.getInt(3));
        return channel;
    };

    public static RowMapper<Chain> chainRowMapper = (res,row)->{
        Chain chain = new Chain();
        chain.setChainId(res.getString(1));
        chain.setChainName(res.getString(2));
        chain.setStatus(res.getInt(3));
        return chain;
    };
}