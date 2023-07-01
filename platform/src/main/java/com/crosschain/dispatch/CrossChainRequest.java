package com.crosschain.dispatch;

import com.crosschain.common.entity.CommonChainRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossChainRequest{
    private CommonChainRequest srcChainRequest;
    private CommonChainRequest desChainRequest;
    private String group;
    private String requestId;
}