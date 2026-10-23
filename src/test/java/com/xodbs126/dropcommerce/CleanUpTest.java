package com.xodbs126.dropcommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class CleanUpTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cleanUpTableA() {
        assertStartsEmptyAndIdResets();
    }

    @Test
    void cleanUpTableB() {
        assertStartsEmptyAndIdResets();
    }

    private void assertStartsEmptyAndIdResets() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user", Integer.class);
        assertThat(count).isZero();

        jdbcTemplate.update(
                "INSERT INTO app_user (username, password) VALUES (?, ?)",
                "test1",
                "test2"
        );

        Long id = jdbcTemplate.queryForObject("SELECT id FROM app_user", Long.class);
        assertThat(id).isEqualTo(1L);
    }
}