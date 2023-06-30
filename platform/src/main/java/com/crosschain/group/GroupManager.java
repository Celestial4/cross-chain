package com.crosschain.group;

import com.crosschain.common.Chain;
import com.crosschain.common.Group;
import com.crosschain.common.Loggers;
import com.crosschain.exception.OperationException;
import com.crosschain.exception.UniException;
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
            log.debug("[group found]: {},{}", c.getGroupName(), c.getMembers());
        }
    }

    public Group getGroup(String groupName) throws Exception {
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

    public void putGroup0(String grpName, int status) throws UniException {
        Group group = new Group();
        group.setGroupId(UUID.randomUUID().toString());
        group.setGroupName(grpName);
        group.setStatus(status);

        groups.put(group.getGroupName(), group);
        ds.newGroup(group);

        log.info(Loggers.LOGFORMAT, String.format("create group:[%s]", grpName));
    }

    //解耦，deprecated
    public int putGroup(String groupName, int status, String... chains) throws UniException {
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
                log.info("add to group:{}, associated chains:{}", groupName, Arrays.toString(chains));
                if (allChain.size() != chains.length) {
                    log.info("add to group, but some of chains not found");
                }
            } else {
                log.info("add to group, but chains:{} not found", (Object) chains);
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

    public void putChain(String chainName, int status) throws UniException {
        String chain_id = UUID.randomUUID().toString();
        Chain chain = new Chain();
        chain.setChainId(chain_id);
        chain.setChainName(chainName);
        chain.setStatus(status);

        log.info("create chain:[{}]", chainName);
        ds.addChain(chain);
    }

    public void updateStatus(int type, String target, int status) throws UniException{

        switch (type) {
            case 1:
                ds.updateGroup(target, status);
                break;
            case 2:
                ds.updateChain(target, status);
                break;
        }
        log.info(Loggers.LOGFORMAT, "update success!");
    }

    public void removeTo(String srcGrpName, String desGrpName, String cName) throws UniException {
        if (Strings.isEmpty(desGrpName) && Strings.isEmpty(srcGrpName)) {
            log.info("operation failed! arguments error");
            throw new OperationException("缺少需要操作的群组参数，请查阅操作手册");
        }
        if (Strings.isEmpty(desGrpName)) {
            //remove from srcGroup
            if (groups.containsKey(srcGrpName)) {
                Group src_grp = groups.get(srcGrpName);
                Chain srcChain = src_grp.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String src_cnl_id = src_grp.getGroupId();
                    ds.deAssociate(src_cnl_id, srcChain.getChainId());
                    log.info("remove chain[{}] from [{}] successfully!", cName, srcGrpName);
                } else {
                    log.info("remove chain[{}] from [{}] failed!, chain not found", cName, srcGrpName);
                    throw new OperationException(String.format("%s链不在%s群组中", cName, srcGrpName));
                }
            } else {
                log.info("remove chain[{}] from [{}] failed!, group not found", cName, srcGrpName);
                throw new OperationException(String.format("%s群组不存在，请先添加群组", srcGrpName));
            }
        } else if (Strings.isEmpty(srcGrpName)) {
            //add to desGroup
            if (groups.containsKey(desGrpName)) {
                Group des_grp = groups.get(desGrpName);
                Chain srcChain = ds.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String des_grp_id = des_grp.getGroupId();
                    ds.associate(des_grp_id, srcChain.getChainId());
                    log.info("add chain[{}] to [{}] successfully!", cName, desGrpName);
                }
                log.info("add chain[{}] to [{}] failed! chain not found", cName, desGrpName);
                throw new OperationException(String.format("%s链不存在，请先添加链", cName));
            }
            log.info("add chain[{}] to [{}] failed! group not found", cName, desGrpName);
            throw new OperationException(String.format("%s群组不存在，请先添加群组", srcGrpName));
        } else {
            //remove from srcGroup and then add to desGroup
            if (groups.containsKey(srcGrpName) && groups.containsKey(desGrpName)) {
                Group src_grp = groups.get(srcGrpName);
                Group des_grp = groups.get(desGrpName);
                Chain srcChain = src_grp.getChain(cName);
                if (Objects.nonNull(srcChain)) {
                    String src_cnl_id = src_grp.getGroupId();
                    String des_cnl_id = des_grp.getGroupId();
                    ds.deAssociate(src_cnl_id, srcChain.getChainId());
                    ds.associate(des_cnl_id, srcChain.getChainId());
                    log.info("remove chain[{}] from [{}] to [{}] successfully!", cName, srcGrpName, desGrpName);
                }
                throw new OperationException(String.format("%s链不在%s群组中", cName, srcGrpName));
            }
            throw new OperationException(String.format("%s或者%s群组不存在，请先添加群组", srcGrpName, desGrpName));
        }
    }
}