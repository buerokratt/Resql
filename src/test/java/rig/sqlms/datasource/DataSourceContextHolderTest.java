package rig.sqlms.datasource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSourceContextHolderTest {

    @AfterEach
    void clear() {
        DataSourceContextHolder.clearDataSourceName();
    }

    @Test
    void setAndGet_shouldRoundTrip() {
        DataSourceContextHolder.setDataSourceName("crm");

        assertEquals("crm", DataSourceContextHolder.getDataSourceName());
    }

    @Test
    void get_shouldReturnNullWhenNothingIsSet() {
        assertNull(DataSourceContextHolder.getDataSourceName());
    }

    @Test
    void clear_shouldRemoveThePreviouslySetName() {
        DataSourceContextHolder.setDataSourceName("crm");
        DataSourceContextHolder.clearDataSourceName();

        assertNull(DataSourceContextHolder.getDataSourceName());
    }

    @Test
    void set_shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> DataSourceContextHolder.setDataSourceName(null));
    }
}
