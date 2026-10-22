package com.xodbs126.dropcommerce;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Import(TestcontainersConfig.class)
@SpringBootTest
public abstract class IntegrationTest {

    @Autowired
    private DataSource dataSource;

    private static final List<String> TABLES = List.of("app_user", "store_application", "store", "product", "product_drop", "purchase_order");

    @BeforeEach
    public void cleanUp() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Statement st = con.createStatement();

            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            try{
                for (String table : TABLES) {
                    st.execute("TRUNCATE TABLE " + table);
                }
            }finally {
                st.execute("SET FOREIGN_KEY_CHECKS  = 1");
            }
        }

    }
}
