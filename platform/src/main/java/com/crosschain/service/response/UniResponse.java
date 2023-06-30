package com.crosschain.service.response;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniResponse implements Response {

    protected Integer code;
    protected String message;
    protected String data;

    @Override
    public String get() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}