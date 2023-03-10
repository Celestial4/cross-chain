package com.crosschain.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Channel {

    private String ChannelId;
    private String ChannelName;
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