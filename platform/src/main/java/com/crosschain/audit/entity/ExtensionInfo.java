package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionInfo {
    String ret = "";
    String vmType = "";
    Integer gasUsed = 0;
    Integer code = 0;
    ArrayList<String> log = new ArrayList<>();
    String retMessage = "";
    String contractAddress = "";
    Integer state = 0;
    String message = "";
    String jsonrpc = "";
    String txHash = "";
    String version = "";
}