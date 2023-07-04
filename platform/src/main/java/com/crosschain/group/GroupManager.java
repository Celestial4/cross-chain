package com.crosschain.group;

import com.crosschain.common.entity.Chain;
import com.crosschain.common.entity.Group;
import com.crosschain.datasource.GroupAndChainSource;
import com.crosschain.exception.OperationException;
import com.crosschain.exception.SqlException;
import com.crosschain.exception.UniException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@Slf4j
public class GroupManager {

    private final Map<String, Group> groups = new HashMap<>();

    public void setDs(GroupAndChainSource ds) {
        this.ds = ds;
    }

    private GroupAndChainSource ds;

    public void init() {
        List<Group> groups;
        try {
            groups = ds.getAllGroups();
        } catch (UniException e) {
            log.error("初始化群组管理器失败：" + e.getErrorMsg());
            return;
        }
        for (Group c : groups) {
            this.groups.put(c.getGroupName(), c);
        }
    }

    public Group getGroup(String groupName) throws Exception {
        return ds.getGroup(groupName);
    }

    public List<Chain> getChains(String[] chainNames) throws UniException {
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

        log.info("创建群组{}", grpName);
    }

    public void putChain(String chainName, int status) throws UniException {
        String chain_id = UUID.randomUUID().toString();
        Chain chain = new Chain();
        chain.setChainId(chain_id);
        chain.setChainName(chainName);
        chain.setStatus(status);
        ds.addChain(chain);

        log.info("创建链{}成功", chainName);
    }

    public void updateStatus(int type, String target, int status) throws UniException {

        switch (type) {
            case 1:
                ds.updateGroup(target, status);
                break;
            case 2:
                ds.updateChain(target, status);
                break;
        }
        log.info("更新{}c成功", target);
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