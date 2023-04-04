package com.crosschain.group;

import com.crosschain.common.Chain;
import com.crosschain.common.Group;
import com.crosschain.common.Loggers;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@Slf4j
public class GroupManager {

    private final Map<String, Group> groups = new HashMap<>();

    public void setDs(GroupSource ds) {
        this.ds = ds;
    }

    private GroupSource ds;

    public void init() {
        List<Group> groups = ds.getAllGroups();
        for (Group c : groups) {
            this.groups.put(c.getGroupName(), c);
            log.debug("[group found]: {},{}",c.getGroupName(),c.getMembers());
        }
    }

    public Group getGroup(String groupName)throws Exception {
        return ds.getGroup(groupName);
    }

    public List<Chain> getChains(String[] chainNames) {
        String allChain = "";
        if (!Objects.isNull(chainNames) && chainNames.length > 0) {
            for (String name : chainNames) {
                allChain += String.format("\"%s\",", name);
            }
            allChain = allChain.substring(0, allChain.length() - 1);
            return ds.getChains(allChain);
        }
        return Collections.emptyList();
    }

    public int putGroup(String groupName, int status, String... chains) {
        int cnt = 0;
        List<Chain> allChain = getChains(chains);
        if (groups.containsKey(groupName)) {
            //add to existed group
            Group existedGroup = groups.get(groupName);
            if (!allChain.isEmpty()) {
                //update cache
                existedGroup.addMember(allChain);
                for (Chain chain : allChain) {
                    cnt = ds.associate(existedGroup.getGroupId(), chain.getChainId());
                }
                log.info("add to group:[{}], associated chains:[{}]", groupName, Arrays.toString(chains));
                if (allChain.size() != chains.length) {
                    log.info("add to group, but some of chains not found");
                }
            } else {
                log.info("add to group, but chains:[{}] not found", (Object) chains);
            }
        } else {
            Group group = new Group();
            group.setGroupId(UUID.randomUUID().toString());
            group.setGroupName(groupName);
            group.setStatus(status);

            if (!allChain.isEmpty()) {
                group.addMember(allChain);
            }

            groups.put(group.getGroupName(), group);
            cnt = ds.newGroup(group);
            ds.associate(group);
            log.info(Loggers.LOGFORMAT, String.format("create group:[%s], associated chains:[%s]", groupName, Arrays.toString(chains)));
        }

        return cnt;
    }

    public int putChain(String chainName, int status) {
        String chain_id = UUID.randomUUID().toString();
        Chain chain = new Chain();
        chain.setChainId(chain_id);
        chain.setChainName(chainName);
        chain.setStatus(status);

        log.info("create chain:[{}]", chainName);
        return ds.addChain(chain);
    }

    public int updateStatus(int type, String target, int status) {
        try {
            switch (type) {
                case 1:
                    ds.updateGroup(target, status);
                    break;
                case 2:
                    ds.updateChain(target, status);
                    break;
            }
        } catch (Exception e) {
            log.error("update [{}]", target);
            return 1;
        }
        log.info(Loggers.LOGFORMAT, "update success!");
        return 0;
    }

    public int removeTo(String srcGrpName, String desGrpName, String cName) {
        if (Strings.isEmpty(desGrpName) && Strings.isNotEmpty(srcGrpName)) {
            //remove from srcGroup
            if (groups.containsKey(srcGrpName)) {
                Group src_cnl = groups.get(srcGrpName);
                Chain srcChain = src_cnl.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String src_cnl_id = src_cnl.getGroupId();
                    ds.deAssociate(src_cnl_id, srcChain.getChainId());
                    log.info("remove chain[{}] from [{}] successfully!", cName, srcGrpName);
                } else {
                    log.info("remove chain[{}] from [{}] failed!, chain not found", cName, srcGrpName);
                    return 0;
                }
            } else {
                log.info("remove chain[{}] from [{}] failed!, group not found", cName, srcGrpName);
                return 0;
            }
        } else if (Strings.isEmpty(srcGrpName) && Strings.isNotEmpty(desGrpName)) {
            //add to desGroup
            if (groups.containsKey(desGrpName)) {
                Group des_cnl = groups.get(desGrpName);
                Chain srcChain = ds.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String des_cnl_id = des_cnl.getGroupId();
                    ds.associate(des_cnl_id, srcChain.getChainId());
                    log.info("add chain[{}] to [{}] successfully!", cName, desGrpName);
                }
                log.info("add chain[{}] to [{}] failed! chain not found", cName, desGrpName);
                return 0;
            }
            log.info("add chain[{}] to [{}] failed! group not found", cName, desGrpName);
            return 0;

        } else if (Strings.isEmpty(desGrpName) && Strings.isEmpty(srcGrpName)) {
            log.info("operation failed! arguments error");
            return 0;
        }else{
            //remove from srcGroup and then add to desGroup
            if (groups.containsKey(srcGrpName) && groups.containsKey(desGrpName)) {
                Group src_cnl = groups.get(srcGrpName);
                Group des_cnl = groups.get(desGrpName);
                Chain srcChain = src_cnl.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String src_cnl_id = src_cnl.getGroupId();
                    String des_cnl_id = des_cnl.getGroupId();
                    ds.deAssociate(src_cnl_id, srcChain.getChainId());
                    ds.associate(des_cnl_id, srcChain.getChainId());
                    log.info("remove chain[{}] from [{}] to [{}] successfully!", cName, srcGrpName, desGrpName);
                }
                return 0;
            }
        }
        return 1;
    }
}