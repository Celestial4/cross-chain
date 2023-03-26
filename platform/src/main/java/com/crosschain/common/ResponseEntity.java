package com.crosschain.common;

import lombok.Data;

@Data
public class ResponseEntity {

    String desResult;
    String srcResult;

    String errorMsg="";

    public ResponseEntity() {
    }

    public ResponseEntity(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}