package com.crosschain.datasource;

import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import com.crosschain.exception.SqlException;
import com.crosschain.exception.UniException;
import com.sun.deploy.net.socket.UnixDomainSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ejb.access.EjbAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.channels.Channel;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Component
public class GroupAndChainSource {

    private final Logger logger = LoggerFactory.getLogger(GroupAndChainSource.class);

    @Resource
    private JdbcTemplate sql;


    public Group getGroup(String channelName) throws UniException {
        Group c;
        try {
            c = sql.queryForObject("select * from channel where channel_name=?", Mappers.channelRowMapper, channelName);
            if (c == null) {
                throw new SqlException("跨链群组不存在");
            }
        } catch (Exception e) {
            logger.error("获取群组{}失败：" + e.getMessage(), channelName);
            throw new SqlException(e.getMessage());
        }
        c.addMember(getRelatedChains(channelName));
        return c;
    }

    public List<Group> getAllGroups() throws UniException {

        List<Group> groups;
        try {
            groups = sql.query("select * from channel", Mappers.channelRowMapper);
        } catch (DataAccessException e) {
            logger.error("获取所有群组失败：" + e.getMessage());
            throw new SqlException(e.getMessage());
        }
        for (Group c : groups) {
            c.addMember(getRelatedChains(c.getGroupName()));
        }
        return groups;
    }

    public List<Chain> getChains(String chainNames) throws UniException {
        String stamt = String.format("select * from chain where chain_name in (%s)", chainNames);
        List<Chain> r;
        try {
            r = sql.query(stamt, Mappers.chainRowMapper);

        } catch (Exception e) {
            logger.error("获取链{}失败：" + e.getMessage(), chainNames);
            throw new SqlException(e.getMessage());
        }
        logger.debug("[query chains]: {},{}", r.size(), Arrays.toString(r.toArray()));
        return r;
    }

    private List<Chain> getRelatedChains(String channel) throws UniException {
        List<Chain> list;
        try {
            list = sql.query("select chain_id,chain_name,chain_status,chain_type from (select t1.channel_name name,t3.*  from channel t1,channel_chain t2,chain t3 where t1.channel_id=t2.channel_id and t2.chain_id=t3.chain_id) t4 where name=?", Mappers.chainRowMapper, channel);
        } catch (DataAccessException e) {
            logger.error("获取{}相关链失败：" + e.getMessage(), channel);
            throw new SqlException(e.getMessage());
        }
        return list;
    }

    public int newGroup(Group group) throws UniException {
        int cnt = 0;
        try {
            cnt = sql.update("insert into channel values(?,?,?)", group.getGroupId(), group.getGroupName(), group.getStatus());
        } catch (Exception e) {
            logger.error("添加群组{}失败：" + e.getMessage(), group);
            throw new SqlException(e.getMessage());
        }
        return cnt;
    }

    /**
     * 新增通道时关联这条通道的所有链
     *
     * @param group
     */
    public void associate(Group group) throws UniException {
        try {
            for (Chain chain : group.getMembers()) {
                sql.update("insert into channel_chain values(?,?)", group.getGroupId(), chain.getChainId());
            }
        } catch (Exception e) {
            logger.error("关联群组{}失败：" + e.getMessage(), group);
            throw new SqlException(e.getMessage());
        }
    }

    public int addChain(Chain... chain) throws UniException {
        int cnt = 0;
        try {
            for (Chain c : chain) {
                cnt += sql.update("insert into chain values(?,?,?,?)", c.getChainId(), c.getChainName(), c.getStatus(), c.getChainType());
            }
        } catch (Exception e) {
            logger.error("添加链{}失败：" + e.getMessage(), chain);
            throw new SqlException(e.getMessage());
        }
        return cnt;
    }

    /**
     * 关联一条链
     *
     * @param channel_id 通道id
     * @param chain_id   链id
     */
    public int associate(String channel_id, String chain_id) throws UniException {
        int cnt = 0;
        try {
            cnt = sql.update("insert into channel_chain values(?,?)", channel_id, chain_id);
        } catch (Exception e) {
            logger.error("关联群组{}和链{}失败：" + e.getMessage(), channel_id, chain_id);
            throw new SqlException(e.getMessage());
        }
        return cnt;
    }

    public void deAssociate(String channel_id, String chain_id) throws UniException {
        try {
            sql.update("delete from channel_chain where channel_id=? and chain_id=?", channel_id, chain_id);
        } catch (DataAccessException e) {
            logger.error("去关联群组{},链{}失败：" + e.getMessage(), channel_id, chain_id);
            throw new SqlException(e.getMessage());
        }
    }

    public void updateGroup(String cnl_name, int status) throws UniException {
        try {
            sql.update("update channel set channel_status=? where channel_name=?", status, cnl_name);
        } catch (Exception e) {
            logger.error("更新群组{}失败：" + e.getMessage(), cnl_name);
            throw new SqlException(e.getMessage());
        }
    }

    public void updateChain(String c_name, int status) throws UniException {
        try {
            sql.update("update chain set chain_status=? where chain_name=?", status, c_name);
        } catch (DataAccessException e) {
            logger.error("更新链{}失败：" + e.getMessage(), c_name);
            throw new SqlException(e.getMessage());
        }
    }

    public Chain getChain(String cName) throws UniException {
        Chain chain;
        try {
            chain = sql.queryForObject("select * from chain where chain_name=?", Mappers.chainRowMapper, cName);
        } catch (DataAccessException e) {
            logger.error("获取链{}失败：" + e.getMessage(), cName);
            throw new SqlException(e.getMessage());
        }
        return chain;
    }
}