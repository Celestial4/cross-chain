package com.crosschain.service.response;

import lombok.Data;

@Data
public class SelfServiceResponse extends UniResponse {
    String data;

    public SelfServiceResponse() {
    }

    public SelfServiceResponse(String data) {
        this.data = data;
    }

    @Override
    public String get() {
        code = 200;
        message = "success";
        super.data = data;
        return super.get();
    }
}