package rig.sqlms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import rig.sqlms.exception.InvalidDirectoryException;
import rig.sqlms.exception.ResqlRuntimeException;
import rig.sqlms.model.SavedQuery;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SavedQueryService {

    private final String[] METHODS = {"GET", "POST"};

    private final Map<String, Map<String, SavedQuery>> savedQueries = new HashMap<>();

    public SavedQueryService(@Value("${sqlms.saved-queries-dir}") String savedQueriesDir) {
        log.info("Initializing SavedQueryService");
        for (String method : METHODS) {
            String queriesPath = savedQueriesDir + method + "/";
            log.debug("Loading queries from "+ queriesPath);
            loadQueries(method, getConfigDir(savedQueriesDir + method + "/").listFiles());
        }
    }

    private void loadQueries(String method, File[] filesList) {
        if (!savedQueries.containsKey(method))
            savedQueries.put(method, new HashMap<>());
        try {
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isDirectory()) {
                        loadQueries(method, file.listFiles());
                    } else {
                        try {
                            savedQueries.get(method).put(getQueryName(file), SavedQuery.of(file.getAbsolutePath()));
                        } catch (Throwable t) {
                            log.error("Failed parsing saved query file {}", file.getName(), t);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed loading configuration service", e);
        }

        log.debug("Loaded queries: "+mapDeepToString(savedQueries));
    }

    private String getQueryName(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf(".")).toLowerCase();
    }

    @NonNull
    public SavedQuery get(String method, String name) {
        SavedQuery query = savedQueries.get(method).get(name.trim().toLowerCase());

        if (query == null) {
            throw new ResqlRuntimeException("Saved query '%s' does not exist".formatted(name));
        }

        return query;
    }

    private File getConfigDir(String path) {
        File configDir;
        if (path == null || path.isEmpty())
            throw new InvalidDirectoryException("Saved configuration directory seems to empty");

        configDir = new File(path);
        if (!configDir.exists() && !configDir.isDirectory())
            throw new InvalidDirectoryException("Saved configuration directory missing or not a directory: " + path);

        return configDir;
    }

    public static String mapDeepToString(Map<String, Map<String, SavedQuery>> map) {
        return map.entrySet().stream()
                .map(method -> "{" + method.getKey() +
                        method.getValue().entrySet().stream().map(path -> path.getKey() + "=>" + path.getValue())
                                .collect(Collectors.joining(",")) + "}")
                .collect(Collectors.joining(","));
    }

}
