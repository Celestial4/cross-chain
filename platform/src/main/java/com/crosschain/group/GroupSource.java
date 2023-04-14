package com.crosschain.group;

import com.crosschain.common.Chain;
import com.crosschain.common.Group;
import com.crosschain.common.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class GroupSource {

    private final Logger logger = LoggerFactory.getLogger(GroupSource.class);

    @Resource
    private JdbcTemplate sql;


    public Group getGroup(String channelName) throws Exception {
        Group c = sql.queryForObject("select * from channel where channel_name=?", Mappers.channelRowMapper, channelName);
        if (c == null) {
            throw new Exception("跨链群组不存在");
        }
        c.addMember(getRelatedChains(channelName));
        return c;
    }

    public List<Group> getAllGroups() {
        List<Group> groups = sql.query("select * from channel", Mappers.channelRowMapper);
        for (Group c : groups) {
            c.addMember(getRelatedChains(c.getGroupName()));
        }
        return groups;
    }

    public List<Chain> getChains(String chainNames) {
        String stamt = String.format("select * from chain where chain_name in (%s)", chainNames);
        List<Chain> r = sql.query(stamt, Mappers.chainRowMapper);
        logger.debug("[query chains]: {},{}", r.size(), Arrays.toString(r.toArray()));
        return r;
    }

    private List<Chain> getRelatedChains(String channel) {
        return sql.query("select chain_id,chain_name,chain_status from (select t1.channel_name name,t3.*  from channel t1,channel_chain t2,chain t3 where t1.channel_id=t2.channel_id and t2.chain_id=t3.chain_id) t4 where name=?", Mappers.chainRowMapper, channel);
    }

    public int newGroup(Group group) {
        int cnt = 0;
        try {
            cnt = sql.update("insert into channel values(?,?,?)", group.getGroupId(), group.getGroupName(), group.getStatus());
            logger.debug("[new group]:create channel successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return cnt;
    }

    /**
     * 新增通道时关联这条通道的所有链
     *
     * @param group
     */
    public void associate(Group group) {
        try {
            for (Chain chain : group.getMembers()) {
                sql.update("insert into channel_chain values(?,?)", group.getGroupId(), chain.getChainId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public int addChain(Chain... chain) {
        int cnt = 0;
        try {
            for (Chain c : chain) {
                cnt += sql.update("insert into chain values(?,?,?)", c.getChainId(), c.getChainName(), c.getStatus());
            }
            logger.info("[add new chain]: insert chains successfully total counts: {}", cnt);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return cnt;
    }

    /**
     * 关联一条链
     *
     * @param channel_id 通道id
     * @param chain_id   链id
     */
    public int associate(String channel_id, String chain_id) {
        int cnt = 0;
        try {
            cnt = sql.update("insert into channel_chain values(?,?)", channel_id, chain_id);
            logger.debug("[associate group chain]:success");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return cnt;
    }

    public void deAssociate(String channel_id, String chain_id) {
        sql.update("delete from channel_chain where channel_id=? and chain_id=?", channel_id, chain_id);
    }

    public void updateGroup(String cnl_name, int status) {
        sql.update("update channel set channel_status=? where channel_name=?", status, cnl_name);
    }

    public void updateChain(String c_name, int status) {
        sql.update("update chain set chain_status=? where chain_name=?", status, c_name);
    }

    public Chain getChain(String cName) {
        return sql.queryForObject("select * from chain where chain_name=?", Mappers.chainRowMapper, cName);
    }
}