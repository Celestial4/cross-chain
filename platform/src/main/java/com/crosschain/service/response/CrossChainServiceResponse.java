package com.crosschain.service.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CrossChainServiceResponse extends UniResponse {

    String desResult;
    String srcResult;

    @Override
    public String get() {
        code = 200;
        message = "success";
        data = String.format("[desChainResult]:\n%s\n[srcChainResult]:\n%s\n", desResult, srcResult);
        return super.get();
    }
}