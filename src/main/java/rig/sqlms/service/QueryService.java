package rig.sqlms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rig.sqlms.datasource.DataSourceContextHolder;
import rig.sqlms.datasource.ResqlJdbcTemplate;
import rig.sqlms.exception.UnknownDataSourceNameException;
import rig.sqlms.model.SavedQuery;
import rig.sqlms.properties.DataSourceConfigProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    private final List<DataSourceConfigProperties> configProperties;
    private final SavedQueryService savedQueryService;
    private final ResqlJdbcTemplate resqlJdbcTemplate;

    private List<Map<String, Object>> execute(String method, String queryName, Map<String, Object> parameters) {
        String project = queryName.substring(0, queryName.indexOf("/"));
        SavedQuery savedQuery = savedQueryService.get(project,method, queryName);
        setDatabaseContext(savedQuery.dataSourceName());
        return resqlJdbcTemplate.queryOrExecute(savedQuery.query(), parameters);
    }

    public List<Map<String, Object>> executePost(String queryName, Map<String, Object> parameters) {
        return execute("POST", queryName, parameters);
    }

    public List<Map<String, Object>> executeGet(String queryName, Map<String, Object> parameters) {
        return execute("GET", queryName, parameters);
    }

    private void setDatabaseContext(String dataSourceName) {
        configProperties.stream()
                .map(DataSourceConfigProperties::getName)
                .filter(propertyDataSourceName -> Objects.equals(dataSourceName, propertyDataSourceName))
                .findFirst()
                .ifPresentOrElse(DataSourceContextHolder::setDataSourceName,
                        () -> {
                            throw new UnknownDataSourceNameException(dataSourceName);
                        }
                );
    }
}
