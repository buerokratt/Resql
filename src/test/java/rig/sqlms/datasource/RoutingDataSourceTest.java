package rig.sqlms.datasource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoutingDataSourceTest {

    private final RoutingDataSource routingDataSource = new RoutingDataSource();

    @AfterEach
    void clear() {
        DataSourceContextHolder.clearDataSourceName();
    }

    @Test
    void determineCurrentLookupKey_shouldReflectTheCurrentDataSourceContext() {
        DataSourceContextHolder.setDataSourceName("debt");

        assertEquals("debt", routingDataSource.determineCurrentLookupKey());
    }

    @Test
    void determineCurrentLookupKey_shouldBeNullWhenNoContextIsSet() {
        assertNull(routingDataSource.determineCurrentLookupKey());
    }
}
