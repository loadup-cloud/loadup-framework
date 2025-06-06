package com.github.loadup.components.testify.tests;

import com.github.loadup.components.testify.annotation.DbCheck;
import com.github.loadup.components.testify.annotation.DbProcess;
import com.github.loadup.components.testify.listener.BaseTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class UserManagementTest extends BaseTest {

    @Test
    @DbProcess(init = "sql/init_user.sql", clear = "sql/clear_user.sql")
    @DbCheck(value = "sql/check_users.yaml", after = true)
    public void testUserExistsAfterInsert() {
        JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM upms_user WHERE id = ?", Integer.class, "1234567898789");

        Assert.assertEquals(count, Integer.valueOf(1), "用户应该存在");
    }
}
