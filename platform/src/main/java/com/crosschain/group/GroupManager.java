package com.crosschain.group;

import com.crosschain.common.Chain;
import com.crosschain.common.Group;
import com.crosschain.common.Loggers;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@Slf4j
public class GroupManager {

    private final Map<String, Group> channels = new HashMap<>();

    public void setDs(GroupSource ds) {
        this.ds = ds;
    }

    private GroupSource ds;

    public void init() {
        List<Group> groups = ds.getAllGroups();
        for (Group c : groups) {
            this.channels.put(c.getChannelName(), c);
        }
        log.info(Loggers.LOGFORMAT, "channelManager initialized!");
    }

    public Group getChannel(String channelName) {
        Group ret = null;
        if (channels.containsKey(channelName)) {
            ret = channels.get(channelName);
        } else {
            ret = ds.getGroup(channelName);
        }

        return ret;
    }

    public List<Chain> getChains(String[] chainNames) {
        String allChain = "";
        if (!Objects.isNull(chainNames) && chainNames.length > 0) {
            for (String name : chainNames) {
                allChain += String.format("%s,", name);
            }
            allChain = allChain.substring(0, allChain.length() - 1);
            return ds.getChains(allChain);
        }
        return Collections.emptyList();
    }

    public int putChannel(String channelName, int status, String... chains) {
        int cnt = 0;
        List<Chain> allChain = getChains(chains);
        if (channels.containsKey(channelName)) {
            //add to existed channel
            Group existedGroup = channels.get(channelName);
            if (!allChain.isEmpty()) {


                //update cache
                existedGroup.addMember(allChain);
                for (Chain chain : allChain) {
                    cnt = ds.associate(existedGroup.getChannelId(),chain.getChainId());
                }
                log.info(Loggers.LOGFORMAT, String.format("add to channel:[%s], associated chains:[%s]", channelName, Arrays.toString(chains)));
            }
        } else {
            Group group = new Group();
            group.setChannelId(UUID.randomUUID().toString());
            group.setChannelName(channelName);
            group.setStatus(status);

            if (!allChain.isEmpty()) {
                group.addMember(allChain);
            }

            channels.put(group.getChannelName(), group);
            cnt = ds.newGroup(group);
            ds.associate(group);
            log.info(Loggers.LOGFORMAT, String.format("create channel:[%s], associated chains:[%s]", channelName, Arrays.toString(chains)));
        }

        return cnt;
    }

    public int putChain(String chainName, int status) {
        String chain_id = UUID.randomUUID().toString();
        Chain chain = new Chain();
        chain.setChainId(chain_id);
        chain.setChainName(chainName);
        chain.setStatus(status);

        log.info(Loggers.LOGFORMAT, "create chain:[%s]", chainName);
        return ds.addChain(chain);
    }

    public int updateStatus(int type, String target, int status) {
        try {
            switch (type) {
                case 1:
                    ds.updateChannel(target, status);
                    break;
                case 2:
                    ds.updateChain(target, status);
                    break;
            }
        } catch (Exception e) {
            log.error(Loggers.LOGFORMAT, "update [%s]", target);
            return 1;
        }
        log.info(Loggers.LOGFORMAT, "update success!");
        return 0;
    }

    public int removeTo(String srcCnlName, String desCnlName, String cName) {
        if (Strings.isEmpty(desCnlName) && channels.containsKey(srcCnlName)) {
            Group src_cnl = channels.get(srcCnlName);
            Chain srcChain = src_cnl.getChain(cName);
            if (Objects.nonNull(srcChain)) {
                String src_cnl_id = src_cnl.getChannelId();
                ds.deAssociate(src_cnl_id, srcChain.getChainId());
            }
            log.info(Loggers.LOGFORMAT, "remove chain[%s] from [%s] successfully!", cName, srcCnlName);
            return 0;
        }
        if (channels.containsKey(srcCnlName) && channels.containsKey(desCnlName)) {
            Group src_cnl = channels.get(srcCnlName);
            Group des_cnl = channels.get(desCnlName);
            Chain srcChain = src_cnl.getChain(cName);
            if (Objects.nonNull(srcChain)) {
                String src_cnl_id = src_cnl.getChannelId();
                String des_cnl_id = des_cnl.getChannelId();
                ds.deAssociate(src_cnl_id, srcChain.getChainId());
                ds.associate(des_cnl_id, srcChain.getChainId());
            }
            log.info(Loggers.LOGFORMAT, "remove chain[%s] from [%s] to [%s] successfully!", cName, srcCnlName, desCnlName);
            return 0;
        }
        log.error(Loggers.LOGFORMAT, "remove chain[%s] from [%s] to [%s] failed!", cName, srcCnlName, desCnlName);
        return 1;
    }
}