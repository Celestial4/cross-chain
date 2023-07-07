package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DPKMechanismInfo implements Mechanism {
    String dpky_id = "";
    String dpky_ip = "";
    String dpky = "";
}