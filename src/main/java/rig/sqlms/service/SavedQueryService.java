package rig.sqlms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import rig.sqlms.exception.InvalidDirectoryException;
import rig.sqlms.exception.ResqlRuntimeException;
import rig.sqlms.model.SavedQuery;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SavedQueryService {

    private final String[] METHODS = {"GET", "POST"};

    private final Map<String, Map<String, Map<String, SavedQuery>>> savedQueries = new HashMap<>();

    private final static String PATHSPLITTER = "(/.+?){3}(/.+)\\..*";
    public final static Pattern  pathPattern = Pattern.compile(PATHSPLITTER);

    public SavedQueryService(@Value("${sqlms.saved-queries-dir}") String savedQueriesDir) {
        log.info("Initializing SavedQueryService");
        File dslRoot = getConfigDir(savedQueriesDir);
        for (File projectDir : dslRoot.listFiles((f) -> f.isDirectory())) {
            for (String method : METHODS) {
                String queriesPath = projectDir.getPath() + "/" + method + "/";
                log.info("Loading queries from " + queriesPath);
                loadQueries(projectDir.getName(), method,
                        getConfigDir(savedQueriesDir + "/"
                                + projectDir.getName() + "/"
                                + method + "/").listFiles());
                log.info("Loaded queries: " +
                        savedQueries.get(projectDir.getName()).get(method).entrySet().stream().map(entry-> entry.getKey()).collect(Collectors.joining(", ")));
            }
        }
    }

    private void loadQueries(String project, String method, File[] filesList) {
        if (!savedQueries.containsKey(project))
            savedQueries.put(project, new HashMap<>());
        if (!savedQueries.get(project).containsKey(method))
            savedQueries.get(project).put(method, new HashMap<>());
        try {
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isDirectory()) {
                        loadQueries(project, method, file.listFiles());
                    } else {
                        try {
                            savedQueries.get(project).get(method).put(getQueryName(file, project, method), SavedQuery.of(file.getAbsolutePath(), project));
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

    private String getQueryName(File file, String project, String method) {
        Matcher pathmatcher = pathPattern.matcher(file.getPath());
        pathmatcher.find();
        return pathmatcher.group(2);
        // return file.getPath().substring(project.length() + 1 + method.length(), file.getName().lastIndexOf(".")).toLowerCase();
    }

    @NonNull
    public SavedQuery get(String project, String method, String name) {
        SavedQuery query = savedQueries.get(project).get(method).get(name.trim().toLowerCase());
        
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

    public static <T> String mapDeepToString(Map<String, T> map) {
        return map == null ? "" : map.entrySet().stream()
                .map(e ->"{ " + e.getKey() + " => " + (e.getValue() instanceof Map ? mapDeepToString((Map)e.getValue()) : e.getValue().toString()) + " }")
                .collect(Collectors.joining(","));
    }

}
