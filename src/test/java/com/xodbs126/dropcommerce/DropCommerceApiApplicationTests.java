package com.xodbs126.dropcommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Import(TestcontainersConfig.class)
class DropCommerceApiApplicationTests extends IntegrationTest{

	@Test
	void contextLoads() {
	}

}
