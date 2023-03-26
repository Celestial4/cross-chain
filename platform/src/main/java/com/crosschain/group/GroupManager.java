package com.crosschain.group;

import com.crosschain.common.Chain;
import com.crosschain.common.Channel;
import com.crosschain.common.Loggers;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@Slf4j
public class GroupManager {

    private final Map<String, Channel> channels = new HashMap<>();

    public void setDs(GroupSource ds) {
        this.ds = ds;
    }

    private GroupSource ds;

    public void init() {
        List<Channel> channels = ds.getAllGroups();
        for (Channel c : channels) {
            this.channels.put(c.getChannelName(), c);
        }
        log.info(Loggers.LOGFORMAT, "channelManager initialized!");
    }

    public Channel getChannel(String channelName) {
        Channel ret = null;
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
            Channel existedChannel = channels.get(channelName);
            if (!allChain.isEmpty()) {


                //update cache
                existedChannel.addMember(allChain);
                for (Chain chain : allChain) {
                    cnt = ds.associate(existedChannel.getChannelId(),chain.getChainId());
                }
                log.info(Loggers.LOGFORMAT, String.format("add to channel:[%s], associated chains:[%s]", channelName, Arrays.toString(chains)));
            }
        } else {
            Channel channel = new Channel();
            channel.setChannelId(UUID.randomUUID().toString());
            channel.setChannelName(channelName);
            channel.setStatus(status);

            if (!allChain.isEmpty()) {
                channel.addMember(allChain);
            }

            channels.put(channel.getChannelName(), channel);
            cnt = ds.newGroup(channel);
            ds.associate(channel);
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
            Channel src_cnl = channels.get(srcCnlName);
            Chain srcChain = src_cnl.getChain(cName);
            if (Objects.nonNull(srcChain)) {
                String src_cnl_id = src_cnl.getChannelId();
                ds.deAssociate(src_cnl_id, srcChain.getChainId());
            }
            log.info(Loggers.LOGFORMAT, "remove chain[%s] from [%s] successfully!", cName, srcCnlName);
            return 0;
        }
        if (channels.containsKey(srcCnlName) && channels.containsKey(desCnlName)) {
            Channel src_cnl = channels.get(srcCnlName);
            Channel des_cnl = channels.get(desCnlName);
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