package rig.sqlms.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rig.sqlms.datasource.DataSourceContextHolder;
import rig.sqlms.datasource.ResqlJdbcTemplate;
import rig.sqlms.exception.UnknownDataSourceNameException;
import rig.sqlms.model.SavedQuery;
import rig.sqlms.properties.DataSourceConfigProperties;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock
    private SavedQueryService savedQueryService;
    @Mock
    private ResqlJdbcTemplate resqlJdbcTemplate;

    private QueryService queryService;

    @BeforeEach
    void setUp() {
        List<DataSourceConfigProperties> configProperties = List.of(configFor("crm"), configFor("debt"));
        queryService = new QueryService(configProperties, savedQueryService, resqlJdbcTemplate);
    }

    @AfterEach
    void clearContext() {
        DataSourceContextHolder.clearDataSourceName();
    }

    private static DataSourceConfigProperties configFor(String name) {
        DataSourceConfigProperties properties = new DataSourceConfigProperties();
        properties.setName(name);
        return properties;
    }

    @Test
    void execute_shouldSetDataSourceContextAndDelegateToJdbcTemplate() {
        SavedQuery savedQuery = new SavedQuery("select 1", "crm");
        Map<String, Object> parameters = Map.of("id", 1);
        List<Map<String, Object>> expectedResult = List.of(Map.of("id", 1));

        when(savedQueryService.get("crm", "GET", "get-users")).thenReturn(savedQuery);
        when(resqlJdbcTemplate.queryOrExecute("select 1", parameters)).thenReturn(expectedResult);

        List<Map<String, Object>> result = queryService.execute("crm", "GET", "get-users", parameters);

        assertEquals(expectedResult, result);
        assertEquals("crm", DataSourceContextHolder.getDataSourceName());
        verify(resqlJdbcTemplate).queryOrExecute("select 1", parameters);
    }

    @Test
    void execute_shouldThrowWhenSavedQueryReferencesUnconfiguredDataSource() {
        when(savedQueryService.get("crm", "GET", "get-users"))
                .thenReturn(new SavedQuery("select 1", "unknown-datasource"));

        UnknownDataSourceNameException exception = assertThrows(UnknownDataSourceNameException.class,
                () -> queryService.execute("crm", "GET", "get-users", Map.of()));

        assertEquals("Specified dataSourceName name: 'unknown-datasource' is unknown to the service",
                exception.getMessage());
    }

    // executePost()/executeGet() go through execute(method, queryName, parameters), which splits
    // queryName on "/" with a limit of 1. String.split(regex, 1) never splits (limit <= 0 elements
    // beyond the first), so projectQuery[1] always throws. This is how QueryController#executeBatch
    // calls in, with a queryName that is a single path segment (no "/" at all) -- e.g. the
    // /{name}/batch endpoint -- so today this path always blows up before savedQueryService is even
    // consulted. Pinning down the current (broken) behavior here so a fix doesn't go unnoticed.
    @Test
    void executePost_currentlyThrowsBecauseOfSplitLimitOfOne() {
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> queryService.executePost("get-user-email-by-login", Map.of()));
    }
}
