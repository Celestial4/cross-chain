package com.crosschain.service.response;

import lombok.Data;

@Data
public class SelfServiceResponse implements Response {
    String data;

    public SelfServiceResponse() {
    }

    public SelfServiceResponse(String data) {
        this.data = data;
    }

    @Override
    public String get() {
        return data;
    }
}