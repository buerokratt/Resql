package rig.sqlms.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rig.sqlms.exception.InvalidDirectoryException;
import rig.sqlms.exception.ResqlRuntimeException;
import rig.sqlms.model.SavedQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SavedQueryServiceTest {

    // getQueryName() derives a query's lookup key by stripping exactly "<root>/<project>/<method>"
    // off the front of the file's path and keeping the rest (including its leading "/"), e.g. a
    // single-segment root like production's "/DSL" turns ".../crm/GET/get-users.sql" into the key
    // "/get-users". A multi-segment root breaks that arithmetic (it starts stripping from the wrong
    // place), so these tests use their own single-segment relative root instead of JUnit's @TempDir,
    // which nests several directories deep.
    private Path root;

    @BeforeEach
    void setUp() throws IOException {
        root = Path.of("./resql-test-" + UUID.randomUUID());
        Files.createDirectories(root);
    }

    @AfterEach
    void tearDown() throws IOException {
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
        }
    }

    // Every project directory needs both a GET/ and a POST/ subdirectory -- even if empty -- or the
    // constructor throws InvalidDirectoryException (see constructor_shouldThrowWhenAProjectIsMissingAMethodDirectory).
    private void writeQuery(String project, String method, String fileName, String contents) throws IOException {
        Path dir = root.resolve(project).resolve(method);
        Path file = dir.resolve(fileName);
        Files.createDirectories(file.getParent());
        Files.writeString(file, contents);
        Files.createDirectories(root.resolve(project).resolve(method.equals("GET") ? "POST" : "GET"));
    }

    @Test
    void get_shouldReturnQueryLoadedFromDisk() throws IOException {
        writeQuery("crm", "GET", "get-users.sql", "select * from users");

        SavedQueryService service = new SavedQueryService(root.toString());
        SavedQuery query = service.get("crm", "GET", "/get-users");

        assertEquals("select * from users", query.query());
        assertEquals("byk", query.dataSourceName());
    }

    @Test
    void get_shouldBeCaseInsensitiveAndTrimTheQueryName() throws IOException {
        writeQuery("crm", "GET", "get-users.sql", "select * from users");

        SavedQueryService service = new SavedQueryService(root.toString());

        assertEquals("select * from users", service.get("crm", "GET", "  /Get-Users  ").query());
    }

    @Test
    void get_shouldThrowWhenQueryDoesNotExist() throws IOException {
        writeQuery("crm", "GET", "get-users.sql", "select * from users");
        SavedQueryService service = new SavedQueryService(root.toString());

        ResqlRuntimeException exception = assertThrows(ResqlRuntimeException.class,
                () -> service.get("crm", "GET", "/unknown-query"));

        assertEquals("Saved query '/unknown-query' does not exist", exception.getMessage());
    }

    @Test
    void get_shouldLoadQueriesFromNestedSubdirectories() throws IOException {
        writeQuery("crm", "GET", "nested/get-users.sql", "select * from users");

        SavedQueryService service = new SavedQueryService(root.toString());

        assertEquals("select * from users", service.get("crm", "GET", "/nested/get-users").query());
    }

    @Test
    void constructor_shouldThrowWhenAProjectIsMissingAMethodDirectory() throws IOException {
        Files.createDirectories(root.resolve("crm").resolve("GET"));
        // no POST/ subdirectory created for "crm"

        assertThrows(InvalidDirectoryException.class, () -> new SavedQueryService(root.toString()));
    }

    @Test
    void constructor_shouldThrowWhenSavedQueriesDirIsBlank() {
        assertThrows(InvalidDirectoryException.class, () -> new SavedQueryService(""));
    }

    @Test
    void constructor_shouldThrowWhenSavedQueriesDirDoesNotExist(@TempDir Path tempDir) {
        assertThrows(InvalidDirectoryException.class,
                () -> new SavedQueryService(tempDir.resolve("does-not-exist").toString()));
    }
}
