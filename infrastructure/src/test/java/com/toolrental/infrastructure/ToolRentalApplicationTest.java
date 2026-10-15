package com.toolrental.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ToolRentalApplicationTest {
  @Autowired private ApplicationContext context;

  @Test
  void startsWithoutDatabaseOrWebServer() {
    assertTrue(context.getBeansOfType(DataSource.class).isEmpty());
    assertNotNull(context.getBean(ToolRentalApplication.class));
    assertFalse(context.getClass().getName().contains("WebServer"));
  }
}
