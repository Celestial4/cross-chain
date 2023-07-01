package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HTLCMechanismInfo {
    String htlc_lock;
    String htlc_unlock;
    String htlc_status;
}