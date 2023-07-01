package com.crosschain.common.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Group {

    private String GroupId;
    private String GroupName;
    private Integer status;
    private List<Chain> members = new ArrayList();

    public int addMember(List<Chain> c) {
        members.addAll(c);
        return members.size();
    }

    public int removeMember(String name) {
        if (members.stream().anyMatch(chain -> chain.getChainName().equals(name))) {
            members.remove(name);
        }
        return members.size();
    }

    public Chain getChain(String name) {
        for (Chain next : members) {
            if (next.getChainName().equals(name)) {
                return next;
            }
        }
        return null;
    }
}