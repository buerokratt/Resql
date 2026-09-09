package rig.sqlms.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SavedQueryTest {

    @Test
    void constructor_shouldRejectNullQuery() {
        assertThrows(NullPointerException.class, () -> new SavedQuery(null, "byk"));
    }

    @Test
    void constructor_shouldRejectNullDataSourceName() {
        assertThrows(NullPointerException.class, () -> new SavedQuery("select 1", null));
    }

    @Test
    void of_shouldReadTheFileContentsAsTheQuery(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("get-users.sql");
        Files.writeString(file, "select * from users");

        SavedQuery savedQuery = SavedQuery.of(file.toAbsolutePath().toString(), "crm");

        assertEquals("select * from users", savedQuery.query());
        // TODO in SavedQuery.getDatabaseName(): always "byk" regardless of project, until
        // multiple datasources per project are supported.
        assertEquals("byk", savedQuery.dataSourceName());
    }
}
