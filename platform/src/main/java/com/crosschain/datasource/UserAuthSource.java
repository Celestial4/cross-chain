package com.crosschain.datasource;

import com.crosschain.auth.AuthEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserAuthSource {

    @Resource
    private JdbcTemplate sql;

    public List<AuthEntity> getAllUser() {
        return sql.query("select * from user", Mappers.authEntityRowMapper);
    }

    public AuthEntity getUser(String userName) {
        return sql.queryForObject("select * from user where name=?", Mappers.authEntityRowMapper, userName);
    }
}