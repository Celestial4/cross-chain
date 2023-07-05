package com.crosschain.datasource;

import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import com.crosschain.exception.SqlException;
import com.crosschain.exception.UniException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
            String ErrorMsg = String.format("群组%s不存在：%s", channelName, e.getMessage());
            logger.error(ErrorMsg);
            throw new SqlException(ErrorMsg);
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
            throw new SqlException("获取所有群组失败：" + e.getMessage());
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
            logger.error("获取链{}失败：{}", chainNames, e.getMessage());
            throw new SqlException(e.getMessage());
        }
        return r;
    }

    private List<Chain> getRelatedChains(String channel) throws UniException {
        List<Chain> list;
        try {
            list = sql.query("select chain_id,chain_name,chain_status,chain_type from (select t1.channel_name name,t3.*  from channel t1,channel_chain t2,chain t3 where t1.channel_id=t2.channel_id and t2.chain_id=t3.chain_id) t4 where name=?", Mappers.chainRowMapper, channel);
        } catch (DataAccessException e) {
            logger.error("获取{}相关链失败：{}", channel, e.getMessage());
            throw new SqlException(e.getMessage());
        }
        return list;
    }

    public void newGroup(Group group) throws UniException {
        try {
            sql.update("insert into channel values(?,?,?)", group.getGroupId(), group.getGroupName(), group.getStatus());
        } catch (Exception e) {
            String msg = String.format("添加群组%s失败：%s", group, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
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
            logger.error("关联群组{}失败：{}", group, e.getMessage());
            throw new SqlException(e.getMessage());
        }
    }

    public void addChain(List<Chain> chain) throws UniException {
        try {
            for (Chain c : chain) {
                sql.update("insert into chain values(?,?,?,?)", c.getChainId(), c.getChainName(), c.getStatus(), c.getChainType());
            }
        } catch (Exception e) {
            String msg = String.format("添加链%s失败：%s", chain, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
    }

    /**
     * 关联一条链
     *
     * @param channel_id 通道id
     * @param chain_id   链id
     */
    public void associate(String channel_id, String chain_id) throws UniException {
        try {
            sql.update("insert into channel_chain values(?,?)", channel_id, chain_id);
        } catch (Exception e) {
            String msg = String.format("添加链%s到群组%s失败: %s", channel_id, chain_id, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
    }

    public void deAssociate(String channel_id, String chain_id) throws UniException {
        try {
            sql.update("delete from channel_chain where channel_id=? and chain_id=?", channel_id, chain_id);
        } catch (DataAccessException e) {
            String msg = String.format("群组%s移除链%s失败: %s", channel_id, chain_id, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
    }

    public void updateGroup(String cnl_name, int status) throws UniException {
        try {
            sql.update("update channel set channel_status=? where channel_name=?", status, cnl_name);
        } catch (Exception e) {
            String msg = String.format("更新群组%s失败：%s", cnl_name, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
    }

    public void updateChain(String c_name, int status) throws UniException {
        try {
            sql.update("update chain set chain_status=? where chain_name=?", status, c_name);
        } catch (Exception e) {
            String msg = String.format("更新链%s失败：%s", c_name, e.getMessage());
            logger.error(msg);
            throw new SqlException(msg);
        }
    }

    public Chain getChain(String cName) throws UniException {
        Chain chain;
        try {
            chain = sql.queryForObject("select * from chain where chain_name=?", Mappers.chainRowMapper, cName);
        } catch (Exception e) {
            String ErrorMsg = String.format("链%s不存在：%s", cName, e.getMessage());
            logger.error(ErrorMsg);
            throw new SqlException(ErrorMsg);
        }
        return chain;
    }
}