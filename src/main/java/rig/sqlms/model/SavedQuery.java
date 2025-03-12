package rig.sqlms.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public record SavedQuery(String query, String dataSourceName) {
    public SavedQuery {
        Objects.requireNonNull(query);
        Objects.requireNonNull(dataSourceName);
    }

    public static SavedQuery of(String absolutePath, String project) throws IOException {
        return new SavedQuery(getContents(absolutePath), getDatabaseName(project));
    }

    private static String getContents(String absolutePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(absolutePath));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static String getDatabaseName(String project) {
        return "byk"; // TODO: change this to project if multiple datasources are needed;
    }

    private static int nthLastIndexOf(int nth, String character, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, character, string.substring(0, string.lastIndexOf(character)));
    }
}
