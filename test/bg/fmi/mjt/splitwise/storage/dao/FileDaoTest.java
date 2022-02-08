package bg.fmi.mjt.splitwise.storage.dao;

import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.storage.dao.exceptions.DaoException;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FileDaoTest {

    private static final String DB_FOLDER_NAME = "tmp1";
    private static final Path PATH_TO_DB = Path.of(DB_FOLDER_NAME);

    private static MockedStatic<LoggerFactory> loggerMock = mockStatic(LoggerFactory.class);

    FileDao<IdentifiableStub> dao;
    Gson gson = new Gson();

    @BeforeAll
    static void setUp() throws IOException {
        loggerMock.when(() -> LoggerFactory.getLogger(ArgumentMatchers.any())).thenReturn(mock(Logger.class));

        try {
            Files.createDirectory(PATH_TO_DB);
        } catch (DirectoryNotEmptyException e) {
            deleteFiles();
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        loggerMock.close();
        Files.delete(PATH_TO_DB);
    }
    @BeforeEach
    void beforeEach() {
        dao = new FileDao<>(PATH_TO_DB, IdentifiableStub.class);
    }

    @AfterEach
    void afterEach() throws IOException {
        deleteFiles();
    }

    private static void deleteFiles() throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(PATH_TO_DB);

        for (var file : files) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void testInsert() {
        IdentifiableStub toInsert = new IdentifiableStub(22);

        dao.insert(toInsert);

        assertEquals(toInsert, dao.find(toInsert.id()), "Inserted object is not equal to the original");
    }

    @Test
    void testInsertSavesOnFile() throws IOException {
        IdentifiableStub toInsert = new IdentifiableStub(22);

        dao.insert(toInsert);

        String json = Files.readString(PATH_TO_DB.resolve(toInsert.id()));

        IdentifiableStub fromFile = gson.fromJson(json, IdentifiableStub.class);

        assertEquals(toInsert, fromFile, "Inserted object is not equal to the one saved in file");
    }

    @Test
    void testInsertThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> dao.insert(null), "Did not throw on null insert");
    }

    @Test
    void testDeleteThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> dao.delete(null), "Did not throw on null delete");
    }

    @Test
    void testDeleteThrowsOnNonExistentDeleteId() {
        assertThrows(DaoException.class, () -> dao.delete(new IdentifiableStub(33).id()), "Did not throw on nonexistent id delete");
    }

    @Test
    void testDelete() {
        IdentifiableStub toInsert = new IdentifiableStub(22);

        dao.insert(toInsert);

        dao.delete(toInsert.id());

        assertNull(dao.find(toInsert.id()), "Should not return deleted objects");
    }

    @Test
    void testDeleteRemovesFiles() {
        IdentifiableStub toInsert = new IdentifiableStub(22);

        dao.insert(toInsert);

        dao.delete(toInsert.id());

        assertFalse(Files.exists(PATH_TO_DB.resolve(toInsert.id())), "Deleting did not remove data from the file system");
    }

    @Test
    void testFindThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> dao.insert(null));
    }

    @Test
    void testFindReturnsActualObject() {
        IdentifiableStub toInsert = new IdentifiableStub(22);

        dao.insert(toInsert);

        IdentifiableStub actual = dao.find(toInsert.id());

        assertEquals(toInsert, actual, "Inserted object does not match returned one");
    }

    @Test
    void testFindAll() {
        var toInsert = List.of(
            new IdentifiableStub(1),
            new IdentifiableStub(2),
            new IdentifiableStub(3),
            new IdentifiableStub(4)
        );

        for (var i : toInsert) {
            dao.insert(i);
        }

        Map<String, IdentifiableStub> map = dao.findAll();

        for (int i = 0; i < 4; i++) {
            assertTrue(map.containsKey(toInsert.get(i).id()), "Returned keys do not contain the id from the object");
            assertTrue(map.containsValue(toInsert.get(i)), "Returned values do not contain the object that was inserted");
        }

        assertEquals(toInsert.size(), map.size(), "The size of the returned map do not match the expected one");
    }

    @Test
    void testUpdateThrowsOnNullObject() {
        assertThrows(NullPointerException.class, () -> dao.update(null));
    }

    @Test
    void testUpdateThrowsOnNonInsertedObject() {
        IdentifiableStub toUpdate = new IdentifiableStub(22);
        assertThrows(DaoException.class, () -> dao.update(toUpdate));
    }

    @Test
    void testUpdateWorks() {
        IdentifiableStub toInsert = new IdentifiableStub(22, "data1");
        dao.insert(toInsert);
        IdentifiableStub toUpdate = new IdentifiableStub(22, "data2");
        dao.update(toUpdate);

        assertEquals(toUpdate, dao.find(toUpdate.id()), "The objects do not match");
    }

    @Test
    void testUpdateChangesFileContent() throws IOException {
        IdentifiableStub toInsert = new IdentifiableStub(22, "data1");
        dao.insert(toInsert);
        IdentifiableStub toUpdate = new IdentifiableStub(22, "data2");
        dao.update(toUpdate);

        String json = Files.readString(PATH_TO_DB.resolve(toUpdate.id()));

        IdentifiableStub fromFile = gson.fromJson(json, IdentifiableStub.class);

        assertEquals(toUpdate, fromFile, "Updated object is not equal to the one expected one");
    }

}