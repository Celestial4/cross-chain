package com.crosschain.channel;

import com.crosschain.common.Chain;
import com.crosschain.common.Channel;
import com.crosschain.common.Loggers;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelSource {

    private final Logger logger = LoggerFactory.getLogger(ChannelSource.class);

    @Resource
    private JdbcTemplate sql;


    public Channel getChannel(String channelName) {
        Channel c = sql.queryForObject("select * from channel where channel_name=?", Mappers.channelRowMapper, channelName);

        c.getMembers().addAll(getRelatedChains(channelName));
        logger.info(Loggers.LOGFORMAT, String.format("read channel[%s] successfully!", c.getChannelName()));
        return c;
    }

    public List<Channel> getAllChannel() {
        List<Channel> channels = sql.query("select * from channel", Mappers.channelRowMapper, null);
        for (Channel c : channels) {
            List<Chain> members = c.getMembers();
            members.addAll(getRelatedChains(c.getChannelName()));
        }
        return channels;
    }

    public List<Chain> getChains(String chainNames) {
        return sql.query("select * from chain where chain_name in (?)", Mappers.chainRowMapper, chainNames);
    }

    private List<Chain> getRelatedChains(String channel) {
        return sql.query("select chain_id,chain_name,chain_status from (select t1.channel_name name,t3.*  from channel t1,channel_chain t2,chain t3 where t1.channel_id=t2.channel_id and t2.chain_id=t3.chain_id) t4 where name=?", Mappers.chainRowMapper, channel);
    }

    public int newChannel(Channel channel) {
        int cnt = 0;
        try {
            cnt = sql.update("insert into channel values(?,?,?)", channel.getChannelId(), channel.getChannelName(), channel.getStatus());
            logger.info(Loggers.LOGFORMAT, "create channel successfully!");
        } catch (Exception e) {
            logger.error(Loggers.LOGFORMAT, e.getMessage());
        }
        return cnt;
    }

    /**
     * ?????????????????????????????????????????????
     * @param channel
     * @return
     */
    public int associate(Channel channel) {
        int cnt = 0;
        for (Chain chain : channel.getMembers()) {
            cnt += sql.update("insert into channel_chain values(?,?)", channel.getChannelId(), chain.getChainId());
        }

        logger.info(Loggers.LOGFORMAT, "build channel chain relationship successfully!, chain counts:" + cnt);
        return cnt;
    }

    public int addChain(Chain... chain) {
        int cnt = 0;
        try {
            for (Chain c : chain) {
                cnt += sql.update("insert into chain values(?,?,?)", c.getChainId(), c.getChainName(), c.getStatus());
            }
            logger.info(Loggers.LOGFORMAT, "insert chains successfully! total counts: " + cnt);
        } catch (Exception e) {
            logger.error(Loggers.LOGFORMAT, e.getMessage());
        }
        return cnt;
    }

    /**
     * ???????????????
     * @param channel_id ??????id
     * @param chain_id  ???id
     */
    public int associate(String channel_id, String chain_id) {
        return sql.update("insert into channel_chain values(?,?)", channel_id, chain_id);
    }

    public void deAssociate(String channel_id, String chain_id) {
        sql.update("delete from channel_chain where channel_id=? and chain_id=?", channel_id, chain_id);
    }

    public void updateChannel(String cnl_name, int status) {
        sql.update("update from channel set status=? where channel_name=?", status, cnl_name);
    }

    public void updateChain(String c_name, int status) {
        sql.update("update from chain set status=? where chain_name=?", status, c_name);
    }
}