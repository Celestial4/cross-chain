package com.crosschain.auth;

import com.crosschain.common.Mappers;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthSource {

    @Resource
    private JdbcTemplate sql;

    public List<AuthEntity> getAllUser() {
        return sql.query("select * from user", Mappers.authEntityRowMapper);
    }
}