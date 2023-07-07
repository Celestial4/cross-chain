package com.crosschain.service.response.entity;

import com.crosschain.service.response.UniResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SelfServiceResponse extends UniResponse {

    public SelfServiceResponse(String data) {
        super.data = data;
    }

    @Override
    public String get() {
        code = 200;
        message = "success";
        return super.get();
    }
}