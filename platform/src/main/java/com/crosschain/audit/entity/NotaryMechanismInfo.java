package com.crosschain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaryMechanismInfo implements Mechanism{
    String na_id;
    String na_choice;
    String ns_ip;
}